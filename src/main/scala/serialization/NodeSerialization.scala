package serialization

import java.io._
import java.text.SimpleDateFormat
import java.util.Date

import pl.edu.mimuw.forum.data.{Comment, Node, Suggestion, Survey, Task}
import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.xml.DomDriver
import com.thoughtworks.xstream.converters.{Converter, MarshallingContext, UnmarshallingContext}
import com.thoughtworks.xstream.io.{HierarchicalStreamReader, HierarchicalStreamWriter}

import scala.collection.mutable
import scala.collection.JavaConversions._

object NodeConverter extends Converter {
	override def marshal(o: scala.Any, hierarchicalStreamWriter: HierarchicalStreamWriter,
						 marshallingContext: MarshallingContext): Unit = {
		val marshalledObject = o.asInstanceOf[Node]

		hierarchicalStreamWriter.startNode("content")
		hierarchicalStreamWriter.setValue(marshalledObject.getContent)
		hierarchicalStreamWriter.endNode()

		hierarchicalStreamWriter.startNode("author")
		hierarchicalStreamWriter.setValue(marshalledObject.getAuthor)
		hierarchicalStreamWriter.endNode()

		for (child <- marshalledObject.getChildren) {
			hierarchicalStreamWriter.startNode(child.getClass.getName)
			marshal(child, hierarchicalStreamWriter, marshallingContext)
			hierarchicalStreamWriter.endNode()
		}
	}

	private val dateFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS zzz")
	private def convertDate:(String => Date) = dateFormat.parse

	override def unmarshal(reader: HierarchicalStreamReader,
						   context: UnmarshallingContext): AnyRef = {
		var content = ""
		var author = ""
		var response = ""
		var likes = 0
		var dislikes = 0
		var dueDate: Date = null
		val children = new mutable.Queue[Node]

		while (reader.hasMoreChildren) {
			reader.moveDown()
			reader.getNodeName match {
				case "content" => content = reader.getValue
				case "author" => author = reader.getValue
				case "response" => response = reader.getValue
				case "dueDate" => dueDate = convertDate(reader.getValue)
				case "likes" => likes = reader.getValue.toInt
				case "dislikes" => dislikes = reader.getValue.toInt
				case "pl.edu.mimuw.forum.data.Comment" => children += unmarshal(reader, context).asInstanceOf[Comment]
				case "pl.edu.mimuw.forum.data.Suggestion" => children += unmarshal(reader, context).asInstanceOf[Suggestion]
				case "pl.edu.mimuw.forum.data.Survey" => children += unmarshal(reader, context).asInstanceOf[Survey]
				case "pl.edu.mimuw.forum.data.Task" => children += unmarshal(reader, context).asInstanceOf[Task]
				case "pl.edu.mimuw.forum.data.Node" => children += unmarshal(reader, context).asInstanceOf[Node]
			}
			reader.moveUp()
		}

		val result = reader.getNodeName match {
			case "pl.edu.mimuw.forum.data.Comment" => new Comment(content, author)
			case "pl.edu.mimuw.forum.data.Suggestion" => new Suggestion(content, author, response)
			case "pl.edu.mimuw.forum.data.Survey" => new Survey(content, author, likes, dislikes)
			case "pl.edu.mimuw.forum.data.Task" => new Task(dueDate, content, author)
			case "pl.edu.mimuw.forum.data.Node" => new Node(content, author)
		}

		for ( child <- children ) {
			result.addChild(child)
		}

		result
	}

	override def canConvert(aClass: Class[_]): Boolean = classOf[Node] isAssignableFrom aClass
}

object NodeSerialization {
	private val xstream: XStream = new XStream(new DomDriver("Unicode"))
	xstream.registerConverter(NodeConverter)

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