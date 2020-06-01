
import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle
import scalafx.scene.shape.Rectangle
import scalafx.application.JFXApp
import scalafx.animation.AnimationTimer
import scalafx.scene._
import scalafx.scene.canvas.Canvas
import scala.collection.mutable.Buffer
import scalafx.scene.input._
import javafx.scene.paint.ImagePattern
import scalafx.scene.text._
import scalafx.scene.paint.Color._

object GameWindow extends JFXApp {
  
  
  var gameCamera:Option[GameCamera] = None //GamePos-luokka laskee sijainnit kuvassa tämän suhteen. 
  
 //Luodaan ikkuna
 stage = new JFXApp.PrimaryStage {
   width = 800
   height = 800
   minWidth = 800
   minHeight = 800
   title.value = "Don't Worry About Ammo"
   scene = Menus.MainMenu.scene
 }
 
  //Poistetaan turhat scalafx:n fullscreenexit vinkit
  stage.setFullScreenExitHint("")
  stage.setFullScreenExitKeyCombination(KeyCombination("Jee"))

//NEW GAME
  var currentGame = new Game
  PlayerHUD
  def player = currentGame.player
  this.gameCamera = Some(currentGame.camera) 
 
  //Tänne laitetaan jutut jotka tehdään joka tick
  def changeThings(time:Long):Unit = {
    
    try{
    
   GameWindow.currentGame.fullImage.cursor.value_=(Cursor.None)
  if(!currentGame.isOver){
    
    currentGame.camera.update
    player.updateState
    currentGame.fullImage.content = currentGame.camera.cameraImage // Muuttaa fullimagen sisältöä ja näin animoi asiat
    currentGame.cleanUp
    
    if (!currentGame.backGroundMusic.isPlaying() && !player.isSlowingTime){
      currentGame.backGroundMusic.play(Settings.musicVolume)
      currentGame.timeSlowAmbience.stop()
    }else if(currentGame.backGroundMusic.isPlaying) currentGame.backGroundMusic.volume = Settings.musicVolume
   
    if(player.isSlowingTime){
     currentGame.backGroundMusic.stop()
     if(!currentGame.timeSlowAmbience.isPlaying()) currentGame.timeSlowAmbience.play(Settings.musicVolume)
     if(currentGame.timeSlowAmbience.isPlaying) currentGame.timeSlowAmbience.volume = Settings.musicVolume
     }
   
    if (currentGame.time < 100000) currentGame.time += 1
    else currentGame.time = 0
    
    }else{
      
    this.clock.stop()
    this.menuClock.start()
    Menus.currentMenu = Menus.DeathMenu
    if(!Menus.fullScreenStatus) this.stage.scene = Menus.DeathMenu.scene 
    else{GameWindow.stage.scene = Menus.DeathMenu.scene; GameWindow.stage.setFullScreen(true) }
     }
   
    }catch{
 
      case e:Exception => exceptionScreen("Something is wrong. + \n" + e)
      case _ :Throwable => exceptionScreen("Something is wrong.")
      
    }
   
  }
  //Tämä metodi huolehtii menujen tilanpäivityksestä
  def changeMenus(time:Long) = {
    
    try{
    
    Menus.currentMenu.scene.cursor.value_=(Cursor.Default)
    Menus.currentMenu.refresh
    if(Menus.currentMenu.theme.isDefined && !Menus.currentMenu.theme.get.isPlaying()) Menus.currentMenu.theme.get.play(Settings.musicVolume)
    if(Menus.currentMenu.theme.isDefined && Menus.currentMenu.theme.get.isPlaying()){
      Menus.currentMenu.theme.get.setVolume(Settings.musicVolume) 
     
    }
   
    }catch{
      case e:Exception => exceptionScreen("Something is wrong. + \n" + e)
      case _ :Throwable => exceptionScreen("Something is wrong.")
    }
  }
  
  //Karttatilan päivitys
  def changeMap(time:Long) ={
    
    try{
    
    val cam = this.currentGame.camera
    cam.location.move(cam.xSpeed, cam.ySpeed)
    cam.zoomIn(cam.zInSpeed)
    cam.zoomOut(cam.zOutSpeed)
    
    }catch{
      
      case e:Exception => exceptionScreen("Something is wrong. + \n" + e)
      case _ :Throwable => exceptionScreen("Something is wrong.")
    }
      
    
    
  }
  
  //Näyttö joka näkyy jos peliä suoritettaessa tapahtuu poikkeus. Estää pelin jäätymisen.
  def exceptionScreen(msg:String):Unit = {
    println("Moving to exScreen")
    this.clock.stop()
    this.menuClock.stop()
    this.mapClock.stop()
    val scene = new Scene
    val text = new Text(GameWindow.stage.width.toDouble/2, GameWindow.stage.height.toDouble/2, msg)
    val backGround =  new Rectangle{
          
          fill = Gray
          width = 8000
          height = 8000
          x = -2000
          y = -2000
          
        }
    
    scene.content = (List[Node](backGround, text))
    text.setFill(Red)
    
    GameWindow.stage.scene = scene
    
  }
  
  //Luodaan kello jonka tikittäessä asioita muutetaan. Pelin main loop.
  //Ottaa parametrikseen funktion changeThings, joka sisältää muutokset.
  val clock = AnimationTimer(changeThings)
  
  val mapClock = AnimationTimer(changeMap)
 
  //Kuin ylempi clock mutta päivittää pelin menuja
  val menuClock = AnimationTimer(changeMenus)
  menuClock.start
  
}



