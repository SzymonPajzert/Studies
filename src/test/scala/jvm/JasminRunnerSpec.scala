package jvm

import java.io._

import org.scalatest._

class JasminRunnerSpec extends FlatSpec with Matchers {
    behavior of "JasminRunner"
    
    val validCode = """
    |.class  public Hello
    |.super  java/lang/Object
    |
    |; standard initializer
    |.method public <init>()V
    |  aload_0
    |  invokespecial java/lang/Object/<init>()V
    |  return
    |.end method
    |
    |.method public static main([Ljava/lang/String;)V
    |.limit stack 2
    |  getstatic  java/lang/System/out Ljava/io/PrintStream;
    |  ldc "Hello"
    |  invokevirtual  java/io/PrintStream/println(Ljava/lang/String;)V
    |  return
    |.end method
    """.stripMargin
    
    it should "compileWithoutErrors" in {
        val mockFile = File.createTempFile("source", "j")
        
        val result = JasminRunner.compile(validCode, mockFile)
        
        val array = new Array[Char]
        assert(!(new FileReader(mockFile).read(array)))
    }
}