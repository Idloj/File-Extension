package org.nlogo.extensions.file

import java.io.{ FileReader, LineNumberReader }

object PositionFileReader {
  private val maxSkipBufferSize = 8192
}

/** A [java.io.BufferedReader] that tracks the position inside the file (line & column) */
class PositionFileReader(path: String) extends LineNumberReader(new FileReader(path)) {
  import PositionFileReader._

  private var _column = 1
  private var markedColumn = 1

  /** The current line, starting from 1 */
  def line = getLineNumber + 1
  /** The current column, starting from 1 */
  def column = _column

  override def read() = {
    val oldLineNumber = getLineNumber
    val res = super.read()
    if (getLineNumber == oldLineNumber)
      _column += 1
    else
      _column = 1
    res
  }

  override def read(cbuf: Array[Char], off: Int, len: Int) = {
    val res = super.read(cbuf, off, len)
    val charsRead = cbuf.drop(off)
    val lastNewlineIndex = charsRead.lastIndexOf("\r") max charsRead.lastIndexOf("\n")
    if (lastNewlineIndex == -1)
      _column += charsRead.length
    else
      _column = charsRead.length - lastNewlineIndex
    res
  }

  override def readLine() = {
    val res = super.readLine()
    if (res != null)
      _column = 1
    res       
  }

  private var skipBuffer: Array[Char] = null
  override def skip(n: Long) = {
    // From java.io.LineNumberReader
    require(n >= 0, "skip() value is negative")
    val nn = (n min maxSkipBufferSize).toInt
    if ((skipBuffer == null) || (skipBuffer.length < nn))
      skipBuffer = new Array[Char](nn)
    var r = n
    var eof = false
    while (r > 0 && !eof) {
      val nc = read(skipBuffer, 0, (r min nn).toInt)
      if (nc == -1)
        eof = true
      r -= nc
    }
    n - r
  }

  override def mark(readAheadLimit: Int) = {
    super.mark(readAheadLimit)
    markedColumn = _column
  }

  override def reset() = {
    super.reset()
    _column = markedColumn
  }
}
