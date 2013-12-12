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
 * unlocks hidden games 3 minutes after they ended.
 * Just in case somebody crashed or similar
 */
class ForceUnlocker(db: DSLContext) {
	def unlock() = {
	  val before3Min = new Timestamp(System.currentTimeMillis() - 3 * 60 * 1000)
	  val before12Hours = new Timestamp(System.currentTimeMillis() - 12 * 60 * 60 * 1000)
	  
	  db.update(playerGameRels).
	  	set(playerGameRels.LOCKED, JFALSE).
	  	where(playerGameRels.ID.in(
	  	    select(playerGameRels.ID).
	  	    from(playerGameRels).
	  	    join(games).onKey().
	  	    where(games.END_TIME.lt(before3Min)).
	  	    and(games.END_TIME.gt(before12Hours))
	  	)).execute()
	}
}