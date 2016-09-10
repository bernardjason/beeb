package beeb.rpn

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes._
import java.io.FileOutputStream
import org.objectweb.asm.Opcodes
import com.typesafe.scalalogging.StrictLogging

class AsmWrapper extends StrictLogging {

 val cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

 var mv:MethodVisitor = null;

 def equals(mv:MethodVisitor) {
   this.mv = mv;
 }

 def visitCode() = {
   logger.info("mv.visitCode()");
   mv.visitCode();
 }
 def visitLabel(label:Label) = {
   logger.info(s"mv.visitLabel(${label})");
   mv.visitLabel(label);
 }
 def visitLineNumber(line:Int,label:Label) = {
   logger.info(s"mv.visitLineNumber($line,$label)");
   mv.visitLineNumber(line, label)
 }
 def visitVarInsn(arg0:Int,arg1:Int) = {
   logger.info(s"mv.visitVarInsn($arg0,$arg1)");
   mv.visitVarInsn(arg0, arg1);
 }
 def visitMethodInsn(arg0:Int,arg1:String,arg2:String,arg3:String,arg4:Boolean) = {
   logger.info(s"mv.visitMethodInsn($arg0,$arg1,$arg2,$arg3,$arg4)");
  mv.visitMethodInsn(arg0, arg1, arg2, arg3,arg4) 
 }
 def visitFieldInsn(arg0:Int,arg1:String,arg2:String,arg3:String) = {
   logger.info(s"mv.visitFieldInsn($arg0,$arg1,$arg2,$arg3)");
   mv.visitFieldInsn(arg0, arg1, arg2, arg3)
 }
 def visitInsn(arg0:Int) = {
   logger.info(s"mv.visitInsn($arg0)");
   mv.visitInsn(arg0)
 }
 def visitLocalVariable(arg0: String, arg1: String, arg2: String, arg3: Label, arg4: Label, arg5: Int) = {
   logger.info(s"mv.visitLocalVariable($arg0,$arg1,$arg2,$arg3,$arg4,$arg5)");
   mv.visitLocalVariable(arg0, arg1, arg2, arg3, arg4, arg5)
 }
 def visitMaxs(arg0:Int,arg1:Int) = {
   logger.info(s"mv.visitMaxs($arg0,$arg1)");
    mv.visitMaxs(arg0, arg1) 
 }
 def visitEnd() = {
   logger.info(s"mv.visitEnd()");
   mv.visitEnd();
 }
 def visitTypeInsn(arg0:Int,arg1:String) = {
   logger.info(s"mv.visitTypeInsn($arg0,$arg1)");
   mv.visitTypeInsn(arg0, arg1)
 }
 
 def visitLdcInsn(arg0:Any) = {
   logger.info(s"mv.visitLdcInsn($arg0)");
   mv.visitLdcInsn(arg0)
 }
 def visitJumpInsn(arg0:Int,arg1:Label) = {
   logger.info(s"mv.visitJumpInsn($arg0,$arg1)");
   mv.visitJumpInsn(arg0, arg1)
 }
 
 def visitIntInsn(arg0: Int, arg1: Int) = {
   logger.info(s"mv.visitIntInsn($arg0,$arg1)");
   mv.visitIntInsn(arg0, arg1)
 }
 def visitFrame(arg0: Int, arg1: Int, arg2: Array[Object], arg3: Int, arg4: Array[Object]) = {
   val display = arg0 match {
     case 1 => "F_APPEND"
     case 2 => "F_CHOP"
     case 3 => "F_SAME"
     case _ => ""+arg0
   }
   logger.info(s"mv.visitFrame($display,$arg1,$arg2,$arg3,$arg4)");
   mv.visitFrame(arg0, arg1, arg2, arg3, arg4)
 }
 def visitLookupSwitchInsn(arg0:Label,arg1:Array[Int],arg2:Array[Label]) = {
   logger.info(s"mv.visitLookupSwitchInsn($arg0,$arg1,$arg2)");
   mv.visitLookupSwitchInsn(arg0,arg1,arg2)
 }
 def visitTryCatchBlock(arg0:Label,arg1:Label,arg2:Label,arg3:String) = {
   logger.info(s"mv.visitTryCatchBlock($arg0,$arg1,$arg2,$arg3)");
   mv.visitTryCatchBlock(arg0, arg1, arg2, arg3)
 }
}