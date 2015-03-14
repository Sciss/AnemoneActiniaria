/*
 *  Anemone.scala
 *  (Anemone-Actiniaria)
 *
 *  Copyright (c) 2014-2015 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.anemone

import com.alee.laf.WebLookAndFeel
import de.sciss.desktop.Desktop

import de.sciss.file._
import de.sciss.lucre.stm
import de.sciss.lucre.swing.defer
import de.sciss.lucre.synth.InMemory
import de.sciss.nuages._
import de.sciss.synth
import de.sciss.synth.ugen.{Constant, LinXFade2}
import de.sciss.synth.{GE, Server}
import de.sciss.synth.proc.AuralSystem

import scala.collection.immutable.{IndexedSeq => Vec}

object Anemone {
  case class Config(masterChannels: Range, soloChannels: Range,
                    micInputs  : Vec[NamedBusConfig],
                    lineInputs : Vec[NamedBusConfig],
                    lineOutputs: Vec[NamedBusConfig],
                    generatorChannels: Int = 0,
                    device: Option[String] = None
  )

  val FirefaceConfig = Config(
    masterChannels  = 0 until 6,
    soloChannels    = 6 until 8,
    micInputs       = Vector(
      NamedBusConfig("m-dpa", 0, 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 4, 1),
      NamedBusConfig("beat" , 5, 1)
    ),
    lineOutputs     = Vector(
      NamedBusConfig("sum", 5, 1)
    )
  )

  val MOTUConfig = Config(
    masterChannels  = 2 to 6,
    soloChannels    = 0 to 1,
    micInputs       = Vector(
      NamedBusConfig("m-at" , 14, 2),
      NamedBusConfig("m-dpa", 16, 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("beat" , 18, 1),
      NamedBusConfig("pirro", 19, 1)
    ),
    lineOutputs     = Vector(
      NamedBusConfig("sum", 8, 2)
    ),
    device = if (Desktop.isMac) Some("MOTU 828mk2") else None
  )

  val Bremen = Config(
    masterChannels    = 2 to 5,
    soloChannels      = 0 to 1,
    generatorChannels = 2,
    micInputs         = Vector(
      NamedBusConfig("m-at" , 14, 2),
      NamedBusConfig("m-dpa", 16, 1)
    ),
    lineInputs        = Vector(
      NamedBusConfig("i-lh" , 18, 2),
      NamedBusConfig("i-mkv", 20, 2)
    ),
    lineOutputs       = Vector(
      NamedBusConfig("sum1", 6, 2)
      // NamedBusConfig("sum2", 8, 2)
    ),
    device = if (Desktop.isMac) Some("MOTU 828mk2") else None
  )

  val Scarlett = Config(
    masterChannels    = 0 to 5,
    soloChannels      = 8 to 9,
    generatorChannels = 3,
    micInputs         = Vector(
      NamedBusConfig("m-dpa", 10, 2)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 0, 1),
      NamedBusConfig("beat" , 1, 1)
    ),
    lineOutputs     = Vector(
      NamedBusConfig("sum", 6, 2)
    ),
    device = None
  )

  val Impuls = Config(
    masterChannels    = 0 to 1,
    soloChannels      = 6 to 7,
    generatorChannels = 0, // 2,
    micInputs         = Vector(
      // NamedBusConfig("m-at" , 0, 2),
      NamedBusConfig("m-dpa" , 10, 1),
      NamedBusConfig("m-hole",  0, 1),
      NamedBusConfig("m-keys",  1, 1)
    ),
    lineInputs      = Vector(
      // NamedBusConfig("beat" , 3, 1),
      // NamedBusConfig("pirro", 4, 1)
    ),
    lineOutputs     = Vector(
      NamedBusConfig("sum", 4, 2)
    ),
    device = None
  )

  val config: Config = Scarlett

  def main(args: Array[String]): Unit = {
    implicit val system = InMemory()
    defer(WebLookAndFeel.install())
    (new Anemone).run()
  }
}
class Anemone extends Wolkenpumpe[InMemory] {
  import Anemone._
  type S = InMemory
  private val dsl = new DSL[S]
  import dsl._

  override protected def configure(sCfg: ScissProcs.ConfigBuilder, nCfg: Nuages.ConfigBuilder,
                                   aCfg: Server.ConfigBuilder): Unit = {
    super.configure(sCfg, nCfg, aCfg)
    sCfg.generatorChannels  = config.generatorChannels
    // println(s"generatorChannels ${sCfg.generatorChannels}")
    sCfg.micInputs          = config.micInputs
    sCfg.lineInputs         = config.lineInputs
    sCfg.lineOutputs        = config.lineOutputs
    // sCfg.highPass           = 100
    sCfg.audioFilesFolder   = Some(userHome / "Music" / "tapes")

    // println(s"master max = ${Turbulence.ChannelIndices.max}")
    nCfg.masterChannels     = Some(config.masterChannels)
    nCfg.soloChannels       = Some(config.soloChannels)
    nCfg.recordPath         = Some((userHome / "Music" / "rec").path) // XXX has no effect?

    aCfg.wireBuffers        = 512 // 1024
    aCfg.audioBuffers       = 4096
    // aCfg.blockSize          = 128
    if (config.device.isDefined) aCfg.deviceName = config.device
  }

  private def mix(in: GE, flt: GE, mix: GE): GE = LinXFade2.ar(in, flt, mix * 2 - 1)
  private def mkMix()(implicit tx: S#Tx): GE = pAudio("mix", ParamSpec(0, 1), default = 0)

  private def mkMix4()(implicit tx: S#Tx): GE = {
    import synth._; import ugen._
    val f1 = pAudio("mix1", ParamSpec(0, 1), default = 0)
    val f2 = pAudio("mix2", ParamSpec(0, 1), default = 0)
    Lag.ar(Seq(f1, f1 * 0.667 + f2 * 0.333, f1 * 0.333, f2 * 0.667, f2))
  }

  //  private def mkTransition(name: String)(fun: (GE, GE) => GE)(implicit tx: S#Tx, nuages: Nuages[S]) = filter(name) { in =>
  //    import de.sciss.synth._
  //    import de.sciss.synth.ugen._
  //    val fade   = mkMix()
  //    val sig   = fun(in, 1 - fade)
  //    sig // mix(in, sig, fade)
  //  }

  // a 10% direct fade-in/out, possibly with delay to compensate for FFT
  private def mkBlend(pred: GE, z: GE, fade: GE, dt: GE = Constant(0)): GE = {
    import de.sciss.synth._
    import de.sciss.synth.ugen._
    val dpa = fade.min(0.1) * 10
    val pa = fade.min(0.1).linlin(0, 0.1, 1, 0)
    val za = 1 - pa
    val dp = if (dt == Constant(0)) pred else DelayN.ar(pred, dt, dt * dpa)
    val pm = dp * pa
    val zm = z  * za
    pm + zm
  }

  override protected def registerProcesses(sCfg: ScissProcs.Config, nCfg: Nuages.Config)
                                          (implicit tx: S#Tx, cursor: stm.Cursor[InMemory],
                                           nuages: Nuages[S], aural: AuralSystem): Unit = {
    super.registerProcesses(sCfg, nCfg)

    Mutagens(dsl, sCfg, nCfg)

    generator("a~pulse") {
      import synth._; import ugen._
      val pFreq   = pAudio("freq"     , ParamSpec(0.1 , 10000, ExpWarp), default = 15 /* 1 */)
      val pW      = pAudio("width"    , ParamSpec(0.0 ,     1.0),        default =  0.5)
      // val pAmp    = pAudio("amp"      , ParamSpec(0.01,     1, ExpWarp), default =  0.1)
      val pLo     = pAudio("lo"     , ParamSpec(0.0 , 1), default = 0)
      val pHi     = pAudio("hi"     , ParamSpec(0.0 , 1), default = 1)

      val freq  = pFreq // LinXFade2.ar(pFreq, inFreq, pFreqMix * 2 - 1)
      val width = pW // LinXFade2.ar(pW, inW, pWMix * 2 - 1)
      val sig   = LFPulse.ar(freq, width)

      sig.linlin(0, 1, pLo, pHi)
    }

    generator("a~sin") {
      import synth._; import ugen._
      val pFreq   = pAudio("freq"     , ParamSpec(0.1 , 10000, ExpWarp), default = 15 /* 1 */)
      // val pAmp    = pAudio("amp"      , ParamSpec(0.01,     1, ExpWarp), default =  0.1)
      val pLo     = pAudio("lo"     , ParamSpec(0.0 , 1), default = 0)
      val pHi     = pAudio("hi"     , ParamSpec(0.0 , 1), default = 1)

      val freq  = pFreq // LinXFade2.ar(pFreq, inFreq, pFreqMix * 2 - 1)
      val sig   = SinOsc.ar(freq)

      sig.linlin(-1, 1, pLo, pHi)
    }

    generator("a~dust") {
      import synth._; import ugen._
      val pFreq   = pAudio("freq" , ParamSpec(0.01, 1000, ExpWarp), default = 0.1 /* 1 */)
      val pDecay  = pAudio("decay", ParamSpec(0.001 , 10, ExpWarp), default = 0.1 /* 1 */)
      val pLo     = pAudio("lo"     , ParamSpec(0.0 , 1), default = 0)
      val pHi     = pAudio("hi"     , ParamSpec(0.0 , 1), default = 1)

      val freq  = pFreq
      val sig   = Decay.ar(Dust.ar(freq), pDecay).clip(0.01, 1).linlin(0.01, 1, pLo, pHi)
      sig
    }

    generator("a~gray") {
      import synth._; import ugen._
      val pLo     = pAudio("lo"     , ParamSpec(0.0 , 1), default = 0)
      val pHi     = pAudio("hi"     , ParamSpec(0.0 , 1), default = 1)
      GrayNoise.ar.linlin(-1, 1, pLo, pHi)
    }

    generator("a~rand") {
      import synth._; import ugen._
      val pLo     = pAudio("lo"     , ParamSpec(0.0 , 1), default = 0)
      val pHi     = pAudio("hi"     , ParamSpec(0.0 , 1), default = 1)
      val pQuant  = pAudio("quant"  , ParamSpec(0.0 , 1), default = 0)
      // val inTrig  = pAudioIn("tr" , 1, ParamSpec(0, 1))
      val inTrig  = pAudio("tr", ParamSpec(0, 1), default = 0)
      // val sig     = TRand.ar(pLo, pHi, inTrig)
      val sig0    = K2A.ar(TRand.kr(0 /* A2K.kr(pLo) */ , 1 /* A2K.kr(pHi) */, T2K.kr(inTrig)))
      val sig     = sig0.roundTo(pQuant).linlin(0, 1, pLo, pHi)
      // sig.poll(inTrig, "rand")
      sig
    }

    filter("a~delay") { in =>
      import synth._; import ugen._
      val pTime   = pAudio("time", ParamSpec(0.0 , 1.0), default = 0)
      val sig     = DelayN.ar(in, pTime, 1.0)
      sig
    }
