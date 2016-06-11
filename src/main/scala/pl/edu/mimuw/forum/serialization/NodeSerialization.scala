package pl.edu.mimuw.forum.serialization

import java.io._

import pl.edu.mimuw.forum.data.Node
import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.xml.DomDriver

object NodeSerialization {
	private val xstream: XStream = new XStream(new DomDriver("Unicode"))
	xstream.addImplicitCollection(classOf[Node], "children")

	def open(file: File): Node = {
		val rdr: Reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))
		val in: ObjectInputStream = xstream.createObjectInputStream(rdr)
		val result: Node = in.readObject().asInstanceOf[Node]
		in.close()
		result
	}

	def save(node: Node, file: File) = {
		val pw: PrintWriter = new PrintWriter(file, "UTF-8")
		val out: ObjectOutputStream = xstream.createObjectOutputStream(pw, "Forum")
		out.writeObject(node)
		out.close()
	}
}