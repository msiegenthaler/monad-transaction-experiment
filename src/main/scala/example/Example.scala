package example

import mt._
import javax.sql.DataSource

object Example extends App {
  // Usage example below
  val carDomain = CarDomain(null, null)
  import CarDomain.Services._

  //boot
  {
    val setup = for {
      _ <- carService.buy("Ford Mustard")
      _ <- carService.buy("Opel Astro")
    } yield ()

    carDomain.openBatch(setup)
  }

  /// example
  {
    val session = carDomain.open(User("Mario", SamlAssertion("secret")))

    val quote = session { carService.quoteFor("Ford Mustard") }
    quote.filter(_.price.dollars < 100).foreach { quote =>
      println(s"Do I really want to sell ${quote.car.name} for ${quote.price.dollars}")
      session { carService.sell(quote.car, quote.price) }
      println("sold car")
    }
  }

}