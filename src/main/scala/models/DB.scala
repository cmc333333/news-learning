package info.cmlubinski.newslearning.models

import com.typesafe.config.ConfigFactory
import scala.slick.driver._
import scala.slick.jdbc.JdbcBackend.Database

trait DBProfile {
  val profile: JdbcProfile
}

object DB extends ArticleComponent with CacheComponent with DBProfile {
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
    case "h2" => H2Driver
    case "hsqldb" => HsqldbDriver
    case "mysql" => MySQLDriver
    case "postres" => PostgresDriver
    case _ => SQLiteDriver
  }
  lazy val driver = dbUrl.split(":")(1) match {
    case "derby" => "org.apache.derby.jdbc.EmbeddedDriver"
    case "h2" => "org.h2.Driver"
    case "hsqldb" => "org.hsqldb.jdbc.JDBCDriver"
    case "mysql" => "com.mysql.jdbc.Driver"
    case "postres" => "org.postgresql.Driver"
    case _ => "org.sqlite.JDBC"
  }
  lazy val imports = profile.simple

  def createSession = Database.forURL(
    dbUrl, user=dbUsername, password=dbPassword, driver=driver).createSession
}
