package info.cmlubinski.newslearning.classify.trainingset

import java.security.SecureRandom

import org.apache.commons.codec.binary.Base64
import unfiltered.Cookie
import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._

import info.cmlubinski.newslearning.models.{DB, TrainingSet}
import info.cmlubinski.newslearning.web.Scalate


object New extends Plan {
  import DB.imports._

  object NameParam extends Params.Extract("name", 
    Params.first ~> Params.nonempty)

  def intent = {
    case req@GET(Path("/trainingset/new")) & Params(params) => 
      Scalate(req, "trainingset/new.jade",
              "errors" -> params("error").toList)
    case req@POST(Path("/trainingset/new") & Params(NameParam(name))) =>
      val ts = newSet(name)
      SetCookies(Cookie("trainingset", ts.uuid, path=Some("/"))) ~>
        Redirect("/classify")
    case req@POST(Path("/trainingset/new")) =>
      Redirect("/trainingset/new?error=Missing+name+field")
  }

  def newSet(name:String) = DB.withTransaction{
    implicit session =>
    val sr = new SecureRandom()
    val uuidBytes = new Array[Byte](18)
    sr.nextBytes(uuidBytes)
    val uuid = Base64.encodeBase64URLSafeString(uuidBytes)

    val ts = TrainingSet(0, name.take(100), uuid, "Yes", "No")
    DB.trainingSets += ts
    ts
  }
}
