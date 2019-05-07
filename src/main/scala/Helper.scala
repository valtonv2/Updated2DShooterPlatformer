import scala.collection.mutable.Buffer
import javafx.scene.paint.ImagePattern
import scalafx.scene.shape._
import scalafx.Includes._
import scalafx.scene.transform._
import scalafx.scene.media._
import scala.math._
import scalafx.scene.paint.Color._
import scalafx.scene.Node
import scalafx.scene.Group


object Helper{
  
  
  //Metodi jonka avulla voi helposti lukea useita kuvatiedostoja kerralla
  def getSpritesFromFolder(folderPath:String, fileNameStart:String, fileNumberRange:Range, fileType:String) = {
    
    val fileList = Buffer[javafx.scene.image.Image]()
    for (number <- fileNumberRange){
      
      val filePath = folderPath + "/" + fileNameStart + number.toString() + fileType
      fileList += new scalafx.scene.image.Image(filePath)
      
      println(filePath)
      }
   
    fileList
  }
  
  //Metodi joka muuttaa kuvan teksturoiduksi suorakulmioksi. Käytetään erityisesti pelaajan ja vihollisen tapauksessa
    
  def spriteFromImage(image:javafx.scene.image.Image ) = {
        
        val texture = new ImagePattern( image, -0.25,-0.1,1.5,1.5,true)
        
        new Rectangle{
          
          fill = texture
          width = 60
          height = 90
          x = 400 - 30
          y = 400 - 45
          
        }
        
      }
    
    
  //Metodi joka muuttaa minkä tahansa kuvan teksturoiduksi suorakulmioksi
  def anySpriteFromImage(imagePath:String, location:(Double, Double), spriteWidth:Double, spriteHeight:Double) = {
    val image = new javafx.scene.image.Image(imagePath)
    
    val texture = new ImagePattern( image, 1.0,1.0,1.0,1.0,true)
        
        new Rectangle{
          
          fill = texture
          width = spriteWidth
          height = spriteHeight
          x = location._1
          y = location._2
          
        }
    
  }
  
  def transformToNode[T <: Node](thing:T, transform:List[Transform]) = {
    
    thing.transforms = transform
    thing
    
  }
  
  //Metodi joka mahdollistaa helpon äänitiedostojen käytön
 def getAudioFromFolder(folderPath:String, fileNameStart:String, fileNumberRange:Range, fileType:String) = {
    
    val fileList = Buffer[AudioClip]()
    for (number <- fileNumberRange){
      
      val filePath = folderPath + "/" + fileNameStart + number.toString() + fileType
      fileList += new AudioClip(filePath)
      
      println(filePath)
      }
   
    fileList
  }
  
 //Apumetodi etäisyyksien laskemiseen. Palauttaa suoraviivaisen etäisyyden.
 def absoluteDistance(a:(Double, Double), b:(Double, Double)) = {
    
    val xDiff = abs(a._1 - b._1).toDouble
    val yDiff = abs(a._2 - b._2).toDouble
    
    if(xDiff == 0 && yDiff>0) yDiff
    else if (yDiff == 0 && xDiff>0) xDiff
    else if (xDiff>=1 && yDiff>=1) sqrt(xDiff*xDiff+yDiff*yDiff)
    else sqrt(xDiff*xDiff+yDiff*yDiff)
  }
 
  //Apumetodi etäisyyksien laskemiseen. Erottelee x ja y akselit
  def axisDistance(a:(Double, Double), b:(Double, Double)) = {
    val xDiff = abs(a._1 - b._1).toDouble
    val yDiff = abs(a._2 - b._2).toDouble
    (xDiff, yDiff)
  } 
}
//#################################################################################################################################################################################

trait UsesGameSprite{
  
  var useMirror = false
  def locationForSprite:Option[(Double, Double)]
  def game:Game
  def lookDirectionForSprite:String
  
}

trait UsesAnimatedGameSprite extends UsesGameSprite{
 
  def locationForSprite:Option[(Double, Double)]
  def game:Game
  def lookDirectionForSprite:String
  def isMovingForSprite:Boolean
  
}
//GameSprite-luokka yksinkertaistaa pelin olioiden kuvien laadintaa, muokkaamista ja liikuttamista
//Käytetään aluksi esineiden kanssa ja myöhemmin jos aikaa riittää muuallakin
//Kuva pelissä saa automaattisesti käyttäjänsä sijainnin joten sitä ei tarvitse grafiikkakomponentissa erikseen päivittää

