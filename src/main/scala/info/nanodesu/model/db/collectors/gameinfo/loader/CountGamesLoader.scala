package info.nanodesu.model.db.collectors.gameinfo.loader

import info.nanodesu.lib.db.CookieFunc._
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._

class CountGamesLoader(val db: DSLContext) {
	def selectGameCount = db.selectCount().from(games).fetchOne(0, classOf[Long])
	def selectAutomatchCount = db.selectCount().from(games).where(games.AUTOMATCH.isTrue().and(games.WINNER_TEAM.isNotNull())).fetchOne(0, classOf[Long])
	def selectFilteredGamesCount(playerId: Option[Int], systemName: Option[String]) = {
	  
	  def mayAddSystemRestriction[T <: Record](step: SelectJoinStep[T], s: Option[String]) = {
	    s match {
	      case Some(id) => step.join(games).onKey().join(planets).on(planets.ID === games.PLANET).and(planets.NAME === id)
	      case _ => step
	    }
	  }
	  
	  def mayAddPlayerRestriction[T <: Record](step: SelectConditionStep[T], p: Option[Int]) = {
	    p.map(x => step.and(playerGameRels.P === x)).getOrElse(step)
	  }
	  
	  mayAddPlayerRestriction(mayAddSystemRestriction(db.select(playerGameRels.G.countDistinct()).
	  	from(playerGameRels), systemName).
	  	where(playerGameRels.LOCKED.isFalse()), playerId).fetchOne(0, classOf[Int])
	}
}