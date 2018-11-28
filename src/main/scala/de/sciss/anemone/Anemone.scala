/*
 *  Anemone.scala
 *  (Anemone-Actiniaria)
 *
 *  Copyright (c) 2014-2017 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.anemone

import java.text.SimpleDateFormat
import java.util.Locale

import de.sciss.equal.Implicits._
import de.sciss.file._
import de.sciss.lucre.stm
import de.sciss.lucre.stm.Folder
import de.sciss.lucre.stm.store.BerkeleyDB
import de.sciss.lucre.synth._
import de.sciss.nuages
import de.sciss.nuages.Nuages.Surface
import de.sciss.nuages.{NamedBusConfig, Nuages, ScissProcs, Wolkenpumpe, WolkenpumpeMain}
import de.sciss.submin.Submin
import de.sciss.synth.proc.{Durable, Timeline, Universe}
import jpen.event.{PenAdapter, PenManagerListener}
import jpen.owner.multiAwt.AwtPenToolkit
import jpen.{PLevel, PLevelEvent, PenDevice, PenProvider}

import scala.collection.immutable.{IndexedSeq => Vec}
import scala.swing.Swing
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

  final case class Config(
                          masterChannels    : Range,
                          masterGroups      : Vec[NamedBusConfig] = Vector.empty,
                          soloChannels      : Range,
                          micInputs         : Vec[NamedBusConfig],
                          lineInputs        : Vec[NamedBusConfig],
                          lineOutputs       : Vec[NamedBusConfig],
                          generatorChannels : Int                 = 0,
                          device            : Option[String]      = None,
                          database          : Option[File]        = None,
                          timeline          : Boolean             = true,
                          tablet            : Boolean             = true
  )

  lazy val Scarlett = Config(
    masterChannels    =  0 to 15,
    soloChannels      = 16 to 17,
    generatorChannels = 4,
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
    generatorChannels = 4,
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
    generatorChannels = 4,
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
    generatorChannels = 4,
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
    generatorChannels = 2, // 4,
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
    generatorChannels = 4,
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
    generatorChannels = 4,
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
    generatorChannels = 4,
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
    generatorChannels = 4,
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
    generatorChannels = 4,
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
    generatorChannels = 4,
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
    generatorChannels = 4,
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

  private val config: Config = Schwaermen

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
    Wolkenpumpe.init()
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
    sCfg.generatorChannels  = config.generatorChannels
    // println(s"generatorChannels ${sCfg.generatorChannels}")
    nCfg.micInputs          = config.micInputs
    nCfg.lineInputs         = config.lineInputs
    nCfg.lineOutputs        = config.lineOutputs
    sCfg.masterGroups       = config.masterGroups
    // sCfg.highPass           = 100
    sCfg.audioFilesFolder   = Some(userHome / "Music" / "tapes")
    sCfg.plugins            = true

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
      auralSystem.whenStarted { _ =>
        Swing.onEDT {
          initTablet()
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
