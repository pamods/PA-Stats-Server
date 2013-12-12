package info.nanodesu.model.updaters.reporting.initial

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.mock._
import info.nanodesu.model.db.updaters.reporting.initial.InitialGameToReportDbLayer
import info.nanodesu.model.db.updaters.reporting.initial.InitialGameToReportLinkUpdater
import org.specs2.matcher.ThrownExpectations
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class InitialGameToReportLinkerSpec extends Specification with Mockito with ThrownExpectations {
  
 "The InitialGameToReportLinker" should {
    val uberName = "cola_colin"
    val displayName = "Cola_Colin|VoW"
     
    "1) find correctly linked players and not further change them, apart from updating the displayName" in {
      val db = mock[InitialGameToReportDbLayer]
      
      val gameId = 1
      val teamId = 5

      
      db.getLinkedTeamIndex(uberName, gameId) returns Some(5)
      
      val t = new InitialGameToReportLinkUpdater()
      t.init(db)
      t.fixUpGameLinkAndReturnIt(gameId, teamId, uberName, displayName)
      
      got {
        one(db).updateDisplayName(uberName, displayName)
        one(db).getLinkedTeamIndex(uberName, gameId)
        one(db).getMyLinkId(gameId, uberName)
      }
      there were noMoreCallsTo(db)
    }
    
    "2) switch the player ids on the links if they are mixed up, even if the reporting player changed his name" in {
      val db = mock[InitialGameToReportDbLayer]
      
      val targetTeamLinkId = 22
      val wrongTeamLinkId = 36728
      val wrongTeamId = 2
      val gameId = 20
      val teamId = 0
      
      db.getLinkedTeamIndex(uberName, gameId) returns Some(wrongTeamId)
      db.getImpostingPlayerLinkIdFor(displayName, teamId, gameId) returns targetTeamLinkId
      db.getLinkInCurrentTeam(wrongTeamId, gameId, uberName) returns wrongTeamLinkId
      
      val t = new InitialGameToReportLinkUpdater()
      t.init(db)
      t.fixUpGameLinkAndReturnIt(gameId, teamId, uberName, displayName)

      got {
        one(db).getLinkedTeamIndex(uberName, gameId)
        one(db).getImpostingPlayerLinkIdFor(displayName, teamId, gameId)
        one(db).getLinkInCurrentTeam(wrongTeamId, gameId, uberName)
        one(db).switchPlayersBetweenLinks(targetTeamLinkId, wrongTeamLinkId)
      }
    }
    
    "3 a.a) correctly insert the uberName on newly created player" in {
      val db = mock[InitialGameToReportDbLayer]
      val gameId = 26
      val newPlayerId = 367
      val teamId = 1
      
      db.getLinkedTeamIndex(uberName, gameId) returns None
      db.getPlayerId(uberName) returns None
      db.getNewPlayerForTeamIfExists(teamId, gameId, displayName) returns Some(newPlayerId)
      
      val t = new InitialGameToReportLinkUpdater()
      t.init(db)
      t.fixUpGameLinkAndReturnIt(gameId, teamId, uberName, displayName)
      
      got {
	      one(db).takeControlOfNewPlayer(newPlayerId, uberName)
	      no(db).replacePlayerOnLink(anyInt, anyInt)
	      no(db).generateNewPlayerFor(anyString, anyString)
	      no(db).switchPlayersBetweenLinks(anyInt, anyInt)
      }
    }
    
    "3 a.b) if no matching new player exists create a new player and replace player on imposter link" in {
      val db = mock[InitialGameToReportDbLayer]
      
      val gameId = 368
      val teamId = 2
      val imposterLink = 3862
      val newPlayerId = 3726
      
      db.getLinkedTeamIndex(uberName, gameId) returns None
      db.getPlayerId(uberName) returns None
      db.getNewPlayerForTeamIfExists(teamId, gameId, displayName) returns None
      db.getImpostingPlayerLinkIdFor(displayName, teamId, gameId) returns imposterLink
      db.generateNewPlayerFor(displayName, uberName) returns newPlayerId 
      
      val t = new InitialGameToReportLinkUpdater()
      t.init(db)
      t.fixUpGameLinkAndReturnIt(gameId, teamId, uberName, displayName)
      
      got {
        one(db).replacePlayerOnLink(imposterLink, newPlayerId)
        no(db).switchPlayersBetweenLinks(anyInt, anyInt)
      }
    }
    
    "3 b) replace the playerId on the link of an imposter if the matching player exists somewhere in the database, but not in the game" in {
      val db = mock[InitialGameToReportDbLayer]
      
      val gameId = 47836
      val teamId = 1
      val playerId = 483267
      val imposterLink = 256
      
      db.getLinkedTeamIndex(uberName, gameId) returns None
      db.getPlayerId(uberName) returns Some(playerId)
      db.getImpostingPlayerLinkIdFor(displayName, teamId, gameId) returns imposterLink
      
      val t = new InitialGameToReportLinkUpdater()
      t.init(db)
      t.fixUpGameLinkAndReturnIt(gameId, teamId, uberName, displayName)
      
      got {
        no(db).generateNewPlayerFor(anyString, anyString)
        no(db).switchPlayersBetweenLinks(anyInt, anyInt)
        one(db).replacePlayerOnLink(imposterLink, playerId)
      }
    }
  }
}