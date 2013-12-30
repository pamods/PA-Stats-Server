package info.nanodesu.comet

import info.nanodesu.snippet.GameInfo
import info.nanodesu.snippet.PlayerInfo
import net.liftweb.common.Full
import net.liftweb.http.CometActor
import net.liftweb.util.Helpers.intToTimeSpanBuilder
import net.liftweb.util.Schedule
import net.liftweb.common.Loggable
import net.liftweb.http.SessionVar
import net.liftweb.common.Empty
import net.liftweb.common.Box
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.json.JString
import scala.xml.Text
import net.liftweb.util.Helpers
import info.nanodesu.snippet.lib.CometInit
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.collectors.playerinfo.GameAndPlayerInfoCollector
import info.nanodesu.model.db.collectors.gameinfo.loader.ActiveReportersForGameLoader
import info.nanodesu.snippet.cometrenderer.PlayerInfoRenderer

class PlayerGameInfo extends GameComet {
  override def lowPriority = {
    case GamePlayersListJsCmd(id, cmd: JsCmd) if (isMyGame(id)) => partialUpdate(cmd)
  }

  def nameKey = CometInit.playerGameInfoKey

  def render = {
    val tx = for (gId <- getGameId) yield {
    	new PlayerInfoRenderer(gId).render
    }
    
    val fooo = tx openOr ("#noop" #> "")
    fooo
  }
}

object PlayerGameInfo {
  private def idBuilder(key: String)(g: Int, p: Int) = key + "_" + g + "_" + p
  def playerName(g: Int, p: Int) = idBuilder("playerName")(p, g)
  def avgBuildSpeed(g: Int, p: Int) = idBuilder("avgbuildSpeed")(p, g)
  def sumMetal(g: Int, p: Int) = idBuilder("sumMetal")(p, g)
  def metalUsed(g: Int, p: Int) = idBuilder("metalUsed")(p, g)
  def sumEnergy(g: Int, p: Int) = idBuilder("sumEnergy")(p, g)
  def energyUsed(g: Int, p: Int) = idBuilder("energyUsed")(p, g)
  def apmAvg(g: Int, p: Int) = idBuilder("apmAvg")(p, g)
}