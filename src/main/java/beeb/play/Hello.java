package beeb.play;

import scala.collection.mutable.Map;
import beeb.rpn.Runtime$;

public class Hello {

	Map<String, Object> localVariables = Runtime$.MODULE$.getLocalVariableStack();

	static int _goto=1;
	public static void main(String[] args) {
		try {
		doLines();
		} catch (RuntimeException r ) {
			System.out.println(Runtime$.MODULE$.currentCommand());
			System.out.println(r.getMessage());
		}
	}
	

	static int doLines() {
		Hello h = new Hello();
		int r=9999;
		if ( _goto == 10 ) System.out.println(_goto); 
		if ( _goto == 20 ) System.out.println(10); 
		r=h._30();
		return r;
	}
	public int _10() {
		_goto = beeb.rpn.Runtime$.MODULE$.getGotoLine();
		//beeb.rpn.Runtime.valueStack().clear();
		//beeb.rpn.Runtime.valueStack().push("first");
		//beeb.rpn.Runtime$.MODULE$.valueStack().push("HELLO");
		//beeb.rpn.Runtime.valueStack().push("second");
		//System.out.println("HELLO WORLD");
		//Runtime$.MODULE$.valueStack().push( "hello");
		//String s = (String) Runtime$.MODULE$.valueStack().pop();
		//System.out.println(s);
		int r=0;
		if ( r == -1 ) { 
			//beeb.rpn.Runtime$.MODULE$.valueStack().push("HELLO");
			//r=beeb.rpn.Commands$.MODULE$.ops("+", localVariables);
			System.out.println("THEN");
		} else {
			//r=beeb.rpn.Commands$.MODULE$.ops("+", localVariables);
			System.out.println("ELSE");
			
		}
		return r;
	}
	public int _20() {
		//beeb.rpn.Runtime.valueStack().push( new java.lang.Double(20d) );
		//beeb.rpn.Runtime.valueStack().push("first");
		//beeb.rpn.Runtime.valueStack().push("second");
		//System.out.println("HELLO WORLD");
		//Commands.ops("X");
		return 0;
	}
	public int _30() {
		return 0;
	}
	
}