package info.cmlubinski.newslearning.learn

import java.io.{ByteArrayOutputStream, ObjectOutputStream}

import chalk.text.analyze.PorterStemmer
import chalk.text.segment.JavaSentenceSegmenter
import chalk.text.tokenize.SimpleEnglishTokenizer
import nak.data.{Example, Featurizer}
import nak.liblinear.LiblinearConfig
import nak.NakContext.trainClassifier

import info.cmlubinski.newslearning.models.{Article, DB, ModelDatum}


trait LearnBase {
  import DB.imports._

  def toSentences(text:String):Iterable[String] = JavaSentenceSegmenter(text)
  def toWords(sent:String):Iterable[String] = {
    val tokenizer = SimpleEnglishTokenizer()
    tokenizer(sent)
  }
  def stem(word:String):String = PorterStemmer()(word)

  val featurizer:Featurizer[Article, String]
  val slug:String

  def main(args:Array[String]) {
    DB.withTransaction {
      implicit session =>

      DB.modelTypes.filter(_.slug === slug).firstOption match {
        case None => println("No " + slug + " model")
        case Some(modelType) =>
          for (trainingSet <- DB.trainingSets) {
            val labXarts = for (data <- DB.trainingData;
                                article <- DB.articles
                                if data.article_id === article.id
                                && data.training_set_id === trainingSet.id)
                           yield (data.value, article)

            val examples = labXarts.list.map{
              case (label, article) => Example(label.toString, article,
                                               article.id.toString)
            }

            if (examples.nonEmpty) {
              val config = LiblinearConfig()
              val classifier = trainClassifier(config, featurizer, examples)

              val byteStream = new ByteArrayOutputStream()
              new ObjectOutputStream(byteStream).writeObject(classifier)
              byteStream.close()

              DB.modelData.filter(md => 
                  md.training_set_id === trainingSet.id
                  && md.model_type === modelType.id).delete
              DB.modelData += ModelDatum(trainingSet.id, modelType.id,
                                         byteStream.toByteArray())
            }
          }
      }
    }
  }
}
