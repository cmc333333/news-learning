import com.github.bigtoast.sbtliquibase.LiquibasePlugin

name := "newslearning"

version := "0.1"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "net.databinder.dispatch" %% "dispatch-jsoup" % "0.11.0",
  "com.typesafe.slick" %% "slick" % "2.0.1",
  "com.h2database" % "h2" % "1.3.174",
  "org.yaml" % "snakeyaml" % "1.13"
)

seq(LiquibasePlugin.liquibaseSettings: _*)

liquibaseDriver := "org.h2.Driver"

liquibaseUrl := "jdbc:h2:file:/tmp/db"

liquibaseUsername := ""

liquibasePassword := ""

liquibaseChangelog := "src/main/migrations/changelog.yaml"
