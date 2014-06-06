package info.cmlubinski.newslearning.learn

import java.io.{ByteArrayOutputStream, ObjectOutputStream}
import scala.collection.mutable

import chalk.text.analyze.PorterStemmer
import chalk.text.segment.JavaSentenceSegmenter
import chalk.text.tokenize.SimpleEnglishTokenizer
import nak.data.{Example, Featurizer, FeatureObservation}
import nak.liblinear.LiblinearConfig
import nak.NakContext.trainClassifier

import info.cmlubinski.newslearning.models.{Article, DB, ModelDatum, ModelType}


trait UnigramBase {
  import DB.imports._

  def toSentences(text:String):Iterable[String] = JavaSentenceSegmenter(text)
  def toWords(sent:String):Iterable[String] = {
    val tokenizer = SimpleEnglishTokenizer()
    tokenizer(sent)
  }
  def stem(word:String):String = PorterStemmer()(word)

  def wordCounts(text:String):Map[String, Int] = {
    val wordCounts = mutable.Map[String, Int]()
    for (sentence <- toSentences(text);
         word <- toWords(sentence.trim)
         if word.matches("^[a-z]+$")) {
      val stemmed = stem(word)
      wordCounts += (stemmed -> (wordCounts.getOrElse(stemmed, 0) + 1))
    }
    wordCounts.toMap
  }

  val featurizer:Featurizer[Article, String]
  val slug:String

  def main(args:Array[String]) {
    DB.withTransaction {
      implicit session =>

      DB.modelTypes.filter(_.slug === slug).firstOption match {
        case None => println("No unigram model")
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

object Unigram extends UnigramBase {
  override val featurizer = new Featurizer[Article, String] {
    def apply(article: Article) = {
      for ((word, count) <- wordCounts(article.title).toSeq)
      yield FeatureObservation("title:" + word, count)
      for ((word, count) <- wordCounts(article.body).toSeq)
      yield FeatureObservation(word, count)
    }
  }
  override val slug = "unigram"
}

object DistinctUnigram extends UnigramBase {
  override val featurizer = new Featurizer[Article, String] {
    def apply(article: Article) = {
      for ((word, count) <- wordCounts(article.title).toSeq)
      yield FeatureObservation("title:" + word)
      for ((word, count) <- wordCounts(article.body).toSeq)
      yield FeatureObservation(word)
    }
  }
  override val slug = "distinct_unigram"
}
