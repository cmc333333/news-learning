package info.cmlubinski.newslearning.models

case class Article(url: String, title: String, body: String)

trait ArticleComponent {
  this:DBProfile =>
  import profile.simple._

  class Articles(tag: Tag) extends Table[Article](tag, "article") {
    def url = column[String]("url", O.PrimaryKey)
    def title = column[String]("title")
    def body = column[String]("body")

    def * = (url, title, body) <> (Article.tupled, Article.unapply)
  }
  val articles = TableQuery[Articles]
}
