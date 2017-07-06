package xiatian.knowledge.highlight

import better.files.File

/**
  * Highlight DataSet
  *
  * @author Tian Xia Email: xiat@ruc.edu.cn
  *         School of IRM, Renmin University of China.
  *         Jul 06, 2017 18:31
  */
object HighlightDataSet {
  lazy val highlightFiles = File("./data/highlight").list.toList.sortBy(_.name)

  lazy val articles = highlightFiles map {
    f =>
      println(s"parsing $f ...")
      Highlight.parseFile(f)
  }

  /**
    * 获取所有的章节类别和对应的出现在不同文章中的数量
    *
    * @return
    */
  lazy val sectionStats: Map[String, Int] =
    articles.flatMap {
      article => article.sections.map(section => (section.category, article.filename))
    }
      .groupBy(_._1) // category->
      .map {
      case (category: String, pairs: List[(String, String)]) =>
        (category, pairs.length)
    }

  /**
    * 按照出现频次排序，打印章节的类别名称和出现数量
    */
  def printSectionStatsMessage() = sectionStats.toList.sortBy(_._2).map(
    pair => s"${pair._1}\t${pair._2}"
  ).foreach(println)
}
