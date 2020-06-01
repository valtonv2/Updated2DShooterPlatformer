import scalafx.Includes
import scalafx.scene.shape.Rectangle
import scalafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import scalafx.scene.text.Text
import javafx.scene.input._
import scalafx.scene.Node
import scalafx.scene.Scene
import scalafx.scene.paint.Color._
import scalafx.scene.media.AudioClip
import scalafx.scene.Group
import scalafx.scene.control._
import scalafx.scene.Cursor



object Menus{
  
  var currentMenu:GameMenu = MainMenu  //Tämän perusteella GUI osaa kutsua oikean menun refresh-metodia
  def fullScreenStatus = GameWindow.stage.isFullScreen()
  
  object MainMenu extends GameMenu {
    
    val name = "Main Menu"
    private val gameLogo = Helper.anySpriteFromImage("file:src/main/resources/Pictures/GameLogo.png", (0,0), 600, 250)
       
    private val playButton = new AnimatedButton(
      textForButton = "Play Game",
      locationOffsetFromCenter = (0, -100),
      dimensions = (200, 50),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonPressed.png",
      announcement = None,
      action ={ 
        
        GameWindow.menuClock.stop()
        GameWindow.clock.start()
         GameWindow.currentGame.fullImage.cursor.value_=(Cursor.None)
        if(!fullScreenStatus) GameWindow.stage.scene = GameWindow.currentGame.fullImage 
        else{GameWindow.stage.scene = GameWindow.currentGame.fullImage; GameWindow.stage.setFullScreen(true) }
       
        })
    
    private val settingsButton = new AnimatedButton(
      textForButton = "Settings",
      locationOffsetFromCenter = (0, -25),
      dimensions = (200, 50),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonPressed.png",
      announcement = None,
      action ={ 
        println("Moved to settings menu")
      
        if(!fullScreenStatus) GameWindow.stage.scene = Menus.SettingsMenu.scene
        else{GameWindow.stage.scene = Menus.SettingsMenu.scene; GameWindow.stage.setFullScreen(true) }
        currentMenu = SettingsMenu
        SettingsMenu.arrivedFrom = this
        })
    
    
     private val levelSelectButton = new AnimatedButton(
      textForButton = "Select Level",
      locationOffsetFromCenter = (0, 50),
      dimensions = (200, 50),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonPressed.png",
      announcement = None,
      action ={ 
        if(!fullScreenStatus) GameWindow.stage.scene = Menus.LevelSelectMenu.scene
        else{GameWindow.stage.scene = Menus.LevelSelectMenu.scene; GameWindow.stage.setFullScreen(true) }
        currentMenu = LevelSelectMenu
        })
    
    private val loadButton = new AnimatedButton(
      textForButton = "Load Game",
      locationOffsetFromCenter = (0, 125),
      dimensions = (200, 50),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonPressed.png",
      announcement = None,
      action ={ 
        if(!fullScreenStatus) GameWindow.stage.scene = Menus.LoadMenu.scene
        else{GameWindow.stage.scene = Menus.LoadMenu.scene; GameWindow.stage.setFullScreen(true) }
        currentMenu = LoadMenu
        })
    
     private val saveButton = new AnimatedButton(
      textForButton = "Save Game",
      locationOffsetFromCenter = (0, 200),
      dimensions = (200, 50),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonPressed.png",
      announcement = None,
      action ={ 
        if(!fullScreenStatus) GameWindow.stage.scene = Menus.SaveMenu.scene
        else{GameWindow.stage.scene = Menus.SaveMenu.scene; GameWindow.stage.setFullScreen(true) }
        currentMenu = SaveMenu
        })
    
    private val exitButton = new AnimatedButton(
      textForButton = "Exit Game",
      locationOffsetFromCenter = (0, 275),
      dimensions = (200, 50),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonPressed.png",
      announcement = None,
      action ={ 
        println("Exiting game")
        GameWindow.stage.close()
        GameWindow.stopApp()
        })
    
    private val buttons = Vector[GameButton](playButton, settingsButton, levelSelectButton, loadButton, saveButton, exitButton )
    private val backGround = Helper.anySpriteFromImage("file:src/main/resources/Pictures/MainMenuBackGround.png", (0,0), 2783 , 2487 )
    
    val theme = None
    val scene = new Scene
    
