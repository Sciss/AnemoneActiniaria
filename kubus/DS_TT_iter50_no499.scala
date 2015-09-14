val x = play {
  RandSeed.ir(trig = 1, seed = 56789.0)
  
  val p1 = "p1".kr(Vector.fill(16)(1f))
  val p2 = "p2".kr(Vector.fill(16)(0.01f))
  
  val lFSaw           = LFSaw.ar(freq = p1, iphase = 0.0)
  val lFTri           = LFTri.ar(freq = p2, iphase = 1.0)

  val freq_0          = Nyquist()
  
  val p3 = "p3".kr(Vector.fill(16)(1.5757418f))
  val p4 = "p4".kr(Vector.fill(16)(1.9f))
  
  val in_6            = CuspL.ar(freq = freq_0, a = p3, b = p4, xi = 0.0)
  val in_7            = LeakDC.ar(in_6, coeff = 0.995)
  val oneZero         = OneZero.ar(in_7, coeff = -1.0)
  
  val p5 = "p5".kr(Vector.fill(16)(31.025694f))
  val p6 = "p6".kr(Vector.fill(16)(4.80601f))
  
  val henonC          = HenonC.ar(freq = p5, a = lFTri, b = p6, x0 = 0.0, x1 = 5.3651776E-4)
  
  val mix             = Mix(Seq[GE](
    lFSaw,  oneZero, henonC
  ))
  
  val in_16           = mix // Mix.Mono(mix)
  val checkBadValues  = CheckBadValues.ar(in_16, id = 0.0, post = 0.0)
  val gate            = checkBadValues sig_== 0.0
  val in_17           = Gate.ar(in_16, gate = gate)
  val pan2            = in_17 // Pan2.ar(in_17, pos = 0.0, level = 1.0)
  val sig = pan2
  Out.ar(0, Limiter.ar(LeakDC.ar(sig)) * "amp".kr(0.5))
}

/*---
  // val p1 = "p1".kr(Vector.fill(16)(1f))
  // val p2 = "p2".kr(Vector.fill(16)(0.01f))

  x.set("p1" -> Vector.fill(16)((1.0 + (math.random - 0.5) * 0.1).toFloat))
  x.set("p2" -> Vector.fill(16)((0.1 + (math.random - 0.5) * 0.01).toFloat))
  x.set("p3" -> Vector.fill(16)((1.57574 + (math.random - 0.5) * 0.1).toFloat))
  x.set("p4" -> Vector.fill(16)((1.9 + (math.random - 0.5) * 0.1).toFloat))
  
  x.set("p1" -> Vector.fill(16)((16.0 + (math.random - 0.5) * 1).toFloat))
  // x.set("p2" -> Vector.fill(16)((100 + (math.random - 0.5) * 0.0001).toFloat))

  x.set("p3" -> Vector.fill(16)((2.9 + (math.random - 0.5) * 0.1).toFloat))
  x.set("p3" -> Vector.fill(16)((0.9 + (math.random - 0.5) * 0.1).toFloat))
  
  x.set("p4" -> Vector.fill(16)((1.85 + (math.random - 0.5) * 0.1).toFloat))
  x.set("p4" -> Vector.fill(16)((1.55 + (math.random - 0.5) * 0.1).toFloat))
  x.set("p4" -> Vector.fill(16)((1.55 + (math.random - 0.5) * 0.5).toFloat))

  x.set("p5" -> Vector.fill(16)((100 + (math.random - 0.5) * 0.5).toFloat))
  x.set("p5" -> Vector.fill(16)((100 + (math.random - 0.5) * 5).toFloat))
  // x.set("p5" -> Vector.fill(16)((100 + (math.random - 0.5) * 0.5).toFloat))
*/

val y = play(addAction = addToTail) {
  val in  = In.ar(0, 16)
  val out = LPF.ar(in, "freq".kr(400).max(20).min(20000))
  ReplaceOut.ar(0, out)
}

y.set("freq" -> 800)
y.set("freq" -> 1100)
y.set("freq" -> 2000)

val z = play(addAction = addToTail) {
  val in  = In.ar(0, 16)
  val out = HPF.ar(in, "freq".kr(400).max(20).min(20000))
  ReplaceOut.ar(0, out)
}

y.free()
z.set("freq" -> 4000)
z.set("freq" -> 6000)
z.set("freq" -> 8000)
z.set("freq" -> 11000)
z.set("freq" -> 14000)
z.set("freq" -> 18000)
