package info.nanodesu.model.db.collectors.gameinfo.loader

import scala.language.implicitConversions
import org.jooq.DSLContext
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._
import info.nanodesu.model.db.updaters.reporting.GenerateNewPlayer
import java.sql.Timestamp
import net.liftweb.json.JValue
import net.liftweb.json.Extraction

class PlanetSizeLoader(db: DSLContext) {
	def selectPlanetSizes(gameId: Int): Option[Map[String, Int]] = {
	  val selected = db.select(planets.RADIUS).from(planets).join(games).onKey().where(games.ID === gameId).fetchOneIntoOption(classOf[java.math.BigDecimal])
	  selected.map(x => Map("0" -> x.intValue()))
	}
}