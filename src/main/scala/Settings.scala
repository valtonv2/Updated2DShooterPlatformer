object Settings{
  
  def settingMenu = Menus.SettingsMenu
  
  
  //Ääniasetukset
  var muteSound = false
  def musicVolume:Double = if(!muteSound) settingMenu.volumeSlider.currentValue/100.0 else 0.0
  def toggleMute = {
    if(muteSound == false) muteSound = true
    else muteSound = false
  }
  
  //Kehittäjätila(Loputon HP, energia ja hypyt)
  
  var devMode = false
  
  
  
  
  
  
  
}