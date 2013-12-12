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

trait RunningGameUpdaterDbLayer {
  def selectGameIdFromLink(link: Int): Option[Int]
  def updateGameEndTime(gameId: Int, time: Date)
  def insertStatsData(data: StatsReportData, link: Int, time: Date)
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
  }
}

object RunningGameStatsReporter {
	
  def apply(db: DSLContext) = {
    val r = new RunningGameStatsReporter()
    r.init(new DbLayer(db))
    r
  }
  
  private class DbLayer(db: DSLContext) extends RunningGameUpdaterDbLayer with GameIdFromLinkLoader with GameEndTimeUpdater {
    def selectGameIdFromLink(link: Int): Option[Int] = selectGameIdFromLink(db, link)
    def updateGameEndTime(gameId: Int, time: Date) = updateGameEndTime(db, gameId, time.getTime())
    def insertStatsData(data: StatsReportData, link: Int, time: Date) = {
      db.insertInto(stats,
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