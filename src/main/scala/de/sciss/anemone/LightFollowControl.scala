/*
 *  LightFollowControl.scala
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

import java.awt.event.MouseEvent

import de.sciss.lucre.stm
import de.sciss.lucre.stm.TxnLike.peer
import de.sciss.lucre.stm.{Disposable, TxnLike}
import de.sciss.lucre.synth.{AudioBus, Synth, Sys, Txn}
import de.sciss.nuages.impl.{NuagesObjImpl, NuagesOutputImpl}
import de.sciss.nuages.{NuagesAttribute, NuagesObj, NuagesParam, NuagesShapeRenderer, NuagesView}
import de.sciss.synth.proc.Proc
import prefuse.Visualization
import prefuse.controls.ControlAdapter
import prefuse.visual.VisualItem

import scala.concurrent.stm.Ref

class LightFollowControl[S <: Sys[S]](view: NuagesView[S], vis: Visualization) extends ControlAdapter {
  private[this] val csr: stm.Cursor[S] = view.panel.cursor

  private[this] val synthRef  = Ref(Option.empty[Synth])
  private[this] val busRef    = Ref(Option.empty[AudioBus])
  private[this] val busSetter = Ref(Disposable.empty[Txn])

  private[this] var lastPressed: AnyRef = _

  def synth(implicit tx: TxnLike): Option[Synth] = synthRef()
  def synth_=(value: Option[Synth])(implicit tx: TxnLike): Unit = synthRef() = value

  private def nodePressed(data: NuagesObj[S])(implicit tx: S#Tx): Unit = data match {
    case impl: NuagesObjImpl[S] =>
      impl.getOutput(Proc.mainOut) match {
        case Some(out: NuagesOutputImpl[S]) =>
          out.meterOption match {
            case Some(m: NuagesOutputImpl.Meter) =>
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

            case _ =>
            // println("NOPE 3")
          }

        case _ =>
        // println("NOPE 2")
      }

    case _ =>
  }

  override def itemEntered(vi: VisualItem, e: MouseEvent): Unit = superChecker(vi)

//  override def itemPressed(vi: VisualItem, e: MouseEvent): Unit = superChecker(vi)

  @inline
  private def superChecker(vi: VisualItem): Unit = {
    val r = vis.getRenderer(vi)
    r match {
      case _ /* pr */: NuagesShapeRenderer[_] =>
        vi.get("nuages" /* COL_NUAGES */) match {
          case data: NuagesObj[S] =>
            if (data == lastPressed) return
            lastPressed = data

            csr.step { implicit tx =>
              nodePressed(data)
            }

          case i: NuagesAttribute.Input[S] =>
            // println("DING")
            if (i == lastPressed) return
            lastPressed = i
            // println("DONG")

            csr.step { implicit tx =>
              i.inputParent match {
                case p: NuagesParam[S] =>
                  // println("DANG")
                  nodePressed(p.parent)
                case _ =>
              }
            }

//          case p: NuagesParam [S] =>
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