package mt

import javax.sql.DataSource
import java.sql.Connection

// so we don't need jms
class ConnectionFactory {
  def createConnection = null
}

trait Messaging extends CommitRollbackAware {
  protected val connectionFactory: ConnectionFactory

  lazy val jmsConnection = connectionFactory.createConnection

  protected override def commit = {
    println("Messaging commited")
    super.commit
  }
  protected override def rollback = {
    println("Messaging commited")
    super.rollback
  }
}

object Messaging {
  def send(queue: String, msg: String) = Transaction.lift(_ match {
    case t: Messaging =>
      val c = t.jmsConnection
      println(s"Sent message $msg to $queue")
    case _ => throw new MatchError("Transaction does not support Messaging.")
  })
}