package pl.edu.mimuw.forum.ui.models;

import java.util.stream.Collectors;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import memento.NodeMemento;
import memento.NodeChange;
import memento.TextFieldChange;
import pl.edu.mimuw.forum.data.Node;
import pl.edu.mimuw.forum.ui.controllers.DetailsPaneController;

public class NodeViewModel {

    public static final String NAME = "";

    final private StringProperty authorProperty;
    final private StringProperty contentProperty;
    final private ListProperty<NodeViewModel> childrenProperty;
    private NodeMemento history;

    public NodeViewModel(String content, String author) {
        this(new Node(content, author));
    }

    public NodeViewModel(Node node) {
        authorProperty = new SimpleStringProperty(node.getAuthor());

        contentProperty = new SimpleStringProperty(node.getContent());

        childrenProperty = new SimpleListProperty<NodeViewModel>(FXCollections.observableArrayList(node.getChildren() != null
                ? node.getChildren().stream().map(Node::getModel).collect(Collectors.toList()).toArray(new NodeViewModel[0])
                : new NodeViewModel[0]));

        authorProperty.addListener((observable, oldValue, newValue) -> {
            history.change(new TextFieldChange(authorProperty, oldValue, newValue));
            System.out.println("Changing user name: " + oldValue + "->" + newValue);
        });

        contentProperty.addListener((observable, oldValue, newValue) -> {
            history.change(new TextFieldChange(contentProperty, oldValue, newValue));
            System.out.println("Changing comment: " + oldValue + "->" + newValue);
        });

    }

    public StringProperty getAuthor() {
        return authorProperty;
    }

    public StringProperty getContent() {
        return contentProperty;
    }

    public void setHistory(NodeMemento history) {
        this.history = history;
    }

    public ListProperty<NodeViewModel> getChildren() {
        return childrenProperty;
    }

    public Node toNode() {
        Node w = createDocument();
        childrenProperty.forEach(childModel -> w.addChild(childModel.toNode()));
        return w;
    }

    protected Node createDocument() {
        return new Node(contentProperty.get(), authorProperty.get());
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public String getName() {
        return NAME;
    }

    public void presentOn(DetailsPaneController detailsPaneController) {
        detailsPaneController.present(this);
    }

}
