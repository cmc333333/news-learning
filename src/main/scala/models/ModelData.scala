package info.cmlubinski.newslearning.models

import java.io.{ByteArrayInputStream, ObjectInputStream}

import nak.core.FeaturizedClassifier


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
    
    def * = ((training_set_id, model_type, serialized) 
             <> (ModelDatum.tupled, ModelDatum.unapply))
  }

  val modelData = TableQuery[ModelData]
}
