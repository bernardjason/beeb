package beeb.rpn

import java.nio.file.Files
import java.nio.file.Path
import com.typesafe.scalalogging.StrictLogging
import java.nio.charset.Charset
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import java.io.BufferedWriter
import java.util.Scanner
import java.io.InputStreamReader

object Commands extends StrictLogging {

  val FALSE = 0
  val TRUE = -1
  val OK = 1
  val ERROR = 2
  val FORLOOP=5
  val STEP=1d
  val SENTINEL="\"@@!!"
  val validops = List("dbexecute","dbrows","dbopen","dbclose","dbinsert","dbselect","dbupdate","dbdelete","fopenout","fopenin",
      "fclose","fprint","finput","feof","getjson","stuff","!=" , "<=" , ">=" , "==" , "+", "-", "/", "*", "(", ")", 
      "print", "tab","cls","=", "input", 
      "local", "if", "then", "else", "<", ">", "param", "dim", ",", "array","for","next","to","step",
       "endwhile","while","rnd","len","instr","repeat","until","and","or","not","get$",
       "startweb","stopweb","sleep","like","goto"
       )
  val arithmeticCommand = List("!=" , "<=" , ">=" , "==" , "+", "-", "/", "*", "<", ">", "like","=")
  //val bracketCommand = List.concat(List("tab","instr","len","dbrows","rnd"),arithmeticCommand)
  val bracketCommand = List("tab","instr","len","dbrows","rnd","array")
  def ops(op: java.lang.String, localVariables: scala.collection.mutable.Map[String, AnyRef]): Int = {
    val r = op match {
      case "+" | "-" | "/" | "*" | "<" | ">" => Commands.arithmetic(op, localVariables)
      case "<=" | ">=" | "==" | "!="         => Commands.arithmetic(op, localVariables)
      case "(" | ")"                         => OK
      case ","                               => OK

      case "dbopen"                           => Commands.dbopen(localVariables)
      case "dbclose"                           => Commands.dbclose(localVariables)
      case "dbinsert"                           => Commands.dbupdate(localVariables)
      case "dbselect"                           => Commands.dbselect(localVariables)
      case "dbrows"                           => Commands.dbrows(localVariables)
      case "dbupdate"                           => Commands.dbupdate(localVariables)
      case "dbdelete"                           => Commands.dbupdate(localVariables)
      case "dbexecute"                           => Commands.dbexecute(localVariables)
      case "fopenin"                           => Commands.dopenin(localVariables)
      case "fopenout"                           => Commands.dopenout(localVariables)
      case "fclose"                           => Commands.dcloseHash(localVariables)
      case "feof"                           => Commands.deofHash(localVariables)
      case "fprint"                           => Commands.dfprint(localVariables)
      case "finput"                           => Commands.dinputHash(localVariables)
      case "print"                           => Commands.dprint(localVariables)
      case "tab"                             => Commands.dtab(localVariables)
      case "cls"                             => Commands.dcls(localVariables)
      case "input"                           => Commands.dinput(localVariables)
      case "get$"                            => Commands.dget(localVariables)
      case "="                               => Commands.dlet(localVariables)
      case "local"                           => Commands.dlocal(localVariables)
      case "if"                              => Commands.dif(localVariables)
      case "then"                            => Commands.dthen(localVariables)
      case "else"                            => Commands.delse(localVariables)
      case "param"                           => Commands.dparam(localVariables)
      case "dim"                             => Commands.ddim(localVariables)
      case "array"                           => Commands.darray(localVariables)
      case "for"                             => Commands.dfor(localVariables)
      case "to"                             => Commands.dto(localVariables)
      case "step"                             => Commands.dstep(localVariables)
      case "next"                            => Commands.dnext(localVariables)
      case "while"                            => Commands.dwhile(localVariables)
      case "endwhile"                            => Commands.dendwhile(localVariables)
      case "rnd"                            => Commands.drnd(localVariables)
      case "len"                            => Commands.dlen(localVariables)
      case "instr"                            => Commands.dinstr(localVariables)
      case "repeat"                            => Commands.drepeat(localVariables)
      case "until"                            => Commands.duntil(localVariables)
      case "and"                            => Commands.dand(localVariables)
      case "or"                            => Commands.dor(localVariables)
      case "not"                            => Commands.dnot(localVariables)
      case "stuff"                            => Commands.dstuff(localVariables)
      case "getjson"                            => Commands.dgetjson(localVariables)
      case "startweb"                            => Commands.dstartweb(localVariables)
      case "stopweb"                            => Commands.dstopweb(localVariables)
      case "sleep"                            => Commands.dsleep(localVariables)
      case "like"                            => Commands.dlike(localVariables)
      case "goto"                            => Commands.dgoto(localVariables)
      case _                                 => if (op.startsWith("proc")) Commands.dproc(localVariables) else ERROR
    }
    logger.info(s"op [$op] return is [$r]")
    r
  }
  implicit class BasicStringContext(sc:StringContext) {
    implicit def B(args : Any*) = '"'+sc.parts.mkString
  } 
  
