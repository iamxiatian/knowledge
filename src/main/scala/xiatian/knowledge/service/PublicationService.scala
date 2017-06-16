package xiatian.knowledge.service

import java.util.UUID

import better.files.File
import xiatian.knowledge.models.Publication

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.xml._

/**
  * 文献服务接口，负责文献的导入、导出等操作
  *
  * @author Tian Xia
  *         Jun 11, 2017 22:59
  */
object PublicationService {
  /**
    * 将存放在单个XML文件中的一组文献信息，读取到列表中
    * XML文件的格式请参考 data/papers目录下的文件
    *
    * @param f
    */
  def loadJournalFromXml(f: File): Seq[Publication] = {
    val doc = XML loadFile f.toJava
    val bibNodes: NodeSeq = doc \\ "Bibliography"
    bibNodes.map(node =>
      Publication(
        UUID.randomUUID().toString,
        (node \\ "Title").filter(
          _.attributes.exists(_.value.text == "zh-CHS")
        ).text.trim,
        ((node \\ "Author" \ "Info")
          .filter(
            _.attributes.exists(_.value.text == "zh-CHS")
          ) \ "FullName").text.trim.split(";").toList,
        (node \\ "Organization" text).trim,
        (node \\ "Abstracts" text).trim,
        (node \\ "Media" text).trim,
        "journal",
        (node \\ "Year" text).trim,
        "",
        (node \\ "Issue" text).trim,
        (node \\ "PageScope" text).trim,
        None,
        (node \\ "Abstracts" text).trim
      )
    )
  }

  /**
    * 针对目录下的所有XML文件，读取其中的文献信息，保存到MongoDB中。该方法会遍历该目录
    * 下的所有XML文件进行处理
    *
    * @param dir 存有XML格式文献信息的文件目录
    */
  def importFromDir(dir: String) = {
    val xmlFiles = File(dir) glob "**/*.xml"
    xmlFiles foreach { f =>
      try {
        Future.sequence(loadJournalFromXml(f)
          .filter(p =>
            p.`abstract`.nonEmpty
              && p.organization.nonEmpty
              && p.authors.nonEmpty)
          .map(p => Publication.add(p)))
          .flatMap {
            results =>
              //返回执行结果列表
              Future.successful(
                results.map {
                  r =>
                    if (r._1) s"插入：${r._2}" else s"失败：${r._2}"
                }
              )
          }.onComplete {
          case Success(results) => println(s"DONE: ${f.pathAsString}")
          case Failure(ex) => println(s"\t失败：${f.pathAsString}\n\t$ex")
        }
      } catch {
        case ex: Exception => println(s"\t失败：${f.pathAsString}\n\t$ex")
      }
    }
  }

}
