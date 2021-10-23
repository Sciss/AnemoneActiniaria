/*
 *  Delaunay.scala
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

package de.sciss.negatum

import scala.collection.immutable.{IndexedSeq => Vec}

/** Delaunay triangulation.
  *
  * Original author: Martin Andrews
  * Original project: https://github.com/mdda/DelaunayScala
  * License: GPL v2
  *
  * Edited by H.H.Rutz 2016
  */
object Delaunay {
  final case class Vector2(x: Float, y: Float)

  /** A triangle given as the index of the three corner points
    * into some known sequence of points (e.g. `Vector2` instances).
    */
  final case class TriangleIndex(p1: Int, p2: Int, p3: Int)

  /** Takes as input a list of vertices (need not be sorted).
    * Returns a list of triangular corners, these triangle corners are arranged in a consistent clockwise order.
    *
    * For the Bourke method, see:
    * http://paulbourke.net/papers/triangulate/ for original source (heavily modified here)
    */
  def apply(measurements: Vec[Vector2]): Vec[TriangleIndex] = {

    val nPoints = measurements.length

    // Find the maximum and minimum vertex bounds, to allow calculation of the bounding triangle
    val pMin      = Vector2(measurements.minBy(_.x).x, measurements.minBy(_.y).y) // Top Left
    val pMax      = Vector2(measurements.maxBy(_.x).x, measurements.maxBy(_.y).y) // Bottom Right
    val diameter  = math.max(pMax.x - pMin.x, pMax.y - pMin.y)
    val pMid      = Vector2((pMin.x + pMax.x) / 2, (pMin.y + pMax.y) / 2)

    /*
      Set up the super-triangle, which is a triangle which encompasses all the sample points.
      The super-triangle coordinates are added to the end of the vertex list.
      The super-triangle is the first triangle in the triangle list.
    */

    val pointList = measurements :+
      Vector2(pMid.x - 2 * diameter, pMid.y - 1 * diameter) :+
      Vector2(pMid.x - 0 * diameter, pMid.y + 2 * diameter) :+
      Vector2(pMid.x + 2 * diameter, pMid.y - 1 * diameter)

    val mainCurrentTriangles    = Vec(TriangleIndex(nPoints + 0, nPoints + 1, nPoints + 2)) // initially containing the super-triangle
    val mainCompletedTriangles  = Vec.empty[TriangleIndex]  // initially empty

    def convertRelevantTrianglesIntoNewEdges(completedTriangles: Vec[TriangleIndex], triangles: Vec[TriangleIndex],
                                             point: Vector2): (Vec[TriangleIndex], Vec[TriangleIndex], EdgeAnnihilationSet) =
      triangles.foldLeft((completedTriangles, Vec.empty[TriangleIndex], EdgeAnnihilationSet(Set.empty[EdgeIndex]))) {
        case ((completed, current, edges), triangle) =>
          // If the point 'pointBeingAdded' lies inside the circumcircle then the three edges
          // of that triangle are added to the edge buffer and that triangle is removed.

          // Find the coordinates of the points in this incomplete triangle
          val corner1 = pointList(triangle.p1)
          val corner2 = pointList(triangle.p2)
          val corner3 = pointList(triangle.p3)

          val Circumcircle(inside, circle, r) = circumcircle(point, corner1, corner2, corner3)

          // have we moved too far in x to bother with this one ever again? (initial point list must be sorted for this to work)
          if (circle.x + r < point.x) {  // (false &&) to disable the 'completed' optimisation
            (triangle +: completed, current, edges) // Add this triangle to the 'completed' accumulator, and don't add it on current list
          }
          else {
            if (inside) {
              // Add the triangle's edge onto the edge pile, and remove the triangle
              val edgesWithTriangleAdded =
              edges
                .add(EdgeIndex(triangle.p1, triangle.p2))
                .add(EdgeIndex(triangle.p2, triangle.p3))
                .add(EdgeIndex(triangle.p3, triangle.p1))

              (completed, current, edgesWithTriangleAdded)
            }
            else {
              (completed, triangle +: current, edges) // This point was not inside this triangle - just add it to the 'current' list
            }
          }
      }

    def updateTriangleListForNewPoint(completedTriangles: Vec[TriangleIndex], triangles: Vec[TriangleIndex],
                                      iPoint: Int): (Vec[TriangleIndex], Vec[TriangleIndex]) = {
      val (completedTrianglesUpdated, currentTrianglesUpdated, edgesCreated) =
        convertRelevantTrianglesIntoNewEdges(completedTriangles, triangles, pointList(iPoint))

      // Form new triangles for the current point, all edges arranged in clockwise order.
      val newTriangles = for (e <- edgesCreated.toVec) yield TriangleIndex(e.p1, e.p2, iPoint)
      (completedTrianglesUpdated, newTriangles ++ currentTrianglesUpdated)
    }

    // Go through points in x ascending order. No need to sort the actual points, just output the iPoint in correct sequence
    // (relies on sortBy being 'stable' - so that sorting on y first will enable duplicate detection afterwards)
    // XXX TODO --- calling `sortBy` twice doesn't make sense
    val pointsSortedXYAscending = pointList.take(nPoints).zipWithIndex
      .sortBy(_._1.y)
      .sortBy(_._1.x).map { case (Vector2(x, y), i) => i }

    val pointsSortedAndDeduped =
      pointsSortedXYAscending.foldLeft((Nil: List[Int], -1)) {
        case ((list, pointLast), iPoint) => if (pointLast >= 0 && pointList(pointLast) == pointList(iPoint)) {
          printf(s"Skipping duplicate points {$pointLast,$iPoint}\n")
          (list, iPoint) // Don't add this point to the list
        }
        else
          (iPoint :: list, iPoint)
      }._1.reverse // list of points is first element of tuple, and were pre-pended, so list needs reversing

    // Add each (original) point, one at a time, into the existing mesh
    val (finalCompleted, finalTriangles) =
    pointsSortedAndDeduped.foldLeft((mainCompletedTriangles, mainCurrentTriangles)) {
      case ((completed, current), iPoint) => updateTriangleListForNewPoint(completed, current, iPoint)
    }

    val fullListOfTriangles = finalCompleted ++ finalTriangles
    // filter out triangles with points that have iPoint >= nPoints (since these are part of the fake super-triangle)
    fullListOfTriangles.filterNot(t => t.p1 >= nPoints || t.p2 >= nPoints || t.p3 >= nPoints)
  }


