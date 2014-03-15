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
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventPackage
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventPlayer
import info.nanodesu.model.db.collectors.gameinfo.ArmyEvent
import net.liftweb.json._
import net.liftweb.http.js.JE
import scala.actors.ActorTask
import net.liftweb.http.S
import net.liftweb.common.Empty
import info.nanodesu.lib.db.CookieBox
import net.liftweb.common.Loggable
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventPackage
import net.liftweb.common.Box
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventDataCollector
import net.liftweb.util.CssSel
import info.nanodesu.snippet.cometrenderer.ArmyCompositionRenderer
import net.liftweb.http.ShutDown
import net.liftweb.http.ShutdownIfPastLifespan

// TODO  handling of these classes in this file looks redundant to me
case class AddEventsMsg(msgs: Map[String, List[ArmyEvent]]) extends MapFlatteningTriggerMessage[ArmyEvent] {
  def input = msgs

  def map(key: String, event: ArmyEvent) = {
   Map("id" -> event.id, "playerId" -> key, "spec" -> event.spec, "time" -> event.time, "watchType" -> event.watchType, "x" -> event.x, "y" -> event.y, "z" -> event.z, "planetId" -> event.planetId)
  }
  
  def triggerName = "new-army-events"
}

case class AddPlayerMessage(msgs: Map[String, List[ArmyEventPlayer]]) extends MapFlatteningTriggerMessage[ArmyEventPlayer] {
  def input = msgs
  
  def map(key: String, e: ArmyEventPlayer) = {
    Map("playerId" -> key, "name" -> e.name, "pColor" -> e.primaryColor, "sColor" -> e.secondaryColor)
  }
 
  def triggerName = "new-players"
}

class GameArmyComposition extends ServerGameComet {
	def nameKey = CometInit.gameArmyComposition
	
	private var armyEventsMap: Set[Int] = Set.empty
	private var armyPlayers: Set[String] = Set.empty
	
	protected def prepareShutdown() = {
	  armyEventsMap = Set.empty
	  armyPlayers = Set.empty
	  gameServer = None
	}
	
	protected def pushDataToClients(server: GameServerActor) {
	    val composition = server.armyComposition.makePackage
	    checkForAddedPlayers(composition.playerInfo)
	    checkForAddedEvents(composition.playerEvents)
	}
	
	private def checkForAddedPlayers(fromPackage: Map[String, ArmyEventPlayer]) = {
	  var changes: Map[String, List[ArmyEventPlayer]] = Map.empty withDefaultValue(Nil)
	  for (pair <- fromPackage) {
	    if (!armyPlayers.contains(pair._1)) {
	      armyPlayers += pair._1
	      val changedList = (changes(pair._1))
	      changes += (pair._1 -> (pair._2 :: changedList))
	    }
	  }
	  if (changes.nonEmpty) {
	    val re = changes.mapValues(x => x.reverse)
	    partialUpdate(AddPlayerMessage(re))
	  }
	}
	
	private def checkForAddedEvents(fromPackage: Map[String, List[ArmyEvent]]) = {
	  var changes: Map[String, List[ArmyEvent]] = Map.empty withDefaultValue(Nil)
	  for (pair <- fromPackage; event <- pair._2) {
	    if (!armyEventsMap.contains(event.id)) {
	      armyEventsMap += event.id
	      val changedList = changes(pair._1)
	      changes += (pair._1 -> (event :: changedList))
	    }
	  }
	  if (changes.nonEmpty) {
	    val re = changes.mapValues(x => x.reverse)
	    partialUpdate(AddEventsMsg(re))
	  }
	}
	
	protected def coreRender = {
	  armyEventsMap = Set.empty
	  armyPlayers = Set.empty
	  val foo = for (gId <- getGameId) yield {
	    new ArmyCompositionRenderer(gId, true).render
	  }
	  val d = foo.openOr("#noop" #> "")
	  d
	}
}