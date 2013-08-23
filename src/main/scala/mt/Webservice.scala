package mt

import scala.xml.Elem

case class SamlAssertion(data: String)

trait WebserviceContext extends TransactionContext {
  val webservice: Webservice
  trait Webservice {
    def assertion: SamlAssertion
  }
}

object Webservice {
  def apply(url: String, input: Elem) = Transaction.lift(_ match {
    case context: WebserviceContext =>
      println(s"--> Calling Webservice $url")
      <response>
        <you-asked-for>{ input }</you-asked-for>
      </response>
    case _ => throw new MatchError("Transaction does not support Werbservice-Calls.")
  })
}