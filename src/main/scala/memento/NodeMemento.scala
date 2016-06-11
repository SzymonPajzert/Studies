package memento

import javafx.beans.property.BooleanProperty
import pl.edu.mimuw.forum.ui.models.NodeViewModel

class NodeMemento(private val node: NodeViewModel,
				  private val canUndo: BooleanProperty,
				  private val canRedo: BooleanProperty) {

	private var undoHistory: List[NodeChange] = Nil
	private var redoHistory: List[NodeChange] = Nil
	private var makeChange = true

	updateProperties()

	def getNode: NodeViewModel = node

	private def updateProperties(): Unit = {
		canUndo.setValue(undoHistory.nonEmpty)
		canRedo.setValue(redoHistory.nonEmpty)
	}

	def doEveryOther(changeValue: Boolean)(expr: => Unit): Unit = {
		if (makeChange) {
			expr
			makeChange = changeValue
		}
		else {
			makeChange = true
		}
	}

	def undo(): Unit = if (makeChange) {
		println("undo called")
		undoHistory match {
			case Nil => ()
			case curChange :: restUndoHistory =>
				undoHistory = restUndoHistory
				redoHistory = curChange.getInverse :: redoHistory
				makeChange = false
				curChange.change()
				updateProperties()
		}
	} else makeChange = true

	def redo(): Unit = if (makeChange) {
		redoHistory match {
			case Nil => ()
			case curChange :: restRedoHistory =>
				redoHistory = restRedoHistory
				undoHistory = curChange.getInverse :: undoHistory
				makeChange = false
				curChange.change()
				updateProperties()
		}
	} else makeChange = true

	def change(newChange: NodeChange): Unit = if (makeChange) {
		println("change called")
		undoHistory = newChange.getInverse :: undoHistory
		redoHistory = Nil
		updateProperties()
	} else makeChange = true
}