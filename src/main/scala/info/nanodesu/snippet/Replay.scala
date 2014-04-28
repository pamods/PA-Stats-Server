package info.nanodesu.snippet

import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Props
import scala.xml.Attribute
import scala.xml.Text
import scala.xml.Null
import scala.xml.NodeSeq
import scala.Array.canBuildFrom
import net.liftweb.util.Helpers._
import info.nanodesu.pages.ReplayPage
import info.nanodesu.model.ReportDataC
import info.nanodesu.model.db.collectors.gameinfo.loader.GameIdFromIdentLoader
import info.nanodesu.lib.db.CookieBox
import net.liftweb.common.Empty
import net.liftweb.common.Full
import info.nanodesu.pages.GamePage
import info.nanodesu.pages.GameIdParam
import info.nanodesu.model.db.collectors.gameinfo.GameTitleCollector
import info.nanodesu.model.db.collectors.gameinfo.GameInfoCollector
import info.nanodesu.snippet.lib.JSLocalTime

object Replay extends DispatchSnippet{
	val dispatch: DispatchIt = {
	  case "relink" => relinkReplay
	}
	
	def relinkReplay = {
	  val startPa = "startpa://replay="
	  val lobbyId = ReplayPage.getReplayId
	  
	  val gameIdBox = (CookieBox withSession {db => 
	    for (l <- lobbyId) yield {
	      val loader = new Object with GameIdFromIdentLoader
	      val gameId = loader.getIdForIdent(db, l)
	      if (gameId.isDefined) Full(gameId.get)
	      else Empty
	    }
	  })
	  
	  val gameId = gameIdBox match {
	    case Full(x) => x
	    case _ => Empty
	  }
	  
	  val gameTitle = gameId.map(x => (CookieBox withSession (GameTitleCollector(_, true))).createGameTitle(x))
	  
	  def getGame(gameId: Int) = CookieBox withSession { db =>  GameInfoCollector(db, gameId) }
	  
	  val replayDate = gameId.map(x => getGame(x).map(g => JSLocalTime.jsTimeSnipFor(g.startTime)).getOrElse(NodeSeq.Empty))
	  
	  "#replayinfo *+" #> lobbyId &
	  "#replayinfo [href]" #> lobbyId.map(startPa+_) &
	  "#redirectmeta" #> lobbyId.map(x => (<meta data-lift="head" http-equiv="Refresh"></meta> %
	      Attribute(None, "content", Text(s"0; url=$startPa$x"), Null))) &
	  "#gameLink" #> gameId.map(x => GamePage.makeLink("Game #"+x, GameIdParam(x))) & 
	  "#playerlist *" #> gameTitle &
	  "#replaydate" #> replayDate
	}
}