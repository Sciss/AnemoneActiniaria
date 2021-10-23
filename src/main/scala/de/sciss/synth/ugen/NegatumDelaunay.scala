/*
 *  NegatumDelaunay.scala
 *  (Anemone-Actiniaria)
 *
 *  Copyright (c) 2014-2021 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.synth
package ugen

import de.sciss.negatum.Delaunay.TriangleIndex
import de.sciss.negatum.Speakers
import de.sciss.negatum.Speakers.{altitudeProjectionsN, selectN, tri}
import de.sciss.synth.UGenSource.Vec

/** A graph element that produces an amplitude signal from a spatial
  * position (x, y).
  *
  * @param x  horizontal position (in the `Speakers.select` space),
  *           normalized between 0 and 1
  * @param y  vertical position (in the `Speakers.select` space),
  *           normalized between 0 and 1
  */
final case class NegatumDelaunay(x: GE, y: GE) extends GE.Lazy {
  def rate: MaybeRate = MaybeRate.max_?(x.rate, y.rate)

  private def insideGE(px: GE, py: GE): Vec[GE] = {
    import de.sciss.synth.Import._
    val sq = tri.map { case TriangleIndex(i1, i2, i3) =>
      val v1    = selectN(i1)
      val v2    = selectN(i2)
      val v3    = selectN(i3)
      // cf. https://en.wikipedia.org/wiki/Barycentric_coordinate_system
      val dx3   = px - v3.x
      val dy3   = py - v3.y
      // det of 2x2 matrix: r1c1 * r2c2 - r1c2 * r2c1
      // where r1c1 = x1 - x3, r2c2 = y2 - y3,
      //       r1c2 = x2 - x3, r2c1 = y1 - y3
      val detT  = (v2.y - v3.y) * (v1.x - v3.x) + (v3.x - v2.x) * (v1.y - v3.y)
      val alpha = ((v2.y - v3.y) * dx3 + (v3.x - v2.x) * dy3) / detT
      val beta  = ((v3.y - v1.y) * dx3 + (v1.x - v3.x) * dy3) / detT
      val gamma = 1.0f - alpha - beta
      alpha >= 0 & beta >= 0 & gamma >= 0
    }
    sq
  }

  private def ampGE(px: GE, py: GE): GE = {
    val amps = Array.fill[GE](selectN.size)(Constant.C0)
    val ins  = insideGE(px, py)

    import de.sciss.synth.Import._
    tri.zipWithIndex.foreach { case (TriangleIndex(i1, i2, i3), triIdx) =>
      val v1    = selectN(i1)
      val v2    = selectN(i2)
      val v3    = selectN(i3)
      val (alt1, alt2, alt3) = altitudeProjectionsN(triIdx)
      val a1x   = alt1.x
      val a1y   = alt1.y
      val a2x   = alt2.x
      val a2y   = alt2.y
      val a3x   = alt3.x
      val a3y   = alt3.y

      val loc1 = Speakers.projectPointLineLoc(a1x, a1y, v1.x, v1.y, px, py)
      val loc2 = Speakers.projectPointLineLoc(a2x, a2y, v2.x, v2.y, px, py)
      val loc3 = Speakers.projectPointLineLoc(a3x, a3y, v3.x, v3.y, px, py)

      val amp1 = loc1.sqrt
      val amp2 = loc2.sqrt
      val amp3 = loc3.sqrt

      val in    = ins(triIdx)
      amps(i1) += amp1 * in
      amps(i2) += amp2 * in
      amps(i3) += amp3 * in
    }

    amps.toIndexedSeq
  }

  protected def makeUGens: UGenInLike = ampGE(px = x, py = y)
}
