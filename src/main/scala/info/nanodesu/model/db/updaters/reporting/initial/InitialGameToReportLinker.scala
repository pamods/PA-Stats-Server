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
import info.nanodesu.model.db.updaters.reporting.GenerateNewPlayer
import java.sql.Timestamp

/**
 * I hate the fact that the UberNet allows multiple players to have the same ubername.
 * It's horrible no matter how you look at it and massively complicates this process.
 *
 * This step is performed after the InitialReportUpdater was run or it was determined that somebody else already did so
 * Just like the InitialReportUpdater this is performed while holding a per game lock.
 *
 * At first the displayName of the player is updated
 * After this a few different cases need to be handled:
 *
 * An "Imposter" is a player that was linked by displayName and is not correctly linked.
 * It is found by this criteria: teamId = ?, displayName = ?, no reports
 *
 * 1.) Player is linked correctly already
 * 	 => do nothing
 * 2.) Linked already, but to the wrong team
 *   => switch the players in the two links
 * 3.) Not linked at all
 * 	 3 a.) Not linked and not present in the database at all
 *     => 3 a.a) check if there was a new player created already, if yes insert the uberName
 *        3 a.b) if not create a new player and replace the playerid on an imposter link
 *   3 b.) Not linked, but present in the database
 *     =>  get imposter link, get playerId, replace player id on imposter link
 */

trait InitialGameToReportDbLayer {
  def updateDisplayName(uberName: String, displayName: String)
  def getLinkedTeamIndex(uberName: String, gameId: Int): Option[Int]
  def getImpostingPlayerLinkIdFor(displayName: String, teamIndex: Int, gameId: Int): Int
  def getLinkInCurrentTeam(team: Int, gameId: Int, uberName: String): Int
  def switchPlayersBetweenLinks(correctPlace: Int, wrongPlace: Int)

  def getPlayerId(uberName: String): Option[Int]
  def getNewPlayerForTeamIfExists(teamId: Int, gameId: Int, displayName: String): Option[Int]
  def takeControlOfNewPlayer(playerId: Int, uberName: String)
  def generateNewPlayerFor(displayName: String, uberName: String): Int
  def replacePlayerOnLink(link: Int, newPlayerId: Int)
  
  def getMyLinkId(gameId: Int, uberName: String): Int
}

trait InitialGameToReportLinker {
  var dbLayer: InitialGameToReportDbLayer = null
  def init(dbLayer: InitialGameToReportDbLayer) = {
    this.dbLayer = dbLayer
  }

  def fixUpGameLinkAndReturnIt(gameId: Int, teamId: Int, uberName: String, displayName: String): Int
}

class InitialGameToReportLinkUpdater extends InitialGameToReportLinker {
  def fixUpGameLinkAndReturnIt(gameId: Int, teamId: Int, uberName: String, displayName: String): Int = {
    dbLayer.updateDisplayName(uberName, displayName)
    val linkedTeamIndex = dbLayer.getLinkedTeamIndex(uberName, gameId)

    linkedTeamIndex match {
      case Some(prevLinkedTeam) => // case 1 or 2 
        if (prevLinkedTeam != teamId) {
          handleCase2(prevLinkedTeam)
        } // else case 1, which doesn't need to do anything else
      case _ => // case 3
        handleCase3()
    }
    
    def handleCase2(prevLinkedTeam: Int) = {
      val imposterLink = dbLayer.getImpostingPlayerLinkIdFor(displayName, teamId, gameId)
      val prevLink = dbLayer.getLinkInCurrentTeam(prevLinkedTeam, gameId, uberName)
      dbLayer.switchPlayersBetweenLinks(imposterLink, prevLink)
    }

    def handleCase3() = {
      val playerId = dbLayer.getPlayerId(uberName)
      val imposterLink = dbLayer.getImpostingPlayerLinkIdFor(displayName, teamId, gameId)
      playerId match {
        case Some(player) => // 3b
          dbLayer.replacePlayerOnLink(imposterLink, player)
        case _ =>
          handleCase3a(imposterLink)
      }
    }

    def handleCase3a(imposterLink: Int) {
      val newPlayer = dbLayer.getNewPlayerForTeamIfExists(teamId, gameId, displayName)
      newPlayer match {
        case Some(newPlayerId) => // 3aa
          dbLayer.takeControlOfNewPlayer(newPlayerId, uberName)
        case _ => // 3ab
          val createdPlayer = dbLayer.generateNewPlayerFor(displayName, uberName)
          dbLayer.replacePlayerOnLink(imposterLink, createdPlayer)
      }
    }
    
    dbLayer.getMyLinkId(gameId, uberName)
  }
}

object InitialGameToReport {
  def apply(db: DSLContext) = {
	  val u = new InitialGameToReportLinkUpdater()
	  u.init(new DbLayer(db))
	  u
  }

  private class DbLayer(dbX: DSLContext) extends InitialGameToReportDbLayer with GenerateNewPlayer {
    def db = dbX
    
