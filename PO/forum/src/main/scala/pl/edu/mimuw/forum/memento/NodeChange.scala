package pl.edu.mimuw.forum.memento

import javafx.beans.property.Property
import javafx.beans.value.{ChangeListener, ObservableValue}
import pl.edu.mimuw.forum.ui.models.NodeViewModel

/* Trait of available operations on the node */
/* This trait is basically group acting on the set of nodes */

object NodeChangeUtilities {
	def setListener[A](property: Property[A], model: NodeViewModel): Unit = {
		property addListener new ChangeListener[A] {
			override def changed(observable: ObservableValue[_ <: A], oldValue: A, newValue: A): Unit = {
				model.getHistory.change(FieldChange[A](property, oldValue, newValue))
			}
		}
	}
}

sealed trait NodeChange {
	/* Returns inverse operation */
	def getInverse: NodeChange = this match {
		case FieldChange(field, oldval, newval) => FieldChange(field, newval, oldval)
		case RemoveChildChange(node, parentViewNode, position) => AddChildChange(node, parentViewNode, position)
		case AddChildChange(node, parentViewNode, position) => RemoveChildChange(node, parentViewNode, position)
	}

	/* Performs change described by object on connected object */
	def change(): Unit
}

/* Change of an*/
case class FieldChange[A](field: Property[A], oldval: A, newval: A) extends NodeChange {
	def change() = field.setValue(newval)
}

case class RemoveChildChange(removedChild: NodeViewModel, node: NodeViewModel, position: Int) extends NodeChange {
	def change() = node.getChildren.remove(position)
}

case class AddChildChange(child: NodeViewModel, node: NodeViewModel, position: Int) extends NodeChange {
	def change() = node.getChildren.add(position, child)
}