package info.cmlubinski.newslearning.classify

import unfiltered.request._
import unfiltered.response._
import unfiltered.jetty.Http

object UI {
  val display = unfiltered.filter.Planify {
    case _ => ResponseString("UI")
  }

  def main(args:Array[String]) {
    args.toList match {
      case hostname :: port :: _ => 
        Http(port.toInt, hostname).plan(display).run()
      case _ =>
        println("Needs two parameters: hostname port")
    }
  }
}
