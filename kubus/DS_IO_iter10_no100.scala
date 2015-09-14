val x = play {
  RandSeed.ir(trig = 1, seed = 56789.0)
  val b_0             = SyncSaw.ar(syncFreq = 872.9059, sawFreq = 0.07972072)
  val lFPulse_0       = LFPulse.ar(freq = 440.0, iphase = 1.0, width = 1.0)
  val lFPar_0         = LFPar.ar(freq = 0.01, iphase = 0.0)
  val clip2_0         = lFPar_0 clip2 0.7677357
  val decayTime_0     = lFPulse_0 >= clip2_0
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
  val in_3            = LeakDC.ar(-1.89771E-5, coeff = 0.995)
  val delay1_0        = Delay1.ar(in_3)
  val ring3           = lFPulse_1 ring3 8.916748
  val ring4           = delay1_0 ring4 ring3
  val max_2           = delay1_0 max 0.0
  val width_0         = max_2 min 1.0
  val lFGauss_0       = LFGauss.ar(dur = 5.0E-5, width = width_0, phase = 1.0, loop = 1644.8522, doneAction = doNothing)
  val sumsqr          = lFGauss_0 sumsqr 117.86163
  val a_0             = LFDClipNoise.ar(0.8250135)
  val fold2           = -697.5932 fold2 lFPar_0
  val bitAnd          = fold2 & 0.018849522
  val in_4            = LeakDC.ar(fold2, coeff = 0.995)
  val b_1             = Delay2.ar(in_4)
  val ring2_0         = fold2 ring2 0.8250135
  val xi_0            = fold2 wrap2 1.0
  val thresh_0        = a_0 thresh fold2
  val in_5            = LeakDC.ar(7.3190875, coeff = 0.995)
  val max_3           = lFPulse_0 max 0.0
  val delayTime_2     = max_3 min 0.0
  val delayL_0        = DelayL.ar(in_5, maxDelayTime = 0.0, delayTime = delayTime_2)
  val quadN_0         = QuadN.ar(freq = 0.0, a = 0.0, b = 0.0, c = 0.0, xi = 0.8250135)
  val min             = Constant(0.0f) min quadN_0
  val freq_0          = CuspL.ar(freq = delayL_0, a = lFPar_0, b = min, xi = 1.5063983E-5)
  val xi_1            = LFDNoise1.ar(0.66143084)
  val neq             = freq_0 sig_!= xi_1
  val linCongL        = LinCongL.ar(freq = freq_0, a = -13.891433, c = 1.5042997E-4, m = 0.080567405, xi = xi_0)
  val rLPF            = RLPF.ar(delayL_0, freq = 10.0, rq = 0.01)
  val max_4           = thresh_0 max 0.01
  val freq_1          = max_4 min 20000.0
  val max_5           = delayL_0 max 0.0
  val iphase_0        = max_5 min 1.0
  val lFPar_1         = LFPar.ar(freq = freq_1, iphase = iphase_0)
  val thresh_1        = 133.04327 thresh delayL_0
  val in_6            = LeakDC.ar(1.0, coeff = 0.995)
  val lag_0           = Lag.ar(in_6, time = 0.0)
  val freq_2          = Nyquist()
  val freq_3          = LinCongN.ar(freq = freq_2, a = -0.36279276, c = lFPulse_0, m = 20.145914, xi = 0.10357987)
  val sqrdif          = freq_3 sqrdif -2425.7073
  val plus_0          = freq_3 + quadN_0
  val lFDClipNoise    = LFDClipNoise.ar(7.531644)
  val cuspN_0         = CuspN.ar(freq = freq_3, a = lFDClipNoise, b = lFPulse_0, xi = 0.8250135)
  val in_7            = LeakDC.ar(20.145914, coeff = 0.995)
  val max_6           = freq_3 max 0.0
  val maxDelayTime_0  = max_6 min 20.0
  val max_7           = lag_0 max 0.0
  val delayTime_3     = max_7 min maxDelayTime_0
  val delayC          = DelayC.ar(in_7, maxDelayTime = maxDelayTime_0, delayTime = delayTime_3)
  val b_2             = LFDClipNoise.ar(-4.619783)
  val c_0             = QuadN.ar(freq = 0.0, a = lFPar_1, b = b_2, c = 0.8250135, xi = 199.98691)
  val ring2_1         = 1.0 ring2 delayC
  val quadC           = QuadC.ar(freq = 0.0, a = 7.3190875, b = -0.008399427, c = quadN_0, xi = 0.0)
  val in_8            = LinCongC.ar(freq = 0.022283768, a = quadC, c = 743.26575, m = 41.52797, xi = quadC)
  val plus_1          = in_8 + 0.09049736
  val in_9            = LeakDC.ar(in_8, coeff = 0.995)
  val b_3             = BPZ2.ar(in_9)
  val in_10           = LeakDC.ar(7388.521, coeff = 0.995)
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
  val in_13           = GVerb.ar(in_12, roomSize = roomSize_0, revTime = 0.0, damping = damping_0, inputBW = 0.0, spread = spread_0, dryLevel = 1.0, earlyRefLevel = 0.8250135, tailLevel = -11.958342, maxRoomSize = 0.8250135)
  val delay1_1        = Delay1.ar(in_13)
  val max_15          = delay1_1 max -3.0
  val a_1             = max_15 min 3.0
  val latoocarfianC_1 = LatoocarfianC.ar(freq = 1.5274479E-4, a = a_1, b = 0.5, c = 0.5, d = -2.821052E-4, xi = xi_1, yi = lFPar_1)
  val decayTime_1     = 1.0 ring4 fold2
  val in_14           = LeakDC.ar(0.028330043, coeff = 0.995)
  val combC_1         = CombC.ar(in_14, maxDelayTime = 0.090563826, delayTime = 0.090563826, decayTime = decayTime_1)
  val in_15           = LeakDC.ar(4542.6772, coeff = 0.995)
  val max_16          = lFPulse_0 max 0.0
  val timeDown_0      = max_16 min 30.0
  val lag2UD          = Lag2UD.ar(in_15, timeUp = 0.080567405, timeDown = timeDown_0)
  val in_16           = LeakDC.ar(0.008464628, coeff = 0.995)
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
  val in_17           = LeakDC.ar(132.21826, coeff = 0.995)
  val delay2_0        = Delay2.ar(in_17)
  val in_18           = LeakDC.ar(7.107886, coeff = 0.995)
  val earlyRefLevel_0 = LPZ2.ar(in_18)
  val gbmanL_0        = GbmanL.ar(freq = 29.838459, xi = 2.1437016, yi = -1.89771E-5)
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
  val gVerb_0         = GVerb.ar(in_20, roomSize = roomSize_1, revTime = revTime_0, damping = damping_1, inputBW = 0.0, spread = 43.0, dryLevel = 0.0, earlyRefLevel = earlyRefLevel_0, tailLevel = -0.008399427, maxRoomSize = 0.55)
  val in_21           = LeakDC.ar(0.007981387, coeff = 0.995)
  val max_24          = atan2 max 0.55
  val roomSize_2      = max_24 min 300.0
  val gVerb_1         = GVerb.ar(in_21, roomSize = roomSize_2, revTime = 0.0, damping = 1.0, inputBW = 0.66143084, spread = 43.0, dryLevel = lFPar_1, earlyRefLevel = 13.28049, tailLevel = 0.5, maxRoomSize = 300.0)
  val in_22           = LeakDC.ar(1.5042997E-4, coeff = 0.995)
  val max_25          = ring2_1 max 0.0
  val revTime_1       = max_25 min 100.0
  val spread_1        = a_0 max 0.0
  val max_26          = lag_0 max 0.55
  val maxRoomSize_0   = max_26 min 300.0
  val roomSize_3      = Constant(7.107886f) min maxRoomSize_0
  val a_2             = GVerb.ar(in_22, roomSize = roomSize_3, revTime = revTime_1, damping = 0.0, inputBW = 1.5274479E-4, spread = spread_1, dryLevel = quadN_0, earlyRefLevel = -5452.869, tailLevel = lFPulse_0, maxRoomSize = maxRoomSize_0)
  val cuspN_1         = CuspN.ar(freq = -0.008399427, a = a_2, b = lorenzL, xi = lFDClipNoise)
  val in_23           = LeakDC.ar(0.0028571805, coeff = 0.995)
  val delay2_1        = Delay2.ar(in_23)
  val geq             = delay2_1 >= 41.52797
  val freq_5          = -4.619783 absdif plus_0
  val cuspN_2         = CuspN.ar(freq = freq_5, a = a_0, b = lFPar_0, xi = lag_0)
  val linCongC_1      = LinCongC.ar(freq = 20.145914, a = min, c = 0.7677357, m = 1.0, xi = 0.7677357)
  val max_27          = linCongC_1 max 0.0
  val phase_0         = max_27 min 1.0
  val lFGauss_1       = LFGauss.ar(dur = 0.0014424388, width = 0.0, phase = phase_0, loop = 1.5274479E-4, doneAction = doNothing)
  val in_24           = LeakDC.ar(0.8250135, coeff = 0.995)
  val delay1_2        = Delay1.ar(in_24)
  val clip2_1         = 1605.479 clip2 gbmanL_0
  val amclip          = 0.0 amclip fold2
  val freq_6          = Nyquist()
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
  val in_28           = LeakDC.ar(0.0021974712, coeff = 0.995)
  val delayN          = DelayN.ar(in_28, maxDelayTime = 0.8250135, delayTime = 0.8250135)
  val gbmanL_1        = GbmanL.ar(freq = 0.022008082, xi = 7.531644, yi = 15.662841)
  val in_29           = LeakDC.ar(gbmanL_1, coeff = 0.995)
  val max_30          = lag_0 max 0.0
  val maxDelayTime_2  = max_30 min 20.0
  val max_31          = linCongC_0 max 0.0
  val delayTime_6     = max_31 min maxDelayTime_2
  val allpassN        = AllpassN.ar(in_29, maxDelayTime = maxDelayTime_2, delayTime = delayTime_6, decayTime = decayTime_0)
  val gbmanN          = GbmanN.ar(freq = gbmanL_1, xi = fold2, yi = 0.026794823)
  val lt              = 370.1297 < in_2
  val in_30           = LeakDC.ar(-93.21562, coeff = 0.995)
  val combC_3         = CombC.ar(in_30, maxDelayTime = 0.8250135, delayTime = 0.8250135, decayTime = lag_0)
  val in_31           = LeakDC.ar(0.032853525, coeff = 0.995)
  val max_32          = decayTime_1 max 0.0
  val maxDelayTime_3  = max_32 min 20.0
  val delayTime_7     = Constant(0.25663114f) min maxDelayTime_3
  val combN           = CombN.ar(in_31, maxDelayTime = maxDelayTime_3, delayTime = delayTime_7, decayTime = 0.028330043)
  val in_32           = LeakDC.ar(-0.0028008358, coeff = 0.995)
  val twoZero         = TwoZero.ar(in_32, freq = 440.0, radius = 0.0)
  val in_33           = LeakDC.ar(-203.25075, coeff = 0.995)
  val max_33          = lFPulse_1 max 0.0
  val delayTime_8     = max_33 min 0.22664568
  val c_2             = AllpassN.ar(in_33, maxDelayTime = 0.22664568, delayTime = delayTime_8, decayTime = -4.619783)
  val quadN_1         = QuadN.ar(freq = 3773.042, a = 2.1437016, b = 0.0, c = c_2, xi = 0.0034691016)
  val in_34           = LeakDC.ar(0.36781657, coeff = 0.995)
  val max_34          = fold2 max 10.0
  val freq_7          = max_34 min 20000.0
  val lPF             = LPF.ar(in_34, freq = freq_7)
  val mix             = Mix(Seq[GE](combC_0, onePole, ring4, sumsqr, bitAnd, ring2_0, neq, linCongL, rLPF, thresh_1, sqrdif, plus_1, latoocarfianC_0, quadL_0, lag3UD, lFPulse_2, latoocarfianC_1, combC_1, delayL_1, lag_1, excess, gVerb_0, gVerb_1, cuspN_1, geq, cuspN_2, lFGauss_1, delay1_2, clip2_1, amclip, quadL_1, allpassL, combC_2, delayN, allpassN, gbmanN, lt, combC_3, combN, twoZero, quadN_1, lPF))
  val in_35           = Mix.Mono(mix)
  val checkBadValues  = CheckBadValues.ar(in_35, id = 0.0, post = 0.0)
  val gate            = checkBadValues sig_== 0.0
  val in_36           = Gate.ar(in_35, gate = gate)
  val pan2            = Pan2.ar(in_36, pos = 0.0, level = 1.0)
  val sig = pan2
  Out.ar(0, Limiter.ar(LeakDC.ar(sig)) * "amp".kr(0.7))
}

// NOTE: needs a lot of memory

// x.free()
