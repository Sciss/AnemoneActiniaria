/*
 *  Almat.scala
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

package de.sciss.anemone

import de.sciss.fscape.lucre.MacroImplicits.FScapeMacroOps
import de.sciss.lucre.synth.Txn
import de.sciss.lucre.{ArtifactLocation => _ArtifactLocation, Obj => LObj}
import de.sciss.nuages.{DSL, Nuages, ScissProcs, Util}
import de.sciss.proc.MacroImplicits.ControlMacroOps
import de.sciss.proc.{FScape, ParamSpec, Proc, Warp}
import de.sciss.{proc, synth}
import de.sciss.synth.ugen._

object Almat {
  def any2stringadd: Any = ()

  def mkActions[T <: Txn[T]]()(implicit tx: T): Map[String, LObj[T]] = {
    val recPrepare    = ScissProcs.actionRecPrepare[T]
    val recDoneRender = ctlRecDoneRender[T]
    Map("rec-prepare" -> recPrepare, "rec-done-render" -> recDoneRender)
  }

  def apply[T <: Txn[T]](dsl: DSL[T], sConfig: ScissProcs.Config, nConfig: Nuages.Config)
                        (implicit tx: T, nuages: Nuages[T]): Unit = {
    import de.sciss.synth.Import._
    import dsl._
    import sConfig.genNumChannels
    import synth.GE

    def filterF   (name: String)(fun: GE => GE): Proc[T] =
      filter      (name, if (DSL.useScanFixed) genNumChannels else -1)(fun)

    def mkMix(df: Double = 0.0): GE = pAudio("mix", ParamSpec(0, 1), default(df))

    def default(in: Double): ControlValues =
      if (genNumChannels <= 0)
        in
      else
        Vector.fill(genNumChannels)(in)

    def mix(in: GE, flt: GE, mix: GE): GE = {
      LinXFade2.ar(in, flt, mix * 2 - 1)
    }

    filterF("hopf") { in =>
      val pCoupling = pAudio("coup" , ParamSpec(0.0005, 0.05, Warp.Exponential), default(0.0005))
      val pRadius   = pAudio("rad"  , ParamSpec(0.0, 1.0), default(1.0))
      val pSelect   = pAudio("sel"  , ParamSpec(0.0, 3.0, Warp.Int), default(0.0))
      val pMix      = mkMix()
      val hopf      = Hopf.ar(in, coupling = pCoupling, radius = pRadius)
      val phase     = hopf.phase / math.Pi
      val omega     = hopf.omega / math.Pi
      val flt       =
        hopf.x * (pSelect sig_== 0) +
        hopf.y * (pSelect sig_== 1) +
        phase  * (pSelect sig_== 2) +
        omega  * (pSelect sig_== 3)
      mix(in, flt, pMix)
    }

    /////////////////////////////////////////

    val mapActions = mkActions[T]()
    applyWithActions[T](dsl, sConfig, nConfig, mapActions)
  }

  def applyWithActions[T <: Txn[T]](dsl: DSL[T], sConfig: ScissProcs.Config, nConfig: Nuages.Config,
                                    actions: Map[String, LObj[T]])
                                   (implicit tx: T, nuages: Nuages[T]): Unit = {
    import dsl._
    import sConfig.genNumChannels
    import synth.GE

    def sinkF     (name: String)(fun: GE => Unit): Proc[T] =
      sink        (name, if (DSL.useScanFixed) genNumChannels else -1)(fun)

    val sinkRecFourier = sinkF("rec-fourier") { in =>
      import de.sciss.synth.Import._
      val disk = synth.proc.graph.DiskOut.ar(Util.attrRecArtifact, in)
      disk.poll(0, "disk")
    }

    val sinkPrepObj       = actions("rec-prepare")
    val sinkDoneRenderObj = actions("rec-done-render")
    val recDirObj         = _ArtifactLocation.newConst[T](sConfig.recDir.toURI)

    // this is done by ScissProcs already:
    // nuages.attr.put("generators", nuages.generators.getOrElse(throw new IllegalStateException()))

    require (genNumChannels > 0)
    val pPlaySinkRec = Util.mkLoop(nuages, "play-sink", numBufChannels = genNumChannels, genNumChannels = genNumChannels)
    sinkDoneRenderObj.attr.put("play-template", pPlaySinkRec)

    val fscFourier = mkFScapeFourier[T]()
    sinkDoneRenderObj.attr.put("fscape", fscFourier)

    val sinkRecA = sinkRecFourier.attr
    sinkRecA.put(Nuages.attrPrepare, sinkPrepObj)
    sinkRecA.put(Nuages.attrDispose, sinkDoneRenderObj)
    sinkRecA.put(Util  .attrRecDir , recDirObj  )
  }

  def mkFScapeFourier[T <: Txn[T]]()(implicit tx: T): FScape[T] = {
    import de.sciss.fscape.GE
    import de.sciss.fscape.Ops._
    import de.sciss.fscape.graph.{AudioFileIn => _, AudioFileOut => _, _}
//    import de.sciss.fscape.lucre.graph.Ops._
    import de.sciss.fscape.lucre.graph._
    val f = FScape[T]()
    f.setGraph {
      val in0           = AudioFileIn("in")
      val sr            = in0.sampleRate
      val numFramesIn   = in0.numFrames
      val fileType      = 0 // "out-type"    .attr(0)
      val smpFmt        = 2 // "out-format"  .attr(2)
//      val gainDb        = -0.2 // "gain-db"     .attr(0.0)
//      val lenMode       = "len-mode"    .attr(0).clip()
//      val dir           = 0 // "direction"   .attr(0).clip()
      val dirFFT        = 1 // dir * -2 + (1: GE)  // bwd = -1, fwd = +1
//      val numFramesOut  = (numFramesIn + lenMode).nextPowerOfTwo / (lenMode + (1: GE))
      val numFramesOut  = numFramesIn.nextPowerOfTwo
      val numFramesInT  = numFramesIn min numFramesOut
//      val gainAmt       = gainDb.dbAmp

      val inT           = in0.take(numFramesInT)
      val inImag  = DC(0.0)
      val inImagT = inImag.take(numFramesInT)
      val inC = inT zip inImagT
      val fft = Fourier(inC, size = numFramesInT,
        padding = numFramesOut - numFramesInT, dir = dirFFT)

      def mkProgress(x: GE, label: String) =
        ProgressFrames(x, numFramesOut, label)

      def normalize(x: GE): GE = {
        val rsmpBuf   = BufferDisk(x)
        val rMax      = RunningMax(Reduce.max(x.abs))
        mkProgress(rMax, "analyze")
        val maxAmp    = rMax.last
        val div       = maxAmp + (maxAmp sig_== 0.0)
        val gainAmtN  = 1.0 /*gainAmt*/ / div
        rsmpBuf * gainAmtN
      }

      val re      = fft.complex.real
      val outN    = normalize(re) * 60
      val limLen  = 44100 * 1
      val lim     = Limiter(outN, attack = limLen, release = limLen)
      // XXX TODO --- why delay > limLen*2 ? (perhaps plus one or two control bufs?)
