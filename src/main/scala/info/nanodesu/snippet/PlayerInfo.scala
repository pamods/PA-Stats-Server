package info.nanodesu.snippet

import net.liftweb.http.DispatchSnippet
import net.liftweb.common.Loggable
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers
import net.liftweb.http.S
import java.text.MessageFormat
import net.liftweb.common.Box
import net.liftweb.common.Full
import net.liftweb.common.Empty
import info.nanodesu.snippet.lib.JSLocalTime
import info.nanodesu.pages.PlayerPage
import info.nanodesu.lib.Formattings._
import info.nanodesu.model.db.collectors.playerinfo.PlayerInfoCollector
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.collectors.playerinfo.PlayerHistoryInfo
import info.nanodesu.model.db.collectors.playerinfo.PlayerHistoryEntry

object PlayerInfo extends DispatchSnippet with Loggable {

  val dispatch: DispatchIt = {
    case "info" => doInfo
    case "listNameHistory" => doNameHistory
  }

  private def selectedPlayer = PlayerPage.getPlayerId openOr -1

  private def doInfo = {
    val inf = CookieBox withSession { db =>
      PlayerInfoCollector(db, selectedPlayer)
    }

    val p = selectedPlayer
    "#playerName *" #> inf.currentDisplayName &
      "#gamesplayed *" #> inf.gamesCount &
      "#gametimesum *" #> inf.playerGameTime &
      "#gametimeavg *" #> inf.playerGameTimeAvg &
      "#avgapm *" #> inf.apmAvg &
      "#summetal *" #> inf.sumMetal &
      "#sumenergy *" #> inf.sumEnergy &
      "#metalusageavg *" #> inf.metalUseAvg &
      "#energyusageavg *" #> inf.energyUseAvg &
      "#avgbuildspeed *" #> inf.buildSpeed
  }

  private def doNameHistory = "#line" #> {
    val nameHistory = CookieBox withSession { db =>
      PlayerHistoryInfo(db, selectedPlayer).displayNameHistory
    }

    nameHistory map {
      case PlayerHistoryEntry(name, time) =>
        ".historyname *" #> name &
          ".historytime *" #> JSLocalTime.jsTimeSnipFor(time)
    }
  }
}