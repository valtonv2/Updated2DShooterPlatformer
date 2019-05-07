import scala.io.Source
import java.io.File
import java.io.PrintWriter

object SaveHandler{
  
  def game = GameWindow.currentGame
  
  /*Metodi joka kerää tallennettavan tiedon nykyisestä pelistä ja kirjoittaa sen tallennustiedostoon
   *Kerättävät tiedot:
   * Pelaajan sijainti pelin koordinaatistossa
   * Jäljellä olevien vihollisten sijainnit pelin koordinaatistossa ja tyypit
   * Pelaajan tavaraluettelossa olevat esineet
   * Kenttien avoimuustilanne
   * Kenttä jossa pelaaja oli edellisen pelin loppuessa
   */
 def saveGame(filePath:String) = {
    
    //Tallennettavan datan keräys
    val playerLoc = game.player.location.locationInGame
    val playerInv = game.player.inventory.values.toArray
    val playerHP = game.player.HP
    val playerEnergy = game.player.energy
    val followingEnemies = game.enemies.filter(_.isInstanceOf[FollowingEnemy])
    val followingEnemyData = followingEnemies.map(enemy => (enemy.HP, enemy.energy)).zip(followingEnemies.map(enemy => enemy.location.locationInGame))
    val enemyData = game.enemies.filter(enemy => !enemy.isInstanceOf[FollowingEnemy]).map(_.location.locationInGame).zip(game.enemies.map(_.HP))
    val levelUnlockStatus = game.levelCompletionStatus
    val lastLevel = game.currentLevel.name
    val levelItems = game.currentLevel.itemsInWorld
    val levelItemCoords = levelItems.map(_.locationInWorld.get.locationInGame)
    
    //Muokkaus muotoon joka kirjoitetaan tiedostoon
    
    /* Player:
     * LOC: 25|50
     * STAT: 450|900
     * INV:HP|HP|EP|SFW|RFW
     * 
     * Enemy:
     * STAT:25|50|900,35|40|200,11|60|400
     * 
     * Level:
     * UNS:true|false
     * LLN:LargeCity
     * LI: HP|25|4005,RFW|679|456
     * 
     * FILEEND
     */
    
    
    var playerPart = "Player:\nLOC:" + playerLoc._1+"|"+playerLoc._2+ "\nSTAT:"+playerHP+"|"+playerEnergy+"\nINV:"
    if(!playerInv.isEmpty){
      playerInv.foreach(item=> playerPart += item.ID+"|")
      playerPart += "\n#"
    }
    else playerPart += "empty" + "\n#"

    
    var enemyPart = "Enemy:\nSTAT:"
    val enemies = enemyData.map(clump => (clump._1._1.toString +"|" + clump._1._2.toString+"|"+clump._2))
    if(!game.enemies.isEmpty) enemyPart = enemyPart + enemies.mkString(",")  + "\n#"
    else enemyPart = enemyPart + "empty" + "\n#"
    
    var followingEnemyPart = "Followingenemy:\nSTAT:"
    val followingenemies = followingEnemyData.map(clump => (clump._1._1.toString +"|" + clump._1._2.toString+"|"+clump._2._1+"|"+clump._2._2+"|"))
    if(!followingEnemyData.isEmpty) followingEnemyPart = followingEnemyPart + followingenemies.mkString(",")  + "\n#"
    else followingEnemyPart = followingEnemyPart + "empty" + "\n#"
    
    var levelPart = "Level:\n" +"UNS:"+ levelUnlockStatus.mkString("|") +"\n" + "LLN:" + lastLevel +"\nLI:"
    val levItemData = if (!levelItems.isEmpty) levelItems.zip(levelItemCoords).map(clump => clump._1.ID + "|" + clump._2._1 +"|" + clump._2._2).mkString(",") else "empty" 
    levelPart = levelPart + levItemData + "\n#"
    
    
    val fullString = playerPart + "\n" + enemyPart + "\n"+ followingEnemyPart + "\n" + levelPart + "\n" + "FILEEND"
    
    println(fullString)
    
    val writer = new PrintWriter(new File(filePath))
    writer.write(fullString)
    writer.close()
    
  }
  
 //LoadGame lukee dataa tallennustiedostosta ja muokkaa pelioliota sen perusteella
 
