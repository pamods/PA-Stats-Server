package info.nanodesu.rest

import java.util.Date
import info.nanodesu.model.ReportData
import info.nanodesu.model.RunningGameData
import net.liftweb._
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.json.Extraction
import net.liftweb.json.JsonAST.JValue
import net.liftweb.util.Helpers._
import net.liftweb.common.Box
import net.liftweb.util.Helpers
import info.nanodesu.comet.GameCometServer
import info.nanodesu.comet.GameDataUpdate
import info.nanodesu.model.RunningGameDataC
import info.nanodesu.model.ReportDataC
import info.nanodesu.model.db.updaters.reporting.initial.InitialReportAcceptor
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.updaters.reporting.running.RunningGameStatsReporter
import info.nanodesu.pages.GamePage
import info.nanodesu.lib.db.CookieFunc._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import java.sql.Timestamp
import info.nanodesu.model.db.collectors.gameinfo.ChartDataCollector
import java.math.BigInteger
import java.lang.Long
import org.apache.commons.lang.StringUtils

object StatisticsReportService extends RestHelper with Loggable {

  def init(): Unit = {
    LiftRules.statelessDispatch append StatisticsReportService
  }

  case class Player(playerId: Int, playerName: String)
  case class Team(teamId: Int, players: List[Player])
  case class Game(gameId: Int, teams: List[Team], winner: Int)
  serve {
    case "report" :: "winners" :: Nil Get _ =>

      val gameList = for (
        start <- Helpers.tryo(S.param("start").map(Long.valueOf).get);
        duration <- Helpers.tryo(S.param("duration").map(Long.valueOf).get)
      ) yield {
        val startTime = BigInteger.valueOf(start)
        val endTime = startTime.add(BigInteger.valueOf(duration))

        CookieBox withSession { db =>
          val result = db.select(teams.INGAME_ID, playerGameRels.P, names.DISPLAY_NAME, games.WINNER_TEAM, games.ID, players.UBER_NAME).
            from(playerGameRels).
            join(games).onKey().
            leftOuterJoin(stats).on(stats.PLAYER_GAME === playerGameRels.ID).
            join(players).onKey().
            join(names).onKey().
            join(teams).onKey().
            where(games.WINNER_TEAM.isNotNull()).
            and(epoch(games.START_TIME).gt(startTime)).
            and(epoch(games.START_TIME).lt(endTime)).
            groupBy(teams.INGAME_ID, playerGameRels.P, names.DISPLAY_NAME, games.WINNER_TEAM, games.ID, players.UBER_NAME).fetch()

          val lst = result.asScala.toList
          val byGames = lst.groupBy(_.getValue(games.ID))

          byGames.map(x => {
            val byTeam = x._2.groupBy(_.getValue(teams.INGAME_ID))
            val teamsWithPlayers = byTeam.map(t => {
              val playersInTeam = t._2.map { foo =>
                val uberNameIsEmpty = StringUtils.isBlank(foo.getValue(players.UBER_NAME))
                val id: Int = if (uberNameIsEmpty) -1 else foo.getValue(playerGameRels.P)
                val name = if (uberNameIsEmpty) "Anon" else foo.getValue(names.DISPLAY_NAME)
                Player(id, name)
              }
              Team(t._1, playersInTeam)
            }).toList
            Game(x._1, teamsWithPlayers, x._2.head.getValue(games.WINNER_TEAM))
          }).toList.sortBy(_.gameId)
        }
      }
      
      gameList match {
        case Full(x) => Extraction decompose x
        case _ => BadResponse()
      }
  }

  case class GameShortInfo(id: Int, start: Long, reporters: List[String])
  serve {
    case "report" :: "games" :: Nil Get _ =>

      val queryR = CookieBox withSession { db =>
        db.select(games.ID, games.START_TIME, names.DISPLAY_NAME).
          from(playerGameRels).
          join(games).onKey().
          join(players).onKey().
          join(names).onKey().
          orderBy(games.ID).
          fetch()
      }

      val queryResults = queryR.asScala.toList.map(x => ((x.value1(), x.value2().getTime(), x.value3())))
      val grouped = queryResults.groupBy(x => (x._1, x._2)).mapValues(_.map(_._3)).toList.map(x => GameShortInfo(x._1._1, x._1._2, x._2)).sortBy(_.id)
      Extraction decompose grouped
  }

