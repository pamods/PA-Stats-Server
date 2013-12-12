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

class PlayerHistoryInfo(db: DSLContext, player: Int) {
  import PlayerHistoryInfo._
  
  val displayNameHistory = getDisplayNameHistory(db, player)
}

case class PlayerHistoryEntry(name: String, time: Date)

object PlayerHistoryInfo extends PlayerHistoryCollector{
  def apply(db: DSLContext, player: Int) = new PlayerHistoryInfo(db, player)
}

trait PlayerHistoryCollector {
  def getDisplayNameHistory(db: DSLContext, player: Int) = {
    db.select(names.DISPLAY_NAME, historyNames.REPLACED_ON).
    	from(names).
    	join(historyNames).onKey().
    	where(historyNames.PLAYER_ID === player).
    	orderBy(historyNames.REPLACED_ON.desc()).
    fetchInto(classOf[PlayerHistoryEntry]).asScala.toList
  }
}