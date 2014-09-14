package info.nanodesu.model.db.updaters.reporting.initial

import scala.language.implicitConversions
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
import info.nanodesu.model.db.updaters.reporting.GenerateNewPlayer
import info.nanodesu.model._
import net.liftweb.common.Loggable



/**
 * This needs to be executed only if the ident of the given game is not 
 * yet present in the database. Also this needs to be executed under a per game lock!
 * 
 * Updater that generates the games, the teams, the players and the links between them.
 * After this process has been completed a game was created that is linked with matching teams
 * that have some players in them that have the right displayname.
 *
 * After this step all reporting players have to take care of making sure that they are linked correctly with the game.
 * This includes checking that their data is locked if they want it to be locked.
 */

// Traits specify interaction with database backend
// useful for testing
trait InitialReportDbLayer {
  def insertNewPlanet(planet: ReportedPlanet): Int
  def insertNewGame(ident: String, paVersion: String, startDate: Timestamp, reportDate: Timestamp, planetId: Int, isAutomatch: Boolean): Int

  def generateNewTeamId(): Int
  def insertTeam(team: ReportTeam, teamId: Int)

  def generateNewPlayerFor(displayName: String): Int
  def loadPlayerIdsByDisplayName(displayName: String): List[Int]
  def linkPlayer(gameId: Int, teamId: Int, playerId: Int): Int
}

trait InitialReport {
  var dbLayer: InitialReportDbLayer = null;
  def init(dbLayer: InitialReportDbLayer) = {
    this.dbLayer = dbLayer;
  }

  def createGameAndReturnId(report: ReportData, reportDate: Timestamp): Int
}

class InitialReportUpdater() extends InitialReport {
  def createGameAndReturnId(report: ReportData, reportDate: Timestamp): Int = {
    val planetId = dbLayer.insertNewPlanet(report.planet)
    val gameId = dbLayer.insertNewGame(report.ident, report.paVersion, new Timestamp(report.gameStartTime), reportDate, planetId, report.isAutomatch)

    var linkedPlayerIds = List[Int]()

    for (team <- report.observedTeams) {
      val teamId = dbLayer.generateNewTeamId()
      dbLayer.insertTeam(team, teamId)

      for (player <- team.players) {
        val possibleIds = dbLayer.loadPlayerIdsByDisplayName(player.displayName).filterNot(linkedPlayerIds.contains(_))

        val playerId = if (possibleIds.isEmpty) dbLayer.generateNewPlayerFor(player.displayName)
        else possibleIds.head

        linkedPlayerIds ::= playerId

        dbLayer.linkPlayer(gameId, teamId, playerId)
      }
    }
    
    gameId
  }
}

object InitialReportUpdater {

  def apply(db: DSLContext) = {
    val layer = new DbLayer(db)
    val updater = new InitialReportUpdater()
    updater.init(layer)
    updater
  }

  private class DbLayer(dbX: DSLContext) extends InitialReportDbLayer with GenerateNewPlayer with Loggable {
    def db = dbX
    
    def insertNewPlanet(planet: ReportedPlanet): Int = {
      import _root_.scala.language.implicitConversions
      implicit def s2B(s: String) = try { new java.math.BigDecimal(s) } catch { case e: Exception => java.math.BigDecimal.ZERO }

      
      db.insertInto(planets, planets.PLANET).values(planet.json).returning().fetchOne().getId()
    }

    def insertNewGame(ident: String, paVersion: String, startDate: Timestamp, reportDate: Timestamp, planetId: Int, isAutomatch: Boolean): Int = {
      db.insertInto(games, games.IDENT, games.START_TIME, games.END_TIME, games.PA_VERSION, games.PLANET, games.AUTOMATCH).
        values(ident, startDate, reportDate, paVersion, planetId, isAutomatch).returning(games.ID).fetchOne().getId()
    }

    def generateNewTeamId(): Int = db.nextval(teamIdSeq).toInt

    def insertTeam(team: ReportTeam, teamId: Int) = {
      db.insertInto(teams, teams.ID, teams.PRIMARY_COLOR, teams.SECONDARY_COLOR, teams.INGAME_ID).
        values(teamId, team.primaryColor, team.secondaryColor, team.index).execute()
    }

    def loadPlayerIdsByDisplayName(displayName: String): List[Int] = {
      db.select(players.ID).
        from(players).
        join(names).onKey().
        where(names.DISPLAY_NAME === displayName).
        and(players.UBER_NAME.isNull()).
      fetch().getValues(0, classOf[Int]).asScala.toList
    }

    def linkPlayer(gameId: Int, teamId: Int, playerId: Int): Int = {
      db.insertInto(playerGameRels, playerGameRels.P, playerGameRels.G, playerGameRels.T)
        .values(playerId, gameId, teamId).returning().fetchOne().getId()
    }
  }
}