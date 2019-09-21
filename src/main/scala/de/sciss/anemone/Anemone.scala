/*
 *  Anemone.scala
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

import java.awt.Color
import java.text.SimpleDateFormat
import java.util.Locale

import de.sciss.audiowidgets.RotaryKnob
import de.sciss.equal.Implicits._
import de.sciss.file._
import de.sciss.fscape.lucre.FScape
import de.sciss.numbers
import de.sciss.lucre.stm
import de.sciss.lucre.stm.Folder
import de.sciss.lucre.stm.store.BerkeleyDB
import de.sciss.lucre.synth._
import de.sciss.nuages.Nuages.Surface
import de.sciss.nuages.{NamedBusConfig, Nuages, ScissProcs, Wolkenpumpe, WolkenpumpeMain}
import de.sciss.submin.Submin
import de.sciss.synth.proc.{Durable, Timeline, Universe}
import de.sciss.synth.{SynthGraph, addAfter}
import de.sciss.{nuages, osc}
import javax.swing.DefaultBoundedRangeModel
import jpen.event.{PenAdapter, PenManagerListener}
import jpen.owner.multiAwt.AwtPenToolkit
import jpen.{PLevel, PLevelEvent, PenDevice, PenProvider}

import scala.collection.immutable.{IndexedSeq => Vec}
import scala.swing.event.ValueChanged
import scala.swing.{Dimension, Swing}
import scala.util.Try
import scala.util.control.NonFatal

object Anemone {
  def mkDatabase(parent: File): File = {
    val recFormat = new SimpleDateFormat("'session_'yyMMdd'_'HHmmss", Locale.US)
    parent / recFormat.format(new java.util.Date)
  }

  def version     : String = buildInfString("version")
  def license     : String = buildInfString("license")
  def homepage    : String = buildInfString("homepage")
  def builtAt     : String = buildInfString("builtAtString")
  def fullVersion : String = s"v$version, built $builtAt"

  private def buildInfString(key: String): String = try {
    val clazz = Class.forName("de.sciss.anemone.BuildInfo")
    val m     = clazz.getMethod(key)
    m.invoke(null).toString
  } catch {
    case NonFatal(_) => "?"
  }

  final val USE_OSC_LIGHTS = false // true -- for ZKM concert 2018

  final case class Config(
                           masterChannels    : Range,
                           masterGroups      : Vec[NamedBusConfig] = Vector.empty,
                           soloChannels      : Range,
                           micInputs         : Vec[NamedBusConfig],
                           lineInputs        : Vec[NamedBusConfig],
                           lineOutputs       : Vec[NamedBusConfig],
                           genNumChannels    : Int                 = 0,
                           device            : Option[String]      = None,
                           database          : Option[File]        = None,
                           timeline          : Boolean             = true,
                           tablet            : Boolean             = true
  )

  lazy val Scarlett = Config(
    masterChannels    =  0 to 15,
    soloChannels      = 16 to 17,
    genNumChannels = 4,
    micInputs         = Vector(
      NamedBusConfig("m-dpa", 0 to 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 5 to 5),
      NamedBusConfig("beat" , 4 to 4)
    ),
    lineOutputs     = Vector(
      // NamedBusConfig("sum", 6, 2)
    ),
    device    = Some("Wolkenpumpe-16"),
    database  = None // Some(mkDatabase(userHome/"Documents"/"applications"/"150131_ZKM"/"sessions"))
  )

  lazy val ZKMAtelier: Config = Scarlett.copy(
    masterChannels  = 0 to 7,
    micInputs       = Vector.empty,
    device          = Some("Wolkenpumpe"),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 0 to 3)
    ),
    lineOutputs = Vector(
      NamedBusConfig("sum", 8 to 9)
    )
  )

  // jack_netsource -H 169.254.1.2 -o 2 -i 18 -N david
  lazy val GrazAtelier: Config = Scarlett.copy(
    masterChannels  = 0 to 3,
    soloChannels    = 4 to 5,
    genNumChannels = 4,
    micInputs         = Vector(
      NamedBusConfig("m-dpa", 0 to 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 4 to 5),
      NamedBusConfig("beat" , 6 to 6)
    ),
    lineOutputs     = Vector(
      //      NamedBusConfig("sum", 6, 2)
    ),
    device    = Some("Wolkenpumpe"),
    database  = Some(mkDatabase(userHome/"Music"/"renibday"/"sessions"))
  )

  lazy val Forum = Config(
    masterChannels    = 0 to 3,
    soloChannels      = 4 to 5,
    genNumChannels = 4,
    micInputs         = Vector(
      NamedBusConfig("m-dpa", 0 to 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 5 to 5),
      NamedBusConfig("beat" , 4 to 4)
    ),
    lineOutputs     = Vector(
      NamedBusConfig("sum", 6 to 7)
    ),
    device    = Some("Wolkenpumpe"),
    database  = None // Some(mkDatabase(userHome/"Documents"/"applications"/"150131_ZKM"/"sessions"))
  )

  lazy val Minoriten = Config(
    masterChannels    = 0 to 3,
    soloChannels      = 4 to 5,
    genNumChannels = 4,
    micInputs         = Vector(
      NamedBusConfig("m-dpa", 0 to 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 4 to 5),
      NamedBusConfig("beat" , 6 to 6)
    ),
    lineOutputs     = Vector(
      //      NamedBusConfig("sum", 6, 2)
    ),
    device    = Some("Wolkenpumpe"),
    database  = None, // Some(mkDatabase(userHome/"Documents"/"projects"/"Anemone"/"sessions"))
    timeline  = false
  )

  lazy val Imperfect = Config(
    masterChannels    = 0 until 24,
    soloChannels      = 24 to 25,
    genNumChannels = 2, // 4,
    micInputs         = Vector(
      NamedBusConfig("m-in" , 0 to 1),
      NamedBusConfig("m-out", 4 to 5)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 6 to 7)
//      NamedBusConfig("beat" , 6, 1)
    ),
    lineOutputs     = Vector(
//      NamedBusConfig("sum", 24, 2)
    ),
    device    = Some("Finissage"),
    database  = Some(mkDatabase(userHome/"Documents"/"projects"/"Anemone"/"sessions")),
    timeline  = true // false
  )

  val NoSolo: Range = 0 until 0

  lazy val BEAST = Config(
    masterChannels    = 0 until 12,
    masterGroups      = Vector(
      NamedBusConfig("B", 0 to 3),
      NamedBusConfig("M", 4 to 7),
      NamedBusConfig("T", 8 to 11)
    ),
    soloChannels      = NoSolo, // 12 to 13,
    genNumChannels = 4,
    micInputs         = Vector(
      NamedBusConfig("m-dpa", 0 to 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 2 to 3),
      NamedBusConfig("beat" , 4 to 4)
    ),
    lineOutputs     = Vector(
      //      NamedBusConfig("sum", 24, 2)
    ),
    device    = Some("Wolkenpumpe"),
//    database  = None, // Some(mkDatabase(file("/") / "data" / "projects"/"Anemone"/"sessions")),
    database  = Some(mkDatabase(userHome / "Documents" /"Anemone"/"sessions")),
    timeline  = true // false
  )

  lazy val Cracks = Config(
    masterChannels    = 0 to 3,
    soloChannels      = (4 + 8) to (5 + 8),
    genNumChannels = 4,
    micInputs         = Vector(
    ),
    lineInputs      = Vector(
    ),
    lineOutputs     = Vector(
    ),
    device    = Some("Wolkenpumpe"),
    database  = Some(mkDatabase(userHome/"Documents"/"projects"/"Anemone"/"sessions")),
    timeline  = true // false
  )

  lazy val SeaM = Config(
    masterChannels    = 0 to 7,
    soloChannels      = 8 until 8,
    genNumChannels = 4,
    micInputs         = Vector(
      NamedBusConfig("m-dpa", 0 to 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("ludger", 2 to 3),
    ),
    lineOutputs     = Vector(
      NamedBusConfig("sum", 8 to 9)
    ),
    device    = Some("SeaM"),
    database  = None, // Some(mkDatabase(userHome/"Documents"/"projects"/"Anemone"/"sessions")),
    timeline  = true // false
  )

  lazy val CUBE = Config(
    masterChannels    = 0 to 11,
    soloChannels      = 12 until 12,
    genNumChannels = 4,
    micInputs         = Vector(
//      NamedBusConfig("m-dpa", 0 to 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 0 to 1),
//      NamedBusConfig("beat" , 4 to 4)
    ),
    lineOutputs     = Vector(
//      NamedBusConfig("sum", 8 to 9)
    ),
    device    = Some("Wolkenpumpe"),
    database  = None, // Some(mkDatabase(userHome/"Documents"/"projects"/"Anemone"/"sessions")),
    timeline  = true  // false
  )

  lazy val Impuls = Config(
    masterChannels    = 0 to 3,
    soloChannels      = 4 to 5,
    genNumChannels = 4,
    micInputs         = Vector(
//      NamedBusConfig("m-dpa", 0 to 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 4 to 5)
//      NamedBusConfig("pirro", 0 to 1)
//      NamedBusConfig("beat" , 6 to 6)
    ),
    lineOutputs     = Vector(
      //      NamedBusConfig("sum", 24, 2)
    ),
    device    = Some("Wolkenpumpe"),
    database  = None, // Some(mkDatabase(userHome/"Documents"/"projects"/"Anemone"/"sessions")),
    timeline  = true // false
  )

  lazy val LAquila = Config(
    masterChannels    = 0 to 7,
    soloChannels      = 8 to 9,
    genNumChannels = 4,
    micInputs         = Vector(
      //      NamedBusConfig("m-dpa", 0 to 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 4 to 5)
      //      NamedBusConfig("pirro", 0 to 1)
      //      NamedBusConfig("beat" , 6 to 6)
    ),
    lineOutputs     = Vector(
      //      NamedBusConfig("sum", 24, 2)
    ),
    device    = Some("Wolkenpumpe"),
    database  = None, // Some(mkDatabase(userHome/"Documents"/"projects"/"Anemone"/"sessions")),
    timeline  = true // false
  )

  lazy val Schwaermen = Config(
    masterChannels    = 0 to 3,
    soloChannels      = 4 to 5,
    genNumChannels = 4,
    micInputs         = Vector(
      //      NamedBusConfig("m-dpa", 0 to 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 4 to 5)
      //      NamedBusConfig("pirro", 0 to 1)
      //      NamedBusConfig("beat" , 6 to 6)
    ),
    lineOutputs     = Vector(
      //      NamedBusConfig("sum", 24, 2)
    ),
    device    = Some("Wolkenpumpe"),
    database  = None, // Some(mkDatabase(userHome/"Documents"/"projects"/"Anemone"/"sessions")),
    timeline  = true // false
  )

  lazy val ZKM_Kubus = Config(
    masterChannels    = 0 until 21,
    masterGroups      = Vector(
      NamedBusConfig("B",  0 to  6),  // bottom
      NamedBusConfig("L",  7 to 13),  // low
      NamedBusConfig("H", 14 to 17),  // high
      NamedBusConfig("T", 18 to 20)   // top
    ),
    soloChannels      = NoSolo,
    genNumChannels = 4,
    micInputs         = Vector(
//      NamedBusConfig("m-dpa", 0 to 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 0 to 3),
//      NamedBusConfig("beat" , 4 to 4)
    ),
    lineOutputs     = Vector(
      //      NamedBusConfig("sum", 24, 2)
    ),
    device    = Some("Wolkenpumpe"),
    database  = None, // Some(mkDatabase(userHome/"Documents"/"projects"/"Anemone"/"sessions")),
    timeline  = true // false
  )

  lazy val MuWa = Config(
    masterChannels    = 0 to 7,
    soloChannels      = 0 until 0,
    genNumChannels = 4,
    micInputs         = Vector(
      //      NamedBusConfig("m-dpa", 0 to 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 0 to 1)
    ),
    lineOutputs     = Vector(
      //      NamedBusConfig("sum", 24, 2)
    ),
    device    = Some("Wolkenpumpe"),
    database  = None, // Some(mkDatabase(userHome/"Documents"/"projects"/"Anemone"/"sessions")),
    timeline  = true // false
  )

  lazy val AlmatMay2019 = Config(
    masterChannels    = 0 to 7,
    soloChannels      = 0 until 0,
    genNumChannels = 4,
    micInputs         = Vector(
      //      NamedBusConfig("m-dpa", 0 to 1)
    ),
    lineInputs      = Vector(
//      NamedBusConfig("others", 0 to 6)
      NamedBusConfig("others", 10 to 16)
    ),
    lineOutputs     = Vector(
      //      NamedBusConfig("sum", 24, 2)
    ),
    device    = Some("Wolkenpumpe"),
    database  = None, // Some(mkDatabase(userHome/"Documents"/"projects"/"Anemone"/"sessions")),
    timeline  = true // false
  )

  lazy val SegMod = Config(
    masterChannels    = 0 to 3,
    soloChannels      = 6 to 7,
    genNumChannels    = 4,
    micInputs         = Vector(
      //      NamedBusConfig("m-dpa", 0 to 1)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 0 to 3)
    ),
    lineOutputs     = Vector(
      NamedBusConfig("sum", 6 to 7)
    ),
    device    = Some("Wolkenpumpe"),
    database  = None, // Some(mkDatabase(userHome/"Documents"/"projects"/"Anemone"/"sessions")),
    timeline  = true // false
  )

  private val config: Config = SegMod

  def mkSurface[S <: Sys[S]](config: Config)(implicit tx: S#Tx): Surface[S] =
    if (config.timeline) {
      val tl = Timeline[S]
      Surface.Timeline(tl)
    } else {
      val f = Folder[S]
      Surface.Folder(f)
    }

  def main(args: Array[String]): Unit = {
    println(s"Anemone Actiniaria $fullVersion")
    nuages.showLog = false
    // de.sciss.nuages. DSL.useScanFixed = true
    // defer(WebLookAndFeel.install())
    Submin.install(true)
    Wolkenpumpe .init()
    FScape      .init()

    config.database match {
      case Some(f) =>
        type S = Durable
        implicit val system: S = Durable(BerkeleyDB.factory(f))
        val anemone = new Anemone[S](config)
        val nuagesH = system.root { implicit tx =>
          Nuages[S](mkSurface(config))
        }
        anemone.run(nuagesH)

      case None =>
        type S = InMemory
        implicit val system: S = InMemory()
        val anemone = new Anemone[InMemory](config)
        val nuagesH = system.step { implicit tx =>
          val n = Nuages[S](mkSurface(config))
          tx.newHandle(n)
        }
        anemone.run(nuagesH)
    }
  }
}
class Anemone[S <: Sys[S]](config: Anemone.Config) extends WolkenpumpeMain[S] {

  override protected def configure(sCfg: ScissProcs.ConfigBuilder, nCfg: Nuages.ConfigBuilder,
                                   aCfg: Server.ConfigBuilder): Unit = {
    super.configure(sCfg, nCfg, aCfg)
    sCfg.genNumChannels  = config.genNumChannels
    // println(s"generatorChannels ${sCfg.generatorChannels}")
    nCfg.micInputs          = config.micInputs
    nCfg.lineInputs         = config.lineInputs
    nCfg.lineOutputs        = config.lineOutputs
    sCfg.masterGroups       = config.masterGroups
    // sCfg.highPass           = 100
    sCfg.audioFilesFolder   = Some(userHome / "Music" / "tapes")
    sCfg.plugins            = true
    sCfg.recDir             = file("/data/audio_work/nuages_test")

    // println(s"master max = ${Turbulence.ChannelIndices.max}")
    nCfg.masterChannels     = Some(config.masterChannels)
    nCfg.soloChannels       = if (config.soloChannels.nonEmpty) Some(config.soloChannels) else None
    nCfg.recordPath         = Some((userHome / "Music" / "rec").path) // XXX has no effect?

    aCfg.wireBuffers        = 512 // 1024
    aCfg.audioBuffers       = 4096
    // aCfg.blockSize          = 128
    if (config.device.isDefined) aCfg.deviceName = config.device
  }

  override protected def registerProcesses(nuages: Nuages[S], nCfg: Nuages.Config, sCfg: ScissProcs.Config)
                                 (implicit tx: S#Tx, universe: Universe[S]): Unit = {
    super.registerProcesses(nuages, nCfg, sCfg)
    Populate(nuages, nCfg, sCfg)
  }

  def initTablet(): Unit = {
    val penManager = AwtPenToolkit.getPenManager
    penManager.addListener(new PenManagerListener {
      def penDeviceAdded(c: PenProvider.Constructor, d: PenDevice): Unit = {
        println(s"penDeviceAdded($c, $d)")
      }

      def penDeviceRemoved(c: PenProvider.Constructor, d: PenDevice): Unit = ()
    })

    val awtComp = view.panel.display
    AwtPenToolkit.addPenListener(awtComp, new PenAdapter {
//      override def penButtonEvent(ev: PButtonEvent): Unit =
//        println(s"penButtonEvent($ev)")

      @volatile
      private[this] var lastLevel = 0f
      private[this] val panel     = view.panel
      // we take the maximum of these readings, so we don't lose the level when releasing the pen
      private[this] val levelMax  = new Array[Float](6)
      private[this] var levelMaxI = 0

      override def penLevelEvent(ev: PLevelEvent): Unit = {
        //        println(s"penLevelEvent($ev)")
        val levels = ev.levels
//        println(s"level: ${levels.mkString("[", ", ", "]")}")
        var i = 0
        while (i < levels.length) {
          val lvl = levels(i)

          // JPen 2.4 -- Tilt information is broken: https://github.com/nicarran/jpen/issues/12
          if (lvl.getType === PLevel.Type.PRESSURE /* TILT_Y */) {
            val raw: Float = lvl.value
            val arr = levelMax
            arr(levelMaxI) = raw
            levelMaxI = (levelMaxI + 1) % arr.length
            var j = 1
            var max = arr(0)
            while (j < arr.length) {
              val k = arr(j)
              if (k > max) max = k
              j += 1
            }
            val lvlClip = math.min(1.0f, math.max(0f, max - 0.1f) / 0.9f)
            // println(s"$raw | $max | $lvlClip | $lastLevel | ${panel.acceptGlideTime}")
            if (lvlClip != lastLevel) {
              lastLevel = lvlClip
              if (panel.acceptGlideTime) Swing.onEDT {
                if (panel.glideTimeSource !== "key") {
                  panel.glideTime       = lastLevel
                  panel.glideTimeSource = "tablet"
                }
              }
            }
            i = levels.length
          } else {
            i += 1
          }
        }
      }

