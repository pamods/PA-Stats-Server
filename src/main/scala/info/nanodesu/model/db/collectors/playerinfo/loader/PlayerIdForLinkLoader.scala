package info.nanodesu.model.db.collectors.playerinfo.loader

import info.nanodesu.lib.db.CookieFunc._
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._

class PlayerIdForLinkLoader(val db: DSLContext) {
	def selectPlayerId(gameLink: Int): Option[Int] = {
	  db.select(playerGameRels.P).from(playerGameRels).where(playerGameRels.ID === gameLink)
	  	.fetchFirstPrimitiveIntoOption(classOf[Int])
	}
}