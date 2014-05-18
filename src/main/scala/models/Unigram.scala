package info.cmlubinski.newslearning.models

case class Unigram(id:Int, word:String)

trait UnigramComponent extends ArticleComponent {
  this:DBProfile =>
  import profile.simple._

  class Unigrams(tag: Tag) extends Table[Unigram](tag, "unigram") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def word = column[String]("word")

    def * = (id, word) <> (Unigram.tupled, Unigram.unapply)
  }
  val unigrams = TableQuery[Unigrams]

  class ArticleUnigrams(tag: Tag) extends Table[(Int, Int, Int)](
    tag, "article_unigram") {
    def article_id = column[Int]("article_id")
    def unigram_id = column[Int]("unigram_id")
    def count = column[Int]("count")

    def article = foreignKey("article", article_id, articles)(_.id)
    def unigram = foreignKey("unigram", unigram_id, unigrams)(_.id)

    def * = (article_id, unigram_id, count)
  }

  val articleUnigrams = TableQuery[ArticleUnigrams]
}
