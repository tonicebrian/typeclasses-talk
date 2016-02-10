# Type classes usage patterns

@tonicebrian

----

## Excursion

- The meaning of functional in FP <!-- .element: class="fragment" data-fragment-index="1" -->
- Programming with effects        <!-- .element: class="fragment" data-fragment-index="2" -->
- Type classes                    <!-- .element: class="fragment" data-fragment-index="3" -->
- Typeclassopedia                 <!-- .element: class="fragment" data-fragment-index="4" -->


---

# The meaning of functional in Functional Programming

----

$\Huge{f}$

----

$\Huge{f(x)}$

----

$\Huge{=}$

----

$\Huge{f(x)=g(x)}$ 

$\Huge{g(x)=f(x)}$ <!-- .element: class="fragment" data-fragment-index="1" -->

Note: this allows us to do equational reasoning

----

$\Huge{f(x) = 5}$

----

$\Huge{f(x) = \pi}$

----

$\Huge{f(x) = sin(x)}$

----

$\Huge{f(x) = \sqrt{x}}$


----

## Take aways

- Given a set of input parameters a function always returns the same result
- Equality means interchangeability not assignment

---

# Programming with effects

----

$$
\Large{\frac{1}{0}}
$$

$$\Large{\sqrt{-1}}$$ <!-- .element: class="fragment" data-fragment-index="1" -->

----

$$
{inv: \mathbb{Z} \rightarrow \mathbb{R}}, \quad inv(x) = ???
$$

----

$$
{inv: \mathbb{Z} \rightarrow \mathbb{R}}, \quad inv(x) = 
 \begin{cases}
  \frac{1}{x} & x \neq 0  \\\\
   ??? & x = 0
  \end{cases}
$$

----

$$
{inv: \mathbb{Z} \rightarrow \mathbb{R}}, \quad inv(x) = 
 \begin{cases}
  \frac{1}{x} & x \neq 0  \\\\
   \varnothing & x = 0
  \end{cases}
$$

How does it look in functional style?
<!-- .element: class="fragment" data-fragment-index="0" -->

```scala
def inv(x:Integer):Option[Double] = if (x!=0) 
                                       Some(1/x)
                                    else 
                                       None 
```
<!-- .element: class="fragment" data-fragment-index="1" -->

----

${sqrt:\mathbb{R} \rightarrow \mathbb{R}}, \quad sqrt(x) = ???$ 

----

$$
{sqrt:\mathbb{R} \rightarrow \mathbb{R}}, \quad sqrt(x) = 
 \begin{cases}
  \sqrt{x} & x \geq 0  \\\\
   ??? & x < 0
  \end{cases}
$$ 

----

$$
{sqrt:\mathbb{R} \rightarrow \mathbb{C}}, \quad sqrt(x) = 
 \begin{cases}
  \sqrt{x} & x \geq 0  \\\\
   \sqrt{-x}j & x < 0
  \end{cases}
$$ 

How does it look in functional style?
<!-- .element: class="fragment" data-fragment-index="0" -->

```scala
sealed abstract class RootResult
case class Real(val v:Double) extends RootResult
case class Img(val v:Double) extends RootResult

def sqrt(x:Double):RootResult = if (x>=0) 
                                   Real(math.sqrt(x))
                                else 
                                   Img(math.sqrt(-x))
```
<!-- .element: class="fragment" data-fragment-index="1" -->

----

## Take aways

There are lots of interesting effects:

- IO
- Futures
- Exceptions
- ...

> In a functional language effects are modelled by mapping those effects to
> algebraic data types so we are still working with pure functions
<!-- .element: class="fragment" data-fragment-index="1" -->

---

# The need for Type classes 

----

## The expression problem (1)

- How to add new cases and functions to a datatype
- How to do so without recompiling and without casting
- Data & operations should be *extensible*

Note: https://en.wikipedia.org/wiki/Expression_problem

----

## The expression problem (2)

- In FP, adding new functions is cheap, modifying data is hard
- In OO, adding new functions is hard, modifying data is cheap

Note:
http://userpages.uni-koblenz.de/~laemmel/paradigms1011/resources/pdf/xproblem.pdf

----

## Example

![semaphore](images/semaphore.jpg)

- Lights are modelled as separate entities
- Light entities can be queried for allowing pass in a street

----

![Three lights](images/three-lights.jpg)

- **QUESTION:** How would you add the `order` functionality in OO?
- **QUESTION:** How would you add the yellow light entity in FP?

----

## An unfortunate name

Type classes are a type construct 

that enforces that a given type belongs to a set 
<!-- .element: class="fragment" data-fragment-index="1" -->

of "like minded" types 
<!-- .element: class="fragment" data-fragment-index="2" -->

So maybe Type Sets?? 
<!-- .element: class="fragment" data-fragment-index="3" -->

----

## This is the API the type class provides

```scala
trait CanCrossy[A] { self =>
  def crossys(a: A): Boolean
}
 
object CanCrossy {
  def apply[A](implicit ev: CanCrossy[A]): CanCrossy[A] = ev
  def crossys[A](f: A => Boolean): CanCrossy[A] = new CanCrossy[A] {
    def crossys(a: A): Boolean = f(a)
  }
}
```

