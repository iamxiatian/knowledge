package xiatian.knowledge.models

import org.slf4j.LoggerFactory
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/**
  * 所有MongoDB的抽象类
  */
abstract class MongoDocument {

  //def collectionName: String = ???

  val log = LoggerFactory.getLogger("MongoClient")

  val driver = MongoDriver()
  val parsedUri = MongoConnection.parseURI("mongodb://localhost:27017/knowledge")

  def connection: Try[MongoConnection] = parsedUri.map(driver.connection(_))

  // Database and collections: Get references
  val futureConnection: Future[MongoConnection] = Future.fromTry(connection)

  val db: Future[DefaultDB] = futureConnection.flatMap(_.database("knowledge"))

  /**
    * 初始化数据库，包括插入初始数据，建立索引等任务
    *
    * @return 异步返回一个二元组：第一个布尔值表示是否成功，第二个字符串表示提示信息
    */
  def init(): Future[(Boolean, String)] = ???

  def terminate() = driver.system.terminate()
}