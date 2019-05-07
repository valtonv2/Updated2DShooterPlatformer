import scalafx.scene.shape.Circle
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

class Projectile(val game:Game, var direction:DirectionVector, var speed:Double, locationModifierX:Int, locationModifierY:Int, var shooter:Actor) extends UsesGameSprite {
  
  private val player = game.player
  private val cursor = game.mouseCursor
  private val level = game.currentLevel
  private val projectileRadius = 15
  private var setDir = direction.copy
  private val range = 1500
  private val randomizer = new Random(9001)

  randomizer.shuffle(game.skyWalkSounds)
  
  
  var hasCollided = false
  
  def xCoordinate = shooter.location.locationInGame._1 + locationModifierX
  def yCoordinate = shooter.location.locationInGame._2 + locationModifierY
  
  val location = new GamePos((xCoordinate, yCoordinate), false)
  
  def locationForSprite = Some(location.locationInImage)
  
  def move = {
    
    val dir = setDir.toUnitVect.scalarProduct(speed)
     
      this.location.move(dir.x, dir.y)

  }
  
  val sprite = new GameSprite("file:src/main/resources/Pictures/Projectile.png", None, (30, 30), this, (0,7), None)
  
  def debugLoc = new Circle{
    
    radius = 10
    centerX= location.locationInGame._1
    centerY = location.locationInGame._2
    fill = Purple
    
    
  }
  
  def addSpeedModifier(modifier:Double){
    
   this.speed = modifier * this.speed
    
  }
        
  
  //Huolehtii törmäyksistä
  def coillisionDetection = {
    //Ammus ja seinä
    if(level.levelGeometryHitBox.exists(coordPair => axisDistance(coordPair, this.location.locationInGame)._1 <= projectileRadius + 25  && axisDistance(coordPair, this.location.locationInGame)._2 <= projectileRadius + 25)){
      this.hasCollided = true
      
     }
    
    //Ammus ja vihollinen
    if (game.enemies.exists(enemy => axisDistance(enemy.location.locationInGame, location.locationInGame)._1<=30 && axisDistance(enemy.location.locationInGame, location.locationInGame)._2<=30 && this.shooter != enemy && !enemy.isShielding)){
      this.hasCollided = true
      game.enemies.filter(enemy => axisDistance(enemy.location.locationInGame, location.locationInGame)._1<=30 && axisDistance(enemy.location.locationInGame, location.locationInGame)._2<=30).foreach(_.takeDamage(100))
    
    }else if(game.enemies.exists(enemy => axisDistance(enemy.location.locationInGame, location.locationInGame)._1<=30 && axisDistance(enemy.location.locationInGame, location.locationInGame)._2<=30 && this.shooter != enemy && enemy.isShielding)){
      
      this.setDir = this.setDir.opposite
      this.shooter = this.game.enemies.head
      this.game.player.shieldBounceSound.play(Settings.musicVolume)
      
    }
   
  
  //Ammus ja pelaaja
  if (axisDistance(player.location.locationInGame, this.location.locationInGame)._1 < 30 && axisDistance(player.location.locationInGame, this.location.locationInGame)._2 < 45 && player.isShielding == false && this.shooter != player && !player.isSlowingTime){
    player.takeDamage(333)
    this.hasCollided = true
    
  }else if(axisDistance(player.location.locationInGame, this.location.locationInGame)._1 < 30 && axisDistance(player.location.locationInGame, this.location.locationInGame)._2 < 45 && player.isShielding  && this.shooter != player && !player.isSlowingTime){
    //Kimpoaminen takaisin
    this.setDir = this.setDir.opposite
    this.game.player.shieldBounceSound.play(Settings.musicVolume)
    this.shooter = player
    
   }
  
   else if(player.southCollider.locations.exists(location =>axisDistance(location, this.location.locationInGame)._1 < 15 && axisDistance(location, this.location.locationInGame)._2 < 15) && player.isSlowingTime && this.shooter != player && player.isShielding){
    //Kävely ammusten päällä kun suojakenttä on käytössä
     player.ySpeed = -3
     this.setDir = this.setDir.opposite
     this.shooter = player
   }
  
  else if(player.southCollider.locations.exists(location =>axisDistance(location, this.location.locationInGame)._1 < 15 && axisDistance(location, this.location.locationInGame)._2 < 15) && player.isSlowingTime && this.shooter != player){
    //Kävely ammusten päällä
     player.ySpeed = -3
     
     val blip = game.skyWalkSounds(randomizer.nextInt(5))
     if (!blip.isPlaying()){
       blip.play(Settings.musicVolume)
     }
   }
  
   else if(axisDistance(player.location.locationInGame, this.location.locationInGame)._1 < 30 && axisDistance(player.location.locationInGame, this.location.locationInGame)._2 < 45 && player.isShielding  && this.shooter != player && player.isSlowingTime){
    this.setDir = this.setDir.opposite
    this.game.player.shieldBounceSound.play(Settings.musicVolume)
    this.shooter = player
   }
  
  else if (axisDistance(player.location.locationInGame, this.location.locationInGame)._1 < 30 && axisDistance(player.location.locationInGame, this.location.locationInGame)._2 < 45 && player.isShielding == false && this.shooter != player){
    player.takeDamage(100)
    this.hasCollided = true
    
  }
  
  
}
  
  
  //Metodi, joka päivittää ammuksen tilaa. Kutsutaan joka tick
  def updateState = {
    if (this.game.time%100 == 0 && Helper.absoluteDistance(player.location.locationInGame, this.location.locationInGame)>this.range) this.hasCollided 
    coillisionDetection
    move
    
  }
  
  
   private def axisDistance(a:(Double, Double), b:(Double, Double)) = Helper.axisDistance(a, b)

   
   def lookDirectionForSprite = "east"
   
  
  //Lisätään ammus pelin ammusten listaan. Sen avulla kaikkia ammuksia on helppo hallita
  game.projectiles += this
  
  
}