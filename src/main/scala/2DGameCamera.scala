import scala.collection.mutable.ArrayBuffer
import scalafx.scene.shape.Circle
import scalafx.scene.shape.Rectangle
import scalafx.scene.paint.Color._
import scala.math._
import scalafx.scene.transform._
import scalafx.scene.Group
import scalafx.scene.Node
import scalafx.Includes._
import scalafx.scene.transform.Scale
import scalafx.scene.input._
import scalafx.scene.text.Text

//2DGameCamera piirtää pelin kuvan ja liikuttaa piirrettävää ympäristöä

class GameCamera(val followee:Player){
  
  var location = new GamePos((followee.location.locationInGame._1+400,followee.location.locationInGame._2+200), true)
  private var zoomCoefficient:Double = 1    // Zoomauskerroin
  private def zoomTransform = new Scale(zoomCoefficient, zoomCoefficient, GameWindow.stage.width.toDouble/2, GameWindow.stage.height.toDouble/2)
  private var followPlayer = true
  var drawDistance = GameWindow.stage.width.toDouble/2+200
  
  var xSpeed:Double = 0
  var ySpeed:Double = 0
  var zInSpeed:Double = 0
  var zOutSpeed:Double = 0
  
  val game = this.followee.game
  def level = this.game.currentLevel
  
  def entireEnvironment ={
    level.allTiles
  }
  
  def colliderImages = followee.colliders.flatMap(collider => collider.images)
 
  def playerLocationDot = Array(new Circle{
    
    radius = 5
    centerX = followee.location.locationInImage._1
    centerY = followee.location.locationInImage._2
    fill = Green
    
  })
   
  //Seuraava metodi merkitsee tiilten koordinaatit. Hyödynnetään debuggauksessa
  val tileCoordinateImages = drawnEnvironment.map(tile => tile.location).map(coord => new Circle{
    
    fill = Blue
    centerX = coord.locationInImage._1
    centerY = coord.locationInImage._2
    radius = 5
    
  })
  
  //Karttatilan kuvat
  
  private val mapOverLay = Helper.anySpriteFromImage("file:src/main/resources/Pictures/MapModeOverLay.png", (0,0), 4000, 4000)
  private val mapModeCross = Helper.anySpriteFromImage("file:src/main/resources/Pictures/MapModeCross.png", (300,300), 100, 100)
  private val mapModeHelpText = new Text("UP [I] | DOWN [K] | LEFT [J] | RIGHT [L] | ZOOMIN [UP] | ZOOMOUT [DOWN] | RETURN [M] ")
  mapModeHelpText.setFill(White)
  private def mapModeSkin:Option[Node] = if(this.game.isInmapMode) Some(mapOverLay) else None
 
  //Suodattaa koko kentän sisällöstä ne tiilet, jotka näkyvät kullakin hetkellä, ja jotka piirretään
  private def activeEnemies = game.enemies.filter(enemy => enemy.isActive)
  private def drawnEnvironment = entireEnvironment.filter(tile => Helper.absoluteDistance(tile.location.locationInGame, this.location.locationInGame)<= drawDistance|| activeEnemies.exists(enemy => Helper.absoluteDistance(enemy.location.locationInGame, tile.location.locationInGame)<100 ))
  
  //Luo pelin kuvan joka välitetään gamelle ja sen jälkeen GUI:lle
  def cameraImage = {
   
    val moonMan = followee.image
    val arm = followee.arm.get.completeImage
    val projectiles = game.projectiles.map(_.sprite.image)
    val tiles = drawnEnvironment.map(_.tileImage).toBuffer 
    lazy val items = level.itemsInWorld.map(_.sprites(0).image)
    val projectileDebug = game.projectiles.map(projectile => projectile.debugLoc)
    val tileDebug = drawnEnvironment.map(_.debugImage) 
    val levelGeomHitBoxDebug = level.levelGeomHitboxDebug 
    val enemies = game.enemies.filter(enemy => Helper.absoluteDistance(enemy.location.locationInGame, this.location.locationInGame) <= drawDistance ).map(_.image).flatten
    val cursor = game.mouseCursor.image
    
    val worldObjects = tiles ++ items ++ moonMan ++  enemies ++ projectiles  //++ colliderImages ++ tileDebug  ++ playerLocationDot ++projectileDebug 
    val guiElements = new Group(PlayerHUD.image, cursor)
    
    val zoomables = new Group()
    zoomables.children_=(worldObjects.toIterable)
    zoomables.transforms_=(List(zoomTransform))
    
    val all = new Group(level.backGround, zoomables)
    if (mapModeSkin.isDefined) all.children.addAll(mapOverLay, mapModeCross, mapModeHelpText)
    all.children.addAll(guiElements)
    all
  }
  
  //Kameran kuvan päivitys
  def update = {
    game.mouseCursor.trackLocation                                              //Hiiren sijainnin seuranta
    game.projectiles.foreach(_.updateState)                                     //Ammusten tilanpäivitys
    game.enemies.foreach(_.update)                                              //Vihollisten tilan päivitys
    level.moveBackGround(-0.1 * game.player.xSpeed, -0.1 * game.player.ySpeed)  //Taustan siirto
    if(followPlayer) location.teleport(followee.location.locationInGame)        //Pelaajan seuraaminen
    if(zoomCoefficient < 1){
    drawDistance = GameWindow.stage.width.toDouble/2+200 + (1/zoomCoefficient)*(GameWindow.stage.width.toDouble/2)+500      //Piirtoetäisyyden päivitys
    }else{
      drawDistance = GameWindow.stage.width.toDouble/2+200
    }
    
    this.mapModeCross.layoutX_=(this.location.locationInImage._1-350)           //Karttamoodin pitäminen koossa
    this.mapModeCross.layoutY_=(this.location.locationInImage._2-350)
    this.mapModeHelpText.layoutY = GameWindow.stage.height.toDouble -50
    this.mapModeHelpText.layoutX = (GameWindow.stage.width.toDouble/2) -300
 }
  
  //Kameran zoomaus
  def changeZoom(newLevel:Double) = {
    this.zoomCoefficient = newLevel
  }
  
  def zoomIn(amount:Double) = if(this.zoomCoefficient<5) this.zoomCoefficient += amount else this.zoomCoefficient = 5
  
  def zoomOut(amount:Double) = if(this.zoomCoefficient>0.1) this.zoomCoefficient -= amount else this.zoomCoefficient = 0.1
  
  def toggleFreeCamera = if(this.followPlayer == false){
    this.followPlayer = true
  }else{
    this.followPlayer = false
  }
 
}