    def refresh = {
      scene.content = Vector[Node](backGround, gameLogo) ++ buttons.map(_.fullImage).flatten
      backGround.height = GameWindow.stage.height.toDouble
      backGround.width = GameWindow.stage.width.toDouble
      gameLogo.x = GameWindow.stage.width.toDouble/2 -275
      gameLogo.y = GameWindow.stage.height.toDouble/2 -375
 
      buttons.foreach(button => button.refreshLocation)
    }
    
    
 }
 //----------------------------------------------------------------------------------------------------------------------------------------------------------------------
    object PauseMenu extends GameMenu {
    
    val name = "Pause Menu"
    private val header = new Text(0,0,"Game Paused")
    header.scaleX = 3
    header.scaleY = 3
    header.fill = Blue
    
    private val headerBG = new Rectangle{fill = Gray; width = 260; height = 40; x=0; y= 0}
    
    private var annotationText = new Text(0,0,"")
    annotationText.scaleX = 3
    annotationText.scaleY = 3
    annotationText.fill = White
       
    private val resumeButton = new AnimatedButton(
      textForButton = "",
      locationOffsetFromCenter = (-200, 0),
      dimensions = (150, 150),
      normalImgPath = "file:src/main/resources/Pictures/SquarePlayButton.png",
      hoverImgPath = "file:src/main/resources/Pictures/SquarePlayButton.png",
      pressedImgPath = "file:src/main/resources/Pictures/SquarePlayButton.png",
      announcement = Some("Resume game"),
      action ={ 
        GameWindow.clock.start()
        GameWindow.menuClock.stop()
        if(!fullScreenStatus) GameWindow.stage.scene = GameWindow.currentGame.fullImage 
        else{GameWindow.stage.scene = GameWindow.currentGame.fullImage; GameWindow.stage.setFullScreen(true) }
        })
    
    private val settingButton = new AnimatedButton(
      textForButton = "",
      locationOffsetFromCenter = (0, 0),
      dimensions = (150, 150),
      normalImgPath = "file:src/main/resources/Pictures/SquareSettingButton.png",
      hoverImgPath = "file:src/main/resources/Pictures/SquareSettingButton.png",
      pressedImgPath = "file:src/main/resources/Pictures/SquareSettingButton.png",
      announcement = Some("Game Settings"),
      action ={ 
        println("Moved to settings menu")
        GameWindow.clock.stop()
        GameWindow.menuClock.start()
        if(!fullScreenStatus) GameWindow.stage.scene = Menus.SettingsMenu.scene 
        else{GameWindow.stage.scene = Menus.SettingsMenu.scene; GameWindow.stage.setFullScreen(true) }
        Menus.currentMenu = SettingsMenu
        SettingsMenu.arrivedFrom = this
        
        })
    
    
     private val exitButton = new AnimatedButton(
      textForButton = "",
      locationOffsetFromCenter = (200, 0),
      dimensions = (150, 150),
      normalImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      hoverImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      pressedImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      announcement = Some("Back to Main Menu"),
      action ={ 
        GameWindow.clock.stop()
        GameWindow.menuClock.start()
        if(!fullScreenStatus) GameWindow.stage.scene = Menus.MainMenu.scene 
        else{GameWindow.stage.scene = Menus.MainMenu.scene; GameWindow.stage.setFullScreen(true) }
        Menus.currentMenu = MainMenu
        })
    
 
    
   private val buttons = Vector[GameButton](resumeButton, settingButton, exitButton )
   private val backGround = Helper.anySpriteFromImage("file:src/main/resources/Pictures/MenuBackGround.png", (0,0), 800 ,800 )
    
   val theme = None
   val scene = new Scene
    
    def refresh = {
      scene.content = Vector[Node](backGround, headerBG, header, annotationText) ++ buttons.map(_.fullImage).flatten
      backGround.height = GameWindow.stage.height.toDouble
      backGround.width = GameWindow.stage.width.toDouble
      header.x = GameWindow.stage.width.toDouble/2 - 50
      header.y = GameWindow.stage.height.toDouble/2 - 250
      headerBG.x = GameWindow.stage.width.toDouble/2 - 50 -85
      headerBG.y = GameWindow.stage.height.toDouble/2 - 250 -20
      annotationText.x = GameWindow.stage.width.toDouble/2 - 50
      annotationText.y = GameWindow.stage.height.toDouble/2 + 250
      
      buttons.foreach(button => button.refreshLocation)
      
      if (buttons.exists(button => button.buttonAnnouncement.isDefined)) this.annotationText.text = buttons.find(_.buttonAnnouncement.isDefined).get.buttonAnnouncement.get
      else this.annotationText.text = ""
    }
   }
//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------    
  
