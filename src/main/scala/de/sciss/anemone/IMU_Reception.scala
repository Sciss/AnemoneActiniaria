/*
 *  IMU_Reception.scala
 *  (Strip and Embellish)
 *
 *  Copyright (c) 2022-2023 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.anemone

import de.sciss.lucre.synth.{Bus, Txn}
import de.sciss.lucre.{Cursor, IntObj}
import de.sciss.nuages.{Nuages, ScissProcs, Util, WolkenpumpeMain}
import de.sciss.proc.{AuralSystem, Proc, SoundProcesses}
import de.sciss.synth.{GE, message}
import de.sciss.synth.proc.graph.ScanOut
import de.sciss.{nuages, osc}

object IMU_Reception {
  def apply[T <: Txn[T]](nm: WolkenpumpeMain[T], dsl: nuages.DSL[T], sCfg: ScissProcs.Config, nCfg: Nuages.Config)
                        (implicit tx: T, n: Nuages[T]): Unit = {
    import dsl._
    import sCfg.genNumChannels

    class Sensor(val name: String, val bus: IntObj.Var[T])

    def ForceChan(in: GE): GE = if (genNumChannels <= 0) in else {
      Util.wrapExtendChannels(genNumChannels, in)
    }

    def genMul(name: String, sensors: Seq[Sensor])(fun: => GE)(implicit tx: T, n: Nuages[T]): Proc[T] = {
      val obj = mkProcObj(name) {
        val sig = fun
        import de.sciss.synth.GEOps._
        sensors.zipWithIndex.foreach { case (sensor, ni) =>
          val sigI  = sig.out(ni)
          val sigIC = ForceChan(sigI)
          ScanOut(sensor.name, sigIC)
        }
      }
      val pOuts = obj.outputs
      val pIns  = obj.attr
      sensors.foreach { sensor =>
        pOuts.add(sensor.name)
        pIns.put(s"$$${sensor.name}", sensor.bus)
      }
      val genOpt = n.generators
      insertByName(genOpt.get, obj)
      obj
    }

    val sensorsBothSeq = Seq("L", "R").zipWithIndex.map { case (lr, chMk) =>
      val sensorHead  = new Sensor("head", IntObj.newVar[T](0))
      val sensorRoll  = new Sensor("roll", IntObj.newVar[T](0))
      val sensorPitch = new Sensor("pitch", IntObj.newVar[T](0))
      val sensorsE    = Seq(sensorHead, sensorRoll, sensorPitch)

      val sensorAX    = new Sensor("X", IntObj.newVar[T](0))
      val sensorAY    = new Sensor("Y", IntObj.newVar[T](0))
      val sensorAZ    = new Sensor("Z", IntObj.newVar[T](0))
      val sensorsA    = Seq(sensorAX, sensorAY, sensorAZ)

      genMul(s"euler$lr", sensorsE) {
        import de.sciss.synth.proc.graph.Ops._
        import de.sciss.synth.ugen._
        val sigSeq: GE = sensorsE.map { sensor =>
          val bus = s"$$${sensor.name}".kr(0)
          val sig = In.kr(bus)
          sig
        }
        sigSeq
      }

      genMul(s"linac$lr", sensorsA) {
        import de.sciss.synth.proc.graph.Ops._
        import de.sciss.synth.ugen._
        val sigSeq: GE = sensorsA.map { sensor =>
          val bus = s"$$${sensor.name}".kr(0)
          val sig = In.kr(bus)
          sig
        }
        sigSeq
      }

      sensorsE ++ sensorsA
    }

    nm.auralSystem.reactNow { implicit tx => {
      case AuralSystem.Running(s) =>
        tx.afterCommit {
          val oCfg = osc.UDP.Config()
          oCfg.localPort = 7771
          oCfg.localIsLoopback = true
          val rcv = osc.UDP.Receiver(oCfg)
          rcv.connect()

          implicit val cursor: Cursor[T] = nm.view.cursor

          SoundProcesses.step[T]("sensor-init") { implicit tx =>
            val Seq(sensorsL, sensorsR) = sensorsBothSeq
            val Seq(sensorLHead, sensorLRoll, sensorLPitch, sensorLAX, sensorLAY, sensorLAZ) = sensorsL
            val Seq(sensorRHead, sensorRRoll, sensorRPitch, sensorRAX, sensorRAY, sensorRAZ) = sensorsR

            val busIndexLHead   = s.allocControlBus(1)
            val busIndexLRoll   = s.allocControlBus(1)
            val busIndexLPitch  = s.allocControlBus(1)
            sensorLHead  .bus() = IntObj.newConst[T](busIndexLHead)
            sensorLRoll  .bus() = IntObj.newConst[T](busIndexLRoll)
            sensorLPitch .bus() = IntObj.newConst[T](busIndexLPitch)
            val busIndexLAX     = s.allocControlBus(1)
            val busIndexLAY     = s.allocControlBus(1)
            val busIndexLAZ     = s.allocControlBus(1)
            sensorLAX.bus()     = IntObj.newConst[T](busIndexLAX)
            sensorLAY.bus()     = IntObj.newConst[T](busIndexLAY)
            sensorLAZ.bus()     = IntObj.newConst[T](busIndexLAZ)

            val busIndexRHead   = s.allocControlBus(1)
            val busIndexRRoll   = s.allocControlBus(1)
            val busIndexRPitch  = s.allocControlBus(1)
            sensorRHead.bus()   = IntObj.newConst[T](busIndexRHead)
            sensorRRoll.bus()   = IntObj.newConst[T](busIndexRRoll)
            sensorRPitch.bus()  = IntObj.newConst[T](busIndexRPitch)
            val busIndexRAX     = s.allocControlBus(1)
            val busIndexRAY     = s.allocControlBus(1)
            val busIndexRAZ     = s.allocControlBus(1)
            sensorRAX.bus()     = IntObj.newConst[T](busIndexRAX)
            sensorRAY.bus()     = IntObj.newConst[T](busIndexRAY)
            sensorRAZ.bus()     = IntObj.newConst[T](busIndexRAZ)

            rcv.action = {
              case (osc.Message(cmd, eH: Float, eR: Float, eP: Float, ax: Float, ay: Float, az: Float), _) if cmd.startsWith("/sensor") =>
                val ch = cmd.charAt(cmd.length - 1) - '1'
                // println(s"head $ch is $eh")
                if (ch == 0) {
                  s.peer ! message.ControlBusSet(
                    busIndexLHead  -> (((eH + 360f) / 360f) % 1.0f),
                    busIndexLRoll  -> (((eR + 360f) / 180f) % 1.0f),
                    busIndexLPitch -> (((eP + 360f) / 360f) % 1.0f),
                    busIndexLAX    -> (ax + 0.5f),
                    busIndexLAY    -> (ay + 0.5f),
                    busIndexLAZ    -> (az + 0.5f),
                  )
                } else if (ch == 1) {
                  s.peer ! message.ControlBusSet(
                    busIndexRHead  -> (((eH + 360f) / 360f) % 1.0f),
                    busIndexRRoll  -> (((eR + 360f) / 180f) % 1.0f),
                    busIndexRPitch -> (((eP + 360f) / 360f) % 1.0f),
                    busIndexRAX    -> (ax + 0.5f),
                    busIndexRAY    -> (ay + 0.5f),
                    busIndexRAZ    -> (az + 0.5f),
                  )
                }

              case (p, _) =>
                println(s"Unknown OSC packet $p")
            }
          }
        }

      case _ => ()
    }}
  }
}
