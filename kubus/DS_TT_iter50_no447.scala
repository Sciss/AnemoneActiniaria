val x = play {
  RandSeed.ir(trig = 1, seed = 56789.0)
  val in_0            = LeakDC.ar(0.9768148, coeff = 0.995)
  val decay_0         = Decay.ar(in_0, time = 0.0073606772)
  val in_1            = LeakDC.ar(-0.0064726463, coeff = 0.995)
  val yi              = Lag2UD.ar(in_1, timeUp = 0.0, timeDown = 0.0)
  val freq_0          = yi sumsqr 797.2432
  val in_2            = LeakDC.ar(3824.141, coeff = 0.995)
  val a_0             = BPF.ar(in_2, freq = 10.0, rq = 0.01)
  val freq_1          = GbmanL.ar(freq = decay_0, 
    xi = Seq.fill(16)(-0.05771263 + math.random * 0.002), 
    yi = yi)
  val leq             = freq_1 <= 440.0
  val in_3            = 47.74325 | leq
  val min_0           = in_3 min leq
  
  val p1 = "p1".kr(0.01)
  
  val c_0             = LFPulse.ar(freq = p1, iphase = 1.0, width = 0.5)
  val in_4            = LinCongC.ar(freq = freq_1, a = min_0, c = c_0, m = decay_0, xi = 20.127739)
  val in_5            = LeakDC.ar(884.81964, coeff = 0.995)
  val lag2UD          = Lag2UD.ar(in_5, timeUp = 1.0, timeDown = 0.0)
  val min_1           = Constant(1482.7852f) min lag2UD
  val in_6            = LeakDC.ar(651.0991, coeff = 0.995)
  val decayTime       = BRZ2.ar(in_6)
  val in_7            = LeakDC.ar(in_4, coeff = 0.995)
  val max_0           = lag2UD max 0.0
  val maxDelayTime_0  = max_0 min 20.0
  val max_1           = min_1 max 0.0
  val delayTime_0     = max_1 min maxDelayTime_0
  val combL_0         = CombL.ar(in_7, maxDelayTime = maxDelayTime_0, delayTime = delayTime_0, decayTime = decayTime)
  val m_0             = LinCongC.ar(freq = freq_0, a = a_0, c = leq, m = 41.963078, xi = 41.963078)
  val in_8            = LeakDC.ar(0.17434232, coeff = 0.995)
  val hPF             = HPF.ar(in_8, freq = 440.0)
  val d_0             = hPF roundUpTo -0.0064726463
  val in_9            = LeakDC.ar(0.17434232, coeff = 0.995)
  val freq_2          = HPF.ar(in_9, freq = 440.0)
  val cuspL           = CuspL.ar(freq = freq_2, a = 10.325185, b = -8.1856985, xi = 0.0026523154)
  val in_10           = LeakDC.ar(2309.9202, coeff = 0.995)
  val b_0             = Lag3.ar(in_10, time = 30.0)
  val in_11           = LeakDC.ar(in_3, coeff = 0.995)
  val max_2           = freq_2 max 0.0
  val time_0          = max_2 min 30.0
  val freq_3          = Lag2.ar(in_11, time = time_0)
  val max_3           = m_0 max 0.5
  val c_1             = max_3 min 1.5
  val latoocarfianN_0 = LatoocarfianN.ar(freq = freq_3, a = 1.0, b = 1.5, c = c_1, d = d_0, xi = 41.963078, yi = 440.0)
  val max_4           = min_0 max 0.01
  val syncFreq        = max_4 min 20000.0
  val syncSaw         = SyncSaw.ar(syncFreq = syncFreq, sawFreq = 0.5)
  val min_2           = min_0 min b_0
  val in_12           = LeakDC.ar(0.9768148, coeff = 0.995)
  val max_5           = b_0 max 0.0
  val maxDelayTime_1  = max_5 min 20.0
  val max_6           = min_2 max 0.0
  val delayTime_1     = max_6 min maxDelayTime_1
  val delayN          = DelayN.ar(in_12, maxDelayTime = maxDelayTime_1, delayTime = delayTime_1)
  val in_13           = LeakDC.ar(928.6766, coeff = 0.995)
  val freq_4          = LPZ1.ar(in_13)
  val in_14           = LeakDC.ar(59.065243, coeff = 0.995)
  val a_1             = BPZ2.ar(in_14)
  val xi_0            = LFDNoise1.ar(3824.141)
  val c_2             = LinCongL.ar(freq = freq_4, a = a_1, c = syncSaw, m = m_0, xi = xi_0)
  val a_2             = LFDNoise3.ar(-0.0064726463)
  val difsqr          = a_2 difsqr xi_0
  val in_15           = LeakDC.ar(440.0, coeff = 0.995)
  val bRZ2            = BRZ2.ar(in_15)
  val quadL           = QuadL.ar(freq = 0.0026523154, a = a_2, b = leq, c = c_2, xi = leq)
  val lFDNoise0       = LFDNoise0.ar(0.14209396)
  val in_16           = LeakDC.ar(1.2026463, coeff = 0.995)
  val freq_5          = Integrator.ar(in_16, coeff = -0.999)
  val in_17           = HenonC.ar(freq = freq_5, a = 0.9768148, b = b_0, x0 = 0.1616343, x1 = -2.7146428)
  val in_18           = LeakDC.ar(in_17, coeff = 0.995)
  val combL_1         = CombL.ar(in_18, maxDelayTime = 0.0, delayTime = 0.0, decayTime = min_0)
  val in_19           = LeakDC.ar(68.532, coeff = 0.995)
  val decay_1         = Decay.ar(in_19, time = 0.9074742)
  val max_7           = lag2UD max 0.01
  val freq_6          = max_7 min 20000.0
  val lFPar           = LFPar.ar(freq = freq_6, iphase = 0.0)
  val hypot           = 405.12585 hypot a_0
  val min_3           = Constant(-2.8190536f) min syncSaw
  val cuspN           = CuspN.ar(freq = 41.902702, a = 1.6799022, b = 0.003201534, xi = syncSaw)
  val in_20           = LeakDC.ar(1.0, coeff = 0.995)
  val lag2            = Lag2.ar(in_20, time = 0.17434232)
  val absdif          = lag2 absdif 41.963078
  val in_21           = LeakDC.ar(3824.141, coeff = 0.995)
  val d_1             = Delay1.ar(in_21)
  val max_8           = a_0 max -3.0
  val a_3             = max_8 min 3.0
  val latoocarfianN_1 = LatoocarfianN.ar(freq = -0.0049043894, a = a_3, b = 1.5, c = 0.9768148, d = d_1, xi = decay_0, yi = 928.6766)
  val in_22           = LeakDC.ar(41.963078, coeff = 0.995)
  val max_9           = latoocarfianN_1 max 0.0
  val pitchDispersion = max_9 min 1.0
  val pitchShift      = PitchShift.ar(in_22, winSize = 1.0, pitchRatio = 4.0, pitchDispersion = pitchDispersion, timeDispersion = 0.0)
  val in_23           = 0.0026092983 amclip xi_0
  val runningSum      = RunningSum.ar(in_23, length = 47.74325)
  
  // val ca = "ca".kr(Vector.fill(17)(1f))
  
  val mix             = Mix((Seq[GE](
    difsqr, cuspN, runningSum
  ): GE)) // * ca)
  val in_24           = mix // Mix.Mono(mix)
  val checkBadValues  = CheckBadValues.ar(in_24, id = 0.0, post = 0.0)
  val gate            = checkBadValues sig_== 0.0
  val in_25           = Gate.ar(in_24, gate = gate)
  val sig = in_25
  
  Out.ar(0, Limiter.ar(LeakDC.ar(sig)) * "amp".kr(1))
}

/*---
                    // 1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17
  x.set("ca" -> Vector(1f,1f,1f,1f,0f,1f,1f,1f,1f,1f,1f,1f,1f,0f,1f,1f,0f))

  // 5  fiep rausch
  // 14 peo peo
  // 17 rauschen
  

*/