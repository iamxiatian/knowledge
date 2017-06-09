name          := "knowledge"
organization  := "xiatian.knowledge"
version       := "1.0-alpha"
scalaVersion  := "2.12.1"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

//fork in run := true
//cancelable in Global := true

//akka
libraryDependencies +="com.typesafe.akka" %% "akka-actor" % "2.5.0"
libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.5.0"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.5.0"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.5"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.5"

//akka http 跨域访问
libraryDependencies += "ch.megard" %% "akka-http-cors" % "0.2.1"

libraryDependencies += "com.typesafe" % "config" % "1.3.1"

//akka kryo serialization, it's faster than java serializer
libraryDependencies += "com.github.romix.akka" %% "akka-kryo-serialization" % "0.5.2"

//XML support
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.6"

libraryDependencies += "org.scala-lang.modules" %% "scala-async" % "0.9.6"

//auto resource management
libraryDependencies += "com.jsuereth" %% "scala-arm" % "2.0"

//command line parser
libraryDependencies += "com.github.scopt" %% "scopt" % "3.5.0"

//Scala wrapper for Joda Time.
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.16.0"

//Scala better file
libraryDependencies += "com.github.pathikrit"  %% "better-files-akka"  % "3.0.0"

//Java mail
libraryDependencies += "javax.mail" % "mail"  % "1.4.7"

//bing and mongodb driver
libraryDependencies += "com.github.etaty" %% "rediscala" % "1.8.0"
libraryDependencies +=  "mysql" % "mysql-connector-java" % "5.1.23"
libraryDependencies +=  "org.reactivemongo" %% "reactivemongo" % "0.12.3"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"


//NLP libraries
libraryDependencies += "com.hankcs" % "hanlp" % "portable-1.2.11"
libraryDependencies += "org.ahocorasick" % "ahocorasick" % "0.3.0"


scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq(
  // other resolvers here
  // if you want to use snapshot builds (currently 0.12-SNAPSHOT), use this.
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

//native package
import NativePackagerHelper._
enablePlugins(JavaServerAppPackaging)
mainClass in Compile := Some("xiatian.knowledge.api.HttpApiServer")

mappings in Universal <++= (packageBin in Compile) map { _ =>
  /**
    * 显示一个目录下的所有文件，包括文件夹中的文件，返回文件和对应的文件名称，文件名称采用相对于prefix的相对路径
    */
  def listAllFiles(root: File, prefix: String):List[(File, String)] = {
    root.listFiles().flatMap {
      f => if(f.isDirectory)
        listAllFiles(f, prefix + f.getName + "/")
      else
        List((f, prefix + f.getName))
    }.toList
  }

  listAllFiles(new File("./web"), "web/"):::listAllFiles(new File("./conf"), "conf/")
    .map{ case (f: File, path:String) => f -> path }
}

javaOptions in Universal ++= Seq(
  // -J params will be added as jvm parameters
  "-J-Xms4G",
  "-J-Xmx8G"
)

