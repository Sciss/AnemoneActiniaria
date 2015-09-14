val x = play {
  RandSeed.ir(trig = 1, seed = 56789.0)
  val syncSaw         = SyncSaw.ar(syncFreq = 0.018849522, sawFreq = 0.07972072)
  val lFPar_0         = LFPar.ar(freq = 0.01, iphase = 0.0)
  val clip2_0         = lFPar_0 clip2 0.7677357
  val fold2           = -697.5932 fold2 lFPar_0
  val in_0            = LeakDC.ar(fold2, coeff = 0.995)
  val b_0             = Delay2.ar(in_0)
  val ring2           = fold2 ring2 0.8250135
  val xi_0            = fold2 wrap2 1.0
  val a_0             = LFDClipNoise.ar(0.8250135)
  val in_1            = LeakDC.ar(1.0, coeff = 0.995)
  val lag_0           = Lag.ar(in_1, time = 0.0)
  val cuspN_0         = CuspN.ar(freq = fold2, a = a_0, b = lFPar_0, xi = lag_0)
  val decayTime_0     = fold2 ring4 1.0
  val d               = fold2 thresh a_0
  val bitAnd          = 0.018849522 & fold2
  val max_0           = bitAnd max 0.0
  val width_0         = max_0 min 1.0
  val lFPulse_0       = LFPulse.ar(freq = 0.07972072, iphase = 1.0, width = width_0)
  val decayTime_1     = lFPulse_0 >= clip2_0
  val in_2            = LeakDC.ar(7.3190875, coeff = 0.995)
  val max_1           = lFPulse_0 max 0.0
  val delayTime_0     = max_1 min 0.0
  val delayL_0        = DelayL.ar(in_2, maxDelayTime = 0.0, delayTime = delayTime_0)
  val quadN_0         = QuadN.ar(freq = 0.0, a = 0.0, b = 0.0, c = 0.0, xi = 0.8250135)
  val min_0           = Constant(0.0f) min quadN_0
  val freq_0          = CuspL.ar(freq = delayL_0, a = lFPar_0, b = min_0, xi = 1.5063983E-5)
  val freq_1          = freq_0 >= 41.52797
  val lFDNoise1       = LFDNoise1.ar(freq_1)
  val xi_1            = LFDNoise1.ar(0.66143084)
  val neq             = freq_0 sig_!= xi_1
  val linCongL        = LinCongL.ar(freq = freq_0, a = -13.891433, c = 0.14163044, m = 0.080567405, xi = xi_0)
  val rLPF            = RLPF.ar(delayL_0, freq = 10.0, rq = 0.01)
  val thresh          = delayL_0 thresh 133.04327
  val max_2           = d max 0.01
  val freq_2          = max_2 min 20000.0
  val max_3           = delayL_0 max 0.0
  val iphase_0        = max_3 min 1.0
  val lFPar_1         = LFPar.ar(freq = freq_2, iphase = iphase_0)
  val mod_0           = 1.0 % lFPulse_0
  val lFPulse_1       = LFPulse.ar(freq = 7.3190875, iphase = 0.0, width = 0.0)
  val in_3            = LeakDC.ar(mod_0, coeff = 0.995)
  val max_4           = lFPulse_1 max 0.0
  val delayTime_1     = max_4 min 0.0014424388
  val combC_0         = CombC.ar(in_3, maxDelayTime = 0.0014424388, delayTime = delayTime_1, decayTime = 7.531644)
  val in_4            = LeakDC.ar(1.5063983E-5, coeff = 0.995)
  val max_5           = mod_0 max 0.0
  val delayTime_2     = max_5 min 0.022008082
  val in_5            = AllpassC.ar(in_4, maxDelayTime = 0.022008082, delayTime = delayTime_2, decayTime = 0.0014424388)
  val atan2           = 1.5274479E-4 atan2 in_5
  val in_6            = LeakDC.ar(0.007981387, coeff = 0.995)
  val max_6           = atan2 max 0.55
  val roomSize_0      = max_6 min 300.0
  val loop            = GVerb.ar(in_6, roomSize = roomSize_0, revTime = 0.0, damping = 1.0, inputBW = 0.66143084, spread = 43.0, dryLevel = lFPar_1, earlyRefLevel = 13.28049, tailLevel = 0.5, maxRoomSize = 300.0)
  val max_7           = d max 5.0E-5
  val dur             = max_7 min 100.0
  val lFGauss_0       = LFGauss.ar(dur = dur, width = 1.0, phase = 0.0, loop = loop, doneAction = doNothing)
  val onePole         = OnePole.ar(in_5, coeff = 0.0026459466)
  val in_7            = LeakDC.ar(-1.89771E-5, coeff = 0.995)
  val delay1_0        = Delay1.ar(in_7)
  val ring3           = lFPulse_1 ring3 8.916748
  val ring4           = delay1_0 ring4 ring3
  val roundTo         = delay1_0 roundTo -4.619783
  val max_8           = delay1_0 max 0.0
  val width_1         = max_8 min 1.0
  val lFGauss_1       = LFGauss.ar(dur = 5.0E-5, width = width_1, phase = 1.0, loop = 1644.8522, doneAction = doNothing)
  val sumsqr          = lFGauss_1 sumsqr 117.86163
  val freq_3          = Nyquist()
  val linCongN        = LinCongN.ar(freq = freq_3, a = -0.36279276, c = lFPulse_0, m = 20.145914, xi = 0.10357987)
  val sqrdif          = linCongN sqrdif -2425.7073
  val in_8            = LeakDC.ar(1.0, coeff = 0.995)
  val combN_0         = CombN.ar(in_8, maxDelayTime = 0.8250135, delayTime = 0.8250135, decayTime = 0.8250135)
  val quadL           = QuadL.ar(freq = combN_0, a = 0.01589117, b = 0.0, c = 9.145937, xi = 0.8250135)
  val in_9            = LeakDC.ar(linCongN, coeff = 0.995)
  val max_9           = quadN_0 max 0.0
  val maxDelayTime_0  = max_9 min 20.0
  val delayTime_3     = Constant(0.0034691016f) min maxDelayTime_0
  val allpassN_0      = AllpassN.ar(in_9, maxDelayTime = maxDelayTime_0, delayTime = delayTime_3, decayTime = combN_0)
  val lFDClipNoise    = LFDClipNoise.ar(7.531644)
  val cuspN_1         = CuspN.ar(freq = linCongN, a = lFDClipNoise, b = lFPulse_0, xi = 0.8250135)
  val in_10           = LeakDC.ar(20.145914, coeff = 0.995)
  val max_10          = linCongN max 0.0
  val maxDelayTime_1  = max_10 min 20.0
  val max_11          = lag_0 max 0.0
  val delayTime_4     = max_11 min maxDelayTime_1
  val freq_4          = DelayC.ar(in_10, maxDelayTime = maxDelayTime_1, delayTime = delayTime_4)
  val amclip          = freq_4 amclip fold2
  val gbmanL_0        = GbmanL.ar(freq = freq_4, xi = 1.0, yi = 0.10357987)
  val quadC_0         = QuadC.ar(freq = 0.0, a = 7.3190875, b = -0.0076536634, c = quadN_0, xi = 0.0)
  val in_11           = LinCongC.ar(freq = 0.022283768, a = quadC_0, c = 743.26575, m = 41.52797, xi = quadC_0)
  val plus            = in_11 + 0.09049736
  val in_12           = LeakDC.ar(in_11, coeff = 0.995)
  val b_1             = BPZ2.ar(in_12)
  val in_13           = LeakDC.ar(7388.521, coeff = 0.995)
  val max_12          = d max 0.0
  val radius_0        = max_12 min 1.0
  val x0              = TwoZero.ar(in_13, freq = 10.0, radius = radius_0)
  val henonC          = HenonC.ar(freq = 1605.479, a = 0.008464628, b = b_1, x0 = x0, x1 = 0.015739825)
  val max_13          = henonC max 0.5
  val b_2             = max_13 min 1.5
  val max_14          = quadC_0 max 0.5
  val c_0             = max_14 min 1.5
  val latoocarfianC_0 = LatoocarfianC.ar(freq = gbmanL_0, a = 0.028330043, b = b_2, c = c_0, d = d, xi = -0.0025060782, yi = -0.42427403)
  val b_3             = LFDClipNoise.ar(-4.619783)
  val c_1             = QuadN.ar(freq = 0.0, a = lFPar_1, b = b_3, c = 0.8250135, xi = 199.98691)
  val c_2             = QuadL.ar(freq = -1.89771E-5, a = gbmanL_0, b = 7.107886, c = c_1, xi = 0.0)
  val quadC_1         = QuadC.ar(freq = -0.0025060782, a = 7.531644, b = -1.0, c = c_2, xi = 29.838459)
  val max_15          = lFGauss_1 max 0.0
  val iphase_1        = max_15 min 1.0
  val lFPulse_2       = LFPulse.ar(freq = 0.01, iphase = iphase_1, width = 0.8250135)
  val mod_1           = 1605.479 % lFPulse_1
  val in_14           = LeakDC.ar(min_0, coeff = 0.995)
  val max_16          = mod_1 max 0.55
  val max_17          = min_0 max 0.0
  val damping_0       = max_17 min 1.0
  val max_18          = lFPar_0 max 0.0
  val spread_0        = max_18 min 43.0
  val roomSize_1      = max_16 min 0.8250135
  val in_15           = GVerb.ar(in_14, roomSize = roomSize_1, revTime = 0.0, damping = damping_0, inputBW = 0.0, spread = spread_0, dryLevel = 1.0, earlyRefLevel = 0.8250135, tailLevel = -11.958342, maxRoomSize = 0.8250135)
  val min_1           = in_15 min loop
  val delay1_1        = Delay1.ar(in_15)
  val max_19          = delay1_1 max -3.0
  val a_1             = max_19 min 3.0
  val latoocarfianC_1 = LatoocarfianC.ar(freq = 1.5274479E-4, a = a_1, b = 0.5, c = 0.5, d = -2.821052E-4, xi = xi_1, yi = lFPar_1)
  val in_16           = LeakDC.ar(0.028330043, coeff = 0.995)
  val combC_1         = CombC.ar(in_16, maxDelayTime = 0.090563826, delayTime = 0.090563826, decayTime = decayTime_0)
  val in_17           = LeakDC.ar(0.008464628, coeff = 0.995)
  val max_20          = onePole max 0.0
  val radius_1        = max_20 min 1.0
  val twoPole         = TwoPole.ar(in_17, freq = 10.0, radius = radius_1)
  val in_18           = LeakDC.ar(4542.6772, coeff = 0.995)
  val max_21          = lFPulse_0 max 0.0
  val timeDown        = max_21 min 30.0
  val lag2UD          = Lag2UD.ar(in_18, timeUp = 0.080567405, timeDown = timeDown)
  val in_19           = LeakDC.ar(0.008464628, coeff = 0.995)
  val max_22          = lag2UD max 0.0
  val maxDelayTime_2  = max_22 min 20.0
  val max_23          = cuspN_1 max 0.0
  val delayTime_5     = max_23 min maxDelayTime_2
  val delayL_1        = DelayL.ar(in_19, maxDelayTime = maxDelayTime_2, delayTime = delayTime_5)
  val max_24          = delayL_0 max 0.0
  val iphase_2        = max_24 min 1.0
  val lFPar_2         = LFPar.ar(freq = 0.01, iphase = iphase_2)
  val freq_5          = lFPar_2 amclip quadN_0
  val in_20           = LeakDC.ar(-203.25075, coeff = 0.995)
  val max_25          = lFPulse_1 max 0.0
  val delayTime_6     = max_25 min 0.22664568
  val c_3             = AllpassN.ar(in_20, maxDelayTime = 0.22664568, delayTime = delayTime_6, decayTime = -4.619783)
  val quadN_1         = QuadN.ar(freq = freq_5, a = 2.1437016, b = 0.0, c = c_3, xi = 0.0034691016)
  val linCongC_0      = LinCongC.ar(freq = freq_5, a = 0.0, c = 0.8250135, m = 0.015739825, xi = 1.0)
  val in_21           = LeakDC.ar(132.21826, coeff = 0.995)
  val delay2_0        = Delay2.ar(in_21)
  val in_22           = LeakDC.ar(7.107886, coeff = 0.995)
  val earlyRefLevel_0 = LPZ2.ar(in_22)
  val in_23           = LeakDC.ar(0.022283768, coeff = 0.995)
  val max_26          = freq_5 max 0.55
  val max_27          = delay2_0 max 0.0
  val revTime_0       = max_27 min 100.0
  val max_28          = lFPar_1 max 0.0
  val damping_1       = max_28 min 1.0
  val roomSize_2      = max_26 min 0.55
  val gVerb           = GVerb.ar(in_23, roomSize = roomSize_2, revTime = revTime_0, damping = damping_1, inputBW = 0.0, spread = 43.0, dryLevel = 0.0, earlyRefLevel = earlyRefLevel_0, tailLevel = -0.0076536634, maxRoomSize = 0.55)
  val in_24           = LeakDC.ar(in_11, coeff = 0.995)
  val max_29          = min_1 max 0.0
  val timeUp_0        = max_29 min 30.0
  val lag3UD          = Lag3UD.ar(in_24, timeUp = timeUp_0, timeDown = 0.0)
  val in_25           = LeakDC.ar(0.14163044, coeff = 0.995)
  val max_30          = gbmanL_0 max 0.0
  val revTime_1       = max_30 min 100.0
  val spread_1        = a_0 max 0.0
  val max_31          = lag_0 max 0.55
  val maxRoomSize_0   = max_31 min 300.0
  val roomSize_3      = Constant(7.107886f) min maxRoomSize_0
  val a_2             = GVerb.ar(in_25, roomSize = roomSize_3, revTime = revTime_1, damping = 0.0, inputBW = 1.5274479E-4, spread = spread_1, dryLevel = quadN_0, earlyRefLevel = 1.5042997E-4, tailLevel = lFPulse_0, maxRoomSize = maxRoomSize_0)
  val in_26           = LeakDC.ar(1.5063983E-5, coeff = 0.995)
  val integrator      = Integrator.ar(in_26, coeff = 0.999)
  val lag_1           = Lag.ar(integrator, time = 0.022008082)
  val excess          = integrator excess earlyRefLevel_0
  val cuspN_2         = CuspN.ar(freq = -0.0076536634, a = a_2, b = integrator, xi = lFDClipNoise)
  val delay2_1        = Delay2.ar(combN_0)
  val absdif          = -4.619783 absdif allpassN_0
  val linCongC_1      = LinCongC.ar(freq = 20.145914, a = min_0, c = 0.7677357, m = 1.0, xi = 0.7677357)
  val max_32          = linCongC_1 max 0.0
  val phase_0         = max_32 min 1.0
  val lFGauss_2       = LFGauss.ar(dur = 1.5274479E-4, width = 0.0, phase = phase_0, loop = 0.0014424388, doneAction = doNothing)
  val in_27           = LeakDC.ar(0.8250135, coeff = 0.995)
  val delay1_2        = Delay1.ar(in_27)
  val gbmanL_1        = GbmanL.ar(freq = 29.838459, xi = 2.1437016, yi = -1.89771E-5)
  val clip2_1         = 1605.479 clip2 gbmanL_1
  val in_28           = LeakDC.ar(0.015739825, coeff = 0.995)
  val max_33          = atan2 max 10.0
  val freq_6          = max_33 min 20000.0
  val hPF             = HPF.ar(in_28, freq = freq_6)
  val sqrsum          = -11.958342 sqrsum hPF
  val in_29           = LeakDC.ar(-4.619783, coeff = 0.995)
  val allpassL        = AllpassL.ar(in_29, maxDelayTime = 7.3190875, delayTime = 0.2, decayTime = -2425.7073)
  val max_34          = ring3 max 0.0
  val h               = max_34 min 0.06
  val in_30           = LorenzL.ar(freq = 0.022283768, s = 9.145937, r = 28.0, b = b_0, h = h, xi = min_0, yi = 7388.521, zi = mod_0)
  val in_31           = LeakDC.ar(in_30, coeff = 0.995)
  val max_35          = x0 max 0.0
  val delayTime_7     = max_35 min 0.015739825
  val combC_2         = CombC.ar(in_31, maxDelayTime = 0.015739825, delayTime = delayTime_7, decayTime = 0.014783192)
  val in_32           = LeakDC.ar(0.0021974712, coeff = 0.995)
  val delayN          = DelayN.ar(in_32, maxDelayTime = 0.8250135, delayTime = 0.8250135)
  val gbmanL_2        = GbmanL.ar(freq = 152.6102, xi = 7.531644, yi = 15.662841)
  val max_36          = lag_0 max 0.0
  val maxDelayTime_3  = max_36 min 20.0
  val max_37          = linCongC_0 max 0.0
  val delayTime_8     = max_37 min maxDelayTime_3
  val allpassN_1      = AllpassN.ar(gbmanL_2, maxDelayTime = maxDelayTime_3, delayTime = delayTime_8, decayTime = decayTime_1)
  val gbmanN          = GbmanN.ar(freq = gbmanL_2, xi = fold2, yi = -0.42427403)
  val lt              = 370.1297 < in_5
  val in_33           = LeakDC.ar(-93.21562, coeff = 0.995)
  val combC_3         = CombC.ar(in_33, maxDelayTime = 0.8250135, delayTime = 0.8250135, decayTime = lag_0)
  val in_34           = LeakDC.ar(0.032853525, coeff = 0.995)
  val max_38          = decayTime_0 max 0.0
  val maxDelayTime_4  = max_38 min 20.0
  val delayTime_9     = Constant(0.0048477706f) min maxDelayTime_4
  val combN_1         = CombN.ar(in_34, maxDelayTime = maxDelayTime_4, delayTime = delayTime_9, decayTime = 0.028330043)
  val in_35           = LeakDC.ar(-0.0028008358, coeff = 0.995)
  val twoZero         = TwoZero.ar(in_35, freq = 440.0, radius = 0.0)
  val mix             = Mix(Seq[GE](syncSaw, ring2, cuspN_0, lFDNoise1, neq, linCongL, rLPF, thresh, combC_0, lFGauss_0, ring4, roundTo, sumsqr, sqrdif, quadL, amclip, plus, latoocarfianC_0, quadC_1, lFPulse_2, latoocarfianC_1, combC_1, twoPole, delayL_1, quadN_1, gVerb, lag3UD, lag_1, excess, cuspN_2, delay2_1, absdif, lFGauss_2, delay1_2, clip2_1, sqrsum, allpassL, combC_2, delayN, allpassN_1, gbmanN, lt, combC_3, combN_1, twoZero))
  val in_36           = Mix.Mono(mix)
  val checkBadValues  = CheckBadValues.ar(in_36, id = 0.0, post = 0.0)
  val gate            = checkBadValues sig_== 0.0
  val in_37           = Gate.ar(in_36, gate = gate)
  val pan2            = Pan2.ar(in_37, pos = 0.0, level = 1.0)
  val sig = pan2
  Out.ar(0, Limiter.ar(LeakDC.ar(sig)) * "amp".kr(0.7))
}

// NOTE: needs a lot of memory

// x.free()