  class BasicString(s:String) {
    def bString = '"'+s
  }
  implicit def stringToString(s: String) = new BasicString(s)

  
  def getValueOrVariableValue(ref: Any, localVariables: scala.collection.mutable.Map[String, AnyRef]): Any = {
    if (ref.isInstanceOf[String]) {
      if (localVariables != null) {
        if (localVariables.isDefinedAt(ref.asInstanceOf[String]) == true) {
          return localVariables(ref.asInstanceOf[String])
        }
      }
      if (Runtime.variableStack.isDefinedAt(ref.asInstanceOf[String]) == true) {
        val v = Runtime.variableStack(ref.asInstanceOf[String])
        if ( v.isInstanceOf[String] ) v.asInstanceOf[String].substring(1)
        else v
      }
      else {
        if ( ref.isInstanceOf[String] ) {
           ref.asInstanceOf[String].substring(1) 
        }
        else
          ref
      }
    } else {
      ref
    }
  }
  def setVariable(name: String, ref: AnyRef, localVariables: scala.collection.mutable.Map[String, AnyRef]) = {

    if (localVariables != null && localVariables.isDefinedAt(name)) {
      if ( ref.isInstanceOf[String] ) 
        //localVariables += (name -> ('"'+ref.asInstanceOf[String]).asInstanceOf[AnyRef] )
        localVariables += (name -> (ref.asInstanceOf[String]).bString.asInstanceOf[AnyRef] )
      else
        localVariables += (name -> ref)
    } else {
      val v1 = getValueOrVariableValue(ref, null)
      if ( v1.isInstanceOf[String] ) Runtime.variableStack += (name -> ("\""+v1).asInstanceOf[AnyRef])
      else Runtime.variableStack += (name -> v1.asInstanceOf[AnyRef])
    }
  }

  def arithmetic(operation: String, localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val v1 = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    val v2 = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    var result: Double = ERROR
    if (v1.isInstanceOf[Double] && v2.isInstanceOf[Double]) {

      result =
        operation match {
          case "*" => v2.asInstanceOf[Double] * v1.asInstanceOf[Double]
          case "/" => v2.asInstanceOf[Double] / v1.asInstanceOf[Double]
          case "+" => v2.asInstanceOf[Double] + v1.asInstanceOf[Double]
          case "-" => v2.asInstanceOf[Double] - v1.asInstanceOf[Double]
          case "<" => if (v2.asInstanceOf[Double] < v1.asInstanceOf[Double]) TRUE else FALSE
          case ">" => if (v2.asInstanceOf[Double] > v1.asInstanceOf[Double]) TRUE else FALSE
          case ">=" => if (v2.asInstanceOf[Double] >= v1.asInstanceOf[Double]) TRUE else FALSE
          case "<=" => if (v2.asInstanceOf[Double] <= v1.asInstanceOf[Double]) TRUE else FALSE
          case "==" => if (v2.asInstanceOf[Double] == v1.asInstanceOf[Double]) TRUE else FALSE
          case "!=" => if (v2.asInstanceOf[Double] != v1.asInstanceOf[Double]) TRUE else FALSE
        }

      Runtime.valueStack.push(new java.lang.Double(result))
    } else if (v2.isInstanceOf[String] && operation == "+") {
      val result = v2.asInstanceOf[String] + v1
      if (result.isInstanceOf[String]) Runtime.valueStack.push(result.bString)
      else Runtime.valueStack.push(result.asInstanceOf[java.lang.Double])
    } else if (v1.isInstanceOf[String] && operation == "+") {
      val result = v2 + v1.asInstanceOf[String]
      if (result.isInstanceOf[String]) Runtime.valueStack.push(result.bString)
      else Runtime.valueStack.push(result.asInstanceOf[java.lang.Double])
    } else if (v2.isInstanceOf[String] && operation == "==") {
      val result = if (v2.asInstanceOf[String] == v1.asInstanceOf[String]) TRUE else FALSE
      Runtime.valueStack.push(new java.lang.Double(result))
    } else {
      Runtime.error(s"${v1} ${v2} arithmetic error dont undertand")
    }
    result.toInt
  }

