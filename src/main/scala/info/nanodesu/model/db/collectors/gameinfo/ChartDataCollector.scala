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
import info.nanodesu.model.StatsReportData

// definition of the data that is sent to the js layer of the webpage that shows the charts

case class ChartDataPoint(
  timepoint: Long,
  armyCount: Int,
  metalIncome: Int,
  energyIncome: Int,
  metalIncomeNet: Int,
  energyIncomeNet: Int,
  metalSpending: Int,
  energySpending: Int,
  metalStored: Int,
  energyStored: Int,
  metalProduced: Int,
  energyProduced: Int,
  metalWasted: Int,
  energyWasted: Int,
  apm: Int){}

case class ChartPlayer(
  name: String,
  color: String){}

case class ChartDataPackage(
  gameId: Int,
  // the maps have the player id as keys. Has to be a string for whatever technical reason
  playerTimeData: Map[String, List[ChartDataPoint]],
  playerInfo: Map[String, ChartPlayer]){}

object ChartDataPackage {
  private implicit val formats = net.liftweb.json.DefaultFormats
  implicit def toJson(report: ChartDataPackage): JValue = Extraction.decompose(report)
  def makeDataPoint(time: Long, s: StatsReportData) = {
	  ChartDataPoint(time, 
	      s.armyCount,
	      s.metalIncome,
	      s.energyIncome,
	      s.metalIncomeNet,
	      s.energyIncomeNet,
	      s.metalSpending,
	      s.energySpending,
	      s.metalStored,
	      s.energyStored,
	      s.metalProducedSinceLastTick,
	      s.energyProducedSinceLastTick,
	      s.metalWastedSinceLastTick,
	      s.energyWastedSinceLastTick,
	      s.apm)    
  }
}

case class ChartDataDbResult(playerId: Int, playerName: String, playerColor: String, data: ChartDataPoint)
trait ChartDataCollectorDblayer {
  def selectDataPointsForGame(gameId: Int, ignoreLocks: Boolean): List[ChartDataDbResult]
}

trait ChartDataCollectorTrait {
  def collectDataFor(gameId: Int, ignoreLocks: Boolean): ChartDataPackage
}

class ChartDataCollector(dbLayer: ChartDataCollectorDblayer) extends ChartDataCollectorTrait {
  def collectDataFor(gameId: Int, ignoreLocks: Boolean = false): ChartDataPackage = {
    val input = dbLayer.selectDataPointsForGame(gameId, ignoreLocks)

    val perPlayer = input.groupBy(_.playerId.toString)
    val perPlayerDataPoints = perPlayer.mapValues(_.map(_.data))
    val sorted = perPlayerDataPoints.mapValues(_.sortBy(_.timepoint))

    val playerData = input.groupBy(x => (x.playerId.toString, ChartPlayer(x.playerName, x.playerColor))).keySet.toMap
    
    ChartDataPackage(gameId, sorted, playerData)
  }
}

object ChartDataCollector {

  def apply(db: DSLContext) = new ChartDataCollector(new DbLayer(db))

  private class DbLayer(db: DSLContext) extends ChartDataCollectorDblayer {
    // if somebody knows a better way: TELL ME. PLEASE!
    // I got the ordering right on first try :D
    def selectDataPointsForGame(gameId: Int, ignoreLocks: Boolean): List[ChartDataDbResult] = {
      val lst = db.select(teams.PRIMARY_COLOR, players.ID, names.DISPLAY_NAME, stats.TIMEPOINT, stats.ARMY_COUNT, stats.METAL_INCOME, stats.ENERGY_INCOME, stats.METAL_INCOME_NET,
        stats.ENERGY_INCOME_NET, stats.METAL_SPENDING, stats.ENERGY_SPENDING, stats.METAL_STORED, stats.ENERGY_STORED, stats.METAL_COLLECTED,
        stats.ENERGY_COLLECTED, stats.METAL_WASTED, stats.ENERGY_WASTED, stats.APM).
        from(stats).
        join(playerGameRels).onKey().
        join(players).onKey().
        join(names).onKey().
        join(teams).onKey().
        where(playerGameRels.LOCKED.isNotDistinctFrom(ignoreLocks).
            or(playerGameRels.LOCKED.isNotDistinctFrom(false))).
        and(playerGameRels.G === gameId)
        .fetch()

      val buf = for (r <- lst.asScala) yield {
        def v[T](f: Field[T]) = r.getValue(f)
        val dat = ChartDataPoint(v(stats.TIMEPOINT).getTime(), v(stats.ARMY_COUNT), v(stats.METAL_INCOME), v(stats.ENERGY_INCOME), v(stats.METAL_INCOME_NET),
          v(stats.ENERGY_INCOME_NET), v(stats.METAL_SPENDING), v(stats.ENERGY_SPENDING), v(stats.METAL_STORED), v(stats.ENERGY_STORED), v(stats.METAL_COLLECTED),
          v(stats.ENERGY_COLLECTED), v(stats.METAL_WASTED), v(stats.ENERGY_WASTED), v(stats.APM))
        ChartDataDbResult(v(players.ID), v(names.DISPLAY_NAME), v(teams.PRIMARY_COLOR), dat)
      }
      buf.toList
    }
  }
}