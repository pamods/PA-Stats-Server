package info.nanodesu.comet.servers

import net.liftweb.common.Loggable
import info.nanodesu.model.db.collectors.gameinfo.ChartDataPoint
import info.nanodesu.model.db.collectors.gameinfo.ChartPlayer
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.collectors.gameinfo.ChartDataCollector
import info.nanodesu.model.db.collectors.gameinfo.ChartDataPackage
import info.nanodesu.model.db.collectors.gameinfo.ChartPlayer
import info.nanodesu.model.StatsReportData
import info.nanodesu.model.db.collectors.gameinfo.ChartDataPoint
import info.nanodesu.model.db.collectors.gameinfo.ChartDataPoint

class ChartDataServer(val gameId: Int) extends Loggable {
	private var dataPoints: Map[String, List[ChartDataPoint]] = Map.empty
	private var players: Map[String, ChartPlayer] = Map.empty
	
	// this is only used in case of an init of a server that is not caused by a starting game
	// it's a bit dangerous (race conditions that can cause wrong game data in the comet),
	// but it should only happen after server restarts for running games
	def forcefulInit(initialData: ChartDataPackage) = {
	  dataPoints = initialData.playerTimeData
	  players = initialData.playerInfo
	}
	
	def clearUp() = {
	  dataPoints = Map.empty
	  players = Map.empty
	}
	
	def addChartDataFor(playerId: Int, time: Long, stats: StatsReportData) = {
	  val pIdAsStr = playerId.toString
	  val data = dataPoints.getOrElse(pIdAsStr, List.empty)
	  dataPoints += pIdAsStr -> (ChartDataPackage.makeDataPoint(time, stats) :: data)
	}
	
	def setPlayerInfo(playerId: Int, name: String, color: String) = {
	  val pIdAsStr = playerId.toString
	  players += pIdAsStr -> ChartPlayer(name, color)
	}
	
	def makePackage: ChartDataPackage = ChartDataPackage(gameId, dataPoints, players)
}