  object DeathMenu extends GameMenu {
    
    val name = "Death Menu"
    private val header = new Text(0,0,"You Died")
    header.scaleX = 3
    header.scaleY = 3
    header.fill = Red
    
   private val headerBG = new Rectangle{fill = Purple; width = 255; height = 40; x=0; y= 0}
    
   private var annotationText = new Text(0,0,"")
    annotationText.scaleX = 3
    annotationText.scaleY = 3
    annotationText.fill = Purple
        
   private val restartButton = new AnimatedButton(
      textForButton = "",
      locationOffsetFromCenter = (-300, -150),
      dimensions = (150, 150),
      normalImgPath = "file:src/main/resources/Pictures/SquarePlayButton.png",
      hoverImgPath = "file:src/main/resources/Pictures/SquarePlayButton.png",
      pressedImgPath = "file:src/main/resources/Pictures/SquarePlayButton.png",
      announcement = Some("Start New Game"),
      action ={
        System.gc()
      
        GameWindow.menuClock.stop()
        GameWindow.clock.start()
        GameWindow.currentGame.reset
 
        PlayerHUD.weaponHud.weaponBoxes.foreach(_.removeItem)
        PlayerHUD.equipmentBox.box.removeItem
        PlayerHUD.equipmentBox.updateItems
        if(!fullScreenStatus) GameWindow.stage.scene = GameWindow.currentGame.fullImage 
        else{GameWindow.stage.scene = GameWindow.currentGame.fullImage; GameWindow.stage.setFullScreen(true) }
        })
      
    private val exitButton = new AnimatedButton(
      textForButton = "",
      locationOffsetFromCenter = (-300, 150),
      dimensions = (150, 150),
      normalImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      hoverImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      pressedImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      announcement = Some("Return to Main Menu"),
      action ={ 
        GameWindow.clock.stop()
        GameWindow.menuClock.start()
        GameWindow.currentGame.reset
        if(!fullScreenStatus) GameWindow.stage.scene = Menus.MainMenu.scene 
        else{GameWindow.stage.scene = Menus.MainMenu.scene; GameWindow.stage.setFullScreen(true) }
        PlayerHUD.weaponHud.weaponBoxes.foreach(_.removeItem)
        PlayerHUD.equipmentBox.box.removeItem
        PlayerHUD.equipmentBox.updateItems
        Menus.currentMenu = MainMenu
        })
    
 
    
    private val buttons = Vector[GameButton](restartButton, exitButton )
    private val backGround = Helper.anySpriteFromImage("file:src/main/resources/Pictures/DeathMenuBackGround.png", (0,0), 800 ,800 )
    val theme = Some(new AudioClip("file:src/main/resources/sound/DWADeathMenuTheme.wav"))
    val scene = new Scene
    
    def refresh = {
      scene.content = Vector[Node](backGround,headerBG, header, annotationText) ++ buttons.map(_.fullImage).flatten
      backGround.height = GameWindow.stage.height.toDouble
      backGround.width = GameWindow.stage.width.toDouble
      header.x = GameWindow.stage.width.toDouble/2 + 100
      header.y = GameWindow.stage.height.toDouble/2 - 245
      headerBG.x = GameWindow.stage.width.toDouble/2 + 100 -85
      headerBG.y = GameWindow.stage.height.toDouble/2 - 250 -20
      annotationText.x = GameWindow.stage.width.toDouble/2 - 50
      annotationText.y = GameWindow.stage.height.toDouble/2 + 250

      
      buttons.foreach(button => button.refreshLocation)
      
      if (buttons.exists(button => button.buttonAnnouncement.isDefined)) this.annotationText.text = buttons.find(_.buttonAnnouncement.isDefined).get.buttonAnnouncement.get
      else this.annotationText.text = ""
    }
   }  
  
 //==================================================================================================================================================================================================== 
  
   object LevelSelectMenu extends GameMenu {
    
    val name = "Level Selection"
    private val header = new Text(0,0,"Level\nSelection\nMenu")
    header.scaleX = 3
    header.scaleY = 3
    header.fill = White
    
