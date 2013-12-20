package info.nanodesu.snippet.lib

import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Helpers._
import info.nanodesu.snippet.GameInfo
import net.liftweb.util.Helpers._
import info.nanodesu.pages.GamePage

object CometInit extends DispatchSnippet {
  val playerGameInfoKey = "game_player_lines_"
  val gameInfoKey = "game_general_"
  val gameArmyComposition = "game_army_composition_"  
    
  val dispatch: DispatchIt = {
    case "playerGameInfo" => doPlayerGameInfo
    case "gameGeneralInfo" => doGameGeneralInfo
    case "gameArmyComposition" => doGameArmyComposition
  }

  private def doPlayerGameInfo = makeShinyComet("PlayerGameInfo", playerGameInfoKey)
  private def doGameGeneralInfo = makeShinyComet("GeneralGameInfo", gameInfoKey)
  private def doGameArmyComposition = makeShinyComet("GameArmyComposition", gameArmyComposition)
  
  private def makeShinyComet(typ: String, key: String): net.liftweb.util.CssSel = {
    ".shiny_comet [data-lift]" #> ("comet?type="+typ+"&name=" + key + GamePage.getGameId.openOr(0))
  }
}