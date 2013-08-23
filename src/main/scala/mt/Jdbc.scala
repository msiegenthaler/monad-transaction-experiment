package mt

import javax.sql.DataSource
import java.sql.Connection

trait JdbcContext extends TransactionContext {
  val jdbc: Jdbc
  trait Jdbc {
    protected val dataSource: DataSource
    lazy val connection = dataSource.getConnection
  }
}

object Jdbc {
  def apply[A](f: Connection => A) = Transaction.lift(_ match {
    case context: JdbcContext => f(context.jdbc.connection)
    case _ => throw new MatchError("Transaction does not support JDBC.")
  })
}