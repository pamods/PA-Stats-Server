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

class GameInfoRenderer(val gId: Int) extends CometRenderer{

  def render = {
	  def getGame(gameId: Int) = CookieBox withSession { db =>
	    GameInfoCollector(db, gameId)
	  }
	  val gameTitleCollector = CookieBox withSession (GameTitleCollector(_))
	  gameTitleCollector.shouldCreateLinks = true
	  val transform = for (game <- getGame(gId)) yield {
		  val playerList = gameTitleCollector.createGameTitle(gId)

		  val planetTransform = for (planet <- CookieBox withSession (db => PlanetCollector(db, gId))) yield {
		      "#size *+" #> planet.size &
	  		  "#radius *+" #> planet.radius &
	  		  "#heightrange *+" #> planet.heightRange &
	  		  "#waterheight *+" #> planet.waterHeight &
	  		  "#planetimg [src]" #> planet.imagePath &
	  		  "#planetimg [title+]" #> planet.biome &
	  		  "#planetseed *+" #> planet.seed &
	  		  "#planetname *+" #> planet.name &
	  		  "#temp *+" #> planet.temp
		  }
		  
		  val base = "#gamenum *+" #> gId &
		  "#players *+" #> playerList &
		  "#version *+" #> game.paVersion &
		  "#winner *+" #> game.winner &
		  "#start *+" #> JSLocalTime.jsTimeSnipFor(game.startTime) &
		  "#length *+" #> prettyTimespan(game.duration)
		  
		  planetTransform match {
		    case Some(tx) => base & tx
		    case _ => base
		  }
	  }
	  
	  val foo = transform getOrElse "*" #> ""
	  foo
  }
  
}