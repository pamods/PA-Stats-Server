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
import info.nanodesu.model.db.collectors.gameinfo.ChartPlayer

class ChartDataServer(val gameId: Int, players: PlayersServer) extends Loggable {
	private var dataPoints: Map[String, List[ChartDataPoint]] = Map.empty

	// this is only used in case of an init of a server that is not caused by a starting game
	// it's a bit dangerous (race conditions that can cause wrong game data in the comet),
	// but it should only happen after server restarts for running games
	def forcefulInit(initialData: ChartDataPackage) = {
	  dataPoints = initialData.playerTimeData
	}
	
	def clearUp() = {
	  dataPoints = Map.empty
	}
	
	def addChartDataFor(playerId: Int, time: Long, stats: StatsReportData) = {
	  val pIdAsStr = playerId.toString
	  val data = dataPoints.getOrElse(pIdAsStr, List.empty)
	  dataPoints += pIdAsStr -> (ChartDataPackage.makeDataPoint(time, stats) :: data)
	}

	def makePackage: ChartDataPackage = 
	  ChartDataPackage(gameId, dataPoints.filterKeys(x => !players.players(x.toInt).locked),
	    players.players.map(x => (x._1.toString, ChartPlayer(x._2.name, x._2.primColor))))
}