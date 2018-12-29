import java.io.File

import backend.Directory
import backend.jvm.{JVMOp, JasminRunner}
import backend.llvm.LlvmRunner
import compiler._
import language.LLVM
import parser.instant.InstantParser
import parser.latte.LatteParser

object Main extends App {

  val directory = Directory.fromFile(new File(args(1)))

  val saveJVM = new Compiler[JVMOp.Code, Unit] {
    def compile(code: JVMOp.Code): Either[List[CompileException], Unit] = {
      println("Compiling JVM")
      Right(JasminRunner.compile(JVMOp.createMain(directory.filenameTrim, code), directory))
    }
  }

  val saveLLVM = new Compiler[LLVM.Code, Unit] {
    def compile(code: LLVM.Code): Either[List[CompileException], Unit] = {
      println("Compiling LLVM")
      Right(LlvmRunner.compile(code, directory))
    }
  }

  val compilers = args(0) match {
    case "jvm" => InstantParser ~> InstantToStackOp ~> StackOpToJVM ~> saveJVM
    case "llvm" => LatteParser ~> LatteToQuadCode ~> saveLLVM
  }

  compilers compile directory
}
