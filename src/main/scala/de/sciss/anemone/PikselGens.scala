package de.sciss.anemone

import de.sciss.lucre.synth.Txn
import de.sciss.nuages.{DSL, Nuages, ScissProcs}
import de.sciss.proc
import de.sciss.proc.{ParamSpec, Warp}
import de.sciss.synth.GE

object PikselGens {
  def apply[T <: Txn[T]](dsl:DSL[T], sConfig: ScissProcs.Config, nConfig: Nuages.Config)
                        (implicit tx: T, nuages: Nuages[T]): Unit = {
    import dsl._
    import sConfig.genNumChannels

//    implicit val _nuages: Nuages[T] = nuages

    import de.sciss.synth.ugen._
    import de.sciss.synth.GEOps._

    def filterF   (name: String)(fun: GE => GE): proc.Proc[T] =
      filter      (name, if (DSL.useScanFixed) genNumChannels else -1)(fun)

    def default(in: Double): ControlValues =
      if (genNumChannels <= 0)
        in
      else
        Vector.fill(genNumChannels)(in)

    def mix(in: GE, flt: GE, mix: GE): GE = LinXFade2.ar(in, flt, mix * 2 - 1)
    def mkMix(df: Double = 0.0): GE = pAudio("mix", ParamSpec(0, 1), default(df))

    // latch and quantize
    filterF("quatch") { in =>
      shortcut = "Q"
      val pQuant    = pAudio("quant" , ParamSpec(0.0001, 1.0, Warp.Exp), default(0.01))
      val pLatchAmt = pAudio("amt"    , ParamSpec(0.0, 1.0), default(0))
      val pLatchIn  = pAudio("mod"   , ParamSpec(0.0, 1.0), default(0.0))
      val pLeak     = pAudio("leak"  , ParamSpec(0, 1, Warp.Int), default(1))
      val pMix      = mkMix()

      val quant     = in.roundTo(pQuant)
      val latch     = Latch.ar(quant, pLatchIn)
      val sig       = quant * (-pLatchAmt + 1) + latch * pLatchAmt
      val leak      = LeakDC.ar(sig)
      val flt       = sig * (-pLeak + 1) + leak * pLeak

      mix(in, flt, pMix)
    }
  }
}
