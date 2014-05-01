lazy val root = project.in( file(".") ).dependsOn( liquibasePlugin )

lazy val liquibasePlugin = uri("git://github.com/cmc333333/sbt-liquibase")
