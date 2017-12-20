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

import de.sciss.lucre.artifact.{Artifact, ArtifactLocation}
import de.sciss.file._
import de.sciss.lucre.stm
import de.sciss.nuages.{ExpWarp, IntWarp, Nuages, ParamSpec, ScissProcs}
import de.sciss.synth.io.AudioFile
import de.sciss.synth.proc.{AudioCue, Folder}
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
    Promenade         (dsl, sConfig, nConfig)
//    Imperfect         (dsl, sConfig, nConfig)
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

    // -------------- SEAM --------------

    val dirUniv = file("/data/projects/SeaM20/audio_work/")
    val fUniv   = dirUniv / "univ-arr.aif"
    val locUniv = ArtifactLocation.newConst[S](dirUniv)
    val procUniv = generator("univ") {
      val thresh  = -80.dbamp
      val runIn   = LocalIn.kr(0)
      val speed   = runIn // * (44100/SampleRate.ir)
      val disk0   = proc.graph.VDiskIn.ar("file", speed = speed, loop = 0)
      val disk    = LeakDC.ar(disk0)
      // NOTE: DetectSilence won't output 1 if the threshold has never been crossed.
      // Thus we add an initial spike
      val silM    = DetectSilence.ar(disk0 + Impulse.ar(0), amp = thresh, dur = 0.3)
      val silAll  = Reduce.&(silM)
      val pRun    = pAudio("run", ParamSpec(0, 1, IntWarp), default = 0f) // (1.0))
      val runTr   = Trig1.ar(pRun)  // XXX TODO --- do we need to trigger?
      val runOut  = SetResetFF.ar(runTr, silAll)
      LocalOut.kr(runOut)
      val noClick = Line.ar(0, 1, 0.1)  // suppress DC since VDiskIn starts at speed zero
      disk * noClick
    }
    val artUniv   = Artifact(locUniv, fUniv)
    val specUniv  = AudioFile.readSpec(fUniv)
    val cueUniv   = AudioCue.Obj[S](artUniv, specUniv, 0L, 1.0)
    procUniv.attr.put("file", cueUniv)

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