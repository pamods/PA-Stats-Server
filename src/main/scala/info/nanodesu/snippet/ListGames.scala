package info.nanodesu.snippet

import net.liftweb.common.Loggable
import net.liftweb.http._
import net.liftweb.http.S._
import net.liftweb.http.SHtml._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.jquery.JqJsCmds._
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers
import java.sql.Connection
import net.liftweb.util.TimeHelpers
import java.util.Date
import net.liftweb.util.Props
import scala.xml.Attribute
import scala.xml.Null
import scala.xml.Text
import net.liftweb.common.Empty
import net.liftweb.common.Full
import scala.xml.NodeSeq
import java.util.Arrays
import java.util.regex.Pattern
import info.nanodesu.lib.Formattings
import info.nanodesu.snippet.lib.JSLocalTime
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.collectors.gameinfo.GameTitleCollector
import info.nanodesu.model.db.collectors.gamelist.GameListCollector
import info.nanodesu.snippet.lib.PagedSnippet
import info.nanodesu.model.db.collectors.gameinfo.loader.CountGamesLoader
import info.nanodesu.model.db.collectors.playerinfo.loader.CountGamesForPlayerLoader
import info.nanodesu.pages._

/**
 * Snippet lists games, either all of them or limited to one player.
 * The limitation is done by the presence of an additional get parameter 
 * with the player id
 */
object ListGames extends DispatchSnippet with PagedSnippet {
  val dispatch: DispatchIt = {
    case "pages" => doPages
    case "list" => doList
  }
  
  def currentPageParameter = "listpage"
  def elementCount = CookieBox withSession { db =>
    selectedPlayer match {
      case Full(id) => new CountGamesForPlayerLoader(db).selectPlayerGamesCount(id)
      case _ => new CountGamesLoader(db).selectGameCount.toInt
    }
  }
  def pageMaxSize = Props.getInt("listGamesPageSize", 20)
  def selectedLinkAttr = "white"
    
  private def selectedPlayer = PlayerPage.getPlayerId
  
  private def doPages = ".pages *" #> renderPages

  private def doList = "#line" #> {
    val ourPlayer = selectedPlayer
    val games = (CookieBox withSession (GameListCollector(_))).getGameList(offset, pageMaxSize, ourPlayer)

    games.map(x => {
      ".gamenum *" #> (<a>#{ x.id }</a> % GamePage.makeLinkAttribute(GameIdParam(x.id))) &
        ".gameplayers *" #> x.title &
        ".gamestart *" #> JSLocalTime.jsTimeSnipFor(x.startTime) &
        ".gametime *" #> Formattings.formatGameDuration(x.startTime.getTime(), x.endTime.getTime())
    })
  }
}