  private final case class EdgeIndex(p1: Int, p2: Int)

  private final case class Circumcircle(inside: Boolean, center: Vector2, radius: Float)

  private final case class EdgeAnnihilationSet(s: Set[EdgeIndex]) {
    def add(e: EdgeIndex): EdgeAnnihilationSet = {
      val s1 = if (s.contains(e)) {
        printf(s"FOUND $e NOT REVERSED *********************\n")
        s - e
      } else {
        val eReversed = EdgeIndex(e.p2, e.p1)
        if (s.contains(eReversed)) {
          s - eReversed
        } else {
          s + e
        }
      }
      EdgeAnnihilationSet(s1)
    }

    def toVec: Vec[EdgeIndex] = s.toIndexedSeq
  }

  private[this] final val EPSILON = 0.0000001

  //  Return TRUE if a point q(x,y) is inside the circumcircle made up of the points p1(x,y), p2(x,y), p3(x,y)
  //  The circumcircle centre (x,y) is returned and the radius r
  //  NOTE: A point on the edge is inside the circumcircle
  private def circumcircle(q: Vector2, p1: Vector2, p2: Vector2, p3: Vector2): Circumcircle = {
    if (math.abs(p1.y - p2.y) < EPSILON && math.abs(p2.y - p3.y) < EPSILON) {
      Console.err.println("circumcircle: Points are colinear")
      println("circumcircle: Points are colinear *****************************")
      Circumcircle(inside = false, Vector2(0, 0), 0f)
    }
    else {
      val mid1 = Vector2((p1.x + p2.x) / 2, (p1.y + p2.y) / 2)
      val mid2 = Vector2((p2.x + p3.x) / 2, (p2.y + p3.y) / 2)

      val c =
        if (math.abs(p2.y - p1.y) < EPSILON) {
          val d2 = -(p3.x - p2.x) / (p3.y - p2.y)
          val xc = mid1.x
          val yc = d2 * (xc - mid2.x) + mid2.y
          Vector2(xc, yc)
        }
        else if (math.abs(p3.y - p2.y) < EPSILON) {
          val d1 = -(p2.x - p1.x) / (p2.y - p1.y)
          val xc = mid2.x
          val yc = d1 * (xc - mid1.x) + mid1.y
          Vector2(xc, yc)
        }
        else {
          val d1 = -(p2.x - p1.x) / (p2.y - p1.y)
          val d2 = -(p3.x - p2.x) / (p3.y - p2.y)
          val xc = ((d1 * mid1.x - mid1.y) - (d2 * mid2.x - mid2.y)) / (d1 - d2)
          val yc = d1 * (xc - mid1.x) + mid1.y
          Vector2(xc, yc)
        }

      val rSqr = {
        val (dx, dy) = (p2.x - c.x, p2.y - c.y) // Distance from (any) 1 point on triangle to circle center
        dx * dx + dy * dy
      }
      val qSqr = {
        val (dx, dy) = (q.x - c.x, q.y - c.y) // Distance from queryPoint to circle center
        dx * dx + dy * dy
      }

      Circumcircle(qSqr <= rSqr, c, math.sqrt(rSqr).toFloat)
    }
  }

