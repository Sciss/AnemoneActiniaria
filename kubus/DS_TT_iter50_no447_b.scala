val x = play {
  RandSeed.ir(trig = 1, seed = 56789.0)
  val in_0            = LeakDC.ar(0.9768148, coeff = 0.995)
  val decay_0         = Decay.ar(in_0, time = 0.0073606772)
  val in_1            = LeakDC.ar(-0.0064726463, coeff = 0.995)
  val yi              = Lag2UD.ar(in_1, timeUp = 0.0, timeDown = 0.0)

  val freq_1          = GbmanL.ar(freq = decay_0, 
    xi = Seq.fill(16)(-0.05771263 + math.random * 0.002), 
    yi = yi)
  val leq             = freq_1 <= 440.0
  val in_3            = 47.74325 | leq
  val min_0           = in_3 min leq
  
  val max_4           = min_0 max 0.01
  val syncFreq        = max_4 min 20000.0
  val syncSaw         = SyncSaw.ar(syncFreq = syncFreq, sawFreq = 0.5)
  
  val xi_0            = LFDNoise1.ar(3824.141)
  val a_2             = LFDNoise3.ar(-0.0064726463)
  val difsqr          = a_2 difsqr xi_0
  
  val p1 = "p1".kr(Vector.fill(16)(41.902702f))
  val p2 = "p2".kr(Vector.fill(16)(1.6799022f))
  val p3 = "p3".kr(Vector.fill(16)(0.003201534f))
  
  val cuspN           = CuspN.ar(freq = p1, a = p2, b = p3, xi = syncSaw)

  val p4 = "p4".kr(Vector.fill(16)(0.0026092983))

  val in_23           = p4 amclip xi_0
  
  val p5 = "p5".kr(47.74325).max(1).min(100)
  
  val runningSum      = RunningSum.ar(in_23, length = 47.74325)
  
  val mix             = Mix(Seq(difsqr, cuspN, runningSum))

  val in_24           = mix // Mix.Mono(mix)
  val checkBadValues  = CheckBadValues.ar(in_24, id = 0.0, post = 0.0)
  val gate            = checkBadValues sig_== 0.0
  val in_25           = Gate.ar(in_24, gate = gate)
  val sig = in_25
  
  Out.ar(0, Limiter.ar(LeakDC.ar(sig)) * "amp".kr(0.2))
}

/*---
                    // 1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17
  x.set("ca" -> Vector(1f,1f,1f,1f,0f,1f,1f,1f,1f,1f,1f,1f,1f,0f,1f,1f,0f))

  // 5  fiep rausch
  // 14 peo peo
  // 17 rauschen
  
//   val p1 = "p1".kr(41.902702)
//   val p2 = "p2".kr(1.6799022)
//   val p3 = "p3".kr(0.003201534)
//   
  x.set("p1" -> (41.902702 * 0.99f))
  x.set("p2" -> 0.9)
  x.set("p3" -> 0.001)

  val p4 = "p4".kr(0.0026092983)

  x.set("p4" -> 0.3)
  x.set("p4" -> Vector.fill(16)((0.01 + math.random * 0.1).toFloat))

  x.set("p1" -> Vector.fill(16)((41.902702 + (math.random - 0.5) * 0.2).toFloat))
  x.set("p1" -> Vector.fill(16)((41.902702 * 1 + (math.random - 0.5) * 0.2).toFloat))
  // x.set("p4" -> Vector.fill(16)((0.01 + math.random * 0.02).toFloat))

   // eher nicht:
  x.set("p2" -> Vector.fill(16)((1.6799022 * 1 + (math.random - 0.5) * 1.0).toFloat))
  x.set("p3" -> Vector.fill(16)((0.003201534 * 8 + (math.random - 0.5) * 0.001).toFloat))
  
  // x.set("p5" -> 100)

*/

x.set("amp" -> 0.2)
