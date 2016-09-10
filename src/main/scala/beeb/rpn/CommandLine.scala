package beeb.rpn

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Scanner
import jline.console.completer.StringsCompleter
import jline.console.ConsoleReader
import scala.io.Source
import java.io.File
import java.io.PrintWriter
import com.sun.org.apache.xml.internal.security.utils.Base64
import com.typesafe.scalalogging.StrictLogging

object CommandLine extends StrictLogging {

  val TARGETDIR = "basic/classes/"
  val BasicMain = "BasicMain"
  val SUFFIX = ".basic"
  val STARTLINE = 1

  val parser = Parser()
  var compiler: Option[Compiler] = None //= Some(Compiler("beeb/play","Basic"));

  val codeLine = scala.collection.mutable.Map[Int, String]()
  val sourceLineRegex = "[ |	]*[0-9].*".r

  var classpackage: Option[String] = None

  val consoleReader = new jline.console.ConsoleReader()
  consoleReader.setPrompt("beebb>")

  val commands = List("QUIT", "LIST", "RUN", "LOAD", "SAVE")
  import scala.collection.JavaConversions._
  consoleReader.addCompleter(new StringsCompleter(commands.map { _.toLowerCase } ++ commands.toList))

  def errorMsg(msg: String) {
    println(Runtime.currentCommand,msg)
  }

  def readLine = {

    var dontEnd = true
    while (dontEnd) {

      val line = consoleReader.readLine()
      if (line == null) {
        println("Ctrl-d")
        dontEnd = false
      } else if (line.size > 0) {
        dontEnd = line.toUpperCase().trim match {
          case "QUIT"                    => false
          case "LIST"                    => list
          case "RUN"                     => run
          case s if s.startsWith("SAVE") => save(s)
          case s if s.startsWith("LOAD") => load(s)
          case s if s.startsWith("DELETE") => delete(s)
          case sourceLineRegex()         => enterSourceCode(line.trim)
          case _                         => parser.basic(0, line, compiler); true
          //case _ => errorMsg("I didnt understand"); true
        }
      }
    }
  }
  def enterSourceCode(line: String): Boolean = {

    if (classpackage.isEmpty) {
      errorMsg("You must SAVE to a package");
      return true;
    }

    val (number, commandLine) = getNumberAndCommandLine(line)

    codeLine += (number.toInt -> commandLine.mkString.trim)
    true
  }

  def getNumberAndCommandLine(line: String) = {
    val number = new StringBuilder
    val commandLine = new StringBuilder

    var appendTo = number

    for (c <- line) {
      if (c < '0' || c > '9') appendTo = commandLine
      appendTo.append(c)
    }
    (number, commandLine)
  }

  def list = {
    val sorted = codeLine.toSeq.sortWith(_._1 < _._1)
    sorted.map {
      case (number, code) => println(s"$number $code")
    }
    true
  }
  def run = {

    Runtime.valueStack.clear()
    Runtime.variableStack.clear()
    Runtime.arrayStack.clear()
    Runtime.operationStack.clear()
    save("save")
    val parentClassLoader = getClass.getClassLoader();
    val pname = classpackage.get
    val classLoader = new MyClassLoader(parentClassLoader, s"${pname}");
    val myClass = classLoader.loadClass(s"${pname}.${BasicMain}");

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

        logger.error(s"syntax error ${Runtime.currentCommand}", ex)
        errorMsg(s"Syntax error at ${linesWeCareAbout.mkString} caused by ${ex.getCause.getMessage}")

      }
      case cause: Throwable => logger.error("runtime error ", cause)
    }
    true
  }
  def save(command: String) = {

    val where = command.split(" ")
    if (where.size != 2 && classpackage.isEmpty) errorMsg("SAVE package not " + command)
    else {
      if (where.size == 2) {
        val dest = where(1).toLowerCase()
        new java.io.File(TARGETDIR + dest).mkdir()
        classpackage = Some(where(1).toLowerCase())
      }
      consoleReader.setPrompt(classpackage.get + ">")

      val pw = new PrintWriter(new File(classpackage.get + SUFFIX))

      val sorted = codeLine.toSeq.sortWith(_._1 < _._1)
      sorted.map {
        case (number, code) => pw.println(s"$number $code")
      }
      pw.close()

      compileCode(BasicMain)
    }
    true
  }

  def compileCode(className: String, beginFrom: Int = STARTLINE): Int = {
    var startLine = beginFrom
    var lastLine = startLine
    if (codeLine.size > 0) {
      new java.io.File(s"${TARGETDIR}/${classpackage.get}").mkdirs()
      val compiler = Some(Compiler(classpackage.get, className));
      compiler.get.startOutput
      val sorted = codeLine.toSeq.sortWith(_._1 < _._1)
      sorted.map {
        case (number, code) => {
          if (number.toInt >= startLine) {
            if (code.startsWith("defproc")) {
              val splitup = code.split(" ") // should be Parse implicit
              startLine = compileCode("proc" + splitup(1), number.toInt + 1)
            } else if (code.startsWith("endproc")) {
              lastLine = number.toInt + 1
              startLine = Int.MaxValue
            } else {
              parser.basic(number, code, compiler)
            }
          }
        }
      }

      compiler.get.writeOut(TARGETDIR)
    }
    compiler = None
    return lastLine
  }
  def load(command: String) = {

    val where = command.split(" ")
    if (where.size != 2) {
      errorMsg("LOAD package not " + command)
    } else {
      val dest = where(1).toLowerCase()
      if ( ! new File(dest+SUFFIX).exists()  ) {
        errorMsg("Could not find "+dest+SUFFIX)     
      } else {
        classpackage = Some(where(1).toLowerCase())

        consoleReader.setPrompt(classpackage.get + ">")
  
        codeLine.clear()
  
        for (line <- Source.fromFile(classpackage.get + SUFFIX).getLines) {
          val (number, commandLine) = getNumberAndCommandLine(line)
          codeLine += (number.toInt -> commandLine.mkString.trim)
        }
  
        compileCode(BasicMain)
      }
    }
    true
  }
  def delete(command: String) = {

    val where = command.split(" ")
    if (where.size < 2) {
      errorMsg("DELETE <linenumber> or <linenumber start>,<linenumber end> " + command)
    } else {
      var start:Option[Int]= None
      var end:Option[Int]= None
      import beeb.rpn.Token.BasicParser
      val yy = command.commandsplit
      if ( yy.length > 1 && yy(1).isnumeric ) start = Some(yy(1).toInt)
      if ( yy.length > 3 && yy(3).isnumeric ) end = Some(yy(3).toInt)

      val s = start.getOrElse(-1)
      val e = end.getOrElse(s)
      for(i <- s to e) {
        val e = codeLine.get(i) 
         e.exists { x => 
          logger.info(s"delete ${i} ${codeLine(i)}")
          codeLine.remove(i)
          true
        }
      }
    }
    true
  }
  def main(args:Array[String]) {
    readLine
  }
}