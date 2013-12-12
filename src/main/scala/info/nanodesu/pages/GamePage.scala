package info.nanodesu.pages

import info.nanodesu.model.db.collectors.gameinfo.loader.GameIdFromIdentLoader
import info.nanodesu.lib.db.CookieBox
import net.liftweb.common.Full
import net.liftweb.common.Empty

case class GameIdParam(id: Int) extends PageParam {
  val paramName = GamePage.idParam
  val paramValue = id.toString
}

object GamePage extends Page with GameIdFromIdentLoader{
	val pageName = "chart"
	
	protected[pages] val idParam = "gameId"
	protected[pages] val identParam = "gameIdent"
	  
	def makeLink(txt: String, gameId: GameIdParam) = makeLink0(txt, gameId)
	def makeLinkAttribute(gameId: GameIdParam) = makeLinkAttribute0(gameId)
	
	def getGameId = getParam(idParam, Integer.parseInt) or {
	  val x = for (ident <- getParam(identParam, a => a)) yield {
	    CookieBox withSession(getIdForIdent(_, ident))
	  } 
	  x match {
	    case Full(Some(a)) =>
	      Full(a)
	    case _ =>
	      Empty
	  }
	}
}