import java.io.File
import scala.concurrent._
import java.util.concurrent.Executors
import sun.misc.IOUtils
import java.io.InputStreamReader
import java.io.BufferedReader
import scala.util.Try
import scala.util.Success
import scala.util.Failure

object Exe {
  val threadPool = Executors.newCachedThreadPool()
  implicit val ec = new ExecutionContext {
    def execute(runnable: Runnable) {
      threadPool.submit(runnable)
    }

    def reportFailure(t: Throwable) {
      //t.printStackTrace()
    }
  }
}

import Exe.ec

object LoadTester {
  val phantomjs = "D:\\Proggen\\phantomjs-1.9.2-windows\\phantomjs.exe"
  val workdir = "C:\\Eclipse\\PA-Stats-Server\\src\\test\\js"
  val simGameScript = "SimulateGame.js"

  val incStepTime = 1500;
  val stepJitter = 350;

  var reporters = 0;
  var failed = false;

  def main(args: Array[String]) {
    val testFailed = Promise[Boolean]
    def foobar(in: Try[String]): Unit = in match {
      case Success(cf) => {
        println("received msg: "+cf)
        if (cf == "timeout") {
          println(s"received timeout at $reporters reporters!")
          failed = true
        } else if (cf.startsWith("time")) {
          println(cf.split(";")(1))
        } else if (cf == "ended") {
          GameSimProc start (testFailed) onComplete foobar
          println("replaced some game!")
        } else {
          println(s"received unknown message $cf")
          failed = true
        }
      }
      case Failure(t) =>
        {
          t.printStackTrace()
          failed = true
        }
        println("running " + reporters)
    }

    while (!failed) {
      if (reporters < 1) {
        reporters += 1
        GameSimProc start (testFailed) onComplete foobar
        println("running " + reporters)
      }
      if (System.in.available() != 0) {
        failed = true;
      }
      Thread.sleep((incStepTime + Math.random() * stepJitter).toInt)
    }
    testFailed.success(true)
    Exe.threadPool.shutdown()
  }
}

object GameSimProc {
  def start(killer: Promise[Boolean]) = {
    val p = Promise[String]

    Future {
      p.complete(Try {
        val builder = new ProcessBuilder(LoadTester.phantomjs, LoadTester.simGameScript)
        builder.directory(new File(LoadTester.workdir));
        val process = builder.start()
        killer.future.onSuccess {
          case r =>
            process.destroy()
        }
        val reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
        reader.readLine()
      });
    }

    p.future
  }
}