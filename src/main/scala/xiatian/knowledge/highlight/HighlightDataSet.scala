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
    * 获取所有的章节类别的文档频度和总文字数量
    *
    * @return
    */
  lazy val sectionStats: Map[String, (Int, Int)] =
    articles.flatMap {
      article =>
        article.sections.map(section =>
          (section.category, article.filename, section.characters))
    }
      .groupBy(_._1) // category->
      .map {
      case (category: String, items: List[(String, String, Int)]) =>
        (category, (items.length, items.map(_._3).sum))
    }

  /**
    * 按照文档频度排序，打印章节的类别名称、文档频度、文字总数量
    */
  def printSectionStatsMessage() = {
    val totalCharacters = sectionStats.map(_._2._2).sum.toDouble
    println("章节类别\t文档频度\t文字总数")

    sectionStats.toList.sortBy(_._2._1).map {
      pair =>
        val chRatio = (pair._2._2 / totalCharacters * 100).formatted("%.2f")
        s"${pair._1}\t${pair._2._1} \t ${pair._2._2}(${chRatio}%)"
    } .foreach(println)
  }

  /**
    * 指定类别的章节总共包含了多少个不同的亮点
    */
  val highlightCoverage: Seq[String] => Seq[(String, Int, Int)]
  = (categories: Seq[String]) => articles.map {
    article =>
      val highlights = article.highlights
      val matchedSectionCount = highlights.map {
        highlight =>
          val matchedSectionLights = highlight.labeledLights
            .filter(
              labeledLight => categories.contains(labeledLight.section.category)
            )
          if (matchedSectionLights.isEmpty) 0 else 1
      }.sum

      (article.filename, matchedSectionCount, highlights.length)
  }

  def printCoverage(category: String*) = {
    val coverage: Seq[(String, Int, Int)] = highlightCoverage(category)
    val average = coverage.map {
      case (filename: String, matched: Int, total: Int) =>
        val ratio = if (total == 0) 0 else matched.toDouble / total
        println(s"$filename \t $matched / $total \t $ratio")
        ratio
    }.sum / coverage.length

    println(s"average: $average")
  }

}
