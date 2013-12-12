package info.nanodesu.snippet.lib

import scala.xml._
import net.liftweb.util.Helpers
import net.liftweb.http.S
import info.nanodesu.pages.Page

trait PagedSnippet {
  /**
   * the name of the parameter that tells the snippet at which page it is
   */
  def currentPageParameter: String

  def offset = (currentPage - 1) * pageMaxSize
  /**
   *  return the page-number that is currently selected
   *  defaults to the integer value of the parameter given by currentPageParameter
   */
  def currentPage: Int = Helpers.tryo((for (x <- S.param(currentPageParameter)) yield Integer.valueOf(x): Int).openOrThrowException("tryo")) openOr 1
  /**
   * return how many elements exist over all pages together
   */
  def elementCount: Int
  /**
   * return how many elements should be displayed per page
   */
  def pageMaxSize: Int
  /**
   * return the parameter that shall be used for links to other pages
   */
  def linkParam(index: Int): (String, String) = (currentPageParameter, index.toString)
  /**
   * return the class that should be used to decorate the selected link
   */
  def selectedLinkAttr: String
  /**
   * return how many pages should be visible at max
   * always needs to be at least 3
   */
  def maxPagesVisible: Int = 30

  def dotsElem: Node = <span>...</span>
  def sepElem: Node = <span>, </span>
  private def sep(in: List[Node]) = {
    if (in.isEmpty) in
    else in.head :: (for (e <- in.drop(1)) yield List(sepElem, e)).flatten
  }

  private def checkPagesMaxCountValid() =
    if (maxPagesVisible < 3) throw new RuntimeException(s"maxPagesVisible was $maxPagesVisible but needs to be at least 3")

  private def makeLink(index: Int) = {
    val param = linkParam(index)
    val elem = <a>{ index }</a> %
      Attribute(None, "href", Text(Page.getCurrentParamsUpdated(param._1, param._2)), Null)
    if (index == currentPage) {
      elem % Attribute(None, "class", Text(selectedLinkAttr), Null)
    } else elem
  }

  def renderPages: NodeSeq = {
    checkPagesMaxCountValid()

    val pages = (elementCount / pageMaxSize) + 1
    if (pages <= 1) NodeSeq.Empty
    else {
      val middleIndex = currentPage
      val halfMiddleWidth = (maxPagesVisible - 3) / 2
      val middleElementsStart = Math.max(middleIndex - halfMiddleWidth, 2)
      val middleElementsEnd = Math.min(middleIndex + halfMiddleWidth, pages - 1)
      val middle = (for (i <- middleElementsStart to middleElementsEnd) yield {
        makeLink(i)
      }).toList
      val start = makeLink(1)
      val end = makeLink(pages)
      val hasDotsAtStart = middleElementsStart != 2
      val hasDotsAtEnd = middleElementsEnd != pages - 1

      val potentialDotsAtStart = if (hasDotsAtStart) List(dotsElem) else Nil
      val potentialDotsAtEnd = if (hasDotsAtEnd) List(dotsElem) else Nil

      val all = start :: potentialDotsAtStart ::: middle ++ potentialDotsAtEnd ++ List(end)
      sep(all)
    }
  }
}



























