implicit class BangBang[A](val in: A) extends AnyVal {
  def !! (n: Int): Vector[A] = Vector.fill(n)(in)
}

val x = play {
  val numChannels = 4
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
  Out.ar(0, Limiter.ar(LeakDC.ar(sig)) * "amp".kr(0.1))
}

// x.set("freq" -> 777)

/*---
*/
