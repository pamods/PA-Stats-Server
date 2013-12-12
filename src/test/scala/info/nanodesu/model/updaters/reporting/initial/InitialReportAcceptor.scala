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
import info.nanodesu.model.db.updaters.reporting.running.RunningGameUpdater
import info.nanodesu.model.RunningGameData

@RunWith(classOf[JUnitRunner])
class InitialReportAcceptorSpec extends Specification with Mockito with ScalaCheck with ThrownExpectations {
	
  import ReportDataGenerators._
  
  "The InitialReportAcceptor" should {
	  "privatize privat games and use the correct link id for it" ! prop { r: (ReportData, Timestamp) =>
	    val linker = mock[InitialGameToReportLinker]
	    val initReporter = mock[InitialReport]
	    val statsInserter = mock[RunningGameUpdater]
	    val db = mock[InitialReportAcceptorDblayer]
	    
	    db.selectGameId(anyString) returns Some(1)
	    linker.fixUpGameLinkAndReturnIt(1, r._1.reporterTeam,
	        r._1.reporterUberName, r._1.reporterDisplayName) returns 12
	    
	    val subject = new InitialReportAcceptor(linker, initReporter, statsInserter)
	    subject.init(db)
	    val link = subject.acceptInitialReport(r._1, r._2)
	    
	    got {
		    if (!r._1.showLive) {
		      one(db).privatizeGame(12)
		    } else {
		      no(db).privatizeGame(anyInt)
		    }
		    val runningDat = RunningGameData(link, r._1.firstStats)
		    one(statsInserter).insertRunningGameData(runningDat, r._2)
	    }
	  }
	  
	  "create a new game if the ident is unknown" ! prop { r: (ReportData, Timestamp, Int) =>
	    val initReporter = mock[InitialReport]
	    val db = mock[InitialReportAcceptorDblayer]

	    val identIsKnown = r._3 > 0
	    
	    db.selectGameId(any) returns (if (identIsKnown) None else Some(r._3))
	    
	    val subject = new InitialReportAcceptor(mock[InitialGameToReportLinker], initReporter,
	        mock[RunningGameUpdater])
	    subject.init(db)
	    subject.acceptInitialReport(r._1, r._2)
	    
	    got {
	      if (identIsKnown) {
	        one(initReporter).createGameAndReturnId(r._1, r._2)
	      } else {
	        no(initReporter).createGameAndReturnId(any, any)
	      }
	    }
	  }
	}
}