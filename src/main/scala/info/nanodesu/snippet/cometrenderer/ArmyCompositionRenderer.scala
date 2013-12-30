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

class ArmyCompositionRenderer(val gId: Int, val cometToBeInitialized: Option[GameArmyComposition] = None) extends CometRenderer {
  def render = {
    implicit val formats = DefaultFormats
    val loaded = CookieBox withSession { db => ArmyEventDataCollector(db).collectEventsFor(gId) }
    for (armyComet <- cometToBeInitialized) {
      armyComet.initSendMapByPackage(loaded)
    }
    "#armyDataSource [data-army-info]" #> compact(net.liftweb.json.render(Extraction decompose loaded))
  }
}