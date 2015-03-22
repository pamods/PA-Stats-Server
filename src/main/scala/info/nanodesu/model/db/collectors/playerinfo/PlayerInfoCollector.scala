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
import java.sql.Date
import net.liftweb.common.Empty
import info.nanodesu.model.db.collectors.playerinfo.loader.CountGamesForPlayerLoader
import info.nanodesu.model.db.collectors.playerinfo.loader.IsReporterLoader
import net.liftweb.common.Loggable
import java.math.BigInteger
import org.apache.commons.lang.StringEscapeUtils
import info.nanodesu.lib.Formattings
import net.liftweb.util.StringHelpers

case class DailyValue(day: Long, value: Double)

case class ValuesPoint(timepoint: Long, games: Int) // , metalUsage: Double, energyUsage: Double
case class NameValue(name: String)
case class DailyValues(
    data: Map[String, List[ValuesPoint]],
    info: Map[String, NameValue]
)

class PlayerInfoCollector(db: DSLContext, player: Int, gameId: Option[Int]) extends Loggable {
  import PlayerInfoCollector._

  val gamesCount = if (gameId.isEmpty) getPlayerGamesCount(db, player) else 0
  val playerGameTime = if (gameId.isEmpty) prettyTime(getPlayerGameTimeSum(db, player)) else ""
  val playerGameTimeAvg = if (gameId.isEmpty) prettyTime(getPlayerGameTimeAvg(db, player)) else ""
  val currentDisplayName = selectCurrentDisplayName(db, player)
  val isReporter = selectIsReporter(db, player)
  
  private val dailyGames = selectDailyGames(db, player)
//  private val dailyMetalUsage = selectDailyResouceUsage(db, player, stats.METAL_COLLECTED, stats.METAL_WASTED)
//  private val dailyEnergyUsage = selectDailyResouceUsage(db, player, stats.ENERGY_COLLECTED, stats.ENERGY_WASTED)
  
  def dailyValues = {
    val values = for (d <- dailyGames) yield { // 
//      if (!(d._1.day == d._2.day && d._2.day == d._3.day)) {
//        logger warn s"something is wrong player = $player => " + d
//      }
      ValuesPoint(d.day , d.value.toInt /*, d._2.value, d._3.value*/)
    }
    																			// we need to escape here or are in danger of weird illegal characters on the page :(
    DailyValues(Map(player.toString -> values), Map(player.toString -> NameValue(StringEscapeUtils.escapeHtml(currentDisplayName))))
  }
}

object PlayerInfoCollector extends GameAndPlayerInfoCollectorBase with Loggable {
  def apply(db: DSLContext, playerId: Int, gameId: Box[Int] = Empty) = new PlayerInfoCollector(db, playerId, gameId)

  private def selectIsReporter(db: DSLContext, player: Int) = new IsReporterLoader().selectIsReporter(db, player)

  private def selectCurrentDisplayName(db: DSLContext, player: Int) = {
    db.select(names.DISPLAY_NAME).
      from(players).
      join(names).onKey().
      where(players.ID === player).
      fetchOne(0, classOf[String])
  }

  private def selectDailyGames(db: DSLContext, player: Int) = {
    db.select(
      epoch(dayTrunk(games.START_TIME)),
      playerGameRels.ID.countDistinct()).
      from(playerGameRels).
//      join(stats).onKey(). // so why exactly was this joined into that?
      join(games).onKey().
      join(players).onKey().
      where(players.ID === player).
      and(playerGameRels.LOCKED.isFalse()).
      groupBy(dayTrunk(games.START_TIME)).
      orderBy(dayTrunk(games.START_TIME)).
      fetchInto(classOf[DailyValue]).toList
  }

  // this is currently not used. It was waaaaaay too slow for players with many games
  private def selectDailyResouceUsage(db: DSLContext, player: Int, resCollected: Field[_ <: Number], resWasted: Field[_ <: Number]) = {
    db.select(epoch(dayTrunk(games.START_TIME)), 
        decode().when(sum(resCollected) > 0, -(sum(resWasted).cast(PostgresDataType.NUMERIC) / sum(resCollected)) 
            + (new JBigDecimal(1))).otherwise(1)).
      from(stats).
      join(playerGameRels).onKey().
      join(games).onKey().
      where(playerGameRels.LOCKED.isFalse())
      .and(playerGameRels.P === player).
      groupBy(dayTrunk(games.START_TIME)).
      orderBy(dayTrunk(games.START_TIME)).
      fetchInto(classOf[DailyValue]).toList
  }

  private def getPlayerGamesCount(db: DSLContext, player: Int) = new CountGamesForPlayerLoader(db).selectPlayerGamesCount(player)
  private def getPlayerGameTimeSum(db: DSLContext, player: Int) = getPlayerGameTime(db, player, _.sum).fetchOne(0, classOf[Long])
  private def getPlayerGameTimeAvg(db: DSLContext, player: Int) = getPlayerGameTime(db, player, _.avg).fetchOne(0, classOf[Long])
}