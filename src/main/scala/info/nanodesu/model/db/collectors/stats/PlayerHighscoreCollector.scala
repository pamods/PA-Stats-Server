package info.nanodesu.model.db.collectors.stats

import info.nanodesu.lib.RefreshRunner
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.generated.Tables._
import collection.JavaConversions._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.db.CookieFunc._

// not used atm
class PlayerHighscoreCollector {
  val maxUnitsPlayer = PlayerHighscoreCollector.maxUnitsPlayer
  //val maxMetalPlayer = PlayerHighscoreCollector.maxMetalPlayer
  //val maxEnergyPlayer = PlayerHighscoreCollector.maxEnergyPlayer
}

case class PlayerHighscore(name: String, pid: Int, score: Int, game: Int)

object PlayerHighscoreCollector extends RefreshRunner {
  def apply() = new PlayerHighscoreCollector()

  override val firstLoadDelay = 60 * 1000 * 5
  override val RUN_INTERVAL = 1000 * 60 * 60 // once per hour, these are slow queries
  val processName = "worker: " + getClass().getName()

  private var maxUnitsPlayer: Option[PlayerHighscore] = None
  private var maxMetalPlayer: Option[PlayerHighscore] = None
  @volatile
  private var maxEnergyPlayer: Option[PlayerHighscore] = None

  def runQuery() = CookieBox withSession { db =>
    maxUnitsPlayer = Some(queryMaxScorePlayer(db, stats.ARMY_COUNT))
   // maxMetalPlayer = Some(queryMaxScorePlayer(db, stats.METAL_INCOME))
   // maxEnergyPlayer = Some(queryMaxScorePlayer(db, stats.ENERGY_INCOME))
  }

  // jooq is the most epic SQL library ever, 
  // in fact it's the only kind-of-ORM lib that actually feels useful to me
  private def queryMaxScorePlayer[T](db: DSLContext, f: Field[T]) = {
    // query does a seq scan on old timepoint stats to find the max
    // current execution time is ~10s
    db.select(commaListDistinctField(names.DISPLAY_NAME), players.ID, f, playerGameRels.G).
      from(stats).
      join(playerGameRels).onKey().
      join(players).onKey().
      join(names).onKey().
      where(f.equal(db.select(max(f)).from(stats))).
      and(players.UBER_NAME.isNotNull()).
      groupBy(f, playerGameRels.G, players.ID).
      orderBy(playerGameRels.G).limit(1).fetchOneInto(classOf[PlayerHighscore]) // mapping just works
  }
}