/*
    filter("a~reso") { in =>
      import synth._; import ugen._
      val pFreq   = pAudio("freq"     , ParamSpec(30  , 13000, ExpWarp), default = 400) // beware of the upper frequency
      val pQ      = pAudio("q"        , ParamSpec( 0.5,    50, ExpWarp), default =   1)
      val pMix    = mkMix()
      val inFreq  = pAudioIn("in-freq", 1, ParamSpec(30  , 13000, ExpWarp))
      val pFreqMix= pAudio  ("freq-src"  , ParamSpec(0, 1, step = 1), default = 0)
      val inQ     = pAudioIn("in-q"   , 1, ParamSpec( 0.5,    50, ExpWarp))
      val pQMix   = pAudio  ("q-src"     , ParamSpec(0, 1, step = 1), default = 0)

      val freq    = Lag.ar(LinXFade2.ar(pFreq, inFreq, pFreqMix * 2 - 1))
      val pq0     = Lag.ar(LinXFade2.ar(pQ, inQ, pQMix * 2 - 1))
      val rq      = pq0.reciprocal
      val makeUp  = pq0
      val flt     = Resonz.ar(in, freq, rq) * makeUp
      mix(in, flt, pMix)
    }
*/
    sCfg.lineInputs.find(c => c.name == "i-mkv" || c.name == "beat").foreach { cfg =>
      generator("a~beat") {
        import synth._; import ugen._
        val off     = cfg.offset
        val pThresh = pAudio("thresh", ParamSpec(0.01, 1, ExpWarp), default = 0.1)
        val in      = Trig1.ar(PhysicalIn.ar(off) - pThresh, 0.02)
        val pDiv    = pAudio("div", ParamSpec(1, 16, IntWarp), default = 1)
        val pulse   = PulseDivider.ar(in, pDiv)
        val pTime   = pAudio("time", ParamSpec(0.0 , 1.0), default = 0)
        val sig     = DelayN.ar(pulse, 1.0, pTime)
        sig
      }
    }

    sCfg.micInputs.find(c => c.name == "m-hole").foreach { cfg =>
      generator("m-feat") {
        import synth._; import ugen._
        val off         = cfg.offset
        val in0         = PhysicalIn.ar(off)
        val gain        = pAudio("gain", ParamSpec(-20, 20), default = 0).dbamp
        val in          = in0 * gain
        val pThresh     = pControl("thresh", ParamSpec(0, 1), default = 0.5)
        val buf         = LocalBuf(numFrames = 1024, numChannels = 1)
        val chain1      = FFT(buf, in)
        val onsets      = Onsets.kr(chain1, pThresh)
        val loud        = Loudness.kr(chain1)
        val cent        = SpecCentroid.kr(chain1)
        val flat        = SpecFlatness.kr(chain1)
        val loudN       = (loud / 64).clip(0, 1)
        val centN       = cent.clip(100, 10000).explin(100, 10000, 0, 1)
        val flatN       = flat.clip(0, 1)

        pAudioOut("loud", loudN)
        pAudioOut("cent", centN)
        pAudioOut("flat", flatN)
        onsets
      }
    }

    generator("a~step8") {
      import synth._; import ugen._
      val vals    = Vector.tabulate(8)(i => pAudio(s"v${i+1}", ParamSpec(0, 1), default = 0))
      val trig    = pAudio("trig", ParamSpec(0.0, 1.0), default = 0)
      val hi      = pAudio("hi", ParamSpec(1, 8, IntWarp), default = 1)
      val index   = Stepper.ar(trig, lo = 0, hi = hi - 1)
      val sig     = Select.ar(index, vals)
      sig
    }

    filter("a~dup") { in =>
      import synth._; import ugen._
      val pThresh = pAudio("thresh", ParamSpec(0.01, 1, ExpWarp), default = 0.1)
      val pDiv    = pAudio("div", ParamSpec(1, 16, IntWarp), default = 1)
      val tr      = in - pThresh
      val tim     = Timer.ar(tr)
      val frq     = tim.reciprocal * pDiv
      val sig     = Phasor.ar(in, frq / SampleRate.ir)
      sig
    }

    filter("a~skew") { in =>
      val pLo     = pAudio("lo" , ParamSpec(0, 1), default = 0)
      val pHi     = pAudio("hi" , ParamSpec(0, 1), default = 1)
      val pPow    = pAudio("pow", ParamSpec(0.125, 8, ExpWarp), default = 1)
      val pRound  = pAudio("rnd", ParamSpec(0, 1), default = 0)

      val pMix    = mkMix()

      val sig = in.clip(0, 1).pow(pPow).linlin(0, 1, pLo, pHi).roundTo(pRound)
      mix(in, sig, pMix)
    }

    filter("a~gate") { in =>
      import synth._; import ugen._
      val pThresh = pAudio("thresh", ParamSpec(0.01, 1, ExpWarp), default = 0.1)
      val pGate   = pAudio("gate", ParamSpec(0.0, 1.0), default = 0) > pThresh
      val pLeak   = pAudio("leak", ParamSpec(0, 1, IntWarp), default = 0) > pThresh
      val pMix    = mkMix()
      val sig0    = Gate.ar(in, pGate)
      val leak    = LeakDC.ar(sig0)
      val sig     = Select.ar(pLeak, Seq(sig0, leak))
      mix(in, sig, pMix)
    }

    sCfg.lineInputs.find(_.name == "i-lh").foreach { cfg =>
      generator("a~lh") {
        import synth._; import ugen._
        val off     = cfg.offset
        val in      = PhysicalIn.ar(off)
        val pAmp    = pAudio("amp"      , ParamSpec(0.01,     1, ExpWarp), default =  0.1)
        val sig     = in * pAmp
        sig
      }
    }

    sCfg.lineInputs.find(_.name == "i-mkv").foreach { cfg =>
      generator("a~mkv") {
        import synth._; import ugen._
        val off     = cfg.offset
        val in      = PhysicalIn.ar(off)
        val pAmp    = pAudio("amp"      , ParamSpec(0.01,     1, ExpWarp), default =  0.1)
        val sig     = in * pAmp
        sig
      }
    }

    sCfg.micInputs.find(_.name == "m-dpa").foreach { cfg =>
      generator("a~dpa") {
        import synth._; import ugen._
        val off = cfg.offset
        val in  = PhysicalIn.ar(off)
        val gain = pAudio("gain", ParamSpec(-20, 20), default = 0).dbamp
        val sig  = in * gain
        sig
      }
    }

    generator("a~ff") {
      import synth._; import ugen._
      val pLo     = pAudio("lo"    , ParamSpec(0.0, 1.0), default = 0.0)
      val pHi     = pAudio("hi"    , ParamSpec(0.0, 1.0), default = 1.0)
      // val inTrig  = pAudioIn("trig", 1, ParamSpec(0.0, 1.0))
      val inTrig  = pAudio("trig", ParamSpec(0.0, 1.0), default = 0)

      val sig     = ToggleFF.ar(inTrig).linlin(0, 1, pLo, pHi)
      sig
    }

    generator("a~trig") {
      import synth._; import ugen._
      val pThresh = pAudio("thresh", ParamSpec(0.01, 1, ExpWarp), default = 0.1)
      val pLo     = pAudio("lo"    , ParamSpec(0.0, 1.0), default = 0.0)
      val pHi     = pAudio("hi"    , ParamSpec(0.0, 1.0), default = 1.0)
      val inTrig  = pAudio("trig"  , ParamSpec(0.0, 1.0), default = 0)
      val pDur    = pAudio("dur"   , ParamSpec(0.001, 1.0, ExpWarp), default = 0.01)

      val sig     = Trig1.ar(inTrig - pThresh, pDur).linlin(0, 1, pLo, pHi)
      sig
    }

    generator("a~step") {
      import synth._; import ugen._
      val pLo     = pAudio("lo"    , ParamSpec(0.0, 1.0), default = 0.0)
      val pHi     = pAudio("hi"    , ParamSpec(0.0, 1.0), default = 1.0)
      val pDiv    = pAudio("div"   , ParamSpec(1, 16, IntWarp), default = 1)
      // val inTrig  = pAudioIn("trig", 1, ParamSpec(0.0, 1.0))
      val inTrig  = pAudio("trig", ParamSpec(0.0, 1.0), default = 0)
      val reset   = pAudio("reset", ParamSpec(0.0, 1.0), default = 0)
      val sig     = Stepper.ar(inTrig, reset = reset, lo = 0, hi = pDiv).linlin(0, pDiv, pLo, pHi)
      sig
    }

    filter("norm") { in =>
      import synth._; import ugen._
      val amp   = pAudio("amp", ParamSpec(-40.0, -3.0, LinWarp), default = -40).dbamp
      val flt   = Normalizer.ar(in, level = amp, dur = 0.25)
      val pMix  = mkMix()
      mix(in, flt, pMix)
    }

    filter("mul") { in =>
      import synth._ // ; import ugen._
      val inB   = pAudio("mod", ParamSpec(0.0, 1.0), default = 0)
      val flt   = in * inB
      val pMix  = mkMix()
      mix(in, flt, pMix)
    }

    filter("L-lpf") { in =>
      import synth._; import ugen._
      val fade  = mkMix4()
      val freq  = fade.linexp(1, 0, 22.05 * 2, 20000) // 22050
      val wet   = LPF.ar(in, freq)
      mkBlend(in, wet, fade)
    }

    filter("L-hpf") { in =>
      import synth._; import ugen._
      val fade  = mkMix4()
      val freq  = fade.linexp(1, 0, 20000, 22.05 * 2)
      val wet   = HPF.ar(HPF.ar(in, freq), freq)
      mkBlend(in, wet, fade)
    }

    val FFTSize = 512

    filter("L-below") { in =>
      import synth._; import ugen._
      val fade    = mkMix4()
      val thresh  = fade.linexp(1, 0, 1.0e-3, 1.0e1)
      val buf     = LocalBuf(FFTSize)
      val wet     = IFFT.ar(PV_MagBelow(FFT(buf, in), thresh))
      mkBlend(in, wet, fade, FFTSize / SampleRate.ir)
    }

    filter("L-above") { in =>
      import synth._; import ugen._
      val fade    = mkMix4()
      val thresh  = fade.linexp(0, 1, 1.0e-3, 2.0e1)
      val buf     = LocalBuf(FFTSize)
      val wet     = IFFT.ar(PV_MagAbove(FFT(buf, in), thresh))
      mkBlend(in, wet, fade, FFTSize / SampleRate.ir)
    }

    filter("L-up") { in =>
      import synth._; import ugen._
      val fade    = mkMix4()
      val numSteps = 16 // 10
      val x        = (1 - fade) * numSteps
      val xh       = x / 2
      val a        = (xh + 0.5).floor        * 2
      val b0       = (xh       .floor + 0.5) * 2
      val b        = b0.min(numSteps)
      val ny       = 20000 // 22050
      val zero     = 22.05
      val aFreq    = a.linexp(numSteps, 0, zero, ny) - zero
      val bFreq    = b.linexp(numSteps, 0, zero, ny) - zero
      val freq: GE = Seq(aFreq, bFreq)

      val z0      = FreqShift.ar(LPF.ar(in, ny - freq),  freq)

      val zig     = x.fold(0, 1)
      val az      = zig     // .sqrt
    val bz      = 1 - zig // .sqrt
    val wet     = az * (z0 \ 1 /* aka ceil */) + bz * (z0 \ 0 /* aka floor */)

      mkBlend(in, wet, fade)
    }

    filter("L-down") { in =>
      import synth._; import ugen._
      val fade    = mkMix4()
      val numSteps = 16
      val x        = (1 - fade) * numSteps
      val xh       = x / 2
      val a        = (xh + 0.5).floor        * 2
      val b0       = (xh       .floor + 0.5) * 2
      val b        = b0.min(numSteps)
      val fd: GE   = Seq(a, b)
      val ny       = 20000 // 20000 // 22050
      val zero     = 22.05
      val freq1    = fd.linexp(0, numSteps, ny, zero)
      val freq2    = fd.linexp(0, numSteps, zero, ny) - zero

      val fltSucc   = HPF.ar(in, freq1)
      val z0        = FreqShift.ar(fltSucc, -freq1)

      val zig = x.fold(0, 1)
      val az  = zig      // .sqrt
      val bz  = 1 - zig  // .sqrt
      val wet = az * (z0 \ 1 /* aka ceil */) + bz * (z0 \ 0 /* aka floor */)

      mkBlend(in, wet, fade)
    }
  }
}
