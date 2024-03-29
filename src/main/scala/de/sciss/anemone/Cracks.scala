/*
 *  Cracks.scala
 *  (Anemone-Actiniaria)
 *
 *  Copyright (c) 2014-2022 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.anemone

import de.sciss.audiofile.AudioFile
import de.sciss.file._
import de.sciss.lucre.synth.{RT, Server, Synth, Txn}
import de.sciss.nuages.{Nuages, NuagesView, ScissProcs}
import de.sciss.proc.{AudioCue, ParamSpec, Warp}
import de.sciss.synth.UGenSource.Vec
import de.sciss.synth.proc.graph.impl.SendReplyResponder
import de.sciss.synth.ugen.ControlValues
import de.sciss.synth.{SynthGraph, addAfter}
import de.sciss.{nuages, osc}

import java.net.{InetAddress, InetSocketAddress, SocketAddress}
import scala.swing.event.SelectionChanged
import scala.swing.{BoxPanel, ComboBox, Label, Orientation}
import scala.util.control.NonFatal

object Cracks {
  final val DEBUG = false

  val raspiIPs: Vec[String] = Vector(
    "192.168.0.11",
    "192.168.0.12",
    "192.168.0.13",
    "192.168.0.24"
  )

  val myIP: String = "192.168.0.77"

  val raspiPort: Int = 57120

  private[this] val raspiSockets: Array[SocketAddress] = raspiIPs.iterator.map { ip =>
    new InetSocketAddress(ip, raspiPort)
  } .toArray

  private[this] val raspiT: osc.UDP.Transmitter.Undirected = {
    val config = osc.UDP.Config()
    config.localAddress = InetAddress.getByName(myIP)
    val t = osc.UDP.Transmitter(config)
    t.connect()
    t
  }

  private final class CrackResponder(protected val synth: Synth)
    extends SendReplyResponder {

    private[this] val NodeID = synth.peer.id

    protected def added()(implicit tx: RT): Unit = ()

    private[this] val data = new Array[Int](8)

    private[this] val haveWarned = new Array[Boolean](4)

    protected def body: PartialFunction[osc.Message, Unit] = {
      case osc.Message("/cracks", NodeID, 0,
          x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float) =>

        val d = data
        val w = raspiWidth
        val h = raspiHeight
        d(0) = (x1 * w).toInt
        d(1) = (y1 * h).toInt

        d(2) = (x2 * w).toInt
        d(3) = (y2 * h).toInt

        d(4) = (x3 * w).toInt
        d(5) = (y3 * h).toInt

        d(6) = (x4 * w).toInt
        d(7) = (y4 * h).toInt

        if (DEBUG) println(s"CRACK COORD 1: x1 ${d(0)}, y1 ${d(1)}")

        val sock  = raspiSockets
        val t     = raspiT
        var i = 0
        var j = 0
        while (i < 4) {
          val target  = sock(i)
          val xi      = d(j)
          j += 1
          val yi      = d(j)
          j += 1
          val p = osc.Message("/xy", xi, yi)
          try {
            t.send(p, target)
          } catch {
            case NonFatal(ex) =>
              if (!haveWarned(i)) {
                haveWarned(i) = true
                println(s"COULD NOT SEND TO RASPI ${i+1}")
                ex.printStackTrace()
              }
          }
          i += 1
        }
    }
  }

  def setStage(chan: Int, stage: Int): Unit = {
    println(s"setStage($chan, $stage)")
    try {
      val sock = raspiSockets(chan)
      require(stage >= 0 && stage <= 4)
      val p = osc.Message("/stage", stage)
      raspiT.send(p, sock)
    } catch {
      case NonFatal(ex) =>
        println(s"Could not set stage of channel $chan to $stage")
        ex.printStackTrace()
    }
  }

  def mkComponent[T <: Txn[T]](view: NuagesView[T]): Unit = {
    val gadgets = (0 until 4).flatMap { ch =>
      val lb = new Label(s"${ch+1}:")
      val ggCombo = new ComboBox(Seq("0 - init", "1 - in", "2 - trace", "3 - remove", "4 - out")) {
        listenTo(selection)
        reactions += {
          case SelectionChanged(_) =>
            val stage = selection.index
            setStage(chan = ch, stage = stage)
        }
      }
      val d = ggCombo.preferredSize
      d.width = math.max(48, d.width)
      ggCombo.minimumSize   = d
      ggCombo.maximumSize   = d
      ggCombo.preferredSize = d

      setStage(ch, 0)

      Seq(lb, ggCombo)
    }

    val pane = new BoxPanel(Orientation.Horizontal) {
      contents ++= gadgets
    }

    view.addSouthComponent(pane)
  }

  def initOSC(busXYOff: Int, s: Server)(implicit tx: RT): Unit = {
    val g = SynthGraph {
      import de.sciss.synth._
      import ugen._
      val in      = In.ar(busXYOff, 8)
      val values  = A2K.kr(in)
      val tr      = Impulse.kr(50)
      SendReply.kr(tr, values, msgName = "/cracks")
    }
    val syn   = Synth.play(g, nameHint = Some("route-crack"))(target = s.defaultGroup, addAction = addAfter)
    val resp  = new CrackResponder(syn)
    resp.add()

    tx.afterCommit {
      println("Launched crack router")
    }
  }

  final val width : Int = 2820 // 5184
  final val height: Int = 2820 // 2981

  final val raspiWidth : Int = 1024
  final val raspiHeight: Int = 1024

  def apply[T <: Txn[T]](dsl: nuages.DSL[T], sCfg: ScissProcs.Config, nCfg: Nuages.Config)
                        (implicit tx: T, n: Nuages[T]): Unit = {
    import de.sciss.synth.Import._
    import dsl._

    def default(in: Double): ControlValues =
      if (sCfg.genNumChannels <= 0)
        in
      else
        Vector.fill(sCfg.genNumChannels)(in)

    for (crackIdx <- 0 until 4) {
      val busXY = (nCfg.mainChannels.get.max + 1) + (crackIdx * 2)

      val pCracks1 = generator(s"cra-${crackIdx + 1}") {
        import de.sciss.synth.proc.graph._
        import de.sciss.synth.ugen._
        import de.sciss.synth.{Buffer => _}

        val b      = Buffer("buf")

        // val cx: GE = "cx".kr(width  * 0.5) + SinOsc.ar("cxmf".kr(0)) * "cxmd".kr(0)
        // val cy: GE = "cy".kr(height * 0.5) + SinOsc.ar("cymf".kr(0)) * "cymd".kr(0)
        // val rx: GE = "rx".kr((width  - 1) * 0.5) + SinOsc.ar("rxmf".kr(0)) * "rxmd".kr(0)
        // val ry: GE = "ry".kr((height - 1) * 0.5) + SinOsc.ar("rymf".kr(0)) * "rymd".kr(0)
        // val freq: GE = "freq".kr(1.0) + SinOsc.ar("fmf".kr(0)) * "fmd".kr(0)

        //      val cx: GE = "cx".ar(0.5) * width
        //      val cy: GE = "cy".ar(0.5) * height
        //      val rx: GE = "rx".ar(0.5) * width
        //      val ry: GE = "ry".ar(0.5) * height

        val cx    = pAudio("cx", ParamSpec(0.0, 1.0), default(0.5))
        val cy    = pAudio("cy", ParamSpec(0.0, 1.0), default(0.5))
        val rx    = pAudio("rx", ParamSpec(0.0, 1.0), default(0.5))
        val ry    = pAudio("ry", ParamSpec(0.0, 1.0), default(0.5))
//        val rx    = rx0 * width
//        val ry    = ry0 * height

        val freq0 = pAudio("freq", ParamSpec(0.001, 10000.0, Warp.Exp), default(0.5))
        val freq  = freq0 // .linexp(0, 1, 0.001, 10000)

        val twopi = math.Pi * 2
        val phase = Phasor.ar(speed = twopi * 2 * freq / SampleRate.ir, lo = 0, hi = twopi)

        val x   = ((cx + rx * phase.cos) * width ).fold(0, width )
        val y   = ((cy + ry * phase.sin) * height).fold(0, height)
        val xm  = Mix.mono(x) / NumChannels(x)
        val ym  = Mix.mono(y) / NumChannels(y)

        ReplaceOut.ar(busXY, Seq(xm / width, ym / height))

        val pos = y.roundTo(1) * width + x

        val read = BufRd.ar(numChannels = 1, buf = b, index = pos, loop = 1, interp = 0 /* 2 */)

        val sig0  = LeakDC.ar(read)
        val sig   = sig0 * 0.25 // Pan2.ar(sig0, "pan".kr(0))

        // Out.ar(0, sig)
        sig // ScanOut(sig)
      }

      val baseDir = file("/") / "data" / "projects" / "Imperfect" / "cracks" / "two_bw"
      val cueName1 = s"cracks-${crackIdx + 1}.aif"
      val art1    = baseDir / cueName1
      val spec1   = AudioFile.readSpec(art1)
      val cue1    = AudioCue.Obj.newConst[T](AudioCue(art1.toURI, spec1, offset = 0L, gain = 1.0))

      pCracks1.attr.put("buf", cue1)
    }

    nCfg.mainChannels.foreach { chans =>
      chans.zipWithIndex.foreach { case (idx, ch) =>
        collector(s"O-cr${idx+1}") { in =>
          import de.sciss.synth._
          import ugen._

          def mkAmp(): GE = {
            val db0 = pAudio("amp", ParamSpec(-inf, 20, Warp.DbFader), default(-inf))
            val db  = db0 - 10 * (db0 < -764)  // BUG IN SUPERCOLLIDER
            val res = db.dbAmp
            CheckBadValues.ar(res, id = 666)
            res
          }

          val pAmp   = mkAmp()
          val sigA   = in * Lag.ar(pAmp, 0.1) // .outputs
          val outSig = Mix(sigA) / NumChannels(sigA)
          val bad    = CheckBadValues.ar(outSig)
          val sig    = Gate.ar(outSig, bad sig_== 0)
          Out.ar(ch, sig)
        }
      }
    }
  }
}
