package info.nanodesu.comet

import net.liftweb.actor.LiftActor
import net.liftweb.http.ListenerManager
import net.liftweb.common.Loggable
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.SetHtml
import scala.xml.Text
import info.nanodesu.snippet.ListGames
import net.liftweb.http.js.JsCmds
import info.nanodesu.snippet.PlayerInfo
import net.liftweb.common._
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.collectors.playerinfo.GameAndPlayerInfoCollector
import info.nanodesu.lib.Formattings
import info.nanodesu.model.db.collectors.gameinfo.loader._
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventPackage
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventDataCollector

case class GameDataUpdate(gameId: Int)
case class ForceGameDataUpdate(gameId: Int)

case class GameArmyCompositionUpdate(gameId: Int, composition: ArmyEventPackage)

case class GeneralGameJsCmd(gameId: Int, cmd: JsCmd)
case class GamePlayersListJsCmd(gameId: Int, cmd: JsCmd)

object GameCometServer extends LiftActor with ListenerManager with Loggable{
    @volatile
    private var timingsMap: Map[Int, Long] = Map()
    
    private var cleanTime = System.currentTimeMillis()
    
    private def minimumTimeForUpdateReached(gId: Int) = {
      gcMap()
      
      val timing = timingsMap.get(gId) getOrElse 0L
      if (System.currentTimeMillis() - timing > 4500) {
        timingsMap = timingsMap + ((gId, System.currentTimeMillis()))
        true
      } else false
    }
    
    private def gcMap() = {
      if (System.currentTimeMillis() - cleanTime > 1000 * 60 * 10) {
        timingsMap = timingsMap.filter(System.currentTimeMillis() - _._2 < 1000 * 60)
        cleanTime = System.currentTimeMillis()
      }
    }
    
	override def lowPriority = {
	  case GameDataUpdate(id: Int) => {
	    if (minimumTimeForUpdateReached(id)) {
	    	doUpdateNow(id)
	    }
	  }
	  case ForceGameDataUpdate(id: Int) => {
	    doUpdateNow(id)
	  }
	}
	
	def doUpdateNow(id: Int) = {
	  updateListeners(GeneralGameJsCmd(id, createGeneralGameUpdate(id)))
      updateListeners(GamePlayersListJsCmd(id, createPlayersListUpdate(id)))
      updateListeners(createArmyCompositionUpdate(id))	  
	}
	
	private def createArmyCompositionUpdate(id: Int) = 
	  GameArmyCompositionUpdate(id, CookieBox withSession (ArmyEventDataCollector(_).collectEventsFor(id)))
	
	private def createPlayersListUpdate(id: Int) = {
	  import PlayerGameInfo._
	  
	  CookieBox withSession { db =>
	    val activeReporters = new ActiveReportersForGameLoader
	    val cmds = for (p <- activeReporters.selectActiveReportersFor(db, id)) yield {
		    val inf = GameAndPlayerInfoCollector(db, p, id)
		    val builder = setHtmlBuilder(p, id) _
		    builder(inf.buildSpeed, avgBuildSpeed) &
		    builder(inf.sumMetal, sumMetal) &
		    builder(inf.metalUseAvg, metalUsed) &
		    builder(inf.sumEnergy, sumEnergy) &
		    builder(inf.energyUseAvg, energyUsed) &
		    builder(inf.apmAvg, apmAvg)
	    }
        cmds.reduce(_&_)
	  }
	}
	
	private def setHtmlBuilder(p: Int, g: Int)(value: Any, idFunc: (Int, Int) => String) = {
	  makeSetHtmlCmd(Full(value.toString), idFunc(g, p))
	}
	
	private def makeSetHtmlCmd(value: Box[String], id: String) = value.map(x => SetHtml(id, Text(x))) openOr JsCmds.Noop
	
	private def createGeneralGameUpdate(id: Int) = {
	  def loadUpdateInfo = {
	    val loader = new Object with UpdatingGameInfoLoader
	    CookieBox withSession (loader.selectWinnerAndDurationForGame(_, id))
	  }
	  
      (for (game <- loadUpdateInfo) yield {
        SetHtml("length", Text("Duration: "+Formattings.prettyTimespan(game.duration))) &
          (if (game.winner != "unknown") SetHtml("winner", Text("Winner: "+game.winner)) else JsCmds.Noop)
      }) getOrElse JsCmds.Noop
	}

	def createUpdate = "Registered"
}