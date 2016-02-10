package foo

import scala.language.implicitConversions
import scalaz.{ValidationNel, Functor}
import scalaz.Scalaz._

abstract class TrafficLight

case object Red extends TrafficLight

case object Yellow extends TrafficLight

case object Green extends TrafficLight


trait CanCrossy[A] {
  self =>
  def crossys(a: A): Boolean
}

object CanCrossy {
  def apply[A](implicit ev: CanCrossy[A]): CanCrossy[A] = ev

  def crossys[A](f: A => Boolean): CanCrossy[A] = new CanCrossy[A] {
    def crossys(a: A): Boolean = f(a)
  }
}

trait CanCrossyOps[A] {
  def self: A

  implicit def F: CanCrossy[A]

  final def crossy: Boolean = F.crossys(self)
}

object ToCanIsCrossyOps {
  implicit def toCanIsCrossyOps[A](v: A)(implicit ev: CanCrossy[A]) =
    new CanCrossyOps[A] {
      def self = v

      implicit def F: CanCrossy[A] = ev
    }
}


sealed abstract class RootResult[T]

case class Real[T](val v: T) extends RootResult[T]

case class Img[T](val v: T) extends RootResult[T]

object Foo {

  implicit val trafficLightCanCrossy: CanCrossy[TrafficLight] = CanCrossy.crossys({
    case Red => false
    case Yellow => false
    case _ => true
  })

  implicit val rootResultFunctor: Functor[RootResult] = new Functor[RootResult] {
    override def map[A, B](fa: RootResult[A])(f: (A) => B): RootResult[B] = fa match {
      case Real(v: A) => Real[B](f(v))
      case Img(v: A) => Img[B](f(v))
    }
  }

  case class MyFoo(a: Int, b: Char, c: String)

  type ErrorsOr[A] = ValidationNel[String, A]
  type Validator[A] = String => ErrorsOr[A]

  val checkA: Validator[Int] = (s: String) =>
    try s.toInt.success catch {
      case _: NumberFormatException => "Not a number!".failureNel
    }

  val checkB: Validator[Char] = (s: String) =>
    if (s.size != 1 || s.head < 'a' || s.head > 'z') {
      "Not a lower case letter!".failureNel
    } else s.head.success

  val checkC: Validator[String] = (s: String) =>
    if (s.size == 4) s.success else "Wrong size!".failureNel


  def validateFoo(a: String, b: String, c: String) =
    (checkA(a) |@| checkB(b) |@| checkC(c)) (MyFoo.apply _)

  val personMap = Map[String, String]()
  personMap.get("Name") match {
    case Some(person) => person.getAddress() match {
      case Some(address) => address.getCity() match {
        case Some(city) => process(city)
        case None => Unit
      }
      case None => Unit
    }
    case None => Unit
  }
}
