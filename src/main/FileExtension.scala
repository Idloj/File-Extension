package org.nlogo.extensions.file

import java.io.{ EOFException, File => JFile, FileNotFoundException, FileReader,
  IOException, LineNumberReader, StringReader }
import java.nio.file.{ Path, Paths }
import scala.collection.mutable

import org.nlogo.api.{ Argument, Command, Context, DefaultClassManager,
  ExtensionException, LocalFile, LogoException, OutputDestination, PrimitiveManager,
  Reporter }
import org.nlogo.api.ScalaConversions._  // implicits
import org.nlogo.core.{ AgentKind, CompilerException, File, FileMode }
import org.nlogo.core.Syntax._
import org.nlogo.nvm.{ ExtensionContext, ImportHandler }
import org.nlogo.parse.{ FrontEnd, LiteralParser, Namer0 }

case class OpenFile(var file: File, var reader: PositionFileReader)

class FileExtension extends DefaultClassManager {
  private val openFiles = mutable.Map[Path, OpenFile]()
  private var currentFile: Option[OpenFile] = None

  override def load(manager: PrimitiveManager) {
    manager.addPrimitive("at-end?", AtEnd)
    manager.addPrimitive("close", Close)
    manager.addPrimitive("close-all", CloseAll)
    manager.addPrimitive("delete", Delete)
    manager.addPrimitive("exists?", Exists)
    manager.addPrimitive("flush", Flush)
    manager.addPrimitive("open", Open)
    manager.addPrimitive("print", Print)
    manager.addPrimitive("read", Read)
    manager.addPrimitive("read-characters", ReadChars)
    manager.addPrimitive("read-line", ReadLine)
    manager.addPrimitive("show", Show)
    manager.addPrimitive("type", Type)
    manager.addPrimitive("write", Write)
  }

  def fm(context: Context): org.nlogo.nvm.FileManager = context.asInstanceOf[ExtensionContext].workspace.fileManager

  private def ensureMode(mode: FileMode) =
    currentFile.fold(throwNoOpenFile()) { file =>
      useMode(file.file, mode)
      file.reader = openFiles(Paths.get(file.file.getAbsolutePath)).reader
    }

  private def throwNoOpenFile() = throw new ExtensionException("No file has been opened.")

  private def useMode(file: File, mode: FileMode): Unit = (file.mode, mode) match {
    case (FileMode.None, mode) =>
      try {
        file.open(mode)
        if (mode == FileMode.Read) {
          val path = file.getAbsolutePath
          openFiles(Paths.get(path)).reader = new PositionFileReader(path)
        }
      } catch {
        case ex: FileNotFoundException =>
          throw new ExtensionException(s"The file ${file.getAbsolutePath} cannot be found")
        case ex: IOException =>
          throw new ExtensionException(ex.getMessage)
      }
    case (FileMode.Read, expectedMode) if expectedMode != FileMode.Read =>
      throw new ExtensionException("You can only use READING primitives with this file")
    case (currentMode, FileMode.Read) if currentMode != FileMode.Read =>
      throw new ExtensionException("You can only use WRITING primitives with this file")
    case _ =>
  }

  object AtEnd extends Reporter {
    override def getSyntax = reporterSyntax(ret = BooleanType)
    override def report(args: Array[Argument], context: Context) = {
      ensureMode(FileMode.Read)
      Boolean.box(!currentFile.get.reader.ready)
    }
  }

  object Close extends Command {
    override def getSyntax = commandSyntax()
    override def perform(args: Array[Argument], context: Context) = {
      currentFile foreach { case OpenFile(file, reader) =>
        openFiles -= Paths.get(file.getAbsolutePath)
        file.close(true)
        if (reader != null) reader.close()
        // temporary: we only need this line becuase `workspace.outputObject`
        // causes DefaultFileManager to use its _currentFile
        fm(context).closeCurrentFile()
      }
      currentFile = None
    }
  }

  object CloseAll extends Command {
    override def getSyntax = commandSyntax()
    override def perform(args: Array[Argument], context: Context) = {
      openFiles.values foreach {
        case OpenFile(file, reader) =>
          file.close(true)
          if (reader != null) reader.close()
      }
      openFiles.clear()
      currentFile = None
      // temporary: we only need this line becuase `workspace.outputObject`
      // causes DefaultFileManager to use its _currentFile
      fm(context).closeAllFiles()
    }
  }

  object Delete extends Command {
    override def getSyntax = commandSyntax(right = List(StringType))
    override def perform(args: Array[Argument], context: Context) = {
      val path = context.attachCurrentDirectory(args(0).getString)
      if (openFiles.contains(Paths.get(path).toAbsolutePath))
        throw new ExtensionException("You need to close the file before deletion")

      val file = new JFile(path)

      if (!file.exists)
        throw new ExtensionException("You cannot delete a non-existent file.")
      else if (!file.canWrite)
        throw new ExtensionException("Modification to this file is denied.")
      else if (!file.isFile)
        throw new ExtensionException("You can only delete files.")
      else if (!file.delete())
        throw new ExtensionException("Deletion failed.")
    }
  }

