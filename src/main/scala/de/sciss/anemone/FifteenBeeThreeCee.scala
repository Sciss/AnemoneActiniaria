/*
 *  FifteenBeeThreeCee.scala
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

import de.sciss.lucre.stm.Sys
import de.sciss.nuages.{ExpWarp, Nuages, ParamSpec, ScissProcs}
import de.sciss.synth.ugen.ControlValues
import de.sciss.{nuages, synth}

object FifteenBeeThreeCee {
  def apply[S <: Sys[S]](dsl: nuages.DSL[S], sCfg: ScissProcs.Config, nCfg: Nuages.Config)
                        (implicit tx: S#Tx, n: Nuages[S]): Unit = {
    import dsl._

    val masterChansOption = nCfg.masterChannels
    val numChannels = if (sCfg.generatorChannels <= 0) masterChansOption.fold(2)(_.size) else sCfg.generatorChannels

    def default(in: Double): ControlValues =
      if (sCfg.generatorChannels <= 0)
        in
      else
        Vector.fill(sCfg.generatorChannels)(in)

    generator("anem-is-20-162") {
      import synth._
      import ugen._
      val _amp   = pAudio("amp", ParamSpec(0.01, 1, ExpWarp), default(0.1))

      // RandSeed.ir(trig = 1, seed = 56789.0)
      val yi              = LFDNoise3.ar(1551.5026 !! numChannels)
      val width           = yi max 0.0
      val in_0            = VarSaw.ar(freq = 100.5704 !! numChannels, iphase = 0.0, width = width)
      val twoPole         = TwoPole.ar(in_0, freq = 55.773136, radius = 1.0)
      val scaleneg        = twoPole scaleneg 0.0014409359
      val in_1            = LeakDC.ar(0.016513553 !! numChannels, coeff = 0.995)
      val oneZero         = OneZero.ar(in_1, coeff = 1.0)
      val mod             = twoPole % oneZero
      val in_2            = LeakDC.ar(0.35691246 !! numChannels, coeff = 0.995)
      val k               = DelayN.ar(in_2, maxDelayTime = 20.0, delayTime = 0.23810405)
      val gbmanL          = GbmanL.ar(freq = 55.773148 !! numChannels, xi = 100.5704, yi = 163.37988)
      val b_0             = LFDNoise3.ar(1551.5026 !! numChannels)
      val a               = 0.016513553 scaleneg b_0
      val decayTime       = LinCongN.ar(freq = gbmanL, a = a, c = 100.5704, m = 4734.57, xi = 100.5704)
      val max_0           = twoPole max 0.0
      val delayTime_0     = max_0 min 20.0
      val delayL          = DelayL.ar(gbmanL, maxDelayTime = 20.0, delayTime = delayTime_0)
      val in_3            = LeakDC.ar(2.3382738 !! numChannels, coeff = 0.995)
      val in_4            = Decay2.ar(in_3, attack = 0.016513553, release = 30.0)
      val in_5            = LeakDC.ar(in_4, coeff = 0.995)
      val delay2          = Delay2.ar(in_5)
      val in_6            = Impulse.ar(freq = 498.64328, phase = 1.0)
      val in_7            = LeakDC.ar(in_4, coeff = 0.995)
      val max_1           = in_6 max 0.0
      val pitchDispersion = max_1 min 1.0
      val max_2           = delayL max 0.0
      val timeDispersion  = max_2 min 2.0
      val pitchShift      = PitchShift.ar(in_7, winSize = 2.0, pitchRatio = 0.0, pitchDispersion = pitchDispersion, timeDispersion = timeDispersion)
      val standardL       = StandardL.ar(freq = 55.773136, k = k, xi = 333.4453, yi = gbmanL)
      val in_8            = LeakDC.ar(-62.88437, coeff = 0.995)
      val max_3           = twoPole max 0.0
      val timeDown        = max_3 min 30.0
      val lag3UD          = Lag3UD.ar(in_8, timeUp = 0.0, timeDown = timeDown)
      val in_9            = LeakDC.ar(-9.2467286E-5, coeff = 0.995)
      val xi_0            = Lag3UD.ar(in_9, timeUp = 30.0, timeDown = 30.0)
      val ring3           = xi_0 ring3 0.23652716
      val max_4           = in_6 max 0.0
      val delayTime_1     = max_4 min 0.23652716
      val combN           = CombN.ar(in_6, maxDelayTime = 0.23652716, delayTime = delayTime_1, decayTime = decayTime)
      val in_10           = LeakDC.ar(-62.88437, coeff = 0.995)
      val max_5           = delay2 max 0.55
      val max_6           = in_6 max 0.0
      val revTime         = max_6 min 100.0
      val max_7           = a max 0.0
      val damping         = max_7 min 1.0
      val max_8           = in_4 max 0.0
      val spread          = max_8 min 43.0
      val roomSize        = max_5 min 300.0
      // val gVerb           = GVerb.ar(in_10, roomSize = roomSize, revTime = revTime, damping = damping, inputBW = 0.0, spread = spread, dryLevel = delay2, earlyRefLevel = 2.5205823E-4, tailLevel = 0.0073382077, maxRoomSize = 300.0)
      val gVerb           = GVerb.ar(Mix.mono(in_10),
        roomSize = roomSize \ 0, revTime = revTime \ 0,
        damping = damping \ 0, inputBW = 0.0, spread = spread \ 0, dryLevel = delay2 \ 0,
        earlyRefLevel = 2.5205823E-4, tailLevel = 0.0073382077, maxRoomSize = 300.0)
      val in_11           = LeakDC.ar(8.832454 !! numChannels, coeff = 0.995)
      val lPZ1            = LPZ1.ar(in_11)
      val max_9           = lPZ1 max 0.01
      val syncFreq        = max_9 min 20000.0
      val max_10          = oneZero max 0.01
      val sawFreq         = max_10 min 20000.0
      val syncSaw         = SyncSaw.ar(syncFreq = syncFreq, sawFreq = sawFreq)
      val lorenzL         = LorenzL.ar(freq = -68.48215, s = -0.88766193, r = 0.0014409359, b = b_0, h = 0.06, xi = 0.23652716, yi = 100.5704, zi = 0.23652716)
      val in_12           = LeakDC.ar(-68.48215 !! numChannels, coeff = 0.995)
      val lPZ2            = LPZ2.ar(in_12)
      val max_11          = a max 0.5
      val b_1             = max_11 min 1.5
      val latoocarfianC   = LatoocarfianC.ar(freq = 2287.8992, a = -9.2467286E-5, b = b_1, c = 0.5, d = delay2, xi = xi_0, yi = yi)
      val mix             = Mix(Seq[GE](scaleneg, mod, pitchShift, standardL, lag3UD, ring3, combN, gVerb, syncSaw, lorenzL, lPZ2, latoocarfianC))
      val in_13           = mix // Mix.Mono(mix)
      val checkBadValues  = CheckBadValues.ar(in_13, id = 0.0, post = 0.0)
      val gate            = checkBadValues sig_== 0.0
      val in_14           = Gate.ar(in_13, gate = gate)
      val pan2            = in_14 // Pan2.ar(in_14, pos = 0.0, level = 1.0)
      // val _trig = Impulse.ar("freq".kr(2))
      // val _pitch = (TIRand.ar(lo = -2, hi = 2, trig = _trig) * 6).midiratio

      // val _karlPitch = PitchShift.ar(in = pan2, pitchRatio = _pitch, pitchDispersion = 0, timeDispersion = 0)
      val sig = pan2 // _karlPitch // pan2 * 0.5 + _karlPitch * 0.5

      // WTF - min, max
      Limiter.ar(LeakDC.ar(sig)).max(-1).min(1) * _amp
    }

    generator("anem-tt-55-dn") {
      import synth._
      import ugen._
      val _amp   = pAudio("amp", ParamSpec(0.01, 1, ExpWarp), default(0.1))

      // RandSeed.ir(trig = 1, seed = 56789.0)
      val in_0            = LeakDC.ar(0.0 !! numChannels, coeff = 0.995)
      val freq_0          = RLPF.ar(in_0, freq = 10.0, rq = 0.014865998)
      val max_0           = freq_0 max 0.01
      val freq_1          = max_0 min 20000.0
      val varSaw_0        = VarSaw.ar(freq = freq_1, iphase = 0.0, width = 0.0)
      val in_2            = LeakDC.ar(0.014865998 !! numChannels, coeff = 0.995)
      val max_1           = freq_0 max 0.0
      val time_0          = max_1 min 30.0
      val freq_3          = Lag.ar(in_2, time = time_0)
      val in_3            = LFDNoise0.ar(freq_3 !! numChannels)
      val in_4            = LeakDC.ar(in_3, coeff = 0.995)
      val lag3_0          = Lag3.ar(in_4, time = 0.0)
      val bPZ2            = BPZ2.ar(lag3_0)
      val min_0           = Constant(0.21584345f) min bPZ2
      val in_5            = LeakDC.ar(988.4579 !! numChannels, coeff = 0.995)
      val max_2           = bPZ2 max 0.0
      val maxDelayTime_0  = max_2 min 20.0
      val max_3           = min_0 max 0.0
      val delayTime_0     = max_3 min maxDelayTime_0
      val combC           = CombC.ar(in_5, maxDelayTime = maxDelayTime_0, delayTime = delayTime_0, decayTime = 1.0)

      val in_10           = LeakDC.ar(0.051471394, coeff = 0.995)
      val max_9           = combC max 0.0
      val time_1          = max_9 min 30.0
      val lag3_1          = Lag3.ar(in_10, time = time_1)
      val min_2           = Constant(-5.20667f) min lag3_1
      val in_11           = LeakDC.ar(8745.995, coeff = 0.995)
      val max_10          = lag3_1 max 0.0
      val maxDelayTime_1  = max_10 min 20.0
      val max_11          = min_2 max 0.0
      val delayTime_1     = max_11 min maxDelayTime_1
      val allpassN        = AllpassN.ar(in_11, maxDelayTime = maxDelayTime_1, delayTime = delayTime_1, decayTime = 0.0047073597)
      val max_12          = allpassN max 0.0
      val iphase_1        = max_12 min 1.0

      // val p4 = "p4".kr(Vector.fill(16)(12.434091))
      val p4 = pAudio("p1", ParamSpec(1, 1000, ExpWarp), default(12.434091))

      val varSaw_1        = VarSaw.ar(freq = p4, iphase = iphase_1, width = 0.28425974)
      val roundUpTo       = varSaw_0 roundUpTo varSaw_1

      val mix = roundUpTo

      val in_21           = mix // Mix.Mono(mix)
      val checkBadValues  = CheckBadValues.ar(in_21, id = 0.0, post = 0.0)
      val gate            = checkBadValues sig_== 0.0
      val in_22           = Gate.ar(in_21, gate = gate)
      val pan2            = in_22 // Pan2.ar(in_22, pos = 0.0, level = 1.0)

      val sig = Limiter.ar(LeakDC.ar(pan2)) * _amp
      sig
    }

    generator("anem-tt-55-dn") {
      import synth._
      import ugen._
      val _amp   = pAudio("amp", ParamSpec(0.01, 1, ExpWarp), default(0.1))

      // RandSeed.ir(trig = 1, seed = 56789.0)
      val b_0             = SyncSaw.ar(syncFreq = 872.9059 !! numChannels, sawFreq = 0.07972072)

      // val p1 = "p1".kr(440.0)
      val p1   = pAudio("freq", ParamSpec(1.0, 2000, ExpWarp), default(440))

      val lFPulse_0       = LFPulse.ar(freq = p1 !! numChannels, iphase = 1.0, width = 1.0)
      val lFPar_0         = LFPar.ar(freq = 0.01 !! numChannels, iphase = 0.0)
      val clip2_0         = lFPar_0 clip2 0.7677357
      val decayTime_0     = lFPulse_0 >= clip2_0
      val mod_0           = 1.0 % lFPulse_0
      val lFPulse_1       = LFPulse.ar(freq = 7.3190875 !! numChannels, iphase = 0.0, width = 0.0)
      val in_0            = LeakDC.ar(mod_0, coeff = 0.995)
      val max_0           = lFPulse_1 max 0.0
      val delayTime_0     = max_0 min 0.0014424388
      val combC_0         = CombC.ar(in_0, maxDelayTime = 0.0014424388, delayTime = delayTime_0, decayTime = 7.531644)
      val in_1            = LeakDC.ar(1.5063983E-5 !! numChannels, coeff = 0.995)
      val max_1           = mod_0 max 0.0
      val delayTime_1     = max_1 min 0.022008082
      val in_2            = AllpassC.ar(in_1, maxDelayTime = 0.022008082, delayTime = delayTime_1, decayTime = 0.0014424388)
      val onePole         = OnePole.ar(in_2, coeff = 0.0023975172)
      val atan2           = 872.9059 atan2 in_2
      val in_3            = LeakDC.ar(-1.89771E-5 !! numChannels, coeff = 0.995)
      val delay1_0        = Delay1.ar(in_3)
      val ring3           = lFPulse_1 ring3 8.916748
      val ring4           = delay1_0 ring4 ring3
      val max_2           = delay1_0 max 0.0
      val width_0         = max_2 min 1.0
      val lFGauss_0       = LFGauss.ar(dur = 5.0E-5, width = width_0, phase = 1.0, loop = 1644.8522, doneAction = doNothing)
      val sumsqr          = lFGauss_0 sumsqr 117.86163
      val a_0             = LFDClipNoise.ar(0.8250135 !! numChannels)
      val fold2           = -697.5932 fold2 lFPar_0
      val bitAnd          = fold2 & 0.018849522
      val in_4            = LeakDC.ar(fold2, coeff = 0.995)
      val b_1             = Delay2.ar(in_4)
      val ring2_0         = fold2 ring2 0.8250135
      val xi_0            = fold2 wrap2 1.0
      val thresh_0        = a_0 thresh fold2
      val in_5            = LeakDC.ar(7.3190875 !! numChannels, coeff = 0.995)
      val max_3           = lFPulse_0 max 0.0
      val delayTime_2     = max_3 min 0.0
      val delayL_0        = DelayL.ar(in_5, maxDelayTime = 0.0, delayTime = delayTime_2)
      val quadN_0         = QuadN.ar(freq = 0.0 !! numChannels, a = 0.0, b = 0.0, c = 0.0, xi = 0.8250135)
      val min             = Constant(0.0f) min quadN_0
      val freq_0          = CuspL.ar(freq = delayL_0, a = lFPar_0, b = min, xi = 1.5063983E-5)
      val xi_1            = LFDNoise1.ar(0.66143084 !! numChannels)
      val neq             = freq_0 sig_!= xi_1
      val linCongL        = LinCongL.ar(freq = freq_0, a = -13.891433, c = 1.5042997E-4, m = 0.080567405, xi = xi_0)
      val rLPF            = RLPF.ar(delayL_0, freq = 10.0, rq = 0.01)
      val max_4           = thresh_0 max 0.01
      val freq_1          = max_4 min 20000.0
      val max_5           = delayL_0 max 0.0
      val iphase_0        = max_5 min 1.0
      val lFPar_1         = LFPar.ar(freq = freq_1, iphase = iphase_0)
      val thresh_1        = 133.04327 thresh delayL_0
      val in_6            = LeakDC.ar(1.0 !! numChannels, coeff = 0.995)
      val lag_0           = Lag.ar(in_6, time = 0.0)
      val freq_2          = SampleRate.ir * 0.5 // Nyquist()
      val freq_3          = LinCongN.ar(freq = freq_2, a = -0.36279276, c = lFPulse_0, m = 20.145914, xi = 0.10357987)
      val sqrdif          = freq_3 sqrdif -2425.7073
      val plus_0          = freq_3 + quadN_0
      val lFDClipNoise    = LFDClipNoise.ar(7.531644 !! numChannels)
      val cuspN_0         = CuspN.ar(freq = freq_3, a = lFDClipNoise, b = lFPulse_0, xi = 0.8250135)
      val in_7            = LeakDC.ar(20.145914 !! numChannels, coeff = 0.995)
      val max_6           = freq_3 max 0.0
      val maxDelayTime_0  = max_6 min 20.0
      val max_7           = lag_0 max 0.0
      val delayTime_3     = max_7 min maxDelayTime_0
      val delayC          = DelayC.ar(in_7, maxDelayTime = maxDelayTime_0, delayTime = delayTime_3)
      val b_2             = LFDClipNoise.ar(-4.619783 !! numChannels)
      val c_0             = QuadN.ar(freq = 0.0, a = lFPar_1, b = b_2, c = 0.8250135, xi = 199.98691)
      val ring2_1         = 1.0 ring2 delayC
      val quadC           = QuadC.ar(freq = 0.0, a = 7.3190875, b = -0.008399427, c = quadN_0, xi = 0.0)
      val in_8            = LinCongC.ar(freq = 0.022283768, a = quadC, c = 743.26575, m = 41.52797, xi = quadC)
      val plus_1          = in_8 + 0.09049736
      val in_9            = LeakDC.ar(in_8, coeff = 0.995)
      val b_3             = BPZ2.ar(in_9)
      val in_10           = LeakDC.ar(7388.521 !! numChannels, coeff = 0.995)
      val max_8           = thresh_0 max 0.0
      val radius          = max_8 min 1.0
      val x0              = TwoZero.ar(in_10, freq = 10.0, radius = radius)
      val henonC          = HenonC.ar(freq = 1605.479, a = 0.008464628, b = b_3, x0 = x0, x1 = 0.015739825)
      val max_9           = henonC max 0.5
      val b_4             = max_9 min 1.5
      val max_10          = quadC max 0.5
      val c_1             = max_10 min 1.5
      val latoocarfianC_0 = LatoocarfianC.ar(freq = ring2_1, a = 0.028330043, b = b_4, c = c_1, d = thresh_0, xi = -0.0025060782, yi = 0.026794823)
      val quadL_0         = QuadL.ar(freq = -1.89771E-5, a = ring2_1, b = 7.107886, c = c_0, xi = 0.0)
      val in_11           = LeakDC.ar(in_8, coeff = 0.995)
      val lag3UD          = Lag3UD.ar(in_11, timeUp = 0.014783192, timeDown = 0.0)
      val max_11          = henonC max 0.0
      val iphase_1        = max_11 min 1.0
      val lFPulse_2       = LFPulse.ar(freq = 0.01, iphase = iphase_1, width = 0.8250135)
      val mod_1           = 1605.479 % lFPulse_1
      val in_12           = LeakDC.ar(min, coeff = 0.995)
      val max_12          = mod_1 max 0.55
      val max_13          = min max 0.0
      val damping_0       = max_13 min 1.0
      val max_14          = lFPar_0 max 0.0
      val spread_0        = max_14 min 43.0
      val roomSize_0      = max_12 min 0.8250135
      val in_13           = GVerb.ar(Mix.mono(in_12),
        roomSize = roomSize_0 \ 0, revTime = 0.0, damping = damping_0 \ 0, inputBW = 0.0,
        spread = spread_0 \ 0, dryLevel = 1.0, earlyRefLevel = 0.8250135,
        tailLevel = -11.958342, maxRoomSize = 0.8250135)
      val delay1_1        = Delay1.ar(in_13)
      val max_15          = delay1_1 max -3.0
      val a_1             = max_15 min 3.0
      val latoocarfianC_1 = LatoocarfianC.ar(freq = 1.5274479E-4, a = a_1, b = 0.5, c = 0.5, d = -2.821052E-4, xi = xi_1, yi = lFPar_1)
      val decayTime_1     = 1.0 ring4 fold2
      val in_14           = LeakDC.ar(0.028330043 !! numChannels, coeff = 0.995)
      val combC_1         = CombC.ar(in_14, maxDelayTime = 0.090563826, delayTime = 0.090563826, decayTime = decayTime_1)
      val in_15           = LeakDC.ar(4542.6772 !! numChannels, coeff = 0.995)
      val max_16          = lFPulse_0 max 0.0
      val timeDown_0      = max_16 min 30.0
      val lag2UD          = Lag2UD.ar(in_15, timeUp = 0.080567405, timeDown = timeDown_0)
      val in_16           = LeakDC.ar(0.008464628 !! numChannels, coeff = 0.995)
      val max_17          = lag2UD max 0.0
      val maxDelayTime_1  = max_17 min 20.0
      val max_18          = cuspN_0 max 0.0
      val delayTime_4     = max_18 min maxDelayTime_1
      val delayL_1        = DelayL.ar(in_16, maxDelayTime = maxDelayTime_1, delayTime = delayTime_4)
      val max_19          = delayL_0 max 0.0
      val iphase_2        = max_19 min 1.0
      val lFPar_2         = LFPar.ar(freq = 0.01, iphase = iphase_2)
      val freq_4          = lFPar_2 amclip quadN_0
      val linCongC_0      = LinCongC.ar(freq = freq_4, a = 0.0, c = 0.8250135, m = 0.015739825, xi = 1.0)
      val in_17           = LeakDC.ar(132.21826 !! numChannels, coeff = 0.995)
      val delay2_0        = Delay2.ar(in_17)
      val in_18           = LeakDC.ar(7.107886 !! numChannels, coeff = 0.995)
      val earlyRefLevel_0 = LPZ2.ar(in_18)
      val gbmanL_0        = GbmanL.ar(freq = 29.838459 !! numChannels, xi = 2.1437016, yi = -1.89771E-5)
      val max_20          = gbmanL_0 max 0.0
      val h_0             = max_20 min 0.06
      val lorenzL         = LorenzL.ar(freq = 1.5063983E-5, s = 7.531644, r = fold2, b = b_0, h = h_0, xi = 2.7886844, yi = thresh_0, zi = quadC)
      val in_19           = LeakDC.ar(lorenzL, coeff = 0.995)
      val lag_1           = Lag.ar(in_19, time = 0.022008082)
      val excess          = earlyRefLevel_0 excess lorenzL
      val in_20           = LeakDC.ar(0.022283768, coeff = 0.995)
      val max_21          = freq_4 max 0.55
      val max_22          = delay2_0 max 0.0
      val revTime_0       = max_22 min 100.0
      val max_23          = lFPar_1 max 0.0
      val damping_1       = max_23 min 1.0
      val roomSize_1      = max_21 min 0.55
      val gVerb_0         = GVerb.ar(Mix.mono(in_20), roomSize = roomSize_1 \ 0, revTime = revTime_0 \ 0,
        damping = damping_1 \ 0, inputBW = 0.0, spread = 43.0, dryLevel = 0.0,
        earlyRefLevel = earlyRefLevel_0 \ 0, tailLevel = -0.008399427, maxRoomSize = 0.55)
      val in_21           = LeakDC.ar(0.007981387, coeff = 0.995)
      val max_24          = atan2 max 0.55
      val roomSize_2      = max_24 min 300.0
      val gVerb_1         = GVerb.ar(Mix.mono(in_21), roomSize = roomSize_2 \ 0,
        revTime = 0.0, damping = 1.0, inputBW = 0.66143084, spread = 43.0,
        dryLevel = lFPar_1 \ 0, earlyRefLevel = 13.28049, tailLevel = 0.5,
        maxRoomSize = 300.0)
      val in_22           = LeakDC.ar(1.5042997E-4, coeff = 0.995)
      val max_25          = ring2_1 max 0.0
      val revTime_1       = max_25 min 100.0
      val spread_1        = a_0 max 0.0
      val max_26          = lag_0 max 0.55
      val maxRoomSize_0   = max_26 min 300.0
      val roomSize_3      = Constant(7.107886f) min maxRoomSize_0
      val a_2             = GVerb.ar(Mix.mono(in_22),
        roomSize = roomSize_3 \ 0, revTime = revTime_1 \ 0, damping = 0.0,
        inputBW = 1.5274479E-4, spread = spread_1 \ 0, dryLevel = quadN_0 \ 0,
        earlyRefLevel = -5452.869, tailLevel = lFPulse_0 \ 0, maxRoomSize = maxRoomSize_0 \ 0)
      val cuspN_1         = CuspN.ar(freq = -0.008399427, a = a_2, b = lorenzL, xi = lFDClipNoise)
      val in_23           = LeakDC.ar(0.0028571805 !! numChannels, coeff = 0.995)
      val delay2_1        = Delay2.ar(in_23)
      val geq             = delay2_1 >= 41.52797
      val freq_5          = -4.619783 absdif plus_0
      val cuspN_2         = CuspN.ar(freq = freq_5, a = a_0, b = lFPar_0, xi = lag_0)
      val linCongC_1      = LinCongC.ar(freq = 20.145914, a = min, c = 0.7677357, m = 1.0, xi = 0.7677357)
      val max_27          = linCongC_1 max 0.0
      val phase_0         = max_27 min 1.0
      val lFGauss_1       = LFGauss.ar(dur = 0.0014424388, width = 0.0, phase = phase_0, loop = 1.5274479E-4, doneAction = doNothing)
      val in_24           = LeakDC.ar(0.8250135 !! numChannels, coeff = 0.995)
      val delay1_2        = Delay1.ar(in_24)
      val clip2_1         = 1605.479 clip2 gbmanL_0
      val amclip          = 0.0 amclip fold2
      val freq_6          = SampleRate.ir * 0.5 // Nyquist()
      val quadL_1         = QuadL.ar(freq = freq_6, a = 0.01589117, b = 0.0, c = 9.145937, xi = 0.8250135)
      val in_25           = LeakDC.ar(-4.619783, coeff = 0.995)
      val allpassL        = AllpassL.ar(in_25, maxDelayTime = 7.3190875, delayTime = 0.2, decayTime = -2425.7073)
      val max_28          = ring3 max 0.0
      val h_1             = max_28 min 0.06
      val in_26           = LorenzL.ar(freq = 0.022283768, s = 9.145937, r = 28.0, b = b_1, h = h_1, xi = min, yi = 7388.521, zi = mod_0)
      val in_27           = LeakDC.ar(in_26, coeff = 0.995)
      val max_29          = x0 max 0.0
      val delayTime_5     = max_29 min 0.015739825
      val combC_2         = CombC.ar(in_27, maxDelayTime = 0.015739825, delayTime = delayTime_5, decayTime = 0.014783192)
      val in_28           = LeakDC.ar(0.0021974712 !! numChannels, coeff = 0.995)
      val delayN          = DelayN.ar(in_28, maxDelayTime = 0.8250135, delayTime = 0.8250135)
      val gbmanL_1        = GbmanL.ar(freq = 0.022008082 !! numChannels, xi = 7.531644, yi = 15.662841)
      val in_29           = LeakDC.ar(gbmanL_1, coeff = 0.995)
      val max_30          = lag_0 max 0.0
      val maxDelayTime_2  = max_30 min 20.0
      val max_31          = linCongC_0 max 0.0
      val delayTime_6     = max_31 min maxDelayTime_2
      val allpassN        = AllpassN.ar(in_29, maxDelayTime = maxDelayTime_2, delayTime = delayTime_6, decayTime = decayTime_0)
      val gbmanN          = GbmanN.ar(freq = gbmanL_1, xi = fold2, yi = 0.026794823)
      val lt              = 370.1297 < in_2
      val in_30           = LeakDC.ar(-93.21562 !! numChannels, coeff = 0.995)
      val combC_3         = CombC.ar(in_30, maxDelayTime = 0.8250135, delayTime = 0.8250135, decayTime = lag_0)
      val in_31           = LeakDC.ar(0.032853525 !! numChannels, coeff = 0.995)
      val max_32          = decayTime_1 max 0.0
      val maxDelayTime_3  = max_32 min 20.0
      val delayTime_7     = Constant(0.25663114f) min maxDelayTime_3
      val combN           = CombN.ar(in_31, maxDelayTime = maxDelayTime_3, delayTime = delayTime_7, decayTime = 0.028330043)
      val in_32           = LeakDC.ar(-0.0028008358 !! numChannels, coeff = 0.995)
      val twoZero         = TwoZero.ar(in_32, freq = 440.0, radius = 0.0)
      val in_33           = LeakDC.ar(-203.25075 !! numChannels, coeff = 0.995)
      val max_33          = lFPulse_1 max 0.0
      val delayTime_8     = max_33 min 0.22664568
      val c_2             = AllpassN.ar(in_33, maxDelayTime = 0.22664568, delayTime = delayTime_8, decayTime = -4.619783)
      val quadN_1         = QuadN.ar(freq = 3773.042, a = 2.1437016, b = 0.0, c = c_2, xi = 0.0034691016)
      val in_34           = LeakDC.ar(0.36781657 !! numChannels, coeff = 0.995)
      val max_34          = fold2 max 10.0
      val freq_7          = max_34 min 20000.0
      val lPF             = LPF.ar(in_34, freq = freq_7)
      val mix             = Mix(Seq[GE](combC_0, onePole, ring4, sumsqr, bitAnd, ring2_0, neq, linCongL, rLPF, thresh_1, sqrdif, plus_1, latoocarfianC_0, quadL_0, lag3UD, lFPulse_2, latoocarfianC_1, combC_1, delayL_1, lag_1, excess, gVerb_0, gVerb_1, cuspN_1, geq, cuspN_2, lFGauss_1, delay1_2, clip2_1, amclip, quadL_1, allpassL, combC_2, delayN, allpassN, gbmanN, lt, combC_3, combN, twoZero, quadN_1, lPF))
      val in_35           = mix // Mix.Mono(mix)
      val checkBadValues  = CheckBadValues.ar(in_35, id = 0.0, post = 0.0)
      val gate            = checkBadValues sig_== 0.0
      val in_36           = Gate.ar(in_35, gate = gate)
      val pan2            = in_36 // Pan2.ar(in_36, pos = 0.0, level = 1.0)
      val sig = pan2
      Limiter.ar(LeakDC.ar(sig)) * _amp
    }

    generator("anem-io-10-282") {
      import synth._
      import ugen._
      val _amp   = pAudio("amp", ParamSpec(0.01, 1, ExpWarp), default(0.1))

      // RandSeed.ir(trig = 1, seed = 56789.0)
      val in_0            = LeakDC.ar(0.015426122 !! numChannels, coeff = 0.995)
      val lPF             = LPF.ar(in_0, freq = 10.0)
      val in_1            = LeakDC.ar(3465.8481 !! numChannels, coeff = 0.995)
      val delayC_0        = DelayC.ar(in_1, maxDelayTime = 0.5899804, delayTime = 0.5899804)
      val standardL       = StandardL.ar(freq = 0.5899804, k = 3465.8481, xi = 57.973328, yi = delayC_0)
      val in_2            = standardL < delayC_0
      val in_3            = LeakDC.ar(-0.008923955 !! numChannels, coeff = 0.995)
      val d               = Ramp.ar(in_3, dur = 0.29398212)
      val max_0           = d max 5.0E-5
      val dur_0           = max_0 min 100.0
      val lFGauss_0       = LFGauss.ar(dur = dur_0, width = 1.0, phase = 0.0, loop = 0.29398212, doneAction = doNothing)
      val in_4            = LeakDC.ar(in_2, coeff = 0.995)
      val max_1           = lFGauss_0 max 0.0
      val delayTime_0     = max_1 min 2.0897863
      val delayL          = DelayL.ar(in_4, maxDelayTime = 2.0897863, delayTime = delayTime_0)
      val yi_0            = lPF thresh standardL
      val in_5            = LeakDC.ar(-159.09827 !! numChannels, coeff = 0.995)
      val max_2           = yi_0 max 10.0
      val freq_0          = max_2 min 20000.0
      val rHPF            = RHPF.ar(in_5, freq = freq_0, rq = 0.01)
      val neq_0           = -3420.6182 sig_!= rHPF
      val in_6            = LeakDC.ar(0.0050909123 !! numChannels, coeff = 0.995)
      val lag3_0          = Lag3.ar(in_6, time = 0.0)
      val in_7            = LeakDC.ar(57.973328 !! numChannels, coeff = 0.995)
      val max_3           = lag3_0 max 10.0
      val freq_1          = max_3 min 20000.0
      val freq_2          = BRF.ar(in_7, freq = freq_1, rq = 11.636825)
      val latoocarfianN_0 = LatoocarfianN.ar(freq = freq_2, a = -3.0, b = 1.5, c = 0.5, d = 75.46957, xi = 0.0054337247, yi = -100.77602)
      val in_8            = LinCongL.ar(freq = 0.004350639 !! numChannels, a = 0.015426122, c = 0.02181583, m = 1.0, xi = 0.004350639)
      val in_9            = LeakDC.ar(in_8, coeff = 0.995)
      val lag3_1          = Lag3.ar(in_9, time = 0.1)
      val in_10           = LFPulse.ar(freq = 1719.5327 !! numChannels, iphase = 1.0, width = 0.0)
      val xi_0            = LPZ2.ar(in_10)
      val lPZ2_0          = LPZ2.ar(in_10)
      val in_11           = LeakDC.ar(2.0344253 !! numChannels, coeff = 0.995)
      val max_4           = in_8 max 0.0
      val maxDelayTime_0  = max_4 min 20.0
      val max_5           = lPZ2_0 max 0.0
      val delayTime_1     = max_5 min maxDelayTime_0
      val freq_3          = CombN.ar(in_11, maxDelayTime = maxDelayTime_0, delayTime = delayTime_1, decayTime = -0.08117196)
      val s_0             = GbmanN.ar(freq = 246.10304 !! numChannels, xi = 57.973328, yi = 53.917908)
      val freq_4          = SampleRate.ir * 0.5 // Nyquist()
      val yi_1            = GbmanN.ar(freq = freq_4, xi = 0.0050909123, yi = yi_0)
      val in_12           = 0.5899804 trunc yi_0
      val in_13           = LeakDC.ar(-3179.6772 !! numChannels, coeff = 0.995)
      val hPZ2            = HPZ2.ar(in_13)
      val max_6           = in_12 max 5.0E-5
      val dur_1           = max_6 min 100.0
      val max_7           = hPZ2 max 0.0
      val width_0         = max_7 min 1.0
      val lFGauss_1       = LFGauss.ar(dur = dur_1, width = width_0, phase = 0.0, loop = 1.5449197, doneAction = doNothing)
      val in_14           = LeakDC.ar(in_12, coeff = 0.995)
      val lag2_0          = Lag2.ar(in_14, time = 0.0)
      val b_0             = yi_1 hypot in_12
      val zi              = b_0 sqrdif 57.973328
      val lorenzL_0       = LorenzL.ar(freq = freq_3, s = s_0, r = 1800.9755, b = 1.2012068E7, h = 0.0, xi = xi_0, yi = -0.025459621, zi = zi)
      val max_8           = s_0 max -3.0
      val a_0             = max_8 min 3.0
      val max_9           = freq_3 max 0.5
      val b_1             = max_9 min 1.5
      val max_10          = in_8 max 0.5
      val c_0             = max_10 min 1.5
      val latoocarfianN_1 = LatoocarfianN.ar(freq = 8.858717, a = a_0, b = b_1, c = c_0, d = d, xi = 246.10304, yi = 12.308682)
      val x1_0            = -100.77602 >= latoocarfianN_1
      val lFDClipNoise_0  = LFDClipNoise.ar(0.0050909123 !! numChannels)
      val in_15           = LeakDC.ar(0.015426122 !! numChannels, coeff = 0.995)
      val bPZ2            = BPZ2.ar(in_15)
      val eq              = bPZ2 sig_== -0.0
      val max_11          = yi_0 max 0.0
      val width_1         = max_11 min 1.0
      val a_1             = LFPulse.ar(freq = 0.114574425, iphase = 1.0, width = width_1)
      val in_16           = LeakDC.ar(0.0060517536 !! numChannels, coeff = 0.995)
      val in_17           = Delay2.ar(in_16)
      val in_18           = LeakDC.ar(3465.8481 !! numChannels, coeff = 0.995)
      val delay2          = Delay2.ar(in_18)
      val lPZ1_0          = LPZ1.ar(delay2)
      val max_12          = lPZ1_0 max 0.0
      val maxDelayTime_1  = max_12 min 20.0
      val delayTime_2     = Constant(0.29398212f) min maxDelayTime_1
      val x0_0            = DelayN.ar(in_17, maxDelayTime = maxDelayTime_1, delayTime = delayTime_2)
      val max_13          = a_1 max 0.01
      val freq_5          = max_13 min 20000.0
      val max_14          = in_17 max 0.0
      val iphase_0        = max_14 min 1.0
      val lFCub_0         = LFCub.ar(freq = freq_5, iphase = iphase_0)
      val lFDNoise0       = LFDNoise0.ar(0.03109021 !! numChannels)
      val roundUpTo       = lFDNoise0 roundUpTo 0.015426122
      val lFDClipNoise_1  = LFDClipNoise.ar(1.2012068E7 !! numChannels)
      val in_19           = LeakDC.ar(1.2012068E7 !! numChannels, coeff = 0.995)
      val oneZero         = OneZero.ar(in_19, coeff = 0.02181583)
      val ring1           = delay2 ring1 bPZ2
      val in_20           = CuspN.ar(freq = delay2, a = 0.009065811, b = 5.521476E-4, xi = 61.769085)
      val in_21           = LeakDC.ar(1.0 !! numChannels, coeff = 0.995)
      val max_15          = in_8 max 0.01
      val rq_0            = max_15 min 100.0
      val freq_6          = BRF.ar(in_21, freq = 10.0, rq = rq_0)
      val latoocarfianN_2 = LatoocarfianN.ar(freq = freq_6, a = 6.231019E-4, b = 1.5, c = 0.5, d = 0.0012962511, xi = 52.966427, yi = 32.840443)
      val in_22           = LeakDC.ar(in_20, coeff = 0.995)
      val max_16          = freq_6 max 0.0
      val maxDelayTime_2  = max_16 min 20.0
      val max_17          = yi_1 max 0.0
      val delayTime_3     = max_17 min maxDelayTime_2
      val delayC_1        = DelayC.ar(in_22, maxDelayTime = maxDelayTime_2, delayTime = delayTime_3)
      val lFSaw           = LFSaw.ar(freq = 440.0 !! numChannels, iphase = 0.02181583)
      val in_23           = LeakDC.ar(2555.6104 !! numChannels, coeff = 0.995)
      val max_18          = lFSaw max 0.01
      val rq_1            = max_18 min 100.0
      val bRF             = BRF.ar(in_23, freq = 10.0, rq = rq_1)
      val decayTime_0     = 2555.6104 ring4 lag3_0
      val in_24           = LeakDC.ar(1.2012155E7 !! numChannels, coeff = 0.995)
      val allpassN_0      = AllpassN.ar(in_24, maxDelayTime = 0.2, delayTime = 0.009065811, decayTime = decayTime_0)
      val in_25           = LeakDC.ar(0.03109021 !! numChannels, coeff = 0.995)
      val lPZ2_1          = LPZ2.ar(in_25)
      val x1_1            = -607.0059 clip2 lFDClipNoise_0
      val in_26           = LeakDC.ar(0.02181583 !! numChannels, coeff = 0.995)
      val lPZ1_1          = LPZ1.ar(in_26)
      val hypot           = -494.09155 hypot delayC_0
      val quadL           = QuadL.ar(freq = -0.008923955 !! numChannels, a = 182.5478, b = 0.02323959, c = 3065.6057, xi = 0.03109021)
      val mod             = quadL % decayTime_0
      val henonN          = HenonN.ar(freq = 1.1700629E7, a = a_1, b = 75.46957, x0 = -96.037476, x1 = x1_0)
      val in_27           = LeakDC.ar(35.001827 !! numChannels, coeff = 0.995)
      val onePole         = OnePole.ar(in_27, coeff = 0.999)
      val ring3           = 0.047778364 ring3 latoocarfianN_2
      val henonC_0        = HenonC.ar(freq = 1115.6718, a = 0.047778364, b = delayC_0, x0 = 0.02323959, x1 = 246.10304)
      val min             = Constant(0.29398212f) min in_2
      val neq_1           = 1.1033629 sig_!= lPZ1_0
      val in_28           = LeakDC.ar(2555.6104 !! numChannels, coeff = 0.995)
      val max_19          = in_8 max 0.0
      val maxDelayTime_3  = max_19 min 20.0
      val delayTime_4     = Constant(0.0f) min maxDelayTime_3
      val combC           = CombC.ar(in_28, maxDelayTime = maxDelayTime_3, delayTime = delayTime_4, decayTime = 57.973328)
      val in_29           = LeakDC.ar(0.015426122 !! numChannels, coeff = 0.995)
      val x0_1            = LPF.ar(in_29, freq = 10.0)
      val henonC_1        = HenonC.ar(freq = 456.53043, a = -0.501102, b = 1719.5327, x0 = x0_1, x1 = 0.08747408)
      val gbmanN          = GbmanN.ar(freq = 456.53043 !! numChannels, xi = 0.29398212, yi = 1814.6665)
      val in_30           = LeakDC.ar(958.20404 !! numChannels, coeff = 0.995)
      val decay2          = Decay2.ar(in_30, attack = 0.015426122, release = 30.0)
      val difsqr          = 0.023828123 difsqr decay2
      val in_31           = LeakDC.ar(2555.6104 !! numChannels, coeff = 0.995)
      val rLPF            = RLPF.ar(in_31, freq = 440.0, rq = 0.023828123)
      val max_20          = combC max 0.0
      val iphase_1        = max_20 min 1.0
      val lFCub_1         = LFCub.ar(freq = 0.01, iphase = iphase_1)
      val max_21          = min max -3.0
      val a_2             = max_21 min 3.0
      val max_22          = lFGauss_0 max 0.5
      val b_2             = max_22 min 1.5
      val latoocarfianC   = LatoocarfianC.ar(freq = 0.15577696, a = a_2, b = b_2, c = 1.5, d = 1814.6665, xi = 12.308682, yi = 149.4075)
      val henonL          = HenonL.ar(freq = 11.636825, a = 0.15577696, b = 4449.062, x0 = x0_0, x1 = x1_1)
      val s_1             = Constant(1532.9478f) max min
      val max_23          = henonC_0 max 0.0
      val iphase_2        = max_23 min 1.0
      val varSaw          = VarSaw.ar(freq = 1532.9478, iphase = iphase_2, width = 0.004350639)
      val in_32           = LeakDC.ar(958.20404 !! numChannels, coeff = 0.995)
      val lag2_1          = Lag2.ar(in_32, time = 0.1)
      val in_33           = LeakDC.ar(0.0043667015 !! numChannels, coeff = 0.995)
      val combL           = CombL.ar(in_33, maxDelayTime = 20.0, delayTime = 0.0, decayTime = 8.858717)
      val syncSaw         = SyncSaw.ar(syncFreq = 3.9252315 !! numChannels, sawFreq = 3.1978712)
      val in_34           = LeakDC.ar(0.006367362 !! numChannels, coeff = 0.995)
      val max_24          = rLPF max 0.0
      val delayTime_5     = max_24 min 0.02646789
      val allpassN_1      = AllpassN.ar(in_34, maxDelayTime = 0.02646789, delayTime = delayTime_5, decayTime = 149.4075)
      val amclip          = allpassN_1 amclip in_2
      val lorenzL_1       = LorenzL.ar(freq = 47.990856, s = s_1, r = -159.09827, b = b_0, h = 0.06, xi = 0.009065811, yi = yi_1, zi = 1800.9755)
      val sumsqr          = 3624.9285 sumsqr lFGauss_0
      val max_25          = x0_1 max 0.0
      val iphase_3        = max_25 min 4.0
      val lFTri           = LFTri.ar(freq = 440.0, iphase = iphase_3)
      val mix             = Mix(Seq[GE](delayL, neq_0, latoocarfianN_0, lag3_1, lFGauss_1, lag2_0, lorenzL_0, eq, lFCub_0, roundUpTo, lFDClipNoise_1, oneZero, ring1, delayC_1, bRF, allpassN_0, lPZ2_1, lPZ1_1, hypot, mod, henonN, onePole, ring3, neq_1, henonC_1, gbmanN, difsqr, lFCub_1, latoocarfianC, henonL, varSaw, lag2_1, combL, syncSaw, amclip, lorenzL_1, sumsqr, lFTri))
      val in_35           = mix // Mix.Mono(mix)
      val checkBadValues  = CheckBadValues.ar(in_35, id = 0.0, post = 0.0)
      val gate            = checkBadValues sig_== 0.0
      val in_36           = Gate.ar(in_35, gate = gate)
      val pan2            = in_36 // Pan2.ar(in_36, pos = 0.0, level = 1.0)
      val sig = pan2 // Resonz.ar(pan2, "freq".kr(777), rq = 1)
      Limiter.ar(LeakDC.ar(sig)) * _amp
    }

    generator("anem-io-10-423") {
      import synth._
      import ugen._
      val _amp   = pAudio("amp", ParamSpec(0.01, 1, ExpWarp), default(0.1))

      // RandSeed.ir(trig = 1, seed = 56789.0)
      val syncSaw_0       = SyncSaw.ar(syncFreq = 0.01 !! numChannels, sawFreq = 0.01)
      val eq              = syncSaw_0 sig_== 0.001002046
      val tailLevel       = eq atan2 1.4401373
      val latoocarfianN   = LatoocarfianN.ar(freq = 0.41197228 !! numChannels, a = 3.0, b = 0.5, c = 0.5, d = -0.008222404, xi = 662.35516, yi = 3721.9795)
      val in_0            = 0.008054061 sumsqr latoocarfianN
      val in_1            = LeakDC.ar(in_0, coeff = 0.995)
      val freq_0          = HPZ1.ar(in_1)
      val lFDNoise3       = LFDNoise3.ar(freq_0)
      val syncSaw_1       = SyncSaw.ar(syncFreq = 0.01, sawFreq = 0.01)
      val clip2           = syncSaw_1 clip2 1.892141
      val lFPar_0         = LFPar.ar(freq = 5.212914 !! numChannels, iphase = 1.0)
      val in_2            = LeakDC.ar(0.01761304 !! numChannels, coeff = 0.995)
      val max_0           = lFPar_0 max 0.0
      val time_0          = max_0 min 30.0
      val lag2_0          = Lag2.ar(in_2, time = time_0)
      val max_1           = lag2_0 max 0.1
      val freq_1          = max_1 min 20000.0
      val impulse_0       = Impulse.ar(freq = freq_1, phase = 0.0010278672)
      val in_3            = LeakDC.ar(0.27556488 !! numChannels, coeff = 0.995)
      val max_2           = impulse_0 max 0.0
      val delayTime_0     = max_2 min 0.2
      val combC           = CombC.ar(in_3, maxDelayTime = 0.2, delayTime = delayTime_0, decayTime = -325.8913)
      val max_3           = syncSaw_1 max 0.0
      val iphase_0        = max_3 min 1.0
      val lFPar_1         = LFPar.ar(freq = 4.814985, iphase = iphase_0)
      val sqrdif          = lFPar_1 sqrdif combC
      val in_4            = SyncSaw.ar(syncFreq = 0.01 !! numChannels, sawFreq = 440.0)
      val a_0             = Lag3.ar(in_4, time = 0.32421353)
      val in_5            = LeakDC.ar(0.42644614 !! numChannels, coeff = 0.995)
      val max_4           = sqrdif max 0.0
      val delayTime_1     = max_4 min 0.011486273
      val allpassC_0      = AllpassC.ar(in_5, maxDelayTime = 0.011486273, delayTime = delayTime_1, decayTime = 0.059060287)
      val max_5           = a_0 max 0.01
      val freq_2          = max_5 min 20000.0
      val max_6           = allpassC_0 max 0.0
      val iphase_1        = max_6 min 1.0
      val in_6            = LFCub.ar(freq = freq_2, iphase = iphase_1)
      val in_7            = LeakDC.ar(in_6, coeff = 0.995)
      val hPZ1            = HPZ1.ar(in_7)
      val gbmanL          = GbmanL.ar(freq = 0.42644614 !! numChannels, xi = 3304.8223, yi = 0.0)
      val in_8            = LeakDC.ar(0.017735861 !! numChannels, coeff = 0.995)
      val max_7           = gbmanL max 0.0
      val time_1          = max_7 min 30.0
      val lag2_1          = Lag2.ar(in_8, time = time_1)
      val b_0             = lag2_1 sqrsum -0.0014914646
      val decayTime_0     = 0.00265905 ring3 hPZ1
      val max_8           = b_0 max 0.1
      val freq_3          = max_8 min 20000.0
      val max_9           = decayTime_0 max 0.0
      val phase_0         = max_9 min 1.0
      val impulse_1       = Impulse.ar(freq = freq_3, phase = phase_0)
      val in_9            = LeakDC.ar(0.4886342 !! numChannels, coeff = 0.995)
      val max_10          = impulse_1 max 0.55
      val max_11          = freq_0 max 0.0
      val revTime         = max_11 min 100.0
      val max_12          = clip2 max 0.0
      val damping_0       = max_12 min 1.0
      val roomSize        = max_10 min 38.01318
      val in_10           = GVerb.ar(Mix.mono(in_9), roomSize = roomSize \ 0, revTime = revTime \ 0,
        damping = damping_0 \ 0, inputBW = 0.007950359, spread = 43.0,
        dryLevel = -0.010030854, earlyRefLevel = 0.0062145633,
        tailLevel = tailLevel \ 0, maxRoomSize = 38.01318)
      val bitXor          = in_10 ^ -0.008222404
      val hypot           = lFPar_0 hypot in_10
      val in_11           = LeakDC.ar(0.017735861 !! numChannels, coeff = 0.995)
      val lag2_2          = Lag2.ar(in_11, time = 0.63454044)
      val in_12           = LeakDC.ar(7.144484 !! numChannels, coeff = 0.995)
      val bRF_0           = BRF.ar(in_12, freq = 10.0, rq = 0.27556488)
      val ring2           = 7.382483 ring2 clip2
      val lFTri           = LFTri.ar(freq = 0.63454044 !! numChannels, iphase = 0.0)
      val max_13          = in_4 max 0.01
      val sawFreq_0       = max_13 min 20000.0
      val syncSaw_2       = SyncSaw.ar(syncFreq = 0.01, sawFreq = sawFreq_0)
      val decayTime_1     = 0.0062145633 ring3 decayTime_0
      val allpassC_1      = AllpassC.ar(in_10, maxDelayTime = 0.36365655, delayTime = 0.0062145633, decayTime = decayTime_1)
      val in_13           = LeakDC.ar(0.27556488 !! numChannels, coeff = 0.995)
      val max_14          = a_0 max 0.0
      val maxDelayTime_0  = max_14 min 20.0
      val delayTime_2     = Constant(27.391266f) min maxDelayTime_0
      val allpassL        = AllpassL.ar(in_13, maxDelayTime = maxDelayTime_0, delayTime = delayTime_2, decayTime = decayTime_0)
      val in_14           = LeakDC.ar(18.501421 !! numChannels, coeff = 0.995)
      val hPF             = HPF.ar(in_14, freq = 10.0)
      val in_15           = LeakDC.ar(0.056376386 !! numChannels, coeff = 0.995)
      val bPZ2_0          = BPZ2.ar(in_15)
      val quadL           = QuadL.ar(freq = 39.348953, a = a_0, b = b_0, c = 0.27556488, xi = -10.100334)
      val roundUpTo       = 0.0015211575 roundUpTo hPF
      val in_16           = LeakDC.ar(0.059060287 !! numChannels, coeff = 0.995)
      val bPZ2_1          = BPZ2.ar(in_16)
      val max_15          = a_0 max 0.0
      val iphase_2        = max_15 min 1.0
      val varSaw          = VarSaw.ar(freq = 0.01, iphase = iphase_2, width = 1.0)
      val mod             = 0.32421353 % varSaw
      val bitOr           = 0.63454044 | impulse_0
      val in_17           = LeakDC.ar(3739.0295 !! numChannels, coeff = 0.995)
      val max_16          = syncSaw_0 max 0.0
      val maxDelayTime_1  = max_16 min 20.0
      val delayTime_3     = Constant(0.3213919f) min maxDelayTime_1
      val delayL          = DelayL.ar(in_17, maxDelayTime = maxDelayTime_1, delayTime = delayTime_3)
      val scaleneg        = 0.0045420905 scaleneg in_0
      val in_18           = SyncSaw.ar(syncFreq = 1.892141 !! numChannels, sawFreq = 0.41197228)
      val in_19           = LeakDC.ar(in_18, coeff = 0.995)
      val max_17          = bitXor max 0.0
      val damping_1       = max_17 min 1.0
      val max_18          = hypot max 0.0
      val inputBW_0       = max_18 min 1.0
      val gVerb           = GVerb.ar(Mix.mono(in_19), roomSize = 0.55, revTime = 0.63454044,
        damping = damping_1 \ 0, inputBW = inputBW_0 \ 0, spread = 0.011486273,
        dryLevel = 0.056376386, earlyRefLevel = -0.0014914646, tailLevel = 0.42644614, maxRoomSize = 0.55)
      val hypotx          = 3721.9795 hypotx freq_0
      val in_20           = LeakDC.ar(0.4886342 !! numChannels, coeff = 0.995)
      val lag2_3          = Lag2.ar(in_20, time = 30.0)
      val lt              = 5.212914 < hPF
      val in_21           = LeakDC.ar(-0.0014914646 !! numChannels, coeff = 0.995)
      val twoPole         = TwoPole.ar(in_21, freq = 6079.9946, radius = 1.0)
      val in_22           = LeakDC.ar(0.63454044 !! numChannels, coeff = 0.995)
      val bRF_1           = BRF.ar(in_22, freq = 10.0, rq = 100.0)
      val in_23           = LeakDC.ar(-0.0014914646 !! numChannels, coeff = 0.995)
      val lag             = Lag.ar(in_23, time = 0.63454044)
      val mix             = Mix(Seq[GE](
        lFDNoise3, lag2_2, bRF_0, ring2, lFTri, syncSaw_2, allpassC_1, allpassL, bPZ2_0, quadL, roundUpTo, bPZ2_1, mod, bitOr, delayL, scaleneg, gVerb, hypotx, lag2_3, lt, twoPole, bRF_1, lag))
      val in_24           = mix // Mix.Mono(mix)
      val checkBadValues  = CheckBadValues.ar(in_24, id = 0.0, post = 0.0)
      val gate            = checkBadValues sig_== 0.0
      val in_25           = Gate.ar(in_24, gate = gate)
      val pan2            = in_25 // Pan2.ar(in_25, pos = 0.0, level = 1.0)
      val sig = pan2 // Resonz.ar(pan2, "freq".kr(777), rq = 1)
      Limiter.ar(LeakDC.ar(sig)) * _amp
    }

    generator("anem-io-10-88") {
      import synth._
      import ugen._
      val _amp   = pAudio("amp", ParamSpec(0.01, 1, ExpWarp), default(0.1))

      val lFPar_0         = LFPar.ar(freq = 0.01 !! numChannels, iphase = 0.0)
      val clip2_0         = lFPar_0 clip2 0.7677357
      val lFPulse_0       = LFPulse.ar(freq = 0.07972072 !! numChannels, iphase = 1.0, width = 0.0)
      val decayTime       = lFPulse_0 >= clip2_0
      val in_4            = LeakDC.ar(7.3190875 !! numChannels, coeff = 0.995)
      val max_3           = lFPulse_0 max 0.0
      val delayTime_2     = max_3 min 0.0
      val delayL_0        = DelayL.ar(in_4, maxDelayTime = 0.0, delayTime = delayTime_2)
      val quadN_0         = QuadN.ar(freq = 0.0 !! numChannels, a = 0.0, b = 0.0, c = 0.0, xi = 0.8250135)
      val in_6            = LeakDC.ar(1.0 !! numChannels, coeff = 0.995)
      val lag_0           = Lag.ar(in_6, time = 0.0)
      val freq_3          = SampleRate.ir * 0.5 // Nyquist()
      val linCongN        = LinCongN.ar(freq = freq_3, a = -0.36279276, c = lFPulse_0, m = 20.145914, xi = 0.10357987)
      val sqrdif          = linCongN sqrdif -2425.7073
      val max_11          = delayL_0 max 0.0
      val iphase_1        = max_11 min 1.0
      val b_0             = LFPar.ar(freq = 0.01, iphase = iphase_1)
      val freq_5          = b_0 amclip quadN_0
      val linCongC_0      = LinCongC.ar(freq = freq_5, a = 0.0, c = 0.8250135, m = 0.015739825, xi = 1.0)
      val gbmanL_1        = GbmanL.ar(freq = 152.6102 !! numChannels, xi = 7.531644, yi = 15.662841)
      val max_13          = lag_0 max 0.0
      val maxDelayTime_2  = max_13 min 20.0
      val max_14          = linCongC_0 max 0.0
      val delayTime_6     = max_14 min maxDelayTime_2
      val allpassN_1      = AllpassN.ar(gbmanL_1, maxDelayTime = maxDelayTime_2, delayTime = delayTime_6, decayTime = decayTime)
      val gbmanN          = GbmanN.ar(freq = gbmanL_1, xi = -419.27997, yi = -0.42427403)
      val gbmanL_2        = GbmanL.ar(freq = 29.838459 !! numChannels, xi = 2.1437016, yi = -1.89771E-5)
      val clip2_1         = 1605.479 clip2 gbmanL_2

      val mix             = Mix(Seq[GE](sqrdif, allpassN_1, gbmanN, clip2_1))
      val in_35           = mix // Mix.Mono(mix)
      val checkBadValues  = CheckBadValues.ar(in_35, id = 0.0, post = 0.0)
      val gate            = checkBadValues sig_== 0.0
      val in_36           = Gate.ar(in_35, gate = gate)
      val pan2            = in_36 // Pan2.ar(in_36, pos = 0.0, level = 1.0)
      val sig = pan2
      Limiter.ar(LeakDC.ar(sig)) * _amp
    }

    generator("anem-mbz-90-385") {
      import synth._
      import ugen._
      val _amp    = pAudio("amp"   , ParamSpec( 0.01,     1   , ExpWarp), default(   0.1  ))
      val f0      = pAudio("freq"  , ParamSpec( 1.0 , 1000.0  , ExpWarp), default(  56.57 ))
      val beat    = pAudio("beat"  , ParamSpec( 0.01,   10.0  , ExpWarp), default(   1.2  ))
      val w0      = pAudio("width1", ParamSpec( 0.001,   0.999, ExpWarp), default(   0.093))
      val w1      = pAudio("width2", ParamSpec( 0.001,   0.999, ExpWarp), default(   0.028))
      val t0      = pAudio("thresh", ParamSpec(-0.2,    +0.2           ), default(  -0.016))

      val f1   = f0 * 2 + beat // "freq2".kr(115.54486 /)

      val pulse_1         = Pulse.ar(freq = f0, width = w0)
      val geq_0           = pulse_1 >= t0
      val pulse_2         = Pulse.ar(freq = f1, width = w1)
      val clip2           = f1 clip2 pulse_2
      val mix             = geq_0 + clip2
      val pan2            = Pan2.ar(mix, pos = 0.0, level = 1.0)
      LeakDC.ar(pan2) * _amp
    }
  }
}