package info.nanodesu.model.db.collectors.playerinfo

import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import net.liftweb.util.Props
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.lib.db.CookieFunc._
import net.liftweb.common.Box
import java.math.{ BigDecimal => JBigDecimal }
import org.jooq.util.postgres.PostgresDataType

trait GamePlayerInfo {
  def apmAvg: Int
  def metalUseAvg: Double
  def energyUseAvg: Double
  def buildSpeed: Double
  def sumMetal: Long
  def sumEnergy: Long
  def name: String
  def primaryColor: String
  def secondaryColor: String
}

class GameAndPlayerInfoCollector(db: DSLContext, player: Int, gameId: Option[Int]) extends GamePlayerInfo {
  import GameAndPlayerInfoCollector._

  val apmAvg = getAvgApm(db, player, gameId)
  val metalUseAvg = getAvgMetalUsage(db, player, gameId)
  val energyUseAvg = getAvgEnergyUsage(db, player, gameId)
  val buildSpeed = getAvgBuildSpeed(db, player, gameId)
  val sumMetal = getMetalSum(db, player, gameId)
  val sumEnergy = getEnergySum(db, player, gameId)
  var name: String = ""
  var primaryColor: String = ""
  var secondaryColor: String = ""
}

object GameAndPlayerInfoCollector extends GameAndPlayerInfoCollectorBase {
  def apply(db: DSLContext, playerId: Int, gameId: Int) = new GameAndPlayerInfoCollector(db, playerId, Some(gameId))
}

trait GameAndPlayerInfoCollectorBase {
  def getMetalSum(db: DSLContext, player: Int, forGame: Option[Int] = None) = {
    getSumStatField(db, player, stats.METAL_COLLECTED, forGame)
  }

  def getEnergySum(db: DSLContext, player: Int, forGame: Option[Int] = None) = {
    getSumStatField(db, player, stats.ENERGY_COLLECTED, forGame)
  }

  def getSumStatField[T](db: DSLContext, player: Int, f: Field[T], forGame: Option[Int] = None) = {
    val base = db.select(f.sum()).
      from(stats).
      join(playerGameRels).onKey().
      where(playerGameRels.LOCKED.isFalse()).
      and(playerGameRels.P === player)

    val finalQ = mayAddGameCondition(base, forGame)

    finalQ.fetchOne(0, classOf[Long])
  }

  def getPlayerGameTime(db: DSLContext, player: Int, agg: Field[_] => Field[java.math.BigDecimal], forGame: Option[Int] = None) = {
    db.select(agg(field("diff", classOf[java.math.BigDecimal])))
      .from(
        mayAddGameCondition(db.select(intervalInSecs(max(stats.TIMEPOINT).sub(min(stats.TIMEPOINT)).mul(int2Num(1000))).as("diff")).
          from(stats).
          join(playerGameRels).onKey().
          join(games).onKey().
          where(playerGameRels.LOCKED.isFalse()).
          and(playerGameRels.P === player), forGame).
          groupBy(games.ID).asTable().as("foo"))
  }

  def getSumActions(db: DSLContext, player: Int, forGame: Option[Int]) = {
    mayAddGameCondition(db.select(stats.APM.sum()).
      from(stats).
      join(playerGameRels).onKey().
      where(playerGameRels.LOCKED.isFalse()).
      and(playerGameRels.P === player), forGame)
  }

  def getAvgApm(db: DSLContext, player: Int, forGame: Option[Int] = None) = {
    val sumActions = getSumActions(db, player, forGame).fetchOne(0, classOf[Long])
    val playTimeMs = getPlayerGameTime(db, player, _.sum, forGame).fetchOne(0, classOf[Long])
    val playTimeMinutes = playTimeMs / 1000.0 / 60.0
    Math.round(sumActions / playTimeMinutes).toInt
  }

  def avgUsageOf(db: DSLContext, player: Int, resCollected: Field[_ <: Number], resWasted: Field[_ <: Number], forGame: Option[Int] = None) = {
    mayAddGameCondition(db.select(decode().
      when(sum(resCollected) > 0, -(sum(resWasted).cast(PostgresDataType.NUMERIC) / sum(resCollected)) + (new JBigDecimal(1))).
      otherwise(1)).
      from(stats).
      join(playerGameRels).onKey().
      where(playerGameRels.LOCKED.isFalse()).and(playerGameRels.P === player), forGame).fetchOne(0, classOf[Double])
  }

  def getAvgMetalUsage(db: DSLContext, player: Int, forGame: Option[Int] = None) = {
    avgUsageOf(db, player, stats.METAL_COLLECTED, stats.METAL_WASTED, forGame)
  }

  def getAvgEnergyUsage(db: DSLContext, player: Int, forGame: Option[Int] = None) = {
    avgUsageOf(db, player, stats.ENERGY_COLLECTED, stats.ENERGY_WASTED, forGame)
  }

  def mayAddGameCondition[T <: Record](query: SelectConditionStep[T], forGame: Option[Int]): SelectConditionStep[T] =
    forGame match {
      case Some(gameId) => query.and(playerGameRels.G === gameId)
      case _ => query
    }

  def getAvgBuildSpeed(db: DSLContext, player: Int, forGame: Option[Int] = None) = {
    def eff(stored: Field[Integer], spending: Field[Integer], income: Field[Integer]) = {
      decode().
        when(stored > inline(0: Integer), inline(new JBigDecimal(1))).
        otherwise(
          decode().
            when(spending > inline(0: Integer), income.cast(PostgresDataType.NUMERIC).div(spending)).
            otherwise(inline(int2JBigD(1))))
    }

    val metalEfficiency = eff(stats.METAL_STORED, stats.METAL_SPENDING, stats.METAL_INCOME)
    val energyEfficiency = eff(stats.ENERGY_STORED, stats.ENERGY_SPENDING, stats.ENERGY_INCOME)
    val buildSpeedClause = least(metalEfficiency, energyEfficiency)

    mayAddGameCondition(db.select(buildSpeedClause.avg()).
      from(stats).
      join(playerGameRels).onKey().
      where(playerGameRels.LOCKED.isFalse()).
      and(playerGameRels.P === player), forGame).fetchOne(0, classOf[Double])
  }
}