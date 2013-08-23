package example

import anorm._
import anorm.SqlParser._
import mt._

case class CarId(id: Long)
case class Car(id: CarId, name: String)

trait CarRepository {
  def all = Jdbc { implicit c =>
    SQL("SELECT * FROM car").as(car *)
  }

  def byName(name: String) = Jdbc { implicit c =>
    SQL("SELECT * FROM car WHERE name = {name}").on("name" -> name).as(car *)
  }

  def byId(id: CarId) = Jdbc { implicit c =>
    SQL("SELECT * FROM car WHERE id = {id}").on("id" -> id.id).as(car.singleOpt)
  }

  def add(name: String) = Jdbc { implicit c =>
    SQL("INSERT INTO car(name) VALUES({name})").on("name" -> name).executeInsert().map(CarId.apply)
  }

  def remove(id: CarId) = Jdbc { implicit c =>
    SQL("DELETE FROM car WHERE id={id}").on("id" -> id.id).executeUpdate()
    ()
  }

  private val car = get[Long]("id") ~ get[String]("name") map {
    case id ~ name => Car(CarId(id), name)
  }
}