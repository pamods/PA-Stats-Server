package info.nanodesu.lib.db

import net.liftweb.util.Props
import com.mchange.v2.c3p0.ComboPooledDataSource
import javax.naming.InitialContext
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import java.sql.Date
import net.liftweb.http.LiftRules
import java.sql.Connection
import org.jooq.util.postgres.information_schema.tables.Routines
import java.sql.Timestamp
import java.math.BigInteger
import javax.sql.DataSource
import net.liftweb.common.Empty
import net.liftweb.common.Full
import com.mchange.v2.c3p0.PooledDataSource
import net.liftweb.common.Loggable
import info.nanodesu.generated.Sequences
/**
 * object offers access to the database
 * cause databases are just like boxes full of cookies
 */
object CookieBox extends Loggable {
  private val dialect = SQLDialect.POSTGRES
  
  def init() {
    // force initialization of the connection pool asap
    withSession(db => db.select(field("1")).fetchOne())
    
    LiftRules.unloadHooks.append{ () =>
      getConnectionPool match {
        case pool: PooledDataSource => {
          pool.close()
          logger.info("closed c3p0 connection pool")
        }
        case _ => logger.warn("could not find a c3p0 connection pool to close!")
      }
    }
  }
   
  private def getConnectionPool() = {
    val db = Props.get("dbName", "DB NOT FOUND")
    InitialContext.doLookup("java:comp/env/jdbc/"+db).asInstanceOf[DataSource]  
  }

  private def getC3P0Pool = getConnectionPool match {
    case c: ComboPooledDataSource => Full(c) 
    case _ => Empty
  }
  
  def getNumPoolConnections() = getC3P0Pool.map(_.getNumConnections())
  def getNumIdleConnections() = getC3P0Pool.map(_.getNumIdleConnections())
  def getNumBusyConnections() = getC3P0Pool.map(_.getNumBusyConnections())
 
  def withSession[T](f: DSLContext => T) = f(DSL.using(getConnectionPool, dialect))
   
  def withTransaction[T](f: DSLContext => T) = {
	  var c: Connection = null
	  try {
	    c = getConnectionPool.getConnection()
	    c.setAutoCommit(false)
	    val r = f(DSL.using(c, dialect))
	    c.commit()
	    r
	  } catch {
	    case th: Throwable => {
	      if (c != null) c.rollback()
	      throw th
	    }
	  } finally {
	    if (c != null) c.close()
	  }
  }
}

object CookieFunc {
  import _root_.scala.language.implicitConversions
  
  val JTRUE = java.lang.Boolean.TRUE
  val JFALSE = java.lang.Boolean.FALSE
  
  // implicit class magic
  implicit class MagicalResultQuery[T <: Record](val s: ResultQuery[T]) extends AnyVal{
    def fetchOneIntoOption[X](clazz: Class[X]): Option[X] = Option(s.fetchOneInto(clazz))
    def fetchFirstPrimitiveIntoOption[X](clazz: Class[X]): Option[X] = {
      val o = s.fetchOne(0)
      if (o == null) None
      else Some(o.asInstanceOf[X])
    }
  }
  
  // conversion helpers
  implicit def int2Num(i: Int): Number = new java.math.BigDecimal(i)
  implicit def int2JBigD(i: Int): java.math.BigDecimal = new java.math.BigDecimal(i)
  
  // table aliases (no real SQL aliases, as they are a bit buggy and don't really do anything helpful for us)
  import info.nanodesu.generated.Tables._
  val games = V2_GAME
  val players = V2_PLAYER
  val names = V2_PLAYER_DISPLAY_NAME
  val playerGameRels = V2_PLAYER_GAME_REL
  val stats = V2_TIMEPOINT_STATS
  val historyNames = V2_DISPLAY_NAME_HISTORY
  val planets = V2_PLANET
  val settings = V2_SETTINGS
  val teams = V2_TEAMS
  val teamIdSeq = Sequences.TEAM_ID
  val armyEvents = V2_ARMY_EVENTS
  val specKeys = V2_SPEC_KEYS
  
  // lifted functions
  def pgDataBaseSize(db: String) = DSL.field("pg_database_size({0})", classOf[Long], DSL.inline(db))
  def commaListDistinctField(f: Field[String]) = DSL.field("array_to_string(array_agg(distinct {0}), ', ')", classOf[String], f)
  def epoch(f: Field[Timestamp]) = DSL.field("date_part('epoch', {0})", classOf[BigInteger], f)
  def dayTrunk(f: Field[Timestamp]) = DSL.field("date_trunc('day', {0})", classOf[Timestamp], f)
}

