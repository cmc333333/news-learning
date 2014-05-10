package info.cmlubinski.newslearning.models


case class TrainingSet(id: Int, name: String, uuid: String)

trait TrainingSetComponent {
  this:DBProfile =>
  import profile.simple._

  class TrainingSets(tag: Tag) extends Table[TrainingSet](tag, "training_set") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def uuid = column[String]("uuid")
    
    def * = (id, name, uuid) <> (TrainingSet.tupled, TrainingSet.unapply)
  }

  val trainingSets = TableQuery[TrainingSets]
}
