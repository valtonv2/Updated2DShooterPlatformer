import scalafx.scene.shape.Rectangle
import scalafx.scene.image._
import javafx.scene.paint.ImagePattern
import scalafx.Includes._
import scalafx.scene.paint.Color._
import scalafx.scene.input._
import scalafx.animation._
import scalafx.event._
import scala.collection.mutable.Buffer
import scala.math._
import scala.util.Random
import scalafx.scene.media._
import scalafx.scene.Node
import scalafx.scene.media.AudioClip

class FollowingEnemy(val name:String, val game:Game,  locationX:Double, locationY:Double) extends Enemy with UsesAnimatedGameSprite {
  
  val location = new GamePos((locationX, locationY), false)
  var HP = 500.0
  energy = 500
  var isActive = false
  var currentAction = ""
  private var jumpCount = 0
  
  //Vihollisen kuva
  private val body = new AnimatedGameSprite("file:src/main/resources/Pictures/CorruptedMoonmanWalk", "MoonmanWalk", 2 to 6, ".png", None, (60,90), this, (-30,-45), false)
  override val arm = Some(new RotatingArm(this, new DirectionVector(this.location.locationInImage, this.game.player.location.locationInImage))) 
  private val shield = new AnimatedGameSprite("file:src/main/resources/Pictures/ShieldAnimated", "Shield", 1 to 5, ".png", None, (60,90), this, (-30,-45), true)
  var lookDirectionForSprite: String = "east"
  var isMovingForSprite = false
  
  //Vihollisen Colliderit
  private val northCollider = new Collider("Enorth", this, 0,  -this.body.spriteHeight.toInt/2 +15, "horizontal")
  private val southCollider = new Collider("Esouth", this, 0,  this.body.spriteHeight.toInt/2 -12, "horizontal")
  private val eastCollider = new Collider("Eeast", this, this.body.spriteWidth.toInt/2, -30, "vertical")
  private val westCollider = new Collider("Ewest", this, -this.body.spriteWidth.toInt/2, -30, "vertical")
  var colliders:Vector[Collider] = Vector(northCollider, eastCollider, southCollider, westCollider)
  
  //Audio
  private val shieldSound = this.game.player.shieldSound
  private val hurtSound = new AudioClip("file:src/main/resources/sound/CorruptedHurt.wav")
  
  def update:Unit = {
    
    if(!this.isActive && this.absDistToPlayer <= (GameWindow.stage.width.toDouble/2)+200) this.isActive = true //Aktiivisuuden säätely
    else if(this.absDistToPlayer > (GameWindow.stage.width.toDouble/2)+200) this.isActive == false
    
     //Aina suoritettavat toiminnot
    
    this.colliders.foreach(_.update)
    if(!this.southCollider.collides) this.ySpeed += 1
    this.arm.get.direction.update(this.location.locationInImage, this.game.player.location.locationInImage)
    if(this.isShielding) this.energy -= 5
    if(this.isShielding && !this.shieldSound.isPlaying) this.shieldSound.play()
    if(this.energy <= 0 ) this.isShielding = false
    if(this.location.locationInGame._2 > this.game.currentLevel.dimensions._2) this.takeDamage(9999) 
    
     //Esineiden pudottaminen
    try{
    if(this.isDead) this.drop(this.inventory.values.head)
    }catch{
      case x:NoSuchElementException => println("FollowingEnemy item drop is acting up.")
    }
    //Kun vihollinen on aktiivinen
    if(this.isActive){
      
      if(this.playerLocator._1 == "left" && !this.isStuck) this.xSpeed = -2
      if(this.playerLocator._1 == "right" && !this.isStuck) this.xSpeed = 2
      
      this.move
      
      if(this.energy >0 && game.projectiles.exists(bolt => Helper.absoluteDistance(this.location.locationInGame, bolt.location.locationInGame) < 300 && bolt.shooter != this)) this.shieldOn
      else this.shieldOff
      
    }
    
    //Kun vihollinen ei ole aktiivinen
    if(!this.isActive){
      this.stop
    }
    
   
  }

//Vihollisen toiminnot
//#########################################################################################################################################################
 
  
  
