package info.cmlubinski.newslearning.learn

import scala.collection.mutable

import nak.data.{Featurizer, FeatureObservation}

import info.cmlubinski.newslearning.models.Article


trait UnigramBase extends LearnBase {
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
