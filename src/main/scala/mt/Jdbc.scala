package mt

import javax.sql.DataSource
import java.sql.Connection

trait Jdbc extends TransactionContext with CommitRollbackAware {
  protected val dataSource: DataSource

  lazy val jdbcConnection = dataSource.getConnection

  protected override def commit = {
    println("-- JDBC commited")
    jdbcConnection.commit
    super.commit
  }
  protected override def rollback = {
    println("-- JDBC commited")
    jdbcConnection.rollback
    super.rollback
  }
}

object Jdbc {
  def apply[A](f: Connection => A) = Transaction.lift(_ match {
    case t: Jdbc => f(t.jdbcConnection)
    case _ => throw new MatchError("Transaction does not support JDBC.")
  })
}