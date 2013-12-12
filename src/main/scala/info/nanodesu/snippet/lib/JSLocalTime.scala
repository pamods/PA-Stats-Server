package info.nanodesu.snippet.lib

import net.liftweb.http.DispatchSnippet
import net.liftweb.http.S
import net.liftweb.util.Helpers
import java.util.Date
import scala.xml.Attribute
import scala.xml.Text
import scala.xml.Null
import net.liftweb.util.Helpers._

object JSLocalTime extends DispatchSnippet {
  val dispatch: DispatchIt = {
    case "time" => injectTimeJsFor
  }

  def injectTimeJsFor = {
    val rnd = Helpers.nextFuncName

    "*" #> {
      val ms = S.attr("ms").map(java.lang.Long.valueOf(_): Long).openOr(System.currentTimeMillis())
      <span/> % Attribute(None, "id", Text(rnd), Null) ++
        <script type="text/javascript">
          $('#{ rnd }').html(new Date({ ms }).toLocaleString());
        </script>
    }
  }

  def jsTimeSnipFor(d: Date) = {
    <span/> % Attribute(None, "data-lift", Text("LocalTime.time?ms=" + d.getTime()), Null);
  }
}