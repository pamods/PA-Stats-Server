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
import info.nanodesu.model.db.collectors.gameinfo.loader.GameStartTimeLoader
import net.liftweb.common.Loggable
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventPackage
import net.liftweb.common.Box
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventDataCollector
import net.liftweb.util.CssSel

// TODO these 2 case classes and their handling in this file look redundant to me
case class AddEventsMsg(msgs: Map[String, List[ArmyEvent]]) extends JsCmd {
  implicit val formats = DefaultFormats
  
  def json: JValue = {
    val lst = msgs.toList.map(x => {
      for (e <- x._2) yield {
        Map("playerId" -> x._1, "spec" -> e.spec, "time" -> e.time, "watchType" -> e.watchType)
      }
    }).flatten
    Extraction decompose Map("value" -> lst)
  }
  
  override val toJsCmd = JE.JsRaw("""$(document).trigger('new-army-events', %s);""".format(compact(net.liftweb.json.render(json)))).toJsCmd
}

case class AddPlayerMessage(msgs: Map[String, List[ArmyEventPlayer]]) extends JsCmd {
  implicit val formats = DefaultFormats
  
  def json: JValue = {
    val lst = msgs.toList.map(x => {
      for (e <- x._2) yield {
        Map("playerId" -> x._1, "name" -> e.name, "pColor" -> e.primaryColor, "sColor" -> e.secondaryColor)
      }
    }).flatten
    Extraction decompose Map("value" -> lst)
  }
  
  override val toJsCmd = JE.JsRaw("""$(document).trigger('new-players', %s);""".format(compact(net.liftweb.json.render(json)))).toJsCmd
}

class GameArmyComposition extends GameComet {
	def nameKey = CometInit.gameArmyComposition
	
	private var armyEventsMap: Set[Int] = Set.empty
	private var armyPlayers: Set[String] = Set.empty

	override def lowPriority = {
	  case GameArmyCompositionUpdate(id: Int, composition: ArmyEventPackage) if isMyGame(id) => {
	    checkForAddedPlayers(composition.playerInfo)
	    checkForAddedEvents(composition.playerEvents)
	  }
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
	
	private def initSendMapByPackage(pack: ArmyEventPackage) {
	  val ids = pack.playerEvents.map(x => x._2.map(_.id)).flatten
	  armyEventsMap ++= ids
	  val pIds = pack.playerInfo.map(x => x._1)
	  armyPlayers ++= pIds
	}
	
	def render = {
	  implicit val formats = DefaultFormats
	  
	  val foo = for (gId <- getGameId) yield {
	    val loaded = CookieBox withSession { db => ArmyEventDataCollector(db).collectEventsFor(gId)}
        initSendMapByPackage(loaded)
	    
        "#armyDataSource [data-army-info]" #> compact(net.liftweb.json.render(Extraction decompose loaded))
	  }

	  val dummy = "#dumymdumm" #> ""
	  val d = foo.openOr(dummy)
	  d
	}
}