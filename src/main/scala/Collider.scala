import scala.math._
import scalafx.scene.shape.Circle
import scalafx.scene.paint.Color.Red

// Collider mahdollistaa "puskurien" luomisen pelaajan ympärille

class Collider(val identifier:String, actor:Actor, xOffset:Int, yOffset:Int, val orientation:String ){
 
 def actorLocationInGame = actor.location.locationInGame
 var collides = false
 var coillisionDistance = 35
  
// Update-metodi tarkkailee colliderin tilaa
 def update = {
  
   val nearbyTiles = actor.game.currentLevel.allTiles.filter(tile => Helper.absoluteDistance(tile.location.locationInGame, actorLocationInGame)<100)
   
   //Seinät 
  if (this.locations.exists(location => nearbyTiles.filter(_.hasCoillision).map(tile => tile.locationForCollider).exists(location2 => Helper.axisDistance(location, location2)._1  <= coillisionDistance && Helper.axisDistance(location, location2)._2 <= coillisionDistance))){
   
    if (this.collides == false){ 
      actor.stop
      println("Bang")
    }
    
    this.collides = true
   
    
    
    //Tikkaat
  }else if(this.locations.exists(location => nearbyTiles.filter(_.isLadder).map(tile => tile.locationForCollider).exists(location2 => Helper.axisDistance(location, location2)._1 <= coillisionDistance + 10 && Helper.axisDistance(location, location2)._2 <= coillisionDistance + 10))){
  
    actor.isOnLadder = true
    this.collides = false
    
    
   //TriggerTiles
  }else if(this.actor.isInstanceOf[Player] && this.locations.exists(location => nearbyTiles.filter(_.isInstanceOf[TriggerTile]).map(tile => tile.locationForCollider).exists(location2 => Helper.axisDistance(location, location2)._1 <= coillisionDistance + 10 && Helper.axisDistance(location, location2)._2 <= coillisionDistance + 10))){
  
    val nearbyTrigger = nearbyTiles.find(_.isInstanceOf[TriggerTile])
    if (nearbyTrigger.isDefined) nearbyTrigger.get.asInstanceOf[TriggerTile].trigger
    
    
    
    //Esineet
    }else if (actor.game.currentLevel.itemsInWorld.exists(item => item.isInWorld && Helper.axisDistance(actor.location.locationInGame, item.locationInWorld.get.locationInGame)._1 <=60 && Helper.axisDistance(actor.location.locationInGame, item.locationInWorld.get.locationInGame)._2 <=90 )){
    val nearbyItem = actor.game.currentLevel.itemsInWorld.find(item=>Helper.axisDistance(actor.location.locationInGame, item.locationInWorld.get.locationInGame)._1<=60 && Helper.axisDistance(actor.location.locationInGame, item.locationInWorld.get.locationInGame)._2<=90)      
    nearbyItem match {
      
      case Some(thing) => actor.pickUp(thing, false)
      case None =>
      }
    
  }else{
   
    this.collides = false
    actor.isOnLadder = false
    
  }
   
  }
  
  def x = actor.location.locationInGame._1 + xOffset
  def y = actor.location.locationInGame._2 + yOffset
  def imgX  = actor.location.locationInImage._1 + xOffset
  def imgY = actor.location.locationInImage._2 + yOffset
  
  //Määrittää sijainnit pelaajan ympärillä
  def locations = {
    val pHeight = this.actor.height.toDouble
    val pWidth = this.actor.width.toDouble
    if (orientation == "horizontal")
      Vector( (this.x-(pWidth/2) + 20 , this.y), (this.x, this.y), (this.x + (pWidth/2) - 20, this.y) )
    else
      Vector( (this.x, this.y - (pHeight/2) + 36 ), (this.x, this.y), (this.x, this.y+(pHeight/2) -11) )
    
    }
  
    //Vastaavat sijainnit kuvan koordinaatistossa 
    def imageLocations = {
    val pHeight = this.actor.height.toDouble
    val pWidth = this.actor.width.toDouble
    if (orientation == "horizontal")
      Vector( (this.imgX-(pWidth/2) + 20 , this.imgY), (this.imgX, this.imgY), (this.imgX + (pWidth/2) - 20, this.imgY) )
    else
      Vector( (this.imgX, this.imgY - (pHeight/2) + 36 ), (this.imgX, this.imgY), (this.imgX, this.imgY+(pHeight/2) - 11 ) )
    
    }
  
  //Colliderian kuvat. Auttavat debuggauksessa
  def images = this.imageLocations.map(location => new Circle{
    
    fill = Red
    centerX = location._1
    centerY = location._2
    radius = 1
    
  })
   
}

