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
	@FXML
	var dialog: Dialog[NodeViewModel] = null

	@FXML
	var userField: TextField = _

	@FXML
	var commentField: TextArea = _

	@FXML
	var group: ToggleGroup = _

	@FXML
	var commentButton: RadioButton = _

	@FXML
	var suggestionButton: RadioButton = _

	@FXML
	var surveyButton: RadioButton = _

	@FXML
	var taskButton: RadioButton = _

	override def initialize(location: URL, resources: ResourceBundle): Unit = {
		dialog.getDialogPane.getButtonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

		commentButton.setUserData("commentButton")
		suggestionButton.setUserData("suggestionButton")
		surveyButton.setUserData("surveyButton")
		taskButton.setUserData("taskButton")


		dialog.setResultConverter(new Callback[ButtonType, NodeViewModel] {
			val content: String = commentField.getPromptText
			val name: String = userField.getPromptText

			def createFromButton(button: AnyRef) = button match {
				case "commentButton" => new CommentViewModel(content, name)
				case "suggestionButton" => new SuggestionViewModel(content, name, "")
				case "surveyButton" => new SurveyViewModel(content, name)
				case "taskViewModel" => new TaskViewModel(content, name, new Date())
			}

			def call(button: ButtonType) = {
				val selected = group.getSelectedToggle
				if (selected != null) {
					val buttonName = selected.getUserData
					if (button == ButtonType.OK) createFromButton(buttonName) else null
				} else null
			}
		})
	}
}
