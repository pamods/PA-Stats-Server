package info.nanodesu.comet

import net.liftweb.http.CometActor
import net.liftweb.util.Helpers
import net.liftweb.http.CometListener
import net.liftweb.common.Full
import net.liftweb.common.Box
import net.liftweb.common.Loggable
import net.liftweb.util.Helpers._
import scala.language.postfixOps
import net.liftweb.http.ShutdownIfPastLifespan
import net.liftweb.util.CssSel

abstract class ServerGameComet extends CometActor with Loggable {
  def nameKey: String
  protected var cachedGameId: Box[Int] = null
  def getGameId = {
    if (cachedGameId == null) {
      cachedGameId = for (n <- name; gId <- Helpers.tryo(n.replaceAll(nameKey, "").toInt)) yield gId
    }
    cachedGameId
  }

  protected var gameServer: Option[GameServerActor] = None

  private var shouldShutDown = false

  override def lifespan = if (shouldShutDown) Full(0) else super.lifespan

  protected def prepareShutdown()
  protected def pushDataToClients(server: GameServerActor)

  override def lowPriority = {
    case ServerShutdown => {
      prepareShutdown()
      shouldShutDown = true
      this ! ShutdownIfPastLifespan
    }

    case RegisterAcknowledged(server) => {
      gameServer = Some(server)
      pushDataToClients(server)
    }

    case PushUpdate(f) => for (server <- gameServer) pushDataToClients(server)
  }

  def render = {
    for (server <- gameServer) {
      this ! PushUpdate(true)
    }
    coreRender
  }

  protected def coreRender: CssSel

  override protected def localSetup() = {
    super.localSetup()
    GameServers.cometCounter.incrementAndGet()
    for (id <- getGameId) {
      GameServers.serverForGame(id) ! RegisterCometActor(this)
    }
  }

  override protected def localShutdown() = {
    GameServers.cometCounter.decrementAndGet()
    prepareShutdown()
    for (id <- getGameId) {
      GameServers.serverForGame(id) ! UnregisterCometActor(this)
    }
    super.localShutdown()
  }
}