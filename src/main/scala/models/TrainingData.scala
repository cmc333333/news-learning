package info.cmlubinski.newslearning.models


trait TrainingDataComponent extends TrainingSetComponent with ArticleComponent {
  this:DBProfile =>
  import profile.simple._

  class TrainingData(tag: Tag) extends Table[(Int, String, Boolean)](
    tag, "training_data") {
    def training_set_id = column[Int]("training_set_id")
    def article_id = column[String]("article_id")
    def value = column[Boolean]("value")

    def trainingSet = foreignKey("training_data_set", training_set_id,
                                 trainingSets)(_.id)
    def article = foreignKey("training_article", article_id, articles)(_.url)

    def * = (training_set_id, article_id, value)
  }

  val trainingData = TableQuery[TrainingData]
}
