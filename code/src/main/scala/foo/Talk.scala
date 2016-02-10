package foo

import scala.language.implicitConversions

abstract class TrafficLight
case object Red extends TrafficLight
case object Yellow extends TrafficLight
case object Green extends TrafficLight


trait CanCrossy[A] { self =>
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

object Foo {

implicit val trafficLightCanCrossy: CanCrossy[TrafficLight] = CanCrossy.crossys({
         case Red    => false
         case Yellow => false
         case _      => true
       })

}
