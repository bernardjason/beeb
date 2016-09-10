package asm.beeb.play;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
public class OutputSimple implements Opcodes {

	public static void main(String[] args) throws Exception {
	    

        File outputDir=new File("./target/classes/beeb/play");
        outputDir.mkdirs();
        DataOutputStream dout=new DataOutputStream(new FileOutputStream(new File(outputDir,"MySimple.class")));
        dout.write(dump());
        dout.close();

    }

	public static byte[] dump () throws Exception {

		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		cw.visit(52, ACC_PUBLIC + ACC_SUPER, "beeb/play/MySimple", null, "java/lang/Object", null);

		cw.visitSource("MySimple.java", null);

		{
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(3, l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitInsn(RETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", "Lbeeb/play/MySimple;", null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		}
		{
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(6, l0);
		mv.visitMethodInsn(INVOKESTATIC, "beeb/play/MySimple", "_10", "()V", false);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLineNumber(7, l1);
		mv.visitInsn(RETURN);
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, l0, l2, 0);
		mv.visitMaxs(0, 1);
		mv.visitEnd();
		}
		{
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "_10", "()V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(10, l0);
		mv.visitMethodInsn(INVOKESTATIC, "beeb/rpn/Runtime", "operationStack", "()Lscala/collection/mutable/Stack;", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, "scala/collection/mutable/Stack", "pop", "()Ljava/lang/Object;", false);
		mv.visitTypeInsn(CHECKCAST, "java/lang/String");
		mv.visitVarInsn(ASTORE, 0);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLineNumber(11, l1);
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		mv.visitLdcInsn("HELLO WORLD");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitLineNumber(12, l2);
		mv.visitInsn(RETURN);
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitLocalVariable("xx", "Ljava/lang/String;", null, l1, l3, 0);
		mv.visitMaxs(2, 1);
		mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
		}
}
