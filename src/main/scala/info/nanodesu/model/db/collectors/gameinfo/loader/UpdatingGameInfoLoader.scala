package info.nanodesu.model.db.collectors.gameinfo.loader
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._

case class LiveGameInfoUpdate(winner: String, duration: Int)
trait UpdatingGameInfoLoader {
  def selectWinnerAndDurationForGame(db: DSLContext, gameId: Int): Option[LiveGameInfoUpdate] = {
    db.select(games.WINNER, intervalInSecs(games.END_TIME.sub(games.START_TIME)).mul(1000:Integer)).
    	from(games).
    	where(games.ID === gameId).
    fetchOneIntoOption(classOf[LiveGameInfoUpdate])
  }
}