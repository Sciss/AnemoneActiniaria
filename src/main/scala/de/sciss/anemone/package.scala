/*
 *  package.scala
 *  (Anemone-Actiniaria)
 *
 *  Copyright (c) 2014-2020 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss

import scala.collection.immutable.{IndexedSeq => Vec}
import scala.language.implicitConversions

package anemone {
  final class BangBang[A](private val in: () => A) extends AnyVal {
    def !! (n: Int): Vec[A] = Vector.fill(n)(in())
  }
}
package object anemone {
  implicit def bangBang[A](in: => A): BangBang[A] = new BangBang(() => in)
}