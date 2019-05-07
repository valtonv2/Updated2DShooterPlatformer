import scala.math._
import scalafx.scene.shape.Rectangle
import scalafx.Includes
import scalafx.scene.media._

//Item on pelin esineitä kuvaava piirreluokka joka sisältää kaikille esineille yhteiset ominaisuudet

abstract class Item(val name:String, var game:Game) extends UsesGameSprite{
  
  val player = game.player
  
  def ID:String
    
  var isInWorld = false
    
  //Kun esine on pelaajan tai vihollisen tavaraluettelossa sijaintia maailmassa ei ole
  //Sijainnille annetaan arvo kun esine on vapaana maailmassa
  var locationInWorld:Option[GamePos] = None 
     
  def sprites:Array[GameSprite] //Sisältää esineen kuvat. 0 = World image 1 = Inventory image
  
  def locationForSprite = {
    
    val pos = this.locationInWorld.getOrElse(new GamePos((0,0),false))
    val value = pos.locationInImage
    Some(value)
    
  }
  
  override def toString = this.name
  
}

//################################################################################################################################################################################

// UtilityItem toimii lajittelun apuna. Sitä hyödynnetään kun maailmasta poimittua esinettä ollaan 
// sijoittamassa tavaraluetteloon.

abstract class UtilityItem(name:String, game:Game, useTimes:Int) extends Item(name, game) {
  
  var amountOfUseTimes = useTimes
  
  def isSpent = (this.amountOfUseTimes == 0)
 
  def use:Unit
  
  def strength:Int
  
  def useTimesLeft = amountOfUseTimes
  
  def sprites:Array[GameSprite]
  
  def lookDirectionForSprite = "east"
  
}

//#################################################################################################################################################################################

//Weapon kuvaa erilaisia aseita.

abstract class Weapon(name:String, var user:Option[Actor], game:Game) extends Item(name, game){
  
  private def cursor = this.game.mouseCursor
  
  val equippedLocation = (game.player.location.locationInGame._1, game.player.location.locationInGame._2)
  
  var isEquipped = false
  
  def fire:Unit
  
  def sprites:Array[GameSprite]
  
  protected def currentTime = game.time
  
  }
 
//##################################################################################################################################################################################

//HealthPack parantaa pelaajaa pelaajan käyttäessä sitä.

class HealthPack(game:Game, useTimes:Int) extends UtilityItem("Health Pack", game, useTimes){
  
  def ID = "HP" + this.useTimesLeft // Hyödynnetään tallentamisessa
  
  val strength = 500 //Parannuksen voimakkuus
  
  
  def use ={ 
    
    player.heal(min(strength, (player.maxHP-player.HP).toInt)) //Pelaajan elinvoiman ei anneta kasvaa yli maksimaalisen määrän
    this.amountOfUseTimes -= 1
    
    PlayerHUD.equipmentBox.updateItems
  }
  
 lazy val sprites = Array(
      
      new GameSprite("file:src/main/resources/Pictures/HealthPack.png", None, (45.0,45.0), this, (0,0), None), //World image
      new GameSprite("file:src/main/resources/Pictures/HealthPack.png", None, (25.0,25.0), this, (15,15), Some(EquipmentBox.location))  //Inventory image
  
  )
  
  
 }
 
//####################################################################################################################################################################################

//EnergyPack kasvattaa pelaajan energian määrää pelaajan käyttäessä sitä.

class EnergyPack(game:Game, useTimes:Int) extends UtilityItem("Energy Pack", game, useTimes){
  
  def ID = "EP" + this.useTimesLeft
  
  val strength = 500 //voimakkuus
  
  def use = { 
    
    player.energy += min(strength, player.maxEnergy-player.energy)//Pelaajan energian ei anneta kasvaa yli maksimaalisen määrän
    this.amountOfUseTimes -= 1 
    PlayerHUD.notificationArea.announce("Used energy pack. Current energy: " + player.energy)  
    PlayerHUD.equipmentBox.updateItems
    
  }
  
