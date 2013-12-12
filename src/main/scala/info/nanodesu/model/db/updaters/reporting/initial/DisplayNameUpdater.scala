package info.nanodesu.model.db.updaters.reporting.initial
import org.jooq.DSLContext
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._
import java.sql.Timestamp

trait DisplayNameUpdaterDbLayer {
  def selectPlayerIdFor(uberName: String): Option[Int]
  def selectDisplayNameInfo(playerId: Int): (String, Int)
  def selectDisplayNameId(displayName: String): Option[Int]
  def insertDisplayName(displayName: String): Int
  def insertHistory(nameId: Int, playerId: Int, now: Timestamp)
  def updateDisplayName(newDisplayNameId: Int, playerId: Int)
}

trait DisplayNameUpdater {
  var dbLayer: DisplayNameUpdaterDbLayer = null
  def init(dbLayer: DisplayNameUpdaterDbLayer) = {
    this.dbLayer = dbLayer
  }

  def processUpdate(uberName: String, newDisplayName: String, now: Timestamp)
}

class DisplayNameHistoryWriter extends DisplayNameUpdater {
  def processUpdate(uberName: String, newDisplayName: String, now: Timestamp) = {

    val playerIdOpt = dbLayer.selectPlayerIdFor(uberName)

    for (playerId <- playerIdOpt) {
      val currentNameRec = dbLayer.selectDisplayNameInfo(playerId)
      val currentName = currentNameRec._1
      val currentNameId = currentNameRec._2

      if (currentName != newDisplayName) {
        val correctDisplayNameOpt = dbLayer.selectDisplayNameId(newDisplayName)
        val correctDisplayNameId = correctDisplayNameOpt getOrElse dbLayer.insertDisplayName(newDisplayName)
        dbLayer.insertHistory(currentNameId, playerId, now)
        dbLayer.updateDisplayName(correctDisplayNameId, playerId)
      }
    }
  }
}

object DisplayNameHistoryWriter {

  def apply(db: DSLContext) = {
    val w = new DisplayNameHistoryWriter()
    w.init(new DbLayer(db))
    w
  }
  
  private class DbLayer(db: DSLContext) extends DisplayNameUpdaterDbLayer {
    def selectPlayerIdFor(uberName: String): Option[Int] = {
      db.select(players.ID).
        from(players).
        where(players.UBER_NAME === uberName).
        fetchFirstPrimitiveIntoOption(classOf[Int])
    }
    def selectDisplayNameInfo(playerId: Int): (String, Int) = {
      val currentNameRec = db.select(names.DISPLAY_NAME, names.ID).
        from(players).
        join(names).onKey().
        where(players.ID === playerId).fetchOne()
      val currentName = currentNameRec.getValue(0, classOf[String])
      val currentNameId = currentNameRec.getValue(1, classOf[Int])
      (currentName, currentNameId)
    }
    def selectDisplayNameId(displayName: String): Option[Int] = {
      db.select(names.ID).
        from(names).
        where(names.DISPLAY_NAME === displayName)
        .fetchFirstPrimitiveIntoOption(classOf[Int])
    }
    def insertDisplayName(displayName: String): Int = {
      db.insertInto(names, names.DISPLAY_NAME).
        values(displayName).returning().fetchOne().getId()
    }
    def insertHistory(nameId: Int, playerId: Int, now: Timestamp) = {
      db.insertInto(historyNames, historyNames.NAME_ID, historyNames.PLAYER_ID, historyNames.REPLACED_ON).
        values(nameId, playerId, now).execute()
    }
    def updateDisplayName(newDisplayNameId: Int, playerId: Int) = {
      db.update(players).
        set(players.CURRENT_DISPLAY_NAME, newDisplayNameId: Integer).
        where(players.ID === playerId).execute()
    }
  }
}