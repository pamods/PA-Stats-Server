package info.nanodesu.rest

import scala.collection.mutable

import info.nanodesu.lib.RefreshRunner
import net.liftweb._
import net.liftweb.common.Box
import net.liftweb.common.Loggable
import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.json.Extraction
import net.liftweb.json.JsonAST.JValue
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers

// Quick, old, horrible code
object LadderService extends RestHelper with Loggable with RefreshRunner{
  
	def initService(): Unit = {
	  LiftRules.statelessDispatch append LadderService
	  init();
	}
	
	val gameTimeOut = 30 * 1000
	val playerTimeOut = 10 * 1000
	
	override val firstLoadDelay = 15*1000
	override val RUN_INTERVAL = 2*1000
	val processName = "ladder service"
	
	override def shouldLog = false
	  
	def runQuery() = {
	  mutex synchronized {
	    // filtering like this is _VERY_ unflexible I really need to rewrite this whole webservice once people use it
	    // basically with this all game setup has to happen within gameCreationTimeOut and can NOT be prolonged
		  val foo = gameCreationTimes.toMap
		  val now = System.currentTimeMillis()
		  for (x <- foo.filter(x => now - x._2._1 > gameTimeOut || now - x._2._2 > gameTimeOut)) {
		    gameCreationTimes remove x._1
		    registeredGames remove x._1._1
		    val lobbyId = registeredLobbyIdByHost remove x._1._1
		    for (gid <- lobbyId) {
		      clientsReadyByLobby remove gid
		    }
		  }
		  
		  val rPlay = registeredPlayers.toMap
		  for (x <- rPlay.filter(System.currentTimeMillis() - _._2 > playerTimeOut)) {
		    registeredPlayers remove x._1
		  }
		  
		  if (!registeredPlayers.isEmpty) {
		    logger info "registered Players are => " + registeredPlayers
		  }
		  if (!registeredGames.isEmpty) {
		    logger info "registered Games are => " + registeredGames
		  }
		  if (!registeredLobbyIdByHost.isEmpty) {
		    logger info "registered Lobby id for hosts => " + registeredLobbyIdByHost
		  }
		  if (!clientsReadyByLobby.isEmpty) {
		    // clearing that set somehow fails sometimes.
		    // no idea why. It is probably not worth it to investigate, I'll really do a rewrite in maybe a week or so
		    //hadStuff = true
		    //logger info "clients ready for lobby => " + clientsReadyByLobby
		  }
		  if (!gameCreationTimes.isEmpty) {
		    logger info "game creation times => " + gameCreationTimes
		  }
	  }
	}
	
	val mutex = new Object()

	val clientsReadyByLobby = mutable.Set[String]()
	val registeredLobbyIdByHost = mutable.Map[String, String]()
	val registeredGames = mutable.Map[String, String]()
	val gameCreationTimes = mutable.Map[(String, String), (Long, Long)]()
	val registeredPlayers = mutable.Map[String, Long]()
	
	val successfulGameIds = mutable.Set[String]()
	
	serve {
	  case "hasPlayersSearching" :: Nil Get _ =>
	    val hasPlayers = mutex synchronized {
	      registeredPlayers.nonEmpty
	    }
	    Extraction decompose Map("hasPlayers" -> hasPlayers)
	}
	
		
	serve {
	  case "resetMyTimeout" :: Nil Get _ => 
	    for (uberName <- S.param("ubername")) {
	    	resetTimeOutFor(uberName)
	    }
	    OkResponse()
	}
	
	case class GameInfo(serverCreated: Boolean, hasTimeOut: Boolean, lobbyId: String)
	serve {
	  case "pollGameId" :: Nil Get _ => 
	    val uberName = S.param("ubername")
	    
	    for (uN <- uberName) yield {
	    	logger info "pollGameId for " + uN
	      
	    	var timeout = false;
	    	
	    	val other = mutex synchronized {
	    	  resetTimeOutFor(uN)
	    	  
	    	  val r = for (x <- registeredGames if x._2 == uN) yield {
	    	    x._1
	    	  }
	    	  
	    	  if (r.nonEmpty) {
	    	    logger info "get " + r.head + " from " + registeredLobbyIdByHost
	    	    
	    	    (registeredLobbyIdByHost get r.head).map((r.head, _)) getOrElse ("", "")
	    	  } else {
	    	    timeout = true;
	    	    ("", "")
	    	  }
	    	}
	    	
	    	val inf = GameInfo(!other._1.isEmpty(), timeout, other._2)
	    	
	    	logger info inf + " polled for " + uberName
	    	
	    	Extraction decompose inf
	    }
	}
	
	// TODO add session verification
	case class NameMessage(uber_name: String, game_id: String)
	object NameMessage {
	    def apply(in: JValue): Box[NameMessage] = Helpers.tryo(in.extract[NameMessage])
	    def unapply(in: JValue): Option[NameMessage] = apply(in)
	}
	
