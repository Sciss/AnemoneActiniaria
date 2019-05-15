/*
 *  Almat.scala
 *  (Anemone-Actiniaria)
 *
 *  Copyright (c) 2014-2019 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.anemone

import de.sciss.lucre.stm.Sys
import de.sciss.nuages.{DSL, ExponentialWarp, IntWarp, Nuages, ParamSpec, ScissProcs}
import de.sciss.synth.GE
import de.sciss.synth.proc.Proc
import de.sciss.synth.ugen._

object Almat {
  def any2stringadd: Any = ()

  def apply[S <: Sys[S]](dsl: DSL[S], sCfg: ScissProcs.Config, nConfig: Nuages.Config)
                            (implicit tx: S#Tx, n: Nuages[S]): Unit = {
    import dsl._
    import sCfg.generatorChannels

    def filterF   (name: String)(fun: GE => GE): Proc[S] =
      filter      (name, if (DSL.useScanFixed) generatorChannels else -1)(fun)

    def mkMix(df: Double = 0.0): GE = pAudio("mix", ParamSpec(0, 1), default(df))

    def default(in: Double): ControlValues =
      if (generatorChannels <= 0)
        in
      else
        Vector.fill(generatorChannels)(in)

    def mix(in: GE, flt: GE, mix: GE): GE = LinXFade2.ar(in, flt, mix * 2 - 1)

    filterF("hopf") { in =>
      val pCoupling = pAudio("coup" , ParamSpec(0.0005, 0.05, ExponentialWarp), default(0.0005))
      val pRadius   = pAudio("rad"  , ParamSpec(0.0, 1.0), default(1.0))
      val pSelect   = pAudio("sel"  , ParamSpec(0.0, 3.0, IntWarp), default(0.0))
      val pMix      = mkMix()
      val hopf      = Hopf.ar(in, coupling = pCoupling, radius = pRadius)
      val phase     = hopf.phase / math.Pi
      val omega     = hopf.omega / math.Pi
      val flt       =
        hopf.x * (pSelect sig_== 0) +
        hopf.y * (pSelect sig_== 1) +
        phase  * (pSelect sig_== 2) +
        omega  * (pSelect sig_== 3)
      mix(in, flt, pMix)
    }
  }
}
