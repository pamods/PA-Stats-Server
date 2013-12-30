package info.nanodesu.comet

import net.liftweb.http.CometActor
import net.liftweb.util.Helpers
import net.liftweb.http.CometListener
import net.liftweb.common.Full
import net.liftweb.common.Box
import net.liftweb.common.Loggable
import net.liftweb.util.Helpers._
import scala.language.postfixOps

abstract class GameComet extends CometActor with CometListener with Loggable {
  def nameKey: String
  protected var cachedGameId: Box[Int] = null
  def getGameId = {
    if (cachedGameId == null) {
      cachedGameId = for (n <- name; gId <- Helpers.tryo(n.replaceAll(nameKey, "").toInt)) yield gId
    }
    cachedGameId
  }
  def isMyGame(id: Int) = {
    GameCometServer.myGameChecksCounter.incrementAndGet()
    id == getGameId.getOrElse(-1)
  }
  def registerWith = GameCometServer

  override protected def localSetup() = {
    super.localSetup()
    GameCometServer.cometCounter.incrementAndGet()
  }

  override protected def localShutdown() = {
    super.localShutdown()
    GameCometServer.cometCounter.decrementAndGet()
  }
  
  override def lifespan = Full(30 seconds)
}