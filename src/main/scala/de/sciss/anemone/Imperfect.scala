/*
 *  Imperfect.scala
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

import de.sciss.lucre.synth.Txn
import de.sciss.nuages._
import de.sciss.proc.{ParamSpec, Proc, Warp}
import de.sciss.synth.GE
import de.sciss.synth.ugen.ControlValues
import de.sciss.{nuages, synth}

object Imperfect {
  def apply[T <: Txn[T]](dsl: nuages.DSL[T], sCfg: ScissProcs.Config, nCfg: Nuages.Config)
                        (implicit tx: T, n: Nuages[T]): Unit = {
    import dsl._

    val mainChansOption = nCfg.mainChannels

    val numOut = if (sCfg.genNumChannels <= 0) mainChansOption.fold(2)(_.size) else sCfg.genNumChannels

//    def mkDetune(in: GE, max: Double = 2.0): GE = {
//      require(max > 1)
//      import synth._
//      val det = pAudio("detune" , ParamSpec(1.0, 2.0), default(1.0))
//      in * det
//    }

    def default(in: Double): ControlValues =
      if (sCfg.genNumChannels <= 0)
        in
      else
        Vector.fill(sCfg.genNumChannels)(in)

//    generator("muta-quietsch") {
//      import synth._
//      import ugen._
//      val v11   = pAudio("p1"     , ParamSpec(0.0001, 1.0, ExpWarp), default(0.014))
//      val v14a  = 6286.0566 // pAudio("p2"     , ParamSpec(10, 10000, ExpWarp), default = 6286.0566)
//      val v24   = pAudio("p3"    , ParamSpec(-0.0005, -5.0, ExpWarp), default(-1.699198))
//      val amp   = pAudio("amp"    , ParamSpec(0.01,     1, ExpWarp), default(0.1))
//      val det   = pAudio("detune" , ParamSpec(1, 2), default(1))
//
//      val v14 = v14a * det
//
//      val v25   = Ringz.ar(v11, v24, v24)
//      val v26   = 1.0
//      val v27   = v26 trunc v25
//      val v28   = v27 | v14
//      val sig   = v28
//      Limiter.ar(LeakDC.ar(sig), dur = 0.01).clip2(1) * amp
//    }

    mainChansOption.foreach { mainChans =>
      val numChans          = mainChans.size
      val mainCfg         = NamedBusConfig("", 0 until numChans)
      val mainGroupsCfg   = mainCfg +: sCfg.mainGroups

      mainGroupsCfg.zipWithIndex.foreach { case (cfg, _) =>

        def mkDirectOut(sig0: GE): Unit = {
          import synth._
          import ugen._
          import Import._
          val bad = CheckBadValues.ar(sig0)
          val sig = Gate.ar(sig0, bad sig_== 0)
          mainChans.zipWithIndex.foreach { case (ch, i) =>
            val sig0 = sig.out(i)
            val hpf  = sCfg.highPass
            val sig1 = if (hpf >= 16 && hpf < 20000) HPF.ar(sig0, hpf) else sig0
            Out.ar(ch, sig1)   // XXX TODO - should go to a bus w/ limiter
          }
        }

        def collectorF(name: String)(fun: GE => Unit): Proc[T] =
          collector(name, sCfg.genNumChannels)(fun)

//        def placeChannels(sig: GE): GE = {
//          import synth._
//          import ugen._
//          if (cfg.numChannels == numChans) sig
//          else {
//            Seq(Silent.ar(cfg.offset),
//              Flatten(sig),
//              Silent.ar(numChans - (cfg.offset + cfg.numChannels))): GE
//          }
//        }

        def mkAmp(): GE = {
          import synth._
          import ugen._
          import Import._
          val db0 = pAudio("amp", ParamSpec(-inf, 20, Warp.DbFader), default(-inf))
          val db  = db0 - 10 * (db0 < -764)  // BUG IN SUPERCOLLIDER
          val res = db.dbAmp
          CheckBadValues.ar(res, id = 666)
          res
        }

        def mkOutTri(in: GE): GE = {
          import synth._
          import ugen._
          import Import._
          val px            = pControl("x" , ParamSpec(0.0, 1.0),   default(0.5))
          val py            = pControl("y" , ParamSpec(0.0, 1.0),   default(0.5))
//          val pSpread       = pControl("spr" , ParamSpec(0.0, 1.0),   default(0.25)) // XXX rand
//          val pRota         = pControl("rota", ParamSpec(0.0, 1.0),   default(0.0))
//          val pBase         = pControl("azi" , ParamSpec(0.0, 360.0), default(0.0))
          val pLag          = pControl("lag" , ParamSpec(0.05, 5), default(0.2))
          val pAmp          = mkAmp()
          val inSig         = in * Lag.ar(pAmp, 0.1) // .outputs

          val outChannels   = cfg.numChannels
          // I have _no_ idea why the coordinates suddenly go only until 0.5
          val x = px // * 0.5 // Mix(px)
          val y = py // * 0.5 // Mix(py)

          var sum: GE = 0
          for (ch <- 0 until numOut) {
            val inCh  = inSig.out(ch)
            val lagCh = pLag .out(ch)
            val xCh   = x    .out(ch)
            val yCh   = y    .out(ch)
            val del   = NegatumDelaunay(xCh, yCh)
            val ampL  = Lag.kr(del, time = lagCh)
            val sig   = inCh * ampL
            sum += sig // Mix(sig)
          }
          val outSig = sum
          // NumChannels(outSig).poll(0, "num-out")
          // placeChannels(outSig)
          val karlheinz: GE = Vector.tabulate(outChannels)(ch => outSig.out(ch))
          karlheinz
        }

        collectorF(s"O-tri${cfg.name}") { in =>
          val sig = mkOutTri(in)
          mkDirectOut(sig)
        }
      }
    }
  }
}
