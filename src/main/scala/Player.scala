import scalafx.scene.shape.Rectangle
import scalafx.scene.shape.Circle
import scalafx.scene.Node
import javafx.scene.image._
import javafx.scene.paint.ImagePattern
import scalafx.scene.transform._
import scalafx.Includes._
import scalafx.scene.paint.Color._
import scalafx.scene.input._
import scalafx.animation._
import scalafx.event._
import scala.collection.mutable.Buffer
import scala.math._
import java.io.File
import scalafx.scene.media._
import scala.collection.mutable.ListMap
import scalafx.scene.Group
import scalafx.scene.Cursor


trait Actor extends UsesGameSprite {
  
  val name:String
  var colliders: Vector[Collider]
  val game:Game
  var isWalking = false
  var isShielding = false
  var isOnLadder = false
  var hasDroppedItem = false
  var height = 90
  var width = 60
  
  val arm:Option[RotatingArm]
  //Tavaraluettelo
  val inventory = ListMap[String, Item]()
  var equippedUtilityItem:Option[UtilityItem] = None
  var equippedWeapon:Option[Weapon] = None
  val location:GamePos
  
  def locationForSprite = Some(location.locationInImage)
  def lookDirectionForSprite:String
  def stop: Unit
 // def image:Vector[Rectangle]
  def takeDamage(amount:Int): Unit
  def image:Vector[Node]
  
  //Esineen poimiminen maailmasta
  
  def pickUp(item:Item, isCheatDrop:Boolean) = {
    
    
    if(isCheatDrop){
      this.inventory += (item.name -> item)
      item.isInWorld = false
    }
    
    if(!this.inventory.contains(item.name) && !this.hasDroppedItem && game.currentLevel.itemsInWorld.contains(item) && !isCheatDrop) {  // Tiettyä esinettä voi tavaraluettelossa olla vain 1. UtilityItemien käyttökertojen määrä kasvaa poimittaessa niitä lisää.
      
      this.inventory += (item.name -> item)
      item.isInWorld = false
      game.currentLevel.itemsInWorld -= item
      NotificationArea.announce(this.name + " picked up " + item)
   
   }
    
    if(this.inventory.contains(item.name) && item.isInstanceOf[UtilityItem] && game.currentLevel.itemsInWorld.contains(item)) {
      this.inventory(item.name).asInstanceOf[UtilityItem].amountOfUseTimes += item.asInstanceOf[UtilityItem].amountOfUseTimes
      item.isInWorld = false
      game.currentLevel.itemsInWorld -= item
      
      NotificationArea.announce(this.name + " picked up " + item)
      
      
    }
    
    
    println("Player inventory: " + this.inventory.keys.mkString(", "))
    if(item.isInstanceOf[Weapon]) PlayerHUD.weaponHud.updateItems
    else PlayerHUD.equipmentBox.updateItems
    
  }
  
   def equipWeapon(gun:Option[Weapon]) = {
     this.equippedWeapon = gun
     
     if(gun.isDefined){
       gun.get.user = Some(this)
       gun.get.sprites(2).user = this
     }
     
   }
   def equipUtilItem(thing:UtilityItem) = {
     this.equippedUtilityItem = Some(thing)
   }
   def useUtilItem = this.equippedUtilityItem match {
     
     case Some(item) => item.use
     case None => println("Tried to use item that did not exist")
     
   }
    
   def drop(item:Item) = {
    
    if (this.inventory.values.toArray.contains(item) && !this.hasDroppedItem){
      
      this.hasDroppedItem = true
      item.isInWorld = true
      this.inventory -= item.name
      item.locationInWorld = Some(this.location)
      game.currentLevel.itemsInWorld += item
    
      println(this.name + " dropped " + item.name)
    } else {
      
      println("Item specified did not exist in player inventory")
      
    }
    
  }
}

class Player(startX:Int, startY:Int, val game:Game) extends Actor with UsesAnimatedGameSprite {
  
  val name = "Moon Man"
  val maxHP = 1000.0
  val maxEnergy = 1000.0
  var ySpeed = 0.0
  var xSpeed = 0.0
  var HP = maxHP
  var energy = maxEnergy
  var isDead = false
  var isSlowingTime = false
  val timeSlowCoefficient = 0.1
  var lookDirection = "east"
  
