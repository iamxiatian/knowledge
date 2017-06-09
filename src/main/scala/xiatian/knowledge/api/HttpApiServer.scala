package xiatian.knowledge.api

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import xiatian.knowledge.models.Article

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn

/**
  * Restful API的服务接口
  */
object HttpApiServer extends JsonSupport {
  implicit val timeout = Timeout(20.seconds)

  private val settings = CorsSettings.defaultSettings.copy(
    allowedOrigins = HttpOriginRange.*
  )

  def start(implicit system: ActorSystem,
            master: Option[ActorRef]): Future[ServerBinding] = {
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher


    val route: Route =
      (path("") & get) {
        //complete(s"Spider ${Settings.version}")
        redirect("index.html", StatusCodes.MovedPermanently)
      } ~ (path("api" / "article" / "list.json") & get & cors(settings)) {

        onSuccess(Article.findAll(1000)) {
          articles => complete(articles)
        }

      } ~ get {
        // 所有其他请求，都直接访问web目录中的对应内容
        //getFromResourceDirectory("web")
        getFromDirectory("web")
      }

    println("Server online at http://localhost:7080/")

    //启动服务，并在服务关闭时，解除端口绑定
    Http(system).bindAndHandle(route, "0.0.0.0", 7080)
  }

  def main(args: Array[String]) {
    val system = ActorSystem("my-system")
    implicit val executionContext = system.dispatcher

    val bindingFuture = start(system, None)

    println(s"Server online at http://localhost:7080/" +
      s"\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
