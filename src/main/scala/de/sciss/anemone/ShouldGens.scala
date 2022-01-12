/*
 *  Mutagens.scala
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
import de.sciss.nuages.{Nuages, ScissProcs}
import de.sciss.proc.{ParamSpec, Warp}
import de.sciss.synth.GE
import de.sciss.synth.ugen.ControlValues
import de.sciss.{nuages, synth}

object ShouldGens {
  def apply[T <: Txn[T]](dsl: nuages.DSL[T], sCfg: ScissProcs.Config, nCfg: Nuages.Config)
                        (implicit tx: T, n: Nuages[T]): Unit = {
    import dsl._

    val masterChansOption = nCfg.mainChannels

    val numOut = if (sCfg.genNumChannels <= 0) masterChansOption.fold(2)(_.size) else sCfg.genNumChannels

    def mkDetune(in: GE, det: GE): GE = {
      import synth._
      import Import._
      val detL = Vector.tabulate(numOut) { ch =>
        val m = (ch: GE).linLin(0, (numOut - 1).max(1), 1, det out 0)
        m
      }
      in * detL
    }

    def mkExpDetune(in: GE, det: GE): GE = {
      import synth._
      import ugen._
      import Import._
      val detL = Vector.tabulate(numOut) { ch =>
        val m = DC.ar(ch: GE).linExp(DC.ar(0), DC.ar((numOut - 1).max(1)), DC.ar(1), det out 0)
        m
      }
      in * detL
    }

    def default(in: Double): ControlValues =
      if (sCfg.genNumChannels <= 0)
        in
      else
        Vector.fill(sCfg.genNumChannels)(in)

    generator("shd-eb5b") {
      import synth._
      import ugen._
      import Import._
      val pFreq0  = pAudio("p1"     , ParamSpec(0.1 , 10000.0, Warp.Exp), default(8.824352))
      val pFreq1  = pAudio("p2"     , ParamSpec(10.0, 18000.0, Warp.Exp), default(7860.8604))
      val amp     = pAudio("amp"    , ParamSpec(0.01,     1, Warp.Exp), default(0.1))
      val pDet1   = pAudio("det1"   , ParamSpec(1.0, 2.0), default(1))
      val pDet2   = pAudio("det2"   , ParamSpec(1.0, 2.0), default(1))
      RandSeed.ir

      val freq0: GE = mkExpDetune(pFreq0, pDet1)
      val freq2: GE = mkExpDetune(pFreq1, pDet2)
//      freq0.poll(0, "freq2")

      val xi        = LFSaw.ar(freq = freq0, iphase = 0.0)
      val freq_0    = xi clip2 0.011223611
      val lFDNoise3 = LFDNoise3.ar(freq_0)
      val cuspN     = CuspN.ar(freq = freq2, a = 1.0, b = 0.0, xi = xi)
      val mix       = Mix(Seq[GE](lFDNoise3, cuspN))
      val sig       = LeakDC.ar(mix.clip2(1.0)) * 0.5 * amp
      sig
    }

    generator("shd-39c4") {
      import synth._
      import ugen._
      import Import._

      val pFreq0  = default(8.722097E-4).seq: GE // pAudio("p1"     , ParamSpec(1.0e-5 , 1.0, Warp.Exp), default(8.722097E-4))
      val pFreq1  = pAudio("p1"     , ParamSpec(1.0 , 10000.0, Warp.Exp), default(10.0))
      val pFreq2  = pAudio("p2"     , ParamSpec(5.0 , 18000.0, Warp.Exp), default(29.300417))
      val pFreq3  = pAudio("p3"     , ParamSpec(5.0 , 18000.0, Warp.Exp), default(1209.1062))
      val pWidth0 = pAudio("width"  , ParamSpec(0.0001, 0.9999, Warp.Lin), default(0.031116353))
      val pDet1   = pAudio("det"    , ParamSpec(1.0, 2.0), default(1))
      val amp     = pAudio("amp"    , ParamSpec(0.01,     1, Warp.Exp), default(0.1))

      //      RandSeed.ir

      val freq0   = pFreq0  // mkExpDetune(pFreq0  , pDet1) // seq0.map(_ * 8.722097E-4)
      val freq1   = mkExpDetune(pFreq1  , pDet1) // seq0.map(_ * "freq1".kr(10.0))
      val width0  = pWidth0 // mkDetune   (pWidth0 , pDet1) // seq0.map(_ * "width0".kr(0.031116353))
      val freq2   = mkExpDetune(pFreq2  , pDet1) // seq0.map(_ * "freq2".kr(29.300417))
      val freq3   = mkExpDetune(pFreq3  , pDet1) // seq0.map(_ * "freq3".kr(1209.1062))

      val lFDNoise3_0   = LFDNoise3.ar(freq0)
      val lFDNoise3_1   = LFDNoise3.ar(501.0)
      val atan2         = lFDNoise3_0 atan2 lFDNoise3_1
      val max           = atan2 max 1.0
      val blip_0        = Blip.ar(freq = freq1, numHarm = 1.0)
      val numHarm_0     = max max 1.0
      val blip_1        = Blip.ar(freq = freq1, numHarm = numHarm_0)
      val in_0          = blip_1 max 5.0E-5
      val dur           = Clip.ar(in_0, lo = 5.0E-5, hi = 100.0)
      val lFGauss       = LFGauss.ar(dur = dur, width = width0, phase = 1.0, loop = 1209.1062,
        doneAction = doNothing)
      val in_1          = LFSaw.ar(freq = 0.01, iphase = 0.0)
      val leakDC_0      = LeakDC.ar(in_1, coeff = 0.995)
      val quadC         = QuadC.ar(freq = 0.0, a = 0.0, b = 0.0, c = 0.0, xi = 0.2067178)
      val in_2          = blip_1 max 0.01
      val sawFreq       = Clip.ar(in_2, lo = 0.01, hi = 20000.0)
      val d             = SyncSaw.ar(syncFreq = 0.01, sawFreq = sawFreq)
      val plus_0        = 4088.3284 + blip_0
      val trunc         = d trunc 4088.3284
      val lFPar         = LFPar.ar(freq = 0.01, iphase = 1.0)
      val latoocarfianC = LatoocarfianC.ar(freq = freq2, a = -3.0, b = 0.5, c = 0.5, d = -0.14014469,
        xi = 0.0, yi = 0.0104253385)
      val standardL     = StandardL.ar(freq = freq2, k = latoocarfianC, xi = latoocarfianC, yi = 0.0)
      val in_3          = LFPar.ar(freq = freq2, iphase = 0.0)
      val freq_0        = BRF.ar(in_3, freq = 10.0, rq = 0.01)
      val in_4          = freq_0 min -0.14014469
      val in_5          = LeakDC.ar(in_4, coeff = 0.995)
      val lPZ2          = LPZ2.ar(in_5)
      val min_0         = lPZ2 min 0.0
      val xi_0          = min_0 absDif trunc
      val min_1         = freq_0 min lFPar
      val latoocarfianL = LatoocarfianL.ar(freq = freq3, a = 3.0, b = 0.5, c = 1.5, d = d,
        xi = -0.14014469, yi = 0.2067178)
      val sumsqr        = min_1 sumSqr latoocarfianL
      val yi_0          = min_1 * plus_0
      val in_6          = GbmanL.ar(freq = freq_0, xi = xi_0, yi = yi_0)
      val leakDC_1      = LeakDC.ar(in_6, coeff = 0.995)
      val plus_1        = leakDC_1 + leakDC_0
      val roundTo       = 0.0092437165 roundTo latoocarfianL
      val mix           = Mix(Seq[GE](lFGauss, quadC, standardL, sumsqr, plus_1, roundTo))
      //NegatumOut(mix)
      //val sig = Limiter.ar(LeakDC.ar(mix.clip2(1.0)) * 0.5)
      //val amp = "gain".kr(1.0) * FadeInOut.ar * (-"mute".kr(0) + 1.0)
      val sig = LeakDC.ar(mix.clip2(1.0)) * 0.65 * amp
      sig
    }

    // XXX TODO: make this triggerable
//    generator("shd-dd71") {
//      import synth._
//      import ugen._
//
//      val pDet1   = pAudio("det"    , ParamSpec(1.0, 2.0), default(1))
//      val amp     = pAudio("amp"    , ParamSpec(0.01,     1, Warp.Exp), default(0.1))
//
//      val seq0 = Seq.tabulate(4)(i => 1.005.pow(i))
//
//      val freq0 = seq0.map(_ * 8703.354)
//
////      val seed = 0
////      RandSeed.ir(seed = 0)
//
//      val lFDNoise0     = LFDNoise0.ar(freq0)
//      val in_0          = lFDNoise0 max 0.0
//      val linCongL_0    = LinCongL.ar(freq = 1.3880533, a = 0.0, c = 0.3006779, m = 472.71542, xi = 0.0)
//      val ring2         = linCongL_0 ring2 4149.2856
//      val in_1          = ring2 absDif -13.328312
//      val freq_0        = in_1 min 4149.2856
//      val lFCub         = LFCub.ar(freq = 0.01, iphase = 0.0)
//      val in_2          = linCongL_0 ^ -13.328312
//      val min_0         = in_2 min 0.0
//      val in_3          = LFDNoise1.ar(-136.7135)
//      val in_4          = LeakDC.ar(in_3, coeff = 0.995)
//      val bRF           = BRF.ar(in_4, freq = 10.0, rq = 1.0)
//      val linCongL_1    = LinCongL.ar(freq = 472.71542, a = 6.7240785E-6, c = 0.13, m = -136.7135,
//        xi = 6.7240785E-6)
//      val yi            = min_0 sqrDif -0.0026739505
//      val in_5          = LeakDC.ar(in_1, coeff = 0.995)
//      val timeUp        = Clip.ar(in_0, lo = 0.0, hi = 30.0)
//      val lag3UD        = Lag3UD.ar(in_5, timeUp = timeUp, timeDown = 0.1)
//      val d             = Saw.ar(10.0)
//      val latoocarfianL = LatoocarfianL.ar(freq = freq_0, a = 3.0, b = 1.5, c = 0.5, d = d, xi = 30.21968,
//        yi = yi)
//      val times         = lag3UD * freq_0
//      val min_1         = times min latoocarfianL
//      val min_2         = min_1 min 0.0
//      val in_6          = min_2 * in_2
//      val rLPF          = RLPF.ar(linCongL_1, freq = 2281.065, rq = 1.0)
//      val linCongL_2    = LinCongL.ar(freq = freq_0, a = 6.7240785E-6, c = 1.5707961, m = linCongL_1,
//        xi = 198.49162)
//      val min_3         = ring2 min lFCub
//      val in_7          = Clip.ar(in_6, lo = 0.01, hi = 20000.0)
//      val syncFreq      = Clip.ar(in_7, lo = 0.01, hi = 20000.0)
//      val syncSaw       = SyncSaw.ar(syncFreq = syncFreq, sawFreq = 4149.2856)
//      val mod           = times % linCongL_2
//      val in_8          = Clip.ar(in_2, lo = 0.01, hi = 20000.0)
//      val freq_1        = Clip.ar(in_8, lo = 0.01, hi = 20000.0)
//      val sinOsc        = SinOsc.ar(freq = freq_1, phase = -6.144882)
//      val dC            = DC.ar(6.7240785E-6)
//      val mix           = Mix(Seq[GE](bRF, rLPF, min_3, syncSaw, mod, sinOsc, dC))
//
//      val sig = LeakDC.ar(mix.clip2(1.0)) * 0.79 * amp
//      sig
//    }

    // XXX TODO --- need to be able to retrigger
//    generator("shd-d979") {
//      import synth._
//      import ugen._
//
//      val pFreq0  = pAudio("p1"     , ParamSpec(0.1 , 1000.0, Warp.Exp), default(1.5504636))
//      val pFreq1  = pAudio("p2"     , ParamSpec(0.1 , 1000.0, Warp.Exp), default(1.0))
//      val pFreq2  = pAudio("p2"     , ParamSpec(5.0 , 18000.0, Warp.Exp), default(3009.2053))
//      val pDet1   = pAudio("det1"   , ParamSpec(1.0, 2.0), default(1))
//      val pDet2   = pAudio("det2"   , ParamSpec(1.0, 2.0), default(1))
//      val amp     = pAudio("amp"    , ParamSpec(0.01,     1, Warp.Exp), default(0.1))
//
//      val freq0: GE = mkExpDetune(pFreq0, pDet1)
//      val freq1: GE = mkExpDetune(pFreq1, pDet2) // seq1.map(_ * "freq1".kr(1.0))
//      val freq2: GE = mkExpDetune(pFreq2, pDet2) // = seq1.map(_ * "freq2".kr(3009.2053))
//
////      RandSeed.ir
//
//      val in_0      = GbmanN.ar(freq = freq0, xi = 1.2, yi = 0.015896164)
//      val in_1      = Clip.ar(in_0, lo = 0.0, hi = 1.0)
//      val loop      = LFDNoise3.ar(1457.0934)
//      val width     = Clip.ar(in_1, lo = 0.0, hi = 1.0)
//      val lFGauss_0 = LFGauss.ar(dur = 100.0, width = width, phase = 0.029190667, loop = loop,
//        doneAction = doNothing)
//      val lorenzL   = LorenzL.ar(freq = lFGauss_0, s = 0.026470285, r = 6.906988E-4, b = 2874.0503,
//        h = 0.026470285, xi = 152.0287, yi = -52.99374, zi = 152.0287)
//      val in_2      = LatoocarfianC.ar(freq = 1495.6631, a = 3.0, b = 3.0, c = 1.5, d = 1495.6631,
//        xi = lorenzL, yi = -52.99374)
//      val in_3      = Clip.ar(in_2, lo = 1.0, hi = 44100.0)
//      val length    = Clip.ar(in_3, lo = 1.0, hi = 44100.0)
//      val in_4      = RunningSum.ar(0.0017667817, length = length)
//      val lFGauss_1 = LFGauss.ar(dur = freq1, width = 1.0, phase = 0.015896164, loop = 0.026470285,
//        doneAction = doNothing)
//      val in_5      = LeakDC.ar(lFGauss_0, coeff = 0.995)
//      val in_6      = LeakDC.ar(lorenzL, coeff = 0.995)
//      val delay1    = Delay1.ar(in_6)
//      val radius    = Clip.ar(in_4, lo = 0.0, hi = 1.0)
//      val twoPole   = TwoPole.ar(in_5, freq = 236.86267, radius = radius)
//      val standardL = StandardL.ar(freq = freq2, k = 0.0, xi = 0.0017667817, yi = -52.99374)
//      val min       = -0.0727693 min standardL
//      val lFDNoise1 = LFDNoise1.ar(-0.0019713242)
//      val dC        = DC.ar(0.9653601)
//      val mix       = Mix(Seq[GE](lFGauss_1, delay1, twoPole, min, lFDNoise1, dC))
//
//      val sig = LeakDC.ar(mix.clip2(1.0)) * 0.49 * amp
//      sig
//    }

    // XXX TODO --- funzt niet (mehr)
    generator("shd-4646") {
      import synth._
      import ugen._
      import Import._

      val pFreq0  = pAudio("p1"     , ParamSpec(5.0 , 18000.0, Warp.Exp), default(9028.016))
      val pFreq1  = pAudio("p2"     , ParamSpec(5.0 , 18000.0, Warp.Exp), default(500.0))
      val pDet1   = pAudio("det"    , ParamSpec(1.0, 2.0), default(1))
      val amp     = pAudio("amp"    , ParamSpec(0.01,     1, Warp.Exp), default(0.1))

      val freq0 = mkExpDetune(pFreq0, pDet1) // seq0.map(_ * "freq0".ar(9028.016))
      val freq1 = mkExpDetune(pFreq1, pDet1) // seq0.map(_ * "freq1".ar(500.0))

      // source code automatically extracted
      val seed = 100
      //NegatumIn()
      RandSeed.ir(seed = seed)

      val lFDNoise0     = LFDNoise0.ar(freq1)
      val in_0          = GbmanL.ar(freq = freq0, xi = 0.0040916516, yi = lFDNoise0)
      val in_1          = Clip.ar(in_0, lo = 0.0, hi = 1.0)
      val width         = Clip.ar(in_1, lo = 0.0, hi = 1.0)
      val lFGauss       = LFGauss.ar(dur = 1.0, width = width, phase = 1.0, loop = 0,
        doneAction = doNothing)
      val linCongN      = LinCongN.ar(freq = 26.002008, a = 12.760639, c = 9035.491, m = 9028.016,
        xi = 1.0)
      val lFDClipNoise  = LFDClipNoise.ar(26.002008)
      val xi_0          = -0.0024913854 min lFDClipNoise
      val min           = 0.0 min linCongN
      val linCongC      = LinCongC.ar(freq = 59.0, a = lFDNoise0, c = 4.2446605E-5, m = 12.760639,
        xi = -199002.66)
      val lFPar         = LFPar.ar(freq = 0.01, iphase = 0.0)
      val in_2          = LFSaw.ar(freq = 12.760639, iphase = 0.0)
      val in_3          = Clip.ar(in_2, lo = 0.5, hi = 1.5)
      val b             = Clip.ar(in_3, lo = 0.5, hi = 1.5)
      val latoocarfianC = LatoocarfianC.ar(freq = 6.5897446, a = 1.0, b = b, c = 0.5, d = -0.051705096,
        xi = xi_0, yi = 1.4142135)
      val dC            = DC.ar(-1.0017723)
      val lFDNoise3     = LFDNoise3.ar(freq1)
      val mix           = Mix(Seq[GE](lFGauss, min, linCongC, lFPar, latoocarfianC, dC, lFDNoise3))
      //NegatumOut(mix)

      val sig = LeakDC.ar(mix.clip2(1.0)) * 0.43 * amp
      sig
    }

//     XXX TODO --- need to retrigger
//    generator("shd-609a") {
//      import synth._
//      import ugen._
//
//      val pFreq0  = pAudio("p1"     , ParamSpec(5.0 , 18000.0, Warp.Exp), default(8703.354))
//      val pFreq1  = pAudio("p2"     , ParamSpec(5.0 , 18000.0, Warp.Exp), default(113.3615))
//      val pDet1   = pAudio("det1"   , ParamSpec(1.0, 2.0), default(1))
//      val pDet2   = pAudio("det2"   , ParamSpec(1.0, 2.0), default(1))
//      val amp     = pAudio("amp"    , ParamSpec(0.01,     1, Warp.Exp), default(0.1))
//
//      val freq0 = mkExpDetune(pFreq0, pDet1) // seq0.map(_ * "freq0".kr(8703.354))
//      val freq1 = mkExpDetune(pFreq1, pDet2) // seq1.map(_ * "freq1".kr(113.3615 * 1))
//      val freq2 = 73.363625
//      val freq3 = 418.54752 // * 0.5
//      val freq4 = 479.24957
//      val noiseAmp = Line.ar(1.0, 0.0, 3.0)
//
//      val seed = 23
//      RandSeed.ir(seed = seed)
//
//      val lFDNoise0       = LFDNoise0.ar(freq0) * noiseAmp
//      val impulse         = Impulse.ar(freq = freq2, phase = 0.0)
//      val in_0            = LFSaw.ar(freq = freq2, iphase = 0.0)
//      val in_1            = Clip.ar(in_0, lo = 0.5, hi = 1.5)
//      val min_0           = lFDNoise0 min -0.0026739505
//      val in_2            = LeakDC.ar(min_0, coeff = 0.995)
//      val sinOsc          = SinOsc.ar(freq = 0.01, phase = -1.8854538)
//      val in_3            = Clip.ar(in_0, lo = 0.0, hi = 0.06)
//      val lFDClipNoise    = LFDClipNoise.ar(freq3) * noiseAmp
//      val h               = Clip.ar(in_3, lo = 0.0, hi = 0.06)
//      val freq_0          = LorenzL.ar(freq = 1.2643237, s = lFDClipNoise, r = freq3, b = 0.058452945,
//        h = h, xi = 179.98584, yi = -0.3874647, zi = 8703.354)
//      val b_0             = Clip.ar(in_1, lo = 0.5, hi = 1.5)
//      val latoocarfianL   = LatoocarfianL.ar(freq = freq_0, a = 0.14208703, b = b_0, c = 0.5, d = 0.0,
//        xi =freq2, yi = -2.2714927)
//      val saw             = Saw.ar(10.0)
//      val bRZ2            = BRZ2.ar(saw)
//      val b_1             = QuadC.ar(freq = latoocarfianL, a = 1.0, b = bRZ2, c = latoocarfianL, xi = saw)
//      val a_0             = Lag3UD.ar(in_2, timeUp = 0.0, timeDown = 30.0)
//      val xi_0            = HenonL.ar(freq = latoocarfianL, a = a_0, b = 179.98584, x0 = 0.0,
//        x1 = latoocarfianL)
//      val a_1             = xi_0 - -0.15114462
//      val quadN           = QuadN.ar(freq = 3730.0078, a = a_1, b = 0.0, c = 276.54956, xi = 0.0)
//      val min_1           = quadN min 73.36357
//      val linCongN        = LinCongN.ar(freq = 16.7419, a = 0.0, c = 0.0052589797, m = 0.0, xi = 0.14208703)
//      val min_2           = lFDClipNoise min linCongN
//      val in_4            = min_2 min min_1
//      val in_5            = Clip.ar(in_4, lo = 0.0, hi = 30.0)
//      val time            = Clip.ar(in_5, lo = 0.0, hi = 30.0)
//      val lag             = Lag.ar(lFDClipNoise, time = time)
//      val freq_1          = 0.0 min in_4
//      val times           = freq_1 * 2.0
//      val c_0             = in_4 min 0.130261
//      val quadL           = QuadL.ar(freq = -0.19312993, a = 1.0, b = b_1, c = c_0, xi = xi_0)
//      val in_6            = Clip.ar(bRZ2, lo = 0.001, hi = 2.0)
//      val in_7            = LFSaw.ar(freq = 0.01, iphase = 0.0)
//      val winSize         = Clip.ar(in_6, lo = 0.001, hi = 2.0)
//      val pitchDispersion = Clip.ar(in_7, lo = 0.0, hi = 1.0)
//      val timeDispersion  = 0.0 min winSize
//      val pitchShift      = PitchShift.ar(saw, winSize = winSize, pitchRatio = 4.0,
//        pitchDispersion = pitchDispersion, timeDispersion = timeDispersion)
//      val lFDNoise3       = LFDNoise3.ar(freq_1)
//      val bitXor          = 0.0 ^ quadL
//      val varSaw          = VarSaw.ar(freq = freq1, iphase = 1.0, width = 0.0)
//      val linCongC        = LinCongC.ar(freq = freq4, a = 0.0, c = freq4, m = min_0, xi = freq4)
//      val mix             = Mix(
//        Seq[GE](
//          impulse,
//          sinOsc,
//          lag,
//          times,
//          pitchShift,
//          lFDNoise3,
//          bitXor,
//          varSaw,
//          linCongC
//        ))
//      //NegatumOut(mix)
//      val sig = LeakDC.ar(mix.clip2(1.0)) * 0.48 * amp
//      sig
//    }

    generator("shd-e6ee") {
      import synth._
      import ugen._
      import Import._

      val pFreq0  = pAudio("p1"     , ParamSpec(1.0 , 10000.0, Warp.Exp), default(10.0))
      val pFreq1  = 10.0 // pAudio("p2"     , ParamSpec(1.0 , 10000.0, Warp.Exp), default(10.0))
      val pFreq2  = 10.0 // pAudio("p3"     , ParamSpec(1.0 , 10000.0, Warp.Exp), default(10.0))
      val pFreq3  = pAudio("p4"     , ParamSpec(5.0 , 18000.0, Warp.Exp), default(7387.0054))
      val pAmt0   = pAudio("sin"    , ParamSpec(0.0001, 1.0, Warp.Exp), default(1.0))
      val pDet1   = pAudio("det"    , ParamSpec(1.0, 0.5), default(1))
      val amp     = pAudio("amp"    , ParamSpec(0.01,     1, Warp.Exp), default(0.1))

      val freq1 = mkExpDetune(pFreq0, pDet1) // "freq1".ar(10.0)
      val freq2 = mkExpDetune(pFreq1, pDet1) // "freq2".ar(10.0)
      val freq3 = mkExpDetune(pFreq2, pDet1) // "freq3".ar(10.0)
      val freq4 = mkExpDetune(pFreq3, pDet1) // "freq4".ar(7387.0054)
      val freq0 = freq1 // seq0.map(_ * freq1)
      val sinAmt = mkDetune(pAmt0, pDet1) // "sin-amt".ar(1.0)
      val durFactor = 1.0 // "dur-factor".ar(1.0)

      val seed = 33
      RandSeed.ir(seed = seed)

      val in_0            = Saw.ar(freq0)
      val in_1            = Lag2.ar(in_0, time = 30.0)
      val width           = Clip.ar(in_1, lo = 0.0, hi = 1.0)
      val in_2            = Pulse.ar(freq = freq2, width = width)
      val min_0           = 2.4775672E-4 min in_2
      val in_3            = Clip.ar(in_2, lo = 0.0, hi = 20.0)
      val max_0           = min_0 max 0.0
      val maxDelayTime_0  = Clip.ar(in_3, lo = 0.0, hi = 20.0)
      val max_1           = max_0 max 0.0
      val delayTime_0     = max_1 min maxDelayTime_0
      val delayC_0        = DelayC.ar(in_2, maxDelayTime = maxDelayTime_0, delayTime = delayTime_0)
      val min_1           = delayC_0 min in_2
      val sinOsc          = SinOsc.ar(freq = 0.01 / durFactor, phase = 7.015298E-4)
      val gbmanL_0        = GbmanL.ar(freq = freq4, xi = 8001.727, yi = 14.1340885)
      val in_4            = SinOsc.ar(freq = 0.01, phase = 0.018470688)
      val in_5            = LeakDC.ar(in_4, coeff = 0.995)
      val freq_0          = Lag2UD.ar(in_5, timeUp = 17.423637, timeDown = 0.34902483)
      val gbmanL_1        = GbmanL.ar(freq = freq_0, xi = 17.423637, yi = 0.0)
      val blip            = Blip.ar(freq = freq3, numHarm = 1.0)
      val min_2           = blip min 0.0
      val min_3           = 0.2 min gbmanL_0
      val min_4           = min_2 min min_3
      val fold2           = -0.0035440645 fold2 gbmanL_1
      val min_5           = min_1 min fold2
      val min_6           = min_5 min -0.0035440645
      val min_7           = min_4 min min_6
      val min_8           = -0.0035440645 min in_0
      val in_6            = LFSaw.ar(freq = 0.01, iphase = 0.0)
      val in_7            = LeakDC.ar(in_6, coeff = 0.995)
      val maxDelayTime_1  = Clip.ar(in_3, lo = 0.0, hi = 20.0)
      val delayTime_1     = 0.0 min maxDelayTime_1
      val delayC_1        = DelayC.ar(in_7, maxDelayTime = maxDelayTime_1, delayTime = delayTime_1)
      val dC              = DC.ar(-0.0035440645)
      val mix             = Mix(Seq[GE](
        sinOsc,
        min_7 * sinAmt,
        min_8,
        delayC_1, dC,
      ))
      val sig = LeakDC.ar(mix.clip2(1.0)) * 0.849 * amp
      sig
    }

    // XXX TODO : doppelt
//    generator("shd-39c4") {
//      import synth._
//      import ugen._
//
//      val pFreq0  = pAudio("p1"     , ParamSpec(1.0e-5 , 1.0, Warp.Exp), default(8.722097E-4))
//      val pFreq1  = pAudio("p2"     , ParamSpec(1.0 , 10000.0, Warp.Exp), default(10.0))
//      val pFreq2  = pAudio("p3"     , ParamSpec(1.0 , 10000.0, Warp.Exp), default(29.300417))
//      val pFreq3  = pAudio("p4"     , ParamSpec(5.0 , 18000.0, Warp.Exp), default(1209.1062))
//      val pWidth  = pAudio("width"  , ParamSpec(0.001 , 0.49, Warp.Exp), default(0.031116353))
//      val pDet1   = pAudio("det"    , ParamSpec(1.0, 0.5), default(1))
//      val amp     = pAudio("amp"    , ParamSpec(0.01,     1, Warp.Exp), default(0.1))
//
//      RandSeed.ir
//      val freq0   = mkExpDetune(pFreq0, pDet1) // seq0.map(_ * 8.722097E-4)
//      val freq1   = mkExpDetune(pFreq1, pDet1) // seq0.map(_ * "freq1".kr(10.0))
//      val width0  = mkDetune(pWidth, pDet1) // seq0.map(_ * "width0".kr(0.031116353))
//      val freq2   = mkExpDetune(pFreq2, pDet1) // seq0.map(_ * "freq2".kr(29.300417))
//      val freq3   = mkExpDetune(pFreq3, pDet1)  // seq0.map(_ * "freq3".kr(1209.1062))
//
//      val lFDNoise3_0   = LFDNoise3.ar(freq0)
//      val lFDNoise3_1   = LFDNoise3.ar(500.0)
//      val atan2         = lFDNoise3_0 atan2 lFDNoise3_1
//      val max           = atan2 max 1.0
//      val blip_0        = Blip.ar(freq = freq1, numHarm = 1.0)
//      val numHarm_0     = max max 1.0
//      val blip_1        = Blip.ar(freq = freq1, numHarm = numHarm_0)
//      val in_0          = blip_1 max 5.0E-5
//      val dur           = Clip.ar(in_0, lo = 5.0E-5, hi = 100.0)
//      val lFGauss       = LFGauss.ar(dur = dur, width = width0, phase = 1.0, loop = 1209.1062,
//        doneAction = doNothing)
//      val in_1          = LFSaw.ar(freq = 0.01, iphase = 0.0)
//      val leakDC_0      = LeakDC.ar(in_1, coeff = 0.995)
//      val quadC         = QuadC.ar(freq = 0.0, a = 0.0, b = 0.0, c = 0.0, xi = 0.2067178)
//      val in_2          = blip_1 max 0.01
//      val sawFreq       = Clip.ar(in_2, lo = 0.01, hi = 20000.0)
//      val d             = SyncSaw.ar(syncFreq = 0.01, sawFreq = sawFreq)
//      val plus_0        = 4088.3284 + blip_0
//      val trunc         = d trunc 4088.3284
//      val lFPar         = LFPar.ar(freq = 0.01, iphase = 1.0)
//      val latoocarfianC = LatoocarfianC.ar(freq = freq2, a = -3.0, b = 0.5, c = 0.5, d = -0.14014469,
//        xi = 0.0, yi = 0.0104253385)
//      val standardL     = StandardL.ar(freq = freq2, k = latoocarfianC, xi = latoocarfianC, yi = 0.0)
//      val in_3          = LFPar.ar(freq = freq2, iphase = 0.0)
//      val freq_0        = BRF.ar(in_3, freq = 10.0, rq = 0.01)
//      val in_4          = freq_0 min -0.14014469
//      val in_5          = LeakDC.ar(in_4, coeff = 0.995)
//      val lPZ2          = LPZ2.ar(in_5)
//      val min_0         = lPZ2 min 0.0
//      val xi_0          = min_0 absDif trunc
//      val min_1         = freq_0 min lFPar
//      val latoocarfianL = LatoocarfianL.ar(freq = freq3, a = 3.0, b = 0.5, c = 1.5, d = d,
//        xi = -0.14014469, yi = 0.2067178)
//      val sumsqr        = min_1 sumSqr latoocarfianL
//      val yi_0          = min_1 * plus_0
//      val in_6          = GbmanL.ar(freq = freq_0, xi = xi_0, yi = yi_0)
//      val leakDC_1      = LeakDC.ar(in_6, coeff = 0.995)
//      val plus_1        = leakDC_1 + leakDC_0
//      val roundTo       = 0.0092437165 roundTo latoocarfianL
//      val mix           = Mix(Seq[GE](lFGauss, quadC, standardL, sumsqr, plus_1, roundTo))
//      val sig = LeakDC.ar(mix.clip2(1.0)) * 0.65 * amp
//      sig
//    }

    generator("shd-65fb") {
      import synth._
      import ugen._
      import Import._

      val pFreq0  = pAudio("p1"     , ParamSpec(1.0 , 10000.0, Warp.Exp), default(7.307391))
      val pFreq1  = pAudio("p2"     , ParamSpec(5.0 , 18000.0, Warp.Exp), default(1093.7816))
      val pDet1   = pAudio("det1"   , ParamSpec(1.0, 0.5), default(1))
      val pDet2   = pAudio("det2"   , ParamSpec(1.0, 0.5), default(1))
      val amp     = pAudio("amp"    , ParamSpec(0.01,     1, Warp.Exp), default(0.1))

      val freq0: GE = mkExpDetune(pFreq0, pDet1) // seq0: GE) * "freq0".ar(7.307391)
      val freq1: GE = mkExpDetune(pFreq1, pDet2) // (seq1: GE) * "freq1".ar(1093.7816 * 0.125)

      RandSeed.ir
      val xi            = LinCongN.ar(freq = freq0, a = 87.24392, c = 87.24392, m = 0.0, xi = 87.24392)
      val freq_0        = Blip.ar(freq = 10.0, numHarm = 1.3612523)
      val phase         = StandardL.ar(freq = freq_0, k = 1.48179, xi = 0.009056966, yi = 3.1415925)
      val sinOsc        = SinOsc.ar(freq = 87.24392, phase = phase)
      val min_0         = sinOsc min 0.0
      val in_0          = LatoocarfianC.ar(freq = 1.6576302E-4, a = 3.0, b = 0.5, c = 0.5, d = -3250.8896,
        xi = 0.23779522, yi = -3250.8896)
      val in_1          = SinOsc.ar(freq = freq1, phase = 0.0)
      val release       = Clip.ar(in_0, lo = 0.0, hi = 30.0)
      val in_2          = Decay2.ar(in_1, attack = 0.0, release = release)
      val in_3          = in_2 trunc xi
      val in_4          = LeakDC.ar(in_3, coeff = 0.995)
      val bPZ2_0        = BPZ2.ar(in_4)
      val r             = -3250.8896 ring3 bPZ2_0
      val min_1         = -3250.8896 min r
      val min_2         = r min 0.0
      val in_5          = LeakDC.ar(min_1, coeff = 0.995)
      val hPF           = HPF.ar(in_5, freq = 8308.33)
      val latoocarfianC = LatoocarfianC.ar(freq = freq0, a = 3.0, b = 1.5, c = 0.5, d = 0.0,
        xi = 1.3612523, yi = -0.016525794)
      val sqrsum        = latoocarfianC sqrSum 0.0
      val lorenzL       = LorenzL.ar(freq = 87.0, s = 2.991784, r = r, b = 0.0, h = 0.0, xi = xi,
        yi = min_0, zi = min_1)
      val in_6          = LeakDC.ar(min_0, coeff = 0.995)
      val delayL        = DelayL.ar(in_6, maxDelayTime = 0.0, delayTime = 0.0)
      val lFGauss       = LFGauss.ar(dur = 5.0E-5, width = 0.17485431, phase = 1.6576302E-4, loop = 0,
        doneAction = doNothing)
      val plus          = freq1 + lFGauss
      val in_7          = LeakDC.ar(in_2, coeff = 0.995)
      val bPZ2_1        = BPZ2.ar(in_7)
      val min_3         = freq_0 min in_2
      val lFCub         = LFCub.ar(freq = 87.24392, iphase = 0.0)
      val dC            = DC.ar(-18547.445)
      val mix           = Mix(Seq[GE](min_2, hPF, sqrsum, lorenzL, delayL, plus, bPZ2_1, min_3, lFCub, dC))
      //NegatumOut(mix)
      val sig = LeakDC.ar(mix.clip2(1.0)) * 0.41 * amp
      sig
    }

    generator("shd-3e66") {
      import synth._
      import ugen._
      import Import._

      val pFreq0  = pAudio("p1"     , ParamSpec(1.0 , 10000.0, Warp.Exp), default(441.0))
      val pFreq1  = pAudio("p2"     , ParamSpec(5.0 , 18000.0, Warp.Exp), default(501.0))
      val pDet1   = pAudio("det"   , ParamSpec(1.0, 0.5), default(1))
//      val pDet2   = pAudio("det2"   , ParamSpec(1.0, 0.5), default(1))
      val amp     = pAudio("amp"    , ParamSpec(0.01,     1, Warp.Exp), default(0.1))

//      val seq0 = Seq.tabulate(4)(i => 1.005.pow(i))
      val freq0 = mkExpDetune(pFreq0, pDet1) // (seq0: GE) * "freq0".ar(440.0)
      val freq1 = mkExpDetune(pFreq1, pDet1) // (seq0: GE) * "freq1".ar(500.0)

      RandSeed.ir

      val freq_0      = Saw.ar(freq0)
      val in_0        = GbmanL.ar(freq = freq_0, xi = 0.0, yi = 0.037021473)
      val iphase      = Clip.ar(in_0, lo = 0.0, hi = 1.0)
      val in_1        = LFCub.ar(freq = 0.041745085, iphase = iphase)
      val in_2        = LeakDC.ar(in_1, coeff = 0.995)
      val freq_1      = LPZ1.ar(in_2)
      val in_3        = in_0 excess -1.0
      val c           = Clip.ar(in_3, lo = 0.5, hi = 1.5)
      val in_4        = LatoocarfianC.ar(freq = freq_1, a = 0.037021473, b = 1.5, c = c, d = 48.15306,
        xi = -0.0027588373, yi = 0.28410193)
      val in_5        = LeakDC.ar(in_4, coeff = 0.995)
      val in_6        = TwoZero.ar(in_5, freq = 48.15306, radius = 1.0)
      val delayC      = DelayC.ar(in_6, maxDelayTime = 0.0, delayTime = 0.0)
      val in_7        = LeakDC.ar(in_0, coeff = 0.995)
      val in_8        = freq_0 pow -0.0027588373
      val in_9        = Clip.ar(in_8, lo = 5.0E-5, hi = 100.0)
      val dur         = Clip.ar(in_9, lo = 5.0E-5, hi = 100.0)
      val lFGauss     = LFGauss.ar(dur = dur, width = 0.009814887, phase = 0.0, loop = -134.3782,
        doneAction = doNothing)
      val min_0       = delayC min 0.0
      val pitchShift  = PitchShift.ar(in_7, winSize = 0.001, pitchRatio = 0.0, pitchDispersion = 0.0,
        timeDispersion = 0.0)
      val min_1       = -0.0027588373 min pitchShift
      val min_2       = 0.0 min pitchShift
      val lFDNoise3   = LFDNoise3.ar(freq1)
      val mix         = Mix(Seq[GE](lFGauss, min_0, min_1, min_2, lFDNoise3))
      //NegatumOut(mix)
      val sig = LeakDC.ar(mix.clip2(1.0)) * 0.69 * amp
      sig
    }

    generator("shd-9f80") {
      import synth._
      import ugen._
      import Import._

      val pFreq0  = pAudio("p1"     , ParamSpec(5.0 , 18000.0, Warp.Exp), default(812.973))
      val pFreq1  = pAudio("p2"     , ParamSpec(5.0 , 18000.0, Warp.Exp), default(179.85915))
      val pFreq2  = pAudio("p3"     , ParamSpec(0.1 , 1000.0, Warp.Exp), default(2.9367235))
      val pDet1   = pAudio("det1"   , ParamSpec(1.0, 0.5), default(1))
      val pDet2   = pAudio("det2"   , ParamSpec(1.0, 0.5), default(1))
      val amp     = pAudio("amp"    , ParamSpec(0.01,     1, Warp.Exp), default(0.1))

      //      val seq0 = Seq.tabulate(4)(i => 1.005.pow(i))
      val freq0 = mkExpDetune(pFreq0, pDet1) // (seq0: GE) * "freq0".ar(440.0)
      val freq1 = mkExpDetune(pFreq1, pDet2) // (seq0: GE) * "freq1".ar(500.0)
      val freq2 = mkExpDetune(pFreq2, pDet2) // (seq0: GE) * "freq1".ar(500.0)

//      val seq0 = Seq.tabulate(4)(i => 0.99995.pow(i))
//      val seq1 = Seq.tabulate(4)(i => 1.00005.pow(i))

//      val freq0 = (seq0: GE) * "freq0".ar(8812.973)
//      val freq1 = (seq1: GE) * "freq1".ar(179.85915 * 0.2)

      val seed = 46
      RandSeed.ir(seed = seed)

      val lFCub     = LFCub.ar(freq = freq0, iphase = 1.0)
      val c         = lFCub min lFCub
      val freq_0    = c min 0.0
      val in_0      = LFDNoise1.ar(-136.7135)
      val in_1      = HenonN.ar(freq = Nyquist() /* could not parse! */, a = 1.4067115, b = 1218.9697,
        x0 = 0.0, x1 = 0.0)
      val minus     = in_1 - lFCub
      val min       = minus min 0.0
      val phase     = 179.85915 * freq_0
      val linCongL  = LinCongL.ar(freq = freq_0, a = 6.7240785E-6, c = c, m = -136.7135,
        xi = 6.7240785E-6)
      val in_2      = LeakDC.ar(linCongL, coeff = 0.995)
      val rLPF      = RLPF.ar(in_2, freq = 2281.065, rq = 1.0)
      val in_3      = min sqrDif rLPF
      val sinOsc    = SinOsc.ar(freq = freq1, phase = phase)
      val in_4      = LeakDC.ar(in_1, coeff = 0.995)
      val bPZ2      = BPZ2.ar(in_4)
      val in_5      = phase * bPZ2
      val in_6      = Clip.ar(in_5, lo = 0.01, hi = 20000.0)
      val syncFreq  = Clip.ar(in_6, lo = 0.01, hi = 20000.0)
      val syncSaw   = SyncSaw.ar(syncFreq = syncFreq, sawFreq = freq2)
      val in_7      = LeakDC.ar(in_0, coeff = 0.995)
      val decayTime = LinCongL.ar(freq = freq_0, a = 6.7240785E-6, c = 1.5707961, m = linCongL,
        xi = 198.49162)
      val allpassC  = AllpassC.ar(in_3, maxDelayTime = 1.3754512, delayTime = 1.3754512,
        decayTime = decayTime)
      val bRF       = BRF.ar(in_7, freq = 10.0, rq = 1.0)
      val mod       = phase % decayTime
      val dC        = DC.ar(1.3448157E-5)
      val mix       = Mix(Seq[GE](sinOsc, syncSaw, allpassC, bRF, mod, dC))
      //NegatumOut(mix)
      val sig = LeakDC.ar(mix.clip2(1.0)) * 0.49 * amp
      sig
    }

    generator("shd-15c2") {
      import synth._
      import ugen._
      import Import._

      val pFreq0  = pAudio("p1"     , ParamSpec(5.0 , 18000.0, Warp.Exp), default(149.40091))
      val pFreq1  = pAudio("p2"     , ParamSpec(-10.0 , -0.01, Warp.Exp), default(-1.5486484))
//      val pFreq2  = pAudio("p3"     , ParamSpec(0.55 * 0.1 , 0.55 * 1.95, Warp.Exp), default(0.55))
//      val pFreq2  = pAudio("p3"     , ParamSpec(0.1 , 1000.0, Warp.Exp), default(10.0))
      val pDet1   = pAudio("det"    , ParamSpec(1.0, 0.5), default(1))
      val amp     = pAudio("amp"    , ParamSpec(0.01,     1, Warp.Exp), default(0.1))

      val freq0 = mkExpDetune(pFreq0, pDet1) // Seq(1.0, 1.02): GE) * "freq0".ar(149.40091)
      val freq1 = mkExpDetune(pFreq1, pDet1) // (Seq(1.0, 0.99): GE) * "freq1".ar(-1.5486484)
      val freq2 = 10.0 // pFreq2 // mkExpDetune(pFreq2, pDet1) // 10.0 // "freq2".ar(10.0)
      val bpfReso = 0.01 // "bpf-q".ar(100).reciprocal

      RandSeed.ir
      val standardL     = StandardL.ar(freq = freq0, k = freq1, xi = 0.009847637, yi = freq1)
      val in_0          = standardL excess 0.009847637 // * 0.1)
      val hPZ1          = HPZ1.ar(in_0)
      val min_0         = hPZ1 min 0.0
      val in_1          = LeakDC.ar(hPZ1, coeff = 0.995)
      val delay1        = Delay1.ar(in_1)
      val min_1         = delay1 min hPZ1
      val min_2         = in_0 min min_1
      val dryLevel      = min_0 min min_2
      val k_0           = SinOsc.ar(freq = 0.01, phase = 0.0)
      val in_2          = StandardL.ar(freq = min_1, k = k_0, xi = min_1, yi = min_2)
      val lFDNoise3     = LFDNoise3.ar(freq1) // -1.5486484)
      val in_3          = Clip.ar(hPZ1, lo = 0.0, hi = 30.0)
      val saw           = Saw.ar(freq2)
      val ring4         = saw ring4 freq1 // -1.5486484
      val decayTime     = in_2 min ring4
      val in_4          = LeakDC.ar(in_2, coeff = 0.995)
      val in_5          = Clip.ar(min_2, lo = 0.0, hi = 1.0)
      val earlyRefLevel = BPF.ar(in_4, freq = 10.0, rq = bpfReso)
      val in_6          = LeakDC.ar(0.0, coeff = 0.995)
      val revTime       = Clip.ar(in_3, lo = 0.0, hi = 100.0)
      val inputBW       = Clip.ar(in_5, lo = 0.0, hi = 1.0)
      val gVerb         = GVerb.ar(in_6, roomSize = 0.55 /*A2K.kr(pFreq2)*/, revTime = revTime, damping = 0.0,
        inputBW = inputBW, spread = 0.0, dryLevel = dryLevel,
        earlyRefLevel = earlyRefLevel, tailLevel = hPZ1, maxRoomSize = 0.55)
      val allpassL      = AllpassL.ar(in_4, maxDelayTime = 0.0, delayTime = 0.0, decayTime = decayTime)
      val mix           = Mix(Seq[GE](lFDNoise3, gVerb, allpassL))

      val sig = LeakDC.ar((mix * 0.25).clip2(1.0)) * 0.94 * amp
      sig
    }
  }
}