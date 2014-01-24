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

class PlayerInfoRenderer(val gId: Int, val server: Option[GameServerActor] = None) extends CometRenderer {
  def render = {
    val activeReporters = new ActiveReportersForGameLoader

    import PlayerGameInfo._
    
    def makeTransform(pid: Int, inf: GamePlayerInfo) = {
        val primColor = if (inf.primaryColor == null) "#000" else inf.primaryColor
        val secColor = if (inf.secondaryColor == null) "#000" else inf.secondaryColor
        
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
    
    val playerSummaries = server map (_.playerSummaries.getSummaries) getOrElse {
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