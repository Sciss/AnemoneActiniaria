val x = play {
  val numChannels = 4
  // RandSeed.ir(trig = 1, seed = 56789.0)
  val yi              = LFDNoise3.ar(Seq.fill(numChannels)(1551.5026))
  val width           = yi max 0.0
  val in_0            = VarSaw.ar(freq = Seq.fill(numChannels)(100.5704), iphase = 0.0, width = width)
  val twoPole         = TwoPole.ar(in_0, freq = 55.773136, radius = 1.0)
  val scaleneg        = twoPole scaleneg 0.0014409359
  val in_1            = LeakDC.ar(Seq.fill(numChannels)(0.016513553), coeff = 0.995)
  val oneZero         = OneZero.ar(in_1, coeff = 1.0)
  val mod             = twoPole % oneZero
  val in_2            = LeakDC.ar(Seq.fill(numChannels)(0.35691246), coeff = 0.995)
  val k               = DelayN.ar(in_2, maxDelayTime = 20.0, delayTime = 0.23810405)
  val gbmanL          = GbmanL.ar(freq = Seq.fill(numChannels)(55.773148), xi = 100.5704, yi = 163.37988)
  val b_0             = LFDNoise3.ar(Seq.fill(numChannels)(1551.5026))
  val a               = 0.016513553 scaleneg b_0
  val decayTime       = LinCongN.ar(freq = gbmanL, a = a, c = 100.5704, m = 4734.57, xi = 100.5704)
  val max_0           = twoPole max 0.0
  val delayTime_0     = max_0 min 20.0
  val delayL          = DelayL.ar(gbmanL, maxDelayTime = 20.0, delayTime = delayTime_0)
  val in_3            = LeakDC.ar(Seq.fill(numChannels)(2.3382738), coeff = 0.995)
  val in_4            = Decay2.ar(in_3, attack = 0.016513553, release = 30.0)
  val in_5            = LeakDC.ar(in_4, coeff = 0.995)
  val delay2          = Delay2.ar(in_5)
  val in_6            = Impulse.ar(freq = 498.64328, phase = 1.0)
  val in_7            = LeakDC.ar(in_4, coeff = 0.995)
  val max_1           = in_6 max 0.0
  val pitchDispersion = max_1 min 1.0
  val max_2           = delayL max 0.0
  val timeDispersion  = max_2 min 2.0
  val pitchShift      = PitchShift.ar(in_7, winSize = 2.0, pitchRatio = 0.0, pitchDispersion = pitchDispersion, timeDispersion = timeDispersion)
  val standardL       = StandardL.ar(freq = 55.773136, k = k, xi = 333.4453, yi = gbmanL)
  val in_8            = LeakDC.ar(-62.88437, coeff = 0.995)
  val max_3           = twoPole max 0.0
  val timeDown        = max_3 min 30.0
  val lag3UD          = Lag3UD.ar(in_8, timeUp = 0.0, timeDown = timeDown)
  val in_9            = LeakDC.ar(-9.2467286E-5, coeff = 0.995)
  val xi_0            = Lag3UD.ar(in_9, timeUp = 30.0, timeDown = 30.0)
  val ring3           = xi_0 ring3 0.23652716
  val max_4           = in_6 max 0.0
  val delayTime_1     = max_4 min 0.23652716
  val combN           = CombN.ar(in_6, maxDelayTime = 0.23652716, delayTime = delayTime_1, decayTime = decayTime)
  val in_10           = LeakDC.ar(-62.88437, coeff = 0.995)
  val max_5           = delay2 max 0.55
  val max_6           = in_6 max 0.0
  val revTime         = max_6 min 100.0
  val max_7           = a max 0.0
  val damping         = max_7 min 1.0
  val max_8           = in_4 max 0.0
  val spread          = max_8 min 43.0
  val roomSize        = max_5 min 300.0
  // val gVerb           = GVerb.ar(in_10, roomSize = roomSize, revTime = revTime, damping = damping, inputBW = 0.0, spread = spread, dryLevel = delay2, earlyRefLevel = 2.5205823E-4, tailLevel = 0.0073382077, maxRoomSize = 300.0)
  val gVerb           = GVerb.ar(Mix.mono(in_10), 
    roomSize = roomSize \ 0, revTime = revTime \ 0, 
    damping = damping \ 0, inputBW = 0.0, spread = spread \ 0, dryLevel = delay2 \ 0, 
    earlyRefLevel = 2.5205823E-4, tailLevel = 0.0073382077, maxRoomSize = 300.0)
  val in_11           = LeakDC.ar(Seq.fill(numChannels)(8.832454), coeff = 0.995)
  val lPZ1            = LPZ1.ar(in_11)
  val max_9           = lPZ1 max 0.01
  val syncFreq        = max_9 min 20000.0
  val max_10          = oneZero max 0.01
  val sawFreq         = max_10 min 20000.0
  val syncSaw         = SyncSaw.ar(syncFreq = syncFreq, sawFreq = sawFreq)
  val lorenzL         = LorenzL.ar(freq = -68.48215, s = -0.88766193, r = 0.0014409359, b = b_0, h = 0.06, xi = 0.23652716, yi = 100.5704, zi = 0.23652716)
  val in_12           = LeakDC.ar(Seq.fill(numChannels)(-68.48215), coeff = 0.995)
  val lPZ2            = LPZ2.ar(in_12)
  val max_11          = a max 0.5
  val b_1             = max_11 min 1.5
  val latoocarfianC   = LatoocarfianC.ar(freq = 2287.8992, a = -9.2467286E-5, b = b_1, c = 0.5, d = delay2, xi = xi_0, yi = yi)
  val mix             = Mix(Seq[GE](scaleneg, mod, pitchShift, standardL, lag3UD, ring3, combN, gVerb, syncSaw, lorenzL, lPZ2, latoocarfianC))
  val in_13           = mix // Mix.Mono(mix)
  val checkBadValues  = CheckBadValues.ar(in_13, id = 0.0, post = 0.0)
  val gate            = checkBadValues sig_== 0.0
  val in_14           = Gate.ar(in_13, gate = gate)
  val pan2            = in_14 // Pan2.ar(in_14, pos = 0.0, level = 1.0)
  // val _trig = Impulse.ar("freq".kr(2))
  // val _pitch = (TIRand.ar(lo = -2, hi = 2, trig = _trig) * 6).midiratio
  
  // val _karlPitch = PitchShift.ar(in = pan2, pitchRatio = _pitch, pitchDispersion = 0, timeDispersion = 0)
  val sig = pan2 // _karlPitch // pan2 * 0.5 + _karlPitch * 0.5
  Out.ar(0, Limiter.ar(LeakDC.ar(sig)) * "amp".kr(0.1))
}

/*---

x.set("amp" -> 0.5)
x.set("freq" -> 5)


*/
