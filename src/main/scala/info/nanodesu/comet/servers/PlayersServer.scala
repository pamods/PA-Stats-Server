package info.nanodesu.comet.servers

import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.collectors.gameinfo.loader.ActiveReportersForGameLoader

case class PlayerInfo(locked: Boolean, name: String, primColor: String, secondaryColor: String)
class PlayersServer(val gameId: Int) {
	private var playersInfo: Map[Int, PlayerInfo] = Map.empty
	
	def players = playersInfo
	
	def setPlayerInfo(id: Int, locked: Boolean, name: String, primColor: String, secColor: String) = {
	  playersInfo += id -> PlayerInfo(locked, name, primColor, secColor)
	}
	
	def unlockPlayer(id: Int) = {
	  val prev = playersInfo(id)
	  playersInfo += id -> prev.copy(locked = false)
	}
	
	def clearUp() = {
	  playersInfo = Map.empty
	}
	
	def forcefulInit() = {
	  CookieBox withSession { db =>
	    for (p <- new ActiveReportersForGameLoader().selectActiveReportersWithName(db, gameId)) {
	      setPlayerInfo(p.id, p.locked, p.name, p.primaryColor, p.secondaryColor)
	    }
	  }
	}
}