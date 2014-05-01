package info.cmlubinski.newslearning.fetch

import scala.collection.JavaConversions._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.slick.driver.H2Driver.simple._

import dispatch._, Defaults._

import info.cmlubinski.newslearning.models.HttpCache


object ChicagoTribune {
  val TRIB_URL = "http://www.chicagotribune.com"
  val RSS_URL = TRIB_URL + "/news/local/breaking/rss2.0.xml"

  def fetchLatest() {
    for (feed <- HttpCache.fetch(RSS_URL, as.xml.Elem)) {
      for (item <- feed \\ "item") Future{
        val title = (item \\ "title")(0).text
        val link = (item \\ "link")(0).text
        implicit val session = Database.forURL("jdbc:h2:file:/tmp/db",
                        driver="org.h2.Driver").createSession
        val article = for (soup <- HttpCache.proxy(link)) yield {
          println(title)
          val body = soup.select("#story-body-text > p").map(_.text)
                         .mkString("\n")
          (link, title, body)
        }
        Await.ready(article, Duration.Inf)
        session.close
      }
    }
  }
}
