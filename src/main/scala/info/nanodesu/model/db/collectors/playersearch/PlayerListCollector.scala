package info.nanodesu.model.db.collectors.playersearch

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

case class IdAndPlayerName(id: Int, name: String) {
  override def equals(other: Any): Boolean = other match {
    case that: IdAndPlayerName =>
      (that canEqual this) && id == that.id
    case _ => false
  }
  def canEqual(other: Any): Boolean = other.isInstanceOf[IdAndPlayerName]
  override def hashCode: Int = id.hashCode
}

class PlayerListCollector(db: DSLContext, term: String) {
  import PlayerListCollector._
  val searchResult = executeSearch(db, term)
}

object PlayerListCollector {
  def apply(db: DSLContext, term: String) = new PlayerListCollector(db, term)

  private def searchCurrentNames(db: DSLContext, term: String) = {
    db.select(players.ID, names.DISPLAY_NAME).
      from(players).
      join(names).onKey().
      where(names.DISPLAY_NAME.likeIgnoreCase("%"+term+"%")).
      and(players.UBER_NAME.isNotNull())
      .fetchInto(classOf[IdAndPlayerName]).asScala.toList
  }

  private def searchHistoryNames(db: DSLContext, term: String) = {
    val p = players.as("p")
    val n = names.as("n")
    val hn = names.as("hn")
    val h = historyNames.as("h")
    db.selectDistinct(p.ID, n.DISPLAY_NAME).
      from(p, n, hn, h).
      where(n.ID === p.CURRENT_DISPLAY_NAME).
      and(h.NAME_ID === hn.ID).
      and(h.PLAYER_ID === p.ID).
      and(hn.DISPLAY_NAME.likeIgnoreCase("%" + term + "%")).
      fetchInto(classOf[IdAndPlayerName]).asScala.toList
  }

  private def executeSearch(db: DSLContext, term: String) = {
    val byCurrent = searchCurrentNames(db, term)
    val byHistory = if (term.isEmpty) Nil else searchHistoryNames(db, term)
    
    val list = (byCurrent ::: byHistory).toSet.toList
    list.sortWith((a, b) => (a.name < b.name))
  }
}