 private var jumpCounter = 0
  
  val location = new GamePos((startX.toDouble, startY.toDouble), false)
  def expMove = location.move(this.xSpeed, this.ySpeed)
  
  //Pelaajan kuvat
  
  private val body = new AnimatedGameSprite("file:src/main/resources/Pictures/MoonmanWalk", "MoonmanWalk", 2 to 6, ".png", None, (60,90), this, (-30,-45), false)
  val arm = Some(new RotatingArm(this, new DirectionVector(this.location.locationInImage, (0,0)))) 
  private val shield = new AnimatedGameSprite("file:src/main/resources/Pictures/ShieldAnimated", "Shield", 1 to 5, ".png", None, (60,90), this, (-30,-45), true)

  
  //Audio
  private val playerHurtSound = new AudioClip("file:src/main/resources/sound/PlayerHurt.wav")
  val shieldSound = new AudioClip("file:src/main/resources/sound/ShieldSound.wav")
  val shieldBounceSound = new AudioClip("file:src/main/resources/sound/ShieldBounce.wav")
  
  /* 
   * Seuraavat Colliderit muodostavat pelaajan sijainnin ympärille "puskurit" jotka tunnistavat
   * niihin kohdistuvat törmäykset. Kukin collider ottaa parametrikseen nimen, pelaajan, etäisyyden 
   * pelaajan sijainnista x ja y - suunnissa ja stringin, joka kertoo onko puskuri vaakasuora vai pystysuora.
   * Kukin collider tarkkailee kolmea sijaintia, jotka ovat pienen välimatkan päässä toisistaan.
   * Kolmea sijaintia käytetään, jotta pelaaja ei tietyin välimatkoin pääsisi "putoamaan" maan sisään.
  */
   
 private val northCollider = new Collider("north", this, 0,  -this.body.spriteHeight.toInt/2+15, "horizontal")
 val southCollider = new Collider("south", this, 0,  this.body.spriteHeight.toInt/2-12, "horizontal")
 private val eastCollider = new Collider("east", this, this.body.spriteWidth.toInt/2 - 20, -30, "vertical")
 private val westCollider = new Collider("west", this, -this.body.spriteWidth.toInt/2 + 20, -30, "vertical")
  
  var colliders = Vector(northCollider, eastCollider, southCollider, westCollider)
  
 
  //Metodi joka muuttaa pelaajan tilaa. Kutsutaan joka tick
  def updateState = {
     
    this.expMove
    
    
    if (this.game.mouseCursor.isOnLeft) this.lookDirection = "west"
    else this.lookDirection = "east"
    
    //Päivittää pelaajan collidereja ja mahdollistaa törmäykset
    this.colliders.foreach(_.update)
    
    if((this.eastCollider.collides || this.westCollider.collides) && this.game.time % 5 == 0) this.isWalking = false
    
    if (this.game.time % 5 == 0 && this.xSpeed == 0 ) this.isWalking = false
     
     //Pelaajaan vaikuttaa painovoima, jos se ei kosketa maata jaloillaan
    if(!this.southCollider.collides && !this.isOnLadder)  this.game.currentLevel.gravity(0.2)
    
    if(this.isShielding && !this.shieldSound.isPlaying() && Settings.muteSound == false){
       this.shieldSound.play(Settings.musicVolume)
        }
    
    if(this.isShielding && !Settings.devMode) this.energy = scala.math.max(0, this.energy-1)
    
    if (this.isSlowingTime && !Settings.devMode) this.energy = scala.math.max(0, this.energy-1)
    
    if(this.isSlowingTime && this.energy == 0) this.toggleSlowTime
    
    if(this.isShielding && this.energy == 0) this.isShielding = false
    
    if (this.isDead) game.isOver = true
    
    this.arm.get.direction.update(this.location.locationInImage, this.game.mouseCursor.location)
      
    PlayerHUD.healthBar.setValue(this.HP / this.maxHP)
    
    PlayerHUD.energyBar.setValue(this.energy / this.maxEnergy)
    
    if(this.location.locationInGame._2 > this.game.currentLevel.dimensions._2){
      println("You fell off map")
      this.takeDamage(9999) 
    }
  }
   
