play {
  RandSeed.ir(trig = 1, seed = 56789.0)
  val in_0            = LeakDC.ar(299.82324, coeff = 0.995)
  val bRF             = BRF.ar(in_0, freq = 4508.6934, rq = 0.14796029)
  val standardL_0     = StandardL.ar(freq = 299.82324, k = 25.466032, xi = 4508.6934, yi = 0.00913873)
  val x0              = LFCub.ar(freq = 0.01, iphase = 0.15674612)
  val henonN          = HenonN.ar(freq = 0.07218959, a = 0.012793035, b = 0.28824735, x0 = x0, x1 = 0.0)
  val b_0             = StandardL.ar(freq = 4508.6934, k = 113.47259, xi = 0.28824735, yi = 46.298378)
  val b_1             = HenonN.ar(freq = -0.024508374, a = 83.59249, b = b_0, x0 = 299.82324, x1 = 0.06283997)
  val quadN           = QuadN.ar(freq = henonN, a = 0.06283997, b = b_1, c = 299.82324, xi = 25.466032)
  val lFDNoise0       = LFDNoise0.ar(299.82324)
  val linCongC        = LinCongC.ar(freq = -2569.0, a = henonN, c = 4508.6934, m = 113.47259, xi = 299.82324)
  val in_1            = LeakDC.ar(0.07218959, coeff = 0.995)
  val xi_0            = Delay1.ar(in_1)
  val in_2            = LeakDC.ar(0.0673406, coeff = 0.995)
  val delay1_0        = Delay1.ar(in_2)
  val in_3            = LeakDC.ar(0.0673406, coeff = 0.995)
  val delay1_1        = Delay1.ar(in_3)
  val clip2           = delay1_1 clip2 299.82324
  val thresh          = 0.109397724 thresh delay1_1
  val bitOr           = thresh | 299.82324
  val in_4            = LeakDC.ar(0.5898464, coeff = 0.995)
  val max_0           = thresh max 0.0
  val dur             = max_0 min 30.0
  val ramp            = Ramp.ar(in_4, dur = dur)
  val in_5            = StandardL.ar(freq = 25.59424, k = 0.5898464, xi = xi_0, yi = -2569.0)
  val in_6            = LeakDC.ar(0.07218959, coeff = 0.995)
  val xi_1            = Delay1.ar(in_6)
  val freq_0          = StandardL.ar(freq = 25.59424, k = 0.5898464, xi = xi_1, yi = -2569.0)
  val in_7            = LeakDC.ar(113.47259, coeff = 0.995)
  val delay2_0        = Delay2.ar(in_7)
  val ring2           = delay2_0 ring2 4.831171
  val b_2             = freq_0 pow 0.012793035
  val scaleneg_0      = 299.82324 scaleneg b_2
  val latoocarfianL   = LatoocarfianL.ar(freq = freq_0, a = 0.012793035, b = 1.5, c = 0.5, d = 45671.24, xi = ring2, yi = scaleneg_0)
  val max_1           = standardL_0 max 10.0
  val freq_1          = max_1 min 20000.0
  val max_2           = latoocarfianL max 0.01
  val rq_0            = max_2 min 100.0
  val bPF             = BPF.ar(in_5, freq = freq_1, rq = rq_0)
  val in_8            = LeakDC.ar(297.71124, coeff = 0.995)
  val delayC_0        = DelayC.ar(in_8, maxDelayTime = 0.5898464, delayTime = 0.5898464)
  val sumsqr          = 297.71124 sumsqr standardL_0
  val standardL_1     = StandardL.ar(freq = 25.59424, k = 113.47259, xi = xi_1, yi = 299.82324)
  val in_9            = LeakDC.ar(46.298378, coeff = 0.995)
  val delay2_1        = Delay2.ar(in_9)
  val lFDNoise3       = LFDNoise3.ar(scaleneg_0)
  val scaleneg_1      = 299.82324 scaleneg b_2
  val in_10           = scaleneg_1 ring4 0.0
  val lPF             = LPF.ar(in_10, freq = 46.298378)
  val in_11           = LFDNoise3.ar(332.15744)
  val max_3           = delay1_0 max 0.0
  val width           = max_3 min 1.0
  val varSaw          = VarSaw.ar(freq = 0.14796029, iphase = 0.28824735, width = width)
  val min_0           = varSaw min 299.82324
  val max_4           = min_0 max 0.0
  val delayTime_0     = max_4 min 20.0
  val delayC_1        = DelayC.ar(in_11, maxDelayTime = 20.0, delayTime = delayTime_0)
  val freq_2          = LinCongL.ar(freq = 0.109397724, a = ring2, c = delay2_0, m = 0.07218959, xi = delayC_1)
  val earlyRefLevel   = LFDClipNoise.ar(freq_2)
  val gVerb           = GVerb.ar(ring2, roomSize = 83.59249, revTime = 100.0, damping = 0.5, inputBW = 1.0, spread = 43.0, dryLevel = 1.0, earlyRefLevel = earlyRefLevel, tailLevel = 113.47259, maxRoomSize = 300.0)
  val min_1           = Constant(299.82324f) min scaleneg_0
  val standardL_2     = StandardL.ar(freq = 4.831171, k = -0.012666181, xi = 46.298378, yi = 0.0)
  val freq_3          = StandardN.ar(freq = 0.07218959, k = 4508.6934, xi = 45671.24, yi = 0.002310892)
  val x1_0            = LatoocarfianL.ar(freq = freq_3, a = 0.07218959, b = 0.5, c = 0.5, d = -2569.0, xi = 4.831171, yi = delay2_0)
  val henonC          = HenonC.ar(freq = -2569.0, a = delayC_1, b = b_2, x0 = delayC_1, x1 = x1_0)
  val excess          = 0.00913873 excess freq_2
  val seq = Seq[GE](
    bRF, /* quadN, */ lFDNoise0, linCongC, clip2, bitOr, ramp, bPF, 
    delayC_0, sumsqr, standardL_1, delay2_1, lFDNoise3, lPF, gVerb, min_1, 
    standardL_2, henonC, excess
  )
  // seq.zipWithIndex.foreach { case (x, i) => x.poll(1, s"elem$i") }
  val mix = Mix(seq)
  val in_12           = Mix.Mono(mix)
  val checkBadValues  = CheckBadValues.ar(in_12, id = 0.0, post = 0.0)
  val gate            = checkBadValues sig_== 0.0
  val in_13           = Gate.ar(in_12, gate = gate)
  val pan2            = Pan2.ar(in_13, pos = 0.0, level = 1.0)
  Out.ar(0, Limiter.ar(LeakDC.ar(pan2)) * "amp".kr(0.2))
}
