package info.nanodesu.comet

import scala.math.BigDecimal.double2bigDecimal
import scala.math.BigDecimal.int2bigDecimal
import scala.xml.Text
import info.nanodesu.snippet.lib._
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.util.Helpers.intToTimeSpanBuilder
import info.nanodesu.snippet.GameInfo
import net.liftweb.common.Full
import net.liftweb.http.js.JsCmd
import info.nanodesu.snippet.lib.CometInit

class GeneralGameInfo extends GameComet {
  def nameKey = CometInit.gameInfoKey

  override def lowPriority = {
    case GeneralGameJsCmd(id: Int, cmd: JsCmd) if (isMyGame(id)) => partialUpdate(cmd)
  }

  def render = GameInfo.renderGameInfo(getGameId)
}