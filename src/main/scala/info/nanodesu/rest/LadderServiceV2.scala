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

object LadderServiceV2 extends RestHelper with Loggable with RefreshRunner {

  override def shouldLog = false

  val matcher = new MatchCreator()

  override val firstLoadDelay = 15 * 1000
  override val RUN_INTERVAL = 5 * 1000
  val processName = "ladder service v2"

  var confirmedLobbys = List[String]()  
    
  def initService(): Unit = {
    LiftRules.statelessDispatch append LadderServiceV2
    init();
  }

  def runQuery() = {
    matcher.processMatching()
    matcher.processCleanTimeouts()
    
    if (!matcher.state.playersInPool.isEmpty || !matcher.state.games.isEmpty) {
	    logger info "____"
	    logger info matcher.state
	    logger info "____"
    }
  }

  case class NameMessage(uber_name: String, game_id: String)
  object NameMessage {
    def apply(in: JValue): Box[NameMessage] = Helpers.tryo(in.extract[NameMessage])
    def unapply(in: JValue): Option[NameMessage] = apply(in)
  }

  def confirmedGames = confirmedLobbys.groupBy(x => x).mapValues(_.size).filter(_._2 == 2).size
  
  serve {
    case "confirmLobby" :: Nil Get _ =>
      for (lobby <- S.param("lobby")) {
        confirmedLobbys ::= lobby
      }
      OkResponse()
  }
  
  serve {
    case "hasPlayersSearching" :: Nil Get _ =>
      Extraction decompose Map("hasPlayers" -> matcher.hasPlayersSearching)
  }

  serve {
    case "minutesTillMatch" :: Nil Get _ =>
      for (
        uberName <- S.param("ubername");
        minutes <- matcher.minutesTillPlayerFindsAGame(uberName)
      ) yield {
        Extraction decompose Map("minutes" -> minutes)
      }
  }

  serve {
    case "resetMyTimeout" :: Nil Get _ =>
      for (uberName <- S.param("ubername")) {
        logger info "resetMyTimeout("+uberName+")"
        matcher.resetTimeout(uberName)
      }
      OkResponse()
  }

  serve {
    case "register" :: Nil JsonPost NameMessage(data) -> _ =>
      logger info "will register " + data.uber_name
      matcher.registerPlayer(data.uber_name)
      matcher.resetTimeout(data.uber_name)
      OkResponse()
  }

  case class HasGameResponse(hasGame: Boolean, isHost: Boolean)

  serve {
    case "hasGame" :: Nil JsonPost NameMessage(data) -> _ =>
      matcher.resetTimeout(data.uber_name)

      matcher.findGameFor(data.uber_name) match {
        case Some(g) =>
          val isPlayerA = g.playerA.ubername == data.uber_name
          val isHost = if (isPlayerA) g.playerA.host else g.playerB.host
          logger info data.uber_name + " HasGame(hasGame = true, isHost = "+isHost+")";
          Extraction decompose HasGameResponse(true, isHost)
        case _ =>
          Extraction decompose HasGameResponse(false, false)
      }
  }

  case class GameInfo(serverCreated: Boolean, hasTimeOut: Boolean, lobbyId: String)
  serve {
    case "pollGameId" :: Nil Get _ =>

      for (uberName <- S.param("ubername")) yield {
        matcher.resetTimeout(uberName)

        Extraction decompose (matcher.findGameFor(uberName) match {
          case Some(g) if g.lobbyId.isDefined =>
            logger info uberName + " has lobby with id "+g.lobbyId.get
            GameInfo(true, false, g.lobbyId.get)
          case Some(g) if g.lobbyId.isEmpty =>
            logger info uberName + " has to wait some more for the lobby"
            GameInfo(false, false, "")
          case _ =>
            logger info uberName + " has a timeout"
            GameInfo(false, true, "")
        })
      }
  }

  serve {
    case "unregister" :: Nil JsonPost NameMessage(data) -> _ =>
      logger info "will unregister " + data.uber_name
      matcher.unregisterPlayer(data.uber_name)
      OkResponse()
  }

  serve {
    case "gameHosted" :: Nil JsonPost NameMessage(data) -> _ =>
      matcher.resetTimeout(data.uber_name)
      logger info "set lobby id "+data.game_id + " for hosting player " + data.uber_name
      matcher.setLobbyIdForPlayer(data.uber_name, data.game_id)
      OkResponse()
  }

  case class ShouldStartResponse(shouldStart: Boolean, hasTimeOut: Boolean)
  serve {
    case "shouldStartServer" :: Nil JsonPost NameMessage(data) -> _ =>
      Extraction decompose (matcher.findGameFor(data.uber_name) match {
        case Some(g) if (g.playerA.isReady) && (g.playerB.isReady) =>
          logger info data.uber_name + " should start server"
          ShouldStartResponse(true, false)
        case Some(g) =>
          logger info data.uber_name + " has to wait for the client player"
          ShouldStartResponse(false, false)
        case _ =>
          logger info data.uber_name + " has a timeout and should leave the game"
          ShouldStartResponse(false, true)
      })
  }

  case class TimeOutInfo(hasTimeOut: Boolean)
  serve {
    case "readyToStart" :: Nil JsonPost NameMessage(data) -> _ =>
      matcher.resetTimeout(data.uber_name)
      if (data.game_id.isEmpty()) {
        logger info "got empty game_id from " + data.uber_name
        BadResponse()
      } else {
        matcher.setClientReady(data.uber_name)
        val result = TimeOutInfo(matcher.findGameFor(data.uber_name).isEmpty)
        logger info "readyToStart for " + data.uber_name + " yields a timeout = " + result.hasTimeOut
        Extraction decompose result
      }
  }
}