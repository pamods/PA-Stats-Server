package info.nanodesu.pages

case class SearchParam(search: String) extends PageParam {
  val paramName = PlayerSearchPage.searchParam 
  val paramValue = search
}

object PlayerSearchPage extends Page {
	val pageName = "players"
	
	protected[pages] val searchParam = "search" // TODO this is copied in the html. That's bad
	 
	def makeLink(txt: String) = makeLink0(txt)
	def makeLink(txt: String, term: SearchParam) = makeLink0(txt, term)
	def makeLinkAttribute(term: SearchParam) = makeLinkAttribute0(term)
	
	def getSearchTerm = getParam(searchParam, x => x)
}