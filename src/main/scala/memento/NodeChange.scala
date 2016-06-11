package memento

import javafx.beans.property.StringProperty
import javafx.scene.control.{TextArea, TextField}

import pl.edu.mimuw.forum.ui.models.NodeViewModel

sealed trait NodeChange {
	def getInverse: NodeChange = this match {
		case TextFieldChange(field, oldval, newval) => TextFieldChange(field, newval, oldval)
		case NodeStructureChange(oldnode, newnode) => NodeStructureChange(newnode, oldnode)
	}

	def change(): Unit
}

case class TextFieldChange(field: StringProperty, oldval: String, newval: String) extends NodeChange {
	override def change(): Unit = field.setValue(newval)
}

case class NodeStructureChange(oldnode: NodeViewModel, newnode: NodeViewModel) extends NodeChange {
	override def change(): Unit = ???
}