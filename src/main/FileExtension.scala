package org.nlogo.extensions.file

import org.nlogo.api.{ Argument, Command, Context, DefaultClassManager,
  ExtensionException, LogoException, OutputDestination, PrimitiveManager, Reporter }
import org.nlogo.api.ScalaConversions._  // implicits
import org.nlogo.core.{ AgentKind, FileMode }
import org.nlogo.core.Syntax._
import org.nlogo.nvm.ExtensionContext

class FileExtension extends DefaultClassManager {
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

  def toExtensionException[T](f: => T) = try f catch { case ex: Exception => throw new ExtensionException(ex) }
  def fm(context: Context): org.nlogo.nvm.FileManager = context.asInstanceOf[ExtensionContext].workspace.fileManager

  object AtEnd extends Reporter {
    override def getSyntax = reporterSyntax(ret = BooleanType)
    override def report(args: Array[Argument], context: Context) = toExtensionException {
      Boolean.box(fm(context).eof)
    }
  }

  object Close extends Command {
    override def getSyntax = commandSyntax()
    override def perform(args: Array[Argument], context: Context) = toExtensionException {
      if (fm(context).hasCurrentFile) fm(context).closeCurrentFile()
    }
  }

  object CloseAll extends Command {
    override def getSyntax = commandSyntax()
    override def perform(args: Array[Argument], context: Context) = toExtensionException {
      fm(context).closeAllFiles()
    }
  }

  object Delete extends Command {
    override def getSyntax = commandSyntax(right = List(StringType))
    override def perform(args: Array[Argument], context: Context) = toExtensionException {
      fm(context).deleteFile(fm(context).attachPrefix(args(0).getString))
    }
  }

  object Exists extends Reporter {
    override def getSyntax = reporterSyntax(right = List(StringType), ret = BooleanType)
    override def report(args: Array[Argument], context: Context) = toExtensionException {
      Boolean.box(fm(context).fileExists(fm(context).attachPrefix(args(0).getString)))
    }
  }

  object Flush extends Command {
    override def getSyntax = commandSyntax()
    override def perform(args: Array[Argument], context: Context) = toExtensionException {
      if (fm(context).hasCurrentFile) fm(context).flushCurrentFile()
    }
  }

  object Open extends Command {
    override def getSyntax = commandSyntax(right = List(StringType))
    override def perform(args: Array[Argument], context: Context) = toExtensionException {
      fm(context).openFile(args(0).getString)
    }
  }

  object Print extends Command {
    override def getSyntax = commandSyntax(right = List(WildcardType))
    override def perform(args: Array[Argument], context: Context) = toExtensionException {
      fm(context).ensureMode(FileMode.Append)
      context.workspace.outputObject(args(0).get, null, true, false, OutputDestination.File)
    }
  }

  object Read extends Reporter {
    override def getSyntax = reporterSyntax(ret = ReadableType)
    override def report(args: Array[Argument], context: Context) = toExtensionException {
      fm(context).read(context.world.asInstanceOf[org.nlogo.agent.World])
    }
  }

  object ReadChars extends Reporter {
    override def getSyntax = reporterSyntax(right = List(NumberType), ret = StringType)
    override def report(args: Array[Argument], context: Context) = toExtensionException {
      fm(context).readChars(args(0).getIntValue)
    }
  }

  object ReadLine extends Reporter {
    override def getSyntax = reporterSyntax(ret = StringType)
    override def report(args: Array[Argument], context: Context) = toExtensionException {
      fm(context).readLine()
    }
  }

  object Show extends Command {
    override def getSyntax = commandSyntax(right = List(WildcardType))
    override def perform(args: Array[Argument], context: Context) = toExtensionException {
      fm(context).ensureMode(FileMode.Append)
      context.workspace.outputObject(args(0).get, context.getAgent, true, true, OutputDestination.File)
    }
  }

  object Type extends Command {
    override def getSyntax = commandSyntax(right = List(WildcardType))
    override def perform(args: Array[Argument], context: Context) = toExtensionException {
      fm(context).ensureMode(FileMode.Append)
      context.workspace.outputObject(args(0).get, null, false, false, OutputDestination.File)
    }
  }

  object Write extends Command {
    override def getSyntax = commandSyntax(right = List(ReadableType))
    override def perform(args: Array[Argument], context: Context) = toExtensionException {
      fm(context).ensureMode(FileMode.Append)
      context.workspace.outputObject(args(0).get, null, false, true, OutputDestination.File)
    }
  }
}
