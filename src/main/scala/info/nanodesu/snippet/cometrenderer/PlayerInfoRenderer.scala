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

class PlayerInfoRenderer(val gId: Int) extends CometRenderer {
  def render = {
    val activeReporters = new ActiveReportersForGameLoader

    import PlayerGameInfo._

    val transform = CookieBox withSession { db =>
      for (p <- activeReporters.selectActiveReportersWithName(db, gId)) yield {
        val inf = GameAndPlayerInfoCollector(db, p.id, gId)

        val primColor = if (p.primaryColor == null) "#000" else p.primaryColor
        val secColor = if (p.secondaryColor == null) "#000" else p.secondaryColor
        
        ".playername *+" #> p.name &
          ".army_primary_color [style]" #> ("background:"+primColor+";") &
          ".army_secondary_color [style]" #> ("background:"+secColor+";") &
          ".avgbuildspeed *" #> inf.buildSpeed &
          ".summetal *" #> inf.sumMetal &
          ".metalused *" #> inf.metalUseAvg &
          ".sumenergy *" #> inf.sumEnergy &
          ".energyused *" #> inf.energyUseAvg &
          ".apmavg *" #> inf.apmAvg &
          ".playername [id]" #> playerName(gId, p.id) &
          ".avgbuildspeed [id]" #> avgBuildSpeed(gId, p.id) &
          ".summetal [id]" #> sumMetal(gId, p.id) &
          ".metalused [id]" #> metalUsed(gId, p.id) &
          ".sumenergy [id]" #> sumEnergy(gId, p.id) &
          ".energyused [id]" #> energyUsed(gId, p.id) &
          ".apmavg [id]" #> apmAvg(gId, p.id)
      }
    }

    "#pstatline" #> (transform)
  }
}