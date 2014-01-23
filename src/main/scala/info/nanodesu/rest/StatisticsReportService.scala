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
import info.nanodesu.model.db.collectors.gameinfo.ArmyEventDataCollector
import info.nanodesu.comet.ForceGameDataUpdate
import net.liftweb.common.Failure
import info.nanodesu.comet.GameServers
import info.nanodesu.model.db.collectors.playerinfo.loader.PlayerIdForLinkLoader
import info.nanodesu.comet.NewPlayerEvents
import info.nanodesu.comet.NewPlayer
import info.nanodesu.comet.PushUpdate
import info.nanodesu.model.db.collectors.playerinfo.CometUpdatePlayerDataCollector
import info.nanodesu.comet.NewChartStats
import info.nanodesu.comet.NewChartStats


object StatisticsReportService extends RestHelper with Loggable {

  def init(): Unit = {
    LiftRules.statelessDispatch append StatisticsReportService
  }

  case class Player(playerId: Int, playerName: String)
  case class Team(teamId: Int, players: List[Player])
  case class Game(gameId: Int, teams: List[Team], winner: Int, startTime: Long)
  serve {
    case "report" :: "winners" :: Nil Get _ =>
      val maxQueryDays = 3 
      val gameList = for (
        start <- Helpers.tryo(S.param("start").map(Long.valueOf).get);
        duration <- Helpers.tryo(S.param("duration").map(Long.valueOf).get)
      ) yield tryo {
        val startTime = BigInteger.valueOf(start)
        val endTime = startTime.add(BigInteger.valueOf(duration))
        
        // currently this isn't really that necessary, but if someday there will be issues it can be changed
        // so false && to deactivate it for now
        if (false && duration > maxQueryDays * 24 * 60 * 60) { 
          throw new RuntimeException(s"You cannot query more that $maxQueryDays days at once!")
        } else {
          CookieBox withSession { db =>
            val result = db.select(teams.INGAME_ID, playerGameRels.P, names.DISPLAY_NAME, games.WINNER_TEAM, games.ID, players.UBER_NAME, games.START_TIME).
              from(playerGameRels).
              join(games).onKey().
              join(players).onKey().
              join(names).onKey().
              join(teams).onKey().
              where(games.WINNER_TEAM.isNotNull()).
              and(epoch(games.START_TIME).gt(startTime)).
              and(epoch(games.START_TIME).lt(endTime)).
              groupBy(teams.INGAME_ID, playerGameRels.P, names.DISPLAY_NAME, games.WINNER_TEAM, games.ID, players.UBER_NAME, games.START_TIME).fetch()

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
              Game(x._1, teamsWithPlayers, x._2.head.getValue(games.WINNER_TEAM), x._2.head.getValue(games.START_TIME).getTime())
            }).toList.sortBy(_.gameId)
          }
        }
      }
      
      gameList match {
        case Full(Failure(msg, exc, chain)) => ResponseWithReason(BadResponse(), msg) 
        case Full(Full(x)) => Extraction decompose x
        case wtf => ResponseWithReason(BadResponse(), "something went wrong somewhere :( data is:\n"+wtf)
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
          GameCometServer ! ForceGameDataUpdate(gameId)
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

      // -2 is the code the mod uses to say "unknown winner", -1 means "draw" and therefore will be stored
      val winnerTeam = if (data.teamIndex != -2) data.teamIndex: Integer else null

      for (gameId <- ReportDataC.getGameIdForLink(data.gameLink)) {
        CookieBox withSession { db =>
          db.update(games).
            set(games.WINNER, data.victor).
            set(games.WINNER_TEAM, winnerTeam).
            where(games.ID === gameId).
            execute()
        }
        GameCometServer ! ForceGameDataUpdate(gameId)
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

        val playerData = CookieBox withSession (CometUpdatePlayerDataCollector(_, link))
        
        for (gameId <- playerData.gameId;
        	 playerId <- playerData.playerId;
        	 playerName <- playerData.playerName) {
          GameCometServer ! GameDataUpdate(gameId)

          // this is the normal location that causes the server to be created
          // here the server really can be empty so far, so it does not need any init from the database
          val server = GameServers.serverForGame(gameId, false)
          val team = data.observedTeams(data.reporterTeam)
          server ! NewPlayer(playerId, playerName, team.primaryColor, team.secondaryColor)
          server ! NewChartStats(playerId, now.getTime(), data.firstStats)
          server ! PushUpdate(false)
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
        
	      for (playerId <- CookieBox.withSession(new PlayerIdForLinkLoader(_).selectPlayerId(data.gameLink))) {
	        val server = GameServers.serverForGame(gameId)
	        if (data.armyEvents.nonEmpty) {
	          server ! NewPlayerEvents(playerId, data.armyEvents)
	        }
		    server ! NewChartStats(playerId, now.getTime(), data.stats)
		    server ! PushUpdate(false)
		  }
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

  // TODO careful redundant code incoming...
  
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
  
  serve {
    case "report" :: "get" :: "events" :: Nil Get _ =>
      try {
        CookieBox withSession { db =>
          val before = System.currentTimeMillis()
          val r = for (id <- GamePage.getGameId) yield Extraction decompose ArmyEventDataCollector(db).collectEventsFor(id)
          logger info (System.currentTimeMillis() - before) + "ms"
          r
        }
      } catch {
        case ex: Exception => {
          logger.error(ex, ex)
          InternalServerErrorResponse()
        }
      }
  }
  
}