  object Exists extends Reporter {
    override def getSyntax = reporterSyntax(right = List(StringType), ret = BooleanType)
    override def report(args: Array[Argument], context: Context) = {
      Boolean.box(new JFile(context.attachCurrentDirectory(args(0).getString)).exists)
    }
  }

  object Flush extends Command {
    override def getSyntax = commandSyntax()
    override def perform(args: Array[Argument], context: Context) = {
      currentFile foreach (_.file.flush())
    }
  }

  object Open extends Command {
    override def getSyntax = commandSyntax(right = List(StringType))
    override def perform(args: Array[Argument], context: Context) = {
      val path = Paths.get(context.attachCurrentDirectory(args(0).getString)).toAbsolutePath
      val openFile = OpenFile(new LocalFile(path.toString), null)
      currentFile = Some(openFiles.getOrElseUpdate(path, openFile))
      // temporary: we only need this line becuase `workspace.outputObject`
      // causes DefaultFileManager to use its _currentFile
      fm(context).openFile(args(0).getString)
    }
  }

  object Print extends Command {
    override def getSyntax = commandSyntax(right = List(WildcardType))
    override def perform(args: Array[Argument], context: Context) = {
      ensureMode(FileMode.Append)
      // temporary: we only need this line becuase `workspace.outputObject`
      // causes DefaultFileManager to use its _currentFile
      fm(context).ensureMode(FileMode.Append)
      context.workspace.outputObject(args(0).get, null, true, false, OutputDestination.File)
    }
  }

  object Read extends Reporter {
    override def getSyntax = reporterSyntax(ret = ReadableType)
    override def report(args: Array[Argument], context: Context) = {
      ensureMode(FileMode.Read)
      val reader = currentFile.get.reader
      if (!reader.ready)
        throw new ExtensionException("The end of file has been reached")
      val line = reader.line
      val column = reader.column
      try {
        val importHandler = new ImportHandler(context.world, context.workspace.getExtensionManager)
        val tokens =
          FrontEnd.tokenizer.tokenizeSkippingTrailingWhitespace(currentFile.get.reader)
            .map(_._1).map(Namer0)
        new LiteralParser(importHandler).readLiteralPrefix(tokens.next(), tokens)
      } catch {
        case ex: CompilerException =>
          val errorInfo = s" (line number ${line}, character ${column})"
          throw new ExtensionException(ex.getMessage + errorInfo)
      }
    }
  }

  object ReadChars extends Reporter {
    override def getSyntax = reporterSyntax(right = List(NumberType), ret = StringType)
    override def report(args: Array[Argument], context: Context) = {
      ensureMode(FileMode.Read)
      val buf = new Array[Char](args(0).getIntValue)
      val numRead = currentFile.get.reader.read(buf, 0, buf.length)
      if (numRead == -1)
        throw new ExtensionException("The end of file has been reached")
      new String(buf)
    }
  }

  object ReadLine extends Reporter {
    override def getSyntax = reporterSyntax(ret = StringType)
    override def report(args: Array[Argument], context: Context) = {
      ensureMode(FileMode.Read)
      val line = currentFile.get.reader.readLine()
      if (line == null)
        throw new ExtensionException("The end of file has been reached")
      line
    }
  }

  object Show extends Command {
    override def getSyntax = commandSyntax(right = List(WildcardType))
    override def perform(args: Array[Argument], context: Context) = {
      ensureMode(FileMode.Append)
      // temporary: we only need this line becuase `workspace.outputObject`
      // causes DefaultFileManager to use its _currentFile
      fm(context).ensureMode(FileMode.Append)
      context.workspace.outputObject(args(0).get, context.getAgent, true, true, OutputDestination.File)
    }
  }

  object Type extends Command {
    override def getSyntax = commandSyntax(right = List(WildcardType))
    override def perform(args: Array[Argument], context: Context) = {
      ensureMode(FileMode.Append)
      // temporary: we only need this line becuase `workspace.outputObject`
      // causes DefaultFileManager to use its _currentFile
      fm(context).ensureMode(FileMode.Append)
      context.workspace.outputObject(args(0).get, null, false, false, OutputDestination.File)
    }
  }

  object Write extends Command {
    override def getSyntax = commandSyntax(right = List(ReadableType))
    override def perform(args: Array[Argument], context: Context) = {
      ensureMode(FileMode.Append)
      // temporary: we only need this line becuase `workspace.outputObject`
      // causes DefaultFileManager to use its _currentFile
      fm(context).ensureMode(FileMode.Append)
      context.workspace.outputObject(args(0).get, null, false, true, OutputDestination.File)
    }
  }
}
