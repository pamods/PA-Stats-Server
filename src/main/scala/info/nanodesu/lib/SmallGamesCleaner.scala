package info.nanodesu.lib

import net.liftweb.util.Props
import info.nanodesu.model.db.updaters.cleaners.ShortGamesCleaner
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.updaters.cleaners.ForceUnlocker
import info.nanodesu.model.db.updaters.cleaners.ObserverDataCleaner

object SmallGamesCleaner extends RefreshRunner{
	override val firstLoadDelay = 1000 * 60
	override def RUN_INTERVAL = 1000 * 60 * Props.getInt("deletionInterval", 5)
	val processName = "small games cleaner"
	
	var minDataPointsToKeep = 0;
	var minGameLength = 0
	var deleteSinglePlayerGames = false
	  
	override def initLoad() = {
	  minGameLength = Props.getInt("minGameLength", 5)
	  deleteSinglePlayerGames = Props.getBool("deleteSinglePlayerGames", true)
	  minDataPointsToKeep = Props.getInt("minDataPointsToKeep", 5);
	  logger info "will delete games below a length of " + minGameLength + " minutes"
	  logger info "deleteSinglePlayerGames = "+deleteSinglePlayerGames
	  logger info "will delete datapoints if player has less than " + minDataPointsToKeep + " datapoints"
	}
	
	def runQuery() = CookieBox withSession { db =>
		if (minGameLength > 0) {
		  new ShortGamesCleaner(db).clean(minGameLength)
		}
		
		new ForceUnlocker(db).unlock()
		
		if (minDataPointsToKeep > 0) {
		  new ObserverDataCleaner(db).clean(minDataPointsToKeep)
		}
	}
}