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
import net.liftweb.json.Extraction
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import info.nanodesu.lib.Formattings._
import info.nanodesu.model.db.collectors.playerinfo.GameAndPlayerInfoCollector

object PlayerInfo extends DispatchSnippet with Loggable {

  val dispatch: DispatchIt = {
    case "info" => doInfo
    case "listNameHistory" => doNameHistory
    case "linkLadder" => linkLadder
  }

  private def selectedPlayer = PlayerPage.getPlayerId openOr -1

  private def doInfo = {
    implicit val formats = net.liftweb.json.DefaultFormats
    
    val inf = CookieBox withSession { db =>
      PlayerInfoCollector(db, selectedPlayer)
    }
    
    val txt = s"var chartdata = ${compact(render(Extraction decompose inf.dailyValues))}"
    
    val graphData = 
      <script type="text/javascript">{txt}</script>
    
    if (inf.isReporter) {
      "#playerName *" #> inf.currentDisplayName &
        "#gamesplayed *" #> inf.gamesCount &
        "#gametimesum *" #> inf.playerGameTime &
        "#gametimeavg *" #> inf.playerGameTimeAvg &
        "#avgapm *" #> (CookieBox withSession (GameAndPlayerInfoCollector.getAvgApm(_, selectedPlayer))) &
        // these values are not very helpful and cost a lot of cpu time to generate
      //  "#summetal *" #> formatKMBT(inf.sumMetal) &
      //  "#sumenergy *" #> formatKMBT(inf.sumEnergy) &
       // "#metalusageavg *" #> formatPercent(inf.metalUseAvg) &
       // "#energyusageavg *" #> formatPercent(inf.energyUseAvg) &
       // "#avgbuildspeed *" #> formatPercent(inf.buildSpeed) &
        "#timelinedatasource" #> graphData
    } else {
      "#playerName *" #> "unknown player ID!"
    }
  }

  private def linkLadder = "#ladderlink [href]" #> ("http://paladder.com/player/"+selectedPlayer)
  
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