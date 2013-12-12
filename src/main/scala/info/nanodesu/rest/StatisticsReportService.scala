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

object StatisticsReportService extends RestHelper with Loggable {

  def init(): Unit = {
    LiftRules.statelessDispatch append StatisticsReportService
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
      }
      OkResponse()
  }

  case class VictorNotification(gameLink: Int, victor: String) {}
  object VictorNotification {
    private implicit val formats = net.liftweb.json.DefaultFormats
    def apply(in: JValue): Box[VictorNotification] = Helpers.tryo(in.extract[VictorNotification])
    def unapply(in: JValue): Option[VictorNotification] = apply(in)
  }
  serve {
    case "report" :: "winner" :: Nil JsonPut VictorNotification(data) -> _ =>

      for (gameId <- ReportDataC.getGameIdForLink(data.gameLink)) {
        CookieBox withSession { db =>
          db.update(games).
            set(games.WINNER, data.victor).
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
      CookieBox withTransaction { db =>
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