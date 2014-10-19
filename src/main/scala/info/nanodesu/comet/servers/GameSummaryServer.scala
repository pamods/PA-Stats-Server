package info.nanodesu.comet.servers

import info.nanodesu.lib.Formattings._
import info.nanodesu.model.StatsReportData
import info.nanodesu.model.StatsReportData
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.collectors.gameinfo.ChartDataCollector
import info.nanodesu.model.db.collectors.gameinfo.ChartDataPackage
import info.nanodesu.model.db.collectors.playerinfo.GamePlayerInfo
import info.nanodesu.model.db.collectors.gameinfo.GameInfoCollector
import info.nanodesu.model.db.collectors.gameinfo.loader.ActiveReportersForGameLoader

class GameSummaryServer(val gameId: Int, players: PlayersServer) {

  class PlayerSummary(myId: Int) extends GamePlayerInfo {
    private var startTime: Long = Long.MaxValue
    private var endTime: Long = 0
    private var metalProduced: Long = 0
    private var energyProduced: Long = 0
    private var metalWasted: Long = 0
    private var energyWasted: Long = 0
    private var actions: Int = 0
    private var buildSpeedSum: Double = 0
    private var buildSpeedPartsCount: Int = 0

    private def me = players.players(myId)
    override def name = me.name
    override def primaryColor = me.primColor
    override def secondaryColor = me.secondaryColor
    private def mayLock[T](d: => T, default: T): T = if (me.locked) default else d
    def apmAvg = {
      mayLock({
        val minutes = ((runTime.toDouble + Double.MinPositiveValue) / 1000 / 60)
        Math.round(actions / minutes).toInt
      }, 0)
    }
    
    def sumMetal = mayLock(metalProduced, 0)
    def sumEnergy = mayLock(energyProduced, 0)
    def metalUseAvg = mayLock(1 - (metalWasted.toDouble / (metalProduced + Double.MinPositiveValue)), 0)
    def energyUseAvg = mayLock(1 - (energyWasted.toDouble / (energyProduced + Double.MinPositiveValue)), 0)
    def buildSpeed = mayLock(if (buildSpeedPartsCount == 0) 1 else buildSpeedSum / buildSpeedPartsCount, 0)
    def runTime = endTime - startTime
    
    def addStats(time: Long, data: StatsReportData) = {
      if (startTime > time) {
        startTime = time
      }
      endTime = time
      metalProduced += data.metalProducedSinceLastTick
      energyProduced += data.energyProducedSinceLastTick
      metalWasted += data.metalWastedSinceLastTick
      energyWasted += data.energyWastedSinceLastTick
      actions += data.apm

      def speedFor(store: Int, spending: Int, income: Int) = {
        if (store > 0 || spending == 0) 1
        else Math.min(1, income.toDouble / spending)
      }
      
      val energySpeed = speedFor(data.energyStored, data.energySpending, data.energyIncome)
      val metalSpeed = speedFor(data.metalStored, data.metalSpending, data.metalIncome)
      
      val speed = Math.min(energySpeed, metalSpeed)
      buildSpeedSum += speed
      buildSpeedPartsCount += 1
    }
  }
  
  private var summaries: Map[Int, PlayerSummary] = Map.empty
  
  private var startTime = Long.MaxValue
  private var endTime = 0L
  
  private var _winner = "unknown"
  
  def forcefulInit(initialData: ChartDataPackage) = {
    // TODO this is still an issue, but let's not spam the logs
    // println("[FIX ME]I was forced to init!")
    
    for (entry <- initialData.playerTimeData) {
      val playerId = entry._1.toInt
      
      for (data <- entry._2) {
        addStats(playerId, data.timepoint, StatsReportData(data.armyCount, data.metalIncome, data.energyIncome, 
            data.metalSpending, data.energySpending, data.metalIncomeNet, data.energyIncomeNet, data.metalStored, 
            data.energyStored, data.metalProduced, data.energyProduced, data.metalWasted, data.energyWasted, data.apm, data.simSpeed))
      }
    }
    
    CookieBox withSession { db =>
      setWinner(GameInfoCollector(db, gameId).map(_.winner).getOrElse("unknown"))
    }
  }
  
  private def modifySummary(pId: Int, func: PlayerSummary => Unit) = {
    val summary = summaries.getOrElse(pId, new PlayerSummary(pId))
    func(summary)
    summaries += pId -> summary
  }
  
  def setWinner(winner: String) = this._winner = winner
  
  def addStats(playerId: Int, time: Long, data: StatsReportData) = {
    modifySummary(playerId, _.addStats(time, data))
    if (startTime > time) {
      startTime = time
    }
    endTime = time
  }
  
  def runTime = endTime - startTime
  def winner = _winner
  def getSummaries = summaries
}