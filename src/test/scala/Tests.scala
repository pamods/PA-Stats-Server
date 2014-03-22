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
import java.sql.Date
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
import org.jooq.util.postgres.PostgresDataType
import java.math.{ BigDecimal => JBigDecimal }

case class Foobar(data: Map[Int, List[ChartDataPoint]])

case class Player(playerId: Int, playerName: String)
case class Team(teamId: Int, players: List[Player])
case class Game(gameId: Int, teams: List[Team], winner: Int)

object Tests extends App{
   import net.liftweb.json._
	import net.liftweb.json.JsonDSL._
    private implicit val formats = net.liftweb.json.DefaultFormats
  
//    val numA = 12.12121
//    println(((numA * 100).toInt / 100.0))
//    
//  val playerA = Player(410, "Cola_Colin")
//  val playerB = Player(1, "Illmaren")
//  val playerC = Player(122, "foobar")
//  val playerD = Player(12, "foo")
//  
//  val teamA = Team(0, List(playerA, playerB))
//  val teamB = Team(1, List(playerC, playerD))
//  
//  val game2v2 = Game(1, List(teamA, teamB), 0)
//  val game1v1 = Game(2, List(Team(0, List(playerA)), Team(1, List(playerB))), 0)
//  
//  println(pretty(render(List(Extraction decompose game2v2, Extraction decompose game1v1))))
  
//  val test = <span>haha</span><a href="test.com">HI</a>;
//  
//  val a = test \\ "a"
//  
//  println(a)
//  private implicit val formats = net.liftweb.json.DefaultFormats

  
  val startTime = System.currentTimeMillis(); 
  