    private val lv1Button = new AnimatedButton(
      textForButton = "Level 1",
      locationOffsetFromCenter = (30, -245),
      dimensions = (140, 95),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonpressed.png",
      announcement = Some("Large City"),
      action ={ 
        GameWindow.menuClock.stop()
        GameWindow.clock.start()
        GameWindow.currentGame.swapLevel(1)
        PlayerHUD.weaponHud.weaponBoxes.foreach(_.removeItem)
        PlayerHUD.equipmentBox.box.removeItem
        PlayerHUD.equipmentBox.updateItems
        if(!fullScreenStatus) GameWindow.stage.scene = GameWindow.currentGame.fullImage
        else{GameWindow.stage.scene = GameWindow.currentGame.fullImage; GameWindow.stage.setFullScreen(true) }

        })
    
    private val lv2Button = new AnimatedButton(
      textForButton = "Level 2",
      locationOffsetFromCenter = (30, -100),
      dimensions = (140, 95),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonpressed.png",
      announcement = Some("Boxes"),
      action ={ 
        GameWindow.menuClock.stop()
        GameWindow.clock.start()
        GameWindow.currentGame.swapLevel(2)
        PlayerHUD.weaponHud.weaponBoxes.foreach(_.removeItem)
        PlayerHUD.equipmentBox.box.removeItem
        PlayerHUD.equipmentBox.updateItems
        if(!fullScreenStatus) GameWindow.stage.scene = GameWindow.currentGame.fullImage
        else{GameWindow.stage.scene = GameWindow.currentGame.fullImage; GameWindow.stage.setFullScreen(true) }

        })
    
    private val scrollPart = new Group(Helper.anySpriteFromImage("file:src/main/resources/Pictures/LevelMenuScrollPart.png", (0,0), 800, 2500)) 
    
    
    scrollPart.autoSizeChildren = false
    scrollPart.onScroll = (event:ScrollEvent) => {
      if(event.getDeltaY>0 && scrollPart.layoutY.toDouble < 0){
        scrollPart.layoutY = scrollPart.layoutY.toDouble + 5
        lv1Button.changeOffset(0, 5)
        lv2Button.changeOffset(0,5)
      }else if(event.getDeltaY<0 && scrollPart.layoutY.toDouble > GameWindow.stage.height.toDouble - 2500){
        scrollPart.layoutY = scrollPart.layoutY.toDouble - 5
        lv1Button.changeOffset(0, -5)
        lv2Button.changeOffset(0, -5)
      }
    }
    
   
    
    private var annotationText = new Text(0,0,"")
    annotationText.scaleX = 3
    annotationText.scaleY = 3
    annotationText.fill = White
       

     private val exitButton = new AnimatedButton(
      textForButton = "",
      locationOffsetFromCenter = (300, 300),
      dimensions = (150, 150),
      normalImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      hoverImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      pressedImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      announcement = Some("Back to main menu"),
      action ={ 
        GameWindow.clock.stop()
        GameWindow.menuClock.start()
        if(!fullScreenStatus) GameWindow.stage.scene = Menus.MainMenu.scene 
        else{GameWindow.stage.scene = Menus.MainMenu.scene; GameWindow.stage.setFullScreen(true) }
        Menus.currentMenu = MainMenu
        })
    
    private val buttons = Vector[GameButton](exitButton, lv1Button, lv2Button )
    private val backGround = Helper.anySpriteFromImage("file:src/main/resources/Pictures/Lastembers.png", (0,0), 800 ,800 )
    val theme = None
    val scene = new Scene
    
    def refresh = {
      scene.content = Vector[Node](backGround, scrollPart, header, annotationText) ++ buttons.map(_.fullImage).flatten
      backGround.height = GameWindow.stage.height.toDouble
      backGround.width = GameWindow.stage.width.toDouble
      header.x = GameWindow.stage.width.toDouble/2 - 280
      header.y = GameWindow.stage.height.toDouble/2 - 290
      annotationText.x = GameWindow.stage.width.toDouble/2 +250
      annotationText.y = GameWindow.stage.height.toDouble/2 -50
      scrollPart.layoutX = GameWindow.stage.width.toDouble/2 - 400
      
      buttons.foreach(button => button.refreshLocation)
      
      if(GameWindow.currentGame.levelCompletionStatus(0) == false && !this.lv2Button.isLocked){
        this.lv2Button.lock
        println("Button Locked")
      }
      else if(GameWindow.currentGame.levelCompletionStatus(0) == true && this.lv2Button.isLocked){
        this.lv2Button.unlock
        println("Button unlocked")
      }
     
      
      if (buttons.exists(button => button.buttonAnnouncement.isDefined)) this.annotationText.text = buttons.find(_.buttonAnnouncement.isDefined).get.buttonAnnouncement.get
      else this.annotationText.text = ""
    }
   }
   
   
   
