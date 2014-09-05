package info.nanodesu.lib

import scala.collection.JavaConverters._
import info.nanodesu.lib.db.CookieBox
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.db.CookieFunc._
import info.nanodesu.rest.StatisticsReportService

object RatingsMachine extends RefreshRunner {
  override val firstLoadDelay = 60 * 1000
  override val RUN_INTERVAL = 60 * 1000
  val processName = "RatingMachine"

  var lastQuery = 0L

  def runQuery() = {
    val candidates = StatisticsReportService.selectGameListForWinners((lastQuery - RUN_INTERVAL) / 1000, 1800, true)
    val filteredBy1v1 = candidates.filter(g => g.teams.size == 2 && g.teams.head.players.size == 1 && g.teams.tail.head.players.size == 1)
    
    for (g <- filteredBy1v1) {
      val playerA = g.teams.head.players.head
      val playerB = g.teams.tail.head.players.head
      
      val skillA = querySkill(playerA.playerId)
      val skillB = querySkill(playerB.playerId)
      
      if (skillA.isDefined && skillB.isDefined) {
        val aWon = g.winner == g.teams.head.teamId
        val bWon = g.winner == g.teams.tail.head.teamId
        if (aWon) {
          val e = calcEloChange(skillA.get, skillB.get)
          val newSkillA = skillA.get + e
          val newSkillB = skillB.get - e
          updateScore(playerA.playerId, newSkillA)
          updateScore(playerB.playerId, newSkillB)
          logger info playerA + " vs "+ playerB + " A won, e="+e+" ratings before: a=" + skillA.get+", b="+skillB.get+" ratings after: a="+newSkillA+", b="+newSkillB
        } else if (bWon) {
          val e = calcEloChange(skillB.get, skillA.get)
          val newSkillB = skillB.get + e
          val newSkillA = skillA.get - e
          updateScore(playerB.playerId, newSkillB)
          updateScore(playerA.playerId, newSkillA)
          logger info playerA + " vs "+ playerB + " B won, e="+e+" ratings before: a=" + skillA.get+", b="+skillB.get+" ratings after: a="+newSkillA+", b="+newSkillB
        } // ignore draws for now, the snippet for elo I found doesn't have it and I am too lazy right now to research more. There are only like 3 draws in all time anyway and nobody will see this rating
      } else {
        logger.warn("could not determine skill for one of these players: " + playerA+" / "+playerB)
      }
    }
    
    markConsideredGames(filteredBy1v1.map(_.gameId))
    lastQuery = System.currentTimeMillis()
  }

  private def calcEloChange(winner: Float, loser: Float) = {
    val diff = loser - winner
    val expectedScoreWinner = 1 / (1 + Math.pow(10, diff / 400))
    val k = 32
    (k * (1 - expectedScoreWinner)).toFloat
  }
  
  private def updateScore(playerId: Int, score: Float) = {
    CookieBox withSession { db =>
      db.update(players).set(players.RATING, score:java.lang.Float).where(players.ID === playerId).execute()
    }
  }
  
  def markConsideredGames(lst: List[Int]) = {
    CookieBox withSession { db =>
      for (l <- lst) {
        // not using IN, as I am unsure of limits it may have in case the list is very long. Yes that means this is probably not THAT fast, but it's okay, the usual case probably has like 1 or 2 entries
        db.update(games).set(games.RATED, JTRUE).where(games.ID === l).execute()
      }
    }
  }
  
  def querySkill(playerId: Int) = {
    CookieBox withSession (_.select(players.RATING).from(players).where(players.ID === playerId).fetchOneIntoOption(classOf[java.lang.Float]))
  }
  
  def querySkill(playerUberName: String) = {
    CookieBox withSession (_.select(players.RATING).from(players).where(players.UBER_NAME === playerUberName).fetchOneIntoOption(classOf[java.lang.Float]))
  }
}