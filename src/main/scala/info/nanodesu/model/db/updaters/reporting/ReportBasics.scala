package info.nanodesu.model.db.updaters.reporting

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
import java.sql.Timestamp

trait QueryMinReportVersion {
  def queryMinReportVersion(db: DSLContext) =
    db.select(settings.REPORT_VERSION).from(settings).fetchFirstPrimitiveIntoOption(classOf[Int]) getOrElse {
	  throw new RuntimeException("the database is missing a minimum version in the settings table!")
    }
}

trait GenerateNewPlayer {
  def db: DSLContext

  def generateNewPlayerFor(displayName: String): Int = {
    generateNewPlayerFor(displayName, null)
  }

  def generateNewPlayerFor(displayName: String, uberName: String): Int = {
    val nameIdOpt = db.select(names.ID).from(names).where(names.DISPLAY_NAME === displayName).fetchFirstPrimitiveIntoOption(classOf[Int])
    val nameId = nameIdOpt getOrElse {
      db.insertInto(names, names.DISPLAY_NAME).values(displayName).returning().fetchOne().getId():Int
    }
    
    db.insertInto(players, players.UBER_NAME, players.CURRENT_DISPLAY_NAME).
      values(uberName, nameId).returning().fetchOne().getId()
  }
}

trait GameEndTimeUpdater {
  def updateGameEndTime(db: DSLContext, gameId: Int, time: Long) = {
    db.update(games).set(games.END_TIME, new Timestamp(time)).where(games.ID === gameId).execute()
  }
}