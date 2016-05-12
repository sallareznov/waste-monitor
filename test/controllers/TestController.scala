package controllers

import controllers.api.ApiHomeController
import org.mockito.Mockito._
import org.scalatestplus.play._
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.{ExecutionContext, Future}

class TestController(implicit ec: ExecutionContext) extends PlaySpec with Results {

  "Example Page#index" should {
    "should be valid" in {
      val messagesApi = mock[MessagesApi]
      val controller = new ApiHomeController(messagesApi)(ec)
      val result: Future[Result] = controller.index().apply(FakeRequest())
      val bodyText: String = contentAsString(result)
      bodyText mustBe "ok"
    }
  }

}
