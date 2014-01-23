package info.nanodesu.model.db.collectors.playerinfo.loader

import info.nanodesu.lib.db.CookieFunc._
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._

class PlayerNameLoader(val db: DSLContext) {
	def selectPlayerName(playerId: Int): Option[String] = {
	  db.select(names.DISPLAY_NAME).
	  	from(names).
	  		join(players).onKey().
	  	where(players.ID === playerId).
	  	fetchFirstPrimitiveIntoOption(classOf[String])
	}
}