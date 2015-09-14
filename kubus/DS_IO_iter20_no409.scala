val x = play {
  RandSeed.ir(trig = 1, seed = 56789.0)
  val in_0            = LFDNoise3.ar(500.0)
  val c_0             = AllpassN.ar(in_0, maxDelayTime = 20.0, delayTime = 4.831171, decayTime = 25.59424)
  val in_1            = LeakDC.ar(-1.7009694E-4, coeff = 0.995)
  val a_0             = Decay2.ar(in_1, attack = 0.039164282, release = 25.59424)
  val in_2            = LeakDC.ar(5000.829, coeff = 0.995)
  val onePole         = OnePole.ar(in_2, coeff = 0.999)
  val b_0             = RLPF.ar(onePole, freq = 10.0, rq = 4.831171)
  val quadL           = QuadL.ar(freq = -14.517894, a = a_0, b = b_0, c = c_0, xi = -2576.3137)
  val xi_0            = quadL absdif 4144.69
  val in_3            = LeakDC.ar(25.59424, coeff = 0.995)
  val c_1             = AllpassN.ar(in_3, maxDelayTime = 0.0, delayTime = 0.0, decayTime = onePole)
  val sumsqr          = c_1 sumsqr xi_0
  val in_4            = LeakDC.ar(5000.829, coeff = 0.995)
  val combN           = CombN.ar(in_4, maxDelayTime = 0.0, delayTime = 0.0, decayTime = sumsqr)
  val eq              = 0.07218959 sig_== combN
  val trunc           = 1961.5284 trunc eq
  val in_5            = LeakDC.ar(-14.0, coeff = 0.995)
  val max_0           = trunc max 0.0
  val timeDown        = max_0 min 30.0
  val lag3UD          = Lag3UD.ar(in_5, timeUp = 30.0, timeDown = timeDown)
  val in_6            = LeakDC.ar(lag3UD, coeff = 0.995)
  val oneZero         = OneZero.ar(in_6, coeff = -1.7009694E-4)
  val in_7            = LeakDC.ar(-4.7972018E-4, coeff = 0.995)
  val hPF             = HPF.ar(in_7, freq = 440.0)
  val absdif          = oneZero absdif hPF
  val max_1           = absdif max 0.8
  val coeff_0         = max_1 min 0.99
  val leakDC          = LeakDC.ar(in_0, coeff = coeff_0)
  val roundUpTo       = -5.888803 roundUpTo xi_0
  val in_8            = LFCub.ar(freq = 0.19427142, iphase = 0.0)
  val thresh          = c_1 thresh 25.59424
  val yi_0            = in_8 difsqr thresh
  val in_9            = LeakDC.ar(304.22394, coeff = 0.995)
  val max_2           = c_1 max 10.0
  val freq_0          = max_2 min 20000.0
  val max_3           = lag3UD max 0.01
  val rq_0            = max_3 min 100.0
  val rLPF            = RLPF.ar(in_9, freq = freq_0, rq = rq_0)
  val in_10           = LeakDC.ar(in_8, coeff = 0.995)
  val max_4           = rLPF max 0.0
  val maxDelayTime_0  = max_4 min 20.0
  val max_5           = xi_0 max 0.0
  val delayTime_0     = max_5 min maxDelayTime_0
  val delayL          = DelayL.ar(in_10, maxDelayTime = maxDelayTime_0, delayTime = delayTime_0)
  val cuspN           = CuspN.ar(freq = 93.457, a = -1.7009694E-4, b = 516.29193, xi = 222.07622)
  val lFDNoise3       = LFDNoise3.ar(0.099789865)
  val max_6           = delayL max 0.0
  val maxDelayTime_1  = max_6 min 20.0
  val max_7           = cuspN max 0.0
  val delayTime_1     = max_7 min maxDelayTime_1
  val allpassN        = AllpassN.ar(sumsqr, maxDelayTime = maxDelayTime_1, delayTime = delayTime_1, decayTime = lFDNoise3)
  val a_1             = LinCongC.ar(freq = lag3UD, a = 93.457, c = c_1, m = thresh, xi = -2.2183145E-4)
  val in_11           = LeakDC.ar(lag3UD, coeff = 0.995)
  val bRZ2            = BRZ2.ar(in_11)
  val in_12           = LeakDC.ar(222.07622, coeff = 0.995)
  val delay2          = Delay2.ar(in_12)
  val yi_1            = CuspL.ar(freq = 222.07622, a = a_1, b = -14.517894, xi = -5.888803)
  val in_13           = LeakDC.ar(-0.2082588, coeff = 0.995)
  val b_1             = Delay1.ar(in_13)
  val impulse         = Impulse.ar(freq = 80.66164, phase = 1.0)
  val b_2             = impulse sig_!= 1961.5284
  val in_14           = LeakDC.ar(2.1872935, coeff = 0.995)
  val max_8           = allpassN max 0.0
  val inputBW         = max_8 min 1.0
  val max_9           = rLPF max 0.55
  val maxRoomSize     = max_9 min 300.0
  val roomSize        = Constant(4.831171f) min maxRoomSize
  val gVerb           = GVerb.ar(in_14, roomSize = roomSize, revTime = 0.0, damping = 0.099789865, inputBW = inputBW, spread = 0.0, dryLevel = 2.1872935, earlyRefLevel = 0.12486284, tailLevel = 3991.0703, maxRoomSize = maxRoomSize)
  val max_10          = gVerb max -1.0
  val iphase_0        = max_10 min 1.0
  val lFSaw           = LFSaw.ar(freq = 0.01, iphase = iphase_0)
  val henonN_0        = HenonN.ar(freq = 0.07218959, a = -214.69283, b = b_1, x0 = 304.22394, x1 = lFSaw)
  val freq_1          = SyncSaw.ar(syncFreq = 0.01, sawFreq = 0.19427142)
  val max_11          = quadL max -3.0
  val a_2             = max_11 min 3.0
  val max_12          = xi_0 max 0.5
  val b_3             = max_12 min 1.5
  val max_13          = onePole max 0.5
  val c_2             = max_13 min 1.5
  val latoocarfianL_0 = LatoocarfianL.ar(freq = freq_1, a = a_2, b = b_3, c = c_2, d = lFDNoise3, xi = xi_0, yi = yi_0)
  val in_15           = QuadC.ar(freq = 1647.5853, a = lag3UD, b = lFSaw, c = 80.66164, xi = 222.07622)
  val allpassL        = AllpassL.ar(in_15, maxDelayTime = 0.0025868728, delayTime = 0.0025868728, decayTime = 45.761463)
  val ring2           = allpassL ring2 -2576.3137
  val in_16           = LeakDC.ar(1.5920054E-4, coeff = 0.995)
  val max_14          = a_1 max 10.0
  val freq_2          = max_14 min 20000.0
  val max_15          = in_15 max 0.01
  val rq_1            = max_15 min 100.0
  val bPF             = BPF.ar(in_16, freq = freq_2, rq = rq_1)
  val max_16          = bRZ2 max 0.5
  val b_4             = max_16 min 1.5
  val latoocarfianL_1 = LatoocarfianL.ar(freq = 3151.4739, a = -3.0, b = b_4, c = 0.5, d = -14.0, xi = 1.5710156, yi = yi_1)
  val in_17           = LinCongN.ar(freq = -0.004966832, a = onePole, c = 0.026580907, m = 55.672913, xi = 0.10910526)
  val in_18           = LeakDC.ar(in_17, coeff = 0.995)
  val hPZ1            = HPZ1.ar(in_18)
  val max_17          = a_0 max 0.0
  val iphase_1        = max_17 min 1.0
  val max_18          = freq_1 max 0.0
  val width           = max_18 min 1.0
  val varSaw          = VarSaw.ar(freq = 0.01, iphase = iphase_1, width = width)
  val henonN_1        = HenonN.ar(freq = 0.77273506, a = 1.0, b = b_2, x0 = thresh, x1 = -4.4542315E-4)
  val mix             = Mix(Seq[GE](leakDC, roundUpTo, delay2, henonN_0, latoocarfianL_0, ring2, bPF, latoocarfianL_1, hPZ1, varSaw, henonN_1))
  val in_19           = Mix.Mono(mix)
  val checkBadValues  = CheckBadValues.ar(in_19, id = 0.0, post = 0.0)
  val gate            = checkBadValues sig_== 0.0
  val in_20           = Gate.ar(in_19, gate = gate)
  val pan2            = Pan2.ar(in_20, pos = 0.0, level = 1.0)
  val sig = pan2 // Resonz.ar(pan2, "freq".kr(777), rq = 1)
  Out.ar(0, Limiter.ar(LeakDC.ar(sig)) * "amp".kr(0.2))
}

/*---
*/
