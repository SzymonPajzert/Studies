package pl.edu.mimuw.forum.ui.controllers

import java.util.Date
import java.net.URL
import java.util.ResourceBundle
import javafx.beans.property.BooleanProperty
import javafx.fxml.{FXML, Initializable}
import javafx.scene.Scene
import javafx.scene.control._
import javafx.stage.Stage
import javafx.util.Callback

import pl.edu.mimuw.forum.ui.models._

class DialogController extends Initializable {
	@FXML var dialog: Dialog[NodeViewModel] = _
	@FXML var userField: TextField = _
	@FXML var commentField: TextArea = _
	@FXML var group: ToggleGroup = _

	override def initialize(location: URL, resources: ResourceBundle): Unit = {
		dialog.getDialogPane.getButtonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
		group.getToggles.get(0).setSelected(true)

		/* Instance of anonymous class extending Callback */
		dialog.setResultConverter {
			new Callback[ButtonType, NodeViewModel] {
				def createFromButton(button: AnyRef) = {
					val content: String = commentField.getText
					val name: String = userField.getText
					button match {
						case "Comment" => new CommentViewModel(content, name)
						case "Suggestion" => new SuggestionViewModel(content, name, "")
						case "Survey" => new SurveyViewModel(content, name)
						case "Task" => new TaskViewModel(content, name, new Date())
					}
				}

				def call(button: ButtonType) = {
					val selected = group.getSelectedToggle.asInstanceOf[RadioButton]
					if (selected != null) {
						val buttonName = selected.getText
						if (button == ButtonType.OK) createFromButton(buttonName) else null
					} else null
				}
			}
		}
	}
}
