/*
 *  Promenade.scala
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

import de.sciss.lucre.synth.Txn
import de.sciss.nuages
import de.sciss.nuages.{Nuages, ScissProcs}
import de.sciss.proc.{ParamSpec, Warp}
import de.sciss.synth.ugen.ControlValues

object Promenade {
  def apply[T <: Txn[T]](dsl: nuages.DSL[T], sCfg: ScissProcs.Config, nCfg: Nuages.Config)
                        (implicit tx: T, n: Nuages[T]): Unit = {
    import dsl._

//    val masterChansOption = nCfg.masterChannels

//    val numOut = if (sCfg.generatorChannels <= 0) masterChansOption.fold(2)(_.size) else sCfg.generatorChannels

    def default(in: Double): ControlValues =
      if (sCfg.genNumChannels <= 0)
        in
      else
        Vector.fill(sCfg.genNumChannels)(in)


    generator("prom-7") {
      import de.sciss.synth._
      import ugen._
      import Import._
      //      val v11   = pAudio("p1"     , ParamSpec(0.0001, 1.0, Warp.Exp), default(0.014))
//      val p1              = 5814.3823.!
      val p1              = pAudio("p1", ParamSpec(0.1, 20000.0, Warp.Exp), default(5814.3823))
      val p2              = pAudio("p2", ParamSpec(0.1, 20000.0, Warp.Exp), default(2298.9329))
      val p3              = pAudio("p3", ParamSpec(0.1, 20000.0, Warp.Exp), default(2195.7336))
      val p4              = pAudio("p4", ParamSpec(0.1, 20000.0, Warp.Exp), default( 440.0))

      val in_0            = Protect(-607.21533, -inf, inf, dynamic = true)
      val maxDelayTime_0  = Protect(-607.21533, 0.0, 20.0, dynamic = false)
      val protect_0       = Protect(-607.21533, 0.0, inf, dynamic = false)
      val delayTime_0     = protect_0 min maxDelayTime_0
      val m               = DelayL.ar(in_0, maxDelayTime = maxDelayTime_0, delayTime = delayTime_0)
      val decayTime_0     = -607.21533 >= m
      val in_1            = Protect(0.51179975, -inf, inf, dynamic = true)
      val c_0             = OneZero.ar(in_1, coeff = 0.5)
      val iphase_0        = Protect(p2, 0.0, 1.0, dynamic = false)
      val lFPar_0         = LFPar.ar(freq = p3, iphase = iphase_0)
      val coeff_0         = LFDNoise3.ar(0.51179975)
      val phase_0         = Protect(-4.9757495, 0.0, 1.0, dynamic = false)
      val impulse         = Impulse.ar(freq = 0.94795537, phase = phase_0)
      val in_3            = Protect(impulse, -inf, inf, dynamic = true)
      val oneZero         = OneZero.ar(in_3, coeff = coeff_0)
      val min_0           = 0.01807581 min oneZero
      val in_4            = coeff_0 min min_0
      val min_1           = in_4 min in_4
      val min_2           = min_0 min -0.03851429
      val min_3           = min_2 min -0.03851429
      val min_4           = min_3 min -0.03851429
      val min_5           = min_4 min -0.03851429
      val a_0             = LFDNoise1.ar(0.7241163)
      val freq_0          = Protect(min_5, -inf, inf, dynamic = false)
      val linCongC        = LinCongC.ar(freq = freq_0, a = a_0, c = -607.21533, m = m, xi = 0.014517559)
      val in_5            = Protect(linCongC, -inf, inf, dynamic = true)
      val maxDelayTime_1  = Protect(-0.03851429, 0.0, 20.0, dynamic = false)
      val protect_1       = Protect(-0.03851429, 0.0, inf, dynamic = false)
      val delayTime_1     = protect_1 min maxDelayTime_1
      val combN_0         = CombN.ar(in_5, maxDelayTime = maxDelayTime_1, delayTime = delayTime_1,
        decayTime = decayTime_0)
      val geq             = -0.008055595 >= linCongC
      val min_6           = lFPar_0 min -0.008055595
      val in_6            = Protect(c_0, -inf, inf, dynamic = true)
      val time_0          = Protect(min_6, 0.0, 30.0, dynamic = false)
      val lag2            = Lag2.ar(in_6, time = time_0)
      val min_7           = p3 min lag2
      val min_8           = min_7 min geq
      val min_9           = min_8 min geq
      val a_1             = min_9 min geq
      val freq_1          = Protect(combN_0, -inf, inf, dynamic = false)
      val henonN          = HenonN.ar(freq = freq_1, a = a_1, b = -0.008055595, x0 = -607.21533,
        x1 = -0.03851429)
      val min_10          = -0.03851429 min henonN
      val min_11          = henonN min min_10
      val min_12          = min_11 min henonN
      val min_13          = min_12 min henonN
      val min_14          = min_13 min henonN
      val min_15          = min_14 min henonN
      val min_16          = min_15 min henonN
      val min_17          = min_16 min henonN
      val in_7            = Protect(min_17, -inf, inf, dynamic = true)
      val freq_2          = Protect(-607.21533, 10.0, 20000.0, dynamic = false)
      val twoZero_1       = TwoZero.ar(in_7, freq = freq_2, radius = 0.4486289)
      val min_18          = min_6 min 0.2474362
      val min_19          = min_18 min 0.0025734832
      val min_20          = min_19 min 0.0025734832
      val in_8            = Protect(min_20, -inf, inf, dynamic = true)
      val bRZ2            = BRZ2.ar(in_8)
      val min_21          = bRZ2 min geq
      val earlyRefLevel_0 = min_21 min geq
      val min_22          = earlyRefLevel_0 min geq
      val min_23          = min_22 min geq
      val min_24          = min_23 min geq
      val min_25          = min_24 min geq
      val min_26          = min_25 min geq
      val min_27          = min_26 min geq
      val min_28          = min_27 min geq
      val min_29          = min_28 min geq
      val min_30          = min_29 min geq
      val min_31          = min_30 min geq
      val min_32          = min_31 min geq
      val in_9            = Protect(geq, -inf, inf, dynamic = true)
      val winSize         = Protect(geq, 0.001, 2.0, dynamic = false)
      val pitchRatio      = Protect(linCongC, 0.0, 4.0, dynamic = false)
      val protect_2       = Protect(min_32, 0.0, inf, dynamic = false)
      val timeDispersion  = protect_2 min winSize
      val pitchShift      = PitchShift.ar(in_9, winSize = winSize, pitchRatio = pitchRatio,
        pitchDispersion = 0.0025734832, timeDispersion = timeDispersion)
      val in_10           = Protect(pitchShift, -inf, inf, dynamic = true)
      val freq_3          = Protect(-410.82947, 10.0, 20000.0, dynamic = false)
      val radius_1        = Protect(-0.03851429, 0.0, 1.0, dynamic = false)
      val twoZero_2       = TwoZero.ar(in_10, freq = freq_3, radius = radius_1)
      val a_2             = Protect(twoZero_2, -3.0, 3.0, dynamic = false)
      val b_0             = Protect(33.599243, 0.5, 1.5, dynamic = false)
      val c_1             = Protect(-0.008055595, 0.5, 1.5, dynamic = false)
      val xi_0            = Protect(min_6, -inf, inf, dynamic = false)
      val yi_0            = Protect(twoZero_1, -inf, inf, dynamic = false)
      val latoocarfianC_0 = LatoocarfianC.ar(freq = -607.21533, a = a_2, b = b_0, c = c_1, d = 0.5,
        xi = xi_0, yi = yi_0)
      val d_0             = latoocarfianC_0 min geq
      val min_33          = d_0 min geq
      val min_34          = 0.1369887 min min_1
      val min_35          = min_34 min min_1
      val freq_4          = Protect(-130.13501, 0.01, 20000.0, dynamic = false)
      val iphase_1        = Protect(latoocarfianC_0, 0.0, 1.0, dynamic = false)
      val lFPar_1         = LFPar.ar(freq = freq_4, iphase = iphase_1)
      val min_36          = 0.004442197 min lFPar_1
      val min_37          = min_36 min lFPar_1
      val min_38          = min_37 min lFPar_1
      val atan2           = 0.1369887 atan2 twoZero_2
      val min_39          = atan2 min henonN
      val min_40          = min_39 min henonN
      val min_41          = min_40 min henonN
      val min_42          = min_41 min henonN
      val min_43          = min_42 min henonN
      val in_11           = Protect(lFPar_1, -inf, inf, dynamic = true)
      val maxDelayTime_2  = Protect(henonN, 0.0, 20.0, dynamic = false)
      val protect_3       = Protect(min_43, 0.0, inf, dynamic = false)
      val delayTime_2     = protect_3 min maxDelayTime_2
      val in_12           = CombC.ar(in_11, maxDelayTime = maxDelayTime_2, delayTime = delayTime_2,
        decayTime = 0.01807581)
      val min_44          = -0.03851429 min min_38
      val min_45          = min_44 min in_12
      val min_46          = min_45 min in_12
      val min_49          = decayTime_0 min -0.03851429
      val min_71          = min_35 min min_1
      val numHarm         = Protect(linCongC, 1.0, inf, dynamic = false)
      val phase_1         = Blip.ar(freq = p4, numHarm = numHarm)
      val min_96          = twoZero_1 min 0.1369887
      val min_97          = min_96 min 0.1369887
      val leq             = min_38 <= in_4
      val min_112         = min_49 min -0.03851429
      val length_1        = Protect(twoZero_2, 1.0, 44100.0, dynamic = false)
      val decayTime_3     = RunningSum.ar(leq, length = length_1)
      val in_25           = Protect(0.0025734832, -inf, inf, dynamic = true)
      val protect_9       = Protect(min_20, 0.0, inf, dynamic = false)
      val delayTime_7     = protect_9 min 0.0025734832
      val combN_2         = CombN.ar(in_25, maxDelayTime = 0.0025734832, delayTime = delayTime_7,
        decayTime = decayTime_3)
      val in_26           = Protect(min_112, -inf, inf, dynamic = true)
      val timeUp_1        = Protect(combN_2, 0.0, 30.0, dynamic = false)
      val timeDown_0      = Protect(31.062782, 0.0, 30.0, dynamic = false)
      val lagUD_1         = LagUD.ar(in_26, timeUp = timeUp_1, timeDown = timeDown_0)
      val amclip          = phase_1 amClip min_97
      val in_27           = Protect(0.0026768947, -inf, inf, dynamic = true)
      val coeff_2         = Protect(lagUD_1, -0.999, 0.999, dynamic = false)
      val dryLevel_1      = Integrator.ar(in_27, coeff = coeff_2)
      val freq_14         = Protect(dryLevel_1, -inf, inf, dynamic = false)
      val a_6             = Protect(lFPar_0, -3.0, 3.0, dynamic = false)
      val c_3             = Protect(175.02394, 0.5, 1.5, dynamic = false)
      val xi_2            = Protect(lFPar_1, -inf, inf, dynamic = false)
      val yi_2            = Protect(amclip, -inf, inf, dynamic = false)
      val d_1             = LatoocarfianL.ar(freq = freq_14, a = a_6, b = 3.0, c = c_3, d = 0.1369887,
        xi = xi_2, yi = yi_2)
      val min_137         = 0.4486289 min min_46
      val min_138         = min_137 min min_46
      val min_139         = min_138 min min_46
      val min_141         = min_139 min min_46
      val min_142         = min_141 min min_46
      val min_143         = min_142 min min_46
      val min_144         = min_112 min -0.03851429
      val min_145         = min_33 min geq
      val freq_16         = Protect(min_145, -inf, inf, dynamic = false)
      val a_7             = Protect(min_143, -3.0, 3.0, dynamic = false)
      val b_2             = Protect(-607.21533, 0.5, 1.5, dynamic = false)
      val c_4             = Protect(min_144, 0.5, 1.5, dynamic = false)
      val xi_5            = Protect(min_29, -inf, inf, dynamic = false)
      val yi_4            = Protect(min_71, -inf, inf, dynamic = false)
      val latoocarfianN   = LatoocarfianN.ar(freq = freq_16, a = a_7, b = b_2, c = c_4, d = d_1, xi = xi_5,
        yi = yi_4)
      val sinOsc          = SinOsc.ar(freq = p1, phase = phase_1)
      val ring4           = p2 ring4 sinOsc
      val mix             = latoocarfianN + ring4
      val sig             = NegatumOutPre(mix)
      val _amp   = pAudio("amp", ParamSpec(0.01, 1, Warp.Exp), default(0.1))
      sig * _amp
    }
  }
}