//      val sigOut  = BufferMemory(outN, limLen * 2 + 8192) * lim
      val sigOut  = BufferDisk(outN) * lim
      val written = AudioFileOut("out", sigOut, fileType = fileType,
        sampleFormat = smpFmt, sampleRate = sr)
      mkProgress(written, "write")
    }
    f

  }

  def ctlRecDoneRender[T <: Txn[T]](implicit tx: T): proc.Control[T] = {
    val c = proc.Control[T]()
//    import de.sciss.lucre.expr.ExImport._
    import de.sciss.lucre.expr.graph._
    import de.sciss.proc.ExImport._
    c.setGraph {
      val artLoc  = ArtifactLocation("value:$rec-dir")
      val artRec  = Artifact("value:$file")
      val procOpt = "play-template" .attr[Obj]
      val invOpt  = "invoker"       .attr[Obj]
      val render  = Runner("fscape")

      val ts      = TimeStamp()
      val name    = ts.format("'fsc_'yyMMdd'_'HHmmss'.aif'")
      val artRender = artLoc / name

      val isDone  = render.state sig_== 4
      val isFail  = render.state sig_== 5

      val actRenderOpt = for {
//        spec    <- specOpt
        procTmp <- procOpt
        invoker <- invOpt
        gen     <- invoker.attr[Folder]("generators")
      } yield {
        val specOpt = AudioFileSpec.Read(artRender)
        val spec  = specOpt.getOrElse(AudioFileSpec.Empty())
        val cue   = AudioCue(artRender, spec)
        val proc  = procTmp.copy
        Act(
          PrintLn("FScape rendering done!"),
          proc.make,
          proc.attr[AudioCue]("file").set(cue),
          proc.attr[String]("name").set(artRender.base),
          gen.append(proc),
        )
      }

      isDone.toTrig --> actRenderOpt.orElse {
        PrintLn("Could not prepare play-proc! proc? " ++ procOpt.isDefined.toStr ++
          ", invoker? " ++ invOpt.isDefined.toStr)
      }

      isFail.toTrig --> PrintLn("FScape rendering failed!")

      val actOpt  = Act(
        PrintLn("File to render: " ++ artRender.path),
//        artRec.set(artRender),
        render.runWith(
          "in"  -> artRec,
          "out" -> artRender,
        ),
      )

      val actDone = actOpt
//        .getOrElse {
//        PrintLn("Could not create player! spec? " ++
//          specOpt.isDefined.toStr ++
//          ", proc? "   ++ procOpt.isDefined.toStr ++
//          ", invoker? " ++ invOpt.isDefined.toStr)
//      }

      LoadBang() --> Act(
        PrintLn("File written: " ++ artRec.toStr),
        actDone
      )
    }
    c
  }
}
