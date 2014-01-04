package info.nanodesu.comet

import net.liftweb.common.Full
import net.liftweb.http.js.JsCmd
import net.liftweb.json._
import net.liftweb.http.js.JE
import net.liftweb.common.Loggable

trait TriggerMessage extends JsCmd{
	implicit val formats = DefaultFormats
	
	def messageValues: List[Map[String, Any]]
	
	def json: JValue = Extraction decompose Map("value" -> messageValues)
	
	def triggerName: String
	
	override val toJsCmd = JE.JsRaw("""$(document).trigger('%s', %s);""".format(triggerName, compact(net.liftweb.json.render(json)))).toJsCmd
}

trait MapFlatteningTriggerMessage[T] extends TriggerMessage {
  def input: Map[String, List[T]]
  def map(key: String, t: T): Map[String, Any]
  
  def messageValues = {
    input.toList.map(x => {
      for (e <- x._2) yield {
        map(x._1, e)
      }
    }).flatten
  }
}