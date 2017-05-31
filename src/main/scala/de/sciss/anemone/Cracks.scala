/*
 *  Cracks.scala
 *  (Anemone Actiniaria)
 *
 *  Copyright (c) 2008-2017 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v2+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.anemone

import de.sciss.file._
import de.sciss.lucre.stm.Sys
import de.sciss.nuages
import de.sciss.nuages.{DbFaderWarp, ExponentialWarp, Nuages, ParamSpec, ScissProcs}
import de.sciss.synth.io.AudioFile
import de.sciss.synth.proc.AudioCue
import de.sciss.synth.ugen.ControlValues

object Cracks {
  def apply[S <: Sys[S]](dsl: nuages.DSL[S], sCfg: ScissProcs.Config, nCfg: Nuages.Config)
                        (implicit tx: S#Tx, n: Nuages[S]): Unit = {
    import dsl._

    def default(in: Double): ControlValues =
      if (sCfg.generatorChannels <= 0)
        in
      else
        Vector.fill(sCfg.generatorChannels)(in)

    val pCracks1 = generator("cra-1") {
      import de.sciss.synth.proc.graph._
      import de.sciss.synth.ugen._
      import de.sciss.synth.{Buffer => _, _}

      val width  = 5184
      val height = 2981
      val b      = Buffer("buf")

      // val cx: GE = "cx".kr(width  * 0.5) + SinOsc.ar("cxmf".kr(0)) * "cxmd".kr(0)
      // val cy: GE = "cy".kr(height * 0.5) + SinOsc.ar("cymf".kr(0)) * "cymd".kr(0)
      // val rx: GE = "rx".kr((width  - 1) * 0.5) + SinOsc.ar("rxmf".kr(0)) * "rxmd".kr(0)
      // val ry: GE = "ry".kr((height - 1) * 0.5) + SinOsc.ar("rymf".kr(0)) * "rymd".kr(0)
      // val freq: GE = "freq".kr(1.0) + SinOsc.ar("fmf".kr(0)) * "fmd".kr(0)

//      val cx: GE = "cx".ar(0.5) * width
//      val cy: GE = "cy".ar(0.5) * height
//      val rx: GE = "rx".ar(0.5) * width
//      val ry: GE = "ry".ar(0.5) * height

      val cx    = pAudio("cx", ParamSpec(0.0, 1.0), default(0.5))
      val cy    = pAudio("cy", ParamSpec(0.0, 1.0), default(0.5))
      val rx0   = pAudio("rx", ParamSpec(0.0, 1.0), default(0.5))
      val ry0   = pAudio("ry", ParamSpec(0.0, 1.0), default(0.5))
      val rx    = rx0 * width
      val ry    = ry0 * height

      val freq0 = pAudio("freq", ParamSpec(0.001, 10000.0, ExponentialWarp), default(0.5))
      val freq  = freq0 // .linexp(0, 1, 0.001, 10000)

      val twopi = math.Pi * 2
      val phase = Phasor.ar(speed = twopi * 2 * freq / SampleRate.ir, lo = 0, hi = twopi)

      val x = (cx + rx * phase.cos).fold(0, width )
      val y = (cy + ry * phase.sin).fold(0, height)

      val pos = y.roundTo(1) * width + x

      val read = BufRd.ar(numChannels = 1, buf = b, index = pos, loop = 1, interp = 0 /* 2 */)

      val sig0  = LeakDC.ar(read)
      val sig   = sig0 * 0.25 // Pan2.ar(sig0, "pan".kr(0))

      // Out.ar(0, sig)
      sig // ScanOut(sig)
    }

    val baseDir = file("/") / "data" / "projects" / "Imperfect" / "cracks" / "two_bw"
    val cueName1 = "cracks2_10bw.aif"
    val art1    = baseDir / cueName1
    val spec1   = AudioFile.readSpec(art1)
    val cue1    = AudioCue.Obj.newConst[S](AudioCue(art1, spec1, offset = 0L, gain = 1.0))

    pCracks1.attr.put("buf", cue1)

    nCfg.masterChannels.foreach { chans =>
      chans.zipWithIndex.foreach { case (idx, ch) =>
        collector(s"O-cr${idx+1}") { in =>
          import de.sciss.synth._
          import ugen._

          def mkAmp(): GE = {
            val db0 = pAudio("amp", ParamSpec(-inf, 20, DbFaderWarp), default(-inf))
            val db  = db0 - 10 * (db0 < -764)  // BUG IN SUPERCOLLIDER
            val res = db.dbamp
            CheckBadValues.ar(res, id = 666)
            res
          }

          val pAmp   = mkAmp()
          val sigA   = in * Lag.ar(pAmp, 0.1) // .outputs
          val outSig = Mix(sigA) / NumChannels(sigA)
          val bad    = CheckBadValues.ar(outSig)
          val sig    = Gate.ar(outSig, bad sig_== 0)
          Out.ar(ch, sig)
        }
      }
    }
  }
}
