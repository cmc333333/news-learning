package info.cmlubinski.newslearning.classify

import scala.collection.JavaConverters._

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._

import info.cmlubinski.newslearning.models.{DB, TrainingSet}
import info.cmlubinski.newslearning.web.Jade


object Classify extends Plan {
  import DB.imports._

  object ArticleParam extends Params.Extract("article_id",
    Params.first ~> Params.int)

  def intent = TSGuard {
    case req@GET(Path("/classify")) & TS(tset) => 
      unseenArticle(tset) match {
        case Some(article) => DB.withTransaction {
          implicit session =>
          
          val mtXp = (for (modelData <- DB.modelData;
                           modelType <- DB.modelTypes
                           if modelData.training_set_id === tset.id
                           && modelData.model_type === modelType.id)
                      yield (modelData, modelType)).list.map{
            case (modelData, modelType) =>
              val classifier = modelData.featurizer
              val prediction:Double = classifier.evalRaw(article)(1)
              (modelType, "%.2f".format(prediction * 100))
          }

          Jade(req, "classify.jade",
               "article" -> article, "tset" -> tset, "mtXp" -> mtXp.asJava)
        }
        case None =>
          Jade(req, "classify-no-articles.jade")
      }
    case req@POST(Path("/classify") & TS(tset)
                  & Params(params@ArticleParam(article_id))) =>
      val value = params("classify-yes").length > 0
      classifyArticle(article_id, tset, value)
      Redirect("/classify")
  }

  def unseenArticle(tset:TrainingSet) = DB.withTransaction {
    implicit session =>

    val query = for (
      (a, d) <- 
      DB.articles
      leftJoin DB.trainingData on((a, d) =>
                                  a.id === d.article_id
                                  && d.training_set_id === tset.id)
      if d.article_id.isNull
    ) yield a
    query.take(1).list.headOption
  }
  def classifyArticle(article_id:Int, tset:TrainingSet, value:Boolean) {
    DB.withTransaction {
      implicit session =>

      val previous = for (d <- DB.trainingData
                          if d.training_set_id === tset.id
                          && d.article_id === article_id) yield d.value
      if (previous.exists.run) {
        previous.update(value)
      } else {
        DB.trainingData += (tset.id, article_id, value)
      }
    }
  }
}
