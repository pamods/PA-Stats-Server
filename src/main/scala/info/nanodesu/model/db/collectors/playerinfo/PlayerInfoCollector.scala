package info.nanodesu.model.db.collectors.playerinfo

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
import info.nanodesu.model.db.collectors.playerinfo.loader.CountGamesForPlayerLoader
import info.nanodesu.model.db.collectors.playerinfo.loader.IsReporterLoader

class PlayerInfoCollector(db: DSLContext, player: Int, gameId: Option[Int]) 
	extends GameAndPlayerInfoCollector(db, player, gameId) {
  import PlayerInfoCollector._

  val gamesCount = if (gameId.isEmpty) getPlayerGamesCount(db, player) else 0
  val playerGameTime = if (gameId.isEmpty) prettyTime(getPlayerGameTimeSum(db, player)) else ""
  val playerGameTimeAvg = if (gameId.isEmpty) prettyTime(getPlayerGameTimeAvg(db, player)) else ""
  val currentDisplayName = selectCurrentDisplayName(db, player)
  val isReporter = selectIsReporter(db, player)
}

object PlayerInfoCollector extends GameAndPlayerInfoCollectorBase{
  def apply(db: DSLContext, playerId: Int, gameId: Box[Int] = Empty) = new PlayerInfoCollector(db, playerId, gameId)
  
  private def selectIsReporter(db: DSLContext, player: Int) = new IsReporterLoader().selectIsReporter(db, player)
  
  private def selectCurrentDisplayName(db: DSLContext, player: Int) = {
    db.select(names.DISPLAY_NAME).
    	from(players).
    	join(names).onKey().
    	where(players.ID === player).
    fetchOne(0, classOf[String])
  }
  
  private def getPlayerGamesCount(db: DSLContext, player: Int) = new CountGamesForPlayerLoader(db).selectPlayerGamesCount(player)
  private def getPlayerGameTimeSum(db: DSLContext, player: Int) = getPlayerGameTime(db, player, _.sum).fetchOne(0, classOf[Long])
  private def getPlayerGameTimeAvg(db: DSLContext, player: Int) = getPlayerGameTime(db, player, _.avg).fetchOne(0, classOf[Long])
}