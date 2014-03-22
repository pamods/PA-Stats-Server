package info.nanodesu.snippet.lib

import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Props
import scala.xml.Attribute
import scala.xml.Text
import scala.xml.Null
import scala.xml.NodeSeq
import scala.Array.canBuildFrom
import net.liftweb.util.Helpers._
import net.liftweb.http.S

object IFrameSnip extends DispatchSnippet {
	val dispatch: DispatchIt = {
	  case "frame" => injectFrame 
	}
	
	def injectFrame = {
	  val src = S.attr("src").getOrElse("")+"?r="+Math.random()
	  val id = S.attr("id").getOrElse("")
	  val link = <a>Your browser does not support iframes. Click here to show the content</a>
	  "*" #> {
	    val pure = <iframe witdh="100%" height="100%">{link % Attribute(None, "href", Text(src), Null)}</iframe>
	    val withSrc = pure % Attribute(None, "src", Text(src), Null)
	    val seamless = withSrc % Attribute(None, "seamless", Text("true"), Null)
	    val withId = seamless % Attribute(None, "id", Text(id), Null)
	    withId
	  }
	}
}