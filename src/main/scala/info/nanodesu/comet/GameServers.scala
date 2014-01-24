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
import java.util.concurrent.atomic.AtomicInteger
import info.nanodesu.model.db.collectors.gameinfo.ChartDataPackage
import info.nanodesu.model.db.collectors.gameinfo.ChartDataCollector
import info.nanodesu.comet.servers.ArmyCompositionServer
import info.nanodesu.model.ArmyEvent
import net.liftweb.http.CometActor
import info.nanodesu.snippet.lib.CometInit
import net.liftweb.util.Schedule
import net.liftweb.util.TimeHelpers
import net.liftweb.util.Props
import info.nanodesu.comet.servers.ChartDataServer
import info.nanodesu.model.StatsReportData
import info.nanodesu.comet.servers.PlayerSummaryServer

object GameServers {
    val cometCounter = new AtomicInteger(0)
  
	private var servers: Map[Int, LiftActor] = Map.empty
	
	// using some sort of concurrent map might be an idea in the future
	def serverForGame(gId: Int, shouldInitIfServerStarts: Boolean = true): LiftActor = synchronized {
	  servers.get(gId) match {
	    case Some(a) => a
	    case None => {
	      val newServer = new GameServerActor(gId)
	      if (shouldInitIfServerStarts) {
	        newServer.forceInit()
	      }
	      servers += gId -> newServer
	      newServer
	    }
	  }
	}

	def mayGetServer(gId: Int): Option[LiftActor] = synchronized {
	  if (hasGameServer(gId)) Some(serverForGame(gId))
	  else None
	}
	
	def removeGameServer(gId: Int) = synchronized {
	  servers -= gId
	}
	
	def hasGameServer(gId: Int) = synchronized {
	  servers.isDefinedAt(gId)
	}
}

case class NewPlayer(id: Int, name: String, primaryColor: String, secondaryColor: String)
case class NewPlayerEvents(playerId: Int, events: List[ArmyEvent])
case class NewChartStats(playerId: Int, time: Long, stats: StatsReportData)
case class RegisterCometActor(actor: CometActor)
case class UnregisterCometActor(actor: CometActor)
case class RegisterAcknowledged(server: GameServerActor)
case class PushUpdate(force: Boolean)
case class CheckGameRelevance()
case class ServerShutdown()

class GameServerActor(gameId: Int) extends LiftActor with Loggable {
  val cometServePastThreshold = Props.getInt("cometServeThreshold", 300000)
  val minUpdateInterval = Props.getInt("minCometInterval", 3500)
  val relevanceCheckTimeInMinutes = 1
  
  private var cometActorsToUpdate: List[CometActor] = List.empty
  
  private var lastUpdateTime = System.currentTimeMillis()

  val armyComposition = new ArmyCompositionServer(gameId)
  val chartData = new ChartDataServer(gameId)
  val playerSummaries = new PlayerSummaryServer(gameId)
  
  private var lastNewDataTime = System.currentTimeMillis()
  
  def forceInit() = {
    armyComposition.forcefulInit()
 	val initialData = CookieBox withSession (ChartDataCollector(_).collectDataFor(gameId))    
    chartData.forcefulInit(initialData)
    playerSummaries.forcefulInit(initialData)
  }
  
  override def messageHandler = {
    case NewChartStats(id, time, stats) =>
      chartData.addChartDataFor(id, time, stats)
      playerSummaries.addStats(id, time, stats)
      
    case NewPlayer(id, name, primaryColor, secondaryColor) =>
      // TODO this looks redundant to me
      chartData.setPlayerInfo(id, name, primaryColor)
      armyComposition.setPlayerInfo(id, name, primaryColor, secondaryColor)
      playerSummaries.setPlayerInfo(id, name, primaryColor, secondaryColor)
      
    case NewPlayerEvents(playerId, events: List[ArmyEvent]) =>
      armyComposition.addArmyEventsFor(playerId, events)
      
    case RegisterCometActor(actor: CometActor) =>
      if (!cometActorsToUpdate.contains(actor)) {
        cometActorsToUpdate = actor :: cometActorsToUpdate
        actor ! RegisterAcknowledged(this)
      }
    
    case UnregisterCometActor(actor: CometActor) => 
      cometActorsToUpdate = cometActorsToUpdate.filter(_ != actor)
    
    case PushUpdate(force: Boolean) =>
      if (cometActorsToUpdate.nonEmpty) {
        mayPushUpdate(force)
        resetLastNewData()
      }
      
    case CheckGameRelevance() => {
      if (isRelevant()) {
        scheduleGameRelevanceCheck()
      } else {
        cometActorsToUpdate.foreach(_ ! ServerShutdown())
        armyComposition.clearUp()
        chartData.clearUp()
        GameServers.removeGameServer(gameId)
      }
    }
  }
  
  private def resetLastNewData() = {
    lastNewDataTime = System.currentTimeMillis()
  }
  
  private def isRelevant(): Boolean = {
    cometServePastThreshold + lastNewDataTime > System.currentTimeMillis()
  }
  
  private def scheduleGameRelevanceCheck() {
    Schedule.schedule(this, CheckGameRelevance(), TimeHelpers.minutes(relevanceCheckTimeInMinutes))
  }
  
  scheduleGameRelevanceCheck()
  
  private def mayPushUpdate(force: Boolean) = {
    if (force || System.currentTimeMillis() - minUpdateInterval > lastUpdateTime) {
      lastUpdateTime = System.currentTimeMillis()
      val up = PushUpdate(force)
	  cometActorsToUpdate.foreach(_ ! up)
    }
  }
}