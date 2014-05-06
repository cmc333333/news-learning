package info.cmlubinski.newslearning.classify

import unfiltered.Cycle
import unfiltered.kit.Auth
import unfiltered.request.{Host, HTTPS, StringHeader}
import unfiltered.response.Redirect

object XForwardedProto extends StringHeader("X-Forwarded-Proto")

object HTTPSGuard {
  def apply[A,B](intent: Cycle.Intent[A,B]) = Cycle.Intent[A,B] {
    case req@HTTPS() => Cycle.Intent.complete(intent)(req)
    case req@XForwardedProto("https") => Cycle.Intent.complete(intent)(req)
    case req@Host(hostname) => Redirect("https://" + hostname + req.uri)
  }
}

object UserGuard {
  val simple:(String,String) => Boolean = (user:String, pass:String) => {
    user == "tst"
  }
  def apply[A,B](intent: Cycle.Intent[A,B]) = 
    Auth.basic(simple, "Use a new username to create a new account.")(intent)
}
