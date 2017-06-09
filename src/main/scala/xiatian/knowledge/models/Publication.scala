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
                       `abstract`:String,
                       journal: String,
                       year: String,
                       volume: String,
                       number: String,
                       pages: String,
                       doi: Option[String],
                       createTime: Date = new Date)


object Publication extends MongoDocument {
  val collectionName = "article"
  val collection: Future[BSONCollection] = db.map(_.collection(collectionName))

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  implicit def userWriter: BSONDocumentWriter[Publication] = Macros.writer[Publication]

  implicit def userReader: BSONDocumentReader[Publication] = Macros.reader[Publication]


  def create(article: Publication): Future[(Boolean, String)] = collection.flatMap {
    c =>
      c.insert(article).flatMap(
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
    create(Publication(
      UUID.randomUUID().toString,
      "词向量聚类加权TextRank的关键词抽取研究",
      List("夏天"),
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

