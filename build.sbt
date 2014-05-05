import com.github.bigtoast.sbtliquibase.LiquibasePlugin
import com.typesafe.sbt.SbtStartScript

name := "newslearning"

version := "0.1"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.2.0",
  "com.typesafe.slick" %% "slick" % "2.0.1",
  "com.h2database" % "h2" % "1.4.178",
  "net.databinder" %% "unfiltered-filter" % "0.7.1",
  "net.databinder" %% "unfiltered-jetty" % "0.7.1",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "net.databinder.dispatch" %% "dispatch-jsoup" % "0.11.0",
  "org.yaml" % "snakeyaml" % "1.13",
  "postgresql" % "postgresql" % "9.1-901.jdbc4"
)

seq(LiquibasePlugin.liquibaseSettings: _*)

liquibaseDriver := "org.h2.Driver"

liquibaseUrl := "jdbc:h2:/tmp/news.db;DATABASE_TO_UPPER=false"

liquibaseUsername := ""

liquibasePassword := ""

liquibaseChangelog := "src/main/migrations/changelog.yaml"

seq(SbtStartScript.startScriptForClassesSettings: _*)
