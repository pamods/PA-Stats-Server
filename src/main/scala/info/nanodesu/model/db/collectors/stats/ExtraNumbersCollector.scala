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

class ExtraNumbersCollector(db: DSLContext) {
  import ExtraNumbersCollector._
  
  val gameCount = getGameCount(db)
  val datapointsCount = getDatapointsCount(db)
  val avgGameTime = prettyTime(getGameLengthAvg(db))
  val sumGameTime = prettyTime(getGameLengthSum(db))
  val userCount = getUsersCount(db)
}

object ExtraNumbersCollector {
  def apply(db: DSLContext) = new ExtraNumbersCollector(db)
  
  private def getGameCount(db: DSLContext) = new CountGamesLoader(db).selectGameCount
  private def getDatapointsCount(db: DSLContext) = db.selectCount().from(stats).fetchOne(0, classOf[Long])
  private def getUsersCount(db: DSLContext) = db.selectCount().from(players).where(players.UBER_NAME.isNotNull()).fetchOne(0, classOf[Long])
  
  private def getGameLength(db: DSLContext, agg: Field[_ <: Number] => Field[_ <: Number]) = {
    db.select(agg(epoch(games.END_TIME.sub(games.START_TIME)).mul(int2JBigD(1000)))).from(games).fetchOne(0, classOf[Long])
  }
  
  private def getGameLengthSum(db: DSLContext) = getGameLength(db, sum)
  private def getGameLengthAvg(db: DSLContext) = getGameLength(db, avg)
}