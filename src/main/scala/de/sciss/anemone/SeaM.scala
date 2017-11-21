/*
 *  SeaM.scala
 *  (Anemone-Actiniaria)
 *
 *  Copyright (c) 2014-2017 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.anemone

import de.sciss.lucre.stm.Sys
import de.sciss.nuages.{ExpWarp, IntWarp, Nuages, ParamSpec, ScissProcs}
import de.sciss.{nuages, synth}

object SeaM {
  def apply[S <: Sys[S]](dsl: nuages.DSL[S], sCfg: ScissProcs.Config, nCfg: Nuages.Config)
                        (implicit tx: S#Tx, n: Nuages[S]): Unit = {
    import dsl._
    import synth._
    import ugen._

    def default(in: Double): ControlValues =
      if (sCfg.generatorChannels <= 0)
        in
      else
        Vector.fill(sCfg.generatorChannels)(in)

    def mkMix(df: Double = 0.0): GE = pAudio("mix", ParamSpec(0, 1), default(df))

    def mix(in: GE, flt: GE, mix: GE): GE = LinXFade2.ar(in, flt, mix * 2 - 1)

    def mkSpread(arg: GE)(gen: GE => GE): GE = {
      val nM = gen(arg)
      if (sCfg.generatorChannels <= 0) nM else {
        val spread    = pAudio("spread", ParamSpec(0.0, 1.0), default(0.0f))
        val spreadAvg = Mix.mono(spread) / sCfg.generatorChannels
        val pan       = spreadAvg.linlin(0, 1, -1, 1)
        val argAvg    = Mix.mono(arg) / sCfg.generatorChannels
        val n0        = gen(argAvg)
        LinXFade2.ar(n0, nM, pan)
      }
    }

    generator("a~noise0") {
      val freq  = pAudio("freq", ParamSpec(0.01, 1000.0, ExpWarp, unit = "Hz"), default(1.0f))
      val lo    = pAudio("lo"  , ParamSpec(0.0, 1.0), default(0.0f))
      val hi    = pAudio("hi"  , ParamSpec(0.0, 1.0), default(1.0f))
//      val pow   = pAudio("pow" , ParamSpec(0.125, 8, ExpWarp), default(1.0))
      mkSpread(freq)(LFDNoise0.ar).linlin(-1, 1, lo, hi) // .pow(pow)
    }

    generator("a~noise1") {
      val freq  = pAudio("freq", ParamSpec(0.01, 1000.0, ExpWarp, unit = "Hz"), default(1.0f))
      val lo    = pAudio("lo"  , ParamSpec(0.0, 1.0), default(0.0f))
      val hi    = pAudio("hi"  , ParamSpec(0.0, 1.0), default(1.0f))
//      val pow   = pAudio("pow" , ParamSpec(0.125, 8, ExpWarp), default(1.0))
      mkSpread(freq)(LFDNoise1.ar).linlin(-1, 1, lo, hi) // .pow(pow)
    }

    generator("a~brown") {
      val up      = pAudio("up"  , ParamSpec(0.1, 10000.0, ExpWarp), default(1.0f))
      val down    = pAudio("down", ParamSpec(0.1, 10000.0, ExpWarp), default(1.0f))
      val n       = mkSpread(default(1f).seq)(BrownNoise.ar)
      val range   = n.linlin(-1, 1, 0, 1)
      Slew.ar(range, up = up, down = down)
    }

    filter("squiz") { in =>
      val lowest  = 30.0
      val maxZero = 32
      val maxDur  = 1.0 / lowest * maxZero
      val pch     = pAudio("shift", ParamSpec(2, 16     , IntWarp), default(2f))
      val zero    = pAudio("zero" , ParamSpec(1, maxZero /*, IntWarp */), default(1))
      val fade    = mkMix() // mkMix4()
      val wet     = Squiz.ar(in, pitchRatio = pch, zeroCrossings = zero, maxDur = maxDur)
      mix(in, wet, fade)
    }

    filter("squiz") { in =>
      val lowest  = 30.0
      val maxZero = 32
      val maxDur  = 1.0 / lowest * maxZero
      val pch     = pAudio("shift", ParamSpec(2, 16     , IntWarp), default(2f))
      val zero    = pAudio("zero" , ParamSpec(1, maxZero /*, IntWarp */), default(1))
      val fade    = mkMix() // mkMix4()
      val wet     = Squiz.ar(in, pitchRatio = pch, zeroCrossings = zero, maxDur = maxDur)
      mix(in, wet, fade)
    }
  }
}
