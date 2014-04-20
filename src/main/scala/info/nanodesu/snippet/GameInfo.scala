package info.nanodesu.snippet

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

object GameInfo extends DispatchSnippet with Loggable {
	val dispatch: DispatchIt = {
	  case "info" => doInfo
	  case "livenote" => doLiveNoteText
	}

	private def loadGameId = GamePage.getGameId
	
	private def hasLockedPlayers = CookieBox withSession(HasSomeLockedPlayersCollector(_, loadGameId.openOr(-1)))
	
	private def doLiveNoteText = "#live_note_txt *" #> ("Game is live" + 
	    (if (hasLockedPlayers) " and will show more data for some players once it has ended!"else"!"))
	
	private def doInfo = renderGameInfo(loadGameId)
	
	def renderGameInfo(gameId: Box[Int]) = {
	  val boxed = for (gId <- gameId) yield {
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
			  "#length *+" #> prettyTimespan(game.duration) &
			  "#replaylink [href]" #> ("startpa://replay="+game.lobbyId)
			  
			  planetTransform match {
			    case Some(tx) => base & tx
			    case _ => base
			  }
		  }
		  
		  val foo = transform getOrElse "*" #> ""
		  foo
	  }
	  boxed.getOrElse("#noop" #> "")
	}
}