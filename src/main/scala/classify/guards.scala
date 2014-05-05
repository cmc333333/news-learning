package info.cmlubinski.newslearning.classify

import unfiltered.Cycle
import unfiltered.request.{Host, HTTPS}
import unfiltered.response.Redirect

object HTTPSGuard {
  def apply[A,B](intent: Cycle.Intent[A,B]) = Cycle.Intent[A,B] {
    case req@HTTPS() => Cycle.Intent.complete(intent)(req)
    case req@Host(hostname) => Redirect("https://" + hostname + req.uri)
  }
}
