package xiatian.knowledge.models

import java.text.SimpleDateFormat
import java.util.Date

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros}

// BSON implementation of the count command
import reactivemongo.api.commands.bson.BSONCountCommand.Count

// BSON serialization-deserialization for the count arguments and result
import reactivemongo.api.commands.bson.BSONCountCommandImplicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * 一个用户的基本信息，目前用户即包含唯一的邮箱地址和密码
  *
  * @author Tian Xia
  *         Mar 03, 2017 11:44 AM
  */

/**
  * 用户的基本信息。
  *
  */
case class User(email: String,
                name: String,
                password: String,
                createTime: Date = new java.util.Date)


object User extends MongoDocument {
  val collectionName = "user"
  val collection: Future[BSONCollection] = db.map(_.collection(collectionName))

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  implicit def userWriter: BSONDocumentWriter[User] = Macros.writer[User]

  implicit def userReader: BSONDocumentReader[User] = Macros.reader[User]

  /**
    * 用户创建，返回一个二元组：第一个布尔值表示是否创建成功，第二个字符串值表示创建失败
    * 时的原因
    *
    * @return
    */
  def create(email: String,
             name: String,
             password: String): Future[(Boolean, String)] = collection.flatMap {
    c =>
      c.runCommand(Count(BSONDocument("email" -> email))).flatMap(
        countResult =>
          if (countResult.count == 0) {
            val user = new User(email, name, password)

            c.insert(user).flatMap (
              r =>
                Future((r.ok, if(r.ok) "Success" else r.toString))
            )
          } else {
            Future((false, "邮件地址已经存在"))
          }
      )
  }


  def findByToken(cellphone: String, token: String): Future[Option[User]] = collection.flatMap(
    c =>
      c.find(BSONDocument("cellphone" -> cellphone, "token" -> token))
        .cursor[User]()
        .headOption
  )

  def findByIdToken(id: String, token: String): Future[Option[User]] = collection.flatMap(
    c =>
      c.find(BSONDocument("id" -> id, "token" -> token))
        .cursor[User]()
        .headOption
  )

  def findById(id: String): Future[Option[User]] = collection.flatMap(
    c =>
      c.find(BSONDocument("id" -> id))
        .cursor[User]()
        .headOption
  )

  def findByPasswordMd5(cellphone: String, passwordMd5: String): Future[Option[User]] = collection.flatMap(
    c =>
      c.find(BSONDocument("cellphone" -> cellphone, "passwordMd5" -> passwordMd5))
        .cursor[User]()
        .headOption
  )

  def findByCellphone(cellphone: String): Future[Option[User]] = collection.flatMap(
    c =>
      c.find(BSONDocument("cellphone" -> cellphone))
        .cursor[User]()
        .headOption
  )


  /**
    * 清楚原有数据，方便测试和初始化
    *
    * @return
    */
  def clean(): Future[Boolean] = collection.flatMap { c: BSONCollection =>
    c.drop(false)
  }

  override def init = collection.flatMap(
    c => c.indexesManager.ensure(
      Index(Seq(("cellphone", IndexType.Ascending)), Some("cellphoneIndex"), true)
    ).map(f => (f, if (f) "成功" else "失败"))
  )

}
