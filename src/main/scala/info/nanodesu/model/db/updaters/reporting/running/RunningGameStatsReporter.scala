package info.nanodesu.model.db.updaters.reporting.running

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
import java.sql.Timestamp
import info.nanodesu.model.db.updaters.reporting.GenerateNewPlayer
import info.nanodesu.model._
import java.util.Date
import info.nanodesu.model.db.collectors.gameinfo.loader.GameIdFromLinkLoader
import info.nanodesu.model.db.updaters.reporting.GameEndTimeUpdater
import net.liftweb.common.Loggable

trait RunningGameUpdaterDbLayer {
  def selectGameIdFromLink(link: Int): Option[Int]
  def updateGameEndTime(gameId: Int, time: Date)
  def insertStatsData(data: StatsReportData, link: Int, time: Date)
  def insertArmyEvents(data: List[ArmyEvent], link: Int)
}

trait RunningGameUpdater {
  var dbLayer: RunningGameUpdaterDbLayer = null
  def init(dbLayer: RunningGameUpdaterDbLayer) = {
    this.dbLayer = dbLayer
  }

  def insertRunningGameData(data: RunningGameData, time: Date)
}

class RunningGameStatsReporter extends RunningGameUpdater {
  def insertRunningGameData(data: RunningGameData, time: Date) = {
    val gameId = dbLayer.selectGameIdFromLink(data.gameLink) getOrElse {
      throw new RuntimeException(s"cannot find game for link = ${data.gameLink}")
    }
    dbLayer.updateGameEndTime(gameId, time)
    dbLayer.insertStatsData(data.stats, data.gameLink, time)
    dbLayer.insertArmyEvents(data.armyEvents, data.gameLink)
  }
}

object RunningGameStatsReporter {

  def apply(db: DSLContext) = {
    val r = new RunningGameStatsReporter()
    r.init(new DbLayer(db))
    r
  }

  private class DbLayer(db: DSLContext) extends RunningGameUpdaterDbLayer with GameIdFromLinkLoader with GameEndTimeUpdater {
    def insertArmyEvents(data: List[ArmyEvent], link: Int) = {
      val specs = data.map(_.spec)

      val existingSpecIdMap = db.select(specKeys.SPEC, specKeys.ID).
        from(specKeys).
        where(specKeys.SPEC.in(specs.toList)).
        fetch().asScala.map(x => (x.value1(), x.value2())).toMap

      // usually this should return an empty map, only when new units are encountered this would insert the spec id's
      // this may be prone to race conditions that may result in multiple datasets. However it is very unlikely that it happens
      // and the performance penality of doing synchronization for all reports all the time would be much too high
      val newSpecIdMap = (for (event <- data if !existingSpecIdMap.contains(event.spec)) yield {
        (event.spec, db.insertInto(specKeys, specKeys.SPEC).values(event.spec).returning(specKeys.ID).fetchOne().getId())
      }).toMap

      val allSpecIdsMap = newSpecIdMap ++ existingSpecIdMap

      // in case of i.e. nuke hits this can be several hundred events, so batch them up
      val insertBatch = db.batch(
        db.insertInto(armyEvents,
          armyEvents.SPEC_ID,
          armyEvents.X,
          armyEvents.Y,
          armyEvents.Z,
          armyEvents.PLANET_ID,
          armyEvents.WATCHTYPE,
          armyEvents.PLAYER_GAME,
          armyEvents.TIMEPOINT).values(0, 0, 0, 0, 0, 0, 0, null))

      def bindLoop(batch: BatchBindStep, data: List[ArmyEvent]): BatchBindStep = data match {
        case head :: tail => bindLoop(
	            batch.bind(allSpecIdsMap(head.spec), 
	            head.x:java.lang.Float, 
	            head.y:java.lang.Float, 
	            head.z:java.lang.Float, 
	            head.planetId:Integer,
	            head.watchType:Integer,
	            link:Integer,
	            new Timestamp(head.time)),
            tail)
        case Nil => batch
      }
      
      bindLoop(insertBatch, data).execute()
    }
    def selectGameIdFromLink(link: Int): Option[Int] = selectGameIdFromLink(db, link)
    def updateGameEndTime(gameId: Int, time: Date) = updateGameEndTime(db, gameId, time.getTime())
    def insertStatsData(data: StatsReportData, link: Int, time: Date) = {
      db.insertInto(stats,
        stats.SIM_SPEED,
        stats.ARMY_COUNT,
        stats.METAL_INCOME,
        stats.ENERGY_INCOME,
        stats.METAL_STORED,
        stats.ENERGY_STORED,
        stats.METAL_COLLECTED,
        stats.ENERGY_COLLECTED,
        stats.METAL_WASTED,
        stats.ENERGY_WASTED,
        stats.METAL_INCOME_NET,
        stats.ENERGY_INCOME_NET,
        stats.METAL_SPENDING,
        stats.ENERGY_SPENDING,
        stats.APM,
        stats.PLAYER_GAME,
        stats.TIMEPOINT).
        values(
          data.simSpeed,
          data.armyCount,
          data.metalIncome,
          data.energyIncome,
          data.metalStored,
          data.energyStored,
          data.metalProducedSinceLastTick,
          data.energyProducedSinceLastTick,
          data.metalWastedSinceLastTick,
          data.energyWastedSinceLastTick,
          data.metalIncomeNet,
          data.energyIncomeNet,
          data.metalSpending,
          data.energySpending,
          data.apm,
          link,
          new Timestamp(time.getTime())).
          execute()
    }
  }
}