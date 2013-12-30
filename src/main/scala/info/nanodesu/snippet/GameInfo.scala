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
import info.nanodesu.snippet.cometrenderer.GameInfoRenderer

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
	    new GameInfoRenderer(gId).render
	  }
	  boxed.getOrElse("#noop" #> "")
	}
}