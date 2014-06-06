package info.cmlubinski.newslearning.learn

import scala.collection.mutable

import nak.data.{Featurizer, FeatureObservation}

import info.cmlubinski.newslearning.models.Article


trait TrigramBase extends BigramBase {
  def triplets(text:String):List[(String, String, String)] = {
    val words = (for (sentence <- toSentences(text);
                      word <- toWords(sentence.trim);
                      stemmed = stem(word)
                      if word.matches("^[a-z]+$")) yield stemmed).toList
    words.zip(words.drop(1)).zip(words.drop(2)).map{
      case ((w1, w2), w3) => (w1, w2, w3)
    }
  }
}


object Trigram extends TrigramBase {
  override val featurizer = new Featurizer[Article, String] {
    def apply(article: Article) = {
      for ((word, count) <- wordCounts(article.title).toSeq)
      yield FeatureObservation("1:title:" + word, count)
      for ((word, count) <- wordCounts(article.body).toSeq)
      yield FeatureObservation("1:" + word, count)
      for ((lhs, rhs) <- pairs(article.title))
      yield FeatureObservation("2:title:" + lhs + ":" + rhs)
      for ((lhs, rhs) <- pairs(article.body))
      yield FeatureObservation("2:" + lhs + ":" + rhs)
      for ((w1, w2, w3) <- triplets(article.title))
      yield FeatureObservation("3:title:" + w1 + ":" + w2 + ":" + w3)
      for ((w1, w2, w3) <- triplets(article.body))
      yield FeatureObservation("3:" + w1 + ":" + w2 + ":" + w3)
    }
  }
  override val slug = "trigram"
}


object DistinctTrigram extends TrigramBase {
  override val featurizer = new Featurizer[Article, String] {
    def apply(article: Article) = {
      for ((word, count) <- Unigram.wordCounts(article.title).toSeq)
      yield FeatureObservation("1:title:" + word)
      for ((word, count) <- Unigram.wordCounts(article.body).toSeq)
      yield FeatureObservation("1:" + word)
      for ((lhs, rhs) <- pairs(article.title))
      yield FeatureObservation("2:title:" + lhs + ":" + rhs)
      for ((lhs, rhs) <- pairs(article.body))
      yield FeatureObservation("2:" + lhs + ":" + rhs)
      for ((w1, w2, w3) <- triplets(article.title))
      yield FeatureObservation("3:title:" + w1 + ":" + w2 + ":" + w3)
      for ((w1, w2, w3) <- triplets(article.body))
      yield FeatureObservation("3:" + w1 + ":" + w2 + ":" + w3)
    }
  }
  override val slug = "distinct_trigram"
}