----

## This is the API the type class provides

```scala
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
```

----

## In code we need to import operations in scope

```scala
import ToCanIsCrossyOps._
```

----


## Now our old types have new operations

```scala
abstract class TrafficLight
case object Red extends TrafficLight
case object Yellow extends TrafficLight
case object Green extends TrafficLight
```

```scala
scala> implicit val trafficLightCanCrossy: CanCrossy[TrafficLight] = CanCrossy.crossys({
         case Red    => false
         case Yellow => false
         case _      => true
       })

scala> (Red : TrafficLight).crossy
res6: Boolean = false
```

----

## Kinds

Same as we have functions mapping values from one value in a type to another
value in another type

```scala
f: A => B
```

A kind is the type of a type constructor  <!-- .element: class="fragment" data-fragment-index="0" -->

```scala  
Option : * -> *
```
<!-- .element: class="fragment" data-fragment-index="0" -->

----

# Typeclassopedia

----

## Monoid

```scala
trait Semigroup[A]  { self =>
  def append(a1: A, a2: => A): A
  ...
}
```

```scala
trait Monoid[A] extends Semigroup[A] { self =>
  ////
  /** The identity element for `append`. */
  def zero: A

  ...
}
```

----

## The most used type classes

- Strings with `""` and `+`
- Integers with `0` and `+` or `1` and `*`
- Matrices with `Identity Matrix` and `Matrix Multiplication`

----

## Functor

```scala
trait Functor[F[_]]  { self =>
  /** Lift `f` into `F` and apply to `F[A]`. */
  def map[A, B](fa: F[A])(f: A => B): F[B]

  ...
}
```

Solves the problem of using functions already defined for some types when those
types are embedded in effectful computations

----

## Functor for our RootResult example

```scala
implicit val rootResultFunctor : Functor[RootResult] = new Functor[RootResult] {
  override def map[A, B](fa: RootResult[A])(f: (A) => B): RootResult[B] = fa match {
    case Real(v:A) => Real[B](f(v))
    case Img(v:A) => Img[B](f(v))
  }
}
```

```scala
scala> val F = Functor[RootResult]
F: scalaz.Functor[foo.RootResult] = foo.Foo$$anon$1@67b0d590

scala> F.map(Real(5.0)){x => (x+1).toInt}
res9: foo.RootResult[Int] = Real(6)
```
<!-- .element: class="fragment" data-fragment-index="1" -->

----

## Applicative

What happens when our function to map has more input parameters than `f: A => B`?

```scala
trait Applicative[F[_]] extends Apply[F] { self =>
  def point[A](a: => A): F[A]

  /** alias for `point` */
  def pure[A](a: => A): F[A] = point(a)

  ...
}

trait Apply[F[_]] extends Functor[F] { self =>
  def ap[A,B](fa: => F[A])(f: => F[A => B]): F[B]
}
```

----

## Applicative example

Suppose that you have to validate a form in order to fill some model object

```scala
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
```

----

```scala
def validateFoo(a: String, b: String, c: String) =
   (checkA(a) |@| checkB(b) |@| checkC(c))(MyFoo.apply _)
```

```
scala> validateFoo("5","c", "1234")
res1: foo.Foo.ErrorsOr[foo.Foo.MyFoo] = Success(MyFoo(5,c,1234))

scala> validateFoo("5","B", "123")
res3: foo.Foo.ErrorsOr[foo.Foo.MyFoo] = Failure(NonEmpty[Not a lower case letter!,Wrong size!])
```

----

## Monad

The scary M-word!!!

```scala
trait Monad[F[_]] extends Applicative[F] with Bind[F] { self =>
  ////
}
```

```scala
trait Bind[F[_]] extends Apply[F] { self =>
  /** Equivalent to `join(map(fa)(f))`. */
  def bind[A, B](fa: F[A])(f: A => F[B]): F[B]
}
```

----

A Monad is a structure that represents computations defined as a sequence of steps

----

## Example

The naïve programmer doing some analytics does:

```scala
val person = personMap.get("Name");
process(person.getAddress().getCity());
```

but this can explode in several places. Let's try to be safe:

```scala
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
```


----

## Monadic style

```scala
personMap.get("Name") >>= {_.getAddress} >>= {_.getCity} match {
      case Some(city) => process(city)
      case None => Unit
}
```


----

## Famous monads

- Option
- Future
- List
- State
- Writer 
- Reader
- etc...

----

# Take Away

By substituting effects by data:

- we have less boilerplate
- we could add more effectful computations without writing code
- we could benefit from common constructs on top of familiar concepts, an applicative parser, a monad for deterministic parallelism, etc

---

## References

- [Learning Scalaz](http://eed3si9n.com/learning-scalaz/)
- [Category Theory for
  Programmers](http://bartoszmilewski.com/2014/10/28/category-theory-for-programmers-the-preface/)
- [Validation example](http://meta.plasm.us/posts/2013/06/05/applicative-validation-syntax/)
- [Railway oriented programming](http://www.slideshare.net/ScottWlaschin/railway-oriented-programming)
- [Neural Networks Types and Functional Programming](http://colah.github.io/posts/2015-09-NN-Types-FP/)
