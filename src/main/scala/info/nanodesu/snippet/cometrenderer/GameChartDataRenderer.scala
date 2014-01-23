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

class GameChartDataRenderer(gId: Int, val hasComet: Boolean = false) extends CometRenderer {
  private implicit val formats = net.liftweb.json.DefaultFormats
  def render = {
    val data = Extraction decompose Map("hasComet" -> hasComet)
    "#chartDataSource [data-comet-info]" #> compact(net.liftweb.json.render(data))
  }
}