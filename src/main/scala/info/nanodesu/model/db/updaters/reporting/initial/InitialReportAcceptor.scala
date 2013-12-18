package info.nanodesu.model.db.updaters.reporting.initial

import java.util.concurrent.ConcurrentHashMap
import info.nanodesu.model.ReportData
import java.util.Date
import org.jooq.DSLContext
import info.nanodesu.model.db.collectors.gameinfo.loader.GameIdFromIdentLoader
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._
import java.sql.Timestamp
import info.nanodesu.model.RunningGameData
import info.nanodesu.model.db.updaters.reporting.running.RunningGameStatsReporter
import info.nanodesu.model.db.updaters.reporting.running.RunningGameUpdater

trait InitialReportAcceptorDblayer {
  def selectGameId(ident: String): Option[Int]
  def privatizeGame(link: Int)
}

trait InitialReportAcceptorBase {
  var dbL: InitialReportAcceptorDblayer = null
  def init(db: InitialReportAcceptorDblayer) = {
    this.dbL = db
  }

  def acceptInitialReport(data: ReportData, reportDate: Date): Int
}

class InitialReportAcceptor(reportLinker: InitialGameToReportLinker, initReport: InitialReport,
  statsInserter: RunningGameUpdater) extends InitialReportAcceptorBase {
  def acceptInitialReport(data: ReportData, reportDate: Date): Int = {
    val gameIdBox = dbL.selectGameId(data.ident)
    val gameId =  gameIdBox getOrElse {
      initReport.createGameAndReturnId(data, new Timestamp(reportDate.getTime()))
    }

    val link = reportLinker.fixUpGameLinkAndReturnIt(gameId, data.reporterTeam, data.reporterUberName, data.reporterDisplayName)
    if (!data.showLive) {
      dbL.privatizeGame(link)
    }

    val dataToReport = RunningGameData(link, data.firstStats, data.armyEvents)
    statsInserter.insertRunningGameData(dataToReport, reportDate)
    
    link
  }
}

object InitialReportAcceptor {
  val lockMap = new ConcurrentHashMap[String, Object]()

  def getAcceptedGamesCount = lockMap.size()

  def getLock(ident: String) = {
    lockMap.putIfAbsent(ident, new Object())
    lockMap.get(ident)
  }
  
  def acceptInitialReport(db: DSLContext, data: ReportData, reportDate: Date): Int = {
      val worker = new InitialReportAcceptor(InitialGameToReport(db), InitialReportUpdater(db),
        RunningGameStatsReporter(db))
      worker.init(new DbLayer(db))
      worker.acceptInitialReport(data, reportDate)
  }

  private class DbLayer(db: DSLContext) extends InitialReportAcceptorDblayer with GameIdFromIdentLoader {
    def selectGameId(ident: String) = getIdForIdent(db, ident)

    def privatizeGame(link: Int) = {
      val cnt = db.update(playerGameRels).set(playerGameRels.LOCKED, java.lang.Boolean.TRUE).where(playerGameRels.ID === link).execute()
      if (cnt != 1) {
        throw new RuntimeException(s"had $cnt updates instead of 1 for link $link WHY?!")
      }
    }
  }
}