 private def playerLocator:(String, String) = {
    
    val xDiff = this.distToPlayer._1
    val yDiff = this.distToPlayer._2
    
    var xStatus = ""
    var yStatus = ""
    
    if(xDiff < -50)  xStatus = "left"
    else if(xDiff > 50) xStatus = "right"
    else xStatus = "nearby"
      
    if(yDiff < -5) yStatus = "above"
    else if(yDiff > 5) yStatus = "below"
    else yStatus = "onlevel"
    
    (xStatus, yStatus)
      
  }
  
 private def shoot = this.equippedWeapon match{
    
    case None => this.equipWeapon(Some(new RapidFireWeapon(this.game, Some(this))))
    case Some(gun) => if(this.game.time % 50 == 0) gun.fire
    
  }
  
 def stop = {
    this.xSpeed = 0
    this.ySpeed = 0
    this.isMovingForSprite = false
  } 
 
 
 //Move metodi hallitsee tämän vihollisen liikkumista playerlocatorin tietojen avulla
  private def move = {
    
    if(!this.isStuck && ((this.playerLocator._1 != "nearby") || ((this.playerLocator._2 != "onlevel") || !this.isOnLadder))){
      
      
      this.location.move(this.xSpeed, this.ySpeed)
     
      this.isMovingForSprite = true
      
    }else this.stop
    
    
    if(this.xSpeed<0) this.lookDirectionForSprite = "west"
    if(this.xSpeed>0) this.lookDirectionForSprite = "east"
    
    if(this.northCollider.collides) this.stop
    
    if(this.eastCollider.collides ^ this.westCollider.collides) this.jump(10)
    
    if(this.isOnLadder && this.playerLocator._2 == "above") this.ySpeed = -5
    else if(!this.isOnLadder && this.playerLocator._1 == "nearby" && this.playerLocator._2 == "above") this.jump(10)
    
    this.shoot
    
     if(this.isStuckBelow){
       
        location.move(0, -1)
       // Pelastetaan jumiutunut vihollinen
     }
    
    if(this.isStuckAbove){
      
      this.location.move(0, 7)
      
    }
  }
  
  private def jump(strength:Int) = {
    
   if(this.jumpCount < 3){
     this.ySpeed = -strength
     this.jumpCount += 1
   }else if(this.southCollider.collides){
     this.jumpCount = 0
     this.ySpeed = -strength
     this.jumpCount += 1
   }
  }
  
  private def shieldOn = if(this.energy > 0) this.isShielding = true
  private def shieldOff = this.isShielding = false
    
//#########################################################################################################################################################

  def image:Vector[Node] = if(!this.isShielding) Vector(body.image, arm.get.completeImage) else Vector(body.image, arm.get.completeImage, shield.image)
  
  def takeDamage(amount:Int):Unit = {
    
    this.HP -= amount
    this.jump(5)
    this.hurtSound.play()
    this.xSpeed -= 10
    
   
   
    
  }
  
  //Metodi, joka vähentää vihollisen energiaa. Käytetään pelin lataamisen yhteydessä
  def useEnergy(amount:Int):Unit = {
    this.energy -= amount
  }
  
 def isStuck = isStuckBelow || isStuckAbove
  
 private def isStuckBelow = this.eastCollider.collides && this.westCollider.collides && this.southCollider.collides
  
 private def isStuckAbove = this.eastCollider.collides && this.westCollider.collides && this.northCollider.collides
  
 private def distToPlayer = (this.game.player.location.locationInGame._1 - this.location.locationInGame._1, this.game.player.location.locationInGame._2 - this.location.locationInGame._2)
  
 private def absDistToPlayer = Helper.absoluteDistance(this.game.player.location.locationInGame, this.location.locationInGame)

 
 //Lisätään esine vihollisen tavaraluetteloon
 this.inventory += ("Health Pack" -> new HealthPack(this.game, 5))  
 
}