  var lastPrint = new StringBuilder
  def dprint(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {

    lastPrint.clear()
    val getListOfValues = Runtime.valueStack.toList.iterator
 
    val upToSentinel = new scala.collection.mutable.ListBuffer[AnyRef]
   
    var finish=false
    while ( finish == false && getListOfValues.hasNext == true) {
      val v = getListOfValues.next()
      if ( v.isInstanceOf[String] && v.asInstanceOf[String] == SENTINEL )  {
        finish = true
      } else {
        upToSentinel += v
      }
    }
    for(value <- upToSentinel.reverse ) {
       val next = getValueOrVariableValue(value, localVariables)
       print(next)
       lastPrint.append(next)
    }
    println
    Runtime.valueStack.clear()

    OK
  }
  def dtab(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val yValue = getValueOrVariableValue(Runtime.valueStack.pop, localVariables).asInstanceOf[Double]
    val xValue = getValueOrVariableValue(Runtime.valueStack.pop, localVariables).asInstanceOf[Double]
    val y = yValue.toInt +1
    val x = xValue.toInt +1
    
    Console.print("\033["+y+";"+x+"H")
    //Console.print("\033[H\033[2J")
    Console.flush()
    OK
  }
  def dcls(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    Console.print("\033[H\033[2J")
    Console.flush()
    OK
  }

  def dlet(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    if ( Runtime.valueStack.length < 2 ) {
      Runtime.error("syntax error")
      ERROR
    }  else {
      val valueOf = Runtime.valueStack.pop()
      logger.info("dlet " + valueOf + "  " + Runtime.valueStack)
      if ( Runtime.valueStack.size > 1 ) {
        
        val assignTo = Runtime.valueStack.pop().asInstanceOf[String]
        val index = Runtime.valueStack.pop().asInstanceOf[String]

        logger.info(s"ASSIGN TO $assignTo INDEX $index VALUE $valueOf")

        val arrayItem = Runtime.arrayStack.get(assignTo)
        if (arrayItem.isDefined) {
             val array = arrayItem.get
            array._2.put(s"${index}", valueOf)
        } else {
            Runtime.error("syntax error, thought it was an array")
            ERROR
        }
      } else  {
        val assignTo = Runtime.valueStack.pop().asInstanceOf[String]
        setVariable(assignTo, valueOf, localVariables)
      }
      OK
    }
  }
  def dinput(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val assignTo = Runtime.valueStack.pop().asInstanceOf[String]

    //val r = scala.io.StdIn.readLine
    // i want to see what i type, echo it back
    val oldPrompt = CommandLine.consoleReader.getPrompt
		val r = CommandLine.consoleReader.readLine("")
		CommandLine.consoleReader.setPrompt(oldPrompt)
		
    if (assignTo.endsWith("$") == false) {
      setVariable(assignTo, new java.lang.Double(r.toDouble), localVariables)
    } else {
      setVariable(assignTo, r.bString, localVariables)
    }
    OK
  }
  def dget(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val r = scala.io.StdIn.readChar()
    //Runtime.valueStack.push('"'+s"$r")
    Runtime.valueStack.push(s"$r".bString)
    OK
  }
  def dlocal(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val assignTo = Runtime.valueStack.pop().asInstanceOf[String]
    if (localVariables != null) localVariables += (assignTo -> null)
    OK
  }
  def dparam(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val assignTo = Runtime.valueStack.pop().asInstanceOf[String]
    val v1 = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)

    if (localVariables != null) localVariables += (assignTo -> v1.asInstanceOf[AnyRef])
    OK
  }
  def dproc(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val where = Runtime.valueStack.pop().asInstanceOf[String]
    OK
  }