  serve {
    case "report" :: "unlock" :: Nil Get _ =>
      for (l <- S.param("link"); link <- Helpers.tryo(Integer.parseInt(l))) {
        CookieBox withSession { db =>
          db.update(playerGameRels).
            set(playerGameRels.LOCKED, JFALSE).
            where(playerGameRels.ID === link).
            execute()
        }
        for (gameId <- ReportDataC.getGameIdForLink(link)) {
          GameCometServer ! GameDataUpdate(gameId)
        }
      }
      OkResponse()
  }

  case class VictorNotification(gameLink: Int, victor: String, teamIndex: Int) {}
  object VictorNotification {
    private implicit val formats = net.liftweb.json.DefaultFormats
    def apply(in: JValue): Box[VictorNotification] = Helpers.tryo(in.extract[VictorNotification])
    def unapply(in: JValue): Option[VictorNotification] = apply(in)
  }
  serve {
    case "report" :: "winner" :: Nil JsonPut VictorNotification(data) -> _ =>

      val winnerTeam = if (data.teamIndex != -1) data.teamIndex: Integer else null

      for (gameId <- ReportDataC.getGameIdForLink(data.gameLink)) {
        CookieBox withSession { db =>
          db.update(games).
            set(games.WINNER, data.victor).
            set(games.WINNER_TEAM, winnerTeam).
            where(games.ID === gameId).
            execute()
        }
        GameCometServer ! GameDataUpdate(gameId)
      }

      OkResponse()
  }

  case class PlayerDeathNotification(gameLink: Int) {}
  object PlayerDeathNotification {
    private implicit val formats = net.liftweb.json.DefaultFormats
    def apply(in: JValue): Box[PlayerDeathNotification] = Helpers.tryo(in.extract[PlayerDeathNotification])
    def unapply(in: JValue): Option[PlayerDeathNotification] = apply(in)
  }
  serve {
    case "report" :: "idied" :: Nil JsonPut PlayerDeathNotification(data) -> _ =>
      CookieBox withSession { db =>
        db.update(playerGameRels).
          set(playerGameRels.DIED, JTRUE).
          where(playerGameRels.ID === data.gameLink).
          execute()
      }
      OkResponse()
  }

  case class PlayerGameLinkIdResponse(gameLink: Int)
  serve {
    case "report" :: Nil JsonPut ReportDataC(data) -> _ =>
      val now = new Date
      if (!ReportDataC.isVersionSupported(data.version)) {
        BadResponse()
      } else {
        val link = InitialReportAcceptor.getLock(data.ident) synchronized { // lock outside of the transaction!!!
          CookieBox withTransaction { db =>
            InitialReportAcceptor.acceptInitialReport(db, data, now)
          }
        }

        for (gameId <- ReportDataC.getGameIdForLink(link)) {
          GameCometServer ! GameDataUpdate(gameId)
        }

        Extraction decompose PlayerGameLinkIdResponse(link)
      }
  }

  serve {
    case "report" :: Nil JsonPut RunningGameDataC(data) -> _ =>
      val now = new Date

      CookieBox withTransaction { db => // needs autocommit = off for batch inserts
        RunningGameStatsReporter(db).insertRunningGameData(data, now)
      }

      for (gameId <- ReportDataC.getGameIdForLink(data.gameLink)) {
        GameCometServer ! GameDataUpdate(gameId)
      }

      OkResponse()
  }

  case class ReportVersionResponse(version: Int)
  serve {
    case "report" :: "version" :: Nil Get _ =>
      Extraction decompose ReportVersionResponse(ReportDataC.selectReportVersion)
  }

  case class CurrentTimeMs(ms: Long)
  serve {
    case "report" :: "get" :: "time" :: Nil Get _ =>
      Extraction decompose CurrentTimeMs(System.currentTimeMillis())
  }

  serve {
    case "report" :: "get" :: Nil Get _ =>
      try {
        CookieBox withSession { db =>
          for (id <- GamePage.getGameId) yield Extraction decompose ChartDataCollector(db).collectDataFor(id)
        }
      } catch {
        case ex: Exception => {
          logger.error(ex, ex)
          InternalServerErrorResponse()
        }
      }
  }
}