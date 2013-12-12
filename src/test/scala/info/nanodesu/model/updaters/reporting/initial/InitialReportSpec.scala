package info.nanodesu.model.updaters.reporting.initial

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock._
import java.sql.Timestamp
import org.apache.commons.lang.StringUtils
import org.specs2.matcher.ThrownExpectations
import info.nanodesu.model.updaters.reporting.ReportDataGenerators
import org.specs2.ScalaCheck
import info.nanodesu.model.db.updaters.reporting.initial._
import info.nanodesu.model.ReportData


/**
 * I always wanted to try writing tests.
 * If somebody experienced would like to comment on this, feel free to do so
 * It's probably not the best way to test this and I want to improve ;)
 */
@RunWith(classOf[JUnitRunner])
class InitialReportSpec extends Specification with Mockito with ScalaCheck with ThrownExpectations {

  import ReportDataGenerators._

  def dbM = {
    val db = mock[InitialReportDbLayer]
    db.loadPlayerIdsByDisplayName(anyString) returns List()
    db
  }
  def newSubject = new InitialReportUpdater()
  
  "The InitialReportUpdater" should {
    
    "return the create game id" ! prop {r: (ReportData, Timestamp) =>
      val t = newSubject
      val db = dbM
      
      val gameId = 28376
      
      db.insertNewGame(any, any, any, any) returns gameId
      
      t.init(db)
      t.createGameAndReturnId(r._1, r._2) === gameId
    }
    
    "insert the correct planet data" ! prop { r: (ReportData, Timestamp) =>
      val t = newSubject
      val db = dbM
      t.init(db)
      t.createGameAndReturnId(r._1, r._2)
      there was one(db).insertNewPlanet(r._1.planet)
    }

    "insert the correct game data" ! prop { r: (ReportData, Timestamp) =>
      val t = newSubject
      val db = dbM
      val g = r._1
      db.insertNewPlanet(g.planet) returns 1
      t.init(db)
      t.createGameAndReturnId(g, r._2)
      there was one(db).insertNewGame(g.ident, g.paVersion, r._2, 1)
    }

    "generate a matching number of team ids" ! prop { r: (ReportData, Timestamp) =>
      val t = new InitialReportUpdater()
      val db = dbM

      t.init(db)
      t.createGameAndReturnId(r._1, r._2)

      there were r._1.observedTeams.size.times(db).generateNewTeamId()
    }

    "insert the correct number of teams" ! prop { r: (ReportData, Timestamp) =>
      val t = newSubject
      val db = dbM

      t.init(db)
      t.createGameAndReturnId(r._1, r._2)

      got {
        r._1.observedTeams.size.times(db).insertTeam(any, any)
      }
    }

    "insert players if necessary and get the id" ! prop { r: (ReportData, Timestamp) =>
      val t = newSubject
      val db = dbM

      // the generator takes care of always having at least some players
      val nonExistingName = r._1.observedTeams(0).players(0).displayName

      // assume no more than 1000 players per game ;)
      db.loadPlayerIdsByDisplayName(anyString) returns List.range(1, 1000)
      db.loadPlayerIdsByDisplayName(nonExistingName) returns List()

      t.init(db)
      t.createGameAndReturnId(r._1, r._2)

      got {
        val names = (for (t <- r._1.observedTeams; p <- t.players) yield p.displayName)
        val groups = names.groupBy(x => x).mapValues(_.size)

        for (name <- groups) {
          name._2.times(db).loadPlayerIdsByDisplayName(name._1)
        }

        one(db).generateNewPlayerFor(nonExistingName)
      }
    }

    "links up according to the propers IDs" ! prop { r: (ReportData, Timestamp) =>
      val t = newSubject
      val db = dbM

      db.insertNewGame(any, any, any, any) returns 1

      var teamId = 0
      db.generateNewTeamId() answers { i =>
        teamId += 1
        teamId
      }

      var playerId = 0
      db.loadPlayerIdsByDisplayName(anyString) answers { i =>
        playerId += 1
        List(playerId)
      }

      t.init(db)
      t.createGameAndReturnId(r._1, r._2)

      got {
        var pc = 0
        var tc = 0
        for (team <- r._1.observedTeams) {
          tc += 1
          for (player <- team.players) {
            pc += 1
            one(db).linkPlayer(1, tc, pc)
          }
        }
      }
    }

    "correctly handle multiple ids being returned by the loadByDisplayname" ! prop { r: (ReportData, Timestamp) =>

      val t = newSubject
      val db = dbM
      db.insertNewGame(any, any, any, any) returns 1
      db.loadPlayerIdsByDisplayName(anyString) returns List(1, 2)
      var teamId = 0
      db.generateNewTeamId() answers { i =>
        teamId += 1
        teamId
      }

      var playerId = 2
      db.generateNewPlayerFor(anyString) answers { i =>
        playerId += 1
        playerId
      }

      t.init(db)
      t.createGameAndReturnId(r._1, r._2)

      got {
        if (r._1.observedTeams.exists(_.players.size > 2)) {
          var pc = 0
          var tc = 0
          for (team <- r._1.observedTeams) {
            tc += 1
            for (player <- team.players) {
              pc += 1
              one(db).linkPlayer(1, tc, pc)
            }
          }
        }
      }
    }
  }

}

