package info.cmlubinski.newslearning.fetch

import scala.collection.JavaConversions._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

import dispatch._, Defaults._

import info.cmlubinski.newslearning.models.{Article, DB}


object ChicagoTribune {
  import DB.imports._
  val TRIB_URL = "http://www.chicagotribune.com"
  val RSS_URL = TRIB_URL + "/news/local/breaking/rss2.0.xml"

  def fetchAndSaveLatest():Future[Seq[Article]] = {
    val cache = new HttpCache()
    for (feed <- cache.fetch(RSS_URL, as.xml.Elem)) yield {
      implicit val session = DB.createSession
      val items = for (item <- feed \\ "item") yield Future{
        val title = (item \\ "title")(0).text
        val link = (item \\ "link")(0).text
        val article = for (soup <- cache.proxy(link)) yield {
          val body = soup.select("#story-body-text > p").map(_.text)
                         .mkString("\n")
          val art = Article(0, link, title, body)
          val previous = for { a <- DB.articles if a.url === link} 
                         yield (a.title, a.body)
          if (previous.exists.run) {
            previous.update((art.title, art.body))
          } else {
            DB.articles += art
          }
          art
        }
        article()
      }
      val resultsFuture = Future.sequence(items)
      val results = resultsFuture()
      session.close
      results
    }
  }

  def main(args:Array[String]) {
    val results = ChicagoTribune.fetchAndSaveLatest()
    println(results().map(_.title))
    System.exit(0)
  }
}
