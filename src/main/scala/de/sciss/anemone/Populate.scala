/*
 *  Populate.scala
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
                        (implicit tx: S#Tx): Unit = {
    implicit val _n: Nuages[S] = n
    val dsl = nuages.DSL[S]
    import dsl._
    import synth._
    import ugen._

    Mutagens          (dsl, sConfig, nConfig)
    FifteenBeeThreeCee(dsl, sConfig, nConfig)
//    Imperfect         (dsl, sConfig, nConfig)
    // ScissProcs        (sConfig, nConfig, ...)
//    Cracks            (dsl, sConfig, nConfig)

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

    (nConfig.micInputs ++ nConfig.lineInputs).find(c => c.name == "i-mkv" || c.name == "beat").foreach { cfg =>
      generator("a~beat") {
        shortcut = "B"
        val off     = cfg.indices // .offset
        val pThresh = pAudio("thresh", ParamSpec(0.01, 1, ExpWarp), default(0.1))
        val in      = Trig1.ar(PhysicalIn.ar(off) - pThresh, 0.02)
        val pDiv    = pAudio("div", ParamSpec(1, 16, IntWarp), default(1.0))
        val pulse   = PulseDivider.ar(in, pDiv)
        val pTime   = pAudio("time", ParamSpec(0.0 , 1.0), default(0.0))
        val sig     = DelayN.ar(pulse, 1.0, pTime)
        sig
      }
    }
  }
}