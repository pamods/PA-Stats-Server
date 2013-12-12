package info.nanodesu.comet

import net.liftweb.http.CometActor
import net.liftweb.util.Helpers
import net.liftweb.http.CometListener

abstract class GameComet extends CometActor with CometListener {
  def nameKey: String
  def getGameId = for (n <- name; gId <- Helpers.tryo(n.replaceAll(nameKey, "").toInt)) yield gId
  def isMyGame(id: Int) = id == getGameId.openOr(-1)
  def registerWith = GameCometServer
}