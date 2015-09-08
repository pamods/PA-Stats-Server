package info.nanodesu.model.db.collectors.stats

import info.nanodesu.lib.RefreshRunner
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.lib.db.CookieFunc._
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import java.sql.Date
import java.sql.Timestamp

class MostPlaytimesCollector {
  val topPlaytime = MostPlaytimesCollector.topPlaytime
}

case class PlayerPersonalTimes(name: String, pid: Int, gameCount: Int, fullTime: Long, avgTime: Long)

object MostPlaytimesCollector extends RefreshRunner {

  def apply() = new MostPlaytimesCollector()

  override val firstLoadDelay = 60 * 1000 * 1
  override val RUN_INTERVAL = 1000 * 60 * 60 * 6
  val processName = "worker: " + getClass().getName()

  @volatile
  private var topPlaytime: List[PlayerPersonalTimes] = Nil

  def runQuery() = {
    val lst = CookieBox withSession { db =>
      db.select(field("name"), field("pid"), field("count(diff)"), field("sum(diff) as t"), field("avg(diff) :: bigint")).
        from( // this is SLOW (30s+), as it calculates a list of all games and their length for all players => improve this once it is too slow
          db.select(
            intervalInSecs(max(stats.TIMEPOINT).sub(min(stats.TIMEPOINT)).mul(int2Num(1000))).as("diff"),
            names.DISPLAY_NAME.as("name"),
            players.ID.as("pid")).
            from(stats).
            join(playerGameRels).onKey().
            join(games).onKey().
            join(players).onKey().
            join(names).onKey().
            where(players.UBER_NAME.isNotNull()).
            and(games.END_TIME.compare(Comparator.GREATER, new Timestamp(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000))).
            groupBy(games.ID, names.DISPLAY_NAME, players.ID).asTable("foo")
       ).
       groupBy(field("name"), field("pid")).
       orderBy(field("t").desc).
       limit(10).
       fetchInto(classOf[PlayerPersonalTimes])
    }
    topPlaytime = lst.asScala.toList
  }
}