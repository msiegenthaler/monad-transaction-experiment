package example

import mt._

case class CarId(id: Long)
case class Car(id: CarId, name: String)

trait CarRepository {
  def all = Jdbc { c =>
    List(Car(CarId(10), "Ford Mustard"))
  }

  def byName(name: String) = all.map(_.filter(_.name == name))

  def byId(id: CarId) = Jdbc { c =>
    val s = c.prepareStatement("SELECT name FROM car WHERE id=?")
    s.setLong(1, id.id)
    val rs = s.executeQuery()
    if (rs.next) Some(Car(id, rs.getString(1)))
    else None
  }

  def add(name: String) = Jdbc { c =>
    // add the stuff
    CarId(100)
  }

  def remove(id: CarId) = Jdbc { c =>
    //remove
    ()
  }
}