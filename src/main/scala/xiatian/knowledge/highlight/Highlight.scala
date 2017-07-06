package xiatian.knowledge.highlight

import better.files.File

import scala.xml.{Node, XML}


case class Article(filename: String,
                   title: String,
                   author: String,
                   journal: String,
                   time: String,
                   doi: String,
                   keywords: List[String],
                   `abstract`: String,
                   content: String,
                   sections: Seq[Section],
                   highlights: Seq[Highlight])

/**
  * 文章的节
  *
  * @param name     小节的名称
  * @param category 小节的类别
  */
case class Section(name: String, category: String)

/**
  * 人工标记的亮点
  */
case class LabeledLight(
                         text: String, //标记的文本
                         matchType: String, //匹配类型
                         section: Section
                       )

/**
  * 亮点
  *
  * @param id            亮点的id
  * @param text          亮点的文字描述
  * @param labeledLights 人工标记的亮点数量
  */
case class Highlight(id: String,
                     text: String,
                     labeledLights: Seq[LabeledLight])


/**
  * 学术亮点的处理
  *
  * @author Tian Xia
  *         Jul 06, 2017 13:13
  */
object Highlight {
  /**
    * 读取XML文件，进行解析
    *
    * @param f
    */
  def parseFile(f: File): Article = {
    val doc = XML.loadFile(f toJava)
    Article(
      f.name,
      (doc \\ "title").text,
      (doc \\ "author").text,
      (doc \\ "journal").text,
      (doc \\ "time").text,
      (doc \\ "doi").text,
      (doc \\ "keywords").map(_.text.trim).toList,
      (doc \\ "abstract").text.trim,
      (doc \\ "section").map(_.text).mkString("\n"),
      (doc \\ "section").map(node =>
        Section(node.attribute("name").get.text.trim,
          node.attribute("category").get.text.trim)
      ),
      extractHighlights(doc)
    )
  }

  /**
    * 从XML文档中抽取出其中的亮点及其与人工标记的亮点信息
    *
    * @param doc
    * @return
    */
  def extractHighlights(doc: Node): List[Highlight] = {
    val highlightTexts: Map[String, String] = (doc \\ "highlight").map {
      h =>
        val id = h.attribute("id").get.head.text.trim
        val content = h.text.trim
        (id, content)
    }.toMap

    // 列出所有Section中人工标记的亮点
    val highlights: Seq[Highlight] = (doc \\ "section") flatMap {
      node =>
        val section = Section(node.attribute("name").get.text.trim,
          node.attribute("category").get.text.trim)

        (node \\ "h") flatMap {
          h =>
            val targetIds = h.attribute("target").get.text.split(";")
            val matchTypes = h.attribute("match").get.text.split(";")
            val labeledText = h.text //人工标注的亮点文本
            targetIds.zip(matchTypes).map {
              case (id: String, matchType: String) =>
                val text = highlightTexts(id)
                Highlight(
                  id,
                  text,
                  List(LabeledLight(labeledText, matchType, section))
                )
            }
        }
    }

    //对所有的亮点（数量和人工标记的数量相同）进行分组，合并人工标记的亮点
    highlights.groupBy(_.id).map {
      case (id: String, hs: Seq[Highlight]) =>
        val h = hs.head
        Highlight(id, h.text, hs.flatMap(_.labeledLights))
    }.toList.sortBy(_.id)
  }
}
