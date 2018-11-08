import java.io.File

import backend.Directory
import backend.jvm.{JVMOp, JasminRunner}
import backend.llvm.{LLVM, LlvmRunner}
import compiler.{Compiler, DirectoryToInstant, InstantToLlvm, InstantToStackOp, StackOpToJVM}

object Main extends App {

  val directory = Directory.fromFile(new File(args(1)))

  val saveJVM = new Compiler[JVMOp.Code, Unit] {
    def compile(code: JVMOp.Code): Unit = {
      println("Compiling JVM")
      JasminRunner.compile(JVMOp.createMain(directory.filenameTrim, code), directory)
    }
  }

  val saveLLVM = new Compiler[LLVM.Code, Unit] {
    def compile(code: LLVM.Code): Unit = {
      println("Compiling LLVM")
      LlvmRunner.compile(code, directory)
    }
  }

  val compiler = args(0) match {
    case "jvm" => DirectoryToInstant ~> InstantToStackOp ~> StackOpToJVM ~> saveJVM
    case "llvm" => DirectoryToInstant ~> InstantToLlvm ~> saveLLVM
  }

  compiler compile directory
}
