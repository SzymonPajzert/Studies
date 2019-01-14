// reviewed: 2018.12.29

package backend

import java.io.{BufferedReader, BufferedWriter, File, FileWriter, InputStreamReader, _}
import java.nio.file.Paths
import java.util.Scanner
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import java.util.function.Consumer

import scala.util.Random

/**
  * Processes output by given stream to consumers
  * @param inputStream Source of the data
  * @param consumers Consumers of the data
  */
private class StreamGobbler(val inputStream: InputStream,
                            val consumers: Consumer[String]*) extends Runnable {
  override def run(): Unit = {
    val singleConsumer = consumers.reduce((consumerA, consumerB) => consumerA.andThen(consumerB))

    new BufferedReader(new InputStreamReader(inputStream)).lines.forEach(singleConsumer)
  }
}

object FileUtil {
  object Interactive {
    def saveToFile[T](content: String, file: File)
                     (implicit runner: Runner): Unit = {
      if(runner.lastFailed.isEmpty) {
        FileUtil.saveToFile(content, file)
      }
    }

    def runCommand[T](command: String, directory: OutputDirectory, maybeWorkingDir: Option[File] = None)
                     (implicit runner: Runner): Unit = {
      if(runner.lastFailed.isEmpty) {
        val result = FileUtil.runCommand(command, directory, maybeWorkingDir)
        if(!result.success) {
          runner.lastFailed = Some(result)
        }
      }
    }

  }

  class Runner(var lastFailed: Option[CommandResult[String]]) {
    def finish: CommandResult[String] = lastFailed match {
      case None => CommandResult[String](true, null, "")
      case Some(a) => a
    }

  }

  def commandRunner: Runner = new Runner(None)


  def mkdirRecur(directory: File): Boolean = {
    if (!directory.exists) {
      mkdirRecur(directory.getParentFile) && directory.mkdir()
    } else true
  }

  def saveToFile(content: String, file: File): Unit = {
    assert(mkdirRecur(file.getParentFile), s"failed to create: ${file}")
    val writer: BufferedWriter = new BufferedWriter(new FileWriter(file))
    writer.write(content)
    writer.close()
  }

  def readFile(file: File): String = {
    val scanner = new Scanner(file)
    val result = new StringBuilder("")

    while (
      scanner.hasNextLine
    ) {
      val line = scanner.nextLine
      result.append(line).append("\n")
    }

    result.toString
  }

  def testFilesRoot: File = new File("/home/svp/Programming/mrjp/src/test/resources/")
  def testFile(filename: String): File = new File("/home/svp/Programming/mrjp/src/test/resources/" + filename)

  def runCommand(command: String, directory: OutputDirectory, maybeWorkingDir: Option[File] = None): CommandResult[String] = {
    val process = maybeWorkingDir match {
      case None =>
        Runtime.getRuntime.exec(command)
      case Some(workingDir) =>
        Runtime.getRuntime.exec(command, Array[String](), workingDir)
    }

    val stdout = new StringBuilder
    val stderr = new StringBuilder

    val stdoutGobbler = new StreamGobbler(process.getInputStream,
      (input) => stdout ++= "\n" + input)

    val errorGobbler = new StreamGobbler(process.getErrorStream,
      (input) => stderr ++= "\n" + input)

    val executor = new ScheduledThreadPoolExecutor(2)

    executor.execute(stdoutGobbler)
    executor.submit(errorGobbler)
    val terminated = process.waitFor(10, TimeUnit.SECONDS)
    val exitCode = process.waitFor()

    executor.shutdown()
    executor.awaitTermination(10, TimeUnit.SECONDS)

    CommandResult(terminated && exitCode == 0, stdout.toString, stderr.toString)
  }

  def parseOut(out: String): List[String] = {
    (for {
      line <- out.split(10.toChar)
      if line.nonEmpty
    } yield line
    ).toList
  }
}


object OutputDirectory {
  def create(directory: File): OutputDirectory = {
    directory.mkdir()
    new OutputDirectory(directory, "source")
  }

  val rand = new Random

  def createTemporary: OutputDirectory = {
    val directoryCounter = rand.nextInt()
    OutputDirectory.create(new File(s"/tmp/mrjp$directoryCounter"))
  }
}

class OutputDirectory(val directory: File, val filename: String) {
  def subfile(filename: String): File = {
    new File(Paths.get(directory.getCanonicalPath, filename).toString)
  }

  def filenameTrim: String = {
    filename.split("\\.")(0)
  }

  def jasminFile: File = subfile(s"$filenameTrim.j")
  def llvmFile: File = subfile(s"$filenameTrim.ll")
  def llvmTempExecutable: File = subfile("tmp.bc")
  def llvmExecutable: File = subfile(s"$filenameTrim.bc")
  def classFile: File = subfile(s"$filenameTrim.class")

  def path: File = directory.getAbsoluteFile

  def jarFile: File = {
    new File(directory.getCanonicalPath + ".jar")
  }

  /**
    * Creates directory which contains both sources and generated outputs
    *
    * @param sourceFilename
    * @param content
    * @return
    */
  def withSourceFile(sourceFilename: String, content: String): Directory = {
    val sourceFile = subfile(sourceFilename)

    FileUtil.saveToFile(content, sourceFile)

    new Directory(directory, filename, sourceFile)
  }
}

object Directory {
  def fromFile(file: File): Directory = new Directory(file.getParentFile, file.getName, file)
}

class Directory(override val directory: File,
                override val filename: String,
                val sourceFile: File) extends OutputDirectory(directory, filename) {
}
