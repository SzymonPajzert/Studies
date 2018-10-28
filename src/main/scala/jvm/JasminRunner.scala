package jvm

import java.io._



object JasminRunner {
    type StdOut = String
    type StdErr = String
    
    case class CommandResult(success: Boolean, stdout: StdOut, stderr: StdErr)
    
    // Simple interface to the run of jasmin.jar on a file
    // Saves to output file
    def compile(assemblyCode: String, outputFile: File): CommandResult = {
        (false, "", "")
    }
    
    def runJava(javaFile: File): CommandResult = {
        val rt = Runtime.getRuntime();
        val pr = rt.exec("java -jar map.jar time.rel test.txt debug");
    }
}