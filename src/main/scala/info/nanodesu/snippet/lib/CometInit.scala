package info.nanodesu.snippet.lib

import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Helpers._
import info.nanodesu.snippet.GameInfo
import net.liftweb.util.Helpers._
import info.nanodesu.pages.GamePage
import net.liftweb.common.Box
import info.nanodesu.model.db.collectors.gameinfo.loader.ActiveReportersForGameLoader
import info.nanodesu.comet.PlayerGameInfo
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.collectors.playerinfo.GameAndPlayerInfoCollector
import info.nanodesu.model.db.collectors.gameinfo.loader.GameTimesLoader
import net.liftweb.common.Loggable
import info.nanodesu.snippet.cometrenderer.PlayerInfoRenderer
import net.liftweb.util.CssSel
import info.nanodesu.snippet.cometrenderer.ArmyCompositionRenderer
import info.nanodesu.snippet.cometrenderer.GameChartDataRenderer
import net.liftweb.util.Props

object CometInit extends DispatchSnippet with Loggable {
  val cometServePastThreshold = Props.getInt("cometServeThreshold", 300000)

  val playerGameInfoKey = "game_summary_comet_"
  val gameArmyComposition = "game_army_composition_"
  val gameChartComet = "game_chart_comet_"
    
  val dispatch: DispatchIt = {
    case "playerGameInfo" => doPlayerGameInfo
    case "gameArmyComposition" => doGameArmyComposition
    case "gameChartInfo" => doGameChartInfo
  }

  def shouldBeConsideredLive(gameId: Int): Boolean = {
    val time = CookieBox withSession (new GameTimesLoader(_).selectEndTimeForGame(gameId))
    time map { t =>
	    val threshold = (t + cometServePastThreshold)
	    threshold > System.currentTimeMillis()
    } getOrElse false
  }

  private def doGameChartInfo = doCometOrSnippet(new GameChartDataRenderer(_).render, "GameChartComet", gameChartComet)
  
  private def doPlayerGameInfo = doCometOrSnippet(new PlayerInfoRenderer(_).render, "PlayerGameInfo", playerGameInfoKey)

  private def doCometOrSnippet(renderSnippet: Int => CssSel, cometType: String, cometKey: String) = {
    val renderer = for (gid <- GamePage.getGameId) yield {
      if (shouldBeConsideredLive(gid)) {
        makeShinyComet(cometType, cometKey, gid)
      } else {
        renderSnippet(gid)
      }
    }
    renderer openOr "#noop" #> ""
  }
  
  private def doGameArmyComposition = doCometOrSnippet(new ArmyCompositionRenderer(_).render, "GameArmyComposition", gameArmyComposition)

  private def makeShinyComet(typ: String, key: String, gid: Int): net.liftweb.util.CssSel = {
    ".shiny_comet [data-lift]" #> ("comet?type=" + typ + "&name=" + key + gid)
  }
}