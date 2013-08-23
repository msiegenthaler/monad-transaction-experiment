package mt

trait TransactionContext

trait Transaction[A] extends Function1[TransactionContext, A] {
  def apply(context: TransactionContext): A

  def run()(implicit context: TransactionContext) = apply(context)

  import Transaction._
  final def map[B](f: A => B): Transaction[B] = {
    lift(context => f(apply(context)))
  }
  final def flatMap[B](f: A => Transaction[B]): Transaction[B] = {
    lift { context =>
      val a = apply(context)
      f(a)(context)
    }
  }
}

object Transaction {
  def apply[A](a: A): Transaction[A] = lift(_ => a)
  def bind[A](a: A) = apply(a)

  implicit def lift[A](f: TransactionContext => A): Transaction[A] = new Transaction[A] {
    override def apply(context: TransactionContext) = f(context)
  }

  implicit def autoApply[A](t: Transaction[A])(implicit context: TransactionContext): A = t(context)

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