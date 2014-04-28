package info.nanodesu.pages

case class ReplayIdParam(id: String) extends PageParam {
  val paramName = ReplayPage.idParam
  val paramValue = id
}

object ReplayPage extends Page {
	val pageName = "replay"
	  
	protected[pages] val idParam = "replayid"
	    
	def makeLink(txt: String, id: ReplayIdParam) = makeLink0(txt, id)
	def makeLinkAttribute(id: ReplayIdParam) = makeLinkAttribute0(id)
	
	def getReplayId = getParam(idParam, x => x)
}