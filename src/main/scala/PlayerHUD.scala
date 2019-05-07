import scalafx.scene.shape.Rectangle
import scalafx.Includes
import scalafx.scene.control.ProgressBar
import scala.math._
import scalafx.scene.text.Text
import scalafx.scene.Node
import scala.collection.mutable.Buffer
import scalafx.scene.Group

//PlayerHUD - olio kokoaa pelaajan HUDin osaset yhteen

object PlayerHUD{

  
  val weaponHud = WeaponHud         //Asevalikko
  val equipmentBox = EquipmentBox   //Hyötytavaralaatikko
  val healthBar = new GameBar("file:src/main/resources/StyleSheets/HealthBarStyle.css",(1, 200), GameWindow.currentGame)
  val energyBar = new GameBar("file:src/main/resources/StyleSheets/EnergyBarStyle.css",(1, 240), GameWindow.currentGame)
  val notificationArea = NotificationArea //Ilmoitusalue
  
  def image:Group = {
    
    val v = Vector(notificationArea.image, weaponHud.image, equipmentBox.image, healthBar.image, energyBar.image).flatten 
    val g = new Group()
    g.children_=(v.toIterable)
    g
  }
  
  def clearAll = {
    weaponHud.empty
    equipmentBox.empty
    notificationArea.clear 
  }

}

//###########################################################################################################################################################

// EquipmentBox näyttää pelaajalle mukaan poimitut hyötytavarat kuten HealthPackit ja mahdollistaa 
// niiden välillä valitsemisen

object EquipmentBox {
  
  def player = GameWindow.player
  val location = (10.0, 80.0)
  val box = new ItemBox(location, GameWindow.currentGame)
  private var selectedIndex = 0
 
  private def possibleContents = player.inventory.values.filter(item => item.isInstanceOf[UtilityItem] && !item.asInstanceOf[UtilityItem].isSpent).toArray
  
  def moveRight = {
    
    possibleContents.lift(selectedIndex + 1) match{
     
      case Some(item:UtilityItem)=>{
        
        selectedIndex += 1
        this.updateItems
        
      }
      
      case None => {
        
        selectedIndex = 0
        this.updateItems
        
      }
      
      case _ => selectedIndex = 0
     }
    }
  
   def moveLeft = {
    
    possibleContents.lift(selectedIndex - 1) match{
     
      case Some(item:UtilityItem)=>{
        
        selectedIndex -= 1
        this.updateItems
        
      }
      
      case None => {
        
        selectedIndex = possibleContents.size-1
        this.updateItems
       
      }
      
        
     }
    } 
   
   def updateItems = {
     
     if(box.item.isDefined && box.item.get.asInstanceOf[UtilityItem].isSpent){
       this.box.removeItem
       player.equippedUtilityItem = None
     }
     
     if (!this.possibleContents.indices.contains(selectedIndex)) selectedIndex = max(this.possibleContents.size-1,0)
       
       
     this.possibleContents.isEmpty match{
  
     case false =>{
     val item = possibleContents(selectedIndex).asInstanceOf[UtilityItem]
     box.insertItem(item)
     player.equipUtilItem(item)
     }
     
     case true => println("Tried to update utilityitems but there were none in player inventory")
   }
     
   }
   
   def empty = {
     
     if (!this.box.isFree) this.box.removeItem
     
   }
   
   def image = box.fullImage
     
}

//###########################################################################################################################################################

// WeaponHud näyttää pelaajalle mukaan poimitut aseet. Sen avulla aseiden välillä voi valita.

object WeaponHud {
  
  private val location = (10, 0.1)
  val weaponBoxes = Vector.tabulate(6)(num => new ItemBox((location._1 + 70 * num, location._2 ), GameWindow.currentGame))
 
  private var selectedBoxNumber = 0
  
  println("weaponBoxes created. " + "Locations are " + weaponBoxes.map(_.locationForSprite) )
  def player = GameWindow.player
  
  def itemsInBoxes = weaponBoxes.map(box => if(box.item.isDefined) box.item.get.name else "Nothing")
  
  def selectBox(boxNumber:Int) = weaponBoxes.lift(boxNumber) match {
    
    case Some(box) => {
      weaponBoxes(boxNumber).select
      player.equipWeapon(box.item.asInstanceOf[Option[Weapon]]) //Jos asevalikon laatikossa on jotain se sisältää varmasti aseen
      if(selectedBoxNumber != boxNumber) weaponBoxes(selectedBoxNumber).deselect
      selectedBoxNumber = boxNumber
    }
    
    case None => {
      println("Going around")
      weaponBoxes(selectedBoxNumber).deselect
      selectedBoxNumber = 0
      weaponBoxes(selectedBoxNumber).select
      player.equipWeapon(weaponBoxes(selectedBoxNumber).item.asInstanceOf[Option[Weapon]])
      }
     }
  
