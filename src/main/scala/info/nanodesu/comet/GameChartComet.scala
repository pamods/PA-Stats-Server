package info.nanodesu.comet

import scala.math.BigDecimal.double2bigDecimal
import scala.math.BigDecimal.int2bigDecimal
import scala.xml.Text
import info.nanodesu.snippet.lib._
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.util.Helpers.intToTimeSpanBuilder
import info.nanodesu.snippet.GameInfo
import net.liftweb.common.Full
import net.liftweb.http.js.JsCmd
import info.nanodesu.snippet.lib.CometInit
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventPackage
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventPlayer
import info.nanodesu.model.db.collectors.gameinfo.ArmyEvent
import net.liftweb.json._
import net.liftweb.http.js.JE
import scala.actors.ActorTask
import net.liftweb.http.S
import net.liftweb.common.Empty
import info.nanodesu.lib.db.CookieBox
import net.liftweb.common.Loggable
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventPackage
import net.liftweb.common.Box
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventDataCollector
import net.liftweb.util.CssSel
import info.nanodesu.snippet.cometrenderer.ArmyCompositionRenderer
import info.nanodesu.snippet.cometrenderer.GameChartDataRenderer
import info.nanodesu.model.db.collectors.gameinfo.ChartDataPoint
import info.nanodesu.model.db.collectors.gameinfo.ChartDataPackage
import info.nanodesu.model.db.collectors.gameinfo.ChartPlayer

case class AddGameChartDataMsg(msgs: Map[String, List[ChartDataPoint]]) extends MapFlatteningTriggerMessage[ChartDataPoint] {
  def input = msgs
  def map(key: String, event: ChartDataPoint) = Map("playerId" -> key, "data" -> event)
  def triggerName = "new-chart-data"
}

case class AddPlayersMsg(players: Map[String, ChartPlayer]) extends TriggerMessage {
  def triggerName = "new-player-data"
  def messageValues = {
    players.toList.map(x => Map("id" -> x._1, "data" -> x._2))
  }
}
 
class GameChartComet extends ServerGameComet {
  def nameKey = CometInit.gameChartComet
		  
  private var chartDataKnownTime = 0L
  private var knownPlayerIds: Set[String] = Set.empty
  
  protected def prepareShutdown() = {
    knownPlayerIds = Set.empty
  }
  
  protected def pushDataToClients(server: GameServerActor) {
    val chartData = server.chartData.makePackage
    checkForAddedPlayers(chartData.playerInfo)
    checkForAddedChartData(chartData.playerTimeData)
  }
  
  private def checkForAddedPlayers(fromPackage: Map[String, ChartPlayer]) = {
    val prevKnown = knownPlayerIds
    val filtered = fromPackage.filterKeys(!prevKnown.contains(_))
    if (filtered.nonEmpty) {
      for (key <- filtered.keys) {
        knownPlayerIds += key
      }
      partialUpdate(AddPlayersMsg(filtered))
    }
  }
  
  private def checkForAddedChartData(fromPackage: Map[String, List[ChartDataPoint]]) = {
    val knownTime = chartDataKnownTime
    val newData = (for (lists <- fromPackage.toList.map(x => (x._1, x._2.filter(_.timepoint > knownTime)))) yield lists).toMap
    val noEmptyPlayers = newData.filter(!_._2.isEmpty)
    if (noEmptyPlayers.nonEmpty) {
      for (v <- noEmptyPlayers.values; d <- v) {
        recheckKnownTime(d.timepoint)
      }
      partialUpdate(AddGameChartDataMsg(noEmptyPlayers))
    }
  }
  
  private def recheckKnownTime(time: Long) = {
    if (chartDataKnownTime < time) {
      chartDataKnownTime = time
    }
  }
  
  def coreRender = {
	chartDataKnownTime = 0
	knownPlayerIds = Set.empty
    val foo = for (gId <- getGameId) yield {
      new GameChartDataRenderer(gId, true).render
    }
    val d = foo.openOr("#noop" #> "")
    d
  }
}