  def dif(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val value = Runtime.valueStack.pop
    OK
  }
  def dthen(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    OK
  }
  def delse(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    OK
  }
  def ddim(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val one = Runtime.valueStack.pop()
    val two = Runtime.valueStack.pop()
    val oneValue = getValueOrVariableValue(one, localVariables)
    val twoValue = getValueOrVariableValue(two, localVariables)
    if (oneValue.isInstanceOf[Double] && twoValue.isInstanceOf[Double]) {
      val variableName = Runtime.valueStack.pop.asInstanceOf[String]
      val x = getValueOrVariableValue(one, localVariables)
      val y = getValueOrVariableValue(two, localVariables)
      logger.info(variableName + "2 DIM " + "ARRAY SIZE " + x + "," + y)

      val values:scala.collection.mutable.Map[String,Any] = if ( variableName.endsWith("$") ) {
        val array = scala.collection.mutable.Map[String, String]()
       
        for(i <- 0 to y.asInstanceOf[Int] ) {
          for(j <- 0 to x.asInstanceOf[Int] ) {
            array += (s"$i,$j" -> "")  
          }
        }
        array.asInstanceOf[scala.collection.mutable.Map[String,Any]]
      } else {
        val array=scala.collection.mutable.Map[String, java.lang.Double]()
        for(i <- 0 to y.asInstanceOf[java.lang.Double].toInt ) {
          for(j <- 0 to x.asInstanceOf[java.lang.Double].toInt ) {
            array += (s"$i,$j" -> 0  )
          }
        }
        array.asInstanceOf[scala.collection.mutable.Map[String,Any]]
      }

      Runtime.arrayStack.put(variableName, (2, values))
    } else {
      val variableName = two.asInstanceOf[String]
      val y = getValueOrVariableValue(one, localVariables)
      logger.info(one + "1 DIM " + "ARRAY SIZE " + y)
      val values = if ( variableName.endsWith("$") ) { 
        val array =scala.collection.mutable.Map[String, String]()
        for(i <- 0 to y.asInstanceOf[Double].toInt) {
          array += (s"$i" -> "")  
        }
        array.asInstanceOf[scala.collection.mutable.Map[String,Any]]
      } else {
        val array =scala.collection.mutable.Map[String, java.lang.Double]()
        for(i <- 0 to y.asInstanceOf[Double].toInt) {
          array += (s"$i" -> 0)  
        }
        array.asInstanceOf[scala.collection.mutable.Map[String,Any]]
      }
      Runtime.arrayStack.put(two.asInstanceOf[String], (1, values))
    }
    OK
  }
  /*
   * keep popping until we hit a array variable name. Then if
   * 2d and 3 values assign to multi
   * if 1d and 2 values assign
   * if 2d and 2 values get
   * if 1d and 1 value get 
   */
  def darray(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val values = new scala.collection.mutable.ArrayBuffer[Any]
    var foundArray = false
    var arrayKey: String = null
    logger.info("start "+Runtime.valueStack.mkString(","))
    while (foundArray == false && Runtime.valueStack.size > 0) {

      val next = Runtime.valueStack.pop()
      if (next.isInstanceOf[String] && Runtime.arrayStack.contains(next.asInstanceOf[String])) {
        foundArray = true
        arrayKey = next.asInstanceOf[String]
      } else {
        values += next
      }
    }
    def addArrayValue(value: Option[Any]) = {
      if (value.isDefined) {
        value.get match {
          case s: String           => Runtime.valueStack.push(s)//.substring(1)) 
          case d: java.lang.Double => Runtime.valueStack.push(d)
          case _                   => Runtime.error("2d WHAT ARRAY couldnt handle " + value)
        }
      }
    }
    logger.info(arrayKey + " " + values)
    if (arrayKey != null) {
      val arrayItem = Runtime.arrayStack.get(arrayKey)
      logger.info("1", arrayKey, values,arrayItem)
      if (arrayItem.isDefined) {
        val array = arrayItem.get
        logger.info(s"Array ${arrayKey} values ${array.toString()}")
        if (array._1 == 2) { //2d
          logger.info(s"2D ARRAY ${Runtime.valueStack.size} "+values.mkString(","));
          if (Runtime.valueStack.size == 0) { //assign value
            val indexx = anyToArrayIndex(getValueOrVariableValue(values(1),localVariables))
            val indexy = anyToArrayIndex(getValueOrVariableValue(values(0),localVariables))
            //array._2.put(s"${indexx},${indexy}", values(0))
            Runtime.valueStack.push(s"${indexx},${indexy}")
            Runtime.valueStack.push(arrayKey)

          } else if (values.size == 2) { //get value
            val indexx = anyToArrayIndex(getValueOrVariableValue(values(1),localVariables))
            val indexy = anyToArrayIndex(getValueOrVariableValue(values(0),localVariables))
            val value = array._2.get(s"${indexx},${indexy}")
            addArrayValue(value)
          } else {
            Runtime.error("2d WHAT ARRAY")
          }
        } else { //1d
          logger.info(s"1D ARRAY ${Runtime.valueStack.size} "+values.mkString(","));
          if (Runtime.valueStack.size == 0) { //assign value
            val index = anyToArrayIndex( getValueOrVariableValue(values(0),localVariables) )
            //array._2.put(s"${index}", values(0))
            Runtime.valueStack.push(s"${index}")
            Runtime.valueStack.push(arrayKey)
            logger.info(s"1D ASSIGNING ${arrayKey}  index =${index} value ${values(0)} ")
          } else if (values.size == 1) { //get value
            //Runtime.valueStack.push (array._2.get(s"${values(0)}"))
            val index = anyToArrayIndex( getValueOrVariableValue(values(0),localVariables) )
            val value = array._2.get(s"${index}")
            logger.info(s"1D GET ${arrayKey} index ${index} value is ${value}") //${Runtime.valueStack} ${index}")
            addArrayValue(value)
          } else {
            Runtime.error("1d WHAT ARRAY")
          }

        }
      }
    }
    logger.info("end "+Runtime.valueStack.mkString(","))
    OK
  }

