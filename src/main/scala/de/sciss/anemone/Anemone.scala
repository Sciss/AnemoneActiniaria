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
import de.sciss.lucre.stm
import de.sciss.lucre.stm.store.BerkeleyDB
import de.sciss.lucre.swing.defer
import de.sciss.lucre.synth.{Sys, InMemory}
import de.sciss.nuages
import de.sciss.nuages.ScissProcs.NuagesFinder
import de.sciss.nuages.{NamedBusConfig, Nuages, ScissProcs, Wolkenpumpe}
import de.sciss.synth.Server
import de.sciss.synth.proc.{Durable, AuralSystem}

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
                    database: Option[File] = None
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

  val Atelier = Scarlett.copy(
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

  private val config: Config = Scarlett // Atelier

  def main(args: Array[String]): Unit = {
    nuages.showLog = false
    defer(WebLookAndFeel.install())
    Wolkenpumpe.init()
    config.database match {
      case Some(f) =>
        type S = Durable
        implicit val system: S = Durable(BerkeleyDB.factory(f))
        val anemone = new Anemone[S](config)
        val nuagesH = system.root { implicit tx => Nuages[S] }
        anemone.run(nuagesH)

      case None =>
        type S = InMemory
        implicit val system: S = InMemory()
        val anemone = new Anemone[InMemory](config)
        val nuagesH = system.step { implicit tx => tx.newHandle(Nuages[S]) }
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
}
