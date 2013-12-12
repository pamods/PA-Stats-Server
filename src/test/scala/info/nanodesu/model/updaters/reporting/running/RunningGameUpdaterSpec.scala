package info.nanodesu.model.updaters.reporting.running

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
import info.nanodesu.model.db.updaters.reporting.running.RunningGameUpdaterDbLayer
import info.nanodesu.model.db.updaters.reporting.running.RunningGameStatsReporter
import info.nanodesu.model.StatsReportData
import info.nanodesu.model.RunningGameData
import java.util.Date

@RunWith(classOf[JUnitRunner])
class RunningGameUpdaterSpec extends Specification with Mockito with ThrownExpectations{
	
    def makeSomeData = new RunningGameData(12, new StatsReportData(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14))
  
	"The RunningGameUpdater" should {
	  
	  "update the end date of the game from the retrieved id to the given time" in {
	    val db = mock[RunningGameUpdaterDbLayer]
	    
	    val dat = makeSomeData
	    val gameId = 1265
	    
	    db.selectGameIdFromLink(dat.gameLink) returns Some(gameId)
	    
	    val subject = new RunningGameStatsReporter()
	    subject.init(db)
	    
	    val now = new Date()
	    
	    subject.insertRunningGameData(dat, now)
	    
	    got {
	      one(db).updateGameEndTime(gameId, now)
	    }
	  }
	  
	  "insert the data to the correct link" in {
	    val db = mock[RunningGameUpdaterDbLayer]
	    
	    val dat = makeSomeData
	    val gameId = 346
	    
	    db.selectGameIdFromLink(dat.gameLink) returns Some(gameId)
	    
	    val subject = new RunningGameStatsReporter()
	    subject.init(db)
	    
	    val now = new Date()
	    
	    subject.insertRunningGameData(dat, now)
	    
	    got {
	      one(db).insertStatsData(dat.stats, dat.gameLink, now)
	    }
	  }
	}
}