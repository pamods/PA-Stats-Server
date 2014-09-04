import java.io.File
import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Extraction._

object UnitJsonConverter {
  implicit val formats = DefaultFormats // Brings in default date formats etc.
  
  val factor = 8
  
  val inputRoot = new File("E:\\Games\\PA\\Planetary Annihilation\\PTE\\media\\pa\\units")
  val outRoot = new File("E:\\units\\unitsx"+factor)

  def main(args: Array[String]) {
    processFile(inputRoot)
  }
  
  def processFile(f: File): Unit = {
    if (f.getPath().endsWith(".json")) {
      processJson(f);
    } else if (f.isDirectory()) {
      val ffs = f.listFiles();
      if (ffs != null) {
        for (x <- ffs) {
          processFile(x)
        }
      }
    }
  }

  def processJson(jsonFile: File) = {
    val text = readFile(jsonFile)
    val input = parse(text).extract[Map[String, JValue]]
    val output = modifyJson(input)
    val outText = pretty(render(decompose(output)))
    val outFile = new File(jsonFile.getAbsolutePath().replace(inputRoot.getAbsolutePath(), outRoot.getAbsolutePath()))
    outFile.getParentFile().mkdirs()
    printToFile(outFile)(p => {
      p.print(outText)
    })
  }

  def modifyJson(in: Map[String, JValue]): Map[String, JValue] = {
    if (in.isDefinedAt("max_health")) {
      in("max_health") match {
        case JInt(h) =>
          in.updated("max_health", JInt(h*factor))
        case _ =>
          in
      }
    } else {
      in
    }
  }
  
  def readFile(f: File): String = {
    val source = scala.io.Source.fromFile(f)
    try {
      source.getLines mkString "\n"
    } finally {
      source.close()
    }
  }

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }
}