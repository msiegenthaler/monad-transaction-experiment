package example

import scala.concurrent._
import scala.concurrent.duration._
import dispatch._
import mt._
import Transaction._

case class Price(dollars: Long) {
  def +(other: Price) = Price(dollars + other.dollars)
}

/** Simulates external ressource such as webservice. */
trait CarEstimator {
  protected def estimatorUrl = "http://www.random.org/integers/"
  protected def min = 0
  protected def max = 100000
  protected implicit def executor = ExecutionContext.global

  def estimate(car: Car) = {
    Webservice.get {
      url(estimatorUrl).
        addQueryParameter("num", "1").
        addQueryParameter("base", "10").
        addQueryParameter("col", "1").
        addQueryParameter("format", "plain").
        addQueryParameter("rnd", "new").
        addQueryParameter("min", min.toString).
        addQueryParameter("max", max.toString)
    }.map(_.getResponseBody).map((p: String) => Price(p.trim.toLong))
  }

  def estimate(cars: List[Car]): Transaction[List[Price]] = cars.mapTransaction(estimate)

  def submitSale(car: Car, price: Price) = {
    val msg = <sale><car>{ car.name }</car><price>{ price.dollars }</price></sale>
    Messaging.send("carestimator.sales", msg.toString)
  }

}