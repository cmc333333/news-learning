package info.cmlubinski.newslearning.learn

import scala.collection.mutable

import nak.data.{Featurizer, FeatureObservation}

import info.cmlubinski.newslearning.models.Article


object Bigram extends LearnBase {
  def pairs(text:String):List[(String, String)] = {
    val words = (for (sentence <- toSentences(text);
                      word <- toWords(sentence.trim);
                      stemmed = stem(word)
                      if word.matches("^[a-z]+$")) yield stemmed).toList
    words.zip(words.drop(1))
  }
  override val featurizer = new Featurizer[Article, String] {
    def apply(article: Article) = {
      for ((word, count) <- Unigram.wordCounts(article.title).toSeq)
      yield FeatureObservation("1:title:" + word, count)
      for ((word, count) <- Unigram.wordCounts(article.body).toSeq)
      yield FeatureObservation("1:" + word, count)
      for ((lhs, rhs) <- pairs(article.title))
      yield FeatureObservation("2:title:" + lhs + ":" + rhs)
      for ((lhs, rhs) <- pairs(article.body))
      yield FeatureObservation("2:" + lhs + ":" + rhs)
    }
  }
  override val slug = "bigram"
}