  case class ForLoop(start:Double,end:Double,step:Double) {
    var counter=start;
    def process:Boolean = {
      counter = counter + step;
      if ( step >= 0  ) {
        if ( counter > end ) return true
      } else{
        if ( counter < end ) return true
      }
      false
    }
  }

  val forLoopStack = scala.collection.mutable.Map[String,ForLoop]()

  def dfor(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val end  = getValueOrVariableValue(Runtime.valueStack.pop, localVariables).asInstanceOf[Double]
    val start  = getValueOrVariableValue(Runtime.valueStack.pop, localVariables).asInstanceOf[Double]
    val variable  = Runtime.valueStack.pop
    val toAdd = ForLoop(start,end,step)
    forLoopStack += ( variable.asInstanceOf[String] -> toAdd)
    setVariable(variable.asInstanceOf[String], toAdd.counter.asInstanceOf[AnyRef], localVariables)
    step=STEP

    OK
  }
  def dto(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    Runtime.valueStack.push("BOGUS")
    Runtime.valueStack.push("99")
    OK
  }
  
  var step=STEP
  def dstep(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
   
      val byStep  = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
     
      step=byStep.asInstanceOf[Double]
    
    OK
  }
  def dnext(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val variable  = Runtime.valueStack.pop
    val doLoop = forLoopStack(variable.asInstanceOf[String])
    
    val doit = doLoop.process
    setVariable(variable.asInstanceOf[String], doLoop.counter.asInstanceOf[AnyRef], localVariables)

    if ( doit == false  ) FORLOOP 
    else OK
  }
  def drepeat(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    OK
  }
  def duntil(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val value = Runtime.valueStack.pop
    if ( value == FALSE ) FORLOOP
    else OK
  }

  
  def anyToArrayIndex(v:Any): Any = {
    val i = (v match {
    case x:Double => x.toInt
    case x:String => x
    case _ => Int.MinValue // should I throw an error?
    })  
    return i
  }
  def dwhile(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val value = Runtime.valueStack.pop
    if ( value == TRUE ) FORLOOP
    else OK
  }
  
