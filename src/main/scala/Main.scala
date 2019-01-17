import java.io.File

import backend.Directory
import backend.llvm.LlvmRunner
import compiler._
import language.LLVM
import parser.latte.LatteParser

object Main extends App {

  val directory = Directory.fromFile(new File(args(1)))

  val saveLLVM = new Compiler[LLVM.Code, Unit] {
    def compile(code: LLVM.Code): Either[CompileException, Unit] = {
      println("Compiling LLVM")
      Right(LlvmRunner.compile(code, directory))
    }
  }

  val compilers = args(0) match {
    case "llvm" =>
      LatteParser ~>
        ParseClasses ~>
        TypePhase ~>
        CheckReturns ~>
        UntypingPhase ~>
        LatteToQuadCode ~>
        NormalizeQuadSubblocks ~>
        saveLLVM
  }

  compilers compile directory
}
