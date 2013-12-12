package info.nanodesu.model.updaters.reporting.initial

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.mock._
import java.sql.Timestamp
import org.specs2.matcher.ThrownExpectations
import info.nanodesu.model.db.updaters.reporting.initial.DisplayNameHistoryWriter
import info.nanodesu.model.db.updaters.reporting.initial.DisplayNameUpdaterDbLayer
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DisplayNameUpdaterSpec extends Specification with Mockito with ThrownExpectations {

  def noUpdatesExecuted(db: DisplayNameUpdaterDbLayer) = {
    no(db).updateDisplayName(anyInt, anyInt)
    no(db).insertDisplayName(anyString)
    no(db).insertHistory(anyInt, anyInt, any)
  }
  
  def now = new Timestamp(System.currentTimeMillis())
    
  "The DisplayNameUpdater" should {

    "not change anything if the name stayed the same" in {
      val db = mock[DisplayNameUpdaterDbLayer]

      val uberName = "cola_colin"
      val displayName = "Cola_Colin|VoW"
      val playerId = 410
      val displayNameId = 2386

      db.selectPlayerIdFor(uberName) returns Some(playerId)
      db.selectDisplayNameInfo(playerId) returns ((displayName, displayNameId))

      val t = new DisplayNameHistoryWriter()
      t.init(db)
      t.processUpdate(uberName, displayName, now)

      got {
        noUpdatesExecuted(db)
      }
    }

    "not change anything if the player does not exist" in {
      val db = mock[DisplayNameUpdaterDbLayer]

      val uberName = "newPlayer123"
      val displayName = "awesome_dude"

      db.selectPlayerIdFor(uberName) returns None

      val t = new DisplayNameHistoryWriter()
      t.init(db)
      t.processUpdate(uberName, displayName, now)

      got {
        noUpdatesExecuted(db)
      }
    }
    
    "create a new displayname if the new names does not exist" in {
      val db = mock[DisplayNameUpdaterDbLayer]
      
      val uberName = "cola_colin"
      val oldDisplayName = "Cola_Colin|VoW"
      val displayName = "I always wanted to rename myself, ...not"
      val playerId = 410
        
      db.selectPlayerIdFor(uberName) returns Some(playerId)
      db.selectDisplayNameInfo(playerId) returns ((oldDisplayName, 3286))
      db.selectDisplayNameId(displayName) returns None
      
      val t = new DisplayNameHistoryWriter()
      t.init(db)
      t.processUpdate(uberName, displayName, now)
      
      got {
        one(db).insertDisplayName(displayName)
      }
    }
    
    "update the displayname if it changed" in {
      val db = mock[DisplayNameUpdaterDbLayer]
      
      val uberName = "cola_colin"
      val displayName = "Cola"
      val oldDisplayName = "Cola_Colin|VoW"
      val playerId = 1337
      val newDisplayNameId = 38626
      
      db.selectPlayerIdFor(uberName) returns Some(playerId)
      db.selectDisplayNameInfo(playerId) returns ((oldDisplayName, 826))
      db.selectDisplayNameId(displayName) returns Some(newDisplayNameId)
      
      val t = new DisplayNameHistoryWriter()
      t.init(db)
      t.processUpdate(uberName, displayName, now)
      
      got {
        one(db).updateDisplayName(newDisplayNameId, playerId)
      }
    }
    
    "create history entries" in {
      val db = mock[DisplayNameUpdaterDbLayer]
      
      val uberName = "cola_colin"
      val displayName = "X"
      val oldDisplayName = "Y"
      val playerId = 362
      val oldDisplayNameId = 3826
      
      db.selectPlayerIdFor(uberName) returns Some(playerId)
      db.selectDisplayNameInfo(playerId) returns ((oldDisplayName, oldDisplayNameId))
      db.selectDisplayNameId(displayName) returns Some(27362)
      
      val n = now
      
      val t = new DisplayNameHistoryWriter()
      t.init(db)
      t.processUpdate(uberName, displayName, n)
      
      got {
        one(db).insertHistory(oldDisplayNameId, playerId, n)
      }
    }
  }

}