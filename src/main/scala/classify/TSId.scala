package info.cmlubinski.newslearning.classify

import unfiltered.{Cookie, Cycle}
import unfiltered.request._
import unfiltered.response.Redirect

import info.cmlubinski.newslearning.models.DB


object TS {
  import DB.imports._

  def unapply[T](req: HttpRequest[T]) = req match {
    case Cookies(cookies) => cookies("trainingset") match {
      case Some(Cookie(_, uuid, _, _, _, _, _, _)) => DB.withTransaction {
        implicit session =>
          DB.trainingSets.filter(_.uuid === uuid).list match {
            case tset :: _ => Some(tset)
            case Nil => None
          }
      }
      case _ => None
    }
  }
}

object TSGuard {
  def apply[A, B](intent: Cycle.Intent[A, B]) = Cycle.Intent[A, B] {
    case req@TS(_) => Cycle.Intent.complete(intent)(req)
    case _ => Redirect("/trainingset/new")
  }
}
