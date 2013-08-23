scalaVersion := "2.10.2"

version := "dev-SNAPSHOT"


libraryDependencies += "com.h2database" % "h2" % "1.3.173"

libraryDependencies += "play" %% "anorm" % "2.1.3"


resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"