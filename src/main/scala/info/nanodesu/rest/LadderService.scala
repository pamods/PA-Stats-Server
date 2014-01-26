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

// Quicky, old, horrible code
object LadderService extends RestHelper with Loggable with RefreshRunner{
  
	def initService(): Unit = {
	  LiftRules.statelessDispatch append LadderService
	  init();
	}
	
	override val firstLoadDelay = 5*1000
	override val RUN_INTERVAL = 3*1000
	val processName = "ladder service"
	
	override def shouldLog = false
	  
	def runQuery() = {
	  mutex synchronized {
		  val foo = gameCreationTimes.toMap
		  for (x <- foo.filterKeys(System.currentTimeMillis() - _ > 60 * 1000)) {
		    gameCreationTimes retain ((k, v) => (v != x._2))
		    registeredGames remove x._2
		    val lobbyId = registeredLobbyIdByHost remove x._2
		    for (gid <- lobbyId) {
		      clientsReadyByLobby remove gid
		    }
		  }
		  
		  val rPlay = registeredPlayers.toMap
		  for (x <- rPlay.filter(System.currentTimeMillis() - _._2 > 10 * 1000)) {
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
		    logger info "clients ready for lobby => " + clientsReadyByLobby
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
	val gameCreationTimes = mutable.Map[Long, String]()
	val registeredPlayers = mutable.Map[String, Long]()
	
	case class GameInfo(serverCreated: Boolean, hasTimeOut: Boolean, lobbyId: String)
	serve {
	  case "pollGameId" :: Nil Get _ => 
	    val uberName = S.param("ubername")
	    
	    for (uN <- uberName) yield {
	    	logger info "pollGameId for " + uN
	      
	    	var timeout = false;
	    	
	    	val other = mutex synchronized {
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
	
	case class NameMessage(uber_name: String, game_id: String)
	object NameMessage {
	    def apply(in: JValue): Box[NameMessage] = Helpers.tryo(in.extract[NameMessage])
	    def unapply(in: JValue): Option[NameMessage] = apply(in)
	}

	serve {
	  case "unregister" :: Nil JsonPost NameMessage(data) -> _ => {
	    mutex synchronized {
	      registeredPlayers remove data.uber_name
	      registeredGames retain ((k, v) => (k != data.uber_name && v != data.uber_name))
	      
	      logger info "unregistered " + data.uber_name
	    }
	    
	    OkResponse()
	  }
	}
	
	serve {
	  case "register" :: Nil JsonPost NameMessage(data) -> _ => {
	    mutex synchronized {
	    	registeredPlayers put (data.uber_name, System.currentTimeMillis())
	    	
	    	logger info "registered " + data.uber_name
	    	
		    if (registeredPlayers.size >= 2) {
		      val iter = registeredPlayers.iterator
		      val pA = iter.next._1
		      val pB = iter.next._1
		      
		      registeredPlayers remove pA
		      registeredPlayers remove pB
		      
		      logger info "registered game " + pA + " vs " + pB
		      
		      registeredGames put (pA, pB)
		      gameCreationTimes put (System.currentTimeMillis(), pA)
		    }
	    }
	    
	    OkResponse()
	  }
	}
	
	case class HasGameResponse(hasGame: Boolean, isHost: Boolean)
	
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
	
	serve {
	  case "gameHosted" :: Nil JsonPost NameMessage(data) -> _ => {
	    mutex synchronized {
	      registeredLobbyIdByHost put (data.uber_name, data.game_id)
	      
	      logger info "gameHosted for " + data
	    }
	    
	    OkResponse()
	  }
	}
	
	case class ShouldStartResponse(shouldStart: Boolean, hasTimeOut: Boolean)
	serve {
	  case "shouldStartServer" :: Nil JsonPost NameMessage(data) -> _ => {
	    val resp = ShouldStartResponse (mutex synchronized (clientsReadyByLobby contains data.game_id), !registeredGames.contains(data.uber_name))

	    logger info "answer to should start server for " + data.uber_name + " is => " + resp
	    
	    if (resp.shouldStart) {
	      mutex synchronized {
		      clientsReadyByLobby remove data.game_id
		      registeredLobbyIdByHost remove data.uber_name
		      registeredGames remove data.uber_name
		      gameCreationTimes retain ((k,v) => (v != data.uber_name))
		      
		      logger info "===done for " + data.game_id+"==="
	      }
	    }
	    
	    Extraction decompose resp
	  }
	}
	
	serve {
	  case "readyToStart" :: Nil JsonPost NameMessage(data) -> _ => {
	    
	    if (data.game_id.isEmpty()) {
	      logger info "got empty game_id from " + data.uber_name
	      BadResponse()
	    } else {
		    mutex synchronized {
		      
		      logger info "rdy to start note from " + data
		      
		      clientsReadyByLobby add data.game_id
		    }
		    
		    OkResponse()
	    }
	  }
	}
}