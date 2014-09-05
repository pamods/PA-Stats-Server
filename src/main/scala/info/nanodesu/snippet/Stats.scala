package info.nanodesu.snippet

import scala.Option.option2Iterable
import scala.xml.Attribute
import scala.xml.Null
import scala.xml.Text
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.collectors.stats._
import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Helpers.strToCssBindPromoter
import info.nanodesu.pages.PlayerPage
import info.nanodesu.pages.IdParam
import info.nanodesu.pages.GamePage
import info.nanodesu.pages.GameIdParam
import info.nanodesu.lib.Formattings
import info.nanodesu.rest.LadderServiceV2

object Stats extends DispatchSnippet {
  val dispatch: DispatchIt = {
    case "general" => doGeneral
    case "highscore" => doHighscores
    case "contributers" => doContributers
    case "technical" => doTechnical
  }

  private def doTechnical = {
    val data = CookieBox withSession (RuntimeInfoCollector(_))
    "#maxMemory" #> data.maxMemory &
    "#totalmem" #> data.totalMemory &
    "#freemem" #> data.freeMemory &
    "#cores" #> data.processorCount &
    "#uptime" #> data.upTime &
    "#tablessize" #> data.dataBaseSize &
    "#idlesql" #> data.idleConnections &
    "#busysql" #> data.busyConnections &
    "#allsql" #> data.poolConnections &
    "#gamecometcnt" #> data.activeGameComets
  }

  private def doContributers = "#line" #> MostPlaytimesCollector().topPlaytime.zipWithIndex.map(x => {
    ".placecnt *" #> PlayerPage.makeLink((x._2 + 1).toString, IdParam(x._1.pid)) &
      ".playernlst *" #> x._1.name &
      ".gamecnt *" #> x._1.gameCount &
      ".playtime *" #> Formattings.prettyTime(x._1.fullTime) &
      ".avgtime *" #> Formattings.prettyTime(x._1.avgTime) 
  })

  private def doHighscores = {
    val h = PlayerHighscoreCollector()
    
    def toPlayerLink (x: PlayerHighscore) = PlayerPage.makeLinkAttribute(IdParam(x.pid)).value
    def toGameLink (x: PlayerHighscore) = GamePage.makeLinkAttribute(GameIdParam(x.game)).value
    
    "#maxUnitsName *+" #> h.maxUnitsPlayer.map(_.name) &
    "#maxUnitsName [href]" #> h.maxUnitsPlayer.map(toPlayerLink) &
    "#maxUnitCount" #> h.maxUnitsPlayer.map(_.score) &
    "#maxUnitGame [href]" #> h.maxUnitsPlayer.map(toGameLink) &
    "#maxUnitGame *+" #> h.maxUnitsPlayer.map(_.game) //&
//    "#maxMetalName *+" #> h.maxMetalPlayer.map(_.name) &
//    "#maxMetalName [href]" #> h.maxMetalPlayer.map(toPlayerLink) &
//    "#maxMetalSum" #> h.maxMetalPlayer.map(_.score).map(Formattings.formatKMBT(_)) &
//    "#maxMetalGame [href]" #> h.maxMetalPlayer.map(toGameLink) &
//    "#maxMetalGame *+" #> h.maxMetalPlayer.map(_.game) &
//    "#maxEnergyName *+" #> h.maxEnergyPlayer.map(_.name) &
//    "#maxEnergyName [href]" #> h.maxEnergyPlayer.map(toPlayerLink) &
//    "#maxEnergySum" #> h.maxEnergyPlayer.map(_.score).map(Formattings.formatKMBT(_))  &
//    "#maxEnergyGame [href]" #> h.maxEnergyPlayer.map(toGameLink) &
//    "#maxEnergyGame *+" #> h.maxEnergyPlayer.map(_.game)
  }

  private def doGeneral = {
    val numbers = CookieBox withSession (ExtraNumbersCollector(_))
    "#gamecount" #> numbers.gameCount &
    "#datapointcnt -*" #> numbers.datapointsCount &
    "#avglen" #> numbers.avgGameTime &
    "#gamelength" #> numbers.sumGameTime &
    "#users" #> numbers.userCount &
    "#unitsCreated -*" #> numbers.unitsCreated &
    "#unitsDestroyed -*" #> numbers.unitsDestroyed &
    "#automatchcnt -*" #> LadderServiceV2.confirmedGames
  }
}