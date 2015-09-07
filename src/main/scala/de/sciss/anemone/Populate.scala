package de.sciss.anemone

import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import java.util.{Date, Locale}

import de.sciss.file._
import de.sciss.lucre.artifact.{Artifact, ArtifactLocation}
import de.sciss.lucre.synth.Sys
import de.sciss.lucre.stm
import de.sciss.nuages.{DbFaderWarp, ExpWarp, IntWarp, NamedBusConfig, Nuages, ParamSpec, ScissProcs, TrigSpec}
import de.sciss.synth.GE
import de.sciss.synth.io.AudioFile
import de.sciss.synth.proc.Implicits._
import de.sciss.synth.proc.graph.ScanOut
import de.sciss.synth.proc.{Action, Folder, Grapheme, Proc, SoundProcesses}
import de.sciss.synth.ugen.Constant
import de.sciss.{nuages, synth}

import scala.concurrent.stm.Ref

object Populate {
  private final val DEBUG = true

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

  def mkLoop[S <: stm.Sys[S]](root: Folder[S], artObj: Artifact[S])(implicit tx: S#Tx): Unit = {
    val dsl = new nuages.DSL[S]
    import dsl._
    // val imp = ExprImplicits[S]

    val f = artObj.value

    val procObj = mkProcObj(f.base) {
      import synth._
      import ugen._
      val pSpeed      = pAudio  ("speed", ParamSpec(0.125, 2.3511, ExpWarp), default = 1)
      val pStart      = pControl("start", ParamSpec(0, 1), default = 0)
      val pDur        = pControl("dur"  , ParamSpec(0, 1), default = 1)
      val bufID       = proc.graph.Buffer("file")
      val loopFrames  = BufFrames.kr(bufID)

      val trig1       = LocalIn.kr(1)
      val gateTrig1   = PulseDivider.kr(trig = trig1, div = 2, start = 1)
      val gateTrig2   = PulseDivider.kr(trig = trig1, div = 2, start = 0)
      val startFrame  = pStart * loopFrames
      val numFrames   = pDur * (loopFrames - startFrame)
      val lOffset     = Latch.kr(in = startFrame, trig = trig1)
      val lLength     = Latch.kr(in = numFrames, trig = trig1)
      val speed       = A2K.kr(pSpeed)
      val duration    = lLength / (speed * SampleRate.ir) - 2
      val gate1       = Trig1.kr(in = gateTrig1, dur = duration)
      val env         = Env.asr(2, 1, 2, Curve.lin) // \sin
      // val bufID       = Select.kr(pBuf, loopBufIDs)
      val play1       = PlayBuf.ar(2, bufID, speed, gateTrig1, lOffset, loop = 0) // XXX TODO - numChannels
      val play2       = PlayBuf.ar(2, bufID, speed, gateTrig2, lOffset, loop = 0)
      val amp0        = EnvGen.kr(env, gate1) // 0.999 = bug fix !!!
      val amp2        = 1.0 - amp0.squared
      val amp1        = 1.0 - (1.0 - amp0).squared
      val sig         = (play1 * amp1) + (play2 * amp2)
      LocalOut.kr(Impulse.kr(1.0 / duration.max(0.1)))
      ScanOut(Proc.scanMainOut, sig)
    }
    procObj.outputs.add(Proc.scanMainOut)
    val spec  = AudioFile.readSpec(f)
    if (DEBUG) println("mkloop ---1")
    val gr    = Grapheme.Expr.Audio(artObj, spec, 0L, 1.0)
    procObj.attr.put("file", gr) // Obj(Grapheme.Expr.Audio(gr)))

    if (DEBUG) println(s"mkloop ---2. root = $root")

    val nuagesOpt = getNuages(root)
    nuagesOpt.foreach { nuagesObj =>
      val genOpt = nuagesObj.folder / Nuages.NameGenerators
      genOpt.foreach {
        case genF: Folder[S] =>
          if (DEBUG) println("mkloop ---3")
          insertByName(genF, procObj)
          if (DEBUG) println("mkloop ---4")
        case _ =>
      }
    }
  }

  private final val ActionKeyRecPrepare = "nuages-prepare-rec"
  private final val ActionKeyRecDispose = "nuages-dispose-rec"

  private val _registerActions = Ref(initialValue = false)

  private final val KeyRecArtifact  = "file"

  def registerActions[S <: Sys[S]]()(implicit tx: S#Tx): Unit = {
    if (_registerActions.swap(true)(tx.peer)) return    // already registered

    val recFormat = new SimpleDateFormat("'rec_'yyMMdd'_'HHmmss'.aif'", Locale.US)
    // val recFormat = new SimpleDateFormat("'rec_'HHmmss'.aif'", Locale.US)

    val sinkRecPrepare = new Action.Body {
      def apply[T <: stm.Sys[T]](universe: Action.Universe[T])(implicit tx: T#Tx): Unit = {
        if (DEBUG) println("prepare ---1")
        import universe._
        invoker.foreach {
          case procObj: Proc[T] =>
            getNuages(root).foreach { nuagesObj =>
              if (DEBUG) println("prepare ---2")
              val name    = recFormat.format(new Date)
              val loc     = getRecLocation(nuagesObj.folder)
              val artM    = Artifact[T](loc, Artifact.Child(name)) // loc.add(loc.directory / name) // XXX TODO - should check that it is different from previous value
              // println(name)
              procObj.attr.put(KeyRecArtifact, artM) // Obj(ArtifactElem(artM)))
              if (DEBUG) println(s"prepare ---3: $name")
            }

          case _ =>
        }
      }
    }
    Action.registerPredef(ActionKeyRecPrepare, sinkRecPrepare)

    val sinkRecDispose = new Action.Body {
      def apply[T <: stm.Sys[T]](universe: Action.Universe[T])(implicit tx: T#Tx): Unit = {
        if (DEBUG) println("dispose ---1")
        import universe._
        invoker.foreach {
          case procObj: Proc[T] =>
            procObj.attr.$[Artifact](KeyRecArtifact).foreach { artObj =>
              if (DEBUG) println("dispose ---2")
              SoundProcesses.scheduledExecutorService.schedule(new Runnable {
                def run(): Unit = SoundProcesses.atomic[T, Unit] { implicit tx =>
                  // println(f)
                  if (DEBUG) println("dispose ---3")
                  mkLoop[T](root, artObj)
                }
              }, 1000, TimeUnit.MILLISECONDS)
            }

          case _ =>
        }
      }
    }
    Action.registerPredef(ActionKeyRecDispose, sinkRecDispose)
  }

  private final val RecName = "rec"

  final val BaseDir = userHome/"Documents"/"applications"/"140616_SteiermarkRiga"/"tallin"

  private final val RecDir  = BaseDir / "rec"

  final val NuagesName = "Nuages"

