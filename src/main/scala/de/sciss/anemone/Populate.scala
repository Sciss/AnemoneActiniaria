/*
 *  Populate.scala
 *  (Anemone-Actiniaria)
 *
 *  Copyright (c) 2014-2016 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.anemone

import de.sciss.lucre.stm
import de.sciss.nuages.{ExpWarp, IntWarp, Nuages, ParamSpec, ScissProcs}
import de.sciss.synth.proc.Folder
import de.sciss.synth.proc.Implicits._
import de.sciss.{nuages, synth}

object Populate {

  // private val _registerActions = Ref(initialValue = false)

  // final val BaseDir = userHome/"Documents"/"applications"/"140616_SteiermarkRiga"/"tallin"

  // private final val RecDir  = BaseDir / "rec"

  final val NuagesName = "Nuages"

  def getNuages[S <: stm.Sys[S]](root: Folder[S])(implicit tx: S#Tx): Option[Nuages[S]] =
    (root / NuagesName).flatMap {
      case n: Nuages[S] => Some(n)
      case _ => None
    }

  def apply[S <: stm.Sys[S]](n: Nuages[S], nConfig: Nuages.Config, sConfig: ScissProcs.Config)
                        (implicit tx: S#Tx, cursor: stm.Cursor[S]): Unit = {
    implicit val _n = n
    val dsl = nuages.DSL[S]
    import dsl._
    import synth._
    import ugen._

    Mutagens          (dsl, sConfig, nConfig)
    FifteenBeeThreeCee(dsl, sConfig, nConfig)
    // ScissProcs        (sConfig, nConfig, ???)

    def default(in: Double): ControlValues =
      if (sConfig.generatorChannels <= 0)
        in
      else
        Vector.fill(sConfig.generatorChannels)(in)

    // -------------- TALLINN --------------

//    filter(">mono") { in =>
//      shortcut = "GREATER"
//      Mix.mono(in) / NumChannels(in)
//    }

    // --------------- ANEMONE ----------------

    (sConfig.micInputs ++ sConfig.lineInputs).find(c => c.name == "i-mkv" || c.name == "beat").foreach { cfg =>
      generator("a~beat") {
        shortcut = "B"
        val off     = cfg.offset
        val pThresh = pAudio("thresh", ParamSpec(0.01, 1, ExpWarp), default(0.1))
        val in      = Trig1.ar(PhysicalIn.ar(off) - pThresh, 0.02)
        val pDiv    = pAudio("div", ParamSpec(1, 16, IntWarp), default(1.0))
        val pulse   = PulseDivider.ar(in, pDiv)
        val pTime   = pAudio("time", ParamSpec(0.0 , 1.0), default(0.0))
        val sig     = DelayN.ar(pulse, 1.0, pTime)
        sig
      }
    }

//    def ForceChan(in: GE): GE = if (sConfig.generatorChannels <= 0) in else {
//      ScissProcs.WrapExtendChannels(sConfig.generatorChannels, in)
//    }

//    (sConfig.micInputs ++ sConfig.lineInputs).find(c => c.name == "m-hole" || c.name == "pirro").foreach { cfg =>
//      generator("m-feat") {
//        val off         = cfg.offset
//        val in0         = PhysicalIn.ar(off)
//        val gain0       = pAudio("gain", ParamSpec(-20, 20), default(0))
//        val gain        = (Mix.mono(gain0) / NumChannels(gain0)).dbamp
//        val in          = in0 * gain
//        val pThresh0    = pControl("thresh", ParamSpec(0, 1), default(0.5))
//        val pThresh     = Mix.mono(pThresh0) / NumChannels(pThresh0)
//        val buf         = LocalBuf(numFrames = 1024, numChannels = 1)
//        val chain1      = FFT(buf, in)
//        val onsets      = Onsets      .kr(chain1, pThresh)
//        val loud        = Loudness    .kr(chain1)
//        val cent        = SpecCentroid.kr(chain1)
//        val flat        = SpecFlatness.kr(chain1)
//        val loudN       = (loud / 64).clip(0, 1)
//        val centN       = cent.clip(100, 10000).explin(100, 10000, 0, 1)
//        val flatN       = flat.clip(0, 1)
//
//        pAudioOut("loud", ForceChan(loudN))
//        pAudioOut("cent", ForceChan(centN))
//        pAudioOut("flat", ForceChan(flatN))
//        ForceChan(onsets)
//      }
//    }

//    sConfig.micInputs.find(_.name == "m-dpa").foreach { cfg =>
//      generator("a~dpa") {
//        val off = cfg.offset
//        val in  = PhysicalIn.ar(off)
//        val gain = pAudio("gain", ParamSpec(-20, 20), default(0)).dbamp
//        val sig  = in * gain
//        sig
//      }
//    }
  }
}