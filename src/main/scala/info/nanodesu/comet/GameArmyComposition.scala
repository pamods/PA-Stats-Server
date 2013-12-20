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

class GameArmyComposition extends GameComet {
	def nameKey = CometInit.gameArmyComposition
	
	private var armyEventsMap: Set[Int] = Set.empty
	private var lastPackage: Box[ArmyEventPackage] = Empty
	
	override def lowPriority = {
	  case GameArmyCompositionUpdate(id: Int, composition: ArmyEventPackage) if isMyGame(id) => {
	    lastPackage = Full(composition)
	    checkForAddedEvents(composition.playerEvents)
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
	}
	
	def render = {
	  implicit val formats = DefaultFormats
	  
	  val foo = for (gId <- getGameId) yield {
	    if (lastPackage.isEmpty) {
	      val loaded = CookieBox withSession { db => ArmyEventDataCollector(db).collectEventsFor(gId)}
	      initSendMapByPackage(loaded)
	      lastPackage = Some(loaded)
	    }
	    
	    CookieBox withSession { db =>
          "#armyDataSource [data-army-info]" #> compact(net.liftweb.json.render(Extraction decompose lastPackage))
	    }
	  }
	  
	  val dummy = "#dumymdumm" #> ""
	  val d = foo.openOr(dummy)
	  d
	}
}