   object SettingsMenu extends GameMenu {
    
    val name = "Settings Menu"
    private val header = new Text(0,0,"Game Settings")
    header.scaleX = 3
    header.scaleY = 3
    header.fill = Black
    
    var arrivedFrom:GameMenu = MainMenu
    
    //Asetusmenun ohjainelementit
    val volumeSlider = new GameSlider("Volume",0,100,100, (700, 10), (-350, -200))
    val muteCheckBox = new GameCheckBox("Mute sound", (-150, 50))
    val devModeCheckBox = new GameCheckBox("Dev Mode", (-150, 130))
    
    private val checkBoxBackground = Helper.anySpriteFromImage("file:src/main/resources/Pictures/CBbackground.png", (0,0), 400 ,1000 )
   
    private val exitButton = new AnimatedButton(
      textForButton = "",
      locationOffsetFromCenter = (300, 200),
      dimensions = (150, 150),
      normalImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      hoverImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      pressedImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      announcement = None,
      action ={ 
        
        if(!fullScreenStatus) GameWindow.stage.scene = arrivedFrom.scene 
        else{GameWindow.stage.scene = arrivedFrom.scene; GameWindow.stage.setFullScreen(true) }
        Menus.currentMenu = arrivedFrom
        })
    

   
    
    private val buttons = Vector[GameButton]( exitButton, muteCheckBox, devModeCheckBox )
    private val backGround = Helper.anySpriteFromImage("file:src/main/resources/Pictures/SettingsMenuBG.png", (0,0), 800 ,800 )
    val theme = Some(new AudioClip("file:src/main/resources/sound/TimeStopAmbience.wav"))
    val scene = new Scene
    
    def refresh = {
      scene.content = Vector[Node](backGround, checkBoxBackground, header) ++ buttons.map(_.fullImage).flatten ++ volumeSlider.image
      backGround.height = GameWindow.stage.height.toDouble
      backGround.width = GameWindow.stage.width.toDouble
      header.x = GameWindow.stage.width.toDouble/2 -50
      header.y = GameWindow.stage.height.toDouble/2 - 350
      checkBoxBackground.x = GameWindow.stage.width.toDouble/2 -400
      checkBoxBackground.y = GameWindow.stage.height.toDouble/2 

      if(muteCheckBox.isSelected) Settings.muteSound=true
      else Settings.muteSound = false
      
      if(devModeCheckBox.isSelected) Settings.devMode=true
      else Settings.devMode = false
      
      volumeSlider.refresh
      
      
    
      buttons.foreach(button => button.refreshLocation)
      
     
    }
   }  
   
   object LoadMenu extends GameMenu {
    
    val name = "Load Menu"
    private val header = new Text(0,0,"Load Game")
    header.scaleX = 3
    header.scaleY = 3
    header.fill = White
    
    private var annotationText = new Text(0,0,"")
    annotationText.scaleX = 3
    annotationText.scaleY = 3
    annotationText.fill = Purple
        
    private val slot1Button = new AnimatedButton(
      textForButton = "Slot 1",
      locationOffsetFromCenter = (-100, -150),
      dimensions = (200, 100),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonpressed.png",
      announcement = None,
      action ={
       SaveHandler.loadGame("src/main/resources/SaveFiles/Save1.DWAsave")
       this.annotationText.text = "Slot 1 loaded"
        })
    
    private val slot2Button = new AnimatedButton(
      textForButton = "Slot 2",
      locationOffsetFromCenter = (-100, -25),
      dimensions = (200, 100),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonpressed.png",
      announcement = None,
      action ={
       SaveHandler.loadGame("src/main/resources/SaveFiles/Save2.DWAsave")
       this.annotationText.text = "Slot 2 loaded"
        })
    
     private val slot3Button = new AnimatedButton(
      textForButton = "Slot 3",
      locationOffsetFromCenter = (-100, 100),
      dimensions = (200, 100),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonpressed.png",
      announcement = None,
      action ={
       SaveHandler.loadGame("src/main/resources/SaveFiles/Save3.DWAsave")
       this.annotationText.text = "Slot 3 loaded"
        })
    
