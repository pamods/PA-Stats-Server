package info.nanodesu.snippet

import scala.xml.Attribute
import scala.xml.Null
import scala.xml.Text
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.collectors.playersearch.PlayerListCollector
import net.liftweb.common.Loggable
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.util.Helpers.strToCssBindPromoter
import info.nanodesu.pages.PlayerPage
import info.nanodesu.pages.IdParam
import info.nanodesu.pages.PlayerSearchPage

object PlayerSearch extends DispatchSnippet with Loggable {
  val dispatch: DispatchIt = {
    case "input" => doInput
    case "list" => doList
  }

  private def doInput = "#searchfield [value]" #> PlayerSearchPage.getSearchTerm

  private def doList = {
    val data = CookieBox withSession { db =>
      PlayerListCollector(db, PlayerSearchPage.getSearchTerm openOr "")
    }

    "#line" #> data.searchResult.map(x => {
      ".namelink *" #> (<a style="display:block;">{ x.name }</a> % PlayerPage.makeLinkAttribute(IdParam(x.id)))
    })
  }
}