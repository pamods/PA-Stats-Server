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

class PlanetJsonLoader(db: DSLContext) {
	private def makeOption(in: Result[Record1[String]]): Option[String] = {
	  if (in.size() > 0) {
	    Some(in.get(0).value1())
	  } else {
	    None
	  }
	}
  
    def selectJsonForGame(gameId: Int): Option[String] = {
      val result = db.select(planets.PLANET).from(games).join(planets).onKey().where(games.ID === gameId).fetch()
      makeOption(result)
    }
  
	def selectJsonForPlanet(planetId: Int): Option[String] = {
	  val result = db.select(planets.PLANET).from(planets).where(planets.ID === planetId).fetch()
	  makeOption(result)
	}
}