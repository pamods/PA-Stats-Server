package info.nanodesu.model.db.collectors.playerinfo.loader

import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import net.liftweb.util.Props
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.lib.db.CookieFunc._
import net.liftweb.common.Box
import java.math.{ BigDecimal => JBigDecimal }
import org.jooq.util.postgres.PostgresDataType
import java.sql.Date
import net.liftweb.common.Empty

class IsReporterLoader {
	def selectIsReporter(db: DSLContext, player: Int) = {
	    db.select(players.ID).
	    	from(players).
	    	where(players.UBER_NAME.isNotNull()).
	    	and(players.ID === player).
	    fetchFirstPrimitiveIntoOption(classOf[Int]).isDefined
	  }
}