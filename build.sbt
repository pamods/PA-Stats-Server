name := "PA Stats"

version := "13.0.0"

organization := "net.liftweb"

scalaVersion := "2.10.0"


resolvers ++= Seq("snapshots"     at "http://oss.sonatype.org/content/repositories/snapshots",
                "releases"        at "http://oss.sonatype.org/content/repositories/releases"
                )

seq(com.github.siasia.WebPlugin.webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

env in Compile := Some(file("./src/main/webapp/WEB-INF/jetty-env.xml").asFile)


libraryDependencies ++= {
  val liftVersion = "2.5"
  Seq(
    "net.liftweb"       %% "lift-webkit"        % liftVersion        % "compile",
	"net.liftmodules" %% "extras_2.5" % "0.1" % "compile",
    "org.eclipse.jetty" % "jetty-webapp"        % "8.1.7.v20120910"  % "container,test",
	"org.eclipse.jetty" % "jetty-plus"        % "8.1.7.v20120910"  % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"    % "logback-classic"     % "1.0.6",
    "org.specs2"        %% "specs2"             % "1.14"            % "test"
  )
}

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  "org.mockito" % "mockito-core" % "1.9.5" % "test",
  "junit" % "junit" % "4.11" % "test"
)

libraryDependencies += "postgresql" % "postgresql" % "9.1-901.jdbc4"

libraryDependencies += "com.mchange" % "c3p0" % "0.9.5-pre4"

libraryDependencies += "org.jooq" % "jooq" % "3.2.0"
            
libraryDependencies += "org.jooq" % "jooq-scala" % "3.2.0"
            
libraryDependencies += "org.jooq" % "jooq-meta" % "3.2.0"
            
libraryDependencies += "org.jooq" % "jooq-codegen" % "3.2.0"

libraryDependencies += "commons-lang" % "commons-lang" % "2.3"
            
