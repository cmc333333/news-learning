package info.cmlubinski.newslearning.models

import scala.concurrent.Future

import dispatch._, Defaults._
import com.ning.http.client
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.slick.driver.H2Driver.simple._

class HttpCache(tag: Tag) extends Table[(String, String)](tag, "HTTP_CACHE") {
  def url = column[String]("URL", O.PrimaryKey)
  def body = column[String]("BODY")

  def * = (url, body)
}

object HttpCache {
  val cache = TableQuery[HttpCache]
  def proxy(urlStr:String)(implicit session:Session):Future[Document] = {
    val existing = cache.filter(_.url === urlStr).list 
    existing match {
      case (body, url) :: _ => Future { Jsoup.parse(body, url) }
      case nil => for (result <- fetch(urlStr)) yield {
        cache += (urlStr, result.toString)
        result
      }
    }
  }
  def fetch[T](urlStr:String,
               trans:(client.Response=>T)=as.jsoup.Document):Future[T] = {
    println("Fetching " + urlStr)
    Http.configure(_ setFollowRedirects true)(url(urlStr) OK trans)
  }
}
