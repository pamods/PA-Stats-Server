package info.nanodesu.model.db.collectors.gameinfo

import collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.math.BigDecimal._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._
import net.liftweb.util.Props


case class GamePlanet(
  seed: Int,
  temp: java.math.BigDecimal,
  waterHeight: String, // converted by the jdbc driver as we never need to know these as numbers
  heightRange: String,
  private val inRadius: java.math.BigDecimal,
  private val doNotUseWrongBiome: String,
  name: String) {
  
  val biome = if (doNotUseWrongBiome == "earth" && javaBigDecimal2bigDecimal(temp) <= -0.5) "ice" else doNotUseWrongBiome
  
  val size = (javaBigDecimal2bigDecimal(4) * Math.PI * inRadius * inRadius) match {
	    case x if (x <= 1000000) => "1"
	    case x if (x <= 6000000) => "2"
	    case x if (x <= 12000000) => "3"
	    case x if (x <= 18000000) => "4"
	    case x if (x <= 30000000) => "5"
	    case _ => "unknown"
	  }
  
  val imagePath = Props.get("imagebase", "error")+"planets/"+biome+".png"
  
  val radius = inRadius.toString
}

object PlanetCollector extends PlanetLoader {
  def apply(db: DSLContext, game: Int) = getGamePlanet(db, game)
}

trait PlanetLoader {
  def getGamePlanet(db: DSLContext, game: Int) = {
    db.select(planets.SEED, planets.TEMPERATURE, planets.WATER_HEIGHT,
      planets.HEIGHT_RANGE, planets.RADIUS, planets.BIOME, planets.PLANET_NAME).
      from(games).
      join(planets).onKey().
      where(games.ID === game).
      fetchOneIntoOption(classOf[GamePlanet])
  }
}