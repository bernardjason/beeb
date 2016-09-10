package beebb.rpn

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import beeb.rpn.Runtime
import beeb.rpn.CommandLine

class TestScript1 extends FlatSpec with ShouldMatchers {
  it should "run testscript1 goto test" in {
    CommandLine.load("load testscript1")
    CommandLine.run
    println(Runtime.variableStack)
    assert(Runtime.variableStack("I") == 10)
  }
  it should "run testscript2 hangman test" in {
    CommandLine.load("load testscript2")
    CommandLine.run
    println(Runtime.variableStack)
    assert(Runtime.variableStack("A$") == "\"hello")
    assert(Runtime.variableStack("P") == 0)
    assert(Runtime.variableStack("CORRECT") == 5)
    assert(Runtime.variableStack("TRIES") == 4)
    assert(Runtime.variableStack("guess") == 5)
  }
  it should "run testscript3 database" in {
    CommandLine.load("load testscript3")
    CommandLine.run
    println(Runtime.variableStack)
    assert(Runtime.variableStack("d$").asInstanceOf[String].contains("--hello"))
  }

}