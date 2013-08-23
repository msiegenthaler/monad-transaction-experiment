package mt

trait TransactionContext

trait CommitRollbackAware {
  protected def commit = ()
  protected def rollback = ()
}

trait Transaction[A] extends Function1[TransactionContext, A] {
  def apply(t: TransactionContext): A

  import Transaction._
  final def map[B](f: A => B): Transaction[B] = {
    lift(t => f(apply(t)))
  }
  final def flatMap[B](f: A => Transaction[B]): Transaction[B] = {
    lift { t =>
      val a = apply(t)
      f(a)(t)
    }
  }
}

object Transaction {
  def apply[A](a: A): Transaction[A] = lift(_ => a)
  def bind[A](a: A) = apply(a)

  implicit def lift[A](f: TransactionContext => A): Transaction[A] = new Transaction[A] {
    override def apply(t: TransactionContext) = f(t)
  }

  implicit class RichTraversable[A](data: Traversable[A]) {
    def mapTransaction[B](f: A => Transaction[B]): Transaction[List[B]] = {
      data.foldLeft[Transaction[List[B]]](bind(Nil)) { (res, toMap) =>
        res.flatMap(soFar => f(toMap).map(_ :: soFar))
      }
    }
  }

  implicit class TraversableOfTransaction[A](trav: Traversable[Transaction[A]]) {
    def sequence: Transaction[List[A]] = trav.mapTransaction(identity)
  }
}