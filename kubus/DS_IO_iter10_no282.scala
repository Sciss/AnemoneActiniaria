val x = play {
  RandSeed.ir(trig = 1, seed = 56789.0)
  val in_0            = LeakDC.ar(0.015426122, coeff = 0.995)
  val lPF             = LPF.ar(in_0, freq = 10.0)
  val in_1            = LeakDC.ar(3465.8481, coeff = 0.995)
  val delayC_0        = DelayC.ar(in_1, maxDelayTime = 0.5899804, delayTime = 0.5899804)
  val standardL       = StandardL.ar(freq = 0.5899804, k = 3465.8481, xi = 57.973328, yi = delayC_0)
  val in_2            = standardL < delayC_0
  val in_3            = LeakDC.ar(-0.008923955, coeff = 0.995)
  val d               = Ramp.ar(in_3, dur = 0.29398212)
  val max_0           = d max 5.0E-5
  val dur_0           = max_0 min 100.0
  val lFGauss_0       = LFGauss.ar(dur = dur_0, width = 1.0, phase = 0.0, loop = 0.29398212, doneAction = doNothing)
  val in_4            = LeakDC.ar(in_2, coeff = 0.995)
  val max_1           = lFGauss_0 max 0.0
  val delayTime_0     = max_1 min 2.0897863
  val delayL          = DelayL.ar(in_4, maxDelayTime = 2.0897863, delayTime = delayTime_0)
  val yi_0            = lPF thresh standardL
  val in_5            = LeakDC.ar(-159.09827, coeff = 0.995)
  val max_2           = yi_0 max 10.0
  val freq_0          = max_2 min 20000.0
  val rHPF            = RHPF.ar(in_5, freq = freq_0, rq = 0.01)
  val neq_0           = -3420.6182 sig_!= rHPF
  val in_6            = LeakDC.ar(0.0050909123, coeff = 0.995)
  val lag3_0          = Lag3.ar(in_6, time = 0.0)
  val in_7            = LeakDC.ar(57.973328, coeff = 0.995)
  val max_3           = lag3_0 max 10.0
  val freq_1          = max_3 min 20000.0
  val freq_2          = BRF.ar(in_7, freq = freq_1, rq = 11.636825)
  val latoocarfianN_0 = LatoocarfianN.ar(freq = freq_2, a = -3.0, b = 1.5, c = 0.5, d = 75.46957, xi = 0.0054337247, yi = -100.77602)
  val in_8            = LinCongL.ar(freq = 0.004350639, a = 0.015426122, c = 0.02181583, m = 1.0, xi = 0.004350639)
  val in_9            = LeakDC.ar(in_8, coeff = 0.995)
  val lag3_1          = Lag3.ar(in_9, time = 0.1)
  val in_10           = LFPulse.ar(freq = 1719.5327, iphase = 1.0, width = 0.0)
  val xi_0            = LPZ2.ar(in_10)
  val lPZ2_0          = LPZ2.ar(in_10)
  val in_11           = LeakDC.ar(2.0344253, coeff = 0.995)
  val max_4           = in_8 max 0.0
  val maxDelayTime_0  = max_4 min 20.0
  val max_5           = lPZ2_0 max 0.0
  val delayTime_1     = max_5 min maxDelayTime_0
  val freq_3          = CombN.ar(in_11, maxDelayTime = maxDelayTime_0, delayTime = delayTime_1, decayTime = -0.08117196)
  val s_0             = GbmanN.ar(freq = 246.10304, xi = 57.973328, yi = 53.917908)
  val freq_4          = Nyquist()
  val yi_1            = GbmanN.ar(freq = freq_4, xi = 0.0050909123, yi = yi_0)
  val in_12           = 0.5899804 trunc yi_0
  val in_13           = LeakDC.ar(-3179.6772, coeff = 0.995)
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
  val lFDClipNoise_0  = LFDClipNoise.ar(0.0050909123)
  val in_15           = LeakDC.ar(0.015426122, coeff = 0.995)
  val bPZ2            = BPZ2.ar(in_15)
  val eq              = bPZ2 sig_== -0.0
  val max_11          = yi_0 max 0.0
  val width_1         = max_11 min 1.0
  val a_1             = LFPulse.ar(freq = 0.114574425, iphase = 1.0, width = width_1)
  val in_16           = LeakDC.ar(0.0060517536, coeff = 0.995)
  val in_17           = Delay2.ar(in_16)
  val in_18           = LeakDC.ar(3465.8481, coeff = 0.995)
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
  val lFDNoise0       = LFDNoise0.ar(0.03109021)
  val roundUpTo       = lFDNoise0 roundUpTo 0.015426122
  val lFDClipNoise_1  = LFDClipNoise.ar(1.2012068E7)
  val in_19           = LeakDC.ar(1.2012068E7, coeff = 0.995)
  val oneZero         = OneZero.ar(in_19, coeff = 0.02181583)
  val ring1           = delay2 ring1 bPZ2
  val in_20           = CuspN.ar(freq = delay2, a = 0.009065811, b = 5.521476E-4, xi = 61.769085)
  val in_21           = LeakDC.ar(1.0, coeff = 0.995)
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
  val lFSaw           = LFSaw.ar(freq = 440.0, iphase = 0.02181583)
  val in_23           = LeakDC.ar(2555.6104, coeff = 0.995)
  val max_18          = lFSaw max 0.01
  val rq_1            = max_18 min 100.0
  val bRF             = BRF.ar(in_23, freq = 10.0, rq = rq_1)
  val decayTime_0     = 2555.6104 ring4 lag3_0
  val in_24           = LeakDC.ar(1.2012155E7, coeff = 0.995)
  val allpassN_0      = AllpassN.ar(in_24, maxDelayTime = 0.2, delayTime = 0.009065811, decayTime = decayTime_0)
  val in_25           = LeakDC.ar(0.03109021, coeff = 0.995)
  val lPZ2_1          = LPZ2.ar(in_25)
  val x1_1            = -607.0059 clip2 lFDClipNoise_0
  val in_26           = LeakDC.ar(0.02181583, coeff = 0.995)
  val lPZ1_1          = LPZ1.ar(in_26)
  val hypot           = -494.09155 hypot delayC_0
  val quadL           = QuadL.ar(freq = -0.008923955, a = 182.5478, b = 0.02323959, c = 3065.6057, xi = 0.03109021)
  val mod             = quadL % decayTime_0
  val henonN          = HenonN.ar(freq = 1.1700629E7, a = a_1, b = 75.46957, x0 = -96.037476, x1 = x1_0)
  val in_27           = LeakDC.ar(35.001827, coeff = 0.995)
  val onePole         = OnePole.ar(in_27, coeff = 0.999)
  val ring3           = 0.047778364 ring3 latoocarfianN_2
  val henonC_0        = HenonC.ar(freq = 1115.6718, a = 0.047778364, b = delayC_0, x0 = 0.02323959, x1 = 246.10304)
  val min             = Constant(0.29398212f) min in_2
  val neq_1           = 1.1033629 sig_!= lPZ1_0
  val in_28           = LeakDC.ar(2555.6104, coeff = 0.995)
  val max_19          = in_8 max 0.0
  val maxDelayTime_3  = max_19 min 20.0
  val delayTime_4     = Constant(0.0f) min maxDelayTime_3
  val combC           = CombC.ar(in_28, maxDelayTime = maxDelayTime_3, delayTime = delayTime_4, decayTime = 57.973328)
  val in_29           = LeakDC.ar(0.015426122, coeff = 0.995)
  val x0_1            = LPF.ar(in_29, freq = 10.0)
  val henonC_1        = HenonC.ar(freq = 456.53043, a = -0.501102, b = 1719.5327, x0 = x0_1, x1 = 0.08747408)
  val gbmanN          = GbmanN.ar(freq = 456.53043, xi = 0.29398212, yi = 1814.6665)
  val in_30           = LeakDC.ar(958.20404, coeff = 0.995)
  val decay2          = Decay2.ar(in_30, attack = 0.015426122, release = 30.0)
  val difsqr          = 0.023828123 difsqr decay2
  val in_31           = LeakDC.ar(2555.6104, coeff = 0.995)
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
  val in_32           = LeakDC.ar(958.20404, coeff = 0.995)
  val lag2_1          = Lag2.ar(in_32, time = 0.1)
  val in_33           = LeakDC.ar(0.0043667015, coeff = 0.995)
  val combL           = CombL.ar(in_33, maxDelayTime = 20.0, delayTime = 0.0, decayTime = 8.858717)
  val syncSaw         = SyncSaw.ar(syncFreq = 3.9252315, sawFreq = 3.1978712)
  val in_34           = LeakDC.ar(0.006367362, coeff = 0.995)
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
  val in_35           = Mix.Mono(mix)
  val checkBadValues  = CheckBadValues.ar(in_35, id = 0.0, post = 0.0)
  val gate            = checkBadValues sig_== 0.0
  val in_36           = Gate.ar(in_35, gate = gate)
  val pan2            = Pan2.ar(in_36, pos = 0.0, level = 1.0)
  val sig = pan2 // Resonz.ar(pan2, "freq".kr(777), rq = 1)
  Out.ar(0, Limiter.ar(LeakDC.ar(sig)) * "amp".kr(0.2))
}

/*---
*/