  lazy val sprites = Array(
      
      new GameSprite("file:src/main/resources/Pictures/Energypack.png", None, (45.0,45.0), this, (0,0), None), //World image
      new GameSprite("file:src/main/resources/Pictures/Energypack.png", None, (25.0,25.0), this, (15,15), Some(EquipmentBox.location))  //Inventory image
  
  )
 
  
 }
 
//####################################################################################################################################################################################
  
class SlowFiringWeapon(game:Game, var actor:Option[Actor]) extends Weapon("Kitten 5000", actor, game){
  
  val ID = "SFW"
  
  private val cooloffTime = 100
  private val laserSound = new AudioClip("file:src/main/resources/sound/Pew.wav")
  private var lastShotTime = 0
  
  private def projectileSpeed = if(player.isSlowingTime) player.timeSlowCoefficient * 15 else 15 
    
  //Hitaasti ampuva ase voi ampua vain tietyin aikavälein
  def fire = {
    
    val shotTime = currentTime
    if( (shotTime - lastShotTime) >= cooloffTime){
      
      if(this.user.isDefined && this.user.get.arm.isDefined) new Projectile(this.game, user.get.arm.get.direction, projectileSpeed, 0, -20, this.user.get )
      else new Projectile(this.game, new DirectionVector(this.game.player.location.locationInImage, this.game.mouseCursor.location), projectileSpeed, 0, -20, this.user.get )
      
      if(Settings.muteSound == false) this.laserSound.play()
      this.lastShotTime = shotTime
      }
    
  }
  
  lazy val sprites = Array(
      
      new GameSprite("file:src/main/resources/Pictures/SlowFIreWeapon.png", None, (45.0,45.0), this, (0,0), None), //World image
      new GameSprite("file:src/main/resources/Pictures/SlowFIreWeapon.png", None, (25.0,25.0), this, (15,15), None), //Inventory image
      new GameSprite("file:src/main/resources/Pictures/SlowFIreWeapon.png", None, (30.0, 30.0), game.player, (18,-18), None)  //Equipped image
  
  )
  
  def lookDirectionForSprite = "east"
}

//##############################################################################################################################################################################

class RapidFireWeapon(game:Game, var actor:Option[Actor]) extends Weapon("Plasma Bolter", actor, game){
  
  val ID = "RFW"
  
  private val coolOffTime = 2
  private val resetTime = 75
  private val accuracyModifiers = Vector(25, -0.2, 0.15, -0.1, 0)
  private var modifierIndex = 0
  private var lastShotTime = 0
  private val laserSound = new AudioClip("file:src/main/resources/sound/RFWsound.wav")
  
  private def projectileSpeed = if(player.isSlowingTime) player.timeSlowCoefficient * 15 else 15 
  
  //Rapidfireweapon ampuu nopeasti, mutta ensimmäiset laukaukset ovat epätarkkoja
  def fire = {
    
    if(currentTime-lastShotTime >= resetTime) modifierIndex = 0
    
    if (currentTime-lastShotTime >= coolOffTime) {
      
      if(this.user.isDefined) new Projectile(this.game, this.user.get.arm.get.direction, projectileSpeed, 0, -20, this.user.get )
      
      if(Settings.muteSound == false) this.laserSound.play()
      
      lastShotTime = currentTime
      if(modifierIndex < accuracyModifiers.size) modifierIndex += 1
      
       }
    
  }
  
 lazy val sprites = Array(
      
      new GameSprite("file:src/main/resources/Pictures/RapidFireWeapon.png", None, (45.0,45.0), this, (0,0), None), //World image
      new GameSprite("file:src/main/resources/Pictures/RapidFireWeapon.png", None, (35.0,35.0), this, (15,15), None), //Inventory image
      new GameSprite("file:src/main/resources/Pictures/RapidFireWeapon.png", None, (40.0, 40.0), this.game.player, (18,-18), None)  //Equipped image
  
  )
  
  def lookDirectionForSprite = "east"
}

