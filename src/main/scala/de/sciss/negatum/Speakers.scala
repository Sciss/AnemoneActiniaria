/*
 *  Speakers.scala
 *  (Negatum)
 *
 *  Copyright (c) 2016 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.negatum

import de.sciss.negatum.Delaunay.{TriangleIndex, Vector2}
import de.sciss.numbers
import de.sciss.synth.GE

import scala.Predef.{any2stringadd => _}
import scala.collection.immutable.{IndexedSeq => Vec}

object Speakers {
  val even: Vec[Vector2] = Vector(
    Vector2(0.00000f, 134.014f),
    Vector2(107.443f, 0f),
    Vector2(322.330f, 0f),
    Vector2(537.217f, 0f),
    Vector2(537.217f, 135.360f),
    Vector2(752.104f, 0f),
    Vector2(966.991f, 0f),
    Vector2(1181.88f, 0f),
    Vector2(1181.88f, 229.738f),
    Vector2(0f, 402.041f),
    Vector2(802.102f, 459.475f),
    Vector2(991.990f, 459.475f),
    Vector2(1181.88f, 459.475f),
    Vector2(1383.19f, 459.475f),
    Vector2(1383.19f, 688.924f),
    Vector2(1181.88f, 689.213f),
    Vector2(1282.53f, 804.081f),
    Vector2(1050.56f, 804.081f),
    Vector2(787.919f, 804.081f),
    Vector2(525.279f, 804.081f),
    Vector2(525.279f, 627.709f),
    Vector2(393.959f, 804.081f),
    Vector2(131.320f, 804.081f),
    Vector2(0.00000f, 670.068f)  )

  val evenCorners: Vec[Vector2] = Vector(
    Vector2(   0.00f,   0.000f),
    Vector2(   0.00f, 804.081f),
    Vector2(1383.19f, 804.081f),
    Vector2(1383.19f,   0.000f)
  )

  val odd: Vec[Vector2] = Vector(
    Vector2(0.00000f, 0f),
    Vector2(214.887f, 0f),
    Vector2(429.774f, 0f),
    Vector2(644.661f, 0f),
    Vector2(859.548f, 0f),
    Vector2(1074.43f, 0f),
    Vector2(1181.88f, 114.869f),
    Vector2(0.00000f, 536.054f),
    Vector2(0.00000f, 268.027f),
    Vector2(127.279f, 335.034f),
    Vector2(897.046f, 459.475f),
    Vector2(1086.93f, 459.475f),
    Vector2(1181.88f, 344.606f),
    Vector2(1282.53f, 459.475f),
    Vector2(1181.88f, 574.344f),
    Vector2(1383.19f, 583.421f),
    Vector2(1383.19f, 804.081f),
    Vector2(1181.88f, 804.081f),
    Vector2(919.239f, 804.081f),
    Vector2(656.599f, 804.081f),
    Vector2(525.279f, 539.523f),
    Vector2(525.279f, 715.895f),
    Vector2(262.640f, 804.081f),
    Vector2(0.00000f, 804.081f)
  )

  /** Speakers selected for the projection. */
  val select : Vec[Vector2]        = even ++ evenCorners
  /** Delaunay triangulation indices. */
  val tri    : Vec[TriangleIndex]  = Delaunay(select)

  /** Minimum horizontal coordinate in `select`. */
  val minX: Float = select.minBy(_.x).x
  /** Minimum vertical coordinate in `select`. */
  val minY: Float = select.minBy(_.y).y
  /** Maximum horizontal coordinate in `select`. */
  val maxX: Float = select.maxBy(_.x).x
  /** Maximum vertical coordinate in `select`. */
  val maxY: Float = select.maxBy(_.y).y

  /** Selected speakers with coordinates normalized to (0, 1) */
  val selectN: Vec[Vector2] = select.map { case Vector2(x, y) =>
    import numbers.Implicits._
    Vector2(x.linlin(minX, maxX, 0, 1), y.linlin(minY, maxY, 0, 1))
  }

  final case class Proj(x: Float, y: Float, loc: Float) {
    def inside: Boolean = 0 <= loc && loc <= 1
  }

  final case class ProjGE(x: GE, y: GE, loc: GE) {
    def inside: GE = 0 <= loc & loc <= 1
  }

  /** Altitude projections for each triangle. */
  val altitudeProjections: Vec[(Proj, Proj, Proj)] = tri.map { case TriangleIndex(i1, i2, i3) =>
    val v1    = select(i1)
    val v2    = select(i2)
    val v3    = select(i3)
    val alt1  = projectPointOntoLineSegment(v2.x, v2.y, v3.x, v3.y, v1.x, v1.y)
    val alt2  = projectPointOntoLineSegment(v3.x, v3.y, v1.x, v1.y, v2.x, v2.y)
    val alt3  = projectPointOntoLineSegment(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y)
    (alt1, alt2, alt3)
  }

  /** Altitude projections for each triangle. */
  val altitudeProjectionsN: Vec[(Proj, Proj, Proj)] = tri.map { case TriangleIndex(i1, i2, i3) =>
    val v1    = selectN(i1)
    val v2    = selectN(i2)
    val v3    = selectN(i3)
    val alt1  = projectPointOntoLineSegment(v2.x, v2.y, v3.x, v3.y, v1.x, v1.y)
    val alt2  = projectPointOntoLineSegment(v3.x, v3.y, v1.x, v1.y, v2.x, v2.y)
    val alt3  = projectPointOntoLineSegment(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y)
    (alt1, alt2, alt3)
  }

  def projectPointOntoLineSegment(v1x: Float, v1y: Float, v2x: Float, v2y: Float, px: Float, py: Float): Proj = {
    val dvx   = v2x - v1x
    val dvy   = v2y - v1y
    val dpx   = px - v1x
    val dpy   = py - v1y
    val dot   = dvx * dpx + dvy * dpy
    val len   = dvx * dvx + dvy * dvy
    val f     = dot / len
    val prjX  = v1x + dvx * f
    val prjY  = v1y + dvy * f
    Proj(x = prjX, y = prjY, loc = f)
  }

  def projectPointOntoLineSegmentGE(v1x: Float, v1y: Float, v2x: Float, v2y: Float, px: GE, py: GE): ProjGE = {
    val dvx   = v2x - v1x
    val dvy   = v2y - v1y
    val dpx   = px - v1x
    val dpy   = py - v1y
    val dot   = dvx * dpx + dvy * dpy
    val len   = dvx * dvx + dvy * dvy
    val f     = dot / len
    val prjX  = v1x + dvx * f
    val prjY  = v1y + dvy * f
    ProjGE(x = prjX, y = prjY, loc = f)
  }

  def projectPointLineLoc(v1x: Float, v1y: Float, v2x: Float, v2y: Float, px: GE, py: GE): GE = {
    val dvx   = v2x - v1x
    val dvy   = v2y - v1y
    val dpx   = px - v1x
    val dpy   = py - v1y
    val dot   = dvx * dpx + dvy * dpy
    val len   = dvx * dvx + dvy * dvy
    val f     = dot / len
    //    val prjX  = v1x + dvx * f
    //    val prjY  = v1y + dvy * f
    f
  }
}