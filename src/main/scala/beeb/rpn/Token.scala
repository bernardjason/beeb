package beeb.rpn

object Token {
     
  implicit class BasicParser(s: String) {
    def commandsplit = {
      val word = new StringBuilder
      val previous = new StringBuilder
      var inquotes = false;
      var quoteused:Char = 0 
      def splitfn(c: Char): (Boolean, String, String) = {
        if (inquotes == false && c == ' ') {
          val r = word.mkString
          word.clear()
          return (true, r, null);
        }
        //close quotes?
        if (inquotes == true && c == quoteused) {
          inquotes = false
          val r = word.mkString.replace("\\n","\n").replace("\\r", "\r")
          word.clear()
          return (true, r, null);
        }
        //open quotes
        if (inquotes == false && (c == '\'' || c == '"') ) {
          inquotes = true
          quoteused=c
          word.append(c)
          return (false, null, null)
        }
        // !=-3
        // 3-3
        // for things like 30/5. where a operator splits values
        if (inquotes == false && Commands.validops.contains("" + c)) {
          val r = word.mkString
          //println(c,previous, "  ",Commands.arithmeticCommand.contains(previous.toString()))
          if ( c == '-' && Commands.arithmeticCommand.contains(previous.toString) ) {
            //println("*** ",r,"  ",previous)
            word.clear()
            word.append(c);
            return (true, r, null);
          } else {
            word.clear()
            return (true, r, c.toString());
          }

        }
        word.append(c)
        if (inquotes == false && Commands.validops.contains(word.mkString)) {
          val r = word.mkString
          word.clear()
          return (true, r, null);
        }
        return (false, null, null);
      } // end splitfn
      //val result = new scala.collection.mutable.ArrayBuilder.ofRef[String]
      val result = new scala.collection.mutable.ArrayBuffer[String]

      var cc: Tuple3[Boolean, String, String] = null
      def dochar(c: Char) {
        cc = splitfn(c);
        if (cc._1 == true) {
          if (cc._2 != null && cc._2.length() > 0) result += cc._2
          if (cc._3 != null && cc._3.length() > 0) result += cc._3
          previous.clear()
          previous.append( result.last)
          //println("XXX" , previous)
        }
      }
      s.map(c => { dochar(c) })
      if (word.length > 0) result += word.mkString
    
      if ( result.size > 1 ) {
        val specialOnes = List("<",">","!","=")
        var i=1
        while( i < result.size) {
          if ( specialOnes.contains(result(i)) && specialOnes.contains(result(i-1)) ) {
            result(i-1) = result(i-1)+result(i)
            result.remove(i)
          }
          i=i+1
        }
      }
      result.result()
    }
    def isvalue: Boolean = {
      val c = s.charAt(0)
      if (c >= '0' && c <= '9' || c == '-' ) return true;
      if (c == '"' || c == '\'' ) return true;
      return false;
    }
    def isnumeric: Boolean = {
      if ( s.length() == 0 ) return false;
      val c = s.charAt(0)
      if (c >= '0' && c <= '9' || c == '-' ) return true;
      return false;
    }
  }

  
}