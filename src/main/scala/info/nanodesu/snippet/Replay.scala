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
	  
	  "#replayinfo *+" #> lobbyId &
	  "#replayinfo [href]" #> lobbyId.map(startPa+_) &
	  "#redirectmeta" #> lobbyId.map(x => (<meta data-lift="head" http-equiv="Refresh"></meta> %
	      Attribute(None, "content", Text(s"0; url=$startPa$x"), Null))) &
	  "#gameLink" #> gameId.map(x => GamePage.makeLink("Game #"+x, GameIdParam(x)))
	}
}