class GameSprite(imagePath:String, var imageStartLocation:Option[(Double, Double)], imageDimensions:(Double, Double), var user:UsesGameSprite, val locationOffset:(Double, Double), var overrideLocation:Option[(Double, Double)]){
  
  
 var spriteWidth = imageDimensions._1
 var spriteHeight = imageDimensions._2
  
 private val texture = new ImagePattern(new scalafx.scene.image.Image(imagePath), 0,0,1,1,true)
 private val transforms = List[Transform]()
  
  
  def normalImage = overrideLocation match{
   
   case Some(location) =>  new Rectangle{
    
    x = location._1 + locationOffset._1
    y = location._2 + locationOffset._2
    width = spriteWidth
    height = spriteHeight
    fill = texture
    
  }
   
   case None  =>  new Rectangle{
    
    x = user.locationForSprite.get._1 + locationOffset._1
    y = user.locationForSprite.get._2 + locationOffset._2
    width = spriteWidth
    height = spriteHeight
    fill = texture
    
  }
 }
 
 private def mirrorImage = {
   val img = normalImage
   Helper.transformToNode(img, this.mirrorRotate)
   img
 }
 
 def image = this.user.lookDirectionForSprite match {
   case "east" => normalImage
   case _ => {
     this.mirrorImage
   }
 }
 
 
 def changeSize(newDimensions:(Double, Double)) = {
   this.spriteWidth = newDimensions._1
   this.spriteHeight= newDimensions._2
   }
 
 def rotate(amount:Double, pivot:(Double, Double)) = {
   
   this.image.transforms.add(new Rotate(amount, pivot._1, pivot._2))
   
 }
 
 private def userSpriteLocation = user.locationForSprite.getOrElse((0.0,0.0))
 
  private def mirrorRotate = List(new Rotate(180.0, userSpriteLocation._1, userSpriteLocation._2 , 0, Rotate.YAxis))
}

//##########################################################################################################################################################################################################

class AnimatedGameSprite(imageFolderPath:String, fileNameStart:String, fileNumberRange:Range, fileType:String, val imageStartLocation:Option[(Double, Double)], imageDimensions:(Double, Double), user:UsesAnimatedGameSprite, val locationOffset:(Double, Double), isAlwaysMoving:Boolean){
  
  private val images = Helper.getSpritesFromFolder(imageFolderPath, fileNameStart, fileNumberRange, fileType)
  private val textures = images.map(image =>new ImagePattern(image, 0,0,1,1,true) )
 

  private var time = 0
  private var spriteIndex = 0
  var spriteWidth = imageDimensions._1
  var spriteHeight = imageDimensions._2
  
  private def updateCurrentSpriteNumber = {
    
    if (this.time % 5 == 0 && spriteIndex < textures.size-1 && (this.user.isMovingForSprite || this.isAlwaysMoving)) {
      
      spriteIndex += 1
     
      
    }else if ((this.time % 5 == 0 && spriteIndex == textures.size-1) || (!this.user.isMovingForSprite && !this.isAlwaysMoving)){
      
      spriteIndex = 0
      
      }
   
    }
  
  def normalImage = new Rectangle{
    
    x = user.locationForSprite.get._1 + locationOffset._1
    y = user.locationForSprite.get._2 + locationOffset._2
    width = spriteWidth
    height = spriteHeight
    fill = textures(spriteIndex)  //Blue
    
  }
  
  
 private def mirrorImage = {
   
   val orig = this.normalImage
   Helper.transformToNode(orig, this.mirrorRotate)
   
  }
  
  def image = {
   
    time += 1 
    updateCurrentSpriteNumber
    user.lookDirectionForSprite match{
      
      case "east" => normalImage
      case "west" => mirrorImage
      
    }
   
    
  }
 
 
 def changeSize(newDimensions:(Double, Double)) = {
   this.spriteWidth = newDimensions._1
   this.spriteHeight= newDimensions._2
   }
 
  private def mirrorRotate = List(new Rotate(180.0, user.locationForSprite.get._1 ,user.locationForSprite.get._2, 0, Rotate.YAxis))
  

  
}

//###########################################################################################################################################################################

//Actoreille saatavilla oleva kääntyvä käsi. Huolehtii käden ja aseen käännöstä. Toistaiseksi vain pelaajan käytössä
class RotatingArm(user:Actor,val direction:DirectionVector){
  
