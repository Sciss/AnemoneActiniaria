play {
  val pulse_1         = Pulse.ar(freq = 56.572437, width = 0.00927054)
  val geq_0           = pulse_1 >= -0.016140295
  val pulse_2         = Pulse.ar(freq = 115.54486, width = 0.027581401)
  val clip2           = 115.54486 clip2 pulse_2
  val mix             = geq_0 + clip2
  val pan2            = Pan2.ar(mix, pos = 0.0, level = 1.0)
  Out.ar(0, Limiter.ar(LeakDC.ar(pan2)) * "amp".kr(0.2))
}

// s.dispose()