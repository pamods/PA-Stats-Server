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

class ObserverDataCleaner(db: DSLContext) {
	def clean(minDataPointsToKeep: Int) = {
	  val before3Hours = new Timestamp(System.currentTimeMillis() - 3 * 60 * 60 * 1000)
	  val before3Min = new Timestamp(System.currentTimeMillis() - 3 * 60 * 1000)
	  
	  db.delete(stats).where(stats.PLAYER_GAME.in(
	      select(playerGameRels.ID).
	      from(playerGameRels).
	      join(stats).onKey().
	      join(games).onKey().
	      where(games.END_TIME.gt(before3Hours)).
	      and(games.END_TIME.lt(before3Min)).
	      groupBy(playerGameRels.ID).
	      having(count(stats.PLAYER_GAME).lt(minDataPointsToKeep))
	  )).execute()
	}
}