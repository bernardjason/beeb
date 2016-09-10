package beeb.rpn

import com.typesafe.scalalogging.StrictLogging
import java.util.HashMap
import java.io.BufferedReader
import java.io.BufferedWriter
import java.util.concurrent.atomic.AtomicInteger

object Runtime extends StrictLogging {

  private var pname:String = ""
  def setPname(packageName:String) = pname = packageName
  def getPname = pname
   
  var gotoLine:Int= -1
  def getGotoLine = gotoLine
  def setGotoLine = { gotoLine = -1 ; gotoLine }
  val valueStack = new scala.collection.mutable.Stack[AnyRef]

  val operationStack = new scala.collection.mutable.Stack[String]
  val variableStack = scala.collection.mutable.Map[String,AnyRef]() 
  val arrayStack = scala.collection.mutable.Map[String,Tuple2[Int, scala.collection.mutable.Map[String,Any]      ]]()

  val fileDescriptorsIn = scala.collection.mutable.Map[Int,BufferedReader]() 
  val fileInDescriptor = new AtomicInteger(1)
  val fileDescriptorsOut = scala.collection.mutable.Map[Int,BufferedWriter]() 
  val fileOutDescriptor = new AtomicInteger(1)

  val dbDescriptorsIn = scala.collection.mutable.Map[Int,Database]() 
  val dbInDescriptor = new AtomicInteger(1)

  def getLocalVariableStack() = {
    scala.collection.mutable.Map[String,AnyRef]() 
  }

  var currentCommand:String=null
  def setCurrentCommand(command:String) = { 
    currentCommand=command 
    logger.info(currentCommand)
  }
  def error(msg:String) {
    println(currentCommand)
    println("ERROR "+msg)
  }
   
}