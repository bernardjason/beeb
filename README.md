# Bernard's Enterprise Edition Basic

This project is a rough and limited attempt at implementing something like BBC Basic on the Java Virtual Machine. There are plenty of things missed out as well as a couple of extras like making it capable of being a webserver, database access and some JSON support.

to build (you must have JDK1.8)

./activator assembly

to start the REPL (https://en.wikipedia.org/wiki/Read%E2%80%93eval%E2%80%93print_loop)
```
java -jar target/scala-2.11/beeb-assembly-1.0-SNAPSHOT.jar
```

Note the REPL does not suppport loop constructs or if then else. Compiled code supports all of the below.

to play hangman based on  BBC Basic userguide example
```
load hangman
run
```

or to try the webserver with a simple blog website
```
load motd
run
```

to call the rest endpoints for the motd blog site.
```
curl --noproxy '*' http://127.0.0.1:8080/rest
curl --noproxy '*' -X POST --Header 'Content-type:application/json' --data "{\"text\":\"hello world at $(date)\"}" http://127.0.0.1:8080/rest
```
or just go to the webpage
http://127.0.0.1:8080

So just again, at a minimum
```
./activator assembly
java -jar target/scala-2.11/beeb-assembly-1.0-SNAPSHOT.jar
load hangman
quit
run and load compile the code to a directory basic/classes so afterwards you can do this to run hangman
java -cp basic/classes:target/scala-2.11/beeb-assembly-1.0-SNAPSHOT.jar hangman.BasicMain
```

Some brief information on the commands supported.

## procedures
Procedures are supported in a nod to BBC Basic
```10 number=10
20 print number
30 procshow
40 print number
130 defproc show
140 local number
150 number=10000
160 print number
170 endproc
```

## dbopen <driver> <driver database url connection> <username> <password>
  open a database using java jdbc driver and return a descriptor for future SQL operations
example:
```
  D=dbopen "org.sqlite.JDBC" "jdbc:sqlite:motd.db" "sa" "sa"
```

## dbexecute <descriptior> <string command>
	Execute a piece of Data Definition Language against the database defined by <descriptior>
example:
```
	"create table motd ( id INTEGER PRIMARY KEY AUTOINCREMENT, text varchar(64), created_at DEFAULT CURRENT_TIMESTAMP NOT NULL)"
```

## dbrows <descriptor>
  return the number of rows from the previous select sql.
example:
```
  dbrows(D)
```

## dbclose <descriptor>
  close an already open database
example
  dbclose D

## dbinsert <descriptor> <sql string>
  insert into already open database. Expects a valid piece of SQL in the provided string
example:
```
  dbinsert (D,"insert into motd(text) values ('hello there')")
```

## dbselect <descriptor> <sql string> <number of fields to return> <name of array to hold rows of data>
  to select from a database query into an already defined array. You dont need to worry about the dimensions for the dim. But must define
the number of columns for the returned database table columns.
example:
```
  dim result(0,0)
  dbselect (D,"select * from motd",3,"result")
```

## dbupdate <descriptor> <sql string>
  update into already open database. Expects a valid piece of SQL in the provided string
example:
```
  dbupdate (D,"update motd set text = 'fred' where id < 10")
```

## dbdelete <descriptor> <sql string>
  dekete rows from an already open database. Expects a valid piece of SQL in the provided string
example:
```
  dbupdate (D,"delete from motd where id < 10")
```

## fopenout <filename>
  (see writer.basic) Opens a file for writing
example:
```
  x=fopenout "singer"
```

## fopenin <filename>
  (see drinks.basic) . Opens a file for reading
example:
```
  Y=fopenin "DRINKS.TXT"
```

## fclose <descriptor>
  Closes a file previously opened.

## fprint <descriptor> <..... things to write out>
  Write to a file. First param is the file descriptor then anything else afterwards
example:
```
  print x "this is a test " $HI
```

## finput <descriptor> <read into variable>
  Read from descriptor into a file

## feof <descriptor>
  check if a file previously opened referenced by <descriptor> is at end of file.
  
## getjson <query> <string of json text>
	(see json.basic) using <query> extract from json vaile from <string>. the query. The query can cope with json that includes arrays. If the query is a numeric it assumes it is an array reference otherwise the path to a json element
exemple:
```
  getjson "residents 0 name" json$
```

## Comparison operators 
  < > != <= >= == and or not    are used in if then statements. Note unlike Basic I've used == for equality check

## Maths operations
  + - / *

## print <multiple arguments>
  display text and variable to the console. You simply list all the values you want to display.
example:
```
  print "this is a test for user " NAME$ " you have passed the test"
```

## tab (column,row)
  move the cursor to column,row for output. Use with print
example:
```
  print tab(20,0) "CORRECT " CORRECT
```

## cls
  clear screen

## input <destination variable>
  read from console and store in <variable>
example:
```
  input d$
```

## local
  make a variable local to a procedure. See showlocal.basic.
example:
```
  local num
```

## if then else
  make a choice based on a condition. The then and else outcomes are single statements only. 
example:
```
if I == 10 then goto 70 else goto 20
if num == 5 then print "FIVE"
if httpurl like "/users" then procgetuser
```

## dim
  create an array ready for use
example
```
dim B(30,30)
dim single(100)
```

## for next to step
  for next loop. supports inner loops. See forloop.basic
example:
```
10 for y = 1 to 4
20 for x = 2 to 10 step 2
30 print y "  " x
40 next x
50 next y
```


## while endwhile
  flow control while endwhile structure. Condition is evaluated before loop entered. see whiledemo.basic.
#example
```
while j < 5
j=j+1
endwhile
```

## repeat until
  flow control repeate until structure. Condition is evaluated at end of loop so done at least once. See repeatuntil.basic
example:
```
repeat
print i "   " j
j=j+1
until j > 4
```

## rnd <range>
  generated a number between 0 and <range>
example:
```
  print rnd(10)
```

## len
  length of a string
example:
```
  print len("jack")
```

## instr <search string> <look for> <start at position>
 	search a string for another string starting at position defined. 
example:
```
print instr("HELLO","E",0)
returns 2
```

## get$,
  read a character. You need to press return though!
example:
```
G$=get$
```

## startweb
  start a webserver running on port 8080. See motd.basic

## stopweb
  stop a webserver. See motd.basic

## sleep <period>
  sleep for a defined period.

## like
  provides a search for a string. Checks is string has a match in another one with regexp
example
```
10 X="say hello and wave bye then"
20 if X like "^.*bye.*$" then print "bye"
```

## goto
  Yes you can goto lines. See goto.basic
