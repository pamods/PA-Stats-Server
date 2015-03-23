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
import info.nanodesu.pages._
import info.nanodesu.model.db.collectors.playerinfo.loader.IsReporterLoader
import info.nanodesu.model.db.collectors.playerinfo.loader.PlayerNameLoader

/**
 * Snippet lists games, either all of them or limited to one player/system.
 * The limitation is done by the presence of an additional get parameter 
 * with the player id/system name
 */
object ListGames extends DispatchSnippet with PagedSnippet {
  val dispatch: DispatchIt = {
    case "pages" => doPages
    case "list" => doList
    case "playerFilterValue" => doPlayerFilterValue
    case "systemFilterValue" => doSystemFilterValue
  }
  
  private def selectedPlayer = PlayerPage.getPlayerId
  private def selectedSystem = S.param("system") match {
    case Full("") => Empty
    case x => x
  }
  
  private def doPlayerFilterValue = "* [value]" #> (CookieBox withSession { db => new PlayerNameLoader(db).selectPlayerName(PlayerPage.getPlayerId.openOr(-1))}) 
  private def doSystemFilterValue = "* [value]" #> selectedSystem
  
  def currentPageParameter = "listpage"
  def elementCount = CookieBox withSession { db =>
    new CountGamesLoader(db).selectFilteredGamesCount(selectedPlayer, selectedSystem)
  }
  def pageMaxSize = Props.getInt("listGamesPageSize", 20)
  def selectedLinkAttr = "white"
  
  private def doPages = ".pages *" #> renderPages

  private def doList = "#line" #> {
    val ourPlayer = selectedPlayer
    val ourSystem = selectedSystem
    val games = CookieBox withSession { db =>
      if (ourPlayer.isEmpty || new IsReporterLoader().selectIsReporter(db, ourPlayer.getOrElse(-1))) {
        GameListCollector(db).getGameList(offset, pageMaxSize, ourPlayer, ourSystem)
      } else {
        Nil
      }
    }

    games.map(x => {
      ".gamenum *" #> (<a>#{ x.id }</a> % GamePage.makeLinkAttribute(GameIdParam(x.id))) &
        ".gameplayers *" #> x.title &
        ".gamestart *" #> JSLocalTime.jsTimeSnipFor(x.startTime) &
        ".gametime *" #> Formattings.formatGameDuration(x.startTime.getTime(), x.endTime.getTime()) &
        ".gamewinner *" #> x.winner &
        ".gamesystem *" #> x.system 
    })
  }
}