//      override def penKindEvent(ev: PKindEvent): Unit = {
//        // println(s"penKindEvent($ev)")
//        println(s"kind: ${ev.kind}")
//      }

//      override def penScrollEvent(ev: PScrollEvent): Unit =
//        println(s"penScrollEvent($ev)")

      //      override def penTock(availableMillis: Long): Unit =
      //        println(s"penTock($availableMillis)")
    })
  }

  override def run(nuagesH: stm.Source[S#Tx, Nuages[S]])(implicit cursor: stm.Cursor[S]): Unit = {
    super.run(nuagesH)

    if (config.tablet) cursor.step { implicit tx =>
      auralSystem.whenStarted { server =>
        Swing.onEDT {
          initTablet()
        }
        if (Anemone.USE_OSC_LIGHTS) {
          println("Setting up OSC lights...")

          val transmitterOpt = Try {
            import osc.Implicits._
            val cfg = osc.UDP.Config()
            cfg.localAddress = "192.168.0.77"
            val transmitter = osc.UDP.Transmitter(cfg)
            transmitter.connect()
            transmitter
          } .toOption

          if (transmitterOpt.isEmpty) println("FAILED TO CREATED LIGHT OSC TRANSMITTER")

          Swing.onEDT {
            val p   = view.panel
            val vis = p.visualization
            val ctl = new LightFollowControl[S](view, vis)
            val dsp = p.display
            dsp.addControlListener(ctl)

            val tgt = new java.net.InetSocketAddress("192.168.0.25", 0x4C69)
            val graph = SynthGraph {
              import de.sciss.synth._
              import Ops._
              import ugen._
              val tr    = Impulse.kr(100.0)
              val sig1a = In.ar("in".kr(0f)).abs
              val sig1  = sig1a.squared // pow(8)
              val sig2  = In.ar(NumOutputBuses.ir).abs
              val sig   = Flatten(Seq(sig1, sig2))
              SendReply.kr(tr, sig, msgName = "/ld")
            }

            def mkRotary(value: Int, min: Int, max: Int)(fun: Int => Unit): Unit = {
              val gg = new RotaryKnob(new DefaultBoundedRangeModel(value, 1, min, max))
              gg.preferredSize = new Dimension(80, 80)
              gg.maximumSize = gg.preferredSize
              gg.minimumSize = gg.preferredSize
              gg.listenTo(gg)
              gg.reactions += {
                case ValueChanged(_) =>
                  fun(gg.value)
              }
              view.addSouthComponent(gg)
            }

            import numbers.Implicits._
            var mLightOff     = 0
            var mLightBoost1  = -40.dbAmp
            var mLightBoost2  = -40.dbAmp

            view.addSouthComponent(Swing.HStrut(8))
            mkRotary(0, 0, 255)(mLightOff = _)
            mkRotary(-40, -40, +40)(v => mLightBoost1 = v.dbAmp)
            mkRotary(-40, -40, +40)(v => mLightBoost2 = v.dbAmp)

            val transmit: (Int, Int) => Unit =
              transmitterOpt match {
                case Some(transmitter) => (rgb1, rgb2) => {
                  val p = osc.Message("/led", rgb1, rgb2)
                  transmitter.send(p, tgt)
                }

                case _ =>
                  val c1 = new LightView // Swing.HStrut(16)
                  val c2 = new LightView // Swing.HStrut(16)

                  view.addSouthComponent(c1)
                  view.addSouthComponent(c2)

                  (rgb1, rgb2) => {
                  // println(rgb1.toHexString)
                    Swing.onEDT {
                      c1.foreground = new Color(rgb1)
                      c2.foreground = new Color(rgb2)
                    }
                }
              }

            cursor.step { implicit tx =>
              val syn = Synth.play(graph, Some("light"))(server.defaultGroup, addAction = addAfter /* addToTail */,
                dependencies = /* node :: */ Nil)
              val NodeId = syn.peer.id
              import de.sciss.synth.message

              val trigResp = message.Responder.add(server.peer) {
                case osc.Message("/ld", NodeId, 0, v1: Float, v2: Float) =>
                  @inline def mkRGB(v: Float, boost: Float): Int = {
                    val i   = Math.max(0, Math.min((v * 256 * boost).toInt + mLightOff, 255))
                    val rgb = (i << 16) | (i << 8) | i
                    rgb
                  }
                  val rgb1  = mkRGB(v1, mLightBoost1)
                  val rgb2  = mkRGB(v2, mLightBoost2)
                  transmit(rgb1, rgb2)
              }
              // Responder.add is non-transactional. Thus, if the transaction fails, we need to remove it.
              scala.concurrent.stm.Txn.afterRollback { _ =>
                trigResp.remove()
              } (tx.peer)
              syn.onEnd(trigResp.remove())

              ctl.synth = Some(syn)
            }
          }
        }
      }
    }

//    defer {
//      Cracks.mkComponent(view)
//    }

//    /* if (config.device.contains("Finissage")) */ cursor.step { implicit tx =>
//      auralSystem.addClient(new AuralSystem.Client {
//        override def auralStarted(s: Server)(implicit tx: Txn): Unit = {
//          Cracks.initOSC(config.masterChannels.max + 1, s)
//
//        }
//
//        override def auralStopped()(implicit tx: Txn): Unit = ()
//      })
//    }
  }
}
