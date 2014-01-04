package info.nanodesu.snippet.cometrenderer

import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Helpers._
import net.liftweb.http.S
import net.liftweb.util.Helpers
import scala.math.BigDecimal
import info.nanodesu.model.ReportedPlanet
import net.liftweb.util.Props
import net.liftweb.common.Full
import info.nanodesu.comet.PlayerGameInfo
import net.liftweb.common.Box
import info.nanodesu.snippet.lib._
import info.nanodesu.pages.GamePage
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.lib.Formattings._
import info.nanodesu.model.db.collectors.gameinfo._
import info.nanodesu.model.db.collectors.gameinfo.HasSomeLockedPlayersCollector
import info.nanodesu.comet.GameArmyComposition
import net.liftweb.json._
import net.liftweb.json.DefaultFormats
import info.nanodesu.comet.GameChartComet

class GameChartDataRenderer(gId: Int, cometToInitialize: Option[GameChartComet] = None) extends CometRenderer {
  private implicit val formats = net.liftweb.json.DefaultFormats
  def render = {
    val loaded = CookieBox withSession (ChartDataCollector(_).collectDataFor(gId))
    for (chartComet <- cometToInitialize; data <- loaded.playerTimeData.values.flatten) {
      chartComet.recheckKnownTime(data.timepoint)
    }
    val data = Extraction decompose loaded
    "#chartDataSource [data-chart-info]" #> compact(net.liftweb.json.render(data))
  }
}