play {
  RandSeed.ir(trig = 1, seed = 56789.0)
  val in_0            = LeakDC.ar(0.0, coeff = 0.995)
  val freq_0          = RLPF.ar(in_0, freq = 10.0, rq = 0.014865998)
  val max_0           = freq_0 max 0.01
  val freq_1          = max_0 min 20000.0
  val varSaw_0        = VarSaw.ar(freq = freq_1, iphase = 0.0, width = 0.0)
  val in_1            = LeakDC.ar(varSaw_0, coeff = 0.995)
  val freq_2          = DelayL.ar(in_1, maxDelayTime = 0.0, delayTime = 0.0)
  val in_2            = LeakDC.ar(0.014865998, coeff = 0.995)
  val max_1           = freq_0 max 0.0
  val time_0          = max_1 min 30.0
  val freq_3          = Lag.ar(in_2, time = time_0)
  val in_3            = LFDNoise0.ar(Seq.fill(16)(freq_3))
  val in_4            = LeakDC.ar(in_3, coeff = 0.995)
  val lag3_0          = Lag3.ar(in_4, time = 0.0)
  val bPZ2            = BPZ2.ar(lag3_0)
  val min_0           = Constant(0.21584345f) min bPZ2
  val in_5            = LeakDC.ar(988.4579, coeff = 0.995)
  val max_2           = bPZ2 max 0.0
  val maxDelayTime_0  = max_2 min 20.0
  val max_3           = min_0 max 0.0
  val delayTime_0     = max_3 min maxDelayTime_0
  val combC           = CombC.ar(in_5, maxDelayTime = maxDelayTime_0, delayTime = delayTime_0, decayTime = 1.0)
  val max_4           = combC max 0.5
  val b_0             = max_4 min 1.5
  
  val p1 = "p1".kr(-2.7041092)
  val p2 = "p2".kr(0.5)
  val p3 = "p3".kr(0.014865998)
  
  val latoocarfianL   = LatoocarfianL.ar(freq = freq_2, 
    a = p1, b = b_0, 
    c = p2, 
    d = p3, 
    xi = Seq.fill(16)(22.448647 + math.random * 4), 
    yi = -0.22611034)
  val min_1           = freq_2 min 988.4579
  val max_5           = varSaw_0 max 0.0
  val iphase_0        = max_5 min 4.0
  val lFTri           = LFTri.ar(freq = 0.014865998, iphase = iphase_0)
  val in_6            = LeakDC.ar(lFTri, coeff = 0.995)
  val in_7            = AllpassN.ar(in_6, maxDelayTime = 20.0, delayTime = 0.0, decayTime = 128.71986)
  val lag             = Lag.ar(in_7, time = 0.0)
  val max_6           = combC max 1.0
  val length          = max_6 min 44100.0
  val runningSum      = RunningSum.ar(lFTri, length = length)
  val in_8            = LeakDC.ar(0.21584345, coeff = 0.995)
  val x1_0            = LPZ2.ar(in_8)
  val max_7           = x1_0 max 0.01
  val freq_4          = max_7 min 20000.0
  val x0              = LFSaw.ar(freq = freq_4, iphase = -0.06592435)
  val henonC          = HenonC.ar(freq = freq_0, 
    a = Seq.fill(16)(-0.22611034 + math.random * 0.01), 
    b = lFTri, x0 = x0, x1 = x1_0)
  val lFPulse         = LFPulse.ar(freq = 0.01, iphase = 0.0, width = 1.0)
  val in_9            = LeakDC.ar(in_3, coeff = 0.995)
  val max_8           = lFPulse max 0.01
  val rq_0            = max_8 min 100.0
  val bPF             = BPF.ar(in_9, freq = 10.0, rq = rq_0)
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
  val varSaw_1        = VarSaw.ar(freq = 12.434091, iphase = iphase_1, width = 0.28425974)
  val roundUpTo       = varSaw_0 roundUpTo varSaw_1
  val in_12           = LeakDC.ar(-0.22611034, coeff = 0.995)
  val lPZ1            = LPZ1.ar(in_12)
  val in_13           = LeakDC.ar(38.6555, coeff = 0.995)
  val decay2          = Decay2.ar(in_13, attack = 0.01, release = 1.0)
  val in_14           = LeakDC.ar(0.21584345, coeff = 0.995)
  val oneZero         = OneZero.ar(in_14, coeff = 1.0)
  val in_15           = LeakDC.ar(-0.22611034, coeff = 0.995)
  val max_13          = bPZ2 max 10.0
  val freq_5          = max_13 min 20000.0
  val rLPF            = RLPF.ar(in_15, freq = freq_5, rq = 0.01)
  val excess          = rLPF excess henonC
  val in_16           = LeakDC.ar(-0.0036990615, coeff = 0.995)
  val max_14          = excess max 0.0
  val timeUp          = max_14 min 30.0
  val lag2UD          = Lag2UD.ar(in_16, timeUp = timeUp, timeDown = 0.0)
  val in_17           = LeakDC.ar(-5.0879025, coeff = 0.995)
  val decay           = Decay.ar(in_17, time = 3.750018)
  val in_18           = LeakDC.ar(-5.0879025, coeff = 0.995)
  val twoPole         = TwoPole.ar(in_18, freq = 10.0, radius = 1.0)
  val in_19           = LeakDC.ar(-5.0879025, coeff = 0.995)
  val max_15          = min_1 max 0.0
  val delayTime_2     = max_15 min 20.0
  val combN           = CombN.ar(in_19, maxDelayTime = 20.0, delayTime = delayTime_2, decayTime = -0.4435867)
  val max_16          = twoPole max 0.01
  val syncFreq        = max_16 min 20000.0
  val max_17          = combN max 0.01
  val sawFreq_0       = max_17 min 20000.0
  val b_1             = SyncSaw.ar(syncFreq = syncFreq, sawFreq = sawFreq_0)
  val max_18          = runningSum max 0.01
  val sawFreq_1       = max_18 min 20000.0
  val syncSaw         = SyncSaw.ar(syncFreq = 0.01, sawFreq = sawFreq_1)
  val x1_1            = LinCongC.ar(freq = -0.0036990615, a = lag3_0, 
    c = Seq.fill(16)(3.4448621 + math.random * 0.1), 
    m = 22.448647, 
    xi = 0.051471394)
  val freq_6          = RunningSum.ar(-22.448647, length = 1.0)
  val henonN          = HenonN.ar(freq = freq_6, 
    a = Seq.fill(16)(0.014865998 + math.random * 0.001), 
    b = b_1, x0 = 128.71986, x1 = x1_1)
  val henonL          = HenonL.ar(freq = -0.4435867, 
    a = Seq.fill(16)(0.003213149 + math.random * 0.0001), 
    b = 4.6396217, x0 = varSaw_0, x1 = lag3_0)
  val hypot           = henonL hypot 988.4579
  val in_20           = LeakDC.ar(1.0469707, coeff = 0.995)
  val hPZ1            = HPZ1.ar(in_20)
  val max_19          = freq_6 max 0.0
  val phase           = max_19 min 1.0
  val impulse         = Impulse.ar(freq = 1.0469707, phase = phase)
  val mix             = Mix(Seq[GE](latoocarfianL, lag, bPF, roundUpTo, lPZ1, decay2, oneZero, lag2UD, decay, syncSaw, henonN, hypot, hPZ1, impulse))
  val in_21           = mix // Mix.Mono(mix)
  val checkBadValues  = CheckBadValues.ar(in_21, id = 0.0, post = 0.0)
  val gate            = checkBadValues sig_== 0.0
  val in_22           = Gate.ar(in_21, gate = gate)
  val pan2            = in_22 // Pan2.ar(in_22, pos = 0.0, level = 1.0)
  // ConfigOut(pan2)
  val amp = "amp".kr(1.0)
  val sig = Limiter.ar(LeakDC.ar(pan2)) * amp
  Out.ar(0, sig)
}
