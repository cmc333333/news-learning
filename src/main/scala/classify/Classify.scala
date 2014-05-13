package info.cmlubinski.newslearning.classify

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._

import info.cmlubinski.newslearning.models.{DB, TrainingSet}
import info.cmlubinski.newslearning.web.Scalate


object Classify extends Plan {
  import DB.imports._

  object ArticleParam extends Params.Extract("article_id",
    Params.first ~> Params.int)

  def intent = TSIdGuard {
    case req@GET(Path("/classify")) & TSId(uuid) => 
      unseenArticle(uuid) match {
        case Some(article) => 
          Scalate(req, "classify.jade", "article" -> article)
        case None =>
          Scalate(req, "classify-no-articles.jade")
      }
    case req@POST(Path("/classify") & TSId(uuid)
                  & Params(params@ArticleParam(article_id))) =>
      val value = params("classify-yes").length > 0
      classifyArticle(article_id, uuid, value)
      Redirect("/classify")
  }

  def unseenArticle(uuid:String) = DB.withTransaction {
    implicit session =>

    val query = for (
      ((a, d), s) <- 
        DB.articles
        leftJoin DB.trainingData on(_.id === _.article_id)
        leftJoin DB.trainingSets on((pair, s) =>
                                    pair._2.training_set_id === s.id
                                    && s.uuid === uuid)
      if d.article_id.isNull
    ) yield a
    query.take(1).list.headOption
  }
  def classifyArticle(article_id:Int, uuid:String, value:Boolean) {
    DB.withTransaction {
      implicit session =>

      DB.trainingSets.filter(_.uuid === uuid).list match {
        case TrainingSet(ts_id, name, uuid) :: _ =>
          val previous = for (d <- DB.trainingData
                              if d.training_set_id === ts_id
                              && d.article_id === article_id) yield d.value
          if (previous.exists.run) {
            previous.update(value)
          } else {
            DB.trainingData += (ts_id, article_id, value)
          }
        case Nil => ()
      }
    }
  }
}
