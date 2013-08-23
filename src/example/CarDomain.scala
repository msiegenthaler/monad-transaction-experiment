package example

import javax.sql.DataSource
import mt._

case class User(name: String, assertion: SamlAssertion)
trait Session {
  def user: User
  def apply[A](transaction: Transaction[A]): A
}

trait CarDomain {
  def open(implicit forUser: User): Session
  def openBatch: Session
}

object CarDomain {
  def apply(messaging: ConnectionFactory, database: DataSource): CarDomain = new CarDomain {
    override def open(implicit forUser: User): Session = new Session {
      override val user = forUser
      override def apply[A](transaction: Transaction[A]) = Ctx(user)(transaction)
    }
    val batchUser = User("batch", SamlAssertion("batch"))
    override def openBatch = open(batchUser)
    private case class Ctx(user: User) extends TransactionContext with Messaging with Webservice with Jdbc {
      override val connectionFactory = messaging
      override val dataSource = database
      override val assertion = user.assertion
      def apply[A](t: Transaction[A]): A = {
        try {
          val res = t(this)
          commit
          res
        } catch {
          case t: Throwable =>
            rollback
            throw t
        }
      }
    }
  }

  /** IoC for Services. */
  object Services {
    object estimator extends CarEstimator
    object carRepo extends CarRepository
    object carService extends CarService {
      override protected val repo = carRepo
      override protected val estimator = Services.this.estimator
    }
  }
}