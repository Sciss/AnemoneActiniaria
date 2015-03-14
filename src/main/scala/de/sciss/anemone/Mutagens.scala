/*
 *  Mutagens.scala
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

import de.sciss.lucre.event.Sys
import de.sciss.synth.GE
import de.sciss.{synth, nuages}
import de.sciss.nuages.{Nuages, ScissProcs, ParamSpec, ExpWarp, LinWarp, IntWarp}

import scala.collection.immutable.{IndexedSeq => Vec}

object Mutagens {
  def apply[S <: Sys[S]](dsl: nuages.DSL[S], sCfg: ScissProcs.Config, nCfg: Nuages.Config)
                        (implicit tx: S#Tx, n: Nuages[S]): Unit = {
    import dsl._

    val masterChansOption = nCfg.masterChannels

    generator("muta-quietsch") {
      import synth._; import ugen._
      val v11   = pAudio("p1"     , ParamSpec(0.0001, 1.0, ExpWarp), default = 0.014)
      val v14a  = 6286.0566 // pAudio("p2"     , ParamSpec(10, 10000, ExpWarp), default = 6286.0566)
      val v24   = pAudio("p3"    , ParamSpec(-0.0005, -5.0, ExpWarp), default = -1.699198)
      val amp   = pAudio("amp"    , ParamSpec(0.01,     1, ExpWarp), default =  0.1)
      val det   = pAudio("detune" , ParamSpec(1, 2), default = 1)

      val numOut  = if (sCfg.generatorChannels <= 0) masterChansOption.fold(2)(_.size) else sCfg.generatorChannels

      val v14: GE = Vec.tabulate(numOut) { ch =>
        val m = (ch: GE).linlin(0, (numOut - 1).max(1), 1, det)
        v14a * m
      }
      val v25   = Ringz.ar(v11, v24, v24)
      val v26   = 1.0
      val v27   = v26 trunc v25
      val v28   = v27 | v14
      val sig   = v28
      Limiter.ar(LeakDC.ar(sig), dur = 0.01) * amp
    }

    val numOut = if (sCfg.generatorChannels <= 0) masterChansOption.fold(2)(_.size) else sCfg.generatorChannels

    def mkDetune(in: GE, max: Double = 2): GE = {
      import synth._
      val det   = pAudio("detune" , ParamSpec( 1      ,    2              ), default =    1          )
      Vec.tabulate(numOut) { ch =>
        val m = (ch: GE).linlin(0, (numOut - 1).max(1), 1, det)
        in * m
      }
    }

    generator("muta-boing") {  // c5f81c53
      import synth._; import ugen._
      val v0a   = pAudio("p1"     , ParamSpec(  0.0015,    0.15  , ExpWarp), default =    0.014171208)
      val v1    = pAudio("p2"     , ParamSpec( 12     , 2586.9963, ExpWarp), default = 2586.9963     )
      val v2    = 42.465885
      val v3    = pAudio("p3"     , ParamSpec(-0.1    ,    0.0   , LinWarp), default =   -0.029823383)
      val amp   = pAudio("amp"    , ParamSpec( 0.01   ,    1     , ExpWarp), default =    0.1        )

      val v5  = 0.00444841
      val v0 = mkDetune(v0a)

      val v4  = CombL.ar(v1, v2, v0, v3)
      val v6  = Impulse.ar(v2, v4)
      val v9  = BRZ2.ar(v6)
      val v10 = Ringz.ar(v0, v9, v4)

      val v12 = 6371.5044
      val v13 = v12 | v1
      val v15 = LFCub.ar(v6, v13)
      val v16 = LinCongL.ar(v15, v5, v6, v1, v2)
      val v18 = 12.339876
      val v19 = Formant.ar(v10, v1, v18)
      val sig = v16 + v19
      Limiter.ar(LeakDC.ar(sig), dur = 0.01) * amp
    }

    generator("muta-wicket") {  // 7c89d6f8
      import synth._; import ugen._
      val v1a   = pAudio("p1"   , ParamSpec( 2   , 1000  , ExpWarp), default =  42.465885  )
      val amp   = pAudio("amp"  , ParamSpec( 0.01,    1  , ExpWarp), default =   0.1       )
      val v2    = pAudio("rhy"  , ParamSpec( 1   , 1000  , ExpWarp), default =   1.0962135 )
      val v7    = pAudio("hpf"  , ParamSpec( 0.02, 1000  , ExpWarp), default =   0.023961771)

      val v1 = mkDetune(v1a)
      val v3 = FSinOsc.ar(v2, v1)
      val v4 = Impulse.ar(v1, v3)
      val v5 = T2K.kr(v4)
      val v8 = 6911.0312
      val v12 = Ringz.ar(3, v5, v8)
      val v24 = HPF.ar(v12, v7)
      val sig = v24
      Limiter.ar(LeakDC.ar(sig), dur = 0.01) * amp
    }

    generator("muta-gas") { // ca0e2d3f
      import synth._; import ugen._
      val v0  = pAudio("p1"  , ParamSpec( 20, 10000  , ExpWarp), default = 2043.3861)
      val v1  = pAudio("p2"  , ParamSpec( 4, 400  , ExpWarp), default = 40.62856)
      val v2  = pAudio("p3"  , ParamSpec( 60, 10000, ExpWarp), default = 6911.0312)
      val v4  = pAudio("p4"  , ParamSpec( 0.001, 1.0, ExpWarp), default = 0.011513037)
      val v8  = pAudio("p5"  , ParamSpec( 10, 11025, ExpWarp), default = 10078.403)
      val v13 = pAudio("p6"  , ParamSpec( -800, 800, LinWarp), default = -415.97122)
      val v26 = pAudio("p7"  , ParamSpec( 0.002, 2.0, ExpWarp), default = 0.02286103)
      val v32a= pAudio("tr"  ,  ParamSpec( -1, 1, IntWarp), default = 1)
      val amp   = pAudio("amp"  , ParamSpec( 0.01,    1  , ExpWarp), default =   0.1       )
      val v5  = Ringz.ar(v4, v1, v2)
      val v6  = HPF.ar(v5, v1)
      val v10 = LFCub.ar(v8, v2)
      val v11 = Blip.ar(v10, v1)
      val v20 = GbmanL.ar(v6, v1, v5)
      val v25 = PanB.ar(v0, v11, v20, v1)
      val v27 = SyncSaw.ar(v1, v26)
      val v28 = LFDNoise3.ar(Seq.fill(numOut)(v2))
      val v30 = SyncSaw.ar(v1, v13)
      val v31 = LPZ2.ar(v30)
      val v32 = LFDNoise3.ar((Seq.fill(numOut)(v31): GE) * v32a)
      // v32.poll(1, "v32")
      // val v32 = Sweep.ar("v32" ar 1, 1000).min(10000)
      val roots = Vector(v32, v28, v27, v25)
      val sig = Mix(roots)
      Limiter.ar(LeakDC.ar(sig), dur = 0.01) * amp
    }

    generator("muta-towtow") {
      import synth._; import ugen._
      val amp = pAudio("amp" , ParamSpec( 0.01,    1  , ExpWarp), default =   0.1       )
      val v4  = pAudio("p1"  , ParamSpec( -2000, 2000, LinWarp), default = -415.97122)
      val v10 = pAudio("p2"  , ParamSpec( 0.1, 4000  , ExpWarp), default =   40.62856)
      val v27 = pAudio("p3"  , ParamSpec( 0.001, 1.0, ExpWarp), default =    0.011513037)
      val v17 = pAudio("p4"  , ParamSpec( 0.1, 100, ExpWarp), default =    9.211388)
      val v73 = pAudio("p5"  , ParamSpec( -2000, 800 , LinWarp), default = -415.97122)
      val v74 = pAudio("p6"  , ParamSpec( 0.1, 4000, ExpWarp), default =   42.465885)
      val v0a = 500.0
      val v0 = LFDNoise0.ar(Seq.fill(numOut)(v0a))
      val v1 = 44.494984
      val v2 = RunningMax.ar(v1, v0)
      val v5 = SyncSaw.ar(v2, v4)
      val v6 = Select.ar(v0, v5)
      val v7 = -5497.8496
      val v8 = Pulse.ar(v6, v7)
      val v9 = LFDNoise3.ar(v8)
      val v11 = 6911.0312
      val v12 = SinOsc.ar(v10, v11)
      val v13 = BRZ2.ar(v12)
      val v14 = BRZ2.ar(v12)
      val v15 = Pulse.ar(v9, v14)
      val v16 = LFDNoise3.ar(v13)
      val v18 = 4.2621555
      val v19 = BRZ2.ar(v18)
      val v20 = SyncSaw.ar(v2, v4)
      val v21 = Select.ar(v20, v19)
      val v22 = 0.023961771
      val v23 = LinXFade2.ar(v21, v5, v17, v22)
      val v24 = 678.952
      val v25 = LFTri.ar(v23, v24)
      val v26 = HPF.ar(v25, v16)
      val v28 = -1.24139
      val v29 = T2K.kr(v28)
      val v30 = 2043.3861
      val v31 = Select.ar(v30, v29)
      val v32 = SinOsc.ar(v31, v27)
      val v33 = SinOsc.ar(v14, v27)
      val v34 = 0.024866452
      val v35 = -407.08
      val v36 = LinXFade2.ar(v35, v25, v33, v34)
      val v37 = 0.09739249
      val v38 = 148.62299
      val v39 = LFClipNoise.ar(Seq.fill(numOut)(v38))
      val v40 = HPF.ar(v39, v37)
      val v41 = BRZ2.ar(v23)
      val v42 = BRZ2.ar(v12)
      val v43 = 968.6194: GE
      val v44 = 2.9921034E-4: GE
      val v45 = v44 <= v43
      val v46 = ToggleFF.ar(v45)
      val v47 = 0.023961771
      val v48 = ToggleFF.ar(v47)
      val v49 = 0.0058684507
      val v50 = TDelay.ar(v32, v49)
      val v51 = TwoPole.ar(v31, v30, 0.8)
      val v52 = 0.069197014
      val v53 = LeastChange.ar(v24, v52)
      val v54 = 40.62856
      val v55 = 0.011513037
      val v56 = 0.09739249
      val v57 = HPF.ar(v52, v56)
      val v58 = v55 excess v57
      val v59 = v54 hypotx v58
      val v60 = 0.023961771
      val v61 = 0.0049379473
      val v62 = Trig1.ar(v60, v61)
      val v63 = PanB.ar(v59, v54, v62, v53) \ 0
      val v64 = 233.4438
      val v65 = 0.0049379473
      val v66 = MantissaMask.ar(v65, v64)
      val v67 = 0.011513037
      val v68 = Trig1.ar(v66, v67)
      val v69 = 6911.0312
      val v70 = LFDNoise3.ar(Seq.fill(numOut)(v69))
      val v71 = 0.02286103
      val v72 = SinOsc.ar(v70, v71)
      val v75 = BRZ2.ar(v74)
      val v76 = SyncSaw.ar(v75, v73)
      val v77 = LFDNoise0.ar(Seq.fill(numOut)(v76))
      val v78 = LPZ2.ar(v76)
      val roots = Vector(v78, v77, v72, v68, v63, v51, v50, v48, v46, v42,
        v41, v40, v36, v26, v15)
      val sig = Mix(roots)
      Limiter.ar(LeakDC.ar(sig), dur = 0.01) * amp
    }
  }
}