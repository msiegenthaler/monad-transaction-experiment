package example

import mt._
import Transaction._

case class Price(dollars: Long) {
  def +(other: Price) = Price(dollars + other.dollars)
}

/** Simulates external ressource such as webservice. */
trait CarEstimator {

  def estimate(car: Car) = {
    val input = <car-name>{ car.name }</car-name>
    for {
      result <- Webservice("http://carestimator.org/estimate", input)
      //just check if we got a result..
      r = (result \ "result" \ "you-asked-for").size * 2000
    } yield Price(r)
  }

  def estimate(cars: List[Car]): Transaction[List[Price]] = cars.mapTransaction(estimate)

  def submitSale(car: Car, price: Price) = {
    val msg = <sale><car>{ car.name }</car><price>{ price.dollars }</price></sale>
    Messaging.send("carestimator.sales", msg.toString)
  }

}