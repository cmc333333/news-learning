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

  val featurizer = new Featurizer[Article, String] {
    def apply(article: Article) = {
      for ((word, count) <- wordCounts(article.title).toSeq)
      yield FeatureObservation("title:" + word, count.toFloat)
      for ((word, count) <- wordCounts(article.body).toSeq)
      yield FeatureObservation(word, count.toFloat)
    }
  }

  def main(args:Array[String]) {
    DB.withTransaction {
      implicit session =>

      val trainingModels = for (data <- DB.trainingData;
                                article <- DB.articles
                                if data.article_id === article.id)
                           yield (data.value, article)

      val trainingSet = trainingModels.list.map{
        case (label, article) => Example(label.toString, article,
                                         article.id.toString)
      }

      val config = LiblinearConfig()
      val classifier = trainClassifier(config, featurizer, trainingSet)

      var a1 = Article(0, "", "Some shooting", "Contains no other details")
      var a2 = Article(0, "", "Some shooting", "Contains shooting details")
      var a3 = Article(0, "", "Something Innocuous", "Contains details")
      var a4 = Article(0, "", "Something Innocuous", "Contains shooting")
      for (a <- List(a1, a2, a3, a4)) {
        println(a)
        println(classifier.predict(a))
        println(classifier.evalRaw(a).toList)
      }
    }
  }
}
