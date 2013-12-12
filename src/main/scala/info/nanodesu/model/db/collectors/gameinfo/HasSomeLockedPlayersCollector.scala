package info.nanodesu.model.db.collectors.gameinfo
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._

object HasSomeLockedPlayersCollector extends HasSomeLockedPlayersLoader{
  def apply(db: DSLContext, gameId: Int) = getHasSomeLockedPlayers(db, gameId)
}

trait HasSomeLockedPlayersLoader {
	def getHasSomeLockedPlayers(db: DSLContext, gameId: Int) = {
	  db.select(playerGameRels.ID.count()).
	  	from(playerGameRels).
	  	join(players).onKey().
	  	where(playerGameRels.LOCKED.isTrue()).
	  	and(playerGameRels.G === gameId).
	  	and(players.UBER_NAME.isNotNull()).fetchFirstPrimitiveIntoOption(classOf[Int]).map(_ > 0).getOrElse(false)
	}
}