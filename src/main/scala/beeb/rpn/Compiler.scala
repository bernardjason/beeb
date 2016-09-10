package beeb.rpn

import java.io.FileOutputStream

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.Opcodes

import com.typesafe.scalalogging.StrictLogging

object LineType extends Enumeration {
  type LineTypeBase = Value
  val For, Next, While, EndWhile, Repeat, Until, Normal = Value
}
case class Compiler(classpackage: String, destination: String) extends StrictLogging {

  val cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
  var fv: FieldVisitor = null;
  var mv = new AsmWrapper();
  var av: AnnotationVisitor = null

  val gotoLineNumbers = scala.collection.mutable.ListBuffer.empty[Int]

  case class LineInformation(number: Int, var ofType: LineType.Value, var goto: Int = -1)
  case class WhileInformation(number: Int, label: Label)
  val allLineNumbers = new scala.collection.mutable.ArrayBuilder.ofRef[LineInformation]
  val ifJumps = new scala.collection.mutable.Stack[Label]
  var ifJumpDestinationFirst = true

  var labelGotoEnd: Option[Label] = None

  val methodLabels = new scala.collection.mutable.Stack[Label]

  val allLocalCalls = new scala.collection.mutable.ArrayBuilder.ofRef[Label]
  val forLoopLabels = new scala.collection.mutable.Stack[Label]
  val whileLoopLabels = new scala.collection.mutable.Stack[WhileInformation]
  var _outputLineNumber = 0
  def outputLineNumber = { _outputLineNumber = _outputLineNumber + 10; _outputLineNumber }
  def startOutput = {
    _outputLineNumber = 0
    cw.visit(52, ACC_PUBLIC + ACC_SUPER, s"${classpackage}/${destination}", null, "java/lang/Object", null);
    cw.visitSource(s"${destination}.java", null);

    fv = cw.visitField(0, "localVariables", "Lscala/collection/mutable/Map;", "Lscala/collection/mutable/Map<Ljava/lang/String;Ljava/lang/Object;>;", null);

    fv.visitEnd();

    fv = cw.visitField(ACC_STATIC, "_goto", "I", null, null);
    fv.visitEnd();

    mv equals cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
    mv.visitCode();
    val l0 = new Label();
    mv.visitLabel(l0);
    mv.visitLineNumber(outputLineNumber, l0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    val l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLineNumber(outputLineNumber, l1);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETSTATIC, "beeb/rpn/Runtime$", "MODULE$", "Lbeeb/rpn/Runtime$;");
    mv.visitMethodInsn(INVOKEVIRTUAL, "beeb/rpn/Runtime$", "getLocalVariableStack", "()Lscala/collection/mutable/Map;", false);
    mv.visitFieldInsn(PUTFIELD, s"${classpackage}/${destination}", "localVariables", "Lscala/collection/mutable/Map;");

    val setGoto = new Label();
    mv.visitLabel(setGoto);
    mv.visitLineNumber(outputLineNumber, setGoto);
    mv.visitInsn(ICONST_1);
    mv.visitFieldInsn(PUTSTATIC, s"${classpackage}/${destination}", "_goto", "I");

    val packageLabel = new Label();
    mv.visitLabel(packageLabel);
    mv.visitLineNumber(outputLineNumber, packageLabel);
    mv.visitFieldInsn(GETSTATIC, "beeb/rpn/Runtime$", "MODULE$", "Lbeeb/rpn/Runtime$;");
    mv.visitLdcInsn(s"${classpackage}");
    mv.visitMethodInsn(INVOKEVIRTUAL, "beeb/rpn/Runtime$", "setPname", "(Ljava/lang/String;)V", false);

    val l2 = new Label();
    mv.visitLabel(l2);
    mv.visitLineNumber(outputLineNumber, l2);
    mv.visitInsn(RETURN);
    val l3 = new Label();
    mv.visitLabel(l3);
    mv.visitLocalVariable("this", s"L${classpackage}/${destination};", null, l0, l3, 0);
    mv.visitMaxs(3, 1);
    mv.visitEnd();

  }

  private def endOutput: Array[Byte] = {
    {
      mv equals cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
      mv.visitCode();
      val l0 = new Label(); val l1 = new Label(); val l2 = new Label();
      mv.visitTryCatchBlock(l0, l1, l2, "java/lang/RuntimeException");
      mv.visitLabel(l0);
      mv.visitLineNumber(13, l0);
      mv.visitMethodInsn(INVOKESTATIC, s"${classpackage}/${destination}", "doLines", "()I", false);
      mv.visitInsn(POP);
      mv.visitLabel(l1);
      mv.visitLineNumber(14, l1);
      val l3 = new Label(); mv.visitJumpInsn(GOTO, l3);
      mv.visitLabel(l2);
      mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, Array("java/lang/RuntimeException"));
      mv.visitVarInsn(ASTORE, 1);
      val l4 = new Label(); mv.visitLabel(l4);
      mv.visitLineNumber(15, l4);
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitFieldInsn(GETSTATIC, "beeb/rpn/Runtime$", "MODULE$", "Lbeeb/rpn/Runtime$;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "beeb/rpn/Runtime$", "currentCommand", "()Ljava/lang/String;", false);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
      val l5 = new Label(); mv.visitLabel(l5);
      mv.visitLineNumber(16, l5);
      mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
      mv.visitVarInsn(ALOAD, 1);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/RuntimeException", "getMessage", "()Ljava/lang/String;", false);
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
      mv.visitLabel(l3);
      mv.visitLineNumber(20, l3);
      mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
      mv.visitInsn(RETURN);
      val l6 = new Label(); mv.visitLabel(l6);
      mv.visitLocalVariable("args", "[Ljava/lang/String;", null, l0, l6, 0);
      mv.visitLocalVariable("r", "Ljava/lang/RuntimeException;", null, l4, l3, 1);
      mv.visitMaxs(2, 2);
      mv.visitEnd();
    }

    {
      mv equals cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "doLines", "()I", null, null);
      mv.visitCode();
      val l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(outputLineNumber, l0);
      mv.visitTypeInsn(NEW, s"${classpackage}/${destination}");
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, s"${classpackage}/${destination}", "<init>", "()V", false);
      mv.visitVarInsn(ASTORE, 0);
    }
    {
      val l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLineNumber(outputLineNumber, l1);
      mv.visitIntInsn(SIPUSH, 9999);
      mv.visitVarInsn(ISTORE, 1);
    }
    val allLocalCalls = new scala.collection.mutable.ArrayBuilder.ofRef[Label]
    var previousLineType = LineType.Normal
    val jumpBackLabel = new scala.collection.mutable.Stack[Label]
    var doneAnAppend = false
    val gotoCalls = new scala.collection.mutable.HashMap[Int, Label]

    val forGotos = gotoLineNumbers
    for (callline <- allLineNumbers.result()) {
      if (forGotos.contains(callline.number)) {
        gotoCalls += (callline.number -> new Label)
      }
    }
    def appendOrSameFrame(notAppend: Int = Opcodes.F_SAME) = {
      if (doneAnAppend == false) {
        mv.visitFrame(Opcodes.F_APPEND, 2, Array(s"${classpackage}/${destination}", Opcodes.INTEGER), 0, null);
        doneAnAppend = true
      } else {
        mv.visitFrame(notAppend, 0, null, 0, null);
      }
    }
    def addGotoCode() = {
      val getValue = new Label();
      mv.visitLabel(getValue);
      mv.visitLineNumber(outputLineNumber, getValue);
      mv.visitFieldInsn(GETSTATIC, "beeb/rpn/Runtime$", "MODULE$", "Lbeeb/rpn/Runtime$;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "beeb/rpn/Runtime$", "getGotoLine", "()I", false);
      mv.visitFieldInsn(PUTSTATIC, s"${classpackage}/${destination}", "_goto", "I");

      val linesArray = gotoCalls.keySet.toArray

      val labelArray = for (i <- 0 to linesArray.length) yield { new Label }

      for (i <- 0 until linesArray.length) {
        val l2 = labelArray(i)
        mv.visitLabel(l2);
        mv.visitLineNumber(outputLineNumber, l2);
        if (i > 0) appendOrSameFrame()
        mv.visitFieldInsn(GETSTATIC, s"${classpackage}/${destination}", "_goto", "I");
        mv.visitIntInsn(BIPUSH, linesArray(i));
        val l3 = labelArray(i + 1)
        mv.visitJumpInsn(IF_ICMPNE, l3);
        val setValue = new Label();
        mv.visitLabel(setValue);
        mv.visitLineNumber(outputLineNumber, setValue);
        mv.visitFieldInsn(GETSTATIC, "beeb/rpn/Runtime$", "MODULE$", "Lbeeb/rpn/Runtime$;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "beeb/rpn/Runtime$", "setGotoLine", "()I", false);
        mv.visitFieldInsn(PUTSTATIC, s"${classpackage}/${destination}", "_goto", "I");
        mv.visitJumpInsn(GOTO, gotoCalls(linesArray(i)))
      }
      val l2 = labelArray(linesArray.length)
      mv.visitLabel(l2);
      mv.visitLineNumber(outputLineNumber, l2);
      if (linesArray.length > 0) appendOrSameFrame()
      val setValue = new Label();
      mv.visitLabel(setValue);
      mv.visitLineNumber(outputLineNumber, setValue);
      mv.visitFieldInsn(GETSTATIC, "beeb/rpn/Runtime$", "MODULE$", "Lbeeb/rpn/Runtime$;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "beeb/rpn/Runtime$", "setGotoLine", "()I", false);
      mv.visitFieldInsn(PUTSTATIC, s"${classpackage}/${destination}", "_goto", "I");

    }

    var gotoInPlay = false
    for (callline <- allLineNumbers.result()) {
      var l1: Label = null
      var whileLoopHeader: WhileInformation = null
      if (callline.ofType == LineType.EndWhile) {
        whileLoopHeader = whileLoopLabels.pop
        l1 = whileLoopHeader.label
      } else {
        l1 = gotoCalls.getOrElse(callline.number, new Label)
      }
      if (previousLineType == LineType.While) {
        jumpBackLabel.push(l1)
      }
      if (previousLineType == LineType.Repeat) {
        jumpBackLabel.push(l1)
      }

      if (callline.ofType == LineType.While) {
        val jump = new Label();
        mv.visitJumpInsn(GOTO, jump);
        allLocalCalls += jump

        whileLoopLabels.push(WhileInformation(callline.number, jump))

      } else {
        allLocalCalls += l1
        mv.visitLabel(l1);
        mv.visitLineNumber(outputLineNumber, l1);

        if (gotoCalls.contains(callline.number) || gotoInPlay == true) {
          gotoInPlay = true
          appendOrSameFrame()
        }
        if (callline.ofType == LineType.For) {
          appendOrSameFrame()
          forLoopLabels.push(l1)
        }
        if (previousLineType == LineType.While) {
          appendOrSameFrame(Opcodes.F_CHOP)
        }
        if (previousLineType == LineType.Repeat) {
          appendOrSameFrame(Opcodes.F_CHOP)
        }
        if (callline.ofType == LineType.EndWhile) {
          mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
        mv.visitVarInsn(ALOAD, 0);
        val invokeLine = if (callline.ofType == LineType.EndWhile) whileLoopHeader.number
        else callline.number

        mv.visitMethodInsn(INVOKEVIRTUAL, s"${classpackage}/${destination}", "_" + invokeLine, "()I", false);
        if (callline.ofType == LineType.Next || callline.ofType == LineType.EndWhile || callline.ofType == LineType.Until) {
          mv.visitInsn(DUP);
        }
        mv.visitVarInsn(ISTORE, 1);
        if (callline.ofType == LineType.Next) {
          val topOfLoop = forLoopLabels.pop
          mv.visitInsn(ICONST_5);
          mv.visitJumpInsn(IF_ICMPEQ, topOfLoop);
        }
        if (callline.ofType == LineType.EndWhile) {
          mv.visitInsn(ICONST_5);
          mv.visitJumpInsn(IF_ICMPEQ, jumpBackLabel.pop());
        }
        if (callline.ofType == LineType.Until) {
          mv.visitInsn(ICONST_5);
          mv.visitJumpInsn(IF_ICMPEQ, jumpBackLabel.pop());
        }
      }
      previousLineType = callline.ofType
      if (callline.goto > 0) {
        addGotoCode
      }
    }

    val l2 = new Label();
    mv.visitLabel(l2);
    mv.visitLineNumber(outputLineNumber, l2);
    mv.visitVarInsn(ILOAD, 1);
    mv.visitInsn(IRETURN);

    val l3 = new Label();
    mv.visitLabel(l3);
    mv.visitLocalVariable("h", s"L${classpackage}/${destination};", null, allLocalCalls.result()(0), l3, 0);
    //mv.visitLocalVariable("r", "[Ljava/lang/String;", null, l0, l3, 0);
    if (allLocalCalls.result().length > 1) {
      mv.visitLocalVariable("r", "I", null, allLocalCalls.result()(1), l3, 1);
    } else {
      mv.visitLocalVariable("r", "I", null, l2, l3, 1);
    }
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    cw.visitEnd();
    return cw.toByteArray();
  }

  private var currentLineNumber = 0
  var currentLineInformation: LineInformation = null
  // create method

  var nextLineType = LineType.Normal
  def setNextLineAsStart(ofType: LineType.Value) = {
    nextLineType = ofType
  }
  def startLine(number: Int) {
    currentLineInformation = LineInformation(number, nextLineType)
    nextLineType = LineType.Normal
    allLineNumbers += currentLineInformation
    currentLineNumber = number;
    mv equals cw.visitMethod(ACC_PUBLIC, "_" + currentLineNumber, "()I", null, null);
    mv.visitCode();

    val setCommand = new Label();
    mv.visitLabel(setCommand);
    mv.visitLineNumber(currentLineNumber, setCommand);
    mv.visitFieldInsn(GETSTATIC, "beeb/rpn/Runtime$", "MODULE$", "Lbeeb/rpn/Runtime$;");
    mv.visitLdcInsn(Runtime.currentCommand);
    mv.visitMethodInsn(INVOKEVIRTUAL, "beeb/rpn/Runtime$", "setCurrentCommand", "(Ljava/lang/String;)V", false);

    allLocalCalls.clear()
    ifJumpDestinationFirst = true
    ifJumps.clear()

    labelGotoEnd = None
    methodLabels.clear()

    valueStackClear()
  }

  //asm to push onto runtime.operationStack ???? Ignore for now dont think its needed
  def operationStackPush(token: String) {
    //IGNORE
  }
  //asm to push value onto runtime.valueStack
  def valueStackPush(d: java.lang.Double) {
    val l0 = new Label();
    methodLabels.push(l0)
    mv.visitLabel(l0);
    mv.visitLineNumber(outputLineNumber, l0);

    mv.visitFieldInsn(GETSTATIC, "beeb/rpn/Runtime$", "MODULE$", "Lbeeb/rpn/Runtime$;");
    mv.visitMethodInsn(INVOKEVIRTUAL, "beeb/rpn/Runtime$", "valueStack", "()Lscala/collection/mutable/Stack;", false);
    mv.visitLdcInsn(d);
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
    mv.visitMethodInsn(INVOKEVIRTUAL, "scala/collection/mutable/Stack", "push", "(Ljava/lang/Object;)Lscala/collection/mutable/Stack;", false);
    mv.visitInsn(POP);

  }
  def valueStackPush(s: String) {

    val l0 = new Label();
    methodLabels.push(l0)
    mv.visitLabel(l0);
    mv.visitLineNumber(outputLineNumber, l0);
    mv.visitFieldInsn(GETSTATIC, "beeb/rpn/Runtime$", "MODULE$", "Lbeeb/rpn/Runtime$;");
    mv.visitMethodInsn(INVOKEVIRTUAL, "beeb/rpn/Runtime$", "valueStack", "()Lscala/collection/mutable/Stack;", false);
    mv.visitLdcInsn(s);
    mv.visitMethodInsn(INVOKEVIRTUAL, "scala/collection/mutable/Stack", "push", "(Ljava/lang/Object;)Lscala/collection/mutable/Stack;", false);
    mv.visitInsn(POP);
  }
  def valueStackClear() {

    val l0 = new Label();
    methodLabels.push(l0)
    mv.visitLabel(l0);
    mv.visitLineNumber(outputLineNumber, l0);
    mv.visitFieldInsn(GETSTATIC, "beeb/rpn/Runtime$", "MODULE$", "Lbeeb/rpn/Runtime$;");
    mv.visitMethodInsn(INVOKEVIRTUAL, "beeb/rpn/Runtime$", "valueStack", "()Lscala/collection/mutable/Stack;", false);
    mv.visitMethodInsn(INVOKEVIRTUAL, "scala/collection/mutable/Stack", "clear", "()V", false);
  }

  // for this line now execute operations
  def doOperations(operations: List[String]) {
    //operations.foreach { opertionOrCommand =>
    for (opertionOrCommand <- operations) {
      doAnOperation(opertionOrCommand)
    }
  }

  def addIfJump(label: Label) = {
    ifJumps push label
  }

  def destinationIfJump: Boolean = {

    if (ifJumps.size > 0) {
      val l6 = ifJumps pop

      mv.visitLabel(l6);
      mv.visitLineNumber(outputLineNumber, l6);

      if (ifJumpDestinationFirst == true)
        mv.visitFrame(Opcodes.F_APPEND, 1, Array(Opcodes.INTEGER), 0, null);
      else
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

      ifJumpDestinationFirst = false
      return true
    } else {
      return false
    }
  }

  def doAnOperation(opertionOrCommand: String) {
    logger.info(s"$currentLineNumber compiler do [ $opertionOrCommand + ]")
    if (opertionOrCommand.startsWith("proc") == true) {
      val l0 = new Label();
      allLocalCalls += l0
      methodLabels.push(l0)
      mv.visitLabel(l0);
      mv.visitLineNumber(outputLineNumber, l0);
      mv.visitMethodInsn(INVOKESTATIC, s"${classpackage}/" + opertionOrCommand, "doLines", "()I", false);
      mv.visitVarInsn(ISTORE, 1);

    } else if (opertionOrCommand == "if") {
      val l0 = new Label();
      allLocalCalls += l0
      methodLabels.push(l0)
      mv.visitLabel(l0);
      mv.visitLineNumber(outputLineNumber, l0);
      mv.visitVarInsn(ILOAD, 1);
      mv.visitInsn(ICONST_M1);

      val l1 = new Label();
      mv.visitJumpInsn(IF_ICMPNE, l1);
      addIfJump(l1)
    } else if (opertionOrCommand == "then") {
      valueStackClear
    } else if (opertionOrCommand == "else") {

      // this should go right to the end of file
      if (labelGotoEnd.isEmpty) {
        labelGotoEnd = Some(new Label)
      }
      val label = labelGotoEnd.get
      mv.visitJumpInsn(org.objectweb.asm.Opcodes.GOTO, label);

      destinationIfJump

      addIfJump(label)

    } else {
      val l0 = new Label();
      allLocalCalls += l0
      methodLabels.push(l0)
      mv.visitLabel(l0);
      mv.visitLineNumber(outputLineNumber, l0);
      mv.visitFieldInsn(GETSTATIC, "beeb/rpn/Commands$", "MODULE$", "Lbeeb/rpn/Commands$;");
      mv.visitLdcInsn(opertionOrCommand);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, s"${classpackage}/${destination}", "localVariables", "Lscala/collection/mutable/Map;");
      mv.visitMethodInsn(INVOKEVIRTUAL, "beeb/rpn/Commands$", "ops", "(Ljava/lang/String;Lscala/collection/mutable/Map;)I", false);
      mv.visitVarInsn(ISTORE, 1);

    }
  }
  def endLine(operations: List[String]) {

    logger.info("END OF LINE");
    doOperations(operations)

    if (false == destinationIfJump) {
      val l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(outputLineNumber, l0);
    }
    mv.visitVarInsn(ILOAD, 1);
    mv.visitInsn(IRETURN);
    val last = new Label();
    mv.visitLabel(last);
    mv.visitLocalVariable("this", s"L${classpackage}/${destination};", null, allLocalCalls.result()(0), last, 0);
    mv.visitLocalVariable("r", "I", null, allLocalCalls.result()(0), last, 1);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

  }

  def writeOut(dir: String) = {
    val out = Some(new FileOutputStream(s"${dir}/${classpackage}/${destination}.class"))
    out.get.write(endOutput)
    out.get.close
  }
}