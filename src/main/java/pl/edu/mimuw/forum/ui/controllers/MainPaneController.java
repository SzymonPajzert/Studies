package pl.edu.mimuw.forum.ui.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import pl.edu.mimuw.forum.memento.AddChildChange;
import pl.edu.mimuw.forum.memento.NodeMemento;
import pl.edu.mimuw.forum.memento.RemoveChildChange;
import pl.edu.mimuw.forum.exceptions.ApplicationException;
import pl.edu.mimuw.forum.ui.bindings.MainPaneBindings;
import pl.edu.mimuw.forum.ui.helpers.DialogHelper;
import pl.edu.mimuw.forum.ui.models.NodeViewModel;
import pl.edu.mimuw.forum.ui.tree.ForumTreeItem;
import pl.edu.mimuw.forum.ui.tree.TreeLabel;
import pl.edu.mimuw.forum.serialization.NodeSerialization;

public class MainPaneController implements Initializable {

    private NodeMemento history;

    private NodeViewModel document;

    private MainPaneBindings bindings;

    @FXML
    private TreeView<NodeViewModel> treePane;

    @FXML
    private DetailsPaneController detailsController;

    public MainPaneController() {
        bindings = new MainPaneBindings();
        history = new NodeMemento(document, bindings.undoAvailableProperty(), bindings.redoAvailableProperty(), bindings.hasChangesProperty());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BooleanBinding nodeSelectedBinding = Bindings.isNotNull(treePane.getSelectionModel().selectedItemProperty());
        bindings.nodeAdditionAvailableProperty().bind(nodeSelectedBinding);
        bindings.nodeRemovaleAvailableProperty()
                .bind(nodeSelectedBinding.and(
                        Bindings.createBooleanBinding(() -> getCurrentTreeItem().orElse(null) != treePane.getRoot(),
                                treePane.rootProperty(), nodeSelectedBinding)));

        bindings.hasChangesProperty().set(false);
    }

    public MainPaneBindings getPaneBindings() {
        return bindings;
    }

    public Node open(File file) throws ApplicationException {
        if (file != null) {
            document = NodeSerialization.open(file).getModel();
        } else {
            document = new NodeViewModel("Welcome to a new forum", "Admin");
        }

        getPaneBindings().fileProperty().set(file);
        document.setHistory(history);
        return openInView(document);
    }

    public void save() throws ApplicationException {
        if (document != null) {
            System.out.println("On save " + document.toNode());    //Tak tworzymy drzewo do zapisu z modelu aplikacji
            NodeSerialization.save(document.toNode(), bindings.fileProperty().get());
        }
    }

    public void undo() throws ApplicationException {
        System.out.println("On undo");
        history.undo();
    }

    public void redo() throws ApplicationException {
        System.out.println("On redo");
        history.redo();

    }

    public void addNode(NodeViewModel node) throws ApplicationException {
        getCurrentNode().ifPresent(currentlySelected -> {
            currentlySelected.getChildren().add(node);        // Zmieniamy jedynie model, widok (TreeView) jest aktualizowany z poziomu
            // funkcji nasluchujacej na zmiany w modelu (zob. metode createViewNode ponizej)
        });
    }

    public void deleteNode() {
        getCurrentTreeItem().ifPresent(currentlySelectedItem -> {
            TreeItem<NodeViewModel> parent = currentlySelectedItem.getParent();

            NodeViewModel parentModel;
            NodeViewModel currentModel = currentlySelectedItem.getValue();
            if (parent == null) {
                return; // Blokujemy usuniecie korzenia - TreeView bez korzenia jest niewygodne w obsludze
            } else {
                parentModel = parent.getValue();
                parentModel.getChildren().remove(currentModel); // Zmieniamy jedynie model, widok (TreeView) jest aktualizowany z poziomu
                // funkcji nasluchujacej na zmiany w modelu (zob. metode createViewNode ponizej)
            }

        });
    }

