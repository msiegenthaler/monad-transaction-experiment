package mt

import dispatch._
import scala.xml.Elem
import com.ning.http.client.Response
import scala.concurrent.ExecutionContext
import com.ning.http.client.ProxyServer

case class SamlAssertion(data: String)

trait WebserviceContext extends TransactionContext {
  val webservice: Webservice
  trait Webservice {
    def assertion: SamlAssertion
    def exec: ExecutionContext
    def proxy: Option[(String, Int)]
  }
}

object Webservice {
  def get(reqBase: Req) = Transaction.lift(_ match {
    case context: WebserviceContext =>
      implicit val exec = context.webservice.exec
      val req = context.webservice.proxy.map(c => reqBase.setProxyServer(new ProxyServer(c._1, c._2))).getOrElse(reqBase)
      println(s"--> Calling Webservice ${req.url}")
      val future = Http(req.GET)
      future()
    case _ => throw new MatchError("Transaction does not support Werbservice-Calls.")
  })
}