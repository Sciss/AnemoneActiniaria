package de.sciss.anemone

import scala.swing.{Component, Dimension, Graphics2D}

class LightView extends Component {
  private[this] val extent = 16

  preferredSize = new Dimension(extent, 16)
  minimumSize   = {
    val d = minimumSize
    d.width = extent
    d
  }
  maximumSize = {
    val d = maximumSize
    d.width = extent
    d
  }

  opaque    = true
  focusable = false

  override protected def paintComponent(g: Graphics2D): Unit = {
    val p   = peer
    val pw  = p.getWidth
    val ph  = p.getHeight
    g.fillRect(0, 0, pw, ph)
  }
}
