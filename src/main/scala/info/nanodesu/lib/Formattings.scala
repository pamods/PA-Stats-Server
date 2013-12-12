package info.nanodesu.lib

import java.text.MessageFormat
import net.liftweb.util.Props

/**
 * Formatting helpers
 */
object Formattings {
  
  def formatGameDuration(start: Long, end: Long) = {
    val liveGameThreshold = Props.getInt("liveGameThreshold").openOr(15000)
    val probablyStillActive = System.currentTimeMillis() - end < liveGameThreshold
    prettyTimespan(end - start) + (if (probablyStillActive) "+" else "")
  }
  
  def transformPercent(in: Double) = f(in, "%")
  
  def formatUnit(in: Double, unit: String) = f(in, unit)
  
  private def f(in: Double, unit: String) = {
    MessageFormat.format("{0,number,#.##" + unit + "}", in: java.lang.Double)
  }
  
  def formatKMBT(n: Long) = {
    Math.abs(n) match {
      case x if (x >= 1E12) => f(n / 1E12, " T")
      case x if (x >= 1E9) => f(n / 1E9, " B")
      case x if (x >= 1E6) => f(n / 1E6, " M")
      case x if (x >= 1E3) => f(n / 1E3, " K")
      case _ => f(n, "")
    }
  }

  def prettySize(n: Long) = {
    Math.abs(n) match {
      case x if (x >= 1125899906842624L) => f(n / 1125899906842624D, " PiB")
      case x if (x >= 1099511627776L) => f(n / 1099511627776D, " TiB")
      case x if (x >= 1073741824L) => f(n / 1073741824D, " GiB")
      case x if (x >= 1048576) => f(n / 1048576D, " MiB")
      case x if (x >= 1024) => f(n / 1024D, " KiB")
      case _ => f(n, "B")
    }
  }
  
 def prettyTimespan(l: Long) = {
    val fullSec = l / 1000;
    val min = fullSec / 60;
    val sec = fullSec % 60;

    val minStr = min + "";
    val secStr = sec + "";
    val minStr0 = if (minStr.length() < 2) "0" + minStr else minStr
    val minSec0 = if (secStr.length() < 2) "0" + secStr else secStr
    minStr0 + ":" + minSec0
  }
 
  def prettyTime(in: Long) = {
	  var diff = in
	  val secondInMillis = 1000;
	  val minuteInMillis = secondInMillis * 60
	  val hourInMillis = minuteInMillis * 60
	  val dayInMillis = hourInMillis * 24
	  
	  val timeNames = Array("d", "h", "m", "s")
	  val times = new Array[Long](4)
	  
	  times(0) = diff / dayInMillis
	  diff = diff % dayInMillis
	  times(1) = diff / hourInMillis
	  diff = diff % hourInMillis
	  times(2) = diff / minuteInMillis
	  diff = diff % minuteInMillis
	  times(3) = diff / secondInMillis
	  
	  var result = ""
	  for (x <- 0 until times.length if (times(x) != 0)) {
	    result += times(x) + timeNames(x) + " ";
	  }
	  
	  if (result.isEmpty()) "0s" else result.trim()
	}
}