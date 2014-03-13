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
import net.liftweb.json._
import net.liftweb.json.DefaultFormats
import info.nanodesu.model.db.collectors.gameinfo.loader.GameTimesLoader
import info.nanodesu.model.db.collectors.gameinfo.loader.PlanetSizeLoader

class ArmyCompositionRenderer(val gId: Int, val hasComet: Boolean = false) extends CometRenderer with Loggable{
  def render = {
    implicit val formats = DefaultFormats

    val gameStart = CookieBox withSession { db => new GameTimesLoader(db).selectStartTimeForGame(gId) }
    val planetSizes = CookieBox withSession { db => new PlanetSizeLoader(db).selectPlanetSizes(gId) }

    "#armyDataSource [data-comet-info]" #> compact(net.liftweb.json.render(
        Extraction decompose Map("hasComet" -> hasComet, "gameStart" -> gameStart.getOrElse(-1),
            "planets" -> planetSizes)
    ))
  }
}