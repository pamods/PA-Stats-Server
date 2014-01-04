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

case class AddGameChartDataMsg(msgs: Map[String, List[ChartDataPoint]]) extends MapFlatteningTriggerMessage[ChartDataPoint] {
  def input = msgs
  def map(key: String, event: ChartDataPoint) = Map("playerId" -> key, "data" -> event)
  def triggerName = "new-chart-data"
}

class GameChartComet extends GameComet {
  def nameKey = CometInit.gameChartComet
		  
  private var chartDataKnownTime = 0L
  
  override def lowPriority = {
    case GameChartUpdate(id: Int, chartData: ChartDataPackage) if isMyGame(id) => {
    	checkForAddedChartData(chartData.playerTimeData)
    	// no live update for new players, as new players are never added after the game started
    }
  }
  
  private def checkForAddedChartData(fromPackage: Map[String, List[ChartDataPoint]]) = {
    val newData = (for (lists <- fromPackage.toList.map(x => (x._1, x._2.filter(_.timepoint > chartDataKnownTime)))) yield lists).toMap
    if (newData.nonEmpty) {
      for (v <- newData.values; d <- v) {
        recheckKnownTime(d.timepoint)
      }
      partialUpdate(AddGameChartDataMsg(newData))
    }
  }
  
  def recheckKnownTime(time: Long) = {
    if (chartDataKnownTime < time) {
      chartDataKnownTime = time
    }
  }
  
  def render = {
    val foo = for (gId <- getGameId) yield {
      new GameChartDataRenderer(gId, Some(this)).render
    }
    val d = foo.openOr("#noop" #> "")
    d
  }
}