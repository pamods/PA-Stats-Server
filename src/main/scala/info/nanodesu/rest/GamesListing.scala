package info.nanodesu.rest

import net.liftweb.http.rest.RestHelper
import net.liftweb.common.Loggable
import info.nanodesu.lib.RefreshRunner
import net.liftweb.http.LiftRules
import net.liftweb.json.Extraction
import net.liftweb.json.JsonAST.JValue
import net.liftweb.common.Box
import net.liftweb.util.Helpers
import net.liftweb.http.OkResponse
import net.liftweb._
import net.liftweb.common.Box
import net.liftweb.common.Loggable
import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.json.Extraction
import net.liftweb.json.JsonAST.JValue
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers
import net.liftweb.common.Full

object GamesListing extends RestHelper with Loggable with RefreshRunner {
	override def shouldLog = false
	
	def initService(): Unit = {
	  LiftRules.statelessDispatch append GamesListing
	  init()
	}
	
	override val firstLoadDelay = 15 * 1000
    override val RUN_INTERVAL = 3 * 1000
    val processName = "games listing service"
	
  	case class Game(beacon: String, version: String, id: String, ip: String, port: String, lastAvailable: Long)
      
    var games = List[Game]()
    val mutex = new Object()
    
    def runQuery() = mutex synchronized {
	  games = games.filter(_.lastAvailable - System.currentTimeMillis() > -7000);
	}
	
  	case class ListMessage(beacon: String, version: String, ip: String, port: String, id: String)
	object ListMessage {
	    def apply(in: JValue): Box[ListMessage] = Helpers.tryo(in.extract[ListMessage])
	    def unapply(in: JValue): Option[ListMessage] = apply(in)
	  }
	
	serve {
	  case "addServer" :: Nil JsonPost ListMessage(data) -> _ =>
	    mutex synchronized {
	      games = games.filter(_.id != data.id)
	      games = Game(data.beacon, data.version, data.id, data.ip, data.port, System.currentTimeMillis()) :: games
	    }
	    
	    OkResponse()
	}
	
	serve {
	  case "servers" :: Nil Get _ =>
	    Extraction decompose games
	}
}