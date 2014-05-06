package info.cmlubinski.newslearning.models

import com.typesafe.config.ConfigFactory
import scala.slick.driver._
import scala.slick.jdbc.JdbcBackend.Database

trait DBProfile {
  val profile: JdbcProfile
}

object DB extends ArticleComponent with CacheComponent
  with TrainingSetComponent with DBProfile {
  lazy val config = ConfigFactory.load()
  lazy val dbUrl = config.getString("db.url")
  lazy val dbUsername = {
    if (config.hasPath("db.username")) config.getString("db.username")
    else null
  }
  lazy val dbPassword = {
    if (config.hasPath("db.password")) config.getString("db.password")
    else null
  }
  override lazy val profile:JdbcProfile = dbUrl.split(":")(1) match {
    case "derby" => DerbyDriver
    case "hsqldb" => HsqldbDriver
    case "mysql" => MySQLDriver
    case "postresql" => PostgresDriver
    case "sqlite" => SQLiteDriver
    case _ => H2Driver
  }
  lazy val driver = dbUrl.split(":")(1) match {
    case "derby" => "org.apache.derby.jdbc.EmbeddedDriver"
    case "hsqldb" => "org.hsqldb.jdbc.JDBCDriver"
    case "mysql" => "com.mysql.jdbc.Driver"
    case "postresql" => "org.postgresql.Driver"
    case "sqlite" => "org.sqlite.JDBC"
    case _ => "org.h2.Driver"
  }
  lazy val imports = profile.simple

  def createSession = Database.forURL(
    dbUrl, user=dbUsername, password=dbPassword, driver=driver).createSession
}
