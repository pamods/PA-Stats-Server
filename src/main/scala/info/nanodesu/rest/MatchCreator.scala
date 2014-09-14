package info.nanodesu.rest

import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import info.nanodesu.lib.RatingsMachine

class MatchCreator extends Loggable {
  val PLAYER_TIMEOUT = 45 * 1000
  val MINUTES_RATING_GROW = 60
  val DEFAULT_SKILL = 1600
  
  type LobbyId = Box[String]

  case class Player(ubername: String, rating: Float, searchingSince: Long, lastMessageTime: Long, wantsToLeave: Boolean = false, clientReady: Boolean = false, host: Boolean = false, shouldReset: Boolean = false) {
    def isReady = (clientReady || host) && !wantsToLeave
  }
  case class Game(lobbyId: LobbyId, playerA: Player, playerB: Player) {
    def hasPlayer(uberName: String) = playerA.ubername == uberName || playerB.ubername == uberName
    def getPlayer(uberName: String) = if (playerA.ubername == uberName) playerA else playerB
  }

  case class MatchMakerState(playersInPool: Set[Player], games: Set[Game])

  @volatile
  var state = MatchMakerState(Set.empty, Set.empty)
  
  def processMatching() = {
    val workState = state
    
    val pairs = workState.playersInPool.toSet[Player].subsets.map(_.toList).toList.filter(_.size == 2).map(x => (x.head, x.tail.head))
    
    // TODO this causes players to be matched "randomly" within the max difference of MINUTES_RATING_GROW. Maybe change that
    val newGames = for (
      (pa, pb) <- pairs if (playersMatch(pa, pb))
    ) yield {
      val result = Game(Empty, pa.copy(host = true), pb)
      logger info "new game " + result
      result
    }
    
    state = state.copy(playersInPool = state.playersInPool.filterNot(x => newGames.exists(g => g.playerA.ubername == x.ubername || g.playerB.ubername == x.ubername)),
        games = state.games ++ newGames)
  }

  def processCleanTimeouts() = {
    def isTimeout(time: Long) = System.currentTimeMillis() - time > PLAYER_TIMEOUT
    def cleanedPool(pool: Set[Player]) = pool.filter(p => !isTimeout(p.lastMessageTime) && !p.wantsToLeave)
    def cleanedGames(games: Set[Game]) = games.filter(g => !g.playerA.wantsToLeave && !g.playerB.wantsToLeave && 
        !isTimeout(g.playerA.lastMessageTime) && !isTimeout(g.playerB.lastMessageTime))
    state = state.copy(playersInPool = cleanedPool(state.playersInPool), games = cleanedGames(state.games))
  }
  
  def setLobbyIdForPlayer(uberName: String, lobbyId: String) = {
    def updateForGames(games: Set[Game]) = {
	    val gameBox = games.find(g => g.playerA.ubername == uberName || g.playerB.ubername == uberName)
	    if (gameBox.isDefined) {
	      val game = gameBox.get
	      val gamesWithout = games.filterNot(_ == game)
	      val hadLobbyAlready = game.lobbyId.isDefined
	      val modGame = game.copy(lobbyId = Full(lobbyId))
	      
	      gamesWithout + (if (hadLobbyAlready) {
	    	  val isPlayerA = game.playerA.ubername == uberName
	    	  if (isPlayerA) {
	    	    logger info "got a new lobby, reset for " + modGame.playerB.ubername
	    	    modGame.copy(playerB = modGame.playerB.copy(shouldReset = true, clientReady = false))
	    	  } else {
	    	    logger info "got a new lobby, reset for " + modGame.playerA.ubername
	    	    modGame.copy(playerA = modGame.playerA.copy(shouldReset = true, clientReady = false))
	    	  }
	      } else modGame)
	    } else {
	      games
	    }
    }
    
    state = state.copy(games = updateForGames(state.games))
  }
  
  def hasTimeout(ubername: String) = {
    val checkedState = state
    !checkedState.playersInPool.exists(_.ubername == ubername) && !checkedState.games.exists(g => g.playerA.ubername == ubername || g.playerB.ubername == ubername)
  }

  def findGameFor(uberName: String) = state.games.find(_.hasPlayer(uberName))
  
  def registerPlayer(uberName: String) = {
    if (findGameFor(uberName).isEmpty && !state.playersInPool.exists(_.ubername == uberName)) {
	    val newPlayer = Player(uberName, lookupRating(uberName), System.currentTimeMillis(), System.currentTimeMillis())
	    state = state.copy(playersInPool = state.playersInPool + newPlayer)
    }
  }

  def setClientReady(uberName: String, lobbyId: String) = {
    def setForGames(games: Set[Game]) = {
      val game = games.find(_.hasPlayer(uberName))
      if (game.isDefined) {
        val gamesWithout = games.filterNot(_ == game.get)
        val correctLobby = game.get.lobbyId == lobbyId
        val modGame = if (game.get.playerA.ubername == uberName) {
          if (correctLobby) game.get.copy(playerA = game.get.playerA.copy(clientReady = true))
          else {
            logger info "client ready resulted in should reset for " + game.get.playerA.ubername
            game.get.copy(playerA = game.get.playerA.copy(shouldReset = true))
          }
        } else {
          if (correctLobby) game.get.copy(playerB = game.get.playerB.copy(clientReady = true))
          else {
            logger info "client ready resulted in should reset for " + game.get.playerB.ubername
            game.get.copy(playerA = game.get.playerB.copy(shouldReset = true))
          }
        }
        gamesWithout + modGame
      } else {
        games
      }
    }
    
    state = state.copy(games = setForGames(state.games))
  }
  
