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
import de.sciss.nuages.{Nuages, ScissProcs, Wolkenpumpe, NamedBusConfig}
import de.sciss.synth.Server
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

  override protected def registerProcesses(sCfg: ScissProcs.Config, nCfg: Nuages.Config)
                                          (implicit tx: S#Tx, cursor: stm.Cursor[InMemory],
                                           nuages: Nuages[S], aural: AuralSystem): Unit = {
    // super.registerProcesses(sCfg, nCfg)
    Populate.registerActions[S]()
    Populate.apply(nuages, nCfg, sCfg)
  }
}
