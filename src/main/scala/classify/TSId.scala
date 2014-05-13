package info.cmlubinski.newslearning.classify

import unfiltered.{Cookie, Cycle}
import unfiltered.request._
import unfiltered.response.Redirect


object TSId {
  def unapply[T](req: HttpRequest[T]) = req match {
    case Cookies(cookies) => cookies("trainingset") match {
      case Some(Cookie(_, uuid, _, _, _, _, _, _)) => Some(uuid)
      case _ => None
    }
  }
}

object TSIdGuard {
  def apply[A, B](intent: Cycle.Intent[A, B]) = Cycle.Intent[A, B] {
    case req@TSId(_) => Cycle.Intent.complete(intent)(req)
    case _ => Redirect("/trainingset/new")
  }
}
