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
import xiatian.knowledge.Settings
import xiatian.knowledge.models.Publication

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
      } ~ (path("api" / "publication" / "list.json") & get & cors(settings)) {

        onSuccess(Publication.findAll(1000)) {
          articles => complete(articles)
        }

      } ~ (path("api" / "publication" / "detail.json")
        & get
        & parameter('id.as[String])
        & cors(settings)) {
        id: String =>
          onSuccess(Publication.findById(id)) {
            case Some(publication) => complete(publication)
            case None =>
              complete(
                StatusCodes.NotFound,
                s"publication does not  exsist for id => $id"
              )
          }
      } ~ (path("api" / "publication" / "list.json" / )
        & get
        & parameters('journal.as[String],'year.as[String])
        & cors(settings)) {
        case (journal:String, year:String) =>
          onSuccess(Publication.find(journal, year)) {
            publications => complete(publications)
          }
      } ~ get {
      // 所有其他请求，都直接访问web目录中的对应内容
      //getFromResourceDirectory("web")
      getFromDirectory("web")
    }

    println(s"Server online at http://0.0.0.0:${Settings.apiServerPort}/")

    //启动服务，并在服务关闭时，解除端口绑定
    Http(system).bindAndHandle(route, "0.0.0.0", Settings.apiServerPort)
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
