/*
 *  NegatumOutPre.scala
 *  (Anemone-Actiniaria)
 *
 *  Copyright (c) 2014-2022 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.synth.ugen

import de.sciss.synth.{AudioRated, GE, UGenInLike}

object NegatumOutPre {
  var CLIP      = true
  val LEAK_DC   = true
//  var NORMALIZE = false
  var PAN2      = false
  var HIGH_PASS = false
  var LIMITER   = false
//  var AMP       = false
  var FADE_IN   = false
}
final case class NegatumOutPre(in: GE) extends GE.Lazy with AudioRated {
  protected def makeUGens: UGenInLike = {
    import NegatumOutPre._
    import de.sciss.synth.Import._

    val sig0  = Mix.mono(in)
    val isOk  = CheckBadValues.ar(sig0, post = 0) sig_== 0
    val sig1  = Gate.ar(sig0, isOk)
    val sig2  = if (!CLIP     ) sig1 else sig1.clip2(1)
    val sig3  = if (!LEAK_DC  ) sig2 else LeakDC.ar(sig2) * 0.47
    val sig4  = if (!LIMITER  ) sig3 else Limiter.ar(sig3, -0.2.dbAmp)
    val sig5  = if (!HIGH_PASS) sig4 else HPF.ar(sig4, 20)
    val sig6  = sig5
//    if (!NORMALIZE) sig5 else {
//      val env = EnvGen.ar(Env.asr, gate = "gate".kr(1f), doneAction = doNothing /* freeSelf */)
//      val doneEnv = Done.kr(env)
//      val normDur = 2.0
//      val tFree = TDelay.kr(doneEnv, normDur)
//      FreeSelf.kr(tFree)
//      Normalizer.ar(sig5, level = -0.2.dbamp, dur = normDur) * DelayN.ar(env, normDur, normDur)
//    }
//    val bus = "out".kr(0f)
    val sig7 = if (!PAN2  ) sig6 else Pan2.ar(sig6)
    val sig8 = sig7 // if (!AMP   ) sig7 else sig7 * "amp".kr(1f)
    val sig  = if (!FADE_IN) sig8 else {
      val ln0 = Line.ar(start = 0, end = 1, dur = 0.05)
      val ln  = if (!LIMITER) ln0 else DelayN.ar(ln0, 0.1, 0.1)
      sig8 * ln
    }
    sig // Out.ar(bus, sig)
  }
}