package info.nanodesu.model.db.updaters.cleaners

import java.util.Date
import org.jooq.DSLContext
import info.nanodesu.model.db.collectors.gameinfo.loader.GameIdFromIdentLoader
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._
import java.sql.Timestamp
import java.math.BigInteger

/**
 * Deletes games that are shorter than the given number of minutes
 */
class ShortGamesCleaner(db: DSLContext) {
	def clean(minGameMinutes: Int) = {
	  val before3Hours = new Timestamp(System.currentTimeMillis() - 3 * 60 * 60 * 1000)
	  val before3Min = new Timestamp(System.currentTimeMillis() - 3 * 60 * 1000)
	  val minLength = BigInteger.valueOf(minGameMinutes * 60)
	  db.delete(games).where(games.ID.in(
	      select(games.ID).
	      	from(games).
	      	where(games.END_TIME.gt(before3Hours)).
	      	and(games.END_TIME.lt(before3Min)).
	      	and(epoch(games.END_TIME.sub(games.START_TIME)).lt(minLength)).
	      	and(games.WINNER_TEAM.isNull())
	  )).execute()
	}
}