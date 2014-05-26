package info.nanodesu.snippet.cometrenderer

import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Helpers._
import info.nanodesu.snippet.GameInfo
import net.liftweb.util.Helpers._
import info.nanodesu.pages.GamePage
import net.liftweb.common.Box
import info.nanodesu.model.db.collectors.gameinfo.loader.ActiveReportersForGameLoader
import info.nanodesu.comet.PlayerGameInfo
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.collectors.playerinfo.GameAndPlayerInfoCollector
import info.nanodesu.model.db.collectors.gameinfo.loader.GameTimesLoader
import net.liftweb.common.Loggable
import info.nanodesu.model.db.collectors.playerinfo.GamePlayerInfo
import info.nanodesu.comet.GameServers
import info.nanodesu.comet.GameServerActor
import info.nanodesu.lib.Formattings._
import org.apache.commons.lang.StringUtils

class PlayerInfoRenderer(val gId: Int, val server: Option[GameServerActor] = None) extends CometRenderer with Loggable {
  def render = {
    val activeReporters = new ActiveReportersForGameLoader

    import PlayerGameInfo._
    
    def makeTransform(pid: Int, inf: GamePlayerInfo) = {
        val primColor = if (StringUtils.isBlank(inf.primaryColor)) "rgb(16,16,16)" else inf.primaryColor
        val secColor = if (StringUtils.isBlank(inf.secondaryColor)) "rgb(168,0,0)" else inf.secondaryColor
        
        ".playername *+" #> inf.name &
          ".army_primary_color [style]" #> ("background:"+primColor+";") &
          ".army_secondary_color [style]" #> ("background:"+secColor+";") &
          ".avgbuildspeed *" #> formatPercent(inf.buildSpeed) &
          ".summetal *" #> formatKMBT(inf.sumMetal) &
          ".metalused *" #> formatPercent(inf.metalUseAvg) &
          ".sumenergy *" #> formatKMBT(inf.sumEnergy) &
          ".energyused *" #> formatPercent(inf.energyUseAvg) &
          ".apmavg *" #> inf.apmAvg &
          ".playername [id]" #> playerName(gId, pid) &
          ".avgbuildspeed [id]" #> avgBuildSpeed(gId, pid) &
          ".summetal [id]" #> sumMetal(gId, pid) &
          ".metalused [id]" #> metalUsed(gId, pid) &
          ".sumenergy [id]" #> sumEnergy(gId, pid) &
          ".energyused [id]" #> energyUsed(gId, pid) &
          ".apmavg [id]" #> apmAvg(gId, pid)
    }
    
    val playerSummaries = server map (_.gameSummary.getSummaries) getOrElse {
      CookieBox withSession { db =>
	      val listed = for (p <- activeReporters.selectActiveReportersWithName(db, gId)) yield {
	        val inf =  GameAndPlayerInfoCollector(db, p.id, gId)
	        inf.name = p.name
	        inf.primaryColor = p.primaryColor
	        inf.secondaryColor = p.secondaryColor
	        p.id -> inf
	      }
	      listed.toMap
      }
    }

    "#pstatline" #> (playerSummaries.map(x => makeTransform(x._1, x._2)))
  }
}