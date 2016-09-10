package beeb.rpn
import scalikejdbc._
import scala.collection.mutable.ArrayBuffer

case class Database(driver:String,url:String,user:String,password:String) {
 
  
GlobalSettings.loggingSQLAndTime = new LoggingSQLAndTimeSettings(
  enabled = true,
  singleLineMode = true,
  logLevel = 'DEBUG
)
  
Class.forName(driver)

ConnectionPool.singleton(url, user,password)

implicit val session = AutoSession

case class Bucket(values:List[String]) 

object Bucket extends SQLSyntaxSupport[Bucket] {
  //override val tableName = dbtable
  def apply(rs: WrappedResultSet,fields:Int) = {
    val values = ArrayBuffer[String]()
    for( i <- 1 to fields ) {
      values += rs.string(i)
    }
    val b = new Bucket(values.toList)
    b
  }
}
var currentQuery:Option[List[Bucket]] = None
def dselect(q:String,fields:Int) = {
   currentQuery = Some(SQL(q).map(rs => Bucket(rs,fields)).list.apply)
}

def dupdate(i:String) =  SQL(i).update.apply() 
def dexecute(i:String) =  SQL(i).execute.apply() 
def dclose() =  session.close()


}