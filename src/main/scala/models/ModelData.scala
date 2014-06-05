package info.cmlubinski.newslearning.models


trait ModelDataComponent extends TrainingSetComponent {
  this:DBProfile =>
  import profile.simple._

  class ModelData(tag: Tag) extends Table[(Int, Int, Array[Byte])](
    tag, "model_data") {
    def training_set_id = column[Int]("training_set_id")
    def model_type = column[Int]("model_type")
    def serialized = column[Array[Byte]]("serialized")

    def trainingSet = foreignKey("training_data_set", training_set_id,
                                 trainingSets)(_.id)
    
    def * = (training_set_id, model_type, serialized)
  }

  val modelData = TableQuery[ModelData]
}