  def mkProcessing(measurements: Vec[Vector2]): Unit = {
    val triangles = Delaunay(measurements)

    printf("\n\n// Copy the following into a Processing sketch\n")

    printf("size(400, 400); noFill();\n")

    measurements.foreach { v =>
      printf(s"rect(${v.x}-3, ${v.y}-3, 5, 5);\n")
    }
    triangles.foreach { t =>
      printf("beginShape(TRIANGLES);\n")
      printf(s" vertex(${measurements(t.p1).x}, ${measurements(t.p1).y});\n")
      printf(s" vertex(${measurements(t.p2).x}, ${measurements(t.p2).y});\n")
      printf(s" vertex(${measurements(t.p3).x}, ${measurements(t.p3).y});\n")
      printf("endShape();\n")
    }
  }

  def main(args: Array[String]): Unit = {
    val nv = if (args.length > 0) args(0).toInt else -1
    val n = if (nv <= 0 || nv > 1000) 20 else nv

    printf(s"\n\n// Creating $n random points.\n\n")
    val measurements = for {i <- 0 until n} yield Vector2((i * 400.0 / n).toFloat, (math.random() * 400).toFloat)
    mkProcessing(measurements)
  }

  //  /* Don't use this : It's O(n^4) */
  //  def triangulationN4(measurements: List[Vector2]): List[ITriangle] = {
  //    val n = measurements.length
  //
  //    val x = measurements.map { _.x }
  //    val y = measurements.map { _.y }
  //    val z = (x zip y).map { case (a, b) => a * a + b * b }
  //
  //    def isAddable(i: Int, j: Int, k: Int): Boolean = {
  //      val xn = (y(j) - y(i)) * (z(k) - z(i)) - (y(k) - y(i)) * (z(j) - z(i))
  //      val yn = (z(j) - z(i)) * (x(k) - x(i)) - (z(k) - z(i)) * (x(j) - x(i))
  //      val zn = (x(j) - x(i)) * (y(k) - y(i)) - (x(k) - x(i)) * (y(j) - y(i))
  //
  //      val flag = (zn < 0) && (0 until n).forall {
  //        m => ((x(m) - x(i)) * xn + (y(m) - y(i)) * yn + (z(m) - z(i)) * zn) <= 0
  //      }
  //      flag
  //    }
  //
  //    val res = for {
  //      i <- 0 until (n - 2)
  //      j <- (i + 1) until n
  //      k <- (i + 1) until n
  //      if j != k
  //      if isAddable(i, j, k)
  //    } yield {
  //      printf(s"Adding ($i, $j, $k)\n")
  //      ITriangle(i, j, k)
  //    }
  //    res.toList
  //  }
}