  def selectNext = {
    
    if (selectedBoxNumber == weaponBoxes.size - 1){
       selectBox(0)
       }else{
       selectBox(selectedBoxNumber + 1)
    }
   }
  
  def selectPrevious = {
    
    if (selectedBoxNumber == 0){
       selectBox(weaponBoxes.size - 1)
       }else{
       selectBox(selectedBoxNumber - 1)
    }
    
  }
  
  def updateItems = {
    
    val possibleNewContents: Array[Item] = player.inventory.values.filter(item => item.isInstanceOf[Weapon]).toArray.dropRight(itemsInBoxes.filterNot(_=="Nothing").size)
    var currentBox = 0
   
    if(!possibleNewContents.isEmpty){
   
      for (weapon <- possibleNewContents){
        
       while(!weaponBoxes(currentBox).isFree && currentBox < weaponBoxes.size) {
         currentBox += 1
         
       }
      
      if(currentBox <= weaponBoxes.size-1 && weaponBoxes(currentBox).isFree){
      weaponBoxes(currentBox).insertItem(weapon.asInstanceOf[Weapon])
      
      }
     }
    
    println("HUD Weapons " + itemsInBoxes)
    }else {
    println("Tried to update weapons but none were in player inventory.")
    
    
      
    }
    }
 
  def empty = this.weaponBoxes.foreach(_.removeItem)
  
  def image = weaponBoxes.flatMap(_.fullImage)
  
}

//###########################################################################################################################################################

// Monet palaajan HUDin elementit rakentuvat ItemBox-olioista. 

class ItemBox (val location:(Double, Double), val game:Game) extends UsesGameSprite{
  
  private var containedItem: Option[Item] = None
  private var isSelected: Boolean = false
  
  val sprites = Vector(
   new GameSprite("file:src/main/resources/Pictures/ItemBoxNotSelected.png", None, (60, 60), this, (0,0), None ),  //Tyhjä
   new GameSprite("file:src/main/resources/Pictures/ItemBoxSelected.png", None, (60, 60), this, (0,0), None)  //Valittu tyhjä
 )
  
  
 def isFree = {this.containedItem == None}
 
 def select = {this.isSelected = true}

 def deselect = {this.isSelected = false}
 
 def insertItem(item:Item) = {this.containedItem = Some(item)} 
 
 def removeItem = {this.containedItem = None} 
 
 private def emptyImage:Vector[Rectangle] = isSelected match{
    
    case true => Vector(sprites(1).image)
    case false => Vector(sprites(0).image)
    }
  
 def locationForSprite = Some(this.location)
 
 def fullImage:Vector[Node] = containedItem match{
    
   
   case Some(item:UtilityItem) => {
      val counter = new Text(this.location._1 + 40, this.location._2 + 55, item.amountOfUseTimes.toString())
      counter.setFill(scalafx.scene.paint.Color.White)
      item.sprites(1).overrideLocation = Some(this.location)
      if(item.isSpent) this.removeItem
      Vector(emptyImage(0), item.sprites(1).image, counter )
      
   }
 
    case Some(item) =>{ 
      
      item.sprites(1).overrideLocation = Some(this.location)
      Vector(emptyImage(0), item.sprites(1).image )
      
      }
    
    case None => Vector(emptyImage(0))
    }
 
 def item = this.containedItem
 
 def lookDirectionForSprite = "east"
 
 
 
   } 
//###########################################################################################################################################################

class GameBar(styleSheetPath:String, location:(Double, Double), val game:Game){
  
  private val dimensions = (150.0, 20.0)
  private val bar = new ProgressBar
  
  //Säädetään palkin kokoa
  bar.prefHeight = dimensions._2
  bar.prefWidth = dimensions._1
  
  //Säädetään palkin tyyli
  println("StyleSheet lisäyksen tulos: " + bar.getStylesheets.add(styleSheetPath))
  
  //Annetaan palkin sijainti
  bar.layoutX_=(location._1)
  bar.layoutY_=(location._2)
  
  bar.setProgress(0.5)
    
  def setValue(value:Double) = bar.setProgress(value)  
  
  def image = Vector[Node](bar)
   
  }

//##############################################################################################################################################

object NotificationArea {
  
  private def location = (GameWindow.stage.width.toDouble - (GameWindow.stage.width.toDouble/2),0.1)
  private var timer = 0

  private val backGround = new Rectangle{
    
    x=location._1
    y=location._2
    width = 8000
    height = 25
    fill = scalafx.scene.paint.Color.DarkGray
  }
  
  private var currentMessage = ""
  
  def announce(message:String) = {
    this.timer = 0
    this.currentMessage = message
    
  }
  
  def clear = this.currentMessage = ""
  
  def image = {
   
    this.timer += 1
    if(this.timer> 150) this.clear
   
    Vector[Node](backGround, new Text(location._1 + 20, location._2 +15, currentMessage))
   
  }
  
  
  
  
}