    def getMyLinkId(gameId: Int, uberName: String): Int = {
      val opt = db.select(playerGameRels.ID).
      	from(playerGameRels).
      	join(players).onKey().
      	where(players.UBER_NAME === uberName).
      	and(playerGameRels.G === gameId).
      fetchFirstPrimitiveIntoOption(classOf[Int])
      
      opt.getOrElse(throw new RuntimeException(s"there should be a link for gameId = $gameId and uberName = $uberName"))
    }
    
    def updateDisplayName(uberName: String, displayName: String) = {
      DisplayNameHistoryWriter(db).processUpdate(uberName, displayName,
          new Timestamp(System.currentTimeMillis()))
    }

    def getLinkedTeamIndex(uberName: String, gameId: Int): Option[Int] = {
      db.select(teams.INGAME_ID).
        from(playerGameRels).
        join(players).onKey().
        join(teams).onKey().
        where(players.UBER_NAME === uberName).
        and(playerGameRels.G === gameId).
        fetchFirstPrimitiveIntoOption(classOf[Int])
    }

    def getImpostingPlayerLinkIdFor(displayName: String, teamIndex: Int, gameId: Int): Int = {
      db.select(playerGameRels.ID).
        from(playerGameRels).
        leftOuterJoin(stats).onKey().
        join(players).onKey().
        join(names).onKey().
        join(teams).onKey().
        where(names.DISPLAY_NAME === displayName).
        and(teams.INGAME_ID === teamIndex).
        and(playerGameRels.G === gameId).
        groupBy(playerGameRels.ID).
        having(stats.ID.count().eq(0:Integer)).
        fetchFirstPrimitiveIntoOption(classOf[Int]).getOrElse {
          throw new RuntimeException(s"There is no imposter available?! displayName=$displayName teamIndex=$teamIndex gameId=$gameId")
        }
    }

    def getLinkInCurrentTeam(team: Int, gameId: Int, uberName: String): Int = {
      db.select(playerGameRels.ID).
        from(playerGameRels).
        join(players).onKey().
        where(players.UBER_NAME === uberName).
        and(playerGameRels.G === gameId).
        and(playerGameRels.T === team).
        fetchFirstPrimitiveIntoOption(classOf[Int]) getOrElse {
          throw new RuntimeException(s"There is no link in the current team? team=$team gameId=$gameId uberName=$uberName")
        }
    }

    def switchPlayersBetweenLinks(correctPlace: Int, wrongPlace: Int) = {
      def selectPlayerIdFor(link: Int) = db.select(playerGameRels.P).from(playerGameRels)
        .where(playerGameRels.ID === link).fetchOne().getValue(0, classOf[Int])

      val wrongPlacePlayer = selectPlayerIdFor(wrongPlace)
      val correctPlacePlayer = selectPlayerIdFor(correctPlace)

      def updatePlayerIdFor(link: Int, newPlayerId: Int) = {
        val cnt = db.update(playerGameRels)
          .set(playerGameRels.P, newPlayerId: Integer).where(playerGameRels.ID === link).execute()
        assumeOne(s"There should be exactly one link($link) updated with the newPlayerId($newPlayerId)", cnt)
      }

      updatePlayerIdFor(correctPlace, wrongPlacePlayer)
      updatePlayerIdFor(wrongPlace, correctPlacePlayer)
    }

    def getPlayerId(uberName: String): Option[Int] = {
      db.select(players.ID).from(players).where(players.UBER_NAME === uberName).
        fetchFirstPrimitiveIntoOption(classOf[Int])
    }

    def getNewPlayerForTeamIfExists(teamId: Int, gameId: Int, displayName: String): Option[Int] = {
      val candidates = db.select(playerGameRels.P).
        from(playerGameRels).
        join(players).onKey().
        join(names).onKey().
        where(playerGameRels.T === teamId).
        and(playerGameRels.G === gameId).
        and(names.DISPLAY_NAME === displayName).
        and(players.UBER_NAME.isNull()).fetch().intoArray(0, classOf[Int])

      // TODO there has to be a better way to do this, but I don't feel like doing more complex joins right now
      def hasOnlyASingleGame(playerId: Int) = {
        db.selectCount().
          from(playerGameRels).
          where(playerGameRels.P === playerId).
          fetchOne().getValue(0, classOf[Int]) == 1
      }
      candidates.find(hasOnlyASingleGame)
    }

    def takeControlOfNewPlayer(playerId: Int, uberName: String) = {
      val cnt = db.update(players).set(players.UBER_NAME, uberName).where(players.ID === playerId).execute()
      assumeOne(s"There should be exactly one player($playerId, uberName = $uberName) who was updated with!", cnt)
    }

    def replacePlayerOnLink(link: Int, newPlayerId: Int) = {
      val cnt = db.update(playerGameRels).set(playerGameRels.P, newPlayerId: Integer).where(playerGameRels.ID === link).execute()
      assumeOne(s"There should be one link($link) updated with the newPlayerId($newPlayerId)!", cnt)
    }

    private def assumeOne(info: String, cnt: Int) = {
      if (cnt != 1) {
        throw new RuntimeException(info)
      }
    }
  }
}