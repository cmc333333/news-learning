package info.cmlubinski.newslearning.classify

import unfiltered.Cycle
import unfiltered.filter.Planify
import unfiltered.jetty.Http
import unfiltered.request._
import unfiltered.response._

import info.cmlubinski.newslearning.classify.trainingset.{New => NewTrainingSet}


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
        Http(port.toInt, hostname).plan(Planify( 
          NewTrainingSet.intent onPass(UserGuard { plan })
        )).run()
      case _ =>
        println("Needs two parameters: hostname port")
    }
  }
}
