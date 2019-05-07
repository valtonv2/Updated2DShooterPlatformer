import scalafx.scene.shape._
import scalafx.scene.paint.Color._
import scalafx.scene.Scene
import scala.collection.mutable.Buffer
import scala.collection.mutable.ArrayBuffer
import javafx.scene.paint.ImagePattern
import scalafx.Includes._
import scalafx.scene.transform._
import scalafx.scene.Group

class Level(val name:String, val levelNO:Int, layoutPath:String, backGroundPath:String, game:Game) {
  
  //Ladataan kaikki kenttään liittyvät kuvat muistiin
  private val decorativeTileSprite = new scalafx.scene.image.Image("file:src/main/resources/Pictures/DecorativeTexture.png")
  private val decorativeTilePattern = new ImagePattern(decorativeTileSprite, 0,0,1,1,true)
  private val backWallImage = new scalafx.scene.image.Image("file:src/main/resources/Pictures/Tiilitekstuuri.jpg")
  private val backWallPattern = new ImagePattern(backWallImage, 0,0,1,1,true)
  private val floorImage = new scalafx.scene.image.Image("file:src/main/resources/Pictures/floorTexture.png")
  private val floorPattern = new ImagePattern(floorImage, 0,0,1,1,true)
  private val ladderImage = new scalafx.scene.image.Image("file:src/main/resources/Pictures/Ladder.png")
  private val ladderPattern = new ImagePattern(ladderImage, 0,0,1,1,true)
  private val goalImage = new scalafx.scene.image.Image("file:src/main/resources/Pictures/FailTexture.png")
  private val goalPattern = new ImagePattern(goalImage, 0,0,1,1,true)
  
  private val backGroundSprite = new scalafx.scene.image.Image(backGroundPath)
  private val backGroundPattern = new ImagePattern(backGroundSprite, 0.5,0.9,1,1,true)
  
  //Seuraavia muuttujia käytetään taustan liikuttamiseen
  private var bgX = -2000.0
  private var bgY = -4000.0
  
  def backGround = new Rectangle{
    
    width = 8000
    height = 8000
    fill = backGroundPattern
    x = bgX
    y = bgY
    
  }
  
  //Metodi joka siirtää taustaa
  def moveBackGround(dX:Double, dY:Double){
    
    this.bgX += dX
    this.bgY += dY
    
    
  }
    
  //Sisältää kentän kaikki tiilet. Jatkokäsittely ja kuvan luonti tapahtuvat GameCamerassa
  val allTiles = ArrayBuffer[GameTile]()
  
  val ladderTiles = allTiles.filter(_.isLadder)
  
  //LevelCreator-metodi lisää tänne tiilten koordinaatit, joilla on "hitbox"
  //Hyödynnetään esim. Projectile-luokan coillisionDetection-metodissa
  lazy val levelGeometryHitBox = allTiles.filter(_.hasCoillision).map(_.locationForCollider) 
  
  def levelGeomHitboxDebug = levelGeometryHitBox.map(pos => new Circle{
    centerX = pos._1
    centerY = pos._2
    radius = 2
    fill = Red
    
  })
  //Lista maailmassa vapaina olevista esineistä
  val itemsInWorld = ArrayBuffer[Item]()
    
