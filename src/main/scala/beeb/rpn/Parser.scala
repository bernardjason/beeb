package beeb.rpn

import com.typesafe.scalalogging.StrictLogging
import java.io.FileOutputStream

case class Parser() extends StrictLogging {

  var currentCommand: String = null

  import beeb.rpn.Token.BasicParser

  def basic(line: Int, str: String, compiler: Option[Compiler]) {

    Runtime.currentCommand = s"$line $str"
    logger.info("command " + str)

    val splitup = str.commandsplit

    val order = Map(
      "array" -> 40,
      //"rnd" -> 40, "tab" -> 40,
      "<" -> 50, ">" -> 50, "+" -> 50, "-" -> 50,
      "step" -> 65,"for" -> 70,"to" ->71, 
      "<=" -> 75 , ">=" -> 75, "==" -> 75, "!=" ->75, "like" -> 75,
      "*" -> 75, "/" -> 75,
      "if" -> 200, //"=" -> 75,
      "(" -> 99 ,")"->99,
      "else" -> 200, "then" -> 200)
    def sortStackByPrescedence = {
      val sorted =Runtime.operationStack.toList.sortWith {
        case (first, second) =>
          val fv = order.getOrElse(first, 100)
          val sv = order.getOrElse(second, 100)
          fv < sv
      }
      sorted 
    }

    splitup.foreach { x => logger.debug("[" + x + "]") }

    if (compiler.isDefined) compiler.get.startLine(line)

    var valuesOnStack = 0
    def processUpToBracket = {
      var somethingToDo = 1
      for(i <- 0 until Runtime.operationStack.length ) {
        val xxx = Runtime.operationStack.elems(i)
        if ( Commands.bracketCommand.contains(xxx) == true ) { 
            somethingToDo = somethingToDo+1
        }
      }
      logger.info("start todo?="+somethingToDo+"  values=["+Runtime.valueStack.mkString(",")+"]  operations=["+Runtime.operationStack.mkString(",")+"]")
      while (somethingToDo > 0 && Runtime.operationStack.size > 0 ) {
        val xxx = Runtime.operationStack.pop()
        logger.info(s"process bracket operation ${xxx} "+Runtime.valueStack.mkString(","))

        valuesOnStack = valuesOnStack - 1
        // NEED TO POP UNTIL A COMMAND THAT NEEDS A BRACKET
        // TAB RND INSTR
        if ( xxx !=  "("  ) {
          if (compiler.isEmpty) Commands.ops(xxx, null)
          if (compiler.isDefined) compiler.get.doAnOperation(xxx)
          if ( Commands.bracketCommand.contains(xxx) == true ) {
            somethingToDo = somethingToDo-1
          }
        } else somethingToDo = somethingToDo -1
      }
      logger.info("end  values=["+Runtime.valueStack.mkString(",")+"]  operations=["+Runtime.operationStack.mkString(",")+"]")

    }
   
    val valuesOnStackStack = new scala.collection.mutable.Stack[Int]
    
    
    //for (token <- splitup) {
    var i=0
    var split = splitup
    
    //var handleOptionalElse = false

    def handleThenAndElseToken(token:String) = {
     Runtime.operationStack.push(token)
     if (compiler.isDefined) compiler.get.operationStackPush(token)
     val byPrescedence = sortStackByPrescedence
     if (compiler.isDefined) compiler.get.doOperations(byPrescedence)
     Runtime.operationStack.clear()
     valuesOnStack = valuesOnStack - 2
    }

   
    var previousToken:Option[String] = None
    while( i < split.length ) { 
      {
        val token=split(i)
        logger.info(s"process ${token}")
        if (Commands.validops.contains(token) || token.startsWith("proc")) {

          if (token == "then" || token == "else") {
            handleThenAndElseToken(token)
          } else if (token == ")") {
            processUpToBracket
            valuesOnStack = valuesOnStackStack.pop
          } else if (token == "(") {
            valuesOnStackStack.push(valuesOnStack)
            valuesOnStack=0
            Runtime.operationStack.push(token)
            if (compiler.isDefined) compiler.get.operationStackPush(token)
          } else if ( token == "dim" ) {
             Runtime.arrayStack += (split(i+1) -> null)
             //i=i+1 // skip as we have already peaked
            Runtime.operationStack.push(token)
            if (compiler.isDefined) compiler.get.operationStackPush(token)
          } else if ( token == "for" ) {
            if (compiler.isDefined) {
              //compiler.get.currentLineInformation.ofType = LineType.For
              compiler.get.setNextLineAsStart(LineType.For)
              Runtime.operationStack.push(token)
              compiler.get.operationStackPush(token)
            }
          } else if ( token == "next" ) {
            if (compiler.isDefined) {
              compiler.get.currentLineInformation.ofType = LineType.Next
              Runtime.operationStack.push(token)
              compiler.get.operationStackPush(token)
            }
          } else if ( token == "while" ) {
            if (compiler.isDefined) {
              compiler.get.currentLineInformation.ofType = LineType.While
              Runtime.operationStack.push(token)
              compiler.get.operationStackPush(token)
            }
          } else if ( token == "endwhile" ) {
            if (compiler.isDefined) {
              compiler.get.currentLineInformation.ofType = LineType.EndWhile
              Runtime.operationStack.push(token)
              compiler.get.operationStackPush(token)
            }
          } else if ( token == "repeat" ) {
            if (compiler.isDefined) {
              compiler.get.currentLineInformation.ofType = LineType.Repeat
              Runtime.operationStack.push(token)
              compiler.get.operationStackPush(token)
            }
          } else if ( token == "until" ) {
            if (compiler.isDefined) {
              compiler.get.currentLineInformation.ofType = LineType.Until
              Runtime.operationStack.push(token)
              compiler.get.operationStackPush(token)
            }
          } else if ( token == "goto" ) {
            if (compiler.isDefined) {
              Runtime.operationStack.push(token)
              compiler.get.operationStackPush(token)
            }
          }  else {
            if ( token == "print" ) {
              Runtime.valueStack.push(Commands.SENTINEL)
              if (compiler.isDefined) compiler.get.valueStackPush(Commands.SENTINEL)
            }
            Runtime.operationStack.push(token)
            if (compiler.isDefined) compiler.get.operationStackPush(token)
          }
          if (token == "=") valuesOnStack = 0
          if ( token == "goto" ) {
            if (compiler.isDefined) {
              compiler.get.currentLineInformation.goto=1
            }
          }
        } else if (token isnumeric) {
          Runtime.valueStack.push(new java.lang.Double(token.toDouble))
          if (compiler.isDefined) compiler.get.valueStackPush(token.toDouble)
          valuesOnStack = valuesOnStack + 1
        } else if (token isvalue) {
          Runtime.valueStack.push(token)
          if (compiler.isDefined) compiler.get.valueStackPush(token)
          valuesOnStack = valuesOnStack + 1
        } else {
          Runtime.valueStack.push(token)
          if (compiler.isDefined) compiler.get.valueStackPush(token)
          valuesOnStack = valuesOnStack + 1

            if ( Runtime.arrayStack.contains(token) && (i == 0 || split(i-1) != "dim") ) {
              logger.info("Will treat as array"+token)
              Runtime.operationStack.push("array")
            }
        }
        
        
        if ( previousToken.getOrElse("XXX") == "goto" ) {
            val lineNumber = token.toInt
            compiler.get.gotoLineNumbers += lineNumber
        }

        val breakers = List("and","or","not",",")
        if ( valuesOnStack >= 2 && breakers.contains(Runtime.operationStack.top)) {
          val byPrescedence = sortStackByPrescedence
       
          Runtime.operationStack.clear()
          for(x <- byPrescedence.reverse) {
            Runtime.operationStack.push(x)
          }
          val xxx = Runtime.operationStack.top
          logger.info("do top operation now??? " + Runtime.operationStack.mkString(",") );
          

          if (Commands.arithmeticCommand.contains(xxx)) {
            Runtime.operationStack.pop
            if (compiler.isEmpty) Commands.ops(xxx, null)
            if (compiler.isDefined) compiler.get.doAnOperation(xxx)
            valuesOnStack = valuesOnStack - 1
          } 
        }

        previousToken = Some(token)
        i=i+1
      }
    }
    //if ( handleOptionalElse == true ) handleThenAndElseToken("else")

    // now order operations by precedence

    val byPrescedence = sortStackByPrescedence
    Runtime.operationStack.clear()
    for(x <- byPrescedence.reverse) {
        Runtime.operationStack.push(x)
    }

    if (compiler.isDefined) compiler.get.endLine(byPrescedence)

    logger.info("Do operations "+Runtime.operationStack.mkString(",")+"  values="+Runtime.valueStack.mkString(","))
    while (Runtime.operationStack.isEmpty == false) {
      val xxx = Runtime.operationStack.pop()
      logger.info("Doing "+xxx+"  "+Runtime.valueStack.mkString(","))
      if (compiler.isEmpty) Commands.ops(xxx, null)
    }

  }

}

