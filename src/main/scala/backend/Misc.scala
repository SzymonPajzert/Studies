package backend

import java.io.{BufferedReader, BufferedWriter, File, FileWriter, InputStreamReader, _}
import java.nio.file.Paths
import java.util.Scanner
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import java.util.function.Consumer

import scala.util.Random

private class StreamGobbler(val inputStream: InputStream,
                            val consumers: Consumer[String]*) extends Runnable {
  override def run(): Unit = {
    val singleConsumer = consumers.reduce((consumerA, consumerB) => consumerA.andThen(consumerB))

    new BufferedReader(new InputStreamReader(inputStream)).lines.forEach(singleConsumer)
  }
}

object FileUtil {
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

  def existsTestFile(filename: String): Boolean = {
    val file = new File("/home/svp/Programming/mrjp/src/test/resources/" + filename)
    file.exists
  }

  def readTestFile(filename: String): String = {
    val file = new File("/home/svp/Programming/mrjp/src/test/resources/" + filename)
    readFile(file)
  }

  def runCommand(javaCommand: String, directory: OutputDirectory, maybeWorkingDir: Option[File] = None): CommandResult[String] = {
    val process = maybeWorkingDir match {
      case None =>
        Runtime.getRuntime.exec(javaCommand)
      case Some(workingDir) =>
        Runtime.getRuntime.exec(javaCommand, Array[String](), workingDir)
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
    val exitCode = process.waitFor(10, TimeUnit.SECONDS)

    executor.shutdown()
    executor.awaitTermination(10, TimeUnit.SECONDS)

    CommandResult(exitCode, stdout.toString, stderr.toString)
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
  private def subfile(filename: String): File = {
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
