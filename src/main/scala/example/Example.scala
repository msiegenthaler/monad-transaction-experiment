package example

import mt._
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource

object Example extends App {
  val dataSource = new JdbcDataSource
  dataSource.setURL("jdbc:h2:mem:car")

  val messaging = new ConnectionFactory

  // Usage example below
  val carDomain = CarDomain(messaging, dataSource)
  import CarDomain.Services._

  //boot
  {
    val setup = for {
      _ <- carService.buy("Ford Mustard")
      _ <- carService.buy("Opel Astro")
    } yield ()

    println("Setup will buy a few cars, so we're ready to go")
    carDomain.openBatch(setup)
  }
  println("Everything set up")

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