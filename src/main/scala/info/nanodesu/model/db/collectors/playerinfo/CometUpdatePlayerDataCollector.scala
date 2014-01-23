package info.nanodesu.model.db.collectors.playerinfo

import org.jooq.DSLContext
import info.nanodesu.model.ReportDataC
import info.nanodesu.model.db.collectors.playerinfo.loader.PlayerIdForLinkLoader
import info.nanodesu.model.db.collectors.playerinfo.loader.PlayerNameLoader
import info.nanodesu.comet.GameServers

class CometUpdatePlayerDataCollector(db: DSLContext, gameLink: Int) {
	val gameId = ReportDataC.selectGameIdFromLink(db, gameLink)
	val playerId = new PlayerIdForLinkLoader(db).selectPlayerId(gameLink)
	val playerName = (for (pId <- playerId) yield new PlayerNameLoader(db).selectPlayerName(pId)).flatten
}

object CometUpdatePlayerDataCollector {
  def apply(db: DSLContext, gameLink: Int) = new CometUpdatePlayerDataCollector(db, gameLink) 
}