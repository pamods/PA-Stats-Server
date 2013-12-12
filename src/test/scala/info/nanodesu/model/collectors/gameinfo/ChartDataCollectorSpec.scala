package info.nanodesu.model.collectors.gameinfo

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock._
import java.sql.Timestamp
import org.apache.commons.lang.StringUtils
import org.specs2.matcher.ThrownExpectations
import org.specs2.ScalaCheck
import org.scalacheck.Gen
import info.nanodesu.model.db.collectors.gameinfo.PlayerInfoForTitle
import org.scalacheck.Arbitrary
import info.nanodesu.model.db.collectors.gameinfo.GameTitleDbLayer
import info.nanodesu.model.db.collectors.gameinfo.GameTitleCollector
import scala.xml.NodeSeq
import org.specs2.matcher.MatchResult
import java.util.Arrays
import info.nanodesu.model.db.collectors.gameinfo.ChartDataPoint
import org.scalacheck._
import org.scalacheck.Arbitrary._
import info.nanodesu.model.db.collectors.gameinfo._
import scala.util.Random

@RunWith(classOf[JUnitRunner])
class ChartDataCollectorSpec extends Specification with Mockito with ScalaCheck with ThrownExpectations {
  private def ix = arbitrary[Int]

  val genChartData: Gen[ChartDataPoint] = for {
    timepoint <- ix
    armyCount <- ix
    metalIncome <- ix
    energyIncome <- ix
    metalIncomeNet <- ix
    energyIncomeNet <- ix
    metalSpending <- ix
    energySpending <- ix
    metalStored <- ix
    energyStored <- ix
    metalProduced <- ix
    energyProduced <- ix
    metalWasted <- ix
    energyWasted <- ix
    apm <- ix
  } yield ChartDataPoint(timepoint, armyCount, metalIncome, energyIncome, metalIncomeNet, energyIncomeNet,
    metalSpending, energySpending, metalStored, energyStored, metalProduced, energyProduced, metalWasted,
    energyWasted, apm)

  var playerIdSeed = 0
  val genChartDataDbResult: Gen[List[ChartDataDbResult]] = for {
    playerInc <- Gen.choose(1, 3)
    playerName <- Gen.alphaStr suchThat (!_.isEmpty)
    data <- Gen.listOf(genChartData)
  } yield {
    playerIdSeed += playerInc
    for (d <- data) yield {
      ChartDataDbResult(playerIdSeed, playerName, playerName, d)
    }
  }
  
  class Subject(val game: Int, val dataIn: List[ChartDataDbResult]) {
    def result = {
	    val db = mock[ChartDataCollectorDblayer]
	    db.selectDataPointsForGame(game) returns Random.shuffle(dataIn)
	    new ChartDataCollector(db).collectDataFor(game)      
    }
    
    override def toString() = s"""
      game = $game
      dataIn = $dataIn
      """
  }
  
  val genSubject: Gen[Subject] = for {
    gameId <- ix
    players <- Gen.choose(1, 5)
    in <- Gen.listOfN(players, genChartDataDbResult)
  } yield new Subject(gameId, in.flatten)
  
  implicit val arbSubject = Arbitrary(genSubject)
  
  "The ChartDataCollector" should {
    "return the correct game id" ! prop { in: Subject =>
      in.result.gameId === in.game
    }
    
    "create the right number of player id to data mappings" ! prop { in: Subject =>
      val playerCount = in.dataIn.map(_.playerId).toSet.size
      in.result.playerTimeData.size === playerCount
    }
    
    "map the ids to the right names" ! prop { in: Subject => 
      val r = in.result
      val idToNameMap = in.dataIn.map(x => (x.playerId.toString, x.playerName)).toMap
      
      val matchers = for (e <- r.playerInfo) yield {
        val nameMustMatch = e._2.name ===  idToNameMap(e._1)
        
        nameMustMatch
      }
      
      if (matchers.isEmpty) ok
      else matchers.reduce(_ and _)
    }
    
    "sort the datapoints by time" ! prop { in: Subject =>
      val timestampList = in.result.playerTimeData
      val matchers = for (it <- timestampList) yield {
        val times = it._2.map(_.timepoint)
        times must beSorted
      }
      if (in.dataIn.isEmpty) ok 
      else matchers.reduce(_ and _)
    }
    
    "return the same amount of datapoints as went in" ! prop { in: Subject =>
      val numIn = in.dataIn.size
      val numOut = in.result.playerTimeData.map(_._2).flatten.size
      
      numIn === numOut
    }
  }
}