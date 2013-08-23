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
  val carDomain = CarDomain(messaging, dataSource)
  import CarDomain.Services._

  //boot
  {
    def ddl = { implicit c: Connection =>
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
    carDomain.openBatch(Jdbc(ddl))
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