/*
 *  LightFollowControl.scala
 *  (Anemone-Actiniaria)
 *
 *  Copyright (c) 2014-2021 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.anemone

import de.sciss.lucre.synth.{AudioBus, RT, Synth, Txn}
import de.sciss.lucre.{Cursor, Disposable, TxnLike}
import de.sciss.lucre.Txn.peer
import de.sciss.nuages.{NuagesAttribute, NuagesObj, NuagesParam, NuagesShapeRenderer, NuagesView}
import de.sciss.proc.Proc
import prefuse.Visualization
import prefuse.controls.ControlAdapter
import prefuse.visual.VisualItem

import java.awt.event.MouseEvent
import scala.concurrent.stm.Ref

class LightFollowControl[T <: Txn[T]](view: NuagesView[T], vis: Visualization) extends ControlAdapter {
  private[this] val csr: Cursor[T] = view.panel.cursor

  private[this] val synthRef  = Ref(Option.empty[Synth])
  private[this] val busRef    = Ref(Option.empty[AudioBus])
  private[this] val busSetter = Ref(Disposable.empty[RT])

  private[this] var lastPressed: AnyRef = _

  def synth(implicit tx: TxnLike): Option[Synth] = synthRef()
  def synth_=(value: Option[Synth])(implicit tx: TxnLike): Unit = synthRef() = value

  private def nodePressed(data: NuagesObj[T])(implicit tx: T): Unit =
    for {
      out <- data.getOutput(Proc.mainOut)
      m   <- out.meterOption
    } {
      // println(s"BUS = ${m.bus.busOption}")
      val now     = m.bus
      val before  = busRef.swap(Some(now))
      // println("HA")
      if (!before.contains(now)) {
        // println(s"NEW BUS $now")
        synth.foreach { syn =>
          busSetter().dispose()
          // println(s"BUS $now")
          val reader = syn.read(now -> "in")
          busSetter() = reader
        }
      }
    }

  override def itemEntered(vi: VisualItem, e: MouseEvent): Unit = superChecker(vi)

//  override def itemPressed(vi: VisualItem, e: MouseEvent): Unit = superChecker(vi)

  @inline
  private def superChecker(vi: VisualItem): Unit = {
    val r = vis.getRenderer(vi)
    r match {
      case _ /* pr */: NuagesShapeRenderer[_] =>
        vi.get("nuages" /* COL_NUAGES */) match {
          case data: NuagesObj[T] =>
            if (data == lastPressed) return
            lastPressed = data

            csr.step { implicit tx =>
              nodePressed(data)
            }

          case i: NuagesAttribute.Input[T] =>
            // println("DING")
            if (i == lastPressed) return
            lastPressed = i
            // println("DONG")

            csr.step { implicit tx =>
              i.inputParent match {
                case p: NuagesParam[T] =>
                  // println("DANG")
                  nodePressed(p.parent)
                case _ =>
              }
            }

//          case p: NuagesParam [T] =>
//            csr.step { implicit tx =>
//              nodePressed(p.parent)
//            }

          case _ =>
            // println("NOPE")
        }

      case _ =>
    }
  }
}