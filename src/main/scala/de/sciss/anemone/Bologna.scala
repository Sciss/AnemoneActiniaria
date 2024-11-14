/*
 *  Mutagens.scala
 *  (Anemone-Actiniaria)
 *
 *  Copyright (c) 2014-2024 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.anemone

import de.sciss.lucre.synth.Txn
import de.sciss.nuages.{DSL, NamedBusConfig, Nuages, ScissProcs, Util}
import de.sciss.proc.{ParamSpec, Warp}
import de.sciss.synth.GE
import de.sciss.proc
import de.sciss.synth.Import.inf
import de.sciss.synth.ugen.{CheckBadValues, ControlValues, Lag}
import de.sciss.{nuages, synth}

object Bologna {
  def apply[T <: Txn[T]](dsl: nuages.DSL[T], sCfg: ScissProcs.Config, nCfg: Nuages.Config)
                        (implicit tx: T, n: Nuages[T]): Unit = {
    import synth.ugen._
    import synth.{proc => _, _}
    import Import._

    import dsl._
    import sCfg.genNumChannels

    val masterChansOption = nCfg.mainChannels

    val numGen = if (sCfg.genNumChannels <= 0) masterChansOption.fold(2)(_.size) else sCfg.genNumChannels

    def default(in: Double): ControlValues =
      if (sCfg.genNumChannels <= 0)
        in
      else
        Vector.fill(sCfg.genNumChannels)(in)

//    def placeChannels(sig: GE): GE = sig

    def mkAmp(): GE = {
      val db0 = pAudio("amp", ParamSpec(-inf, 20, Warp.DbFader), default(-inf))
      val db  = db0 - 10 * (db0 < -764)  // BUG IN SUPERCOLLIDER
      val res = db.dbAmp
      CheckBadValues.ar(res, id = 666)
      res
    }

    val mainChannels = nCfg.mainChannels.get // OrElse(0 until numOut)
    val numMain = mainChannels.size

    def mkDirectOut(sig0: GE): Unit = {
      val bad = CheckBadValues.ar(sig0)
      val sig = Gate.ar(sig0, bad sig_== 0)
      mainChannels.zipWithIndex.foreach { case (ch, i) =>
        val sig0 = sig out i
        val hpf  = sCfg.highPass
        val sig1 = if (hpf >= 16 && hpf < 20000) HPF.ar(sig0, hpf) else sig0
        Out.ar(ch, sig1)   // XXX TODO - should go to a bus w/ limiter
      }
    }

    def mkFade(x: GE): GE = {
      val fade    = pAudio("fade", ParamSpec(-1.0, 1.0), default(-1.0f))
      val (sigA, sigB) = Seq.tabulate(numGen) { i =>
        val p = Pan2.ar(x.out(i), pos = fade.out(i))
        (p.left, p.right)
      } .unzip
      val numA = numMain >> 1
      val numB = numMain - numA
      val sigAW = Util.wrapExtendChannels(numA, sigA)
      val sigBW = Util.wrapExtendChannels(numB, sigB)
      Flatten(Seq(sigAW, sigBW))
    }

    def mkOutAll(in: GE): GE = {
      val pAmp          = mkAmp()
      val sig           = in * Lag.ar(pAmp, 0.1) // .outputs
      sig
    }

    def mkOutPan(in: GE): GE = {
//      val pSpread       = pControl("spr" , ParamSpec(0.0, 1.0),   default(0.25)) // XXX rand
      val pRota         = pControl("rota", ParamSpec(0.0, 1.0),   default(0.0))
      val pBase         = pControl("azi" , ParamSpec(0.0, 360.0), default(0.0))
      val pAmp          = mkAmp()

      val baseAzi       = Lag.kr(pBase, 0.5) // + IRand(0, 360)
      val rotaAmt       = Lag.kr(pRota, 0.1)
//      val spread        = Lag.kr(pSpread, 0.5)
      val outChannels   = numMain >> 1 // cfg.numChannels
      val rotaSpeed     = 0.1
      val inSig0        = in * Lag.ar(pAmp, 0.1) // .outputs
      val inSig         = Mix(inSig0)
      val noise         = LFDNoise1.kr(rotaSpeed) * rotaAmt * 2
      val indicesIn     = ChannelIndices(in)
      val numChanIn     = NumChannels(in)
      //          indicesIn.poll(0, "indices")
      //          numChanIn.poll(0, "num-chans")
      //          val pos0          = indicesIn * 2 / numChanIn
//      val pos0          = indicesIn * 4 / numChanIn * 0.25 // pSpread
      // pos0.poll(0, "pos0")
      val pos1          = (baseAzi / 180) // + pos0
      val pos           = pos1 + noise
//      val level         = 1
//      val width         = (spread * (outChannels - 2)) + 2
//      val panAz         = PanAz.ar(outChannels, in = inSig, pos = pos, level = level, width = width, orient = 0)
//      // tricky
//      val outSig        = Mix(panAz)

      val fade = (pos - 1).fold(-1, 1)
//      fade.poll(1, "fade")
      val (sigA, sigB) = Seq.tabulate(numGen) { i =>
        val p = Pan2.ar(inSig.out(i), pos = fade.out(i))
        (p.left, p.right)
      } .unzip

      val numA = outChannels >> 1
      val numB = outChannels - numA
      val sigAW = Util.wrapExtendChannels(numA, sigA)
      val sigBW = Util.wrapExtendChannels(numB, sigB)
      val outSig = Flatten(Seq(sigAW, sigBW))

      outSig // placeChannels(outSig)
    }

    def mkOutRnd(in: GE): GE = {
      val pAmp        = mkAmp()
      val pFreq       = pControl("freq", ParamSpec(0.01, 10, Warp.Exp), default(0.05))
      val pPow        = pControl("pow" , ParamSpec(1, 10), default(2.0))
      val pLag        = pControl("lag" , ParamSpec(0.1, 10), default(4.0))

      val sig         = in * Lag.ar(pAmp, 0.1) // .outputs
      //          NumChannels(sig).poll(0, "HELLO-IN")
      val outChannels = numMain >> 1 // cfg.numChannels
      val sig1        = Util.wrapExtendChannels(outChannels, sig)
      //          NumChannels(sig).poll(0, "HELLO-OUT")
      val freq        = pFreq
      val lag         = pLag
      val pw          = pPow
      //          val rands       = Lag.ar(TRand.ar(0, 1, Dust.ar(List.fill(outChannels)(freq))).pow(pw), lag)
      val rands       = Lag.ar(TRand.ar(0, 1, Dust.ar(Util.wrapExtendChannels(outChannels, freq))).pow(pw), lag)
      //          NumChannels(rands).poll(0, "HELLO-rands")
      val outSig      = sig1 * rands
      //          NumChannels(outSig).poll(0, "HELLO-outSig")
      outSig // placeChannels(outSig)
    }

    def collectorF(name: String)(fun: GE => Unit): proc.Proc[T] =
      collector   (name, if (DSL.useScanFixed) genNumChannels else -1)(fun)

    collectorF("Ox-all") { in =>
      val sig   = mkOutAll(in)
      val sigF  = mkFade(sig)
      mkDirectOut(sigF)
    }
    collectorF("Ox-pan") { in =>
      val sig   = mkOutPan(in)
      val sigF  = mkFade(sig)
      mkDirectOut(sigF)
    }
    collectorF("Ox-rnd") { in =>
      val sig   = mkOutRnd(in)
      val sigF  = mkFade(sig)
      mkDirectOut(sigF)
    }
  }
}