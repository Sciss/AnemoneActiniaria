val x = play {
  RandSeed.ir(trig = 1, seed = 56789.0)
  val syncSaw         = SyncSaw.ar(syncFreq = 0.018849522, sawFreq = 0.07972072)
  val lFPar_0         = LFPar.ar(freq = 0.01, iphase = 0.0)
  val clip2_0         = lFPar_0 clip2 0.7677357
  val lFPulse_0       = LFPulse.ar(freq = 0.07972072, iphase = 1.0, width = 0.0)
  val decayTime       = lFPulse_0 >= clip2_0
  val mod_0           = 1.0 % lFPulse_0
  val lFPulse_1       = LFPulse.ar(freq = 0.01, iphase = 1.0, width = 0.0)
  val in_0            = LeakDC.ar(mod_0, coeff = 0.995)
  val max_0           = lFPulse_1 max 0.0
  val delayTime_0     = max_0 min 0.0014424388
  val combC_0         = CombC.ar(in_0, maxDelayTime = 0.0014424388, delayTime = delayTime_0, decayTime = 7.531644)
  val in_1            = LeakDC.ar(1.5063983E-5, coeff = 0.995)
  val max_1           = mod_0 max 0.0
  val delayTime_1     = max_1 min 0.022008082
  val in_2            = AllpassC.ar(in_1, maxDelayTime = 0.022008082, delayTime = delayTime_1, decayTime = 0.0014424388)
  val onePole         = OnePole.ar(in_2, coeff = 0.0023975172)
  val atan2           = 1.5274479E-4 atan2 in_2
  val in_3            = LeakDC.ar(-1.89771E-5, coeff = 0.995)
  val delay1_0        = Delay1.ar(in_3)
  val ring3           = lFPulse_1 ring3 8.916748
  val ring4           = delay1_0 ring4 ring3
  val roundTo         = delay1_0 roundTo -4.619783
  val max_2           = delay1_0 max 0.0
  val width_0         = max_2 min 1.0
  val lFGauss_0       = LFGauss.ar(dur = 5.0E-5, width = width_0, phase = 1.0, loop = 1644.8522, doneAction = doNothing)
  val sumsqr          = lFGauss_0 sumsqr 117.86163
  val lFDClipNoise    = LFDClipNoise.ar(0.8250135)
  val d               = lFDClipNoise thresh -419.27997
  val in_4            = LeakDC.ar(7.3190875, coeff = 0.995)
  val max_3           = lFPulse_0 max 0.0
  val delayTime_2     = max_3 min 0.0
  val delayL_0        = DelayL.ar(in_4, maxDelayTime = 0.0, delayTime = delayTime_2)
  val quadN_0         = QuadN.ar(freq = 0.0, a = 0.0, b = 0.0, c = 0.0, xi = 0.8250135)
  val min_0           = Constant(0.0f) min quadN_0
  val freq_0          = CuspL.ar(freq = delayL_0, a = lFPar_0, b = min_0, xi = 1.5063983E-5)
  val xi_0            = LFDNoise1.ar(0.66143084)
  val neq             = freq_0 sig_!= xi_0
  val linCongL        = LinCongL.ar(freq = freq_0, a = -13.891433, c = 0.14163044, m = 0.080567405, xi = 0.72003174)
  val freq_1          = freq_0 >= 41.52797
  val lFDNoise1       = LFDNoise1.ar(freq_1)
  val rLPF            = RLPF.ar(delayL_0, freq = 10.0, rq = 0.01)
  val thresh          = delayL_0 thresh 133.04327
  val max_4           = d max 0.01
  val freq_2          = max_4 min 20000.0
  val max_5           = delayL_0 max 0.0
  val iphase_0        = max_5 min 1.0
  val lFPar_1         = LFPar.ar(freq = freq_2, iphase = iphase_0)
  val in_5            = LeakDC.ar(0.007981387, coeff = 0.995)
  val max_6           = atan2 max 0.55
  val roomSize_0      = max_6 min 300.0
  val loop_0          = GVerb.ar(in_5, roomSize = roomSize_0, revTime = 0.0, damping = 1.0, inputBW = 0.66143084, spread = 43.0, dryLevel = lFPar_1, earlyRefLevel = 13.28049, tailLevel = 0.5, maxRoomSize = 300.0)
  val max_7           = d max 5.0E-5
  val dur_0           = max_7 min 100.0
  val lFGauss_1       = LFGauss.ar(dur = dur_0, width = 1.0, phase = 0.0, loop = loop_0, doneAction = doNothing)
  val in_6            = LeakDC.ar(1.0, coeff = 0.995)
  val lag_0           = Lag.ar(in_6, time = 0.0)
  val freq_3          = Nyquist()
  val linCongN        = LinCongN.ar(freq = freq_3, a = -0.36279276, c = lFPulse_0, m = 20.145914, xi = 0.10357987)
  val sqrdif          = linCongN sqrdif -2425.7073
  val in_7            = LeakDC.ar(1.0, coeff = 0.995)
  val combN_0         = CombN.ar(in_7, maxDelayTime = 0.8250135, delayTime = 0.8250135, decayTime = 0.8250135)
  val quadL           = QuadL.ar(freq = combN_0, a = -11.958342, b = 0.0, c = 9.145937, xi = 0.8250135)
  val in_8            = LeakDC.ar(linCongN, coeff = 0.995)
  val max_8           = quadN_0 max 0.0
  val maxDelayTime_0  = max_8 min 20.0
  val delayTime_3     = Constant(0.0034691016f) min maxDelayTime_0
  val allpassN_0      = AllpassN.ar(in_8, maxDelayTime = maxDelayTime_0, delayTime = delayTime_3, decayTime = combN_0)
  val cuspN_0         = CuspN.ar(freq = linCongN, a = lFDClipNoise, b = lFPulse_0, xi = 0.8250135)
  val in_9            = LeakDC.ar(20.145914, coeff = 0.995)
  val max_9           = linCongN max 0.0
  val maxDelayTime_1  = max_9 min 20.0
  val max_10          = lag_0 max 0.0
  val delayTime_4     = max_10 min maxDelayTime_1
  val freq_4          = DelayC.ar(in_9, maxDelayTime = maxDelayTime_1, delayTime = delayTime_4)
  val amclip          = freq_4 amclip -419.27997
  val gbmanL_0        = GbmanL.ar(freq = freq_4, xi = 1.0, yi = 0.10357987)
  val max_11          = delayL_0 max 0.0
  val iphase_1        = max_11 min 1.0
  val b_0             = LFPar.ar(freq = 0.01, iphase = iphase_1)
  val freq_5          = b_0 amclip quadN_0
  val in_10           = LeakDC.ar(-203.25075, coeff = 0.995)
  val max_12          = lFPulse_1 max 0.0
  val delayTime_5     = max_12 min 0.22664568
  val c_0             = AllpassN.ar(in_10, maxDelayTime = 0.22664568, delayTime = delayTime_5, decayTime = -4.619783)
  val quadN_1         = QuadN.ar(freq = freq_5, a = 2.1437016, b = 0.0, c = c_0, xi = 0.0034691016)
  val linCongC_0      = LinCongC.ar(freq = freq_5, a = 0.0, c = 0.8250135, m = 0.015739825, xi = 1.0)
  val c_1             = QuadN.ar(freq = 0.0, a = lFPar_1, b = b_0, c = 0.8250135, xi = 199.98691)
  val gbmanL_1        = GbmanL.ar(freq = 152.6102, xi = 7.531644, yi = 15.662841)
  val max_13          = lag_0 max 0.0
  val maxDelayTime_2  = max_13 min 20.0
  val max_14          = linCongC_0 max 0.0
  val delayTime_6     = max_14 min maxDelayTime_2
  val allpassN_1      = AllpassN.ar(gbmanL_1, maxDelayTime = maxDelayTime_2, delayTime = delayTime_6, decayTime = decayTime)
  val gbmanN          = GbmanN.ar(freq = gbmanL_1, xi = -419.27997, yi = -0.42427403)
  val quadC_0         = QuadC.ar(freq = 0.0, a = 7.3190875, b = -0.0076536634, c = quadN_0, xi = 0.0)
  val in_11           = LinCongC.ar(freq = 0.022283768, a = quadC_0, c = 743.26575, m = 41.52797, xi = quadC_0)
  val plus            = in_11 + 0.09049736
  val in_12           = LeakDC.ar(in_11, coeff = 0.995)
  val b_1             = BPZ2.ar(in_12)
  val in_13           = LeakDC.ar(7388.521, coeff = 0.995)
  val max_15          = d max 0.0
  val radius          = max_15 min 1.0
  val x0              = TwoZero.ar(in_13, freq = 10.0, radius = radius)
  val henonC          = HenonC.ar(freq = 1605.479, a = 0.008464628, b = b_1, x0 = x0, x1 = 0.015739825)
  val max_16          = henonC max 0.5
  val b_2             = max_16 min 1.5
  val max_17          = quadC_0 max 0.5
  val c_2             = max_17 min 1.5
  val latoocarfianC_0 = LatoocarfianC.ar(freq = gbmanL_1, a = 0.028330043, b = b_2, c = c_2, d = d, xi = -0.0025060782, yi = -0.42427403)
  val c_3             = QuadL.ar(freq = -1.89771E-5, a = gbmanL_1, b = 7.107886, c = c_1, xi = 0.0)
  val quadC_1         = QuadC.ar(freq = -0.0025060782, a = 7.531644, b = -1.0, c = c_3, xi = 29.838459)
  val max_18          = lFGauss_0 max 0.0
  val iphase_2        = max_18 min 1.0
  val lFPulse_2       = LFPulse.ar(freq = 0.01, iphase = iphase_2, width = 0.8250135)
  val mod_1           = 1605.479 % lFPulse_1
  val in_14           = LeakDC.ar(min_0, coeff = 0.995)
  val max_19          = mod_1 max 0.55
  val max_20          = min_0 max 0.0
  val damping_0       = max_20 min 1.0
  val max_21          = lFPar_0 max 0.0
  val spread_0        = max_21 min 43.0
  val roomSize_1      = max_19 min 0.8250135
  val in_15           = GVerb.ar(in_14, roomSize = roomSize_1, revTime = 0.0, damping = damping_0, inputBW = 0.0, spread = spread_0, dryLevel = 1.0, earlyRefLevel = 0.8250135, tailLevel = -11.958342, maxRoomSize = 0.8250135)
  val min_1           = in_15 min loop_0
  val delay1_1        = Delay1.ar(in_15)
  val max_22          = delay1_1 max -3.0
  val a_0             = max_22 min 3.0
  val latoocarfianC_1 = LatoocarfianC.ar(freq = 1.5274479E-4, a = a_0, b = 0.5, c = 0.5, d = -2.821052E-4, xi = xi_0, yi = lFPar_1)
  val max_23          = lFGauss_1 max 0.5
  val c_4             = max_23 min 1.5
  val latoocarfianC_2 = LatoocarfianC.ar(freq = 176214.97, a = 0.22664568, b = 0.5, c = c_4, d = 0.35394838, xi = mod_0, yi = 0.5)
  val in_16           = LeakDC.ar(0.028330043, coeff = 0.995)
  val combC_1         = CombC.ar(in_16, maxDelayTime = 7.3190875, delayTime = 0.090563826, decayTime = 176214.97)
  val in_17           = LeakDC.ar(152.6102, coeff = 0.995)
  val max_24          = lFPulse_0 max 0.0
  val timeDown        = max_24 min 30.0
  val lag2UD          = Lag2UD.ar(in_17, timeUp = 0.080567405, timeDown = timeDown)
  val in_18           = LeakDC.ar(0.008464628, coeff = 0.995)
  val max_25          = lag2UD max 0.0
  val maxDelayTime_3  = max_25 min 20.0
  val max_26          = cuspN_0 max 0.0
  val delayTime_7     = max_26 min maxDelayTime_3
  val delayL_1        = DelayL.ar(in_18, maxDelayTime = maxDelayTime_3, delayTime = delayTime_7)
  val in_19           = LeakDC.ar(132.21826, coeff = 0.995)
  val delay2_0        = Delay2.ar(in_19)
  val minus           = -0.6436421 - lFDNoise1
  val in_20           = LeakDC.ar(7.107886, coeff = 0.995)
  val earlyRefLevel_0 = LPZ2.ar(in_20)
  val in_21           = LeakDC.ar(0.022283768, coeff = 0.995)
  val max_27          = freq_5 max 0.55
  val max_28          = delay2_0 max 0.0
  val revTime_0       = max_28 min 100.0
  val max_29          = lFPar_1 max 0.0
  val damping_1       = max_29 min 1.0
  val roomSize_2      = max_27 min 0.55
  val gVerb           = GVerb.ar(in_21, roomSize = roomSize_2, revTime = revTime_0, damping = damping_1, inputBW = 0.0, spread = 43.0, dryLevel = 0.0, earlyRefLevel = earlyRefLevel_0, tailLevel = -0.0076536634, maxRoomSize = 0.55)
  val in_22           = LeakDC.ar(in_11, coeff = 0.995)
  val max_30          = min_1 max 0.0
  val timeUp_0        = max_30 min 30.0
  val lag3UD          = Lag3UD.ar(in_22, timeUp = timeUp_0, timeDown = 0.0)
  val in_23           = LeakDC.ar(0.14163044, coeff = 0.995)
  val max_31          = gbmanL_1 max 0.0
  val revTime_1       = max_31 min 100.0
  val spread_1        = lFDClipNoise max 0.0
  val max_32          = lag_0 max 0.55
  val maxRoomSize_0   = max_32 min 300.0
  val roomSize_3      = Constant(7.107886f) min maxRoomSize_0
  val a_1             = GVerb.ar(in_23, roomSize = roomSize_3, revTime = revTime_1, damping = 0.0, inputBW = 1.5274479E-4, spread = spread_1, dryLevel = quadN_0, earlyRefLevel = 1.5042997E-4, tailLevel = lFPulse_0, maxRoomSize = maxRoomSize_0)
  val in_24           = LeakDC.ar(1.5063983E-5, coeff = 0.995)
  val integrator      = Integrator.ar(in_24, coeff = 0.999)
  val lag_1           = Lag.ar(integrator, time = 0.022008082)
  val excess          = integrator excess earlyRefLevel_0
  val cuspN_1         = CuspN.ar(freq = -0.0076536634, a = a_1, b = integrator, xi = lFDClipNoise)
  val delay2_1        = Delay2.ar(combN_0)
  val absdif          = -4.619783 absdif allpassN_0
  val linCongC_1      = LinCongC.ar(freq = 20.145914, a = min_0, c = 0.7677357, m = 1.0, xi = 0.7677357)
  val max_33          = linCongC_1 max 0.0
  val phase_0         = max_33 min 1.0
  val lFGauss_2       = LFGauss.ar(dur = 1.5274479E-4, width = 0.0, phase = phase_0, loop = 0.0014424388, doneAction = doNothing)
  val in_25           = LeakDC.ar(0.8250135, coeff = 0.995)
  val delay1_2        = Delay1.ar(in_25)
  val gbmanL_2        = GbmanL.ar(freq = 29.838459, xi = 2.1437016, yi = -1.89771E-5)
  val clip2_1         = 1605.479 clip2 gbmanL_2
  val in_26           = LeakDC.ar(0.015739825, coeff = 0.995)
  val max_34          = atan2 max 10.0
  val freq_6          = max_34 min 20000.0
  val hPF             = HPF.ar(in_26, freq = freq_6)
  val sqrsum          = -11.958342 sqrsum hPF
  val cuspN_2         = CuspN.ar(freq = -419.27997, a = lFDClipNoise, b = lFPar_0, xi = lag_0)
  val in_27           = LeakDC.ar(-4.619783, coeff = 0.995)
  val allpassL        = AllpassL.ar(in_27, maxDelayTime = 7.3190875, delayTime = 0.2, decayTime = -2425.7073)
  val in_28           = LeakDC.ar(-419.27997, coeff = 0.995)
  val b_3             = Delay2.ar(in_28)
  val max_35          = ring3 max 0.0
  val h               = max_35 min 0.06
  val in_29           = LorenzL.ar(freq = 0.022283768, s = 9.145937, r = 28.0, b = b_3, h = h, xi = min_0, yi = 7388.521, zi = mod_0)
  val in_30           = LeakDC.ar(in_29, coeff = 0.995)
  val max_36          = x0 max 0.0
  val delayTime_8     = max_36 min 0.015739825
  val combC_2         = CombC.ar(in_30, maxDelayTime = 0.015739825, delayTime = delayTime_8, decayTime = 5.5349483)
  val in_31           = LeakDC.ar(0.0021974712, coeff = 0.995)
  val delayN          = DelayN.ar(in_31, maxDelayTime = 0.8250135, delayTime = 0.8250135)
  val lt              = 370.1297 < in_2
  val in_32           = LeakDC.ar(-93.21562, coeff = 0.995)
  val combC_3         = CombC.ar(in_32, maxDelayTime = 0.8250135, delayTime = 0.8250135, decayTime = lag_0)
  val in_33           = LeakDC.ar(0.032853525, coeff = 0.995)
  val combN_1         = CombN.ar(in_33, maxDelayTime = 20.0, delayTime = 0.0048477706, decayTime = 0.028330043)
  val in_34           = LeakDC.ar(-0.0028008358, coeff = 0.995)
  val twoZero         = TwoZero.ar(in_34, freq = 440.0, radius = 0.0)
  
  val ca = "ca".kr(Vector.fill(45)(1f))
  
  val mix             = Mix((Seq[GE](
      syncSaw , combC_0  , onePole        , ring4  , roundTo  , sumsqr         , neq            , linCongL, 
      rLPF    , thresh   , sqrdif         , quadL  , amclip   , gbmanL_0       , quadN_1        , allpassN_1, 
      gbmanN  , plus     , latoocarfianC_0, quadC_1, lFPulse_2, latoocarfianC_1, latoocarfianC_2, combC_1, 
      delayL_1, minus    , gVerb          , lag3UD , lag_1    , excess         , cuspN_1        , delay2_1, 
      absdif  , lFGauss_2, delay1_2       , clip2_1, sqrsum   , cuspN_2        , allpassL       , combC_2, 
      delayN  , lt       , combC_3        , combN_1, twoZero
  ): GE) * ca)
  val in_35           = Mix.Mono(mix)
  val checkBadValues  = CheckBadValues.ar(in_35, id = 0.0, post = 0.0)
  val gate            = checkBadValues sig_== 0.0
  val in_36           = Gate.ar(in_35, gate = gate)
  val pan2            = Pan2.ar(in_36, pos = 0.0, level = 1.0)
  val sig = pan2
  Out.ar(0, Limiter.ar(LeakDC.ar(sig)) * "amp".kr(0.7))
}

/*---

  x.set"amp" -> 1)

  x.set("ca" -> Vector.fill(45)(1f))

  x.set("ca" -> Vector.fill(45)(0f)
     .updated(15, 1f)
     .updated(16, 1f)
     .updated(35, 1f)
     .updated(10, 1f)
//     .updated(7, 1f)
    )

  // 15
  // 16
  // 35

*/
