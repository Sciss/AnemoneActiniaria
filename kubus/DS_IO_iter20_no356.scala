val x = play {
  RandSeed.ir(trig = 1, seed = 56789.0)
  val lFDNoise3_0     = LFDNoise3.ar(0.93811005)
  val in_0            = LeakDC.ar(0.93811005, coeff = 0.995)
  val twoZero_0       = TwoZero.ar(in_0, freq = 10.0, radius = 1.0)
  val sqrsum          = twoZero_0 sqrsum 0.93811005
  val in_1            = LeakDC.ar(0.93811005, coeff = 0.995)
  val max_0           = twoZero_0 max 0.0
  val radius_0        = max_0 min 1.0
  val a_0             = TwoPole.ar(in_1, freq = 10.0, radius = radius_0)
  val in_2            = LinCongN.ar(freq = 27.382282, a = a_0, c = 0.09420269, m = 6208.715, xi = 0.93811005)
  val ring1           = a_0 ring1 in_2
  val ring2           = ring1 ring2 6208.715
  val difsqr          = a_0 difsqr twoZero_0
  val delay1_0        = Delay1.ar(difsqr)
  val ring4           = a_0 ring4 0.0
  val xi_0            = a_0 amclip 4321.0586
  val freq_0          = RHPF.ar(in_2, freq = 95.52773, rq = 0.01)
  val lFCub           = LFCub.ar(freq = 0.01, iphase = 1.0)
  val lFDNoise3_1     = LFDNoise3.ar(lFCub)
  val in_3            = LeakDC.ar(0.1128317, coeff = 0.995)
  val max_1           = lFCub max 0.0
  val timeUp          = max_1 min 30.0
  val max_2           = freq_0 max 0.0
  val timeDown        = max_2 min 30.0
  val yi_0            = LagUD.ar(in_3, timeUp = timeUp, timeDown = timeDown)
  val in_4            = StandardN.ar(freq = 0.008342929, k = lFDNoise3_1, xi = 0.5, yi = yi_0)
  val max_3           = lFDNoise3_1 max 0.8
  val coeff_0         = max_3 min 0.99
  val in_5            = LeakDC.ar(lFDNoise3_1, coeff = coeff_0)
  val yi_1            = -0.7252769 & in_5
  val max_4           = a_0 max 0.0
  val width_0         = max_4 min 1.0
  val lFGauss_0       = LFGauss.ar(dur = 0.1128317, width = width_0, phase = 0.0, loop = -0.30011576, doneAction = doNothing)
  val max_5           = in_4 max -3.0
  val a_1             = max_5 min 3.0
  val max_6           = yi_1 max 0.5
  val b               = max_6 min 1.5
  val freq_1          = LatoocarfianN.ar(freq = freq_0, a = a_1, b = b, c = 0.5, d = -25.763144, xi = 6208.715, yi = lFGauss_0)
  val max_7           = yi_1 max -3.0
  val a_2             = max_7 min 3.0
  val latoocarfianC   = LatoocarfianC.ar(freq = freq_1, a = a_2, b = 1.5, c = 0.5, d = 0.1128317, xi = xi_0, yi = lFCub)
  val bRF             = BRF.ar(in_2, freq = 10.0, rq = 95.52773)
  val in_6            = LeakDC.ar(lFDNoise3_1, coeff = 0.995)
  val decay           = Decay.ar(in_6, time = 7.360578)
  val delay1_1        = Delay1.ar(in_5)
  val wrap2           = 409.47137 wrap2 in_5
  val eq              = 0.93811005 sig_== decay
  val varSaw          = VarSaw.ar(freq = 0.01, iphase = 0.0, width = 0.5)
  val in_7            = LeakDC.ar(0.03103786, coeff = 0.995)
  val max_8           = difsqr max 0.0
  val maxDelayTime    = max_8 min 20.0
  val delayTime       = Constant(203.33731f) min maxDelayTime
  val freq_2          = CombL.ar(in_7, maxDelayTime = maxDelayTime, delayTime = delayTime, decayTime = -61.093445)
  val in_8            = LFDNoise0.ar(freq_2)
  val in_9            = LeakDC.ar(in_8, coeff = 0.995)
  val lPZ2            = LPZ2.ar(in_9)
  val geq             = lPZ2 >= ring1
  val in_10           = LeakDC.ar(in_4, coeff = 0.995)
  val max_9           = wrap2 max 0.0
  val radius_1        = max_9 min 1.0
  val twoZero_1       = TwoZero.ar(in_10, freq = 10.0, radius = radius_1)
  val in_11           = LeakDC.ar(0.03103786, coeff = 0.995)
  val max_10          = in_5 max 0.0
  val spread          = max_10 min 43.0
  val maxRoomSize     = lFDNoise3_1 max 0.55
  val roomSize        = Constant(6.706537f) min maxRoomSize
  val gVerb           = GVerb.ar(in_11, roomSize = roomSize, revTime = 0.93811005, damping = 1.0, inputBW = 5.222734E-4, spread = spread, dryLevel = lFCub, earlyRefLevel = difsqr, tailLevel = sqrsum, maxRoomSize = maxRoomSize)
  val gbmanN          = GbmanN.ar(freq = 95.52773, xi = 145.82329, yi = sqrsum)
  val neq             = 4321.0586 sig_!= gbmanN
  val in_12           = LeakDC.ar(4321.0586, coeff = 0.995)
  val delayL          = DelayL.ar(in_12, maxDelayTime = 0.0, delayTime = 0.0)
  val loop_0          = 0.008342929 pow a_0
  val max_11          = yi_0 max 0.0
  val width_1         = max_11 min 1.0
  val lFGauss_1       = LFGauss.ar(dur = 100.0, width = width_1, phase = 0.0, loop = loop_0, doneAction = doNothing)
  val max_12          = a_0 max 0.0
  val h               = max_12 min 0.06
  val in_13           = LorenzL.ar(freq = 1403.8345, s = 0.03103786, r = 145.82329, b = lFGauss_0, h = h, xi = 6.706537, yi = yi_1, zi = -0.6822276)
  val lag             = Lag.ar(in_13, time = 0.0)
  val gt              = 203.33731 > gbmanN
  val in_14           = LeakDC.ar(4321.0586, coeff = 0.995)
  val max_13          = bRF max 0.0
  val radius_2        = max_13 min 1.0
  val twoZero_2       = TwoZero.ar(in_14, freq = 10.0, radius = radius_2)
  val mix             = Mix(Seq[GE](lFDNoise3_0, ring2, delay1_0, ring4, latoocarfianC, delay1_1, eq, varSaw, geq, twoZero_1, gVerb, neq, delayL, lFGauss_1, lag, gt, twoZero_2))
  val in_15           = Mix.Mono(mix)
  val checkBadValues  = CheckBadValues.ar(in_15, id = 0.0, post = 0.0)
  val gate            = checkBadValues sig_== 0.0
  val in_16           = Gate.ar(in_15, gate = gate)
  val pan2            = Pan2.ar(in_16, pos = 0.0, level = 1.0)
  val sig = pan2 // Resonz.ar(pan2, "freq".kr(777), rq = 1)
  Out.ar(0, Limiter.ar(LeakDC.ar(sig)) * "amp".kr(0.2))
}

/*---
*/
