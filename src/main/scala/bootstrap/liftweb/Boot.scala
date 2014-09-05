package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._
import common._
import http._
import sitemap._
import Loc._
import net.liftweb.http.js.jquery._
import info.nanodesu.rest.StatisticsReportService
import info.nanodesu.snippet.ListGames
import info.nanodesu.lib.SmallGamesCleaner
import net.liftweb.common.Loggable
import net.liftweb.http.provider.HTTPParam
import info.nanodesu.snippet.GameInfo
import info.nanodesu.snippet.Stats
import info.nanodesu.snippet.PlayerInfo
import info.nanodesu.comet.PlayerGameInfo
import info.nanodesu.snippet.PlayerSearch
import info.nanodesu.model.db.collectors.stats.PlayerHighscoreCollector
import info.nanodesu.model.db.collectors.stats.MostPlaytimesCollector
import info.nanodesu.snippet.lib.JSLocalTime
import info.nanodesu.snippet.lib.CometInit
import info.nanodesu.snippet.lib.HeadInjection
import info.nanodesu.pages.PlayerPage
import info.nanodesu.pages.PlayerSearchPage
import info.nanodesu.pages.GamePage
import info.nanodesu.pages.StatsPage
import info.nanodesu.lib.db.CookieBox
import info.nanodesu.model.db.collectors.stats.RuntimeInfoCollector
import info.nanodesu.model.db.collectors.stats.ExtraNumbersCollector
import info.nanodesu.snippet.lib.IFrameSnip
import info.nanodesu.rest.LadderService
import info.nanodesu.snippet.Replay
import info.nanodesu.pages.ReplayPage
import info.nanodesu.rest.LadderServiceV2
import info.nanodesu.lib.RatingsMachine

// see: http://cookbook.liftweb.net/#InstallAndRunning
/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 * // When you deploy your application, pass -Drun.mode=production to your web container. In production mode, Lift aggressively caches templates, snippet classes, etc.
 */

class Boot extends Loggable {
	
  def boot {
    // in production this is done by the apache in front of the jetty
    if (Props.getBool("allowStarOrigin", false)) {
	    LiftRules.supplimentalHeaders = s => s.addHeaders(
	      List(HTTPParam("X-Lift-Version", LiftRules.liftVersion),
	        HTTPParam("Access-Control-Allow-Origin", "*"),
	        HTTPParam("Access-Control-Allow-Credentials", "true"),
	        HTTPParam("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS"),
	        HTTPParam("Access-Control-Allow-Headers", "WWW-Authenticate,Keep-Alive,User-Agent,X-Requested-With,Cache-Control,Content-Type")))
    } 
    
    // no need for jmx access to c3p0
    System.getProperties().setProperty("com.mchange.v2.c3p0.management.ManagementCoordinator", "com.mchange.v2.c3p0.management.NullManagementCoordinator")
    
    logger info "starttime = "+RuntimeInfoCollector.startTime
    
    LiftRules.addToPackages("info.nanodesu")
    
    LiftRules.snippetDispatch.append {
      case "LocalTime" => JSLocalTime
      case "ListGames" => ListGames
      case "HeadInjection" => HeadInjection
      case "GameInfo" => GameInfo
      case "Stats" => Stats
      case "PlayerInfo" => PlayerInfo
      case "CometInit" => CometInit
      case "PlayerSearch" => PlayerSearch
      case "IFrameSnip" => IFrameSnip
      case "Replay" => Replay
    }

    CookieBox.init()
    StatisticsReportService.init()
    SmallGamesCleaner.init()
    PlayerHighscoreCollector.init()
    MostPlaytimesCollector.init()
    ExtraNumbersCollector.init()
    LadderServiceV2.initService()
    RatingsMachine.init()
    
    // Build SiteMap
    val entries = List(
      Menu.i("Info") / "index",
      Menu.i("Installation") / "download",
      Menu.i("Updates") / "updates",
      Menu.i("Games") / "listgames",
      Menu.i("Players") / PlayerSearchPage.pageName,
      Menu(Loc("Units", ExtLink("http://www.nanodesu.info/pa-db/"), "Units")),
      Menu(Loc("Ladder", ExtLink("http://ladder.pastats.tk/"), "Ladder")),
      Menu.i("Extra") / StatsPage.pageName,
      Menu.i("Player") / PlayerPage.pageName >> Hidden,
      Menu.i("Chart") / GamePage.pageName >> Hidden,
      Menu.i("Ingamechart") / "ingamechart" >> Hidden,
      Menu.i("Replay") / ReplayPage.pageName >> Hidden)

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(entries: _*))

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))
      
    initRewrites()
      
    logger info "BOOT complete! run.mode=" + System.getProperty("run.mode")
    logger info "min comet interval is " + Props.getInt("minCometInterval")
    logger info "comet serve threshold is " + Props.getInt("cometServeThreshold")
  }
  
  def initRewrites() = {
    LiftRules.statelessRewrite.prepend(NamedPF("Replay Rewrite") {
      case RewriteRequest(ParsePath("replay" :: id :: Nil, _, _, _), _, _) =>
          RewriteResponse("replay" :: Nil, Map("replayid" -> id))
    })
  }
}
