package info.cmlubinski.newslearning.models


case class TrainingSet(id: Int, name: String, uuid: String, yesLabel:String,
                       noLabel: String)

trait TrainingSetComponent {
  this:DBProfile =>
  import profile.simple._

  class TrainingSets(tag: Tag) extends Table[TrainingSet](tag, "training_set") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def uuid = column[String]("uuid")
    def yesLabel = column[String]("yes_label")
    def noLabel = column[String]("no_label")
    
    def * = (id, name, uuid, yesLabel, noLabel) <> (TrainingSet.tupled, TrainingSet.unapply)
  }

  val trainingSets = TableQuery[TrainingSets]
}
