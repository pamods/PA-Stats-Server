package info.nanodesu.model.db.collectors.gamelist

import java.util.Date
import scala.xml.NodeSeq
import info.nanodesu.model.db.collectors.gameinfo.GameTitleCollector
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._
import net.liftweb.common.Loggable

case class GameListEntry(id: Int, title: NodeSeq, startTime: Date, endTime: Date)
class GameListCollector(val dbLayer: GameListCollectorDbLayer, val titleCollector: GameTitleCollector) {
  def getGameList(offset: Int, limit: Int, playerId: Option[Int]) = {
	val baseList = dbLayer.selectGameListEntries(offset, limit, playerId)
	for (base <- baseList) yield {
	  val title = titleCollector.createGameTitle(base.id)
	  GameListEntry(base.id, title, base.startTime, base.endTime)
	}
  }
}

object GameListCollector {
  def apply(db: DSLContext) = {
    new GameListCollector(new DbLayer(db), GameTitleCollector(db))
  }
  
  private class DbLayer(db: DSLContext) extends GameListCollectorDbLayer with Loggable{
    def selectGameListEntries(offset: Int, limit: Int, playerId: Option[Int]): List[GameListEntryBase] = {
      val baseSelect = db.select(games.ID, games.START_TIME, games.END_TIME).from(games)
      val addedPlayer = mayAddPlayerRestrictions(baseSelect, playerId) 
      val orderedAndLimited = addedPlayer.orderBy(games.END_TIME.desc()).limit(limit).offset(offset)
      orderedAndLimited.fetchInto(classOf[GameListEntryBase]).toList
    }
    
    private def mayAddPlayerRestrictions[T <: Record](step: SelectJoinStep[T], playerId: Option[Int]) = {
      playerId match {
        case Some(id) => 
          step.
          	join(playerGameRels).onKey().
          	where(playerGameRels.P === id)
        case _ => step
      }
    }
  }
}

private[gamelist] case class GameListEntryBase(id: Int, startTime: Date, endTime: Date)
trait GameListCollectorDbLayer {
  def selectGameListEntries(offset: Int, limit: Int, playerId: Option[Int]): List[GameListEntryBase]
}