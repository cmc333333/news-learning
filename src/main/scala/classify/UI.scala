package info.cmlubinski.newslearning.classify

import unfiltered.Cycle
import unfiltered.filter.Planify
import unfiltered.request._
import unfiltered.response._
import unfiltered.jetty.Http


object UI {
  val display = Cycle.Intent[Any, Any] {
    case _ => ResponseString("UI")
  }

  def main(args:Array[String]) {
    args.toList match {
      case hostname :: port :: remaining =>
        val plan = remaining match {
          case "true" :: _ => HTTPSGuard{ display }
          case _ => display
        }
        Http(port.toInt, hostname).plan(Planify(plan)).run()
      case _ =>
        println("Needs two parameters: hostname port")
    }
  }
}
