package info.cmlubinski.newslearning.models

import scala.slick.driver.H2Driver.simple._


class Article(tag: Tag) extends Table[(String, String, String)](tag, 
                                                                "ARTICLE") {
  def url = column[String]("URL", O.PrimaryKey)
  def title = column[String]("TITLE")
  def body = column[String]("BODY")

  def * = (url, title, body)
}

object articles extends TableQuery(new Article(_))
