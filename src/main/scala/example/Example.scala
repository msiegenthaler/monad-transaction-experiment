package example

import java.sql.Connection
import org.h2.jdbcx.JdbcDataSource
import anorm._
import mt._

object Example extends App {
  val dataSource = new JdbcDataSource
  dataSource.setURL("jdbc:h2:mem:car")

  val messaging = new ConnectionFactory

  // Usage example below
  val carDomain = CarDomain(messaging, dataSource, Some(("proxy.bedag.ch", 8088)))
  import CarDomain.Services._

  //boot
  {
    def ddl = Jdbc { implicit c: Connection =>
      SQL("CREATE SEQUENCE car_id_seq").execute
      SQL("""CREATE TABLE car(
              id bigint not null default nextval('car_id_seq'),
              name varchar(1023) not null)""").execute
    }

    val setup = for {
      _ <- carService.buy("Ford Mustard")
      _ <- carService.buy("Opel Astro")
    } yield ()

    println("Creating tables")
    carDomain.openBatch(ddl)
    println("Setup will buy a few cars, so we're ready to go")
    carDomain.openBatch(setup)
  }
  println("Everything set up")

  /// example
  {
    val session = carDomain.open(User("Mario", SamlAssertion("secret")))

    val quote = session { carService.quoteFor("Ford Mustard") }
    quote.foreach { quote =>
      println(s"Do I really want to sell ${quote.car.name} for ${quote.price.dollars}$$?")
      if (quote.price.dollars > 50000) {
        println("jep, good deal")
        session { carService.sell(quote.car, quote.price) }
        println("sold car")
      } else {
        println("too cheap")
      }
    }

    val quote2 = session { carService.quoteFor("Ford Mustard") }
    println(s"New quote: $quote2")
  }

}