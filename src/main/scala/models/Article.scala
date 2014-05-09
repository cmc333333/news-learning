package info.cmlubinski.newslearning.models

case class Article(id:Int, url: String, title: String, body: String)

trait ArticleComponent {
  this:DBProfile =>
  import profile.simple._

  class Articles(tag: Tag) extends Table[Article](tag, "article") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def url = column[String]("url")
    def title = column[String]("title")
    def body = column[String]("body")

    def * = (id, url, title, body) <> (Article.tupled, Article.unapply)
  }
  val articles = TableQuery[Articles]
}
