package info.nanodesu.pages

import scala.xml.Attribute
import scala.xml.Null
import scala.xml.Text
import net.liftweb.http.S
import scala.util.Try
import net.liftweb.util.Helpers
import net.liftweb.common.Empty
import net.liftweb.common.Full

trait PageParam {
  def paramName: String
  def paramValue: String
}

trait Page {
  def pageName: String
  
  protected def makeLink0(text: String, p: PageParam*) = {
    <a>{text}</a> % makeLinkAttribute0(p:_*)
  }
  
  protected def makeLinkAttribute0(p: PageParam*) = {
    val fullLinkString = pageName + Page.makeLinkString(p:_*)
    Attribute(None, "href", Text(fullLinkString), Null)
  }
  
  protected def getParam[T](name: String, mapper: (String => T)) = {
	S.param(name) match {
	  case Full(v: String) => Helpers.tryo(mapper(v)) 
	  case _ => Empty
	}
  }
}

object Page {
  def makeLinkString(p: PageParam*): String = {
    val paramString = p.foldLeft("")((b, a) => (b match {
      case "" => ""
      case x: String => x + "&"
    }) + a.paramName + "=" + a.paramValue)
    paramString match {
      case "" => ""  
      case x: String => "?"+x
    }
  } 
  def makeLinkStringBase(p: (String, String)*): String = {
    val params = p map { x =>
      new PageParam {
        val paramName = x._1
        val paramValue = x._2
      }
    }
    makeLinkString(params:_*)
  }
  
  def getCurrentParamsUpdated(update: PageParam): String = {
    val initial = S.request.map(_.params).openOr(Map.empty)
    val updated = initial.updated(update.paramName, List(update.paramValue)).filterNot(_._2.isEmpty)
    val stringPairs = updated.map(x => (x._1, x._2.head)).toList
    makeLinkStringBase(stringPairs:_*)
  }
  
  def getCurrentParamsUpdated(updateName: String, updateValue: String): String = {
    val param = new PageParam {
      val paramName = updateName
      val paramValue = updateValue
    }
    getCurrentParamsUpdated(param)
  }
}