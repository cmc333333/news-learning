package info.cmlubinski.newslearning.web
// Based off https://github.com/unfiltered/unfiltered-scalate.g8/blob/master/src/main/g8/src/main/scala/Scalate.scala
import scala.collection.JavaConversions._

import de.neuland.jade4j.Jade4J
import unfiltered.request.HttpRequest
import unfiltered.response.{HtmlContent, ResponseString}

object Jade {
  def apply[A, B](request: HttpRequest[A],
                  template: String,
                  attributes:(String, AnyRef)*) 
  = HtmlContent ~> ResponseString(
    Jade4J.render("src/main/resources/templates/" + template,
                  Map(attributes :_*))
  )
}
