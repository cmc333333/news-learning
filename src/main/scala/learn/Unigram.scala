package info.cmlubinski.newslearning.learn

import scala.collection.mutable

import chalk.text.analyze.PorterStemmer
import chalk.text.segment.JavaSentenceSegmenter
import chalk.text.tokenize.SimpleEnglishTokenizer
import nak.data.{Example, Featurizer, FeatureObservation}
import nak.liblinear.LiblinearConfig
import nak.NakContext.trainClassifier

import info.cmlubinski.newslearning.models.{Article, DB}


object Unigram {
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

  val featurizer = new Featurizer[String, String] {
    def apply(text: String) = {
      for ((word, count) <- wordCounts(text).toSeq)
      yield FeatureObservation(word, count.toFloat)
    }
  }


  def main(args:Array[String]) {
    DB.withTransaction {
      implicit session =>

      val training = (for (data <- DB.trainingData;
                          article <- DB.articles
                          if data.article_id === article.id)
                      yield (data.value, article)).iterator.map{ pair =>
                        val (label, article) = pair
                        Example(label.toString,
                                article.title + ". " + article.body,
                                article.id.toString)
                     }.toList
      val config = LiblinearConfig()
      val classifier = trainClassifier(config, featurizer, training)

      println(classifier.predict("Some shooting"))
      println(classifier.evalRaw("Some shooting").toList)
      println(classifier.predict("Baloney"))
      println(classifier.evalRaw("Baloney").toList)
      println(classifier.predict("Two dead"))
      println(classifier.evalRaw("Two dead").toList)
    }
  }
}