	// TODO add session verification	
	serve {
	  case "unregister" :: Nil JsonPost NameMessage(data) -> _ => {
	    mutex synchronized {
	      
	      registeredPlayers remove data.uber_name
	      val targets = registeredGames filter ((x) => (x._1 == data.uber_name || x._2 == data.uber_name))
	      registeredGames --= targets.keys
	      
	      for (target <- targets) {
	        logger info "clear game: "+target
			val lobbyId = registeredLobbyIdByHost remove target._1
			logger info "lobbyId for target = " + lobbyId  
			for (gid <- lobbyId) {
			   clientsReadyByLobby remove gid
			}
	      }
	    }
	    
	    OkResponse()
	  }
	}
	
	// TODO add session verification
	serve {
	  case "register" :: Nil JsonPost NameMessage(data) -> _ => {
	    mutex synchronized {
	    	registeredPlayers put (data.uber_name, System.currentTimeMillis())
	    	
	    	logger info "registered " + data.uber_name
	    	
	    	cleanDataForSearchingPlayer(data.uber_name)
	    	
		    if (registeredPlayers.size >= 2) {
		      val iter = registeredPlayers.iterator
		      val pA = iter.next._1
		      val pB = iter.next._1
		      
		      registeredPlayers remove pA
		      registeredPlayers remove pB
		      
		      logger info "registered game " + pA + " vs " + pB
		      
		      registeredGames put (pA, pB)
		      val now = System.currentTimeMillis()
		      gameCreationTimes put ((pA, pB), (now, now))
		    }
	    }
	    
	    OkResponse()
	  }
	}
	
	case class HasGameResponse(hasGame: Boolean, isHost: Boolean)
	
		// TODO add session verification
	serve {
	  case "hasGame" :: Nil JsonPost NameMessage(data) -> _ => {
	    val r = mutex synchronized  {
		    val isHost = registeredGames.keySet.contains(data.uber_name)
		    val isPlayer = registeredGames.values.toList.contains(data.uber_name)
		    (isHost, isPlayer)
	    }
	    
	    val inf = HasGameResponse(r._1 || r._2, r._1)
	    
	    logger info "has game response for " + data.uber_name + " is => " + inf
	    
	    Extraction decompose inf	 
	  }
	}
	
		// TODO add session verification
	serve {
	  case "gameHosted" :: Nil JsonPost NameMessage(data) -> _ => {
	    mutex synchronized {
	      registeredLobbyIdByHost put (data.uber_name, data.game_id)
	      
	      resetTimeOutFor(data.uber_name)
	      
	      logger info "gameHosted for " + data
	    }
	    
	    OkResponse()
	  }
	}
	
	private def timeOutFor(uberName: String) = {
	  val hasTimeOut = gameCreationTimes.count(x => x._1._1 == uberName || x._1._2 == uberName) == 0
	  if (hasTimeOut) {
	    cleanDataForSearchingPlayer(uberName)
	  }
	  hasTimeOut
	}

	private def resetTimeOutFor(uberName: String) = {
	  for(game <- gameCreationTimes.find(x => x._1._1 == uberName || x._1._2 == uberName)) {
	    val now = System.currentTimeMillis()
        val host = game._1._1
	    val client = game._1._2
	    if (host == uberName) {
	      gameCreationTimes put ((host, client), (now, game._2._2))
	    } else {
	      gameCreationTimes put ((host, client), (game._2._1, now))
	    }
	  }
	}
	
	// TODO add session verification
	case class ShouldStartResponse(shouldStart: Boolean, hasTimeOut: Boolean)
	serve {
	  case "shouldStartServer" :: Nil JsonPost NameMessage(data) -> _ => {
	    val resp = ShouldStartResponse (mutex synchronized (clientsReadyByLobby contains data.game_id),
	        timeOutFor(data.uber_name))

	    logger info "answer to should start server for " + data.uber_name + " is => " + resp
	    
	    mutex synchronized {
	    	resetTimeOutFor(data.uber_name)
	    }
	    
	    if (resp.shouldStart) {
	      mutex synchronized {
		      clientsReadyByLobby remove data.game_id

		      cleanDataForSearchingPlayer(data.uber_name)
		      
		      successfulGameIds += data.game_id
		      
		      logger info "===done for " + data.game_id+"==="
	      }
	    }
	    
	    Extraction decompose resp
	  }
	}
	
	private def cleanDataForSearchingPlayer(uberName: String) = {
		registeredLobbyIdByHost remove uberName
		registeredGames remove uberName
	}
	
	case class TimeOutInfo(hasTimeOut: Boolean)

	// TODO add session verification
	serve {
	  case "readyToStart" :: Nil JsonPost NameMessage(data) -> _ => {
	    
	    if (data.game_id.isEmpty()) {
	      logger info "got empty game_id from " + data.uber_name
	      BadResponse()
	    } else { 
	      var timeout = false;
          mutex synchronized {
            resetTimeOutFor(data.uber_name)
        	timeout = timeOutFor(data.uber_name)
            logger info "rdy to start note from " + data + " timeout="+timeout
		  	clientsReadyByLobby add data.game_id
		  }
	      Extraction decompose TimeOutInfo(timeout)
	    }
	  }
	}
}