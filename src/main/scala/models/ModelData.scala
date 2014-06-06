package info.cmlubinski.newslearning.models

import java.io.{ByteArrayInputStream, ObjectInputStream}

import nak.core.FeaturizedClassifier


case class ModelType(id:Int, slug: String, description:String)
case class ModelDatum(training_set_id:Int, model_type:Int,
                      serialized:Array[Byte]) {
  lazy val featurizer:FeaturizedClassifier[String,Article] = {
    val byteStream = new ByteArrayInputStream(serialized)
    new ObjectInputStream(byteStream)
        .readObject.asInstanceOf[FeaturizedClassifier[String,Article]]
  }
}

trait ModelDataComponent extends TrainingSetComponent {
  this:DBProfile =>
  import profile.simple._

  class ModelData(tag: Tag) extends Table[ModelDatum](tag, "model_data") {
    def training_set_id = column[Int]("training_set_id")
    def model_type = column[Int]("model_type")
    def serialized = column[Array[Byte]]("serialized")

    def trainingSet = foreignKey("training_data_set", training_set_id,
                                 trainingSets)(_.id)
    def modelType = foreignKey("model_type", model_type, modelTypes)(_.id)
    
    def * = ((training_set_id, model_type, serialized) 
             <> (ModelDatum.tupled, ModelDatum.unapply))
  }

  class ModelTypes(tag: Tag) extends Table[ModelType](tag, "model_type") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def slug = column[String]("slug")
    def description = column[String]("description")

    def * = (id, slug, description) <> (ModelType.tupled, ModelType.unapply)
  }

  val modelData = TableQuery[ModelData]
  val modelTypes = TableQuery[ModelTypes]
}
