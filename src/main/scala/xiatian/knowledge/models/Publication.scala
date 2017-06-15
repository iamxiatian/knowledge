package xiatian.knowledge.models

import java.text.SimpleDateFormat
import java.util.{Date, UUID}

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * 文章发表信息
  *
  * @author Tian Xia
  *         Jun 08, 2017 18:59
  */
case class Publication(id: String,
                       title: String,
                       authors: List[String],
                       organization: String,
                       `abstract`: String,
                       journal: String,
                       year: String,
                       volume: String,
                       number: String,
                       pages: String,
                       doi: Option[String],
                       taggedAbstract: String = "",
                       username: String = "", // 最后一次更新操作的用户
                       createTime: Date = new Date,
                       updateTime: Date = new Date
                      )


object Publication extends MongoDocument {
  val collectionName = "article"
  val collection: Future[BSONCollection] = db.map(_.collection(collectionName))

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  implicit def publicationWriter: BSONDocumentWriter[Publication] =
    Macros.writer[Publication]

  implicit def publicationReader: BSONDocumentReader[Publication] =
    Macros.reader[Publication]

  /**
    * 把文献信息加入到MongoDB中
    *
    * @param publication
    * @return 如果成功，返回(true, "success")，否则返回(false, 出错信息)
    */
  def add(publication: Publication): Future[(Boolean, String)] = collection
    .flatMap {
      c =>
        c.insert(publication).flatMap(
          r =>
            Future((r.ok, if (r.ok) publication.title else r.toString))
        )
    }

  /**
    * 根据ID更新人工标记的摘要
    */
  def updateById(id: String, taggedAbstract: String): Future[(Boolean, String)] =
    collection.flatMap {
      c =>
        c.update(BSONDocument("id" -> id),
          BSONDocument(
            "$set" -> BSONDocument("taggedAbstract" -> taggedAbstract))
        ).flatMap(
          r =>
            Future((r.ok, if (r.ok) "Success" else r.toString))
        )
    }

  def findById(id: String): Future[Option[Publication]] = collection.flatMap {
    c =>
      c.find(BSONDocument.empty).one
  }

  def findAll(topN: Int): Future[List[Publication]] = collection.flatMap {
    c =>
      c.find(BSONDocument.empty).cursor[Publication]().collect[List](topN)
  }

  def find(journal: String, year: String) = collection.flatMap {
    c =>
      c.find(BSONDocument(
        "journal" -> journal,
        "year" -> year
      )).cursor[Publication]().collect[List]()
  }

  override def init: Future[(Boolean, String)] = {
    add(Publication(
      UUID.randomUUID().toString,
      "词向量聚类加权TextRank的关键词抽取研究",
      List("夏天"),
      "中国人民大学",
      "简介。。。。。",
      "数据分析与知识发现",
      "2017",
      "1",
      "2",
      "28-34",
      Some("10.11925/infotech.2096-3467.2017.02.04")
    ))
  }

}


