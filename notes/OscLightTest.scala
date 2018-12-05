val cfg = osc.UDP.Config()
cfg.localAddress = "192.168.0.77"
val t = osc.UDP.Transmitter(cfg)
t.connect()
t.localPort
t.isConnected
t.dump()

val tgt = new java.net.InetSocketAddress("192.168.0.25", 0x4C69)
t.send(osc.Message("/foo"), tgt)
t.send(osc.Message("/led", 0x080808, 0x080808, 0x080808, 0xFFFFFF), tgt)
t.send(osc.Message("/led", 0x080007, 0x080407, 0x080607, 0x00A0), tgt)

t.send(osc.Message("/led", 0xFF0000, 0x00FF00, 0x0000FF, 0x1F1F1F, 0x3F3F3F, 0x5F5F5F, 0x7F7F7F, 0x9F9F9F, 0xBFBFBF, 0xDFDFDF, 0xFFFFFF), tgt)
t.send(osc.Message("/led", 0,0,0,0,0,0,0,0,0,0,0), tgt)

t.send(osc.Message("/led", 0xDFDFDF, 0xFFFFFF), tgt)

var running = true
var delay = 10

val th = new Thread {
  override def run(): Unit = {
    val rnd = new util.Random
    while (running) {
      val rgb1 = rnd.nextInt & 0xFFFFFF
      val rgb2 = rnd.nextInt & 0xFFFFFF
      t.send(osc.Message("/led", rgb1, rgb2), tgt)
      val d = math.max(1, math.min(1000, delay))
      Thread.sleep(d)
    }
  }
}

t.dump(osc.Dump.Off)
th.start()

delay = 100
running = false

running = true
var power = 2.0

val th = new Thread {
  override def run(): Unit = {
    val rnd = new util.Random
    while (running) {
      @inline def mkRGB(): Int = {
        val v   = (rnd.nextDouble.pow(power) * 0xFF).toInt
        val rgb = (v << 16) | (v << 8) | v
        rgb
      }
      val rgb1 = mkRGB()
      val rgb2 = mkRGB()
      t.send(osc.Message("/led", rgb1, rgb2), tgt)
      val d = math.max(1, math.min(1000, delay))
      Thread.sleep(d)
    }
  }
}

th.start()

delay = 10
power = 4
power = 8

running = false

t.send(osc.Message("/led", 0,0), tgt)
