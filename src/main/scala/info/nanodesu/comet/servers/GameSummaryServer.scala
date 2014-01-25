package info.nanodesu.comet.servers

import info.nanodesu.lib.Formattings._
import info.nanodesu.model.StatsReportData
import info.nanodesu.model.StatsReportData
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.collectors.gameinfo.ChartDataCollector
import info.nanodesu.model.db.collectors.gameinfo.ChartDataPackage
import info.nanodesu.model.db.collectors.playerinfo.GamePlayerInfo

class GameSummaryServer(val gameId: Int) {

  class PlayerSummary extends GamePlayerInfo {
    // FIXME this currently is not initialized by forceful init
    private var _name: String = ""
    private var _primaryColor: String = "#000"
    private var _secondaryColor: String = "#000"
    
    private var startTime: Long = Long.MaxValue
    private var endTime: Long = 0
    private var metalProduced: Long = 0
    private var energyProduced: Long = 0
    private var metalWasted: Long = 0
    private var energyWasted: Long = 0
    private var actions: Int = 0
    private var buildSpeedSum: Double = 0
    private var buildSpeedPartsCount: Int = 0

    override def name = _name
    override def primaryColor = _primaryColor
    override def secondaryColor = _secondaryColor
    def apmAvg = {
      val minutes = ((runTime.toDouble + Double.MinPositiveValue) / 1000 / 60)
      Math.round(actions / minutes).toInt
    }
    def sumMetal = metalProduced
    def sumEnergy = energyProduced
    def metalUseAvg = 1 - (metalWasted.toDouble / (metalProduced + Double.MinPositiveValue))
    def energyUseAvg = 1 - (energyWasted.toDouble / (energyProduced + Double.MinPositiveValue))
    def buildSpeed = if (buildSpeedPartsCount == 0) 1 else buildSpeedSum / buildSpeedPartsCount
    def runTime = endTime - startTime

    def setPlayerInfo(name: String, primColor: String, secColor: String) = {
      this._name = name
      this._primaryColor = primColor
      this._secondaryColor = secColor
    }
    
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
  
  // FIXME add this to forceful init
  private var _winner = "unknown"
  
  def forcefulInit(initialData: ChartDataPackage) = {
    for (entry <- initialData.playerTimeData) {
      val playerId = entry._1.toInt
      
      for (data <- entry._2) {
        addStats(playerId, data.timepoint, StatsReportData(data.armyCount, data.metalIncome, data.energyIncome, 
            data.metalSpending, data.energySpending, data.metalIncomeNet, data.energyIncomeNet, data.metalStored, 
            data.energyStored, data.metalProduced, data.energyProduced, data.metalWasted, data.energyWasted, data.apm))
      }
    }
  }
  
  private def modifySummary(pId: Int, func: PlayerSummary => Unit) = {
    val summary = summaries.getOrElse(pId, new PlayerSummary)
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
  
  def setPlayerInfo(playerId: Int, name: String, primaryColor: String, secondaryColor: String) = {
    modifySummary(playerId, _.setPlayerInfo(name, primaryColor, secondaryColor))
  }
  
  def runTime = endTime - startTime
  def winner = _winner
  def getSummaries = summaries
}