     private val slot4Button = new AnimatedButton(
      textForButton = "Slot 4",
      locationOffsetFromCenter = (-100, 225),
      dimensions = (200, 100),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonpressed.png",
      announcement = None,
      action ={
       SaveHandler.loadGame("src/main/resources/SaveFiles/Save4.DWAsave")
       this.annotationText.text = "Slot 4 loaded"
        })
      
     private val exitButton = new AnimatedButton(
      textForButton = "",
      locationOffsetFromCenter = (300, 150),
      dimensions = (150, 150),
      normalImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      hoverImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      pressedImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      announcement = Some("Return to Main Menu"),
      action ={ 
        if(!fullScreenStatus) GameWindow.stage.scene = Menus.MainMenu.scene 
        else{GameWindow.stage.scene = Menus.MainMenu.scene; GameWindow.stage.setFullScreen(true) }
        Menus.currentMenu = MainMenu
        })
    
 
    
    private val buttons = Vector[GameButton](slot1Button, slot2Button, slot3Button, slot4Button, exitButton)
    private val backGround = Helper.anySpriteFromImage("file:src/main/resources/Pictures/DeathMenuBackGround.png", (0,0), 800 ,800 )
    val theme = None
    val scene = new Scene
    
    def refresh = {
      scene.content = Vector[Node](backGround, header, annotationText) ++ buttons.map(_.fullImage).flatten
      backGround.height = GameWindow.stage.height.toDouble
      backGround.width = GameWindow.stage.width.toDouble
      header.x = GameWindow.stage.width.toDouble/2 + 100
      header.y = GameWindow.stage.height.toDouble/2 - 250
      annotationText.x = GameWindow.stage.width.toDouble/2 - 50
      annotationText.y = GameWindow.stage.height.toDouble/2 + 250

      
      buttons.foreach(button => button.refreshLocation)
      
      if (buttons.exists(button => button.buttonAnnouncement.isDefined)) this.annotationText.text = buttons.find(_.buttonAnnouncement.isDefined).get.buttonAnnouncement.get
      else this.annotationText.text = ""
    }
   }  
    
   
  object SaveMenu extends GameMenu {
    
    val name = "Save Menu"
    private val header = new Text(0,0,"Save Game")
    header.scaleX = 3
    header.scaleY = 3
    header.fill = White
    
    private var annotationText = new Text(0,0,"")
    annotationText.scaleX = 3
    annotationText.scaleY = 3
    annotationText.fill = Purple
        
    private val slot1Button = new AnimatedButton(
      textForButton = "Slot 1",
      locationOffsetFromCenter = (-100, -150),
      dimensions = (200, 100),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonpressed.png",
      announcement = None,
      action ={
       SaveHandler.saveGame("src/main/resources/SaveFiles/Save1.DWAsave")
       this.annotationText.text = "Slot 1 saved"
        })
    
    private val slot2Button = new AnimatedButton(
      textForButton = "Slot 2",
      locationOffsetFromCenter = (-100, -25),
      dimensions = (200, 100),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonpressed.png",
      announcement = None,
      action ={
       SaveHandler.saveGame("src/main/resources/SaveFiles/Save2.DWAsave")
       this.annotationText.text = "Slot 2 saved"
        })
    
     private val slot3Button = new AnimatedButton(
      textForButton = "Slot 3",
      locationOffsetFromCenter = (-100, 100),
      dimensions = (200, 100),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonpressed.png",
      announcement = None,
      action ={
       SaveHandler.saveGame("src/main/resources/SaveFiles/Save3.DWAsave")
       this.annotationText.text = "Slot 3 saved"
        })
    
     private val slot4Button = new AnimatedButton(
      textForButton = "Slot 4",
      locationOffsetFromCenter = (-100, 225),
      dimensions = (200, 100),
      normalImgPath = "file:src/main/resources/Pictures/GrayRectButtonNormal.png",
      hoverImgPath = "file:src/main/resources/Pictures/GrayRectButtonHover.png",
      pressedImgPath = "file:src/main/resources/Pictures/GrayRectButtonpressed.png",
      announcement = None,
      action ={
       SaveHandler.saveGame("src/main/resources/SaveFiles/Save4.DWAsave")
       this.annotationText.text = "Slot 4 saved"
        })
      
     private val exitButton = new AnimatedButton(
      textForButton = "",
      locationOffsetFromCenter = (300, 150),
      dimensions = (150, 150),
      normalImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      hoverImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      pressedImgPath = "file:src/main/resources/Pictures/SquareExitButton.png",
      announcement = Some("Return to Main Menu"),
      action ={ 
        if(!fullScreenStatus) GameWindow.stage.scene = Menus.MainMenu.scene 
        else{GameWindow.stage.scene = Menus.MainMenu.scene; GameWindow.stage.setFullScreen(true) }
        Menus.currentMenu = MainMenu
        })
    
 
    
