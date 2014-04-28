package info.nanodesu.model.db.collectors.gameinfo

import scala.xml.NodeSeq
import scala.xml.Text
import scala.xml.Elem
import scala.xml.Node
import scala.xml.Attribute
import scala.xml.Null
import info.nanodesu.pages.PlayerPage
import info.nanodesu.pages.IdParam
import collection.JavaConversions._
import scala.collection.JavaConverters._
import org.jooq._
import org.jooq.impl._
import org.jooq.impl.DSL._
import org.jooq.scala.Conversions._
import info.nanodesu.lib.Formattings._
import info.nanodesu.lib.db.CookieFunc._
import net.liftweb.util.Props
import net.liftweb.common.Loggable

/**
 * Creates a NodeSeq that can be used as a game's title.
 * Reporting players can either be links to their
 * own page or they can just be given some extra css class.
 */
class GameTitleCollector(dbLayer: GameTitleDbLayer) {
  val playerSep = <span>, </span>
  val teamSep = <span> vs </span>

  var shouldCreateLinks: Boolean = false
  var reportingPlayerCss: String = "bold"
  var linkedPlayerCss: String = "playerlink"
  var teamSepCss: String = "teamsep"
    
  def createGameTitle(gameId: Int): NodeSeq = {
    val players = dbLayer.selectPlayerInfoForGame(gameId).sortBy(x => (x.teamId, x.playerId))
    if (players.isEmpty) {
      NodeSeq.Empty
    } else {
      
      def setClass(e: Elem, clazz: String) = e % Attribute(None, "class", Text(clazz), Null)
      
      def markupPlayer(player: PlayerInfoForTitle): Elem = {
        def baseElem = <span>{ player.playerName }</span>
        
        player.playerId match {
          case Some(pId) =>
            if (shouldCreateLinks) {
              val elem = <a>{ player.playerName }</a>
              val classyElem = setClass(elem, linkedPlayerCss)
              classyElem % PlayerPage.makeLinkAttribute(IdParam(pId))
            } else {
              setClass(baseElem, reportingPlayerCss)
            }
          case None =>
            baseElem
        }
      }
      
      def linkElemsWith(elems: List[NodeSeq], linker: Elem): NodeSeq = {
        (elems.head :: (for (e <- elems.drop(1)) yield {
          linker ++ e
        })).flatten.flatten
      }
      
      val groupedByTeam = players.groupBy(_.teamId)
      val teamNodeSeqs = for (x <- groupedByTeam.keySet.toList.sorted) yield {
        val playerElements = groupedByTeam(x).map(markupPlayer).map(x => x ++ NodeSeq.Empty)
        linkElemsWith(playerElements, playerSep)
      }
      linkElemsWith(teamNodeSeqs, setClass(teamSep, teamSepCss))
    }
  }
}

case class PlayerInfoForTitle(teamId: Int, playerId: Option[Int], playerName: String)
trait GameTitleDbLayer {
  def selectPlayerInfoForGame(gameId: Int): List[PlayerInfoForTitle]
}

object GameTitleCollector extends Loggable {
  def apply(db: DSLContext, shouldCreateLinks: Boolean = false) = {
    val r = new GameTitleCollector(new DbLayer(db))
    r.shouldCreateLinks = shouldCreateLinks
    r
   }
  
  private class DbLayer(db: DSLContext) extends GameTitleDbLayer {
    def selectPlayerInfoForGame(gameId: Int): List[PlayerInfoForTitle] = {
      val query = db.select(teams.INGAME_ID, playerGameRels.P, names.DISPLAY_NAME, stats.ID.count()).
         from(playerGameRels).
         leftOuterJoin(stats).onKey().
         join(players).onKey().
         join(names).onKey().
         join(teams).onKey().
         where(playerGameRels.G === gameId).
         groupBy(teams.INGAME_ID, playerGameRels.P, names.DISPLAY_NAME)
      
      query.fetch().asScala.toList.map(x => {
        PlayerInfoForTitle(x.value1(), if (x.value4() > 0) Some(x.value2()) else None, x.value3())
      })
    }
  }
}