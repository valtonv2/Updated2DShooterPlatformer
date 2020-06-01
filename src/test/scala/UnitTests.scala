

import org.scalatest._
import org.scalatest.FlatSpec
import scalafx.application.JFXApp
import scalafx._
import org.scalatest._

//import org.junit.runner.RunWith



//@RunWith(classOf[JUnitRunner])

//TÄRKEÄÄ!!!!!!
//Nämä yksikkötestit eivät toimi tämän tiedoston kautta käynnistettyinä hankalan NullPointerExceptionin vuoksi.
//Testit on ajettava käynnistämällä peli normaalisti ja painamalla pelin aikana nappia o .


object GameTests extends FlatSpec with Matchers {

  val x = GameWindow
 
def testGame = x.currentGame
def testPlayer = testGame.player
def testEnemy = testGame.enemies(testGame.enemies.size-1)
val testItemP = new TestItem
val testItemE = new TestItem

  
class TestItem extends Item("Test Item", testGame){
  
  val ID = "TI"
  val sprites = Array(
      
      new GameSprite("file:Pictures/EnergyPack.png", None, (45.0,45.0), this, (0,0), Some(testPlayer.location.locationInGame)), //World image
      new GameSprite("file:Pictures/EnergyPack.png", None, (25.0,25.0), this, (15,15), Some(testPlayer.location.locationInGame))  //Inventory image
  
  )
  
  def lookDirectionForSprite = "east"
  
}
  
println("------------------RUNNING UNIT TESTS-----------------------")
println("TG: " + testGame)
println("TP: " + testPlayer)
println("TE: " + testEnemy)
println("TI: " + testItemP)
  //PickUp ja Drop-metodit


  
 "Method PickUp" should "pick up item given as a parameter correctly when the user is either enemy  or player" in{
    
    
    testPlayer.pickUp(testItemP, false)
    testEnemy.pickUp(testItemE, false)
    
    assert(!testPlayer.inventory.values.toArray.exists(item => item.name == "Test Item"), "Player picked up item when it did not exist in the game world.")
    assert(!testEnemy.inventory.values.toArray.exists(item => item.name == "Test Item"), "Enemy picked up item when it did not exist in the game world.")
    
    testGame.currentLevel.itemsInWorld += testItemP
    assert(testGame.currentLevel.itemsInWorld(testGame.currentLevel.itemsInWorld.size-1).name == "Test Item", "Unit test is at fault")
    
    testPlayer.pickUp(testItemP, false)
    
    testGame.currentLevel.itemsInWorld += testItemE
    testEnemy.pickUp(testItemE, false)
    
    
    println("Player Inventory: " + testPlayer.inventory.values)
    assert(testPlayer.inventory.values.toArray.contains(testItemP), "Player inventory did not contain item.")
    assert(testEnemy.inventory.values.toArray.exists(item => item.name == "Test Item"), "Enemy inventory did not contain item.")
    
  }

  
  "Method Drop" should "behave correctly when used by an enemy" in{
    
    testEnemy.drop(testItemE)
    
    assert(!testEnemy.inventory.values.toArray.contains(testItemE), "Enemy inventory contained item when it was supposed to be dropped")
    assert(testGame.currentLevel.itemsInWorld.contains(testItemE), "Item was not placed in game world correctly")
    
    
  }
  
  "Method Load Game" should "work properly with a normal save file" in {
    GameWindow.stage.scene = Menus.MainMenu.scene 
    Menus.currentMenu = Menus.MainMenu
    GameWindow.menuClock.start()
    SaveHandler.loadGame("SaveFiles/TestSave.DWAsave")
    Thread.sleep(3000)
    GameWindow.clock.stop()
    
    //Pelaajan testit
    assert(testPlayer.location.locationInGame == (8350, 250), "Player is not where it should be.")
    assert(testPlayer.HP == 1000, "Player HP did not load properly")
    assert(testPlayer.energy == 123, "Player HP did not load properly")
    assert(testPlayer.inventory.isEmpty, "Player inventory was not empty")
    
    //Esineet
    assert(GameWindow.currentGame.currentLevel.itemsInWorld.size == 15, "All items were not loaded")
    
    //Viholliset
    assert(GameWindow.currentGame.enemies.size == 24, "All enemies were not loaded")
    
    //Kenttien avoimuus
    assert(GameWindow.currentGame.levelCompletionStatus(0) == true && GameWindow.currentGame.levelCompletionStatus(1) == false, "Level unlock status was not kept properly")
    
    GameWindow.clock.start()
  }
  
  "Method Load Game" should "work properly when savefile is corrupted" in {
    
    assertThrows[CorruptedSaveFile]{                            //Virheellinen lohkon nimi
      SaveHandler.loadGame("SaveFiles/Corrupted1.DWAsave")
    }
    
    assertThrows[CorruptedSaveFile]{                            //Puuttuva kaksoispiste
      SaveHandler.loadGame("SaveFiles/Corrupted2.DWAsave")
    }
    
    assertThrows[CorruptedSaveFile]{                            //Puuttuva pystyviiva
      SaveHandler.loadGame("SaveFiles/Corrupted3.DWAsave")
    }
    
    assertThrows[CorruptedSaveFile]{                            //Puuttuva hashtag lohkojen välistä
      SaveHandler.loadGame("SaveFiles/Corrupted4.DWAsave")
    }
    
    assertThrows[CorruptedSaveFile]{                            //FILEEND Poissa
      SaveHandler.loadGame("SaveFiles/Corrupted5.DWAsave")
    }
    
  }
  
  println("------------------TESTS ENDED-----------------------")
 
}



