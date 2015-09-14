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
import de.sciss.file._
import de.sciss.lucre.stm
import de.sciss.lucre.swing.defer
import de.sciss.lucre.synth.InMemory
import de.sciss.nuages
import de.sciss.nuages.ScissProcs.NuagesFinder
import de.sciss.nuages.{NamedBusConfig, Nuages, ScissProcs, Wolkenpumpe}
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
    device = Some("Wolkenpumpe-16")
  )

  val config: Config = Scarlett

  def main(args: Array[String]): Unit = {
    nuages.showLog = false
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
