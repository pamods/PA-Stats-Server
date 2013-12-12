package info.nanodesu.snippet.lib

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mock._
import java.sql.Timestamp
import org.apache.commons.lang.StringUtils
import org.specs2.matcher.ThrownExpectations
import org.specs2.ScalaCheck
import org.scalacheck._
import scala.xml._
import org.specs2.matcher.MatchResult
import java.util.Arrays
import java.util.regex.Pattern

class TestSubject(val curPage: Int,
  val elemCnt: Int, val maxPageSize: Int, val maxPagesM: Int,
  val linkTargetBase: String, val selectedClass: String) extends PagedSnippet {
  def currentPageParameter = linkTargetBase
  override def currentPage = curPage
  def elementCount = elemCnt
  def pageMaxSize = maxPageSize
  def selectedLinkAttr: String = selectedClass
  override def maxPagesVisible = maxPagesM

  override def toString() = {
    s"""
    	currentPage = $currentPage,
        elementCount = $elementCount,
        pageMaxSize = $pageMaxSize,
        maxPagesVisible = $maxPagesM,
        linkTargetBase = $linkTargetBase,
        selectedClass = $selectedClass
        """
  }
}

@RunWith(classOf[JUnitRunner])
class PagedSnippetSpec extends Specification with Mockito with ScalaCheck with ThrownExpectations {

  val testCaseGen = for {
    elemCnt <- Gen.choose(0, 10000)
    maxPageSize <- Gen.choose(10, 100)
    curPage <- Gen.choose(1, elemCnt / maxPageSize)
    maxMiddle <- Gen.choose(3, 15)
    linkTarget <- Gen.alphaStr
    selectedClass <- Gen.alphaStr
  } yield new TestSubject(curPage, elemCnt, maxPageSize, maxMiddle, linkTarget, selectedClass)

  implicit val arbTestSubject = Arbitrary(testCaseGen)

  def isInteger(txt: String) = txt must be matching "[\\d]+"

  "The pages XML" should {

    "produce an empty NodeSeq for elements < maxPageSize" in {
      val subject = new TestSubject(1, 10, 30, 10, "", "")
      subject.renderPages must_== NodeSeq.Empty

      val subject2 = new TestSubject(1, 0, 30, 10, "", "")
      subject2.renderPages must_== NodeSeq.Empty

      val subject3 = new TestSubject(1, 40, 30, 10, "", "")
      subject3.renderPages must_!= NodeSeq.Empty
    }

    def splitToText(subject: TestSubject) = {
      val nodes = subject.renderPages
      val sep = Pattern.quote(subject.sepElem.text)
      val dots = Pattern.quote(subject.dotsElem.text)
      val sepRegex = s"$sep|$dots"
      nodes.text.split(sepRegex).filterNot(_.isEmpty)
    }

    "be build of separated ordered integers" ! prop { subject: TestSubject =>
      val split = splitToText(subject)
      val numberMatchers = for (item <- split) yield isInteger(item)
      val nums = for (item <- split) yield Integer.parseInt(item)
      val mustBeSorted = nums.toList must be sorted

      val mustBeNumbers = if (numberMatchers.isEmpty) ok
      else numberMatchers.reduce(_ and _)
      mustBeNumbers and mustBeSorted
    }

    "have no duplicates" ! prop { subject: TestSubject =>
      val nums = splitToText(subject).map(_.toInt)
      nums.toSet.size === nums.size
    }

    "begin and end with an integer" ! prop { subject: TestSubject =>
      val text = subject.renderPages.text

      def firstIsNum = isInteger(text.substring(0, 1))
      def lastIsNum = isInteger(text.substring(text.length - 1, text.length))
      (text must be empty) or (firstIsNum and lastIsNum)
    }

    "have offset >= 0 and < elementCount" ! prop { subject: TestSubject =>
      val offset = subject.offset
      val elements = subject.elementCount
      if (elements == 0) ok
      else (offset must be_>=(0)) and (offset must be_<(elements))
    }

    "never have more than maxPagesVisible entries" ! prop { subject: TestSubject =>
      val split = splitToText(subject)
      split.size must be_<=(subject.maxPagesVisible)
    }

    "contain the active page either at the start, the end or in the middle" ! prop {
      subject: TestSubject =>
        val split = splitToText(subject)
        val startNum = split(0).toInt
        val endNum = split(split.length - 1).toInt

        val middle = if (split.length > 2) split.slice(1, split.length - 1) else Array[String]()

        // if the selected page is near the end or the start it cannot be in the middle area of the middle
        val isNearBorders = subject.curPage - 1 < (subject.maxPagesVisible - 2) ||
          subject.elementCount / subject.maxPageSize - subject.curPage < (subject.maxPagesVisible - 2)

        val middleMiddle = (if (isNearBorders) middle else {
          val middleMiddleStart = Math.max(0, middle.length / 2 - 1)
          val middleMiddleEnd = Math.min(middle.length, middle.length / 2 + 2)
          middle.slice(middleMiddleStart, middleMiddleEnd)
        }).map(_.toInt)

        val candidates = startNum :: middleMiddle.toList ++ List(endNum)

        candidates must contain(subject.curPage)
    }

    "have a correct link for the pages" ! prop { subject: TestSubject =>
      val nodes = subject.renderPages

      val checks = for (node <- nodes if node.label == "a") yield {
        val href = node \ "@href"
        if (node.text != subject.dotsElem.text) {
          val num = node.text
          val containNum = href.text must contain(num)
          val containLinkParam = href.text must contain(subject.linkTargetBase)
          containNum and containLinkParam
        } else ok
      }

      if (checks.isEmpty) ok else checks.reduce(_ and _)
    }

    "mark the selected page" ! prop { subject: TestSubject =>
      val nodes = subject.renderPages

      val checks = for (node <- nodes if node.text == subject.currentPage.toString) yield {
        val clazz = node \ "@class"
        clazz.text === subject.selectedLinkAttr
      }

      val oneHit = checks.size === 1
      val correctClass = checks.head

      oneHit and correctClass
    }

    // TODO this test is bad, rewrite it
    "insert dots between jumps in the numbers" ! prop { subject: TestSubject =>
      if (subject.currentPage == 1) {
        ok
      } else {
        val pages = (subject.elementCount / subject.pageMaxSize) + 1
        val middleIndex = subject.currentPage
        val halfMiddleWidth = (subject.maxPagesVisible - 3) / 2
        val middleElementsStart = Math.max(middleIndex - halfMiddleWidth, 2)
        val middleElementsEnd = Math.min(middleIndex + halfMiddleWidth, pages - 1)
        val hasDotsAtStart = middleElementsStart != 2
        val hasDotsAtEnd = middleElementsEnd != pages - 1

        val nodes = subject.renderPages
        
        val checkAtStart = if (hasDotsAtStart && nodes.size > 2) nodes(2) === subject.dotsElem else nodes(2) !== subject.dotsElem
        val checkAtEnd = if (hasDotsAtEnd && nodes.size - 3 > 0) nodes(nodes.size - 3) === subject.dotsElem else nodes(nodes.size - 3) !== subject.dotsElem

        checkAtStart and checkAtEnd
      }
    }
  }
}