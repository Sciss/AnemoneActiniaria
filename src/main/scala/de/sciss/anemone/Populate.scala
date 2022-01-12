/*
 *  Populate.scala
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

package de.sciss.anemone

import de.sciss.lucre.Folder
import de.sciss.lucre.synth.Txn
import de.sciss.nuages.{Nuages, ScissProcs}
import de.sciss.proc.Implicits._
import de.sciss.proc.{ParamSpec, Warp}
import de.sciss.{nuages, synth}

object Populate {

  // private val _registerActions = Ref(initialValue = false)

  // final val BaseDir = userHome/"Documents"/"applications"/"140616_SteiermarkRiga"/"tallin"

  // private final val RecDir  = BaseDir / "rec"

  final val NuagesName = "Nuages"

  def getNuages[T <: Txn[T]](root: Folder[T])(implicit tx: T): Option[Nuages[T]] =
    (root / NuagesName).flatMap {
      case n: Nuages[T] => Some(n)
      case _ => None
    }

  def apply[T <: Txn[T]](n: Nuages[T], nConfig: Nuages.Config, sConfig: ScissProcs.Config)
                        (implicit tx: T): Unit = {
    implicit val _n: Nuages[T] = n
    val dsl = nuages.DSL[T]
    import dsl._
    import synth._
    import ugen._
    import Import._

    Mutagens          (dsl, sConfig, nConfig)
    FifteenBeeThreeCee(dsl, sConfig, nConfig)
    Promenade         (dsl, sConfig, nConfig)
    Almat             (dsl, sConfig, nConfig)
    ShouldGens        (dsl, sConfig, nConfig)
//    PikselGens        (dsl, sConfig, nConfig)
//    Imperfect         (dsl, sConfig, nConfig)
//    Cracks            (dsl, sConfig, nConfig)

    def default(in: Double): ControlValues =
      if (sConfig.genNumChannels <= 0)
        in
      else
        Vector.fill(sConfig.genNumChannels)(in)

    // -------------- TALLINN --------------

//    filter(">mono") { in =>
//      shortcut = "GREATER"
//      Mix.mono(in) / NumChannels(in)
//    }

    // -------------- SEAM --------------

//    val dirUniv = file("/data/projects/SeaM20/audio_work/")
//    val fUniv   = dirUniv / "univ-arr.aif"
//    val locUniv = ArtifactLocation.newConst[T](dirUniv)
//    val procUniv = generator("univ") {
//      val thresh  = -80.dbamp
//      val runIn   = LocalIn.kr(0)
//      val speed   = runIn // * (44100/SampleRate.ir)
//      val disk0   = proc.graph.VDiskIn.ar("file", speed = speed, loop = 0)
//      val disk    = LeakDC.ar(disk0)
//      // NOTE: DetectSilence won't output 1 if the threshold has never been crossed.
//      // Thus we add an initial spike
//      val silM    = DetectSilence.ar(disk0 + Impulse.ar(0), amp = thresh, dur = 0.3)
//      val silAll  = Reduce.&(silM)
//      val pRun    = pAudio("run", ParamSpec(0, 1, IntWarp), default = 0f) // (1.0))
//      val runTr   = Trig1.ar(pRun)  // XXX TODO --- do we need to trigger?
//      val runOut  = SetResetFF.ar(runTr, silAll)
//      LocalOut.kr(runOut)
//      val noClick = Line.ar(0, 1, 0.1)  // suppress DC since VDiskIn starts at speed zero
//      disk * noClick
//    }
//    val artUniv   = Artifact(locUniv, fUniv)
//    val specUniv  = AudioFile.readSpec(fUniv)
//    val cueUniv   = AudioCue.Obj[T](artUniv, specUniv, 0L, 1.0)
//    procUniv.attr.put("file", cueUniv)

    // --------------- ANEMONE ----------------

    (nConfig.micInputs ++ nConfig.lineInputs).find(c => c.name == "i-mkv" || c.name == "beat").foreach { cfg =>
      generator("a~beat") {
        shortcut = "B"
        val off     = cfg.indices // .offset
        val pThresh = pAudio("thresh", ParamSpec(0.01, 1, Warp.Exp), default(0.1))
        val in      = Trig1.ar(PhysicalIn.ar(off) - pThresh, 0.02)
        val pDiv    = pAudio("div", ParamSpec(1, 16, Warp.Int), default(1.0))
        val pulse   = PulseDivider.ar(in, pDiv)
        val pTime   = pAudio("time", ParamSpec(0.0 , 1.0), default(0.0))
        val sig     = DelayN.ar(pulse, 1.0, pTime)
        sig
      }
    }
  }
}