  def dendwhile(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    OK
  }
  def drnd(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val value = if ( Runtime.valueStack.size != 0 ) 
       getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    else 0d
    val range=value.asInstanceOf[Double]
    val result = range match {
      case 1 => Math.random()
      case x if x > 1 => Math.round(Math.random()*(Math.round(range)-1)) +1
      case _ => Math.round(Math.random()*2147483647*( Math.round(Math.random()*2 ) -1))
    }
    Runtime.valueStack.push(new java.lang.Double(result))
    OK
  }
  def dlen(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val string = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    val result = string.asInstanceOf[String].length()
    Runtime.valueStack.push(new java.lang.Double(result))
    OK
  }
  def dinstr(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val startFrom = getValueOrVariableValue(Runtime.valueStack.pop, localVariables).asInstanceOf[Double]
    
    val xxx = Runtime.valueStack.pop
    val searchFor = getValueOrVariableValue(xxx, localVariables)

    val searchIn = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)

    val result = searchIn.asInstanceOf[String].indexOf(searchFor.asInstanceOf[String],startFrom.toInt-1)
    val r = if ( result >= 0 && searchFor != ""  ) result+1 else 0

    Runtime.valueStack.push(new java.lang.Double(r))
    OK
  }

  def dstuff(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    println("variables ",Runtime.variableStack)
    println("locals  ",localVariables)
    println("values ",Runtime.valueStack)
    println("arrays ",Runtime.arrayStack)
    OK
  }
  def dand(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val a = Runtime.valueStack.pop
    val b = Runtime.valueStack.pop
    if ( a == TRUE && b == TRUE )  Runtime.valueStack.push(new java.lang.Double(TRUE))
    else Runtime.valueStack.push(new java.lang.Double(FALSE))
    OK
  }
  def dor(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val a = Runtime.valueStack.pop
    val b = Runtime.valueStack.pop
    if ( a == TRUE || b == TRUE )  Runtime.valueStack.push(new java.lang.Double(TRUE))
    else Runtime.valueStack.push(new java.lang.Double(FALSE))
    OK
  }
  def dnot(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val a = Runtime.valueStack.pop
    val b = Runtime.valueStack.pop
    if ( a == TRUE || b == TRUE )  Runtime.valueStack.push(new java.lang.Double(TRUE))
    else Runtime.valueStack.push(new java.lang.Double(FALSE))
    OK
  }
  def dgoto(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val a = Runtime.valueStack.pop
    Runtime.gotoLine = a.asInstanceOf[Double].toInt
    OK
  }


  implicit class Regex(sc: StringContext) {
      def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  } 
  
  def dgetjson(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {

    import play.api.libs.json._


    val content = getValueOrVariableValue(Runtime.valueStack.pop, localVariables).asInstanceOf[String]

    val json: JsValue = Json.parse(content.mkString)

    val path = getValueOrVariableValue(Runtime.valueStack.pop, localVariables).asInstanceOf[String]
    val elements = path.split(" ")
    
    var current:JsValue = json
    elements.foreach { x => 
      x match {
        case r"\d+" => current = current.\(x.toInt).getOrElse(JsString("undefined"))
        case _ => current = current.\(x).getOrElse(JsString("undefined"))
      }
      
    }

    current.result.get match {
      case v:JsString =>   Runtime.valueStack.push(v.toString.stripPrefix("\"").stripSuffix("\"" ).bString)
      case v:JsNumber =>  Runtime.valueStack.push(new java.lang.Double(java.lang.Double.parseDouble(v.toString())))
      case v:JsBoolean =>  Runtime.valueStack.push(v.toString.bString)
      case v:JsObject => Runtime.valueStack.push(v.toString.bString )
      case v:JsArray => Runtime.valueStack.push("invalid array") 
      case _ => Runtime.valueStack.push("invalid") 
    }
    
    OK
  }
  def dopenin(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val fileName = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    logger.info(s"fileName");

    try {
    val reader = new BufferedReader(new FileReader(new java.io.File(fileName.asInstanceOf[String])))

    val key = Runtime.fileInDescriptor.incrementAndGet()
    Runtime.fileDescriptorsIn.put(key, reader)
    Runtime.valueStack.push(new java.lang.Double(key)) 
    } catch {
      case ex:Throwable => Runtime.error(s"fopenin ${ex.getMessage}") 
      Runtime.valueStack.push(new java.lang.Double(-1)) 
    }
    OK
  }
  def dopenout(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val fileName = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)

    try {
      val writer = new BufferedWriter(new FileWriter(new java.io.File(fileName.asInstanceOf[String])))
      val key = Runtime.fileOutDescriptor.incrementAndGet()
      Runtime.fileDescriptorsOut.put(key, writer)
      Runtime.valueStack.push(new java.lang.Double(key)) 
    } catch {
      case ex:Throwable => Runtime.error(s"fopenout ${ex.getMessage}") 
      Runtime.valueStack.push(new java.lang.Double(-1)) 
    }
    OK
  }
  def dcloseHash(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val v = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)

    val key = v.asInstanceOf[Double].toInt
    val desc = Runtime.fileDescriptorsIn.get(key)
    if ( desc.isDefined ) desc.get.close()
    val descOut = Runtime.fileDescriptorsOut.get(key)
    if ( descOut.isDefined ) descOut.get.close()
    OK
  }
  def deofHash(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val v = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)

    val key = v.asInstanceOf[Double].toInt
    val desc = Runtime.fileDescriptorsIn.get(key)
    val eof = if ( desc.isDefined ) desc.get.ready() else false
    if ( eof )  Runtime.valueStack.push(new java.lang.Double(FALSE))
    else Runtime.valueStack.push(new java.lang.Double(TRUE))
    OK
  }
  def dinputHash(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val variable = Runtime.valueStack.pop
    val v = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    val key = v.asInstanceOf[Double].toInt
    val desc = Runtime.fileDescriptorsIn.get(key)
    val s = desc.get.readLine()
    setVariable(variable.asInstanceOf[String], s.bString, localVariables)

    OK
  }
  
  
  def dfprint(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {

    val getListOfValues = Runtime.valueStack.toList.iterator
 
    val upToSentinel = new scala.collection.mutable.ListBuffer[AnyRef]
   
    var finish=false
    while ( finish == false && getListOfValues.hasNext == true) {
      val v = getListOfValues.next()
      if ( v.isInstanceOf[String] && v.asInstanceOf[String] == SENTINEL )  {
        finish = true
      } else {
        upToSentinel += v
      }
    }
    val ordered = upToSentinel.reverse
    val key = getValueOrVariableValue(ordered(0), localVariables)
    val out = Runtime.fileDescriptorsOut.get(key.asInstanceOf[Double].toInt).get
    for(value <- ordered.drop(1) ) {
       out.write( ""+getValueOrVariableValue(value, localVariables))
    }
    out.newLine()
    Runtime.valueStack.clear()

    OK
  }
  def dstartweb(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    SingleScalatraService.create
    OK
  }
  def dstopweb(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    SingleScalatraService.stop
    OK
  }
  def dsleep(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val milli = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    Thread.sleep(milli.asInstanceOf[Double].toLong)
    OK
  }
  def dlike(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val a =getValueOrVariableValue(Runtime.valueStack.pop, localVariables).asInstanceOf[String]
    val b =getValueOrVariableValue(Runtime.valueStack.pop, localVariables).asInstanceOf[String]
    val r = if ( b.matches(a) )  TRUE else FALSE
    r
  }
 
  def gethttpresponse = {
   Runtime.variableStack.get(SingleScalatraService.HTTPRESPONSE ).getOrElse("\"").asInstanceOf[String].substring(1)
  }
  def sethttpresponse(s:String) = {
   Runtime.variableStack.put(SingleScalatraService.HTTPRESPONSE,"\""+s)
  }
  def sethttpurl(s:String) = {
   Runtime.variableStack.put(SingleScalatraService.HTTPURL,"\""+s)
  }
  def sethttprequest(s:String) = {
   Runtime.variableStack.put(SingleScalatraService.HTTPREQUEST,"\""+s)
  }
  def gethttpcode = {
   Runtime.variableStack.get(SingleScalatraService.HTTPCODE ).getOrElse(200).asInstanceOf[Double].toInt
  }
  def sethttpcode(i:Int) = {
   Runtime.variableStack.put(SingleScalatraService.HTTPCODE,new java.lang.Double(i))
  }
  def gethttptype = {
   Runtime.variableStack.get(SingleScalatraService.HTTPTYPE ).getOrElse("\"").asInstanceOf[String].substring(1)
  }
  def sethttptype(s:String) = {
   Runtime.variableStack.put(SingleScalatraService.HTTPTYPE,"\""+s)
  }

  def dbopen(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val password = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    val user = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    val url = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    val className = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)

    val key = Runtime.dbInDescriptor.incrementAndGet()

    Runtime.dbDescriptorsIn.put(key, Database(className.asInstanceOf[String],url.asInstanceOf[String],user.asInstanceOf[String],password.asInstanceOf[String]))
    Runtime.valueStack.push(new java.lang.Double(key)) 
    OK
  }
  def dbclose(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val key = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    val db = Runtime.dbDescriptorsIn.get(key.asInstanceOf[Double].toInt).get
    db.dclose
    OK
  }
  def dbselect(localVariables: scala.collection.mutable.Map[String, AnyRef]) = {
    val result = Runtime.valueStack.pop.asInstanceOf[String].substring(1)
    val fields = Runtime.valueStack.pop.asInstanceOf[Double]
    val query = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    val key = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    val db = Runtime.dbDescriptorsIn.get(key.asInstanceOf[Double].toInt).get
    Runtime.arrayStack.put(result,(2,scala.collection.mutable.Map("" -> "".asInstanceOf[Any] ) ) ) 
    val arrayItem = Runtime.arrayStack.get(result)
    val array = arrayItem.get
    db.dselect(query.asInstanceOf[String],fields.toInt)
    var indexy=0
    db.currentQuery.get.map{ b => 
      for(indexx <- 0 until fields.toInt ) {
        array._2.put(s"${indexx},${indexy}", "\""+b.values(indexx))
      }
      indexy=indexy+1
    }
    OK
  }
  def dbrows(localVariables: scala.collection.mutable.Map[String, AnyRef]) = { 
    val key = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    val db = Runtime.dbDescriptorsIn.get(key.asInstanceOf[Double].toInt).get
    if ( db.currentQuery.isDefined  )  {
      Runtime.valueStack.push(new java.lang.Double(db.currentQuery.get.size))
    } else {
      Runtime.valueStack.push(new java.lang.Double(-1))
    }
    OK
  }
  def dbupdate(localVariables: scala.collection.mutable.Map[String, AnyRef]) = { 
    val query = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    val key = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    val db = Runtime.dbDescriptorsIn.get(key.asInstanceOf[Double].toInt).get
    db.dupdate(query.asInstanceOf[String])
    OK
  }
  def dbexecute(localVariables: scala.collection.mutable.Map[String, AnyRef]) = { 
    val query = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    val key = getValueOrVariableValue(Runtime.valueStack.pop, localVariables)
    val db = Runtime.dbDescriptorsIn.get(key.asInstanceOf[Double].toInt).get
    db.dexecute(query.asInstanceOf[String])
    OK
  }
}