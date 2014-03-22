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
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.Formattings

class PlayerGameInfo extends ServerGameComet {
  def nameKey = CometInit.playerGameInfoKey

  protected def prepareShutdown() = {
    // nothing to do, as we do not have our own data
  }

  protected def pushDataToClients(server: GameServerActor) = {
    def setHtmlBuilder(p: Int, g: Int)(value: Any, idFunc: (Int, Int) => String) = {
      makeSetHtmlCmd(Full(value.toString), idFunc(g, p))
    }
    def makeSetHtmlCmd(value: Box[String], id: String) = value.map(x => SetHtml(id, Text(x))) openOr JsCmds.Noop
    
    val gameSummary = server.gameSummary
    val playerSummaries = gameSummary.getSummaries
    
    for (gameId <- getGameId.toList) {
      val cmds = for (summary <- playerSummaries) yield {
        import PlayerGameInfo._
        val builder = setHtmlBuilder(summary._1, gameId) _
        val inf = summary._2
        builder(formatPercent(inf.buildSpeed), avgBuildSpeed) &
          builder(formatKMBT(inf.sumMetal), sumMetal) &
          builder(formatPercent(inf.metalUseAvg), metalUsed) &
          builder(formatKMBT(inf.sumEnergy), sumEnergy) &
          builder(formatPercent(inf.energyUseAvg), energyUsed) &
          builder(inf.apmAvg, apmAvg)
      }
      val timeUpdate = SetHtml("length", Text("Duration: "+Formattings.prettyTimespan(gameSummary.runTime)))
      val winnerUpdate = if (gameSummary.winner != "unknown") SetHtml("winner", Text("Winner: "+gameSummary.winner)) else JsCmds.Noop
      partialUpdate(cmds.reduce(_&_) & timeUpdate & winnerUpdate)
    }
  }

  // no need to push any updates after render
  // this is a rather static comet after all
  override def render = {
    val tx = for (gId <- getGameId) yield {
      new PlayerInfoRenderer(gId, gameServer).render
    }

    val fooo = tx openOr ("#noop" #> "")
    fooo
  }

  // should never be called, as we overwrote render
  protected def coreRender = ???
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