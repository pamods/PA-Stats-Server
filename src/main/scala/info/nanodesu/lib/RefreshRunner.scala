package info.nanodesu.lib

import net.liftweb.common._
import scala.concurrent.ops._
import net.liftweb.http.LiftRules

trait RefreshRunner extends Loggable {
  protected def RUN_INTERVAL = 1000 * 3600 * 24
  protected def RETRY_TIME = 1000 * 600

  @volatile
  private var initExecuted = false
  @volatile
  private var thread: Box[Thread] = Empty
  @volatile
  private var containerIsAlive = true
  @volatile
  private var lastUpdate: Long = Long.MinValue

  def init() = {
    if (initExecuted) {
      throw new IllegalStateException("already initialized!")
    }
    initExecuted = true
    initLoad()

    spawn { // this was written for scala 2.9 => investigate why this is now deprecated?
      // just replacing this with a Future instead breaks something in the jndi lookup for the datasource ?! :(
      thread = Full(Thread.currentThread())
      Thread.currentThread().setName(processName)
      logger.info("started " + processName)
      Thread.sleep(firstLoadDelay)
      doWork()
      logger.info(processName + " shutdown!")
    }

    LiftRules.unloadHooks.append(() => { killReloading() })
  }

  def shouldLog = true
  
  private def doWork() = {
    while (containerIsAlive) {
      if (lastUpdate + RUN_INTERVAL < System.currentTimeMillis()) {
        try {
          if (shouldLog) {
            logger info "starting task "+processName
          }
          runQuery()
          if (shouldLog) {
            logger info "completed task "+processName
          }
        } catch {
          case th: Throwable =>
            {
              logger.error("Exception while running query for task: " + processName + ". " +
                "Will retry in " + (RETRY_TIME / 1000 / 60) + " minutes !", th)

              try {
                Thread.sleep(RETRY_TIME)
              } catch {
                case e: InterruptedException =>
              }
            }
        }
        lastUpdate = System.currentTimeMillis()
      }
      try {
        Thread.sleep(RUN_INTERVAL)
      } catch {
        case e: InterruptedException =>
      }         
    }
  }

  def firstLoadDelay: Int = 0
  def processName: String
  def runQuery(): Unit

  def initLoad(): Unit = {  
  }
  
  def killReloading() = {
    containerIsAlive = false
    for (t <- thread) {
      t.interrupt()
    }
  }

  def reloadNow() = {
    lastUpdate = Long.MinValue
    thread.openOr(throw new RuntimeException("missing refresh thread. cannot reload " + processName)).interrupt()
  }
}