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

@RunWith(classOf[JUnitRunner])
class GameTitleCollectorSpec extends Specification with Mockito with ScalaCheck with ThrownExpectations {
  private def nonEmptyString = Gen.alphaStr suchThat (!_.isEmpty)
  val teamPlayersGen = for {
    firstPlayerId <- Gen.choose(1, 1000000)
    playerIdInc <- Gen.choose(1, 200)
    playerCount <- Gen.choose(1, 10)
    playerWithIdCount <- Gen.choose(1, Math.max(2, playerCount-1))
    playerNames <- Gen.listOfN(playerCount, nonEmptyString)
  } yield {
    var playerId = firstPlayerId
    var playersWithId = 0
    for (c <- 0 until playerCount) yield {
      playersWithId += 1
      playerId += playerIdInc
      val playerIdOpt = if (playersWithId < playerWithIdCount) Some(playerId) 
      					else None
      (playerIdOpt, playerNames(c))
    }
  }
  val playerInfoForTitleGen = for {
    teamCount <- Gen.choose(1, 10)
    players <- Gen.listOfN(teamCount, teamPlayersGen)
  } yield {
    val seq = for (teamIndex <- 0 until teamCount;
    		player <- players(teamIndex)) yield {
      PlayerInfoForTitle(teamIndex, player._1, player._2)
    }
    seq.toList
  }
  implicit def arbPlayerInfo = Arbitrary(playerInfoForTitleGen)
  
  class GameTitleTest(val in: List[PlayerInfoForTitle], val gameId: Int = 12,
      val reporterCss: String = "bold", shouldCreateLinks: Boolean = false,
      val linkedPlayerCss: String = "playerlink", teamSepCss: String = "white") {
    def test(testFunc: (NodeSeq, GameTitleDbLayer) => MatchResult[_]) = {
      val db = mock[GameTitleDbLayer]
      db.selectPlayerInfoForGame(gameId) returns in
      val subject = new GameTitleCollector(db)
      subject.reportingPlayerCss = reporterCss
      subject.shouldCreateLinks = shouldCreateLinks
      subject.linkedPlayerCss = linkedPlayerCss
      subject.teamSepCss = teamSepCss
      val r = subject.createGameTitle(gameId)
      testFunc(r, db)
    }
  }
  
  "The GameTitleCollector" should {
    "call the select method to prove the tester works" ! prop { in: List[PlayerInfoForTitle] =>
      new GameTitleTest(in) test { (nodes: NodeSeq, db: GameTitleDbLayer) =>
        there was one(db).selectPlayerInfoForGame(anyInt)
      }
    }
    
    "mention all players" ! prop { in: List[PlayerInfoForTitle] =>
      new GameTitleTest(in) test { (nodes: NodeSeq, db: GameTitleDbLayer) =>
        val allPlayers = in.map(_.playerName)
        val text = nodes.text
        
        allPlayers.filterNot(text.contains(_)) must be empty
      }
    }
    
    def sepPlayers(in: NodeSeq) = in.text.split(",| vs |\\s").filterNot(_.isEmpty)
    def sepTeams(in: NodeSeq) = in.text.split(" vs ").filterNot(_.isEmpty)
    
    "seperate players with , or vs " ! prop { in: List[PlayerInfoForTitle] =>
      new GameTitleTest(in) test { (nodes: NodeSeq, db: GameTitleDbLayer) =>
        val allPlayers = in.map(_.playerName)
        val split = sepPlayers(nodes)
        allPlayers must containTheSameElementsAs(split)
      }
    }
    
    "order players by the teamindex" ! prop { in: List[PlayerInfoForTitle] =>
      new GameTitleTest(in.sortBy(x => x.playerName.length)) test { (nodes: NodeSeq, db: GameTitleDbLayer) =>
        val text = nodes.text
        def getIndexNear(name: String, cur: Int) = {
          in.find(x => x.playerName == name && x.teamId >= cur).map(_.teamId).getOrElse {
            throw new RuntimeException(s"The order was violated! name = $name and cur = $cur text = $text in = $in")
          }
        }
        var curIndex = 0
        
        val res = for (player <- sepPlayers(nodes)) yield {
        	val pIndex = getIndexNear(player, curIndex)
        	val tmp = curIndex
        	curIndex = pIndex
        	tmp must be_<=(pIndex)
        }
        res.reduce(_ and _)
      }
    }
    
    "split the correct number of teams with _vs_" ! prop { in: List[PlayerInfoForTitle] =>
      new GameTitleTest(in) test { (nodes: NodeSeq, db: GameTitleDbLayer) =>
        val teamCnt = in.map(_.teamId).toSet.size
        sepTeams(nodes).length must_== teamCnt
      }
    }
    
    "mark the vs with the correct class" ! prop { in: List[PlayerInfoForTitle] =>
      val teamCss = "teamCss"
      new GameTitleTest(in, teamSepCss = "teamCss") test { (nodes: NodeSeq, db: GameTitleDbLayer) =>
        val spans = nodes \\ "span"
        val vsSpans = spans.filter(_.text == " vs ")
        val matchers = for (span <- vsSpans) yield {
          ((span \ "@class").text must_== teamCss)
        }
        if (matchers.isEmpty) ok else matchers.reduce(_ and _)
      }
    }
    
    "link players with the correct id if required" ! prop { in: (Boolean, List[PlayerInfoForTitle]) =>
       new GameTitleTest(in._2, shouldCreateLinks = in._1) test { (nodes: NodeSeq, db: GameTitleDbLayer) =>
         val links = nodes \\ "a"

         val playerLinks = (for (node <- links) yield {
            ((node \ "@href").text.replaceAll("[^\\d]", "").toInt, (node.text))
         }).toMap
         
         val matchers = if (in._1) {
           for (playerInf <- in._2; id <- playerInf.playerId) yield {
        	   playerLinks(id) must_== playerInf.playerName
           }
         } else {
           (playerLinks must be empty) :: Nil
         }
         
         (matchers must be empty) or (matchers.reduce(_ and _))
       }
    }
    
    "use the correct css class for reporting players" ! prop { in: (Boolean, List[PlayerInfoForTitle]) =>
      val rCss = "reporter"
      val lCss = "linked"
      new GameTitleTest(in._2, shouldCreateLinks = in._1,
          reporterCss = rCss, linkedPlayerCss = lCss) test { (nodes: NodeSeq, db: GameTitleDbLayer) =>
        val reporters = in._2.map(_.playerId).filter(_.isDefined).size
        val cssMatches = StringUtils.countMatches((nodes \\ "@class").text, if (in._1) lCss else rCss)
        reporters must_== cssMatches
      }
    }
  }
}