 def loadGame(filePath:String):Boolean = {
  
   //Luetaan data ensin String-muotoon seuraaviin muuttujiin
    var playerLoc:Option[Array[String]] = None    
    var playerInv:Option[Array[String]] = None
    var playerHP:Option[String] = None
    var playerEnergy:Option[String] = None
    var enemyData:Option[Array[String]] = None
    var followingEnemyData:Option[Array[String]] = None
    var levelUnlockStatus:Option[Array[String]] = None
    var levelItems:Option[Array[String]] = None
    var lastLevel:Option[String] = None
    
    def fullData = Array(playerLoc, playerInv, playerHP, playerEnergy, enemyData, followingEnemyData, levelUnlockStatus, lastLevel, levelItems)
   
   val reader = Source.fromFile(filePath).getLines()
   var currentLine =""
   
   var isReadingPlayerData = false
   var isReadingEnemyData = false
   var isReadingFollowingEnemyData = false
   var isReadingLevelData = false
  
   
   while(currentLine != "FILEEND" && currentLine != null){
     
     val line = try{reader.next().trim()}catch{case _:Throwable =>throw CorruptedSaveFile("Reached null without file end")}
     currentLine = line
     
    if(!isReadingPlayerData && !isReadingEnemyData && !isReadingLevelData && !isReadingFollowingEnemyData) currentLine match{
       case "Player:" =>      {isReadingPlayerData = true; isReadingEnemyData = false; isReadingLevelData=false; isReadingFollowingEnemyData = false}
       case "Enemy:" =>       {isReadingPlayerData = false; isReadingEnemyData = true; isReadingLevelData=false; isReadingFollowingEnemyData = false}
       case "Followingenemy:"=>{isReadingPlayerData = false; isReadingEnemyData = false; isReadingLevelData=false; isReadingFollowingEnemyData = true}
       case "Level:" => {isReadingPlayerData = false; isReadingEnemyData = false; isReadingLevelData=true; isReadingFollowingEnemyData = false}
       case "FILEEND" => currentLine = "FILEEND"
       case x:String => throw CorruptedSaveFile("Data chunk names corrupted: " + x)
       case _ => throw CorruptedSaveFile("Data chunk names corrupted")
     }
     
     else if(isReadingPlayerData) currentLine.split(":")(0) match{
       case "LOC" => playerLoc = Some(line.split(":")(1).split('|'))
       case "STAT" =>{playerHP = Some(line.split(":")(1).split('|')(0)); playerEnergy = Some(line.split(":")(1).split('|')(1))}
       case "INV" => if(line.split(":")(1) != "empty") playerInv = Some(line.split(":")(1).split('|')) else playerInv = Some(Array("empty"))
       case "#"  => isReadingPlayerData = false
       case x:String => throw CorruptedSaveFile("Player data corrupted: " + x)
       case _ => throw CorruptedSaveFile("Player data corrupted")
     }
       
     else if(isReadingEnemyData) currentLine.split(":")(0) match{
       case "STAT" => if(line.split(":")(1) != "empty") enemyData = Some(line.split(":")(1).split(",")) else enemyData = Some(Array("empty"))
       case "#"  => isReadingEnemyData = false
       case x:String => throw CorruptedSaveFile("Enemy data corrupted: " + x)
       case _ => throw CorruptedSaveFile("Enemy data corrupted")
     }
     
      else if(isReadingFollowingEnemyData) currentLine.split(":")(0) match{
       case "STAT" => if(line.split(":")(1) != "empty") followingEnemyData = Some(line.split(":")(1).split(",")) else followingEnemyData = Some(Array("empty"))
       case "#"  => isReadingFollowingEnemyData = false
       case x:String => throw CorruptedSaveFile("Enemy data corrupted: " + x)
       case _ => throw CorruptedSaveFile("Enemy data corrupted")
     }
     
     else if(isReadingLevelData) currentLine.split(":")(0) match{
       case "UNS" => levelUnlockStatus = Some(line.split(":")(1).split('|'))
       case "LLN" => lastLevel = Some(line.split(":")(1))
       case "LI"  => levelItems = Some(line.split(":")(1).split(","))
       case "#"  => isReadingLevelData = false
       case x:String => throw CorruptedSaveFile("Level data corrupted: " + x)
       case _ => throw CorruptedSaveFile("Level data corrupted")
     }
   }
  
    //Huolehditaan siitä että kaikki on saatu luettua
    if(fullData.forall(_.isDefined)) println("Everything loaded into variables")
    else throw CorruptedSaveFile("Something was not loaded correctly")
    
    //Tarkastetaan myös datan eheys
    def isValidDouble(s:String) = {
      val tested = scala.util.Try{s.toDouble}.toOption
      if(tested.isDefined) true
      else false 
    }
    
    def isValidBoolean(s:String) = {
      val tested = scala.util.Try{s.toBoolean}.toOption
      if(tested.isDefined) true
      else false 
    }
    
    if (playerLoc.get.size != 2 || !isValidDouble(playerLoc.get(0)) || !isValidDouble(playerLoc.get(1))) throw CorruptedSaveFile("Player location data was not valid.")
    else println("Player LOC data ok")  
    
    if (!isValidDouble(playerHP.get) || !isValidDouble(playerEnergy.get)) throw CorruptedSaveFile("Player status data was not valid.")
    else println("Player status data ok")  
    
    if (!enemyData.get.exists(string => string == "empty") && (!enemyData.get.map(clump => clump.split('|')).forall(_.size == 3) || !enemyData.get.map(clump => clump.split('|')).flatten.forall(isValidDouble(_)))) throw CorruptedSaveFile("Enemy data was not valid.")
    else println("Enemy data OK")
    
    if (!followingEnemyData.get.exists(string => string == "empty") && (!followingEnemyData.get.map(clump => clump.split('|')).forall(_.size == 4) || !followingEnemyData.get.map(clump => clump.split('|')).flatten.forall(isValidDouble(_)))) throw CorruptedSaveFile("FollowingEnemy data was not valid.")
    else println("FollowingEnemy data OK")
    
    if (levelUnlockStatus.get.size == 2 && levelUnlockStatus.get.forall(isValidBoolean(_))) println("Level Unlock Data OK")
    else throw CorruptedSaveFile("Level unlock data was not valid.")
    
    if(!levelItems.get.forall(clump => clump.split('|').drop(1).forall(isValidDouble(_)))) throw CorruptedSaveFile("Level item data was not valid.")
    else println("Level item data ok")
    
 //Tehdään muokkaukset peliolioon
    
   //Kenttien avoimuustilanne
   (0 to GameWindow.currentGame.levelCompletionStatus.size-1).foreach(index => GameWindow.currentGame.levelCompletionStatus(index) = levelUnlockStatus.get(index).toBoolean)
    
   //Viimeisimmän kentän nimi
   lastLevel.get match{
      case "Large City" => GameWindow.currentGame.swapLevel(1)
      case "Boxes" => GameWindow.currentGame.swapLevel(2)
      case x:String => throw CorruptedSaveFile("Level name data corrupted: " + x)
      case _ => throw CorruptedSaveFile("Level name data corrupted")
    }
    
     //Kentän tavarat
  val lvl = GameWindow.currentGame.currentLevel  
  lvl.itemsInWorld.clear()
  if(levelItems.get(0) != "empty")levelItems.get.foreach((id:String) => id.take(2) match{
      case "HP" => lvl.spawnItem(new HealthPack(GameWindow.currentGame, id(2).toString.toInt), (id.split('|')(1).toString().toDouble, id.split('|')(2).toString().toDouble) )
      case "EP" => lvl.spawnItem(new EnergyPack(GameWindow.currentGame, id(2).toString.toInt), (id.split('|')(1).toString().toDouble, id.split('|')(2).toString().toDouble) )
      case "SF" => lvl.spawnItem(new SlowFiringWeapon(GameWindow.currentGame, None), (id.split('|')(1).toString().toDouble, id.split('|')(2).toString().toDouble) )
      case "RF" => lvl.spawnItem(new RapidFireWeapon(GameWindow.currentGame, None), (id.split('|')(1).toString().toDouble, id.split('|')(2).toString().toDouble) )
      case x:String => throw CorruptedSaveFile("Level item data corrupted: " + x)
      case _ => throw CorruptedSaveFile("Level item data corrupted")
    })
   
    println("Testaus pelaajan sijainnista: " + playerLoc.get.size)
    //Pelaajan sijainti
    val plr = GameWindow.currentGame.player
    val loadedPlayerLocation:(Double, Double) = (playerLoc.get(0).toDouble, playerLoc.get(1).toDouble) 
    plr.location.zero
    plr.location.move(loadedPlayerLocation._1, loadedPlayerLocation._2)
    println("Player location in game: " + plr.location.locationInGame)
   
  //Pelaajan tavarat
  plr.inventory.clear()
  if(playerInv.get(0) != "empty")playerInv.get.foreach((id:String) => id.take(2) match{
      case "HP" => println("Restoring Health Pck");plr.pickUp(new HealthPack(GameWindow.currentGame, id(2).toString.toInt), true)
      case "EP" => println("Restoring Energy Pck");plr.pickUp(new EnergyPack(GameWindow.currentGame, id(2).toString.toInt), true)
      case "SF" => println("Restoring Slow firing weapon");plr.pickUp(new SlowFiringWeapon(GameWindow.currentGame, Some(plr)), true)
      case "RF" => println("Restoring Rapid fire weapon");plr.pickUp(new RapidFireWeapon(GameWindow.currentGame, Some(plr)), true)
      case x:String => throw CorruptedSaveFile("Player inventory data corrupted: " + x)
      case _ => throw CorruptedSaveFile("Player inventory data corrupted")
    })
      
    //Pelaajan energia ja HP
    plr.HP = playerHP.get.toDouble
    plr.energy = playerEnergy.get.toDouble
    
    //Viholliset
    GameWindow.currentGame.enemies.clear()
    
    if(enemyData.get(0) != "empty") enemyData.get.foreach(clump =>{
      val split = clump.split('|')
      val x = split(0).toDouble
      val y = split(1).toDouble
      val health = split(2)
      
      val monster = new ShooterEnemy("Green Slime", GameWindow.currentGame, x, y )
      GameWindow.currentGame.enemies += monster
      
    })
    
    if(followingEnemyData.get(0) != "empty") followingEnemyData.get.foreach(clump =>{
      val split = clump.split('|')
      val x = split(2).toDouble
      val y = split(3).toDouble
      val health = split(0).toDouble
      val energy = split(1).toDouble
      
      val monster = new FollowingEnemy("Corrupted Moonman", GameWindow.currentGame, x, y )
      monster.takeDamage((monster.HP-health).toInt)
      monster.useEnergy((monster.energy-energy).toInt)
      GameWindow.currentGame.enemies += monster
      
    })
     
   true
 }  
}

case class CorruptedSaveFile(description: String) extends java.lang.Exception(description)