    private val buttons = Vector[GameButton](slot1Button, slot2Button, slot3Button, slot4Button, exitButton)
    private val backGround = Helper.anySpriteFromImage("file:src/main/resources/Pictures/DeathMenuBackGround.png", (0,0), 800 ,800 )
    val theme = None
    val scene = new Scene
    
    def refresh = {
      scene.content = Vector[Node](backGround, header, annotationText) ++ buttons.map(_.fullImage).flatten
      backGround.height = GameWindow.stage.height.toDouble
      backGround.width = GameWindow.stage.width.toDouble
      header.x = GameWindow.stage.width.toDouble/2 + 100
      header.y = GameWindow.stage.height.toDouble/2 - 250
      annotationText.x = GameWindow.stage.width.toDouble/2 - 50
      annotationText.y = GameWindow.stage.height.toDouble/2 + 250

      
      buttons.foreach(button => button.refreshLocation)
      
      if (buttons.exists(button => button.buttonAnnouncement.isDefined)) this.annotationText.text = buttons.find(_.buttonAnnouncement.isDefined).get.buttonAnnouncement.get
      else this.annotationText.text = ""
    }
   }       
}

//##########################################################################################################################################################################################

//Luokka animatedButton tarjoaa helpon tavan luoda nappeja valikoihin. Action-parametri sisältää nappia painettaessas suoritettavan koodin.

class AnimatedButton(textForButton:String, var locationOffsetFromCenter:(Double, Double), val dimensions:(Double, Double), normalImgPath:String, hoverImgPath:String, pressedImgPath:String, announcement:Option[String], action: => Unit )extends GameButton{
  
  private val buttonText = new Text(0,0, textForButton)
  buttonText.scaleX = 2.0
  buttonText.scaleY = 2.0
  buttonText.setMouseTransparent(true)
  
  var buttonAnnouncement:Option[String] = None  //Teksti joka näkyy valikossa kun hiiri viedään napin päälle
  
  var isLocked = false         //Jos nappi on lukittu sitä ei voi painaa. Käytetään pelin tasojen yhteydessä
  
  private val normalImg = Helper.anySpriteFromImage(normalImgPath, (0,0), dimensions._1, dimensions._2)   //Kuvat kolmeen eri tilanteeseen
  private val hoverImg = Helper.anySpriteFromImage(hoverImgPath, (0,0), dimensions._1, dimensions._2)
  private val pressedImg = Helper.anySpriteFromImage(pressedImgPath, (0,0), dimensions._1, dimensions._2)
  
  var currentImage = normalImg
  
  def fullImage = Vector[Node](currentImage, buttonText)
  
  def reset = this.currentImage = normalImg //Nappi resetoidaan painalluksen jälkeen jotta se olisi valmis uudelleen käytettäväksi
  
  def refreshLocation={
    
    //Seuraavat rivit pitävät napin paikoillaan ikkunan koon muuttuessa
    this.currentImage.x = GameWindow.stage.width.toDouble/2 - dimensions._1/2  + locationOffsetFromCenter._1
    this.currentImage.y = GameWindow.stage.height.toDouble/2 - dimensions._2/2 + locationOffsetFromCenter._2
    this.buttonText.x = GameWindow.stage.width.toDouble/2 - dimensions._1/2 + dimensions._1/2-35 + locationOffsetFromCenter._1
    this.buttonText.y = GameWindow.stage.height.toDouble/2 - dimensions._2/2  + dimensions._2/2 + locationOffsetFromCenter._2
  }
  
  def changeOffset(dx:Double, dy:Double) = {
    
    val orig = this.locationOffsetFromCenter
    this.locationOffsetFromCenter = (orig._1 + dx, orig._2 + dy)
    
  }
  
  def lock = this.isLocked = true
  def unlock = this.isLocked = false
  
  
//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
//Napin tapahtumankuuntelijat ja käsittelijät
  
  normalImg.onMouseEntered = (event:MouseEvent) => {
    try{
    this.currentImage = hoverImg
    this.buttonAnnouncement = announcement 
    }catch{
      case e:Exception => GameWindow.exceptionScreen("Something is wrong. + \n" + e)
      case _ :Throwable => GameWindow.exceptionScreen("Something is wrong.")
    }
  }
  
