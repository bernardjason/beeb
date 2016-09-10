package beebb.rpn

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import beeb.rpn.Parser
import beeb.rpn.Runtime
import beeb.rpn.Commands

class TestToken extends FlatSpec with ShouldMatchers {

  import beeb.rpn.Token.BasicParser

  it should "split if then statement" in {
    val xx = "if a !=-10 then print \"hello = to you if then else line\"".commandsplit
    xx.foreach { x => print("["+x+"] ") } ; println
  
    assert( xx(0) == "if" )
    assert( xx(1) == "a" )
    assert( xx(2) == "!=" )
    assert( xx(3) == "-10" )
    assert( xx(4) == "then" )
    assert( xx(5) == "print" )
    assert( xx(6) == "\"hello = to you if then else line" )

  }

  it should "split 30-5" in {
    val xx = "30-5".commandsplit
    xx.foreach { x => print("["+x+"] ") } ; println
    assert( xx(0) == "30" )
    assert( xx(1) == "-" )
    assert( xx(2) == "5" )
  }
  
  it should "split 30--5" in {
    val xx = "30--5".commandsplit
    xx.foreach { x => print("["+x+"] ") } ; println
    assert( xx(0) == "30" )
    assert( xx(1) == "-" )
    assert( xx(2) == "-5" )
  } 

  it should "split for loop" in {
    val xx = "for i = 0 to rows-1".commandsplit
    xx.foreach { x => print("["+x+"] ") } ; println
    assert( xx(0) == "for" )
    assert( xx(1) == "i" )
    assert( xx(2) == "=" )
    assert( xx(3) == "0" )
    assert( xx(4) == "to" )
    assert( xx(5) == "rows" )
    assert( xx(6) == "-" )
    assert( xx(7) == "1" )
  }

  it should "assign -1 to Y" in {
    val xx = "Y=-1".commandsplit
    xx.foreach { x => print("["+x+"] ") } ; println
    assert( xx(0) == "Y" )
    assert( xx(1) == "=" )
    assert( xx(2) == "-1" )
  }

   it should "handle array correctly" in {
    val xx = "d$=''+result(0,i)+'--'+result(1,i)+'---'+result(2,i)".commandsplit
    xx.foreach { x => print("["+x+"] ") } ; println
  }
   it should "also handle array correctly" in {
    val xx = "d$=result(0,i)+'--'+result(1,i)+'---'+result(2,i)".commandsplit
    xx.foreach { x => print("["+x+"] ") } ; println
  }

   it should "parse correctly" in {
     val parser = Parser()
     parser.basic(5, "dim result(3,3)", None)
     parser.basic(6, "result(1,1)=\"one\"", None)
     parser.basic(7, "result(2,1)=\"two\"", None)
     parser.basic(8, "result(3,1)=\"three\"", None)
     parser.basic(10, "d$=result(1,1)+'--'+result(2,1)+'--'+result(3,1)", None)
     parser.basic(11, "f$=''+result(1,1)+'--'+result(2,1)+'--'+result(3,1)", None)
    
     assert(Runtime.variableStack("d$") == "\"one--two--three")
     assert(Runtime.variableStack("f$") == "\"one--two--three")
   }

  it should "parse simple assign ok" in {
     val parser = Parser()
     parser.basic(5, "A$=\"hello \"+rnd(1000)", None)
     println(Runtime.valueStack)
     println(Runtime.variableStack)
     assert(Runtime.variableStack("A$").asInstanceOf[String].startsWith("\"hello "))
   } 
  
   it should "parse more complicated assign ok" in {
     val parser = Parser()
     parser.basic(5, "A$=\"hello \"+rnd(10*10)+\" jack\"", None)
     println("Values",Runtime.valueStack)
     println("Variables",Runtime.variableStack)
     assert(Runtime.variableStack("A$").asInstanceOf[String].startsWith("\"hello "))
     assert(Runtime.variableStack("A$").asInstanceOf[String].endsWith("jack"))
   }
 
   it should "parse print statement" in {
     val parser = Parser()
     parser.basic(1, "L=5",None)
     parser.basic(5, "print \"you have \" (L*2)  \" tries\"",None)
     val printed = Commands.lastPrint.toString()
     assert(printed == "you have 10.0 tries")
   }
   it should "parse print tab statement" in {
     val parser = Parser()
     parser.basic(1, "G$=\"hello\"",None)
     parser.basic(2, "P=8",None)
     parser.basic(5, "print tab(P+12,15) G$",None)
     assert(Commands.lastPrint.toString == "hello")
   }
}