package info.cmlubinski.newslearning.wordmodels

import scala.collection.mutable

import chalk.text.analyze.PorterStemmer
import chalk.text.segment.JavaSentenceSegmenter
import chalk.text.tokenize.SimpleEnglishTokenizer

import info.cmlubinski.newslearning.models.{DB, Unigram => UnigramModel}


object Unigram {
  import DB.imports._

  def toSentences(body:String):Iterable[String] = JavaSentenceSegmenter(body)
  def toWords(sent:String):Iterable[String] = {
    val tokenizer = SimpleEnglishTokenizer()
    tokenizer(sent)
  }
  def stem(word:String):String = PorterStemmer()(word)

  def main(args:Array[String]) {
    DB.withTransaction {
      implicit session =>

      val unprocessed = for (
        (a, u) <- DB.articles 
                  leftJoin DB.articleUnigrams on (_.id === _.article_id)
        if u.article_id.isNull) yield a

      for (article <- unprocessed.take(5)) {
        val wordCounts = mutable.Map[String, Int]()
        for (sentence <- Seq(article.title) ++ toSentences(article.body);
             word <- toWords(sentence.trim)
             if word.matches("^[a-z]+$")) {
          val stemmed = stem(word)
          wordCounts += (stemmed -> (wordCounts.getOrElse(stemmed, 0) + 1))
        }
        //  Load existing unigrams
        val unigrams = mutable.Map[String, UnigramModel]()
        for (unigram <- DB.unigrams
             if unigram.word inSet wordCounts.keys) {
          unigrams += (unigram.word -> unigram)
        }
        //  Create new unigrams
        val newUnigrams = for (
          word <- wordCounts.keys
          if !unigrams.keySet.contains(word)
        ) yield UnigramModel(0, word)
        DB.unigrams.insertAll(newUnigrams.toSeq:_*)
        //  Get all unigram models
        for (unigram <- DB.unigrams
             if unigram.word inSet wordCounts.keys) {
          unigrams += (unigram.word -> unigram)
        }
        //  Finally, associate with the article
        val articleAssociations = for (
          (word, count) <- wordCounts
        ) yield (article.id, unigrams(word).id, count)
        DB.articleUnigrams.insertAll(articleAssociations.toSeq:_*)
      }
    }
  }
}
