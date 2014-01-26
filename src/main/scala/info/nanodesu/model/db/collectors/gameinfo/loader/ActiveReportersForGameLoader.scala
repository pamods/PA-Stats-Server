package info.nanodesu.model.db.collectors.gameinfo.loader
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._

case class ActiveReporter(id: Int, locked: Boolean, name: String, primaryColor: String, secondaryColor: String)

class ActiveReportersForGameLoader {
	def selectActiveReportersFor(db:DSLContext, gameId: Int) = {
	  db.select(playerGameRels.P).
	  	from(playerGameRels).
	  	leftOuterJoin(stats).onKey().
	  	where(playerGameRels.G === gameId).
	  	groupBy(playerGameRels.P).
	  	having(stats.ID.count().gt(0)).
	  fetch(0, classOf[Int]).asScala.toList
	}
	
	def selectActiveReportersWithName(db: DSLContext, gameId: Int) = {
	  db.select(playerGameRels.P, playerGameRels.LOCKED, names.DISPLAY_NAME, teams.PRIMARY_COLOR, teams.SECONDARY_COLOR).
	  	from(playerGameRels).
	  	leftOuterJoin(stats).onKey().
	  	join(teams).on(teams.ID === playerGameRels.T).
	  	join(players).onKey().
	  	join(names).onKey().
	  	where(playerGameRels.G === gameId).
	  	groupBy(playerGameRels.P, playerGameRels.LOCKED, names.DISPLAY_NAME, teams.PRIMARY_COLOR, teams.SECONDARY_COLOR).
	  	having(stats.ID.count().gt(0)).
	  fetchInto(classOf[ActiveReporter]).toList
	}
}