object Parser {
  def main(args: Array[String]): Unit = {
    {
      val parser = Parser()
      val basic = Some(Compiler("beeb/play", "Basic"));
      basic.get.startOutput
      parser.basic(10, "if 10 < 20 then A=5 else A=10", basic)
      parser.basic(20, "print A", basic)
      parser.basic(30, "B=20", basic)
      parser.basic(110, "A = 10 + 20", basic);
      parser.basic(115, "print \"A IS \"+A", basic);
      parser.basic(116, "procdemoproc \"fred\"", basic);
      parser.basic(117, "print \"A IS \"+A", basic);
      parser.basic(120, "B = A* 30 / 5", basic);
      parser.basic(140, "print \"B IS \"+B", basic);
      parser.basic(150, "print 20", basic);
      parser.basic(160, "B$ = \"hello world\"+\" \"+\"bernard\"", basic);
      parser.basic(170, "print B$", basic);
      parser.basic(180, "input HELLO$", basic);
      parser.basic(190, "print HELLO$ + B$", basic);
      basic.get.writeOut("basic/classes")
    }
    {
      val parser = Parser()
      val demoproc = Some(Compiler("beeb/play", "procdemoproc"));
      demoproc.get.startOutput
      parser.basic(1, "param B", demoproc)
      parser.basic(5, "local A", demoproc)
      parser.basic(7, "print B", demoproc)
      parser.basic(10, "A = 1000 + 5", demoproc);
      parser.basic(15, "print \"demoproc A IS \"+A", demoproc);
      demoproc.get.writeOut("basic/classes")
    }

  }
}
