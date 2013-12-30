package info.nanodesu.model.db.collectors.gameinfo

import scala.language.implicitConversions
import org.jooq.DSLContext
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._
import info.nanodesu.model.db.updaters.reporting.GenerateNewPlayer
import java.sql.Timestamp
import net.liftweb.json.JValue
import net.liftweb.json.Extraction
import info.nanodesu.model.db.collectors.gameinfo.loader.GameTimesLoader

// yes currently this is practically a copy of the ArmyEvent in ReportData.
// However I want to keep these 2 classes separate since they are used for different communication channels that may change independently from each other
// i.e. for now this returns integer positions. They are probably accurate enough anyway.
case class ArmyEvent(id: Int, spec: String, x: Int, y: Int, z: Int, planetId: Int, watchType: Int, time: Long)

case class ArmyEventPlayer(name: String, primaryColor: String, secondaryColor: String)

case class ArmyEventPackage(game: Int, gameStart: Long, playerEvents: Map[String, List[ArmyEvent]],
    playerInfo: Map[String, ArmyEventPlayer])

case class ArmyEventDbResult(playerId: Int, playerName: String, playerPrimaryColor: String, playerSecondaryColor: String, event: ArmyEvent)
trait ArmyEventDbLayer {
  def selectArmyEventsForGame(gameId: Int): List[ArmyEventDbResult]
  def selectStartTimeForGame(gameId: Int): Long
}
    
class ArmyEventDataCollector(dbLayer: ArmyEventDbLayer) {
	def collectEventsFor(gameId: Int): ArmyEventPackage = {
	  val raw = dbLayer.selectArmyEventsForGame(gameId)
	  
	  val perPlayer = raw.groupBy(_.playerId.toString)
	  
	  val perPlayerEvents = perPlayer.mapValues(_.map(_.event))
	  val sorted = perPlayerEvents.mapValues(_.sortBy(_.time))
	  
	  val playerInfo = raw.groupBy(x => (x.playerId.toString, ArmyEventPlayer(x.playerName, x.playerPrimaryColor, x.playerSecondaryColor))).keySet.toMap
	  
	  ArmyEventPackage(gameId, dbLayer.selectStartTimeForGame(gameId), sorted, playerInfo)
	}
}


object ArmyEventDataCollector {
  def apply(db: DSLContext) = new ArmyEventDataCollector(new DbLayer(db)) 
  
  private class DbLayer(db: DSLContext) extends ArmyEventDbLayer {
    
    def selectStartTimeForGame(gameId: Int): Long = new GameTimesLoader(db).selectStartTimeForGame(gameId)
    
    def selectArmyEventsForGame(gameId: Int): List[ArmyEventDbResult] = {
      val lst  = db.select(playerGameRels.P, 
          names.DISPLAY_NAME, 
          teams.PRIMARY_COLOR, 
          teams.SECONDARY_COLOR, 
          specKeys.SPEC, 
          armyEvents.X, 
          armyEvents.Y, 
          armyEvents.Z, 
          armyEvents.PLANET_ID, 
          armyEvents.WATCHTYPE, 
          armyEvents.TIMEPOINT,
          armyEvents.ID).
          	from(playerGameRels).
          join(players).onKey().
          join(names).onKey().
          join(teams).onKey().
          join(armyEvents).on(armyEvents.PLAYER_GAME === playerGameRels.ID).
          join(specKeys).onKey().
          	where(playerGameRels.LOCKED.isFalse()).
          and(playerGameRels.G === gameId).
          fetch()
          
      val buf = for (r <- lst.asScala) yield {
        def v[T](f: Field[T]) = r.getValue(f)
        val dat = ArmyEvent(v(armyEvents.ID), v(specKeys.SPEC), v(armyEvents.X).toInt, v(armyEvents.Y).toInt, v(armyEvents.Z).toInt, v(armyEvents.PLANET_ID), v(armyEvents.WATCHTYPE), v(armyEvents.TIMEPOINT).getTime())
        ArmyEventDbResult(v(playerGameRels.P), v(names.DISPLAY_NAME), v(teams.PRIMARY_COLOR), v(teams.SECONDARY_COLOR), dat)
      }
          
      buf.toList
    }
  }
}
