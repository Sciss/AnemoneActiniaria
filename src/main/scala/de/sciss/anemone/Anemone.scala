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

import java.text.SimpleDateFormat
import java.util.Locale

import com.alee.laf.WebLookAndFeel
import de.sciss.file._
import de.sciss.nuages.Nuages.Surface

import de.sciss.lucre.stm
import de.sciss.lucre.stm.store.BerkeleyDB
import de.sciss.lucre.swing.defer
import de.sciss.lucre.synth.{InMemory, Sys}
import de.sciss.nuages
import de.sciss.nuages.ScissProcs.NuagesFinder
import de.sciss.nuages.{NamedBusConfig, Nuages, ScissProcs, Wolkenpumpe}
import de.sciss.synth.Server
import de.sciss.synth.proc.{AuralSystem, Durable, Folder, Timeline}

import scala.collection.immutable.{IndexedSeq => Vec}

object Anemone {
  def mkDatabase(parent: File): File = {
    val recFormat = new SimpleDateFormat("'session_'yyMMdd'_'HHmmss", Locale.US)
    parent / recFormat.format(new java.util.Date)
  }

  case class Config(masterChannels: Range, soloChannels: Range,
                    micInputs  : Vec[NamedBusConfig],
                    lineInputs : Vec[NamedBusConfig],
                    lineOutputs: Vec[NamedBusConfig],
                    generatorChannels: Int = 0,
                    device: Option[String] = None,
                    database: Option[File] = None,
                    timeline: Boolean = true
  )

