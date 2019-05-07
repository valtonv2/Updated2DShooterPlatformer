import scalafx.scene.shape.Rectangle
import scalafx.scene.image._
import javafx.scene.paint.ImagePattern
import scalafx.Includes._
import scalafx.scene.paint.Color._
import scalafx.scene.input._
import scalafx.animation._
import scalafx.event._
import scala.collection.mutable.Buffer

class MouseCursor(player:Player){
  
  private var xCoordinate = 0.0
  private var yCoordinate = 0.0
  var isOnLeft = false
  var isOnRight = false
  
  //Ladataan tähtäinkuva muistiin
  private val cursorImage = new scalafx.scene.image.Image("file:src/main/resources/Pictures/Cursor.png")
  private val pattern = new ImagePattern(cursorImage, 0,0,1,1,true)
  
  //Luodaan kuva
  def image = new Rectangle{
    
    width = 30
    height = 30
    fill = pattern
    x = xCoordinate - 15
    y = yCoordinate - 15
    
    }
  
  //Kursorin sijainnin seuranta. Kutsutaan joka tick
  def trackLocation = {
    
    this.player.game.fullImage.onMouseMoved = (event:MouseEvent) => {
      
      this.xCoordinate = event.sceneX
      this.yCoordinate = event.sceneY
      
      val dX = playerXDiff
      val dY = playerYDiff
      
      if(dX <= 0){
       this.isOnLeft = true
       this.isOnRight = false
       }
     
      if(dX > 0){
       this.isOnLeft = false
       this.isOnRight = true  
       }
      
     }
   }
  
  
  private def playerXDiff = this.xCoordinate - player.location.locationInImage._1
  private def playerYDiff = this.yCoordinate - player.location.locationInImage._2
        
  def location = (this.xCoordinate, this.yCoordinate)
  
}