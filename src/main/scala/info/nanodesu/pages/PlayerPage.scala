package info.nanodesu.pages

case class IdParam(id: Int) extends PageParam {
  val paramName = PlayerPage.idParam
  val paramValue = id.toString
}

object PlayerPage extends Page {
  val pageName = "player"
		  
  protected[pages] val idParam = "player"  
    
  def makeLink(txt: String, id: IdParam) = makeLink0(txt, id)
  def makeLinkAttribute(id: IdParam) = makeLinkAttribute0(id)
  
  def getPlayerId = getParam(idParam, Integer.parseInt)
}