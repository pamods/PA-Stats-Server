package info.nanodesu.model.db.collectors.gameinfo.loader

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

class GameTimesLoader(db: DSLContext) {

  private def selectTimeSave(gameId: Int, timeField: Field[Timestamp]): Option[Long] = {
    val time = for (d: Record1[Timestamp] <- db.select(timeField).from(games).where(games.ID === gameId).fetch()) yield {
      d.value1().getTime()
    }
    if (time.size == 1) Some(time(0))
    else None
  }
  
  def selectStartTimeForGame(gameId: Int): Option[Long] = selectTimeSave(gameId, games.START_TIME)
  def selectEndTimeForGame(gameId: Int): Option[Long] = selectTimeSave(gameId, games.END_TIME)
}