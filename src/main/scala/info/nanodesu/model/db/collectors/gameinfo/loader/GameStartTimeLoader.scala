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

class GameStartTimeLoader(db: DSLContext) {
    def selectStartTimeForGame(gameId: Int): Long = {
      return db.select(games.START_TIME).from(games).where(games.ID === gameId).fetchOne().value1().getTime()
    }
}