package info.nanodesu.model.db.collectors.stats

import info.nanodesu.lib.db.CookieFunc._
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import java.math.{ BigDecimal => JBigDecimal }
import info.nanodesu.model.db.collectors.gameinfo.loader.CountGamesLoader
import info.nanodesu.lib.RefreshRunner
import info.nanodesu.lib.db.CookieBox

class ExtraNumbersCollector(db: DSLContext) {
  import ExtraNumbersCollector._
  
  val gameCount = getGameCount(db)
  val datapointsCount = dataPointCount
  val unitsCreated = unitsCreatedCount
  val unitsDestroyed = unitsDestroyedCount
  val avgGameTime = prettyTime(getGameLengthAvg(db))
  val sumGameTime = prettyTime(getGameLengthSum(db))
  val userCount = getUsersCount(db)
}

object ExtraNumbersCollector extends RefreshRunner {
  
  override val firstLoadDelay = 60 * 1000 * 5
  override val RUN_INTERVAL = 1000 * 60 * 60 * 3
  var processName = "worker: "+getClass().getName()
  
  private var dataPointCount: Option[Long] = None
  private var unitsCreatedCount: Option[Long] = None
  @volatile
  private var unitsDestroyedCount: Option[Long] = None
  
  def runQuery() = CookieBox withSession { db =>
    dataPointCount = Some(getDatapointsCount(db))
    unitsCreatedCount = Some(selectCreatedUnits(db))
    unitsDestroyedCount = Some(selectDestroyedUnits(db))
  }
  
  def apply(db: DSLContext) = new ExtraNumbersCollector(db)
  
  private def selectEventsOfType(db: DSLContext, eventType: Int) = db.selectCount().from(armyEvents).where(armyEvents.WATCHTYPE === eventType).fetchOne(0, classOf[Long])
  private def selectCreatedUnits(db: DSLContext) = selectEventsOfType(db, 0)
  private def selectDestroyedUnits(db: DSLContext) = selectEventsOfType(db, 2)
  
  private def getGameCount(db: DSLContext) = new CountGamesLoader(db).selectGameCount
  private def getDatapointsCount(db: DSLContext) = db.selectCount().from(stats).fetchOne(0, classOf[Long])
  private def getUsersCount(db: DSLContext) = db.selectCount().from(players).where(players.UBER_NAME.isNotNull()).fetchOne(0, classOf[Long])
  
  private def getGameLength(db: DSLContext, agg: Field[_ <: Number] => Field[_ <: Number]) = {
    db.select(agg(epoch(games.END_TIME.sub(games.START_TIME)).mul(int2JBigD(1000)))).from(games).fetchOne(0, classOf[Long])
  }
  
  private def getGameLengthSum(db: DSLContext) = getGameLength(db, sum)
  private def getGameLengthAvg(db: DSLContext) = getGameLength(db, avg)
}