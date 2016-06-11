package memento

import javafx.beans.property.StringProperty
import pl.edu.mimuw.forum.ui.models.NodeViewModel

sealed trait NodeChange {
	def getInverse: NodeChange = this match {
		case TextFieldChange(field, oldval, newval) => TextFieldChange(field, newval, oldval)
		case RemoveChildChange(node, parentViewNode, position) => AddChildChange(node, parentViewNode, position)
		case AddChildChange(node, parentViewNode, position) => RemoveChildChange(node, parentViewNode, position)
	}

	def change(): Unit
}

case class TextFieldChange(field: StringProperty, oldval: String, newval: String) extends NodeChange {
	def change() = field.setValue(newval)
}

case class RemoveChildChange(removedChild: NodeViewModel, node: NodeViewModel, position: Int) extends NodeChange {
	def change() = node.getChildren.remove(position)
}

case class AddChildChange(child: NodeViewModel, node: NodeViewModel, position: Int) extends NodeChange {
	def change() = node.getChildren.add(position, child)
}