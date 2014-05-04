package info.cmlubinski.newslearning.fetch

import scala.concurrent.Future

import dispatch._, Defaults._
import com.ning.http.client
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import info.cmlubinski.newslearning.models.{CacheEntry, DB}

object HttpCache {
  import DB.imports._

  def proxy(urlStr:String)(implicit session:Session):Future[Document] = {
    DB.cache.filter(_.url === urlStr).list match {
      case CacheEntry(body, url) :: _ => Future { Jsoup.parse(body, url) }
      case nil => for (result <- fetch(urlStr)) yield {
        DB.cache += CacheEntry(urlStr, result.toString)
        result
      }
    }
  }
  def fetch[T](urlStr:String,
               trans:(client.Response=>T)=as.jsoup.Document):Future[T] = {
    Http.configure(_ setFollowRedirects true)(url(urlStr) OK trans)
  }
}