  hoverImg.onMouseExited = (event:MouseEvent) => {
    
    try{
    this.currentImage = normalImg
    this.buttonAnnouncement = None
    }catch{
      case e:Exception => GameWindow.exceptionScreen("Something is wrong. + \n" + e)
      case _ :Throwable => GameWindow.exceptionScreen("Something is wrong.")
    }
  }
  
  hoverImg.onMouseClicked = (event:MouseEvent) =>{
    try{
   if(this.isLocked){
     this.buttonAnnouncement = Some("Locked")
   }else{
    this.currentImage = pressedImg
    action
    this.reset
    }
   }catch{
     case e:Exception => GameWindow.exceptionScreen("Something is wrong. + \n" + e)
     case _ :Throwable => GameWindow.exceptionScreen("Something is wrong.")
   }
  
  }
 }
//################################################################################################################################################################################################

class GameSlider(header:String, min:Double, max:Double, value:Double, dimensions:(Double, Double), locationOffset:(Double, Double)) {
  
 private val slider  = new Slider(min, max, value)
    slider.prefWidth = dimensions._1
    slider.prefHeight = dimensions._2
    slider.getStylesheets.addAll("file:src/main/resources/StyleSheets/SliderStyle.css")
    
 private val text = new Text(0,0,header)  
  
 private val percentage = new Text(0,0,slider.value + "%")
    
  def refresh = {
   slider.layoutX = GameWindow.stage.width.toDouble/2 + locationOffset._1
   slider.layoutY = GameWindow.stage.height.toDouble/2 + locationOffset._2
   text.x = GameWindow.stage.width.toDouble/2 + locationOffset._1 + 20
   text.y = GameWindow.stage.height.toDouble/2 + locationOffset._2 - 20
   percentage.x = GameWindow.stage.width.toDouble/2 + locationOffset._1 + 0.9*dimensions._1
   percentage.y = GameWindow.stage.height.toDouble/2 + locationOffset._2 + 55
   percentage.text = slider.value.toDouble.ceil +"%"
  }
  
  val image = Array[Node](text,percentage, slider)
  
  def currentValue:Double = this.slider.value.toDouble
    
  
  
}




//######################################################################################################################################################################################################

class GameCheckBox(text:String, locationOffsetFromCenter:(Double, Double)) extends GameButton{
  
 var isSelected = false
  
 private val buttonBase = Helper.anySpriteFromImage("file:src/main/resources/Pictures/CheckBoxBase.png", (0,0), 50, 50)
 private val buttonMark = Helper.anySpriteFromImage("file:src/main/resources/Pictures/CheckBoxmark.png", (0,0), 30, 30)
 private val labelText = new Text(0,0,text)
 
 labelText.setFill(White)
  
  def fullImage ={
    
    this.isSelected match{
  
    case true => Vector[Node](buttonBase, buttonMark, labelText)
    case false =>Vector[Node](buttonBase, labelText)
    
  } 
 }
    
  def refreshLocation{
    
    buttonBase.x = GameWindow.stage.width.toDouble/2 + locationOffsetFromCenter._1
    buttonBase.y = GameWindow.stage.height.toDouble/2 + locationOffsetFromCenter._2
    buttonMark.x = GameWindow.stage.width.toDouble/2 + locationOffsetFromCenter._1 + 10
    buttonMark.y = GameWindow.stage.height.toDouble/2 + locationOffsetFromCenter._2 + 10
    labelText.x  = GameWindow.stage.width.toDouble/2 + locationOffsetFromCenter._1 -120
    labelText.y  = GameWindow.stage.height.toDouble/2 + locationOffsetFromCenter._2 + 20
    
  } 
  
  def buttonAnnouncement = None
  
  
//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
  buttonBase.onMouseClicked = (event:MouseEvent) => {
    if(this.isSelected == false) this.isSelected = true
    else this.isSelected = false
  }
  
  buttonMark.onMouseClicked = (event:MouseEvent) => {
    if(this.isSelected == false) this.isSelected = true
    else this.isSelected = false
  }  
}
//################################################################################################################################################################


trait GameMenu{
  
  val name:String
  val scene:Scene
  def refresh:Unit
  def theme:Option[AudioClip]
  
}

trait GameButton{
  def refreshLocation:Unit
  def fullImage:Vector[Node]
  def buttonAnnouncement:Option[String]

}