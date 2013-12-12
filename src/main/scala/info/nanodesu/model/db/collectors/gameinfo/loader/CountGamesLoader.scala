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
}