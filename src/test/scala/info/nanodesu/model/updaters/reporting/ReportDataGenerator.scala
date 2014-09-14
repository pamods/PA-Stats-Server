package info.nanodesu.model.updaters.reporting

import org.scalacheck.Gen
import info.nanodesu.model.ReportData
import org.scalacheck._
import org.scalacheck.Arbitrary._
import info.nanodesu.model._
import java.sql.Timestamp
import java.util.UUID

object ReportDataGenerators {
  private def ix = arbitrary[Int]
  private def dx = arbitrary[BigDecimal]
  private def nonEmptyString = Gen.alphaStr suchThat (!_.isEmpty)

  val genUuid: Gen[String] = for {
	  x <- Gen.choose(Int.MinValue, Int.MaxValue)
	  y <- Gen.choose(Int.MinValue, Int.MaxValue)
	  x2 <- Gen.choose(Int.MinValue, Int.MaxValue)
	  y2 <- Gen.choose(Int.MinValue, Int.MaxValue)
  } yield new UUID({
    // somebody someday needs to tell me why Gen.choose(Long.MinValue, Long.MaxValue) fails to work
    // also I wonder if this is a proper way to do this bit fiddeling. It has been a while since I last did this
    ((x:Long) << 32) | x2 
  }, {
    ((y:Long) << 32) | y2
  }).toString()
  
  val genTimestamp: Gen[Timestamp] = for {
    seed <- arbitrary[Long]
  } yield new Timestamp(seed)
  implicit val arbTimestamp = Arbitrary(genTimestamp)

  val genStatsReportData: Gen[StatsReportData] = for {
    armyCount <- ix
    metalIncome <- ix
    energyIncome <- ix
    metalSpending <- ix
    energySpending <- ix
    metalIncomeNet <- ix
    energyIncomeNet <- ix
    metalStored <- ix
    energyStored <- ix
    metalProducedSinceLastTick <- ix
    energyProducedSinceLastTick <- ix
    metalWastedSinceLastTick <- ix
    energyWastedSinceLastTick <- ix
    apm <- ix
  } yield StatsReportData(
    armyCount,
    metalIncome,
    energyIncome,
    metalSpending,
    energySpending,
    metalIncomeNet,
    energyIncomeNet,
    metalStored,
    energyStored,
    metalProducedSinceLastTick,
    energyProducedSinceLastTick,
    metalWastedSinceLastTick,
    energyWastedSinceLastTick,
    apm)
  implicit val arbStatsReport: Arbitrary[StatsReportData] = Arbitrary(genStatsReportData)
  
  val genArmyEvent: Gen[ArmyEvent] = for {
    spec <- nonEmptyString
    x <- ix
    y <- ix
    z <- ix
    planetId <- ix
    watchType <- ix
    time <- ix
  } yield ArmyEvent(spec, x, y, z, planetId, watchType, time)
  
  implicit val arbArmyEvent = Arbitrary(genArmyEvent)
  
  val genGameData = Gen.resultOf(RunningGameData.apply _)
  implicit val arbRunningGame: Arbitrary[RunningGameData] = Arbitrary(genGameData)

  val genPlanet: Gen[ReportedPlanet] = for {
    notReallyJson <- nonEmptyString
  } yield ReportedPlanet(notReallyJson)
  implicit val arbRepPlanet: Arbitrary[ReportedPlanet] = Arbitrary(genPlanet)

  val genPlayer: Gen[ReportPlayer] = for {
    name <- nonEmptyString
  } yield ReportPlayer(name)
  
  implicit val arbPlayer = Arbitrary(genPlayer)

  var teamIndexCounter = -1
  val genTeam = for {
    n <- Gen.choose(1, 5)
    primaryColor <- nonEmptyString
    secondarColor <- nonEmptyString
    players <- Gen.listOfN(n, genPlayer)
  } yield {
    teamIndexCounter += 1
    ReportTeam(teamIndexCounter, primaryColor, secondarColor, players)
  }

  implicit val arbTeam = Arbitrary(genTeam)

  // these reports are not really valid, as they dont have the
  // correct relation between reporter ubername and reported teams
  // doesnt matter for the tests though...
  val genReport: Gen[ReportData] = (for {
    ident <- genUuid // this value needs to be unique
    playerName <- genUuid
    playerUberName <- genUuid
    n <- Gen.choose(1, 4)
    teams <- Gen.listOfN(n, genTeam)
    s <- genStatsReportData
    v <- arbitrary[Int]
    planet <- genPlanet
    pv <- nonEmptyString
    live <- arbitrary[Boolean]
    events <- Gen.listOf1(genArmyEvent)
    time <- arbitrary[Long]
    automatch <- arbitrary[Boolean]
  } yield ReportData(ident, playerUberName, playerName, n-1, teams, live, s, v, planet, pv, events, time, automatch))

  implicit val arbReport = Arbitrary(genReport)

}