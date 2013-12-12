import java.util.concurrent.ConcurrentHashMap
import info.nanodesu.model.ReportData
import java.util.Date
import org.jooq.DSLContext
import info.nanodesu.model.db.collectors.gameinfo.loader.GameIdFromIdentLoader
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._
import net.liftweb.util.Props
import java.sql.Timestamp
import info.nanodesu.model.ReportedPlanet
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.ReportTeam
import info.nanodesu.model.ReportedPlanet
import info.nanodesu.model.ReportTeam
import org.apache.commons.lang.StringUtils
import java.sql.DriverManager
import info.nanodesu.model.db.collectors.gameinfo.ChartDataPoint
import info.nanodesu.model.db.collectors.gameinfo.ChartDataDbResult
import net.liftweb.json.Extraction
import info.nanodesu.model.db.collectors.gameinfo.ChartDataCollector
import java.util.Date
import info.nanodesu.model.ReportData
import info.nanodesu.model.RunningGameData
import net.liftweb._
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.json.Extraction
import net.liftweb.json.JsonAST.JValue
import net.liftweb.util.Helpers._
import net.liftweb.common.Box
import net.liftweb.util.Helpers
import info.nanodesu.comet.GameCometServer
import info.nanodesu.comet.GameDataUpdate
import info.nanodesu.model.RunningGameDataC
import info.nanodesu.model.ReportDataC
import info.nanodesu.model.db.updaters.reporting.initial.InitialReportAcceptor
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.updaters.reporting.running.RunningGameStatsReporter
import info.nanodesu.pages.GamePage
import info.nanodesu.lib.db.CookieFunc._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import java.sql.Timestamp
import info.nanodesu.model.db.collectors.gameinfo.ChartDataCollector
import info.nanodesu.model.db.collectors.gameinfo.ChartDataPackage
import java.math.BigInteger

case class Foobar(data: Map[Int, List[ChartDataPoint]])

case class Player(playerId: Int, playerName: String)
case class Team(teamId: Int, players: List[Player])
case class Game(gameId: Int, teams: List[Team], winner: Int)

object Tests extends App{
   import net.liftweb.json._
	import net.liftweb.json.JsonDSL._
    private implicit val formats = net.liftweb.json.DefaultFormats
  
  val playerA = Player(410, "Cola_Colin")
  val playerB = Player(1, "Illmaren")
  val playerC = Player(122, "foobar")
    
  val teamA = Team(0, List(playerA, playerB))
  val teamB = Team(1, List(playerC))
  
  val game = Game(1, List(teamA, teamB), 0)
  
  println(pretty(render(List(Extraction decompose game, Extraction decompose game))))
  
//  val test = <span>haha</span><a href="test.com">HI</a>;
//  
//  val a = test \\ "a"
//  
//  println(a)
//  private implicit val formats = net.liftweb.json.DefaultFormats

  
  
  val c = DriverManager.getConnection("jdbc:postgresql://10.8.0.1/rbztest", "xxx", "xxx")
  try {
    val db = DSL.using(c, SQLDialect.POSTGRES)
//    		
//    val gameId = 8310
//    
//    val data = ChartDataCollector(db).collectDataFor(gameId)
//    
//    val minGameMinutes = 1
//    
//    	  val before3Hours = new Timestamp(System.currentTimeMillis() - 3 * 60 * 60 * 1000)
//	  val before3Min = new Timestamp(System.currentTimeMillis() - 3 * 60 * 1000)
//	  val minLength = BigInteger.valueOf(minGameMinutes * 60)
//	  
//	  println(db.select(games.ID).
//	      	from(games).
//	      	where(games.END_TIME.gt(before3Hours)).
//	      	and(games.END_TIME.le(before3Min)).
//	      	and(epoch(games.END_TIME.sub(games.START_TIME)).lt(minLength)).fetch())
//	  
    	  
    
    
        
//    val r = db.select(playerGameRels.ID).
//      	from(playerGameRels).
//      	join(players).onKey().
//      	join(names).onKey().
//      	join(teams).onKey().
//      	join(relStats).on(relStats.ID === playerGameRels.ID).
//      	where(names.DISPLAY_NAME === displayName).
//      	and(teams.INGAME_ID === teamIndex).
//      	and(playerGameRels.G === gameId).
//      	and(relStats.REPORTED.isFalse()).
//     fetchOne().getValue(0, classOf[Int])
     
//     println(r)
  } finally {
    c.close()
  }
}