  def getNuages[S <: stm.Sys[S]](root: Folder[S])(implicit tx: S#Tx): Option[Nuages[S]] =
    (root / NuagesName).flatMap {
      case n: Nuages[S] => Some(n)
      case _ => None
    }

  def getRecLocation[S <: stm.Sys[S]](root: Folder[S])(implicit tx: S#Tx): ArtifactLocation[S] = {
    val it = root.iterator.flatMap {
      case objT: ArtifactLocation[S] if objT.name == RecName => Some(objT) // .modifiableOption
      case _ => None
    }
    if (it.hasNext) it.next() else {
      if (!RecDir.exists()) tx.afterCommit(RecDir.mkdirs())
      val newLoc    = ArtifactLocation.newVar[S](RecDir)
      val newLocObj = newLoc // Obj(ArtifactLocationElem(newLoc))
      newLocObj.name = RecName
      root.modifiableOption.foreach(_.addLast(newLocObj))
      newLoc
    }
  }

  def apply[S <: Sys[S]](n: Nuages[S], nConfig: Nuages.Config, sConfig: ScissProcs.Config)
                        (implicit tx: S#Tx, cursor: stm.Cursor[S]): Unit = {
    implicit val _n = n
    val dsl = new nuages.DSL[S]
    import dsl._
    import synth._
    import ugen._

    Mutagens          (dsl, sConfig, nConfig)
    FifteenBeeThreeCee(dsl, sConfig, nConfig)

    val masterChansOption = nConfig.masterChannels

    def ForceChan(in: GE): GE = if (sConfig.generatorChannels <= 0) in else {
      WrapExtendChannels(sConfig.generatorChannels, in)
    }

    def mix(in: GE, flt: GE, mix: GE): GE = LinXFade2.ar(in, flt, mix * 2 - 1)
    def mkMix(): GE = pAudio("mix", ParamSpec(0, 1), default = 0)

    def mkMix4(): GE = {
      val f1 = pAudio("mix1", ParamSpec(0, 1), default = 0)
      val f2 = pAudio("mix2", ParamSpec(0, 1), default = 0)
      Lag.ar(Seq(f1, f1 * 0.667 + f2 * 0.333, f1 * 0.333, f2 * 0.667, f2))
    }

    def WrapExtendChannels(n: Int, sig: GE): GE = Vector.tabulate(n)(sig \ _)

    sConfig.audioFilesFolder.foreach { folder =>
      val loc = ArtifactLocation.newVar[S](folder)

      def abbreviate(s: String) = if (s.length < 16) s else s"${s.take(7)}...${s.takeRight(7)}"

      val audioFiles = folder.children
      audioFiles.filter(AudioFile.identify(_).isDefined).foreach(f => {
        val name = s"t-${abbreviate(f.base)}"

        val procObj = generator(name) {
          val p1    = pAudio("speed", ParamSpec(0.1f, 10f, ExpWarp), 1)
          val speed = p1
          val disk  = proc.graph.VDiskIn.ar("file", speed = speed, loop = 1)
          //          val b     = bufCue(path)
          //          val disk  = VDiskIn.ar(b.numChannels, b.id, p1.ar * BufRateScale.ir(b.id), loop = 1)
          // HPF.ar( disk, 30 )
          val sig = ForceChan(disk)
          sig
        }

        val art   = Artifact(loc, f) // loc.add(f)
        val spec  = AudioFile.readSpec(f)
        val gr    = Grapheme.Expr.Audio(art, spec, 0L, 1.0)
        procObj.attr.put("file", gr) // Obj(Grapheme.Expr.Audio(gr)))
      })
    }

    masterChansOption.foreach { masterChans =>
      val numChans = masterChans.size

      generator("(test)") {

        val pAmp  = pControl("amp" , ParamSpec(0.01,  1.0, ExpWarp),  default = 1)
        val pSig  = pControl("sig" , /* ParamSpec(0, 2, IntWarp) */ TrigSpec, default = 0)
        val pFreq = pControl("freq", ParamSpec(0.1 , 10  , ExpWarp),  default = 1)

        val idx       = Stepper.kr(Impulse.kr(pFreq), lo = 0, hi = numChans)
        val sig0: GE  = Seq(WhiteNoise.ar(1), SinOsc.ar(441))
        val sig       = Select.ar(pSig, sig0) * pAmp
        val sigOut: GE = Seq.tabulate(numChans)(ch => sig * (1 - (ch - idx).abs.min(1)))
        sigOut
      }
    }

    // val loopFrames  = (sConfig.loopDuration * 44100 /* config.server.sampleRate */).toInt
    //    val loopBuffers = Vec.fill[Buffer](config.numLoops)(Buffer.alloc(config.server, loopFrames, 2))
    //    val loopBufIDs  = loopBuffers.map(_.id)

    //    if (settings.numLoops > 0) {

    //      masterBusOption.foreach { masterBus =>
    //        gen( "sum_rec" ) {
    //          val pbuf    = pControl( "buf",  ParamSpec( 0, settings.numLoops - 1, LinWarp, 1 ), 0 )
    //          val pfeed   = pControl( "feed", ParamSpec( 0, 1 ), 0 )
    //          val ploop   = pScalar( "loop", ParamSpec( 0, 1, LinWarp, 1 ), 0 )
    //          /* val ppos = */ pControl( "pos", ParamSpec( 0, 1 ), 0 )
    //          graph {
    //            val in      = InFeedback.ar( masterBus.index, masterBus.numChannels )
    //            val w       = 2.0 / in.numChannels // numOutputs
    //            val sig     = SplayAz.ar( 2, in )
    //
    //            val sig1    = LeakDC.ar( Limiter.ar( sig /* .toSeq */ * w ))
    //            val bufID   = Select.kr( pbuf.kr, loopBufIDs )
    //            val feed    = pfeed.kr
    //            val prelvl  = feed.sqrt
    //            val reclvl  = (1 - feed).sqrt
    //            val loop    = ploop.ir
    //            val rec     = RecordBuf.ar( sig1, bufID, recLevel = reclvl, preLevel = prelvl, loop = loop )
    //
    //            // pos feedback
    //            val bufFr   = BufFrames.kr( bufID )
    //            val pos     = Phasor.kr( 1, SampleRate.ir/ControlRate.ir, 0, bufFr * 2 ) / bufFr // BufDur.kr( bufID ).reciprocal
    //            val me      = Proc.local
    //            val lp0     = ploop.v
    //            Impulse.kr( 10 ).react( pos ) { smp => ProcTxn.spawnAtomic { implicit tx =>
    //              val pos0 = smp( 0 )
    //              // not sure we can access them in this scope, so just retrieve the controls...
    //              val ppos = me.control( "pos" )
    //              ppos.v   = if( lp0 == 1 ) (pos0 % 1.0) else pos0.min( 1.0 )
    //            }}
    //
    //            Done.kr( rec ).react { ProcTxn.spawnAtomic { implicit tx => me.stop }}
    //
    //            Silent.ar( 2 )// dummy thru
    //          }
    //        }
    //      }
    //    }

    sConfig.micInputs.foreach { cfg =>
      generator(cfg.name) {
        val pBoost    = pAudio("gain", ParamSpec(0.1, 10, ExpWarp), default = 0.1 /* 1 */)
        val pFeed     = pAudio("feed", ParamSpec(0, 1), default = 0)

        val boost     = pBoost
        val pureIn    = In.ar(NumOutputBuses.ir + cfg.offset, cfg.numChannels) * boost
        val bandFrequencies = List(150, 800, 3000)
        val ins       = HPZ1.ar(pureIn) // .outputs
        var outs: GE = 0
        var flt: GE = ins
        bandFrequencies.foreach { maxFreq =>
          val band = if (maxFreq != bandFrequencies.last) {
            val res = LPF.ar(flt, maxFreq)
            flt     = HPF.ar(flt, maxFreq)
            res
          } else {
            flt
          }
          val amp   = Amplitude.kr(band, 2, 2)
          val slope = Slope.kr(amp)
          val comp  = Compander.ar(band, band, 0.1, 1, slope.max(1).reciprocal, 0.01, 0.01)
          outs      = outs + comp
        }
        val dly   = DelayC.ar(outs, 0.0125, LFDNoise1.kr(5).madd(0.006, 0.00625))
        val feed  = pFeed * 2 - 1
        val sig   = XFade2.ar(pureIn, dly, feed)

        // val numOut = masterChansOption.fold(2)(_.size)
        val numOut = if (sConfig.generatorChannels <= 0) masterChansOption.fold(2)(_.size) else sConfig.generatorChannels

        val sig1: GE = if (numOut == cfg.numChannels) {
          sig
        } else if (cfg.numChannels == 1) {
          Seq.fill[GE](numOut)(sig)
        } else {
          SplayAz.ar(numOut, sig)
        }
        sig1
      }
    }

    sConfig.lineInputs.foreach { cfg =>
      generator(cfg.name) {
        val pBoost = pAudio("gain", ParamSpec(0.1, 10, ExpWarp), default = 1)

        val boost   = pBoost
        val sig     = In.ar(NumOutputBuses.ir + cfg.offset, cfg.numChannels) * boost
        // val numOut  = masterChansOption.fold(2)(_.size)
        val numOut  = if (sConfig.generatorChannels <= 0) masterChansOption.fold(2)(_.size) else sConfig.generatorChannels

        val sig1: GE = if (numOut == cfg.numChannels) {
          sig
        } else if (cfg.numChannels == 1) {
          Seq.fill[GE](numOut)(sig)
        } else {
          SplayAz.ar(numOut, sig)
        }
        sig1
      }
    }

    // -------------- SIGNAL GENERATORS --------------

    generator("~dust") {
      val pFreq   = pAudio("freq" , ParamSpec(0.01, 1000, ExpWarp), default = 0.1 /* 1 */)
      val pDecay  = pAudio("decay", ParamSpec(0.1 ,   10, ExpWarp), default = 0.1 /* 1 */)

      val freq0 = pFreq
      val freq  = ForceChan(freq0)
      Decay.ar(Dust.ar(freq), pDecay)
    }

    generator("~gray") {
      val pAmp  = pAudio("amp", ParamSpec(0.01, 1, ExpWarp), default = 0.1)
      val amp0  = pAmp
      val amp   = ForceChan(amp0)
      GrayNoise.ar(amp)
    }

    generator("~sin") {
      val pFreq1  = pAudio("freq"     , ParamSpec(0.1 , 10000, ExpWarp), default = 15 /* 1 */)
      val pFreq2  = pAudio("freq-fact", ParamSpec(0.01,   100, ExpWarp), default =  1 /* 1 */)
      val pAmp    = pAudio("amp"      , ParamSpec(0.01,     1, ExpWarp), default =  0.1)

      val numOut  = if (sConfig.generatorChannels <= 0) masterChansOption.fold(2)(_.size) else sConfig.generatorChannels

      // val f1 = pFreq1
      // val f2 = f1 * pFreq2
      val freq = Vector.tabulate(numOut) { ch =>
        val w = (ch: GE).linlin(0, (numOut - 1).max(1), 1, pFreq2)
        (pFreq1 * w).clip(0.01, 20000)
      }
      SinOsc.ar(freq) * pAmp
    }

    generator("~pulse") {
      val pFreq1  = pAudio("freq"     , ParamSpec(0.1 , 10000, ExpWarp), default = 15 /* 1 */)
      val pFreq2  = pAudio("freq-fact", ParamSpec(0.01,   100, ExpWarp), default =  1 /* 1 */)
      val pw1     = pAudio("width1"   , ParamSpec(0.0 ,     1.0),        default =  0.5)
      val pw2     = pAudio("width2"   , ParamSpec(0.0 ,     1.0),        default =  0.5)
      val pAmp    = pAudio("amp"      , ParamSpec(0.01,     1, ExpWarp), default =  0.1)

      //      val f1 = pFreq1
      //      val f2 = f1 * pFreq2
      //      val w1 = pw1
      //      val w2 = pw2

      // val numOut = masterChansOption.fold(2)(_.size)
      val numOut  = if (sConfig.generatorChannels <= 0) masterChansOption.fold(2)(_.size) else sConfig.generatorChannels

      val sig: GE = Vector.tabulate(numOut) { ch =>
        val freqM = (ch: GE).linlin(0, (numOut - 1).max(1), 1, pFreq2)
        val freq  = (pFreq1 * freqM).clip(0.01, 20000)
        val width = (ch: GE).linlin(0, (numOut - 1).max(1), pw1, pw2)
        Pulse.ar(freq, width)
      }

      // Pulse.ar(f1 :: f2 :: Nil, w1 :: w2 :: Nil) * pAmp
      sig * pAmp
    }

    // -------------- FILTERS --------------

    filter("staub") { in =>
      val pAmt      = pAudio("amt" , ParamSpec(0.0, 1.0), default = 1.0)
      val pFact     = pAudio("fact", ParamSpec(0.5, 2.0, ExpWarp), default = 1)
      val pMix      = mkMix()

      val f1        =   10
      val f2        = 2000

      val relIdx    = ChannelIndices(in) / (NumChannels(in) - 1).max(1)
      val fade      = (pAmt * relIdx.linlin(0, 1, 1, pFact)).clip(0, 1)
      val dustFreqS = fade.linexp(0, 1, f1, f2)
      // val dustFreqP = fade.linexp(1, 0, f1, f2)

      val decayTime = 0.01
      val dustS     = Decay.ar(Dust.ar(dustFreqS), decayTime).min(1)
      // val dustP = Decay.ar(Dust.ar(dustFreqP), decayTime).min(1)

      val flt       = in * dustS

      mix(in, flt, pMix)
    }

    filter("delay") { in =>
      val pTime       = pAudio("time", ParamSpec(0.03, 30.0, ExpWarp), default = 10)
      val pFeed       = pAudio("feed", ParamSpec(0.001, 1.0, ExpWarp), default = 0.001)
      val pMix        = mkMix()

      val numFrames   = SampleRate.ir * 30
      val buf         = LocalBuf(numFrames = numFrames, numChannels = Pad(1, in))
      val time        = Lag.ar(pTime)
      val lin         = Pad.LocalIn.ar(in)
      val feed        = pFeed
      val wDry        = (1 - feed).sqrt
      val wWet        = feed.sqrt
      val flt0        = BufDelayL.ar(buf, (in * wDry) + (lin * wWet), time)
      val flt         = LeakDC.ar(flt0)
      LocalOut.ar(flt)

      mix(in, flt, pMix)
    }

    filter("mantissa") { in =>
      val pBits = pAudio("bits", ParamSpec(2, 14, IntWarp), default = 14)
      val pMix  = mkMix()

      val flt = MantissaMask.ar(in, pBits)
      mix(in, flt, pMix)
    }

    filter("achil") { in =>
      shortcut = "A"
      val pSpeed  = pAudio("speed", ParamSpec(0.125, 2.3511, ExpWarp), default = 0.5)
      val pMix    = mkMix()

      val speed       = Lag.ar(pSpeed, 0.1)
      val numFrames   = SampleRate.ir // sampleRate.toInt
    val bufID       = LocalBuf(numFrames = numFrames, numChannels = Pad(1, in))
      val writeRate   = BufRateScale.kr(bufID)
      val readRate    = writeRate * speed
      val readPhasor  = Phasor.ar(0, readRate, 0, numFrames)
      val read0       = BufRd.ar(1, bufID, readPhasor, 0, 4)
      val readBad     = CheckBadValues.ar(read0, id = 1000)
      val read        = Gate.ar(read0, readBad sig_== 0)

      val writePhasor = Phasor.ar(0, writeRate, 0, numFrames)
      val old         = BufRd.ar(1, bufID, writePhasor, 0, 1)
      val wet0        = SinOsc.ar(0, (readPhasor - writePhasor).abs / numFrames * math.Pi)
      val dry         = 1 - wet0.squared
      val wet         = 1 - (1 - wet0).squared
      val write0      = (old * dry) + (in * wet)
      val writeBad    = CheckBadValues.ar(write0, id = 1001)
      val writeSig    = Gate.ar(write0, writeBad sig_== 0)

      // NOTE: `writeSig :: Nil: GE` does _not_ work because single
      // element seqs are not created by that conversion.
      BufWr.ar(Pad.Split(writeSig), bufID, writePhasor)
      mix(in, read, pMix)
    }

    filter("a-gate") { in =>
      val pAmt = pAudio("amt", ParamSpec(0, 1), default = 1)
      val pMix = mkMix()

      val amount  = Lag.ar(pAmt, 0.1)
      val flt     = Compander.ar(in, in, Amplitude.ar(in * (1 - amount) * 5), 20, 1, 0.01, 0.001)
      mix(in, flt, pMix)
    }

    filter("a-hilb") { in =>
      val pMix = mkMix()
      val hlb   = Hilbert.ar(DelayN.ar(in, 0.01, 0.01))
      val hlb2  = Hilbert.ar(Normalizer.ar(in, dur = 0.02))
      val flt   = hlb.real * hlb2.real - hlb.imag * hlb2.imag
      mix(in, flt, pMix)
    }

    filter("hilbert") { in =>
      shortcut = "H"
      val pFreq = pAudio("freq", ParamSpec(-1, 1), 0.0)
      val pMix  = mkMix()

      val freq    = pFreq
      val freqHz  = freq.abs.linexp(0, 1, 20, 12000) * freq.signum
      val flt     = FreqShift.ar(in, freqHz)
      mix(in, flt, pMix)
    }

    filter("reso") { in =>
      shortcut = "R"
      val pFreq   = pAudio("freq"     , ParamSpec(30  , 13000, ExpWarp), default = 400) // beware of the upper frequency
    val pFreq2  = pAudio("freq-fact", ParamSpec( 0.5,     2, ExpWarp), default =   1)
      val pq      = pAudio("q"        , ParamSpec( 0.5,    50, ExpWarp), default =   1)
      val pMix    = mkMix()

      val freq0   = pFreq
      val freq    = freq0 :: (freq0 * pFreq2).max(30).min(13000) :: Nil
      val rq      = pq.reciprocal
      val makeUp  = pq // (rq + 0.5).pow(1.41) // rq.max( 1 ) // .sqrt
    val flt     = Resonz.ar(in, freq, rq) * makeUp
      mix(in, flt, pMix)
    }

    filter("notch") { in =>
      shortcut = "N"
      val pFreq   = pAudio("freq", ParamSpec(30, 16000, ExpWarp), default = 400)
      val pFreq2  = pAudio("freq-fact", ParamSpec(0.5, 2, ExpWarp), default = 1)
      val pq      = pAudio("q", ParamSpec(1, 50, ExpWarp), default = 1) // beware of the lower q
    val pMix    = mkMix()

      val freq0   = pFreq
      val freq    = freq0 :: (freq0 * pFreq2).max(30).min(16000) :: Nil
      val rq      = pq.reciprocal
      val flt     = BRF.ar(in, freq, rq)
      mix(in, flt, pMix)
    }

    filter("filt") { in =>
      shortcut = "F"
      val pFreq = pAudio("freq", ParamSpec(-1, 1), default = 0.54)
      val pMix  = mkMix()

      val normFreq  = pFreq
      val lowFreqN  = Lag.ar(Clip.ar(normFreq, -1, 0))
      val highFreqN = Lag.ar(Clip.ar(normFreq, 0, 1))
      val lowFreq   = LinExp.ar(lowFreqN, -1, 0, 30, 20000)
      val highFreq  = LinExp.ar(highFreqN, 0, 1, 30, 20000)
      val lowMix    = Clip.ar(lowFreqN * -10.0, 0, 1)
      val highMix   = Clip.ar(highFreqN * 10.0, 0, 1)
      val dryMix    = 1 - (lowMix + highMix)
      val lpf       = LPF.ar(in, lowFreq) * lowMix
      val hpf       = HPF.ar(in, highFreq) * highMix
      val dry       = in * dryMix
      val flt       = dry + lpf + hpf
      mix(in, flt, pMix)
    }

    filter("frgmnt") { in =>
      val pSpeed      = pAudio  ("speed", ParamSpec(0.125, 2.3511, ExpWarp), default = 1)
      val pGrain      = pControl("grain", ParamSpec(0, 1), default = 0.5)
      val pFeed       = pAudio  ("fb"   , ParamSpec(0, 1), default = 0)
      val pMix        = mkMix()

      val bufDur      = 4.0
      val numFrames   = bufDur * SampleRate.ir
      //      val numChannels = in.numChannels // numOutputs
      //      val buf         = bufEmpty(numFrames, numChannels)
      //      val bufID       = buf.id
      val buf         = LocalBuf(numFrames = numFrames, numChannels = Pad(1, in))

      val feedBack    = Lag.ar(pFeed, 0.1)
      val grain       = pGrain // Lag.kr( grainAttr.kr, 0.1 )
    val maxDur      = LinExp.kr(grain, 0, 0.5, 0.01, 1.0)
      val minDur      = LinExp.kr(grain, 0.5, 1, 0.01, 1.0)
      val fade        = LinExp.kr(grain, 0, 1, 0.25, 4)
      val rec         = (1 - feedBack).sqrt
      val pre         = feedBack.sqrt
      val trig        = LocalIn.kr(1)
      val white       = TRand.kr(0, 1, trig)
      val dur         = LinExp.kr(white, 0, 1, minDur, maxDur)
      val off0        = numFrames * white
      val off         = off0 - (off0 % 1.0)
      val gate        = trig
      val lFade       = Latch.kr(fade, trig)
      val fadeIn      = lFade * 0.05
      val fadeOut     = lFade * 0.15
      val env         = EnvGen.ar(Env.linen(fadeIn, dur, fadeOut, 1, Curve.sine), gate, doneAction = 0)
      val recLevel0   = env.sqrt
      val preLevel0   = (1 - env).sqrt
      val recLevel    = recLevel0 * rec
      val preLevel    = preLevel0 * (1 - pre) + pre
      // val run         = recLevel > 0
      //      run     .poll(1, "run"      )
      //      recLevel.poll(1, "rec-level")
      // val ins = Pad.Split(in)
      // ins.poll(1, "ins")
      // RecordBuf.ar(ins, buf = buf, offset = off, recLevel = recLevel, preLevel = preLevel, run = run, loop = 1)
      val writeIdx    = Phasor.ar(speed = 1, lo = 0, hi = numFrames)
      val preSig      = BufRd.ar(1, buf = buf, index = (writeIdx + off) % numFrames, loop = 1)
      val write0      = in * recLevel + preSig * preLevel
      val writeBad    = CheckBadValues.ar(write0, id = 2001)
      val writeSig    = Gate.ar(write0, writeBad sig_== 0)

      // writeSig.poll(1, "write")
      BufWr.ar(Pad.Split(writeSig), buf = buf, index = writeIdx, loop = 1)
      LocalOut.kr(Impulse.kr(1.0 / (dur + fadeIn + fadeOut).max(0.01)))

      // NOTE: PlayBuf doesn't seem to work with LocalBuf !!!

      val speed       = pSpeed
      // val play0       = PlayBuf.ar(1 /* numChannels */, buf, speed, loop = 1)
      val readIdx     = Phasor.ar(speed = speed, lo = 0, hi = numFrames)
      val read0       = BufRd.ar(1, buf = buf, index = readIdx, loop = 1)
      val readBad     = CheckBadValues.ar(read0, id = 2000)
      val play0       = Gate.ar(read0, readBad sig_== 0)
      val play        = Flatten(play0)
      // play.poll(1, "outs")
      mix(in, play, pMix)
    }

    filter("*") { in =>
      shortcut = "ASTERISK"
      val pMix  = mkMix()
      val in2   = pAudio("in2", ParamSpec(0 /* -1 */, 1), default = 0.0)
      val pLag  = pAudio("lag", ParamSpec(0.001, 0.1, ExpWarp), default = 0.001)
      val flt   = in * Lag.ar(in2, pLag - 0.001)
      mix(in, flt, pMix)
    }

    filter("gain") { in =>
      shortcut = "G"
      val pGain = pAudio("gain", ParamSpec(-30, 30), default = 0)
      val pMix  = mkMix()

      val amp = pGain.dbamp
      val flt = in * amp
      mix(in, flt, pMix)
    }

    filter("gendy") { in =>
      val pAmt    = pAudio("amt", ParamSpec(0, 1), default = 1)
      val pMix    = mkMix()

      val amt     = Lag.ar(pAmt, 0.1)
      val minFreq = amt * 69 + 12
      val scale   = amt * 13 + 0.146
      val gendy   = Gendy1.ar(2, 3, 1, 1,
        minFreq   = minFreq,
        maxFreq   = minFreq * 8,
        ampScale  = scale,
        durScale  = scale,
        initCPs   = 7,
        kNum      = 7) * in
      val flt = Compander.ar(gendy, gendy, 0.7, 1, 0.1, 0.001, 0.02)
      mix(in, flt, pMix)
    }

    filter("~skew") { in =>
      val pLo     = pAudio("lo" , ParamSpec(0, 1), default = 0)
      val pHi     = pAudio("hi" , ParamSpec(0, 1), default = 1)
      val pPow    = pAudio("pow", ParamSpec(0.125, 8, ExpWarp), default = 1)
      val pRound  = pAudio("rnd", ParamSpec(0, 1), default = 0)

      val pMix    = mkMix()

      val sig = in.clip2(1).linlin(-1, 1, pLo, pHi).pow(pPow).roundTo(pRound) * 2 - 1
      mix(in, sig, pMix)
    }

    filter("~onsets") { in =>
      val pThresh     = pControl("thresh", ParamSpec(0, 1), default = 0.5)
      val pDecay      = pAudio  ("decay" , ParamSpec(0, 1), default = 0)

      val pMix        = mkMix()

      //      val numChannels = in.numChannels // numOutputs
      //      val bufIDs      = Seq.fill(numChannels)(bufEmpty(1024).id)
      val bufIDs      = LocalBuf(numFrames = 1024, numChannels = Pad(1, in))
      val chain1      = FFT(bufIDs, in)
      val onsets      = Onsets.kr(chain1, pThresh)
      val sig         = Decay.ar(Trig1.ar(onsets, SampleDur.ir), pDecay).min(1) // * 2 - 1
      mix(in, sig, pMix)
    }

    filter("m-above") { in =>
      val pThresh = pAudio("thresh", ParamSpec(1.0e-3, 1.0e-0, ExpWarp), 1.0e-2)
      val pMix    = mkMix()

      val thresh  = A2K.kr(pThresh)
      val env     = Env(0.0, Seq(Env.Segment(0.2, 0.0, Curve.step), Env.Segment(0.2, 1.0, Curve.lin)))
      val ramp    = EnvGen.kr(env)
      val volume  = thresh.linlin(1.0e-3, 1.0e-0, 4 /* 32 */, 2)
      val bufIDs  = LocalBuf(numFrames = 1024, numChannels = Pad(1, in))
      val chain1  = FFT(bufIDs, HPZ1.ar(in))
      val chain2  = PV_MagAbove(chain1, thresh)
      val flt     = LPZ1.ar(volume * IFFT.ar(chain2)) * ramp

      // account for initial dly
      val bufDur  = 1024 / SampleRate.ir
      val env2    = Env(0.0, Seq(Env.Segment(bufDur * 2, 0.0, Curve.step), Env.Segment(0.2, 1, Curve.lin)))
      val wet     = EnvGen.kr(env2)
      val sig     = (in * (1 - wet).sqrt) + (flt * wet)
      mix(in, sig, pMix)
    }

    filter("m-below") { in =>
      val pThresh     = pAudio("thresh", ParamSpec(1.0e-1, 10.0, ExpWarp), default = 1.0)
      val pMix        = mkMix()

      val thresh      = A2K.kr(pThresh)
      val env         = Env(0.0, Seq(Env.Segment(0.2, 0.0, Curve.step), Env.Segment(0.2, 1.0, Curve.lin)))
      val ramp        = EnvGen.kr(env)
      //            val volume		   = LinLin.kr( thresh, 1.0e-2, 1.0e-0, 4, 1 )
      val volume      = thresh.linlin(1.0e-1, 10, 2, 1)
      val bufIDs      = LocalBuf(numFrames = 1024, numChannels = Pad(1, in))
      val chain1      = FFT(bufIDs, in)
      val chain2      = PV_MagBelow(chain1, thresh)
      val flt         = volume * IFFT.ar(chain2) * ramp

      // account for initial dly
      val env2 = Env(0.0, Seq(Env.Segment(BufDur.kr(bufIDs) * 2, 0.0, Curve.step), Env.Segment(0.2, 1, Curve.lin)))
      val wet = EnvGen.kr(env2)
      val sig = (in * (1 - wet).sqrt) + (flt * wet)
      mix(in, sig, pMix)
    }

    filter("pitch") { in =>
      val pTrans  = pAudio("shift", ParamSpec(0.125, 4, ExpWarp), 1)
      val pTime   = pAudio("time", ParamSpec(0.01, 1, ExpWarp), 0.1)
      val pPitch  = pAudio("pitch", ParamSpec(0.01, 1, ExpWarp), 0.1)
      val pMix    = mkMix()

      val grainSize     = 0.5f
      val pitch         = A2K.kr(pTrans)
      val timeDisperse  = A2K.kr(pTime )
      val pitchDisperse = A2K.kr(pPitch)
      val flt           = PitchShift.ar(in, grainSize, pitch, pitchDisperse, timeDisperse * grainSize)
      mix(in, flt, pMix)
    }

    filter("pow") { in =>
      val pAmt = pAudio("amt", ParamSpec(0, 1), default = 0.5)
      val pMix = mkMix()

      val amt   = pAmt
      val amtM  = 1 - amt
      val exp   = amtM * 0.5 + 0.5
      val flt0  = in.abs.pow(exp) * in.signum
      val amp0  = Amplitude.ar(flt0)
      val amp   = amtM + (amp0 * amt)
      val flt   = flt0 * amp
      mix(in, flt, pMix)
    }

    filter("renoise") { in =>
      val pColor      = pAudio("color", ParamSpec(0, 1), default = 0)
      val pMix        = mkMix()
      val step        = 0.5
      val freqF       = math.pow(2, step)
      val frequencies = Vector.iterate(32.0, 40)(_ * freqF).filter(_ <= 16000)

      val color       = Lag.ar(pColor, 0.1)
      val noise       = WhiteNoise.ar(Pad(1, in))
      val sig         = frequencies.foldLeft[GE](0) { (sum, freq) =>
        val flt   = BPF.ar(in, freq, step)
        val freq2 = ZeroCrossing.ar(flt)
        val w0    = Amplitude.ar(flt)
        val w2    = w0 * color
        val w1    = w0 * (1 - color)
        sum + BPF.ar((noise * w1) + (LFPulse.ar(freq2) * w2), freq, step)
      }
      val amp         = step.reciprocal // compensate for Q
    val flt         = sig * amp
      mix(in, flt, pMix)
    }

    filter("verb") { in =>
      val pExtent = pControl("size" , ParamSpec(0, 1), default = 0.5)
      val pColor  = pControl("color", ParamSpec(0, 1), default = 0.5)
      val pMix    = mkMix()

      val extent      = pExtent
      val color       = Lag.kr(pColor, 0.1)
      val i_roomSize  = LinExp.kr(extent, 0, 1, 1, 100)
      val i_revTime   = LinExp.kr(extent, 0, 1, 0.3, 20)
      val spread      = 15
      //      val numChannels = in.numOutputs
      //      val ins         = in.outputs
      //      val verbs       = (ins :+ ins.last).grouped(2).toSeq.flatMap(pair =>
      //        (GVerb.ar(Mix(pair), i_roomSize, i_revTime, color, color, spread, 0, 1, 0.7, i_roomSize) * 0.3).outputs
      //      )
      //      val flt: GE = Vector(verbs.take(numChannels): _*) // drops last one if necessary
      val fltS        = GVerb.ar(in, i_roomSize, i_revTime, color, color, spread, 0, 1, 0.7, i_roomSize) * 0.3
      val flt         = fltS \ 0   // simply drop the right channels of each verb
      mix(in, flt, pMix)
    }

    filter("zero") { in =>
      val pWidth  = pAudio("width", ParamSpec(0, 1), default = 0.5)
      val pDiv    = pAudio("div"  , ParamSpec(1, 10, IntWarp), default = 1)
      val pLag    = pAudio("lag"  , ParamSpec(0.001, 0.1, ExpWarp), default = 0.01)
      val pMix    = mkMix()

      val freq    = ZeroCrossing.ar(in).max(20)
      val width0  = Lag.ar(pWidth, 0.1)
      val amp     = width0.sqrt
      val width   = width0.reciprocal
      val div     = Lag.ar(pDiv, 0.1)
      val lagTime = pLag
      val pulse   = Lag.ar(LFPulse.ar(freq / div, 0, width) * amp, lagTime)
      val flt     = in * pulse
      mix(in, flt, pMix)
    }

    // -------------- TALLIN --------------

    filter("pulse-div") { in0 =>
      val pThresh = pAudio("thresh", ParamSpec(0.01, 1, ExpWarp), default = 0.1)
      val in      = (in0 - pThresh).max(0.0)
      val pDiv    = pAudio("div", ParamSpec(1, 16, IntWarp), default = 1)
      val pMul    = pAudio("mul", ParamSpec(1, 16, IntWarp), default = 1)
      val pulse   = PulseDivider.ar(in, pDiv)
      val timer   = Timer.ar(pulse).max(0.0001)
      val freq    = timer.reciprocal * pMul
      val phSpeed = freq / SampleRate.ir
      val phasor  = Phasor.ar(trig = pulse, speed = phSpeed)
      val tr      = HPZ1.ar(phasor) < 0
      val pTime   = pAudio("time", ParamSpec(0.0 , 1.0), default = 0)
      val sig     = DelayN.ar(tr /* pulse */, 1.0, pTime)

      sig
    }

    filter(">mono") { in =>
      shortcut = "GREATER"
      Mix.mono(in) / NumChannels(in)
    }

    // -------------- SINKS --------------

    val sinkRec = sink("rec") { in =>
      proc.graph.DiskOut.ar(KeyRecArtifact, in)
    }
    val sinkPrepObj = Action.predef[S](ActionKeyRecPrepare)
    val sinkDispObj = Action.predef[S](ActionKeyRecDispose)
    // val locRec      = getRecLocation(n.folder)
    // val artRec      = locRec.add(locRec.directory / "undefined")
    // val artRecObj   = Obj(ArtifactElem(artRec))
    // sinkPrepObj.attr.put(KeyRecArtifact, artRecObj  )
    // sinkDispObj.attr.put(KeyRecArtifact, artRecObj  )
    // sinkRec    .attr.put(KeyRecArtifact, artRecObj  )
    sinkRec    .attr.put("nuages-prepare", sinkPrepObj)
    sinkRec    .attr.put("nuages-dispose", sinkDispObj)

    val sumRec = generator("rec-sum") {
      val numCh = masterChansOption.map(_.size).getOrElse(1)
      val in    = InFeedback.ar(0, numCh)   // XXX TODO --- should find correct indices!
      proc.graph.DiskOut.ar(KeyRecArtifact, in)
      DC.ar(0)
    }
    sumRec.attr.put("nuages-prepare", sinkPrepObj)
    sumRec.attr.put("nuages-dispose", sinkDispObj)

    // -------------- DIFFUSIONS --------------

    masterChansOption.foreach { masterChans =>
      val numChans          = masterChans.size
      val masterCfg         = NamedBusConfig("", 0, numChans)
      val masterGroupsCfg   = masterCfg +: sConfig.masterGroups

      masterGroupsCfg.zipWithIndex.foreach { case (cfg, idx) =>
        def placeChannels(sig: GE): GE = {
          if (cfg.numChannels == numChans) sig
          else {
            Seq(Silent.ar(cfg.offset),
              Flatten(sig),
              Silent.ar(numChans - (cfg.offset + cfg.numChannels))): GE
          }
        }

        def mkAmp(): GE = {
          val db0 = pAudio("amp", ParamSpec(-inf, 20, DbFaderWarp), -inf)
          val db  = db0 - 10 * (db0 < -764)  // FUCKING BUG IN SUPERCOLLIDER. HELL WHY ARE PEOPLE WRITING C CODE. SHIT LANGUAGE
          val res = db.dbamp
          CheckBadValues.ar(res, id = 666)
          res
        }

        def mkOutAll(in: GE): GE = {
          val pAmp          = mkAmp()
          val sig           = in * Lag.ar(pAmp, 0.1) // .outputs
          val outChannels   = cfg.numChannels
          val outSig        = WrapExtendChannels(outChannels, sig)
          placeChannels(outSig)
        }

        def mkOutPan(in: GE): GE = {
          val pSpread       = pControl("spr" , ParamSpec(0.0, 1.0),   default = 0.25) // XXX rand
          val pRota         = pControl("rota", ParamSpec(0.0, 1.0),   default = 0.0)
          val pBase         = pControl("azi" , ParamSpec(0.0, 360.0), default = 0.0)
          val pAmp          = mkAmp()

          val baseAzi       = Lag.kr(pBase, 0.5) + IRand(0, 360)
          val rotaAmt       = Lag.kr(pRota, 0.1)
          val spread        = Lag.kr(pSpread, 0.5)
          val outChannels   = cfg.numChannels
          val rotaSpeed     = 0.1
          val inSig         = in * Lag.ar(pAmp, 0.1) // .outputs
          val noise         = LFDNoise1.kr(rotaSpeed) * rotaAmt * 2
          val pos0          = ChannelIndices(in) * 2 / NumChannels(in)
          // pos0.poll(0, "pos0")
          val pos1          = (baseAzi / 180) + pos0
          val pos           = pos1 + noise
          val level         = 1
          val width         = (spread * (outChannels - 2)) + 2
          val panAz         = PanAz.ar(outChannels, inSig, pos, level, width, 0)
          // tricky
          val outSig        = Mix(panAz)
          placeChannels(outSig)
        }

        def mkOutRnd(in: GE): GE = {
          val pAmp        = mkAmp()
          val pFreq       = pControl("freq", ParamSpec(0.01, 10, ExpWarp), default = 0.1)
          val pPow        = pControl("pow" , ParamSpec(1, 10), default = 2)
          val pLag        = pControl("lag" , ParamSpec(0.1, 10), default = 1)

          val sig         = in * Lag.ar(pAmp, 0.1) // .outputs
          val outChannels = cfg.numChannels
          val sig1        = WrapExtendChannels(outChannels, sig)
          val freq        = pFreq
          val lag         = pLag
          val pw          = pPow
          val rands       = Lag.ar(TRand.ar(0, 1, Dust.ar(List.fill(outChannels)(freq))).pow(pw), lag)
          val outSig      = sig1 * rands
          placeChannels(outSig)
        }

        if (nConfig.collector) {
          filter(s"O-all${cfg.name}")(mkOutAll)
          filter(s"O-pan${cfg.name}")(mkOutPan)
          filter(s"O-rnd${cfg.name}")(mkOutRnd)
        } else {
          def mkDirectOut(sig0: GE): Unit = {
            val bad = CheckBadValues.ar(sig0)
            val sig = Gate.ar(sig0, bad sig_== 0)
            masterChans.zipWithIndex.foreach { case (ch, i) =>
              val sig0 = sig \ i
              val hpf  = sConfig.highPass
              val sig1 = if (hpf >= 16 && hpf < 20000) HPF.ar(sig0, hpf) else sig0
              Out.ar(ch, sig1)   // XXX TODO - should go to a bus w/ limiter
            }
          }

          collector(s"O-all${cfg.name}") { in =>
            val sig = mkOutAll(in)
            mkDirectOut(sig)
          }

          collector(s"O-pan${cfg.name}") { in =>
            val sig = mkOutPan(in)
            mkDirectOut(sig)
          }

          collector(s"O-rnd${cfg.name}") { in =>
            val sig = mkOutRnd(in)
            mkDirectOut(sig)
          }
        }
      }
    }

    // --------------- ANEMONE ----------------

    generator("a~pulse") {
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
      val pFreq   = pAudio("freq"     , ParamSpec(0.1 , 10000, ExpWarp), default = 15 /* 1 */)
      // val pAmp    = pAudio("amp"      , ParamSpec(0.01,     1, ExpWarp), default =  0.1)
      val pLo     = pAudio("lo"     , ParamSpec(0.0 , 1), default = 0)
      val pHi     = pAudio("hi"     , ParamSpec(0.0 , 1), default = 1)

      val freq  = pFreq // LinXFade2.ar(pFreq, inFreq, pFreqMix * 2 - 1)
      val sig   = SinOsc.ar(freq)

      sig.linlin(-1, 1, pLo, pHi)
    }

    generator("a~dust") {
      val pFreq   = pAudio("freq" , ParamSpec(0.01, 1000, ExpWarp), default = 0.1 /* 1 */)
      val pDecay  = pAudio("decay", ParamSpec(0.001 , 10, ExpWarp), default = 0.1 /* 1 */)
      val pLo     = pAudio("lo"     , ParamSpec(0.0 , 1), default = 0)
      val pHi     = pAudio("hi"     , ParamSpec(0.0 , 1), default = 1)

      val freq  = pFreq
      val sig   = Decay.ar(Dust.ar(freq), pDecay).clip(0.01, 1).linlin(0.01, 1, pLo, pHi)
      sig
    }

    generator("a~gray") {
      val pLo     = pAudio("lo"     , ParamSpec(0.0 , 1), default = 0)
      val pHi     = pAudio("hi"     , ParamSpec(0.0 , 1), default = 1)
      GrayNoise.ar.linlin(-1, 1, pLo, pHi)
    }

    generator("a~rand") {
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
      val pTime   = pAudio("time", ParamSpec(0.0 , 1.0), default = 0)
      val sig     = DelayN.ar(in, pTime, 1.0)
      sig
    }
    /*
        filter("a~reso") { in =>

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
    sConfig.lineInputs.find(c => c.name == "i-mkv" || c.name == "beat").foreach { cfg =>
      generator("a~beat") {
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

    sConfig.micInputs.find(c => c.name == "m-hole").foreach { cfg =>
      generator("m-feat") {
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
      val vals    = Vector.tabulate(8)(i => pAudio(s"v${i+1}", ParamSpec(0, 1), default = 0))
      val trig    = pAudio("trig", ParamSpec(0.0, 1.0), default = 0)
      val hi      = pAudio("hi", ParamSpec(1, 8, IntWarp), default = 1)
      val index   = Stepper.ar(trig, lo = 0, hi = hi - 1)
      val sig     = Select.ar(index, vals)
      sig
    }

    filter("a~dup") { in =>
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
      val pThresh = pAudio("thresh", ParamSpec(0.01, 1, ExpWarp), default = 0.1)
      val pGate   = pAudio("gate", ParamSpec(0.0, 1.0), default = 0) > pThresh
      val pLeak   = pAudio("leak", ParamSpec(0, 1, IntWarp), default = 0) > pThresh
      val pMix    = mkMix()
      val sig0    = Gate.ar(in, pGate)
      val leak    = LeakDC.ar(sig0)
      val sig     = Select.ar(pLeak, Seq(sig0, leak))
      mix(in, sig, pMix)
    }

    sConfig.micInputs.find(_.name == "m-dpa").foreach { cfg =>
      generator("a~dpa") {
        val off = cfg.offset
        val in  = PhysicalIn.ar(off)
        val gain = pAudio("gain", ParamSpec(-20, 20), default = 0).dbamp
        val sig  = in * gain
        sig
      }
    }

    generator("a~ff") {
      val pLo     = pAudio("lo"    , ParamSpec(0.0, 1.0), default = 0.0)
      val pHi     = pAudio("hi"    , ParamSpec(0.0, 1.0), default = 1.0)
      // val inTrig  = pAudioIn("trig", 1, ParamSpec(0.0, 1.0))
      val inTrig  = pAudio("trig", ParamSpec(0.0, 1.0), default = 0)

      val sig     = ToggleFF.ar(inTrig).linlin(0, 1, pLo, pHi)
      sig
    }

    generator("a~trig") {
      val pThresh = pAudio("thresh", ParamSpec(0.01, 1, ExpWarp), default = 0.1)
      val pLo     = pAudio("lo"    , ParamSpec(0.0, 1.0), default = 0.0)
      val pHi     = pAudio("hi"    , ParamSpec(0.0, 1.0), default = 1.0)
      val inTrig  = pAudio("trig"  , ParamSpec(0.0, 1.0), default = 0)
      val pDur    = pAudio("dur"   , ParamSpec(0.001, 1.0, ExpWarp), default = 0.01)

      val sig     = Trig1.ar(inTrig - pThresh, pDur).linlin(0, 1, pLo, pHi)
      sig
    }

    generator("a~step") {
      val pLo     = pAudio("lo"    , ParamSpec(0.0, 1.0), default = 0.0)
      val pHi     = pAudio("hi"    , ParamSpec(0.0, 1.0), default = 1.0)
      val pDiv    = pAudio("div"   , ParamSpec(1, 16, IntWarp), default = 1)
      // val inTrig  = pAudioIn("trig", 1, ParamSpec(0.0, 1.0))
      val inTrig  = pAudio("trig", ParamSpec(0.0, 1.0), default = 0)
      val reset   = pAudio("reset", ParamSpec(0.0, 1.0), default = 0)
      val sig     = Stepper.ar(inTrig, reset = reset, lo = 0, hi = pDiv).linlin(0, pDiv, pLo, pHi)
      sig
    }

    filter("mul") { in => // ; import ugen._
      val inB   = pAudio("mod", ParamSpec(0.0, 1.0), default = 0)
      val flt   = in * inB
      val pMix  = mkMix()
      mix(in, flt, pMix)
    }

    filter("L-lpf") { in =>
      val fade  = mkMix4()
      val freq  = fade.linexp(1, 0, 22.05 * 2, 20000) // 22050
    val wet   = LPF.ar(in, freq)
      mkBlend(in, wet, fade)
    }

    filter("L-hpf") { in =>
      val fade  = mkMix4()
      val freq  = fade.linexp(1, 0, 20000, 22.05 * 2)
      val wet   = HPF.ar(HPF.ar(in, freq), freq)
      mkBlend(in, wet, fade)
    }

    val FFTSize = 512

    filter("L-below") { in =>
      val fade    = mkMix4()
      val thresh  = fade.linexp(1, 0, 1.0e-3, 1.0e1)
      val buf     = LocalBuf(FFTSize)
      val wet     = IFFT.ar(PV_MagBelow(FFT(buf, in), thresh))
      mkBlend(in, wet, fade, FFTSize / SampleRate.ir)
    }

    filter("L-above") { in =>
      val fade    = mkMix4()
      val thresh  = fade.linexp(0, 1, 1.0e-3, 2.0e1)
      val buf     = LocalBuf(FFTSize)
      val wet     = IFFT.ar(PV_MagAbove(FFT(buf, in), thresh))
      mkBlend(in, wet, fade, FFTSize / SampleRate.ir)
    }

    filter("L-up") { in =>
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
      // val freq2    = fd.linexp(0, numSteps, zero, ny) - zero

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