  val c = DriverManager.getConnection("jdbc:postgresql://10.8.0.1/rbztest", "cookie", "ibda1!")
  try {
    val db = DSL.using(c, SQLDialect.POSTGRES)
//    		
    val baseDate = java.sql.Date.valueOf("2013-12-20")
    
    val startTime = BigInteger.valueOf(baseDate.getTime()/1000)
    val duration = 60 * 60
    
    println(startTime)
    println(duration)
//    
//    val f = stats.ARMY_COUNT
//
//	  val before3Hours = new Timestamp(System.currentTimeMillis() - 3 * 60 * 60 * 1000)
//	  val before3Min = new Timestamp(System.currentTimeMillis() - 3 * 60 * 1000)
//	  val minLength = BigInteger.valueOf(5 * 60)
//	  val before12Hours = new Timestamp(System.currentTimeMillis() - 12 * 60 * 60 * 1000)
//	  
    val q = db.select(
    		playerGameRels.ID.count(),
        	dayTrunk(games.START_TIME)). 
        from(playerGameRels).
    		join(games).onKey().
    		join(players).onKey().
        where(players.ID === 412).
        and(playerGameRels.LOCKED.isFalse()).
        	groupBy(dayTrunk(games.START_TIME)).
        	orderBy(dayTrunk(games.START_TIME))
//    
//	val resCollected: Field[_ <: Number] = stats.METAL_COLLECTED
//	val resWasted: Field[_ <: Number] = stats.METAL_WASTED
//	
//   val q2 = db.select(decode().
//      when(sum(resCollected) > 0, -(sum(resWasted).cast(PostgresDataType.NUMERIC) / sum(resCollected)) + (new JBigDecimal(1))).
//      otherwise(1), dayTrunk(games.START_TIME)).
//      from(stats).
//      join(playerGameRels).onKey().
//      join(games).onKey().
//      where(playerGameRels.LOCKED.isFalse()).and(playerGameRels.P === 412).
//          	groupBy(dayTrunk(games.START_TIME)).
//        	orderBy(dayTrunk(games.START_TIME))
//      
//    println(q2.fetch());
        	
//    println(q.fetch());
        
//	  println {
//      db.delete(stats).where(stats.PLAYER_GAME.in(
//	      select(playerGameRels.ID).
//	      from(playerGameRels).
//	      join(stats).onKey().
//	      join(games).onKey().
//	      where(games.END_TIME.gt(inline(before3Hours))).
//	      and(games.END_TIME.lt(inline(before3Min))).
//	      groupBy(playerGameRels.ID).
//	      having(count(stats.PLAYER_GAME).lt(inline(12:Integer)))
//	  )).getSQL()
//    }
//    
//    val q = db.select(intervalInSecs(max(stats.TIMEPOINT).sub(min(stats.TIMEPOINT)).mul(int2Num(1000))).as("diff"),
//            names.DISPLAY_NAME.as("name"),
//            players.ID.as("pid")).
//            from(stats).
//            join(playerGameRels).onKey().
//            join(games).onKey().
//            join(players).onKey().
//            join(names).onKey().
//            where(players.UBER_NAME.isNotNull()).
//            groupBy(games.ID, names.DISPLAY_NAME, players.ID)
    
//    val q = db.select(field("name"), field("pid"), field("count(diff)"), field("sum(diff) as t"), field("avg(diff) :: bigint")).
//        from( // this is SLOW (30s+), as it calculates a list of all games and their length for all players => improve this once it is too slow
//          db.select(
//            intervalInSecs(max(stats.TIMEPOINT).sub(min(stats.TIMEPOINT)).mul(int2Num(1000))).as("diff"),
//            names.DISPLAY_NAME.as("name"),
//            players.ID.as("pid")).
//            from(stats).
//            join(playerGameRels).onKey().
//            join(games).onKey().
//            join(players).onKey().
//            join(names).onKey().
//            where(players.UBER_NAME.isNotNull()).
//            groupBy(games.ID, names.DISPLAY_NAME, players.ID).asTable("foo")
//       ).
//       groupBy(field("name"), field("pid")).
//       orderBy(field("t").desc).
//       limit(10)
       
//      println(q.getSQL())
      
//    val fetched = q.fetch()
    
//      println(fetched)
      
//    val endTime = startTime.add(BigInteger.valueOf(duration))
//    
//    val result = db.select(teams.INGAME_ID, playerGameRels.P, names.DISPLAY_NAME, games.WINNER_TEAM, games.ID).
//         from(playerGameRels).
//         join(games).onKey().
//         join(stats).on(stats.PLAYER_GAME === playerGameRels.ID). // limit to rows that have some stats => reporters
//         join(players).onKey().
//         join(names).onKey().
//         join(teams).onKey().
//         where(games.WINNER_TEAM.isNotNull()).
//         and(epoch(games.START_TIME).gt(startTime)).
//         and(epoch(games.START_TIME).lt(endTime)).
//         groupBy(teams.INGAME_ID, playerGameRels.P, names.DISPLAY_NAME, games.WINNER_TEAM, games.ID).orderBy(games.ID).fetch()
//    
//   val lst = result.asScala.toList      
//   val byGames = lst.groupBy(_.getValue(games.ID))
//   
//   val mapped = byGames.map(x => {
//     val byTeam = x._2.groupBy(_.getValue(teams.INGAME_ID))
//     val teamsWithPlayers = byTeam.map(t => {
//       val playersInTeam = t._2.map(foo => Player(foo.getValue(playerGameRels.P), foo.getValue(names.DISPLAY_NAME)))
//       Team(t._1, playersInTeam)
//     }).toList
//     Game(x._1, teamsWithPlayers, 0) //x._2.head.getValue(games.WINNER_TEAM)
//   }).toList
   
//   println(result.formatCSV())
//   println(mapped)
//   println(pretty(render(Extraction decompose mapped)))
    
//    db.select().
//    	from(playerGameRels).
//    	join(stats).onKey().
//    	join(games).onKey().
//    	join(players).onKey().
//    	join(names).onKey().
//    	where(games.WINNER_TEAM.isNotNull()).
    	
    
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
//	      	and(intervalInSecs(games.END_TIME.sub(games.START_TIME)).lt(minLength)).fetch())
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
  
  println((System.currentTimeMillis() - startTime)/1000)
}