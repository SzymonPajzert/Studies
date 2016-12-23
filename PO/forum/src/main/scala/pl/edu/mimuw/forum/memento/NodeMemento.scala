package pl.edu.mimuw.forum.memento

import javafx.beans.property.BooleanProperty
import pl.edu.mimuw.forum.ui.models.NodeViewModel

class NodeMemento(private val node: NodeViewModel,
				  private val canUndo: BooleanProperty,
				  private val canRedo: BooleanProperty,
				  private val changes: BooleanProperty) {

	/* Stack of previous operations */
	private var undoHistory: List[NodeChange] = Nil

	/* Stack of reverted operations, cleared if new operation is introduced */
	private var redoHistory: List[NodeChange] = Nil

	/* Flag set to false if undo or redo operation is executed, to make change not listed */
	private var makeChange = true

	updateProperties()

	def getNode: NodeViewModel = node

	private def updateProperties(): Unit = {
		changes.setValue(undoHistory.nonEmpty)
		canUndo.setValue(undoHistory.nonEmpty)
		canRedo.setValue(redoHistory.nonEmpty)
	}


	def undo(): Unit = if (makeChange) {
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
		undoHistory = newChange.getInverse :: undoHistory
		redoHistory = Nil
		updateProperties()
	} else makeChange = true
}