package beeb.rpn

import java.util.Timer
import java.util.TimerTask

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.language.implicitConversions
import scala.util.Try
import scala.util.Try

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.json4s.DefaultFormats
import org.json4s.Formats
import org.scalatra._
import org.scalatra.json._
import org.scalatra.servlet.ScalatraListener

import com.typesafe.scalalogging.StrictLogging

import javax.servlet.ServletContext

object HttpReturn {
  def responseStatus(status: Int, reason: String) = reason match {
    case "" | null => ResponseStatus(status)
    case _ => new ResponseStatus(status, reason)
  }
  def apply(body: Any = Unit, code: Integer = 200, headers: Map[String, String] = Map.empty, reason: String = "") =
    ActionResult(responseStatus(code, reason), body, headers)
}

class BasicController extends ScalatraServlet with JacksonJsonSupport with StrictLogging {

  protected implicit val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
    Commands.sethttpcode(200)

  }

  // Basic isnt thread safe, sorry 

  def common(basicFn: String) = {
    SingleScalatraService.synchronized {
      Commands.sethttpurl(request.getPathInfo)
      Commands.sethttpresponse("")
      Commands.sethttprequest(request.body)
      Commands.sethttptype(request.getContentType)
      runBasic(basicFn)

      contentType = Commands.gethttptype
      HttpReturn(Commands.gethttpresponse, Commands.gethttpcode)
    }
  }
  get("/*") {
    common("get")
  }

  post("/*") {
    common("post")
  }

  put("/*") {
    common("put")
  }

  delete("/*") {
    common("delete")
  }

  def runBasic(httpMethod: String) = {

    Runtime.valueStack.clear()
    Runtime.operationStack.clear()
    val parentClassLoader = getClass.getClassLoader();
    val pname = if (CommandLine.classpackage.isEmpty) {
      Runtime.getPname
    } else {
      CommandLine.classpackage.get
    }

    val classLoader = new MyClassLoader(parentClassLoader, s"${pname}");
    val myClass = classLoader.loadClass(s"${pname}.proc${httpMethod}");

    val method = for (m <- myClass.getMethods if (m.getName == "doLines")) yield m
    try {

      method(0).invoke(null)

    } catch {
      case ex: java.lang.reflect.InvocationTargetException => {
        val linesWeCareAbout = new scala.collection.mutable.ArrayBuffer[AnyRef]

        for (line <- ex.getCause.getStackTrace) {
          if (line.getClassName.contains(pname)) {
            linesWeCareAbout += line.toString()
          }
        }

        logger.error("Runtime stack is " + Runtime.valueStack.mkString(","))
        logger.error("syntax error ", ex)
        CommandLine.errorMsg(s"Syntax error at ${linesWeCareAbout.mkString} caused by ${ex.getCause.getMessage}")
      }
      case cause: Throwable => logger.error("runtime error ", cause)
    }
  }
}

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context mount (new BasicController, "/*")
  }
}
object SingleScalatraService {

  val HTTPURL = "httpurl"
  val HTTPRESPONSE = "httpresponse"
  val HTTPREQUEST = "httprequest"
  val HTTPCODE = "httpcode"
  val HTTPTYPE = "httptype"

  var server: Option[Server] = None

  def stop {
    if (server.isDefined) {
      server.get.stop()
      server.get.destroy()
      server = None
    }
  }
  def create {
    val port = 8080
    stop
    server = Some(server.getOrElse(new Server(port)))
    server.get.stop()
    val context = new WebAppContext()
    context.setContextPath("/")
    context.setResourceBase(".")
    context.setInitParameter(ScalatraListener.LifeCycleKey, "beeb.rpn.ScalatraBootstrap")
    context.setEventListeners(Array(new ScalatraListener))

    server.get.setHandler(context)
    server.get.start

    //server.get.join
  }

}
