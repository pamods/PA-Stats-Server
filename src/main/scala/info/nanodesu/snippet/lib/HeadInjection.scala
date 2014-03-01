package info.nanodesu.snippet.lib

import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Props
import scala.xml.Attribute
import scala.xml.Text
import scala.xml.Null
import scala.xml.NodeSeq
import scala.Array.canBuildFrom
import net.liftweb.util.Helpers._

object HeadInjection extends DispatchSnippet {
	val dispatch: DispatchIt = {
	  case "chartpage" => injectChartPageQueryUrl 
	  case "imports" => doImports
	  case "imgbase" => injectImgBaseUrl
	  case "banner" => doBanner
	}
	 
	def injectChartPageQueryUrl = "*" #> <script data-lift="head" type="text/javascript"> 
		var queryUrl = "{Props.get("queryUrl") openOrThrowException "you need to configure queryUrl in the props!" }";
		var gameIsLiveOffsetGuess = {Props.getInt("liveGameThreshold").openOr(15000)};
	</script>

		
   def injectImgBaseUrl = "*" #> <script data-lift="head" type="text/javascript">var imageBaseUrl = "{Props.get("imagebase", "error")}";</script>		

   def doBanner = "*" #> {
     <img alt="PA Stats Banner"></img> % Attribute(None, "src", Text(Props.get("imagebase", "error")+"banner.png"), Null)
   }
   
   private val jslibs = resolveProperty("jslibs") split " "
   private val csslibs = resolveProperty("csslibs") split " "
   
   private def resolveProperty(x: String) = Props.get(x) openOrThrowException("you need to configure the property " + x)
   
   def doImports = "*" #> {
     val jsimports = jslibs.map(x => (<script data-lift="head" type="text/javascript"> </script> 
         % Attribute(None, "src", Text(resolveProperty(x)), Null))).foldRight(NodeSeq.Empty)((a,b) => a ++ b)
         
     val cssimports = csslibs.map(x => (<link data-lift="head" type="text/css" rel="stylesheet"> </link>
         % Attribute(None, "href", Text(resolveProperty(x)), Null))).foldRight(NodeSeq.Empty)((a,b) => a ++ b)
       
     cssimports ++ jsimports
   }
}