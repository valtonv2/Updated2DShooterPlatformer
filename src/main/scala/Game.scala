import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import scalafx.geometry._
import scalafx.animation
import scalafx.event._
import scalafx.scene.input._
import scala.math._
import scala.collection.mutable.Buffer
import javafx.scene.paint.ImagePattern
import scalafx.scene.Cursor
import scalafx.scene.transform._
import scalafx.scene.media._


class Game {
  
  var isOver = false
  var isInmapMode = false
  var time = 0
  val fullImage = new Scene
  
  lazy val backGroundMusic = new AudioClip("file:src/main/resources/sound/BackGroundBeat.wav")
  lazy val timeSlowAmbience = new AudioClip("file:src/main/resources/sound/TimeStopAmbience.wav")
  lazy val skyWalkSounds = Helper.getAudioFromFolder("file:src/main/resources/sound/ProjectileBlips", "Blip", 1 to 6, ".wav")
  
  val player = new Player(0, 0, this) //Sijainnin oltava tässä (0,0) jotta levelin spawn-location menee oikein
  
  var enemies = Buffer[Enemy]()
 
  var currentLevel = firstLevel
  var currentLevelName = ""
  
  private def firstLevel = new Level("Large City", 1,"file:src/main/resources/Pictures/LevelImageLarge.png", "file:src/main/resources/Pictures/DarkClouds.png", this)
  private def secondLevel = new Level("Boxes", 2, "file:src/main/resources/Pictures/LevelFloatingBoxes.png", "file:src/main/resources/Pictures/Earth.jpg", this)
  
  val levelCompletionStatus = Array[Boolean](false, false)
  
  val mouseCursor = new MouseCursor(player)
  val camera = new GameCamera(player)
  var projectiles = Buffer[Projectile]()
  
  def cleanUp{
    
    this.projectiles = this.projectiles.filter(_.hasCollided == false)
    this.enemies = this.enemies.filter(enemy => enemy.isDead == false)
    
  }
  
  
  def reset = {
    
    this.isOver = false
    player.HP = player.maxHP
    player.energy = player.maxEnergy
    player.isDead = false
    player.xSpeed = 0
    player.ySpeed = 0
    player.inventory.clear()
    val pos = player.location.locationInGame
    player.location.move(-pos._1, -pos._2)    // Pelaajan sijainti on nollattava
    this.enemies.clear
    this.currentLevel = firstLevel
     
  }
  
  def swapLevel(levelNo:Int){
     player.HP = player.maxHP
     player.energy = player.maxEnergy
     player.inventory.clear()
     player.equippedUtilityItem = None
     player.equippedWeapon = None
     val pos = player.location.locationInGame
     player.location.move(-pos._1, -pos._2)    // Pelaajan sijainti on nollattava
     player.xSpeed = 0
     player.ySpeed = 0
    
     this.enemies.clear()
     
     levelNo match{
       case 1 => this.currentLevel = firstLevel
       case 2 => this.currentLevel = secondLevel
       case _ => println("Tried to switch to level that doesn't exist")
     }
  }
  
  def toggleMap = {
    
    if(!this.isInmapMode){
     this.isInmapMode = true
     
     camera.changeZoom(0.5)
     camera.toggleFreeCamera
     GameWindow.mapClock.start()
    }else{
      this.isInmapMode = false
     GameWindow.mapClock.stop()
      camera.changeZoom(1)
      camera.toggleFreeCamera
    }
    
    
    
  }
  
  //Tässä tehdään kuva joka vältetään GUI:lle
  
  fullImage.content = camera.cameraImage
  
  
 
}

