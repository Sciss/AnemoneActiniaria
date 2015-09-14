val x = play {
  RandSeed.ir(trig = 1, seed = 56789.0)
  val syncSaw         = SyncSaw.ar(syncFreq = 872.9059, sawFreq = 0.07972072)
  val lFPulse_0       = LFPulse.ar(freq = 440.0, iphase = 1.0, width = 1.0)
  val mod_0           = 1.0 % lFPulse_0
  val lFPulse_1       = LFPulse.ar(freq = 7.3190875, iphase = 0.0, width = 0.0)
  val in_0            = LeakDC.ar(mod_0, coeff = 0.995)
  val max_0           = lFPulse_1 max 0.0
  val delayTime_0     = max_0 min 0.0014424388
  val combC_0         = CombC.ar(in_0, maxDelayTime = 0.0014424388, delayTime = delayTime_0, decayTime = 7.531644)
  val in_1            = LeakDC.ar(1.5063983E-5, coeff = 0.995)
  val max_1           = mod_0 max 0.0
  val delayTime_1     = max_1 min 0.022008082
  val in_2            = AllpassC.ar(in_1, maxDelayTime = 0.022008082, delayTime = delayTime_1, decayTime = 0.0014424388)
  val onePole         = OnePole.ar(in_2, coeff = 0.0023975172)
  val atan2           = 872.9059 atan2 in_2
  val lFPar_0         = LFPar.ar(freq = 0.01, iphase = 0.0)
  val clip2_0         = lFPar_0 clip2 0.7677357
  val a_0             = LFDClipNoise.ar(0.8250135)
  val d               = a_0 thresh 872.9059
  val in_3            = LeakDC.ar(7.3190875, coeff = 0.995)
  val max_2           = lFPulse_0 max 0.0
  val delayTime_2     = max_2 min 0.0
  val delayL_0        = DelayL.ar(in_3, maxDelayTime = 0.0, delayTime = delayTime_2)
  val in_4            = LeakDC.ar(1.0, coeff = 0.995)
  val lag_0           = Lag.ar(in_4, time = 0.0)
  val freq_0          = Nyquist()
  val linCongN        = LinCongN.ar(freq = freq_0, a = -0.36279276, c = lFPulse_0, m = 20.145914, xi = 0.10357987)
  val quadN_0         = QuadN.ar(freq = 0.0, a = 0.0, b = 0.0, c = 0.0, xi = 0.8250135)
  val in_5            = LeakDC.ar(20.145914, coeff = 0.995)
  val max_3           = linCongN max 0.0
  val maxDelayTime_0  = max_3 min 20.0
  val max_4           = lag_0 max 0.0
  val delayTime_3     = max_4 min maxDelayTime_0
  val freq_1          = DelayC.ar(in_5, maxDelayTime = maxDelayTime_0, delayTime = delayTime_3)
  val gbmanL_0        = GbmanL.ar(freq = freq_1, xi = 1.0, yi = 0.10357987)
  val min_0           = gbmanL_0 min quadN_0
  val freq_2          = CuspL.ar(freq = delayL_0, a = lFPar_0, b = min_0, xi = 1.5063983E-5)
  val geq             = freq_2 >= 41.52797
  val xi_0            = LFDNoise1.ar(0.66143084)
  val neq             = freq_2 sig_!= xi_0
  val linCongL        = LinCongL.ar(freq = freq_2, a = -13.891433, c = 1.5042997E-4, m = 0.080567405, xi = 0.0)
  val sqrdif          = linCongN sqrdif 872.9059
  val lFDClipNoise    = LFDClipNoise.ar(7.531644)
  val cuspN_0         = CuspN.ar(freq = linCongN, a = lFDClipNoise, b = lFPulse_0, xi = 0.8250135)
  val in_6            = LeakDC.ar(1.0, coeff = 0.995)
  val combN_0         = CombN.ar(in_6, maxDelayTime = 0.8250135, delayTime = 0.8250135, decayTime = 0.8250135)
  val delay2          = Delay2.ar(combN_0)
  val in_7            = LeakDC.ar(linCongN, coeff = 0.995)
  val max_5           = quadN_0 max 0.0
  val maxDelayTime_1  = max_5 min 20.0
  val delayTime_4     = Constant(0.0014424388f) min maxDelayTime_1
  val freq_3          = AllpassN.ar(in_7, maxDelayTime = maxDelayTime_1, delayTime = delayTime_4, decayTime = combN_0)
  val cuspN_1         = CuspN.ar(freq = freq_3, a = a_0, b = lFPar_0, xi = lag_0)
  val decayTime_0     = cuspN_1 ring4 1.0
  val max_6           = decayTime_0 max 10.0
  val freq_4          = max_6 min 20000.0
  val rLPF            = RLPF.ar(delayL_0, freq = freq_4, rq = 0.01)
  val thresh          = delayL_0 thresh 133.04327
  val max_7           = delayL_0 max 0.01
  val freq_5          = max_7 min 20000.0
  val max_8           = d max 0.0
  val iphase_0        = max_8 min 1.0
  val lFPar_1         = LFPar.ar(freq = freq_5, iphase = iphase_0)
  val in_8            = LeakDC.ar(7388.521, coeff = 0.995)
  val max_9           = d max 0.0
  val radius_0        = max_9 min 1.0
  val x0              = TwoZero.ar(in_8, freq = 10.0, radius = radius_0)
  val ring3           = lFPulse_1 ring3 -88.06463
  val in_9            = LeakDC.ar(132.21826, coeff = 0.995)
  val in_10           = Delay2.ar(in_9)
  val b_0             = Delay2.ar(in_10)
  val max_10          = ring3 max 0.0
  val h               = max_10 min 0.06
  val in_11           = LorenzL.ar(freq = 0.022283768, s = 9.145937, r = 28.0, b = b_0, h = h, xi = min_0, yi = 7388.521, zi = mod_0)
  val in_12           = LeakDC.ar(in_11, coeff = 0.995)
  val max_11          = x0 max 0.0
  val delayTime_5     = max_11 min 0.015739825
  val combC_1         = CombC.ar(in_12, maxDelayTime = 0.015739825, delayTime = delayTime_5, decayTime = 0.014783192)
  val amclip          = freq_1 amclip combC_1
  val henonC          = HenonC.ar(freq = 1605.479, a = 0.008464628, b = -1.89771E-5, x0 = x0, x1 = 0.015739825)
  val quadC_0         = QuadC.ar(freq = 0.8250135, a = 7.3190875, b = -0.008399427, c = quadN_0, xi = 0.0)
  val max_12          = henonC max 0.5
  val b_1             = max_12 min 1.5
  val max_13          = quadC_0 max 0.5
  val c_0             = max_13 min 1.5
  val latoocarfianC_0 = LatoocarfianC.ar(freq = gbmanL_0, a = 0.028330043, b = b_1, c = c_0, d = d, xi = -0.0025060782, yi = -0.42427403)
  val mod_1           = 1605.479 % lFPulse_1
  val in_13           = LeakDC.ar(min_0, coeff = 0.995)
  val max_14          = mod_1 max 0.55
  val max_15          = min_0 max 0.0
  val damping_0       = max_15 min 1.0
  val max_16          = lFPar_0 max 0.0
  val spread_0        = max_16 min 43.0
  val roomSize_0      = max_14 min 0.8250135
  val in_14           = GVerb.ar(in_13, roomSize = roomSize_0, revTime = 0.0, damping = damping_0, inputBW = 1.0, spread = spread_0, dryLevel = 1.0, earlyRefLevel = 0.8250135, tailLevel = -11.958342, maxRoomSize = 0.8250135)
  val delay1_0        = Delay1.ar(in_14)
  val in_15           = LinCongC.ar(freq = 0.022283768, a = quadC_0, c = 743.26575, m = 41.52797, xi = quadC_0)
  val in_16           = LeakDC.ar(1605.479, coeff = 0.995)
  val max_17          = atan2 max 0.55
  val roomSize_1      = max_17 min 300.0
  val gVerb_0         = GVerb.ar(in_16, roomSize = roomSize_1, revTime = 0.0, damping = 1.0, inputBW = 1.0, spread = 0.007981387, dryLevel = lFPar_1, earlyRefLevel = 0.66143084, tailLevel = 0.5, maxRoomSize = 300.0)
  val min_1           = gVerb_0 min in_14
  val in_17           = LeakDC.ar(in_15, coeff = 0.995)
  val max_18          = min_1 max 0.0
  val timeUp          = max_18 min 30.0
  val lag3UD          = Lag3UD.ar(in_17, timeUp = timeUp, timeDown = 0.0)
  val clip2_1         = 0.022008082 clip2 lag3UD
  val in_18           = LeakDC.ar(-1.89771E-5, coeff = 0.995)
  val delay1_1        = Delay1.ar(in_18)
  val ring4           = delay1_1 ring4 ring3
  val max_19          = delay1_1 max 0.0
  val width_0         = max_19 min 1.0
  val lFGauss_0       = LFGauss.ar(dur = 5.0E-5, width = width_0, phase = 1.0, loop = 1644.8522, doneAction = doNothing)
  val sumsqr          = lFGauss_0 sumsqr 117.86163
  val b_2             = LFDClipNoise.ar(-4.619783)
  val c_1             = QuadN.ar(freq = 0.0, a = lFPar_1, b = b_2, c = 0.8250135, xi = 23.458443)
  val c_2             = QuadL.ar(freq = -1.89771E-5, a = gbmanL_0, b = 7.107886, c = c_1, xi = 0.0)
  val quadC_1         = QuadC.ar(freq = -0.0025060782, a = 7.531644, b = -1.0, c = c_2, xi = 29.838459)
  val max_20          = lFGauss_0 max 0.0
  val iphase_1        = max_20 min 1.0
  val lFPulse_2       = LFPulse.ar(freq = 0.01, iphase = iphase_1, width = 0.8250135)
  val in_19           = LeakDC.ar(1.5042997E-4, coeff = 0.995)
  val max_21          = gbmanL_0 max 0.0
  val revTime_0       = max_21 min 100.0
  val inputBW_0       = a_0 max 0.0
  val spread_1        = a_0 max 0.0
  val max_22          = lag_0 max 0.55
  val maxRoomSize_0   = max_22 min 300.0
  val roomSize_2      = Constant(7.107886f) min maxRoomSize_0
  val gVerb_1         = GVerb.ar(in_19, roomSize = roomSize_2, revTime = revTime_0, damping = 0.0, inputBW = inputBW_0, spread = spread_1, dryLevel = quadN_0, earlyRefLevel = -5452.869, tailLevel = lFPulse_0, maxRoomSize = maxRoomSize_0)
  val max_23          = delay1_0 max -3.0
  val a_1             = max_23 min 3.0
  val latoocarfianC_1 = LatoocarfianC.ar(freq = gVerb_1, a = a_1, b = 0.5, c = 0.5, d = -2.821052E-4, xi = xi_0, yi = lFPar_1)
  val in_20           = LeakDC.ar(0.028330043, coeff = 0.995)
  val combC_2         = CombC.ar(in_20, maxDelayTime = 0.090508856, delayTime = 0.007982805, decayTime = decayTime_0)
  val max_24          = delayL_0 max 0.0
  val iphase_2        = max_24 min 1.0
  val lFPar_2         = LFPar.ar(freq = 872.9059, iphase = iphase_2)
  val freq_6          = lFPar_2 amclip quadN_0
  val decayTime_1     = LinCongC.ar(freq = freq_6, a = 0.0, c = 1.0, m = 0.015739825, xi = 1.0)
  val in_21           = LeakDC.ar(7.107886, coeff = 0.995)
  val earlyRefLevel_0 = LPZ2.ar(in_21)
  val in_22           = LeakDC.ar(0.022283768, coeff = 0.995)
  val max_25          = freq_6 max 0.55
  val max_26          = in_10 max 0.0
  val revTime_1       = max_26 min 100.0
  val max_27          = lFPar_1 max 0.0
  val damping_1       = max_27 min 1.0
  val roomSize_3      = max_25 min 0.55
  val gVerb_2         = GVerb.ar(in_22, roomSize = roomSize_3, revTime = revTime_1, damping = damping_1, inputBW = 0.0, spread = 23.458443, dryLevel = 0.0, earlyRefLevel = earlyRefLevel_0, tailLevel = -0.008399427, maxRoomSize = 0.55)
  val in_23           = LeakDC.ar(0.008464628, coeff = 0.995)
  val max_28          = gVerb_2 max 0.0
  val maxDelayTime_2  = max_28 min 20.0
  val max_29          = cuspN_0 max 0.0
  val delayTime_6     = max_29 min maxDelayTime_2
  val delayL_1        = DelayL.ar(in_23, maxDelayTime = maxDelayTime_2, delayTime = delayTime_6)
  val plus            = Constant(0.09049736f) + in_15
  val in_24           = LeakDC.ar(1.5063983E-5, coeff = 0.995)
  val integrator      = Integrator.ar(in_24, coeff = 0.999)
  val lag_1           = Lag.ar(integrator, time = 0.022008082)
  val excess          = integrator excess earlyRefLevel_0
  val cuspN_2         = CuspN.ar(freq = -0.008399427, a = gVerb_1, b = integrator, xi = lFDClipNoise)
  val absdif          = 0.0 absdif freq_3
  val linCongC        = LinCongC.ar(freq = 20.145914, a = min_0, c = 0.7677357, m = 1.0, xi = 0.7677357)
  val max_30          = linCongC max 0.0
  val phase_0         = max_30 min 1.0
  val lFGauss_1       = LFGauss.ar(dur = 0.0014424388, width = 0.0, phase = phase_0, loop = 0.66143084, doneAction = doNothing)
  val in_25           = LeakDC.ar(0.8250135, coeff = 0.995)
  val delay1_2        = Delay1.ar(in_25)
  val gbmanL_1        = GbmanL.ar(freq = 29.838459, xi = 2.1437016, yi = -1.89771E-5)
  val clip2_2         = 1605.479 clip2 gbmanL_1
  val in_26           = LeakDC.ar(0.015739825, coeff = 0.995)
  val max_31          = atan2 max 10.0
  val freq_7          = max_31 min 20000.0
  val hPF             = HPF.ar(in_26, freq = freq_7)
  val sqrsum          = -11.958342 sqrsum hPF
  val in_27           = LeakDC.ar(-4.619783, coeff = 0.995)
  val allpassL        = AllpassL.ar(in_27, maxDelayTime = 7.3190875, delayTime = 0.2, decayTime = 872.9059)
  val in_28           = LeakDC.ar(0.0021974712, coeff = 0.995)
  val delayN          = DelayN.ar(in_28, maxDelayTime = 0.8250135, delayTime = 0.8250135)
  val in_29           = GbmanL.ar(freq = 7.531644, xi = 152.6102, yi = 15.662841)
  val in_30           = LeakDC.ar(in_29, coeff = 0.995)
  val max_32          = lag_0 max 0.0
  val maxDelayTime_3  = max_32 min 20.0
  val max_33          = decayTime_1 max 0.0
  val delayTime_7     = max_33 min maxDelayTime_3
  val allpassN        = AllpassN.ar(in_30, maxDelayTime = maxDelayTime_3, delayTime = delayTime_7, decayTime = decayTime_1)
  val in_31           = LeakDC.ar(in_29, coeff = 0.995)
  val lPF             = LPF.ar(in_31, freq = 10.0)
  val lt              = 12.524387 < in_2
  val in_32           = LeakDC.ar(-93.21562, coeff = 0.995)
  val combC_3         = CombC.ar(in_32, maxDelayTime = 0.8250135, delayTime = 0.8250135, decayTime = lag_0)
  val in_33           = LeakDC.ar(0.032853525, coeff = 0.995)
  val max_34          = decayTime_0 max 0.0
  val maxDelayTime_4  = max_34 min 20.0
  val delayTime_8     = Constant(0.028330043f) min maxDelayTime_4
  val combN_1         = CombN.ar(in_33, maxDelayTime = maxDelayTime_4, delayTime = delayTime_8, decayTime = 33.432255)
  val in_34           = LeakDC.ar(-0.0028008358, coeff = 0.995)
  val max_35          = min_1 max 0.0
  val radius_1        = max_35 min 1.0
  val twoZero         = TwoZero.ar(in_34, freq = 41.097713, radius = radius_1)
  val in_35           = LeakDC.ar(-203.25075, coeff = 0.995)
  val max_36          = lFPulse_1 max 0.0
  val delayTime_9     = max_36 min 0.22664568
  val c_3             = AllpassN.ar(in_35, maxDelayTime = 0.22664568, delayTime = delayTime_9, decayTime = -4.619783)
  val quadN_1         = QuadN.ar(freq = 3773.042, a = 2.1437016, b = 0.0, c = c_3, xi = 0.0014424388)
  
  val ca = "ca".kr(Vector.fill(39)(0f))
  
  val mix             = Mix((Seq[GE](
    syncSaw , combC_0   , onePole         , clip2_0 , geq             , neq     , linCongL, sqrdif, 
    delay2  , rLPF      , thresh          , amclip  , latoocarfianC_0 , clip2_1 , ring4   , sumsqr, 
    quadC_1 , lFPulse_2 , latoocarfianC_1 , combC_2 , delayL_1        , plus    , lag_1   , excess, 
    cuspN_2 , absdif    , lFGauss_1       , delay1_2, clip2_2         , sqrsum  , allpassL, delayN, 
    allpassN, lPF       , lt              , combC_3 , combN_1         , twoZero , quadN_1
    ): GE) * ca)
    
  val in_36           = Mix.Mono(mix)
  val checkBadValues  = CheckBadValues.ar(in_36, id = 0.0, post = 0.0)
  val gate            = checkBadValues sig_== 0.0
  val in_37           = Gate.ar(in_36, gate = gate)
  val pan2            = Pan2.ar(in_37, pos = 0.0, level = 1.0)
  val sig = Median.ar(pan2, 3)
  Out.ar(0, Limiter.ar(LeakDC.ar(sig)) * "amp".kr(0.5))
}

/*---

  x.set("ca" -> Vector.fill(39)(0f)
    .updated(7, 1f)
//    .updated(24, 1f)
//    .updated(25, 1f)
    .updated(28, 1f)
//    .updated(32, 1f)
  )
  x.set("amp" -> 0.5)
  
  // 7
  // 24
  // 25
  // 28
  // 32

*/