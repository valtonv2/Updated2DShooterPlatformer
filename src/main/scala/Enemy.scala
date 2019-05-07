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



trait Enemy extends Actor{
  
val location:GamePos  
var HP:Double
var energy = 0.0
var currentAction:String
var isActive:Boolean
var ySpeed:Double = 0.0
var xSpeed:Double = 0.0
val arm:Option[RotatingArm] = None
def isDead = this.HP<=0
def update:Unit

def addSpeedModifier(modifier:Double) = {
    
     this.xSpeed = modifier * this.xSpeed
     this.ySpeed = modifier * this.ySpeed
  }
  
}

class ShooterEnemy(val name:String, val game:Game,  locationX:Double, locationY:Double) extends Enemy {
  
  val location = new GamePos((locationX, locationY), false)
 
  
  var HP = 200
  var actionCounter = 0
  var actionNumber = 0
  var lookDirection = "east"
  var isActive = false //Monet raskaat toiminnot suoritetaan vain kun tämä on true. Vaikutus suorituskykyyn on huima
  var isReadyForNextAction = true
  var moves = false
  var idles = false
  val player = game.player
  val itemDropIndex = Random.nextInt(5)
  def itemDrop = this.inventory.values.toArray.lift(itemDropIndex)
  val newImage = new GameSprite("file:src/main/resources/Pictures/Enemy.png", None, (60, 90), this, (-30, -38), None)
  var currentAction = "idling"
  
  def image = Vector(this.newImage.image)
  //Audio
   val enemyHurtSound = new AudioClip("file:src/main/resources/sound/EnemyHurt.wav")
  //Seuraavat Colliderit huolehtivat vihollisen törmäyksistä
  private val northCollider = new Collider("Enorth", this, 0,  -this.image(0).height.toInt/2, "horizontal")
  private val southCollider = new Collider("Esouth", this, 0,  this.image(0).height.toInt/2, "horizontal")
  private val eastCollider = new Collider("Eeast", this, this.image(0).width.toInt/2, 0, "vertical")
  private val westCollider = new Collider("Ewest", this, -this.image(0).width.toInt/2, 0, "vertical")
  
  var colliders = Vector(northCollider, eastCollider, southCollider, westCollider)
   
 //Seuraava metodi hallitsee vihollisen ampumista laskemalla suunnan kohti pelaajaa ja luomala ammuksen
  def shoot = {
    
    val aimDirection = new DirectionVector(this.location.locationInGame, player.location.locationInGame)
    val projectileSpeed = if(player.isSlowingTime) player.timeSlowCoefficient * 10 else 10

    new Projectile(this.game, aimDirection, projectileSpeed, -15, 0, this )
  }
  
  
  private def randomNumber = Random.nextInt(2)
  
  //update-metodi muodostaa vihollisen "aivot" ja päivittää vihollisen tilaa
  def update = {
   
    //Säädellään aktiivisuutta tehokkuussyistä. Jos vihollinen ei ole aktiivinen se ei ammu
    if (Helper.absoluteDistance(this.location.locationInGame, this.game.player.location.locationInGame) <= (GameWindow.stage.width.toDouble/2)+200) this.isActive = true
    else {this.isActive = false; this.stop}
    
    if (this.xSpeed == 0 && this.ySpeed == 0) this.moves = false
    else this.moves = true
    
    if(this.isDead)  this.itemDrop match{
      
      case Some(item) => this.drop(item)
      case None => println(this.name + " vanished but did not drop any items this time")
      
    }
   
   if(this.isActive && this.moves) this.colliders.foreach(_.update)  //Colliderin päivitys törmäyksien havaitsemista varten
    
    if (this.northCollider.collides) 
      this.ySpeed = 0 
    if (this.southCollider.collides) 
      this.ySpeed = 0  
    
    if (game.time % 100 == 0 && this.isActive) this.shoot //Ampuminen. Tapahtuu tietyin aikavälein kun vihollinen on aktiivinen
    
    if (this.isActive && !this.southCollider.collides && !player.isSlowingTime) this.ySpeed += 1 //Painovoima
    else if(this.isActive && !this.southCollider.collides && player.isSlowingTime) this.ySpeed += 0.1
    
    if(this.location.locationInGame._2 > this.game.currentLevel.dimensions._2) this.takeDamage(9999) 
    
    //Vihollisen toiminnon valinta. Valitaan satunnaisesti kun aikaisempi toiminto valmistuu
    if (this.isReadyForNextAction && this.isActive) {
      
        val chooser = randomNumber
      
        if(chooser == 0){
          this.isReadyForNextAction = false
          this.actionNumber = 0
  
          }else if(chooser == 1){
            this.isReadyForNextAction = false
            this.actionNumber = 1
          }
      
    //Toiminnon suorittaminen 
     }else if(this.actionNumber == 0) {
       this.idle
     }else if(this.actionNumber == 1){
       this.move
     }
  }
  
 // Idle-toiminto pitää vihollisen paikoillaan 15 tickin ajan
  private def idle = {
      
      this.actionCounter += 1
      this.xSpeed = 0
      this.ySpeed = 0
      
      if (this.actionCounter == 15) {
        this.idles = false
        this.isReadyForNextAction = true
        this.actionCounter = 0
        this.xSpeed = 5
      }
      
 }  
  
  //Move- toiminto saa vihollisen liikkeelle
 private def move {
    

    if (this.eastCollider.collides && this.lookDirection == "east")
      this.xSpeed = this.xSpeed * -1 
      this.lookDirection == "west"
    if (this.westCollider.collides && this.lookDirection == "west") 
      this.xSpeed = this.xSpeed * -1
      this.lookDirection == "east"
      
    if (this.actionCounter == 15)
      this.isReadyForNextAction = true
      this.actionCounter = 0
    
    this.location.move(this.xSpeed, this.ySpeed)
    
    this.actionCounter += 1
  }
  
  def takeDamage(amount:Int) = {
    this.HP -= amount
    this.enemyHurtSound.play()
    this.ySpeed += 10
    this.xSpeed += 5
  } 
  
  def stop = {
    this.xSpeed = 0
    this.ySpeed = 0
  } 
  
  
  def lookDirectionForSprite = "east"
  
  //Täytetään vihollisen tavaraluettelo esineillä
  this.inventory += ("Health Pack" -> new HealthPack(this.game, 1))  
  this.inventory += ("Energy Pack" -> new EnergyPack(this.game, 1))
  this.inventory += ("Kitten 5000" -> new SlowFiringWeapon(this.game, None))
  this.inventory += ("Heat Bolter" -> new RapidFireWeapon(this.game, None))
  
}