  val Scarlett = Config(
    masterChannels    =  0 to 15,
    soloChannels      = 16 to 17,
    generatorChannels = 4,
    micInputs         = Vector(
      NamedBusConfig("m-dpa", 0, 2)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 5, 1),
      NamedBusConfig("beat" , 4, 1)
    ),
    lineOutputs     = Vector(
      // NamedBusConfig("sum", 6, 2)
    ),
    device    = Some("Wolkenpumpe-16"),
    database  = None // Some(mkDatabase(userHome/"Documents"/"applications"/"150131_ZKM"/"sessions"))
  )

  val ZKMAtelier = Scarlett.copy(
    masterChannels  = 0 to 7,
    micInputs       = Vector.empty,
    device          = Some("Wolkenpumpe"),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 0, 4)
    ),
    lineOutputs = Vector(
      NamedBusConfig("sum", 8, 2)
    )
  )

  // jack_netsource -H 169.254.1.2 -o 2 -i 18 -N david
  val GrazAtelier = Scarlett.copy(
    masterChannels  = 0 to 23,
    soloChannels    = 26 to 27,
    micInputs       = Vector.empty,
    device          = Some("Wolkenpumpe-24"),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 0, 1),
      NamedBusConfig("beat" , 1, 1)
    ),
    lineOutputs = Vector(
      NamedBusConfig("sum", 24, 2)
    )
  )

  val Forum = Config(
    masterChannels    = 0 to 3,
    soloChannels      = 4 to 5,
    generatorChannels = 4,
    micInputs         = Vector(
      NamedBusConfig("m-dpa", 0, 2)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 5, 1),
      NamedBusConfig("beat" , 4, 1)
    ),
    lineOutputs     = Vector(
      NamedBusConfig("sum", 6, 2)
    ),
    device    = Some("Wolkenpumpe"),
    database  = None // Some(mkDatabase(userHome/"Documents"/"applications"/"150131_ZKM"/"sessions"))
  )

  val Feierabend = Config(
    masterChannels    = 0 to 1,
    soloChannels      = 2 to 3,
    generatorChannels = 2,
    micInputs         = Vector(
//      NamedBusConfig("m-dpa", 0, 2)
    ),
    lineInputs      = Vector(
      NamedBusConfig("pirro", 0, 1),
      NamedBusConfig("beat" , 1, 1)
    ),
    lineOutputs     = Vector(
//      NamedBusConfig("sum", 6, 2)
    ),
    device    = Some("Wolkenpumpe"),
    database  = Some(mkDatabase(userHome/"Documents"/"projects"/"Anemone"/"sessions"))
  )

  private val config: Config = Feierabend

  def mkSurface[S <: Sys[S]](config: Config)(implicit tx: S#Tx): Surface[S] =
    if (config.timeline) {
      val tl = Timeline[S]
      Surface.Timeline(tl)
    } else {
      val f = Folder[S]
      Surface.Folder(f)
    }

  def main(args: Array[String]): Unit = {
    nuages.showLog = false
    defer(WebLookAndFeel.install())
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
class Anemone[S <: Sys[S]](config: Anemone.Config) extends Wolkenpumpe[S] {

  override protected def configure(sCfg: ScissProcs.ConfigBuilder, nCfg: Nuages.ConfigBuilder,
                                   aCfg: Server.ConfigBuilder): Unit = {
    super.configure(sCfg, nCfg, aCfg)
    sCfg.generatorChannels  = config.generatorChannels
    // println(s"generatorChannels ${sCfg.generatorChannels}")
    sCfg.micInputs          = config.micInputs
    sCfg.lineInputs         = config.lineInputs
    sCfg.lineOutputs        = config.lineOutputs
    // sCfg.highPass           = 100
    sCfg.audioFilesFolder   = None // Some(userHome / "Music" / "tapes")

    // println(s"master max = ${Turbulence.ChannelIndices.max}")
    nCfg.masterChannels     = Some(config.masterChannels)
    nCfg.soloChannels       = Some(config.soloChannels)
    nCfg.recordPath         = Some((userHome / "Music" / "rec").path) // XXX has no effect?

    aCfg.wireBuffers        = 512 // 1024
    aCfg.audioBuffers       = 4096
    // aCfg.blockSize          = 128
    if (config.device.isDefined) aCfg.deviceName = config.device
  }

  override protected def registerProcesses(sCfg: ScissProcs.Config, nCfg: Nuages.Config, nuagesFinder: NuagesFinder)
                                          (implicit tx: S#Tx, cursor: stm.Cursor[S], nuages: Nuages[S],
                                           aural: AuralSystem): Unit = {
    super.registerProcesses(sCfg, nCfg, nuagesFinder)
    // Populate.registerActions[S]()
    Populate.apply(nuages, nCfg, sCfg)
  }

//  override def run(nuagesH: Source[S#Tx, Nuages[S]])(implicit cursor: Cursor[S]): Unit = {
//    super.run(nuagesH)
//    config.micInputs.find(_.name == "m-dpa").foreach { micConfig =>
//      defer {
//        val tutti = new Tutti(micIn = micConfig.offset)
//        view.addSouthComponent(tutti)
//      }
//    }
//  }

//  private class Tutti(micIn: Int)
//    extends Component {
//
//    preferredSize = new Dimension(32, 32)
//    minimumSize   = preferredSize
//    maximumSize   = preferredSize
//
//    override protected def paintComponent(g: Graphics2D): Unit = {
//      val colr = state.single.get match {
//        case 0 => Color.blue
//        case 1 => Color.orange
//        case _ => Color.black
//      }
//      g.setColor(colr)
//      val w = peer.getWidth
//      val h = peer.getHeight
//      val y = (h - 32) >> 1
//      // println(s"w = $w, h = $h")
//      g.fillRect(0, y, w, 32)
//    }
//
//    listenTo(mouse.clicks)
//    reactions += {
//      case _: MousePressed => toggleState()
//    }
//
//    def toggleState(): Unit = {
//      state.single.get match {
//        case 0 => startRecord()
//        case 1 => cancelRecord()
//      }
//      repaint()
//    }
//
//    private val state     = Ref(0)
//    private val recSynth  = Ref(Option.empty[Synth])
//
//    private def cancelRecord(): Unit = {
//      val p = view.panel
//      p.cursor.step { implicit tx =>
//        require(state.swap(0)(tx.peer) == 1)
//        recSynth.swap(None).foreach(_.dispose())
//      }
//    }
//
//    private def startRecord(): Unit = {
//      val p     = view.panel
//      val path  = File.createTemp(suffix = "aif").path
//      p.cursor.step { implicit tx =>
//        require(recSynth.get(tx.peer).isEmpty)
//        require(state.get(tx.peer) == 0)
//        // val tl  = p.nuages.timeline
//        // val pos = p.transport.position
//        auralSystem.serverOption.foreach { s =>
//          val gr = SynthGraph {
//            import synth._
//            import ugen._
//            val bufId = "buf".kr
//            val sig   = PhysicalIn.ar(micIn)
//            DiskOut.ar(bufId, sig * 12.dbamp)
//            val dur   = 14 * 60 // less than fifteen minutes
//            Line.kr(dur = dur, doneAction = freeSelf)
//          }
//          val buf = Buffer.diskOut(s)(path = path, numChannels = 1)
//          val syn = Synth.play(gr, nameHint = Some("tutti"))(
//            target = s, args = List("buf" -> buf.id), dependencies = buf :: Nil)
//          syn.onEndTxn { implicit tx =>
//            if (state.get(tx.peer) == 1) {  // not cancelled
//
//            }
//          }
//          recSynth.set(Some(syn))(tx.peer)
//          state.set(1)(tx.peer)
//        }
//      }
//    }
//
//    private lazy val fscape = FScapeJobs()
//
//    private val workFolder = userHome/"Documents"/"misc"/"Anemone"/"zkm_rec"
//
//    private def runFScape(inPath: String): Unit = {
//      import FScapeJobs._
//      val sliceOut = workFolder / "slice.aif"
//      fscape.connect() { ok =>
//        val docSlice = Slice(in = inPath, out = sliceOut.path, spec = OutputSpec.aiffFloat, separateFiles = true,
//          initialSkip = "0s", sliceLength = "1s", skipLength = "0s", finalSkip = "0s",
//          autoScale = true, autoNum = 16)
//        val docMerge = FScapeJobs.Channel
//        fscape.processChain(name = ???, docs = ???, progress = ???)
//      }
//    }
//  }
//
}
