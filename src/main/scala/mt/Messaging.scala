package mt

import javax.sql.DataSource
import java.sql.Connection

// so we don't need jms
class ConnectionFactory {
  def createConnection = null
}

trait MessagingContext extends TransactionContext {
  val messaging: Messaging
  trait Messaging {
    protected val connectionFactory: ConnectionFactory
    lazy val connection = connectionFactory.createConnection
  }
}

object Messaging {
  def send(queue: String, msg: String) = Transaction.lift(_ match {
    case context: MessagingContext =>
      val c = context.messaging.connection
      println(s"--> Send message $msg to $queue")
    case _ => throw new MatchError("Transaction does not support Messaging.")
  })
}