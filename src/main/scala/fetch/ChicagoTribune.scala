package info.cmlubinski.newslearning.fetch

import scala.collection.JavaConversions._

import dispatch._, Defaults._


object ChicagoTribune {
  val TRIB_URL = "http://www.chicagotribune.com"
  val RSS_URL = TRIB_URL + "/news/local/breaking/rss2.0.xml"

  def fetchLatest() = {
    for (feed <- Http(url(RSS_URL) OK as.xml.Elem);
         item <- feed \\ "item";
         link <- item \\ "link";
         wholeArticle <- Http(url(link.text) OK as.jsoup.Document);
         print_button <- wholeArticle.select("a[title=print]").take(1);
         href = print_button.attr("href");
         print_url = if (href.take(1) == "/") TRIB_URL + href else href;
         soup <- Http(url(print_url) OK as.jsoup.Document)) {
      val text = soup.select("body > div > p").map(_.text).mkString("\n")
      println(text.take(70))
    }
  }
}
