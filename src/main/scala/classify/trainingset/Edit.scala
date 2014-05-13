package info.cmlubinski.newslearning.classify.trainingset

import unfiltered.Cookie
import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._

import info.cmlubinski.newslearning.classify.{TSGuard, TS}
import info.cmlubinski.newslearning.models.{DB, TrainingSet}
import info.cmlubinski.newslearning.web.Scalate


object Edit extends Plan {
  import DB.imports._

  object YesLabel extends Params.Extract("yes_label", 
    Params.first ~> Params.nonempty)
  object NoLabel extends Params.Extract("no_label", 
    Params.first ~> Params.nonempty)

  def intent = TSGuard{
    case req@GET(Path("/trainingset/edit")) & TS(tset) => 
      Scalate(req, "trainingset/edit.jade", "tset" -> tset)
    case req@POST(Path("/trainingset/edit") & TS(tset)
                  & Params(YesLabel(yesLabel) & NoLabel(noLabel)
                           & New.NameParam(name))) =>
      DB.withTransaction {
        implicit session =>

        val query = for (ts <- DB.trainingSets if ts.id === tset.id)
                    yield (ts.name, ts.yesLabel, ts.noLabel)
        query.update((name.take(100), yesLabel.take(100), noLabel.take(100)))
        Redirect("/trainingset/edit")
      }
  }
}
