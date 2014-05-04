package info.cmlubinski.newslearning.models

case class CacheEntry(url: String, body: String)

trait CacheComponent {
  this:DBProfile =>
  import profile.simple._

  class CacheEntries(tag: Tag) extends Table[CacheEntry](tag, "http_cache") {
    def url = column[String]("url", O.PrimaryKey)
    def body = column[String]("body")

    def * = (url, body) <> (CacheEntry.tupled, CacheEntry.unapply)
  }

  val cache = TableQuery[CacheEntries]
}
