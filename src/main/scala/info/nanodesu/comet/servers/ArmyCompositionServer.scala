package info.nanodesu.comet.servers

import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.collectors.gameinfo.loader.GameTimesLoader
import info.nanodesu.model.db.collectors.gameinfo.{ArmyEvent => DbArmyEvent}
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventPlayer
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventPackage
import info.nanodesu.model.ArmyEvent
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventDataCollector
import net.liftweb.common.Loggable

class ArmyCompositionServer(val gameId: Int, players: PlayersServer) extends Loggable {
	private var gameStart: Long = 0
	private var playerEvents: Map[String, List[DbArmyEvent]] = Map.empty
	private var eventIds = -1 // the IDs from init are from the db and can be only positive values, so it is save to use negative values here

	// this is only used in case of an init of a server that is not caused by a starting game
	// it's a bit dangerous (race conditions that can cause wrong game data in the comet),
	// but it should only happen after server restarts for running games
	def forcefulInit() {
	  val initialData = CookieBox withSession {ArmyEventDataCollector(_).collectEventsFor(gameId, true)}
	  gameStart = initialData.gameStart
	  playerEvents = initialData.playerEvents
	}
	
	def clearUp() = {
	  playerEvents = Map.empty
	}
	
	def addArmyEventsFor(playerId: Int, events: List[ArmyEvent]) = {
	  val pIdAsStr = playerId.toString
	  val evts = playerEvents.getOrElse(pIdAsStr, List.empty)
	  playerEvents += pIdAsStr -> (events.map{ x => 
        eventIds -= 1
	    DbArmyEvent(eventIds, x.spec, x.x.toInt, x.y.toInt, x.z.toInt, x.planetId, x.watchType, x.time)
	  } ::: evts)
	}
	
	def makePackage: ArmyEventPackage = {
	  ArmyEventPackage(gameId, gameStart, playerEvents.filterKeys(x => !players.players(x.toInt).locked),
	      players.players.map(x => (x._1.toString,
	          ArmyEventPlayer(x._2.name, x._2.primColor, x._2.secondaryColor))))
	}
}