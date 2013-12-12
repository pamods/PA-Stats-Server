package info.nanodesu.model.db.collectors.gameinfo.loader
import org.jooq.DSLContext
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._
import info.nanodesu.model._

trait GameIdFromLinkLoader {
	def selectGameIdFromLink(db: DSLContext, linkId: Int): Option[Int] = {
	  db.select(playerGameRels.G).from(playerGameRels).where(playerGameRels.ID === linkId).
	  	fetchFirstPrimitiveIntoOption(classOf[Int])
	}
}