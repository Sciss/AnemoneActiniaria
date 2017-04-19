package de.sciss

import scala.collection.immutable.{IndexedSeq => Vec}
import scala.language.implicitConversions

package object anemone {
  final class BangBang[A](private val in: () => A) extends AnyVal {
    def !! (n: Int): Vec[A] = Vector.fill(n)(in())
  }
  implicit def BangBang[A](in: => A): BangBang[A] = new BangBang(() => in)
}