    private Node openInView(NodeViewModel document) throws ApplicationException {
        Node view = loadFXML();

        treePane.setCellFactory(tv -> {
            try {
                return new TreeLabel();
            } catch (ApplicationException e) {
                DialogHelper.ShowError("Error creating a tree cell.", e);
                return null;
            }
        });

        ForumTreeItem root = createViewNode(document);
        root.addEventHandler(TreeItem.<NodeViewModel>childrenModificationEvent(), event -> {
            if (event.wasAdded()) {
                System.out.println("Adding to " + event.getSource());
            }

            if (event.wasRemoved()) {
                System.out.println("Removing from " + event.getSource());
            }
        });

        treePane.setRoot(root);

        for (NodeViewModel w : document.getChildren()) {
            addToTree(w, root);
        }

        expandAll(root);

        treePane.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> onItemSelected(oldValue, newValue));

        return view;
    }

    private Node loadFXML() throws ApplicationException {
        FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        loader.setLocation(getClass().getResource("/fxml/main_pane.fxml"));

        try {
            return loader.load();
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private Optional<? extends NodeViewModel> getCurrentNode() {
        return getCurrentTreeItem().<NodeViewModel>map(TreeItem::getValue);
    }

    private Optional<TreeItem<NodeViewModel>> getCurrentTreeItem() {
        return Optional.ofNullable(treePane.getSelectionModel().getSelectedItem());
    }

    private void addToTree(NodeViewModel node, ForumTreeItem parentViewNode, int position) {
        /* Set history tracking of newly added node */
        node.setHistory(history);
        ForumTreeItem viewNode = createViewNode(node);

        List<TreeItem<NodeViewModel>> siblings = parentViewNode.getChildren();
        siblings.add(position == -1 ? siblings.size() : position, viewNode);

        node.getChildren().forEach(child -> addToTree(child, viewNode));
    }

    private void addToTree(NodeViewModel node, ForumTreeItem parentViewNode) {
        addToTree(node, parentViewNode, -1);
    }

    private void removeFromTree(ForumTreeItem viewNode) {
        viewNode.removeChildListener();
        TreeItem<NodeViewModel> parent = viewNode.getParent();
        if (parent != null) {
            viewNode.getParent().getChildren().remove(viewNode);
        } else {
            treePane.setRoot(null);
        }
    }

    private void removeFromTree(ForumTreeItem viewNode, int position) {
        removeFromTree((ForumTreeItem) viewNode.getChildren().get(position));
    }


    private ForumTreeItem createViewNode(NodeViewModel node) {
        ForumTreeItem viewNode = new ForumTreeItem(node);
        viewNode.setChildListener(change -> {    // wywolywanem, gdy w modelu dla tego wezla zmieni sie zawartosc kolekcji dzieci
            while (change.next()) {
                if (change.wasAdded()) {
                    int i = change.getFrom();
                    for (NodeViewModel child : change.getAddedSubList()) {
                        history.change(new AddChildChange(child, node, i));
                        addToTree(child, viewNode, i);    // uwzgledniamy nowy wezel modelu w widoku
                        i++;
                    }
                }

                if (change.wasRemoved()) {
                    for (int i = change.getFrom(); i <= change.getTo(); ++i) {
                        NodeViewModel removedChild = viewNode.getChildren().get(i).getValue();
                        history.change(new RemoveChildChange(removedChild, node, i));
                        removeFromTree(viewNode, i); // usuwamy wezel modelu z widoku
                    }
                }
            }
        });

        return viewNode;
    }

    private void expandAll(TreeItem<NodeViewModel> item) {
        item.setExpanded(true);
        item.getChildren().forEach(this::expandAll);
    }

    private void onItemSelected(TreeItem<NodeViewModel> oldItem, TreeItem<NodeViewModel> newItem) {
        detailsController.setModel(newItem != null ? newItem.getValue() : null);
    }

}