   private val levelImage =  new scalafx.scene.image.Image(layoutPath)
   private val pixelReader  = levelImage.getPixelReader  
    

    
  //Metodi, joka luo kentän kuvan pikselien punaisten väriarvojen perusteella
    def levelCreator = {
    
    for{
      x<-0 until levelImage.width.toInt
      y<-0 until levelImage.height.toInt
      
    }{
     
     
      if ((pixelReader.getColor(x, y).getRed * 255).toInt == 222){                // # = Koristetiili
        val xPoint = (x*50).toDouble
        val yPoint = (y*50).toDouble
        allTiles += new tile(xPoint, yPoint, false, false, decorativeTilePattern, 50, 50)
        
      }else if((pixelReader.getColor(x, y).getRed * 255).toInt == 174){           //Tiili jossa on törmäykset
        val xPoint = (x*50).toDouble
        val yPoint = (y*50).toDouble
        val coordPair = (xPoint, yPoint)
        allTiles += new tile(xPoint, yPoint, true, false, floorPattern, 50, 50)
      
      }else if((pixelReader.getColor(x, y).getRed * 255).toInt == 255){        // Shooterenemy
        
         val xPoint = (x*50).toDouble
         val yPoint = (y*50).toDouble
         game.enemies += new ShooterEnemy("Mursunen", game, xPoint, yPoint)
         
      }else if((pixelReader.getColor(x, y).getRed * 255).toInt == 235){      // Followingenemy
        
         val xPoint = (x*50).toDouble
         val yPoint = (y*50).toDouble
         println("Spawning new enemy")
         game.enemies += new FollowingEnemy("Corrupted Moon Man", game, xPoint, yPoint)
        
      }else if((pixelReader.getColor(x, y).getRed * 255).toInt == 66){                // Rakennuksen taustatiili
        val xPoint = (x*50).toDouble
        val yPoint = (y*50).toDouble
        allTiles += new tile(xPoint, yPoint, false, false, backWallPattern, 50, 50)
        
      
      }else if((pixelReader.getColor(x, y).getRed * 255).toInt == 250){                // Tikkaat
        val xPoint = (x*50).toDouble
        val yPoint = (y*50).toDouble
        allTiles += new tile(xPoint, yPoint, false, true, ladderPattern, 50, 50)
        
      
      }else if((pixelReader.getColor(x, y).getRed * 255).toInt == 100){                // Health Pack
        val xPoint = (x*50).toDouble
        val yPoint = (y*50).toDouble
        val healthPack = new HealthPack(game, 1)
        itemsInWorld += healthPack
        healthPack.isInWorld = true
        healthPack.locationInWorld = Some(new GamePos((xPoint, yPoint), false))
       
       
      }else if((pixelReader.getColor(x, y).getRed * 255).toInt == 12){                // Energy Pack
        val xPoint = (x*50).toDouble
        val yPoint = (y*50).toDouble
        val energyPack = new EnergyPack(game, 1)
        itemsInWorld += energyPack
        energyPack.isInWorld = true
        energyPack.locationInWorld = Some(new GamePos((xPoint, yPoint), false))
      
    
      }else if((pixelReader.getColor(x, y).getRed * 255).toInt == 181){                // Slow Firing Weapon
        val xPoint = (x*50).toDouble
        val yPoint = (y*50).toDouble
        val gun = new SlowFiringWeapon(game, None)
        itemsInWorld += gun
        gun.locationInWorld = Some(new GamePos((xPoint, yPoint), false))
        gun.isInWorld = true
        
      
      
      }else if((pixelReader.getColor(x, y).getRed * 255).toInt == 246){                //RapidFire Weapon
        val xPoint = (x*50).toDouble
        val yPoint = (y*50).toDouble
        val gun = new RapidFireWeapon(game, None)
        itemsInWorld += gun
        gun.locationInWorld = Some(new GamePos((xPoint, yPoint), false))
        gun.isInWorld = true
        
      }else if((pixelReader.getColor(x, y).getRed * 255).toInt == 3){                //Pelaajan spawnaus
       
        val xPoint = (x*50).toDouble
        val yPoint = (y*50).toDouble
        game.player.location.move(xPoint, yPoint) 
      
      
      }else if((pixelReader.getColor(x, y).getRed * 255).toInt == 140){                //Tiili, jonka saavutettuaan pelaaja voittaa tason
       
        val xPoint = (x*50).toDouble
        val yPoint = (y*50).toDouble
        allTiles += new TriggerTile(xPoint, yPoint, false, false, goalPattern, 50, 50, function ={
          
          GameWindow.currentGame.levelCompletionStatus(GameWindow.currentGame.currentLevel.levelNO-1) = true
       
          GameWindow.menuClock.start
          GameWindow.clock.stop()
          PlayerHUD.clearAll                                      //Pelaajan HUD on bugien välttämiseksi tyhjennettävä
          GameWindow.stage.scene = Menus.LevelSelectMenu.scene
          Menus.currentMenu = Menus.LevelSelectMenu
     
          
        })
      }
     }
     println("Luotu " + allTiles.size + " tiiltä")
    }
       
  def gravity(strength:Double) = {
    
    game.player.ySpeed += strength
    
  }
      
  def spawnItem(item:Item, location:(Double, Double)) = {
     
     item.isInWorld = true
     item.locationInWorld = Some(new GamePos((location._1, location._2), false))
     this.itemsInWorld += item
     
   }
  
  def dimensions:(Double, Double) = (this.levelImage.width.toDouble*50, this.levelImage.height.toDouble*50)
    
  
  this.levelCreator //Luodaan taso
  game.currentLevelName = this.name

  
}
//################################################################################################################################################################
  //Yksinkertainen perusrakennuspalikka kentälle.
 
  abstract class GameTile(startX:Double, startY:Double, var hasCoillision:Boolean, var isLadder:Boolean, pattern:ImagePattern, width2:Double, height2:Double) {
   

    
    val location = new GamePos((startX, startY), false)
    val locationForCollider = (startX + 25, startY + 25)
    def tileImage = new Rectangle{
    
      height = height2
      width = width2
      fill = pattern
      x = location.locationInImage._1
      y = location.locationInImage._2
    
    }
    
    def debugImage = new Circle{
      radius= 2
      centerX = location.locationInImage._1
      centerY = location.locationInImage._2
      fill = Green
      
    }

    
 }
  class tile(startX:Double, startY:Double, hasCoillision:Boolean, isLadder:Boolean, pattern:ImagePattern, width2:Double, height2:Double) extends GameTile(startX, startY, hasCoillision,isLadder, pattern, width2, height2)
 
  
  //Tiilen alatyyppi joka suorittaa parametrina annettavan funktion pelaajan koskettaessa sitä
  //Funktiokutsu tapahtuu colliderissa
  class TriggerTile(startX:Double, startY:Double, hasCoillision:Boolean, isLadder:Boolean, pattern:ImagePattern, width2:Double, height2:Double, function: =>Unit) extends GameTile(startX, startY, hasCoillision,isLadder, pattern, width2, height2){
    
    def trigger = function
    
    override def tileImage = new Rectangle{
    
      height = height2
      width = width2
      fill = pattern
      x = location.locationInImage._1
      y = location.locationInImage._2
    
    }
    
  }