package info.nanodesu.model.db.collectors.gameinfo

import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._
import net.liftweb.util.Props
import java.sql.Date
import net.liftweb.common.Loggable

case class GameInfo(startTime: Date, paVersion: String, winner: String, duration: Long)

object GameInfoCollector extends GameInfoLoader {
	def apply(db: DSLContext, gameId: Int) = getGameInfo(db, gameId)
}

trait GameInfoLoader {
	def getGameInfo(db: DSLContext, gameId: Int) = {
	  db.select(games.START_TIME, games.PA_VERSION, games.WINNER, 
	      intervalInSecs(games.END_TIME.sub(games.START_TIME).mul(int2Num(1000)))).
	  from(games).
	  	where(games.ID === gameId).fetchOneIntoOption(classOf[GameInfo])
	}
}