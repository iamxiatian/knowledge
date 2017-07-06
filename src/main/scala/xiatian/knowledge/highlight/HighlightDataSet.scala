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

}
