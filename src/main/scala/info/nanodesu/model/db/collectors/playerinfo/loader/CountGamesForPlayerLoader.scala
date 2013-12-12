package info.nanodesu.model.db.collectors.playerinfo.loader

import info.nanodesu.lib.db.CookieFunc._
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._

class CountGamesForPlayerLoader(val db: DSLContext) {
	def selectPlayerGamesCount(player: Int) = {
    db.select(playerGameRels.ID.countDistinct()).
      from(stats).
      join(playerGameRels).onKey().
      where(playerGameRels.P === player).
      and(playerGameRels.LOCKED.isFalse()).
    fetchOne(0, classOf[Int])
  }
}