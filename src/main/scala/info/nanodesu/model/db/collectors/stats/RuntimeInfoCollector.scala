package info.nanodesu.model.db.collectors.stats

import collection.JavaConversions._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import net.liftweb.util.Props
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.lib.db.CookieFunc
import bootstrap.liftweb.Boot

class RuntimeInfoCollector(db: DSLContext) {
  import RuntimeInfoCollector._

  val maxMemory = prettySize(r.maxMemory())
  val totalMemory = prettySize(r.totalMemory())
  val freeMemory = prettySize(r.freeMemory())
  val processorCount = r.availableProcessors()
  val upTime = prettyTime(System.currentTimeMillis() - startTime)
  val dataBaseSize = prettySize(selectDataBaseSize(db))
  val idleConnections = CookieBox.getNumIdleConnections
  val poolConnections = CookieBox.getNumPoolConnections
  val busyConnections = CookieBox.getNumBusyConnections
}

object RuntimeInfoCollector {
  def apply(db: DSLContext) = new RuntimeInfoCollector(db)

  val startTime = System.currentTimeMillis()

  private val r = Runtime.getRuntime()
  
  def selectDataBaseSize(db: DSLContext) = {
    val dbName = Props.get("dbName").openOr(throw new RuntimeException("dbName needed!"))
    db.select(CookieFunc.pgDataBaseSize(dbName)).fetchOne(0, classOf[Long])
  }
}