  def resetGameBy(uberName: String) = {
    def setForGames(games: Set[Game]) = {
      val game = games.find(_.hasPlayer(uberName))
      if (game.isDefined) {
        val gamesWithout = games.filterNot(_ == game.get)
        val modGame = if(game.get.playerA.ubername == uberName) {
          game.get.copy(lobbyId = Empty, playerB = game.get.playerB.copy(shouldReset = true, clientReady = false))
        } else {
          game.get.copy(lobbyId = Empty, playerA = game.get.playerA.copy(shouldReset = true, clientReady = false))
        }
        gamesWithout + modGame
      } else {
        games
      }
    }
    
    state = state.copy(games = setForGames(state.games))
  }
  
  def checkResetFor(uberName: String) = {
    val hadReset = state.games.find(_.hasPlayer(uberName)).map(x => x.getPlayer(uberName).shouldReset).getOrElse(false)
    
    def setForGames(games: Set[Game]) = {
      val game = games.find(_.hasPlayer(uberName))
      if (game.isDefined) {
        val gamesWithout = games.filterNot(_ == game.get)
        gamesWithout + (if (uberName == game.get.playerA.ubername) game.get.copy(playerA = game.get.playerA.copy(shouldReset = false))
        			   else game.get.copy(playerB = game.get.playerB.copy(shouldReset = false)))
      } else {
        games
      }
    }
    
    state = state.copy(games = setForGames(state.games))
    logger info "had reset for " + uberName + " = " + hadReset
    hadReset
  }
  
  def resetTimeout(uberName: String) = {
    def resetForPool(pool: Set[Player]) = {
      val player = pool.find(_.ubername == uberName)
      if (player.isDefined) {
        val poolWithout = pool.filterNot(_ == player.get)
        val modPlayer = player.get.copy(lastMessageTime = System.currentTimeMillis())
        poolWithout + modPlayer
      } else {
        pool
      }
    }
    
    def resetForGames(games: Set[Game]) = {
      val game = games.find(_.hasPlayer(uberName))
      if (game.isDefined) {
        val gamesWithout = games.filterNot(_ == game.get)
        val modGame = if (game.get.playerA.ubername == uberName) {
          game.get.copy(playerA = game.get.playerA.copy(lastMessageTime = System.currentTimeMillis()))
        } else {
          game.get.copy(playerB = game.get.playerB.copy(lastMessageTime = System.currentTimeMillis()))
        }
        gamesWithout + modGame
      } else {
        games
      }
    }
    
    state = state.copy(playersInPool = resetForPool(state.playersInPool), games = resetForGames(state.games))
  }
  
  def unregisterPlayer(uberName: String) = {
    def cleanPool(pool: Set[Player]) = {
      val player = pool.find(_.ubername == uberName)
      if (player.isDefined) {
        val poolWithout = pool.filterNot(_ == player.get)
        val modPlayer = player.get.copy(wantsToLeave = true)
        poolWithout + modPlayer
      } else {
        pool
      }
    }
    
    def cleanedGames(games: Set[Game]) = {
      val game = games.find(g => g.hasPlayer(uberName))
      if (game.isDefined) {
        val gamesWithout = games.filterNot(_ == game.get)
        val modGame = if (game.get.playerA.ubername == uberName) {
          game.get.copy(playerA = game.get.playerA.copy(wantsToLeave = true))
        } else {
          game.get.copy(playerB = game.get.playerB.copy(wantsToLeave = true))
        }
        gamesWithout + modGame
      } else {
        games
      }
    }
    
    state = state.copy(playersInPool = cleanPool(state.playersInPool), games = cleanedGames(state.games))
  }
  
  def minutesTillPlayerFindsAGame(ubername: String): Box[Int] = {
    val playerInQueue = state.playersInPool.find(_.ubername == ubername);
    
    val minutes = playerInQueue match {
      case Some(player) =>
        for (p <- state.playersInPool if p.ubername != ubername) yield {
          val ptsDiff = Math.abs(player.rating - p.rating)
          val aOffset = playerTimeBasedMatchOffset(player)
          val bOffset = playerTimeBasedMatchOffset(p)
          val minOffset = Math.min(aOffset, bOffset)
          if (minOffset >= ptsDiff) 0
          else Math.ceil((ptsDiff - minOffset) / MINUTES_RATING_GROW) 
        }
      case None =>
		val ratingOfView = lookupRating(ubername)
		 for (p <- state.playersInPool) yield {
		  minutesTillPlayersAreMatched(ratingOfView, p.rating)
		}
    }
    
	val sortedList = minutes.toList.sorted
	if (sortedList.isEmpty) Empty
	else Full(sortedList.head.toInt)
  }

  def hasPlayersSearching = !state.playersInPool.isEmpty
  
  private def playersMatch(playerA: Player, playerB: Player) = {
    val aOffset = playerTimeBasedMatchOffset(playerA)
    val bOffset = playerTimeBasedMatchOffset(playerB)
    val diff = Math.abs(playerA.rating - playerB.rating)
    logger info playerA.ubername+"-offset is "+aOffset+", "+playerB.ubername+"-offset is "+bOffset+"; diff is "+diff
    val aInRange = diff <= aOffset
    val bInRange = diff <= bOffset
    aInRange && bInRange
  }

  private def playerTimeBasedMatchOffset(player: Player) = {
    val timeOffsetMins = Math.ceil((System.currentTimeMillis() - player.searchingSince) / 1000f / 60)
    timeOffsetMins * MINUTES_RATING_GROW
  }

  private def minutesTillPlayersAreMatched(skillA: Float, skillB: Float) = {
    val diff = Math.abs(skillA - skillB)
    Math.ceil(diff / MINUTES_RATING_GROW)
  }

  private def lookupRating(ubername: String) = 5 //RatingsMachine.querySkill(ubername).map(_.toFloat).getOrElse(DEFAULT_SKILL.toFloat)
}