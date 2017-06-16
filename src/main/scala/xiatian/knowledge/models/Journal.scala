package xiatian.knowledge.models

import java.text.SimpleDateFormat

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * 期刊对象
  *
  * @author Tian Xia
  *         Jun 15, 2017 17:20
  */
case class Journal(name: String, rank: Int)


object Journal extends MongoDocument {
  val collectionName = "journal"
  val collection: Future[BSONCollection] = db.map(_.collection(collectionName))

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  implicit def journalWriter: BSONDocumentWriter[Journal] =
    Macros.writer[Journal]

  implicit def journalReader: BSONDocumentReader[Journal] =
    Macros.reader[Journal]


  /**
    * 把期刊加入到数据库中
    *
    * @return 如果成功，返回(true, "success")，否则返回(false, 出错信息)
    */
  def add(name: String, rank: Int): Future[(Boolean, String)] = collection
    .flatMap {
      c =>
        c.count(Some(BSONDocument("name" -> name))).flatMap(
          count =>
            if (count > 0) {
              Future.successful((false, s"$name has already exists."))
            } else {
              c.insert(Journal(name, rank)).flatMap(
                r =>
                  Future.successful((r.ok, if (r.ok) name else r.toString))
              )
            }
        )
    }

  /**
    * 初始化时，注入期刊名称
    */
  override def init(): Future[(Boolean, String)] = {
    //把data/papers目录下的期刊名称都输出来
//    import better.files.File
//    val dir = File("./data/papers")
//    dir.list.zipWithIndex
//      .foreach {
//        case (x: File, idx: Int) =>
//          println(s"""add("${x.name}", ${idx + 1}),""")
//      }

    val addingFutures = Future.sequence(Seq(
      add("情报资料工作", 1),
      add("图书馆建设", 2),
      add("图书馆", 3),
      add("图书馆杂志", 4),
      add("档案学通讯", 5),
      add("情报杂志", 6),
      add("图书情报知识", 7),
      add("国家图书馆学刊", 8),
      add("情报科学", 9),
      add("情报学报", 10),
      add("图书馆论坛", 11),
      add("中国图书馆学报", 12),
      add("大学图书馆学报", 13),
      add("图书馆工作与研究", 14),
      add("图书情报工作", 15),
      add("现代图书情报技术", 16),
      add("图书馆学研究", 17),
      add("图书与情报", 18),
      add("情报理论与实践", 19),
      add("档案学研究", 20)
    )).flatMap(
      results => Future.successful((results.forall(_._1), "插入期刊名称"))
    )

    //删除原有collection, 然后再重新插入
    collection.flatMap(c=>
      c.drop(false).flatMap(_ => addingFutures)
    )
  }
}
