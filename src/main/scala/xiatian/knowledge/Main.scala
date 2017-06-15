package xiatian.knowledge

import xiatian.knowledge.service.PublicationService

object Main extends App {

  case class Config(dumpIn: Boolean = false,
                    dir: Option[String] = None)

  val parser = new scopt.OptionParser[Config]("bin/knowledge") {
    head("Knowledge Mining from Publications", "1.0")

    opt[Unit]("dumpIn").action((_, c) =>
      c.copy(dumpIn = true)).text("dump publications from directory.")

    opt[String]('d', "dir").optional().
      action((x, c) => c.copy(dir = Some(x))).
      text("Directory, use with other params like dumpIn.")

    help("help").text("prints this usage text")

    note("\n xiatian, xia@ruc.edu.cn.")
  }

  parser.parse(args, Config()) match {
    case Some(config) =>
      if (config.dumpIn) {
        if (config.dir.isEmpty) {
          println("Please specify a directory by parameter d")
        }
        else {
          PublicationService.importFromDir(config.dir.get)
        }
      }

    case None => {
      println("Wrong parameters :(")
    }
  }


}