  //Luodaan scalafx:n mukainen kuva
  def image ={ 
  
    val fullPlayerImage ={
      
      if (!this.isShielding) Vector[Node](body.image, arm.get.completeImage) 
      else Vector[Node](body.image, arm.get.completeImage, shield.image) 
    }
    
    fullPlayerImage
    
  }
  
 
 //Seuraava shoot-metodi laukaisee aseen.
  def shoot = this.equippedWeapon match{
    
    case Some(weapon) => if(!this.game.isInmapMode) weapon.fire
    case None => PlayerHUD.notificationArea.announce("You have not equipped a weapon")
  
  }
  
  //Pelaajan vahingoittaminen
  def takeDamage(amount:Int) = {
   if(Settings.muteSound == false) playerHurtSound.play(Settings.musicVolume)
    
    if(Settings.devMode == false) this.HP -= amount
     
    if (this.HP <= 0) this.isDead = true
  }
  
  
  def stop = {
    
    this.xSpeed = 0
    this.ySpeed = 0
  }
  
  //Pelaajan parantaminen
  def heal(amount:Int) = {
    this.HP += amount
    PlayerHUD.notificationArea.announce("Healed. Current HP: " + this.HP)
  }
  
  
  //Ajan hidastaminen. Tämä pienentää liikkuvien asioiden nopeutta.
  //Sallii myös ammusten päällä hyppimisen
  
  private def toggleSlowTime = {
    
    
    
    if(this.isSlowingTime == false && this.energy>0){
    
    this.isSlowingTime = true
    
    this.game.projectiles.foreach(_.addSpeedModifier(timeSlowCoefficient))
    this.game.enemies.foreach(_.addSpeedModifier(timeSlowCoefficient))
    
    
    }else{
      
    
      this.isSlowingTime = false
      this.game.projectiles.foreach(_.addSpeedModifier(1/timeSlowCoefficient))
      this.game.enemies.foreach(_.addSpeedModifier(1/timeSlowCoefficient))
      
      
    }
    
  }
  
  private def useShield = {
    
    if (this.energy>0) this.isShielding = true
    
  }
  
  //Pelaajan hyppy
  private def jump = {
    
    if (this.jumpCounter <= 1){
      this.ySpeed = -8
      this.jumpCounter += 1
    }
    
    if (this.southCollider.collides || Settings.devMode){
      
      this.jumpCounter = 0  
    } 
  }
 
  def lookDirectionForSprite = this.lookDirection
  
  //Pelaajan kontrollit
  
   this.game.fullImage.onKeyPressed = (event:KeyEvent) => {
     
     
     if(!this.eastCollider.collides && event.code == KeyCode.D && this.xSpeed<=10 && !this.game.isInmapMode){
       this.xSpeed = 10
       this.isWalking = true
    
     }else if(this.eastCollider.collides && event.code == KeyCode.D && this.xSpeed<=10 ){
       this.xSpeed = 0

     }
     
     
     if(!this.westCollider.collides && event.code == KeyCode.A && this.xSpeed >= -10 && !this.game.isInmapMode){
     this.xSpeed = -10
     this.isWalking = true

     }else if(this.westCollider.collides && event.code == KeyCode.A && this.xSpeed<=10){
       this.xSpeed = 0
 
 
     }
     
     if(!this.northCollider.collides && event.code == KeyCode.W && this.ySpeed >= -10 && !this.game.isInmapMode ){
         
       if(this.isOnLadder) this.ySpeed = -10
       else this.jump
       
     }else if(this.northCollider.collides && event.code == KeyCode.W && this.ySpeed<=10){
       this.xSpeed = 0
     }
     
     if(!this.southCollider.collides && event.code == KeyCode.S && this.ySpeed <= 10 && !this.game.isInmapMode){
     this.ySpeed = 10
     }else if(this.southCollider.collides && event.code == KeyCode.S && this.ySpeed<=10){
       this.xSpeed = 0
     }
     
     //Suojakenttä
     if(event.code == KeyCode.E){
     this.useShield 
     }
     
     //Ajan hidastus
     if(event.code == KeyCode.Q){
     this.toggleSlowTime
     }
     
     
     //Pause-screen
      if(event.code == KeyCode.Escape){
      GameWindow.clock.stop()
      GameWindow.menuClock.start()
      if(Menus.fullScreenStatus == false) GameWindow.stage.scene = Menus.PauseMenu.scene 
      else{GameWindow.stage.scene = Menus.PauseMenu.scene; GameWindow.stage.setFullScreen(true) }
      Menus.currentMenu = Menus.PauseMenu
    }
     
      //Kamera
      
     if(event.code == KeyCode.M){
     game.toggleMap
     }
     
      if(event.code == KeyCode.L && this.game.isInmapMode){
       this.game.camera.xSpeed = 50
       }
     
     if(event.code == KeyCode.J && this.game.isInmapMode){
       this.game.camera.xSpeed = -50
       }
     
     if(event.code == KeyCode.I && this.game.isInmapMode){
       this.game.camera.ySpeed = -50
       }
     
     if(event.code == KeyCode.K && this.game.isInmapMode){
       this.game.camera.ySpeed = 50
       }
     
      if(event.code == KeyCode.Up && this.game.isInmapMode){
     this.game.camera.zInSpeed = 0.05
   
     }

     if(event.code == KeyCode.Down && this.game.isInmapMode){
     this.game.camera.zOutSpeed = 0.05
    
     } 
      
   }
  
