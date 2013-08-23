package example

import mt._
import Transaction._

case class QuotedCar(car: Car, price: Price)

trait CarService {
  protected val repo: CarRepository
  protected val estimator: CarEstimator

  def quoteFor(name: String) = for {
    inventory <- repo.byName(name)
    priced <- inventory.mapTransaction(car => estimator.estimate(car).map(QuotedCar(car, _)))
  } yield priced.sortBy(_.price.dollars).headOption

  def sell(car: Car, forPrice: Price) = for {
    car <- repo.byId(car.id).map(_.getOrElse(throw new IllegalStateException("Car already sold")))
    _ <- repo.remove(car.id)
    _ <- estimator.submitSale(car, forPrice)
  } yield ()

  def buy(name: String) = repo.add(name).map(_ => ())
}