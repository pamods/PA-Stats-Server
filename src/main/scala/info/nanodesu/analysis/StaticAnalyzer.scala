package info.nanodesu.analysis

import java.sql.DriverManager
import scala.collection.JavaConverters._
import org.jooq.SQLDialect
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import info.nanodesu.lib.db.CookieFunc._
import org.jooq.DSLContext
import org.jooq.scala.Conversions._
import java.sql.Date
import info.nanodesu.lib.Formattings
import net.liftweb.json._
import java.sql.Timestamp
import java.math.BigInteger

class StaticAnalyzer(db: DSLContext) {

  def listPatches: List[String] = {
    db.selectDistinct(games.PA_VERSION).from(games).fetch().asScala.map(_.value1()).toList.sorted
  }

  def uniqueUsers(patch: String): Int = {
    db.select(players.ID.countDistinct()).
      from(games).
      join(playerGameRels).onKey().
      join(players).on(players.ID === playerGameRels.P).
      where(players.UBER_NAME.isNotNull()).
      and(games.PA_VERSION === patch).fetchOne().value1()
  }

  def uniqueGames(patch: String, minLengthInMin: Int): Int = {
    db.selectCount().
      from(games).
      where(games.PA_VERSION === patch).
      and(epoch(games.END_TIME.sub(games.START_TIME)).ge(BigInteger.valueOf(minLengthInMin*60))).
      fetchOne().value1()
  }

  def timeSpan(patch: String): (Date, Date) = {
    val r = db.select(games.START_TIME.min(), games.START_TIME.max()).
      from(games).
      where(games.PA_VERSION === patch).
      fetchOne()
    (new Date(r.value1().getTime()), new Date(r.value2().getTime()))
  }

  def avgGameTime(patch: String): Long = {
    db.select(avg(epoch(games.END_TIME.sub(games.START_TIME)).mul(int2JBigD(1000)))).
      from(games).
      where(games.PA_VERSION === patch).
      fetchOne(0, classOf[Long])
  }

  def analyzeUnitUsage(patch: String, minTimeInMin: Int, maxTimeInMin: Int): List[(String, Int)] = {
    val q = db.select(specKeys.SPEC, armyEvents.ID.count()).
      from(games).
      join(playerGameRels).onKey().
      join(armyEvents).on(armyEvents.PLAYER_GAME === playerGameRels.ID).
      join(specKeys).onKey().
      where(armyEvents.WATCHTYPE === 0).
      and(games.PA_VERSION === patch).
      and(epoch(armyEvents.TIMEPOINT.sub(games.START_TIME)).gt(BigInteger.valueOf(minTimeInMin*60))).
      and(epoch(armyEvents.TIMEPOINT.sub(games.START_TIME)).lt(BigInteger.valueOf(maxTimeInMin*60))).
      groupBy(specKeys.SPEC).orderBy(armyEvents.ID.count().desc()).fetch()

    val lines = for (line <- q.asScala) yield (line.value1(), line.value2(): Int)
    lines.toList
  }
}

object StaticAnalyzer {
  def main(args: Array[String]) {
    val url = "jdbc:postgresql://127.0.0.1/rbztest"
    val user = "postgres"
    val pass = "roxyrox1"
    val c = DriverManager.getConnection(url, user, pass)
    try {
      val db = DSL.using(c, SQLDialect.POSTGRES)
      val worker = new StaticAnalyzer(db)

      val patches = worker.listPatches.filterNot(_ == "unknown").reverse
      println(patches.size + " known patches:\n" + patches.mkString("\n") + "\n")

      println("Version;Start;End;Exact patch livetime;games;users;avg games per hour;avg game time")
      for (patch <- patches) {
        val timespan = worker.timeSpan(patch)
        val timeDiff = (timespan._2.getTime() - timespan._1.getTime())
        val timeDiffInHours = timeDiff / 1000 / 3600
        val gameCnt = worker.uniqueGames(patch, 0)
        val usersCnt = worker.uniqueUsers(patch)

        println(s"$patch;${timespan._1};${timespan._2};${Formattings.prettyTime(timeDiff)};$gameCnt;$usersCnt;${gameCnt.toDouble / timeDiffInHours};" +
          s"${Formattings.prettyTime(worker.avgGameTime(patch))}")

        //	      println("data for PA v."+patch + " from " + timespan._1 + " to " + timespan._2 + ", or to be exact for " + Formattings.prettyTime(timeDiff))
        //	      println(s"$gameCnt Games from ${worker.uniqueUsers(patch)} Users for this patch.")
        //	      println("That means during this patch PA Stats saw "+(gameCnt.toDouble / timeDiffInHours)+" games per hour")
      }

      println("\n\n\n")

      def printForTimePart(start: Int, end: Int) = {
    	println("for start = "+start+"min and end = " + end + "min")
        println("Version/Unit;avg build per game")
        for (patch <- patches) {
          val unitsUsage = worker.analyzeUnitUsage(patch, start, end)
          val gamesCnt = worker.uniqueGames(patch, end)

          if (unitsUsage.nonEmpty) {
            for (uu <- unitsUsage if uu._2.toDouble / gamesCnt >= 1) {
              println(patch + ";" + nameMap(uu._1) + ";" + uu._2.toDouble / gamesCnt)
            }
          }
        }
    	println("\n\n\n")
      }

      for (start <- (0 to 50) if start % 10 == 0) {
        printForTimePart(start, start+10)
      }
      
    } finally {
      if (c != null) c.close()
    }
  }