 private val armImage = new GameSprite("file:src/main/resources/Pictures/MoonmanHand.png", None, (40, 25), user, (-5, -13), None)
 private val armRotate = new Rotate(0.0, pivotPoint._1, pivotPoint._2, 400)
  
 private def pivotPoint = user.location.locationInImage
  
  def completeImage = {
   
    armRotate.angle = this.direction.angle * 50
    armRotate.pivotX = pivotPoint._1
    armRotate.pivotY = pivotPoint._2
   
    val group = user.equippedWeapon match{
    
    case Some(weapon) => new Group(armImage.image, weapon.sprites(2).image)
    case None => new Group(armImage.image)
    
  }
    
    
   user.lookDirectionForSprite match{
      
      case "east" => group.transforms.addAll(armRotate)
      case _ => group.transforms.addAll(armRotate)
      
      } 
      
      

    group
    
  } 
  
}

//#########################################################################################################################################################################

//Luokka DirectionVector tarjoaa yksinkertaisemman tavan käsitellä suuntia esim ammusten tapauksessa
class DirectionVector(var originalStartPoint:(Double, Double), var originalEndPoint:(Double, Double)){
  
  var x = originalEndPoint._1 - originalStartPoint._1
  var y = originalEndPoint._2 - originalStartPoint._2
  
  def isTowardsLeft = this.x<0
  def isTowardsRight = this.x>0
  
  def length = {
    
    if(x != 0 && y != 0) sqrt(x*x + y*y)
    else if(x==0) y
    else x
    
  }
  
  def toUnitVect:DirectionVector = { //Metodi muuttaa vektorin yksikkövektoriksi. Uuden vektorin alkupiste on vanhan alkupiste
    
    val length = this.length
    
    if(length == 1) this
    else{
      this.x = this.x/length
      this.y = this.y/length
      this
      
    }
  }
  
  def angle = {
    atan(y/x)
  }
    
  def opposite:DirectionVector = {
    
    new DirectionVector(this.originalEndPoint, this.originalStartPoint)
    
    }
  
  def sum(x:DirectionVector):DirectionVector = {
    
    new DirectionVector(this.originalStartPoint, x.originalEndPoint)
    
  }
  
  def scalarProduct(x:Double):DirectionVector = {
    
    new DirectionVector(this.originalStartPoint, (this.originalStartPoint._1 + x*this.x, this.originalStartPoint._2 +x*this.y))
    
    }
  
  def update(newStart:(Double, Double), newEnd:(Double, Double)){
    
    this.originalStartPoint = newStart
    this.originalEndPoint = newEnd
    
    x = originalEndPoint._1 - originalStartPoint._1
    y = originalEndPoint._2 - originalStartPoint._2
    
  }
  
  def copy = new DirectionVector(this.originalStartPoint, this.originalEndPoint)
  
  
}

//###########################################################################################################################################################################
  
  //Luokan GamePos tarkoitus on helpottaa pelin asioiden sijaintien käsittelyä. Sen avulla pelin varsinaisten koordinaattien ja kuvakoordinaattien välillä vaihtelu on helppoa.
  //Luokka otetaan käyttöön myöhemmin jos aikaa jää
  
class GamePos(inGameCoordinates:(Double, Double), val isCenter:Boolean){
 
 def center:Option[GameCamera] = GameWindow.gameCamera

 private var inGameX = inGameCoordinates._1
 private var inGameY = inGameCoordinates._2
 var playerHeightOffset = -10
  
 def locationInGame = (inGameX, inGameY) 
 
 //Jos jonkin asian sijainti muuttuu pelissä, sen sijainti muuttuu kuvassa. Pelaaja on poikkeus.
 
 
 def locationInImage:(Double, Double) = center match{
   case Some(center) =>{
    
     if (!this.isCenter){(inGameX-center.location.locationInGame._1+center.location.locationInImage._1, inGameY - center.location.locationInGame._2 + center.location.locationInImage._2 + playerHeightOffset)}
     else (GameWindow.stage.width.toDouble/2 ,GameWindow.stage.height.toDouble/2)
   }
   case None => (0.0,0.0)
 }
  
 def move(dx: Double, dy: Double) = {
   this.inGameX += dx
   this.inGameY += dy
 }
 
 def teleport(newLoc:(Double, Double)) = {
   this.inGameX = newLoc._1
   this.inGameY = newLoc._2
 }
 
 def zero = {
   this.inGameX = 0
   this.inGameY = 0
   
 }
  
}
  
  
  
  
