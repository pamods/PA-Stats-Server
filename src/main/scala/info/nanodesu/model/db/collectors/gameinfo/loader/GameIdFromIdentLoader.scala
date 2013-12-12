package info.nanodesu.model.db.collectors.gameinfo.loader
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._
import net.liftweb.common.Loggable

trait GameIdFromIdentLoader extends Loggable {
	def getIdForIdent(db: DSLContext, ident: String) = {
	  db.select(games.ID).
	  	from(games).
	  	where(games.IDENT === ident).
	  fetchFirstPrimitiveIntoOption(classOf[Int])
	}
}