  implicit val formats = DefaultFormats
  val nameMap = parse("""{"/pa/units/air/base_flyer/base_flyer.json":"Base Flyer","/pa/units/air/air_scout/air_scout.json":"Firefly","/pa/units/commanders/base_commander/base_commander.json":"Base Commander","/pa/units/land/assault_bot_adv/assault_bot_adv.json":"Slammer","/pa/units/land/assault_bot/assault_bot.json":"Dox","/pa/units/land/amphibious_bot/amphibious_bot.json":"Dox","/pa/units/land/base_bot/base_bot.json":"Base Bot","/pa/units/land/air_defense/air_defense.json":"Missile Defense Tower","/pa/units/air/air_factory/air_factory.json":"Air Factory","/pa/units/land/air_defense_adv/air_defense_adv.json":"Flak Cannon","/pa/units/land/artillery_short/artillery_short.json":"Pelter","/pa/units/air/air_factory_adv/air_factory_adv.json":"Advanced Air Factory","/pa/units/land/base_structure/base_structure.json":"Base Structure","/pa/units/land/artillery_long/artillery_long.json":"Holkins","/pa/units/land/anti_nuke_launcher/anti_nuke_launcher.json":"Anti-Nuke Launcher","/pa/units/land/avatar_factory/avatar_factory.json":"Avatar Factory","/pa/units/land/aa_missile_vehicle/aa_missile_vehicle.json":"Spinner","/pa/units/land/base_vehicle/base_vehicle.json":"Base Vehicle","/pa/units/land/land_mine/land_mine.json":"Land Mine","/pa/units/orbital/base_orbital/base_orbital.json":"Base Orbital","/pa/units/air/bomber/bomber.json":"Bumblebee","/pa/units/air/bomber_adv/bomber_adv.json":"Hornet","/pa/units/air/fighter_adv/fighter_adv.json":"Peregrine","/pa/units/air/fighter/fighter.json":"Hummingbird","/pa/units/air/fabrication_aircraft/fabrication_aircraft.json":"Fabrication Aircraft","/pa/units/commanders/avatar/avatar.json":"Avatar Commander","/pa/units/air/bomber_torpedo/bomber_torpedo.json":"Albatross","/pa/units/air/fabrication_aircraft_adv/fabrication_aircraft_adv.json":"Advanced Fab Aircraft","/pa/units/commanders/quad_base/quad_base.json":"Quadruped Class Commander","/pa/units/commanders/raptor_base/raptor_base.json":"Raptor Class Commander","/pa/units/commanders/tank_base/tank_base.json":"Tank Class Commander","/pa/units/commanders/imperial_delta/imperial_delta.json":"Delta","/pa/units/commanders/imperial_alpha/imperial_alpha.json":"Alpha","/pa/units/sea/naval_factory/naval_factory.json":"Naval Factory","/pa/units/land/metal_storage/metal_storage.json":"Metal Storage","/pa/units/sea/torpedo_launcher_adv/torpedo_launcher_adv.json":"Advanced Torpedo Launcher","/pa/units/land/radar_adv/radar_adv.json":"Advanced Radar","/pa/units/sea/sonar/sonar.json":"Sonar","/pa/units/orbital/deep_space_radar/deep_space_radar.json":"Orbital and Deepspace Radar","/pa/units/land/energy_storage/energy_storage.json":"Energy Storage","/pa/units/orbital/ion_defense/ion_defense.json":"Umbrella","/pa/units/land/metal_extractor_adv/metal_extractor_adv.json":"Advanced Metal Extractor","/pa/units/sea/sea_mine/sea_mine.json":"Jellyfish","/pa/units/land/nuke_launcher/nuke_launcher.json":"Nuclear Missile Launcher","/pa/units/land/tactical_missile_launcher/tactical_missile_launcher.json":"Catapult","/pa/units/land/metal_extractor/metal_extractor.json":"Metal Extractor","/pa/units/land/bot_factory/bot_factory.json":"Bot Factory","/pa/units/land/energy_plant/energy_plant.json":"Energy Plant","/pa/units/land/base_unit/base_unit.json":"Do Not Ever Inherit Me","/pa/units/sea/sonar_adv/sonar_adv.json":"Advanced Sonar","/pa/units/sea/torpedo_launcher/torpedo_launcher.json":"Torpedo Launcher","/pa/units/land/laser_defense_single/laser_defense_single.json":"Single Laser Defense Tower","/pa/units/land/vehicle_factory_adv/vehicle_factory_adv.json":"Advanced Vehicle Factory","/pa/units/land/teleporter/teleporter.json":"Teleporter","/pa/units/orbital/orbital_launcher/orbital_launcher.json":"Orbital Launcher","/pa/units/land/vehicle_factory/vehicle_factory.json":"Vehicle Factory","/pa/units/land/unit_cannon/unit_cannon.json":"Unit Cannon","/pa/units/land/laser_defense_adv/laser_defense_adv.json":"Advanced Laser Defense Tower","/pa/units/land/radar/radar.json":"Radar","/pa/units/land/energy_plant_adv/energy_plant_adv.json":"Advanced Energy Plant","/pa/units/sea/naval_factory_adv/naval_factory_adv.json":"Advanced Naval Factory","/pa/units/land/land_barrier/land_barrier.json":"Wall","/pa/units/land/laser_defense/laser_defense.json":"Laser Defense Tower","/pa/units/orbital/delta_v_engine/delta_v_engine.json":"Halley","/pa/units/land/bot_factory_adv/bot_factory_adv.json":"Advanced Bot Factory","/pa/units/land/tank_heavy_armor/tank_heavy_armor.json":"Vanguard","/pa/units/land/tank_armor/tank_armor.json":"Inferno","/pa/units/land/land_scout/land_scout.json":"Skitter","/pa/units/land/fabrication_vehicle_adv/fabrication_vehicle_adv.json":"Advanced Fabrication Vehicle","/pa/units/land/tank_light_laser/tank_light_laser.json":"Pounder","/pa/units/land/tank_amphibious_adv/tank_amphibious_adv.json":"Flux","/pa/units/land/tank_hover/tank_hover.json":"Ant","/pa/units/land/tank_laser_adv/tank_laser_adv.json":"Leveler","/pa/units/land/fabrication_vehicle/fabrication_vehicle.json":"Fabrication Vehicle","/pa/units/land/tank_heavy_mortar/tank_heavy_mortar.json":"Sheller","/pa/units/land/bot_aa/bot_aa.json":"Stinger","/pa/units/land/bot_bomb/bot_bomb.json":"Advanced Fabrication Bot","/pa/units/land/fabrication_bot_combat/fabrication_bot_combat.json":"Fabrication Bot Combat","/pa/units/land/bot_spider_adv/bot_spider_adv.json":"Recluse","/pa/units/land/fabrication_bot/fabrication_bot.json":"Fabrication Bot","/pa/units/land/bot_artillery_adv/bot_artillery_adv.json":"Gil-E","/pa/units/land/fabrication_bot_combat_adv/fabrication_bot_combat_adv.json":"Advanced Combat Fabrication Bot","/pa/units/land/fabrication_bot_adv/fabrication_bot_adv.json":"Advanced Fabrication Bot","/pa/units/orbital/orbital_laser/orbital_laser.json":"SXX-1304 Laser Platform","/pa/units/orbital/orbital_fabrication_bot/orbital_fabrication_bot.json":"Orbital Fabrication Bot","/pa/units/orbital/orbital_fighter/orbital_fighter.json":"Avenger","/pa/units/orbital/radar_satellite_adv/radar_satellite_adv.json":"Advanced Radar Satellite","/pa/units/orbital/orbital_gas_mine/orbital_gas_mine.json":"Orbital Gas Mine","/pa/units/orbital/defense_satellite/defense_satellite.json":"Anchor","/pa/units/orbital/radar_satellite/radar_satellite.json":"Radar Satellite","/pa/units/orbital/orbital_lander/orbital_lander.json":"Astraeus","/pa/units/orbital/orbital_egg/orbital_egg.json":"The Egg","/pa/units/orbital/solar_array/solar_array.json":"Solar Array","/pa/units/orbital/mining_platform/mining_platform.json":"Mining Platform"}""").
    asInstanceOf[JObject].values.withDefault(x => x)
}