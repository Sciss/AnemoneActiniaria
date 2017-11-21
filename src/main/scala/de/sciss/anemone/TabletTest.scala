package de.sciss.anemone

import jpen.event.{PenAdapter, PenManagerListener}
import jpen.owner.multiAwt.AwtPenToolkit
import jpen.{PButtonEvent, PKindEvent, PLevelEvent, PScrollEvent, PenDevice, PenProvider}

import scala.swing.Swing._
import scala.swing.{Component, MainFrame}

object TabletTest {
  def main(args: Array[String]): Unit = {
    run() // JPenDemoControl.main(args: _*)
  }

  def run(): Unit = {
    val penManager = AwtPenToolkit.getPenManager
//    penManager.pen.setFirePenTockOnSwing(true)
//    penManager.pen.setFrequencyLater(40)
//    penManager.pen.levelEmulator.setPressureTriggerForLeftCursorButton(0.5F)
    penManager.addListener(new PenManagerListener {
      def penDeviceAdded(c: PenProvider.Constructor, d: PenDevice): Unit = {
        println(s"penDeviceAdded($c, $d)")
        c.getConstructed
      }

      def penDeviceRemoved(c: PenProvider.Constructor, d: PenDevice): Unit = ()
        // println(s"penDeviceRemoved($c, $d)")
    })

    val c = new Component {
      preferredSize = (400, 400)
    }

    new MainFrame {
      title = "Pen Test"
      contents = c
      pack().centerOnScreen()
      open()
    }

    AwtPenToolkit.addPenListener(c.peer, new PenAdapter {
      override def penButtonEvent(ev: PButtonEvent): Unit =
        println(s"penButtonEvent($ev)")

      override def penLevelEvent(ev: PLevelEvent): Unit = {
//        println(s"penLevelEvent($ev)")
        println(s"level: ${ev.levels.mkString("[", ", ", "]")}")
      }

      override def penKindEvent(ev: PKindEvent): Unit = {
        // println(s"penKindEvent($ev)")
        println(s"kind: ${ev.kind}")
      }

      override def penScrollEvent(ev: PScrollEvent): Unit =
        println(s"penScrollEvent($ev)")

//      override def penTock(availableMillis: Long): Unit =
//        println(s"penTock($availableMillis)")
    })
  }
}