  this.game.fullImage.onKeyReleased = (event:KeyEvent) => {
     
     if(event.code == KeyCode.D && this.xSpeed>0){
       this.xSpeed = 0
       this.isWalking = false
       }
     
     if(event.code == KeyCode.A && this.xSpeed<0){
       this.xSpeed = 0
       this.isWalking = false
       }
     
     if(event.code == KeyCode.W && this.isOnLadder){
       this.ySpeed = 0
      
       }
     
     if(event.code == KeyCode.D && this.isOnLadder){
       this.ySpeed = 0
       }
     
     if(event.code == KeyCode.E){
     this.isShielding = false
     }
     
     if(event.code == KeyCode.X){
     PlayerHUD.equipmentBox.moveLeft
     }
     
     if(event.code == KeyCode.C){
     PlayerHUD.equipmentBox.moveRight
     }
     
     if(event.code == KeyCode.Digit1){
     PlayerHUD.weaponHud.selectBox(0)
     }
    
     if(event.code == KeyCode.Digit2){
     PlayerHUD.weaponHud.selectBox(1)
     }
       
     if(event.code == KeyCode.Digit3){
     PlayerHUD.weaponHud.selectBox(2)
     }
        
     if(event.code == KeyCode.Digit4){
     PlayerHUD.weaponHud.selectBox(3)
     }
         
     if(event.code == KeyCode.Digit5){
     PlayerHUD.weaponHud.selectBox(4)
     }
          
     if(event.code == KeyCode.Digit6){
     PlayerHUD.weaponHud.selectBox(5)
     }
     
     if(event.code == KeyCode.H){
     this.useUtilItem
     }
     
      if(event.code == KeyCode.O){
      //GameTests.execute()
     }
     
     if(event.code == KeyCode.Up){
     this.game.camera.zInSpeed = 0
    
     }

     if(event.code == KeyCode.Down){
     this.game.camera.zOutSpeed = 0
    
     }
    
      if(event.code == KeyCode.L && this.game.isInmapMode){
       this.game.camera.xSpeed = 0
       }
     
     if(event.code == KeyCode.J && this.game.isInmapMode){
       this.game.camera.xSpeed = 0
       }
     
     if(event.code == KeyCode.I && this.game.isInmapMode){
       this.game.camera.ySpeed = 0
      
       }
     
     if(event.code == KeyCode.K && this.game.isInmapMode){
       this.game.camera.ySpeed = 0
       }
    
     
   }
  
  //Ampuminen
  this.game.fullImage.setOnMousePressed((happening:MouseEvent) => {
    this.shoot 
   })
  
  //Hudissa liikkuminen
  
  this.game.fullImage.onScroll = (event:ScrollEvent) => {
    
    if(event.deltaY > 0) PlayerHUD.weaponHud.selectNext
    else PlayerHUD.weaponHud.selectPrevious
    
  }
 def isMovingForSprite = this.isWalking
  
override def toString = this.name + " now at (" + this.location.locationInGame._1 +" ; "+ this.location.locationInGame._2 + ")"
}