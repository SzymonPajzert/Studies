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
import pl.edu.mimuw.forum.example.Dummy;
import pl.edu.mimuw.forum.exceptions.ApplicationException;
import pl.edu.mimuw.forum.ui.bindings.MainPaneBindings;
import pl.edu.mimuw.forum.ui.helpers.DialogHelper;
import pl.edu.mimuw.forum.ui.models.NodeViewModel;
import pl.edu.mimuw.forum.ui.tree.ForumTreeItem;
import pl.edu.mimuw.forum.ui.tree.TreeLabel;

public class MainPaneController implements Initializable {

    private NodeViewModel document;

    private MainPaneBindings bindings;

    @FXML
    private TreeView<NodeViewModel> treePane;

    @FXML
    private DetailsPaneController detailsController;

    public MainPaneController() {
        bindings = new MainPaneBindings();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BooleanBinding nodeSelectedBinding = Bindings.isNotNull(treePane.getSelectionModel().selectedItemProperty());
        bindings.nodeAdditionAvailableProperty().bind(nodeSelectedBinding);
        bindings.nodeRemovaleAvailableProperty()
                .bind(nodeSelectedBinding.and(
                        Bindings.createBooleanBinding(() -> getCurrentTreeItem().orElse(null) != treePane.getRoot(),
                                treePane.rootProperty(), nodeSelectedBinding)));

        bindings.hasChangesProperty().set(true);        // TODO Nalezy ustawic na true w przypadku, gdy w widoku sa zmiany do
        // zapisania i false wpp, w odpowiednim miejscu kontrolera (niekoniecznie tutaj)
        // Spowoduje to dodanie badz usuniecie znaku '*' z tytulu zakladki w ktorej
        // otwarty jest plik - '*' oznacza niezapisane zmiany
        bindings.undoAvailableProperty().set(true);
        bindings.redoAvailableProperty().set(true);        // Podobnie z undo i redo
    }

    public MainPaneBindings getPaneBindings() {
        return bindings;
    }

    public Node open(File file) throws ApplicationException {
        if (file != null) {
            // TODO Tutaj dodaj obsluge otwierania forum z pliku
            // Tymczasem tworzone jest przykladowe drzewo w pamieci
            document = Dummy.Create().getModel();
        } else {
            document = new NodeViewModel("Welcome to a new forum", "Admin");
        }

        getPaneBindings().fileProperty().set(file);

        return openInView(document);
    }

    public void save() throws ApplicationException {
        //TODO Tutaj umiescic wywolanie obslugi zapisu drzewa z widoku do pliku
        if (document != null) {
            System.out.println("On save " + document.toNode());    //Tak tworzymy drzewo do zapisu z modelu aplikacji
        }
    }

    public void undo() throws ApplicationException {
        System.out.println("On undo");    //TODO Tutaj umiescic obsluge undo
    }

    public void redo() throws ApplicationException {
        System.out.println("On redo");    //TODO Tutaj umiescic obsluge redo
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
            //TODO Moze przydac sie do wykrywania usuwania/dodawania wezlow w drzewie (widoku)
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

    private ForumTreeItem createViewNode(NodeViewModel node) {
        ForumTreeItem viewNode = new ForumTreeItem(node);
        viewNode.setChildListener(change -> {    // wywolywanem, gdy w modelu dla tego wezla zmieni sie zawartosc kolekcji dzieci
            while (change.next()) {
                if (change.wasAdded()) {
                    int i = change.getFrom();
                    for (NodeViewModel child : change.getAddedSubList()) {
                        // TODO Tutaj byc moze nalezy dodac zapisywanie jaka operacja jest wykonywana
                        // by mozna bylo ja odtworzyc przy undo/redo
                        addToTree(child, viewNode, i);    // uwzgledniamy nowy wezel modelu w widoku
                        i++;
                    }
                }

                if (change.wasRemoved()) {
                    for (int i = change.getFrom(); i <= change.getTo(); ++i) {
                        // TODO Tutaj byc moze nalezy dodac zapisywanie jaka operacja jest wykonywana
                        // by mozna bylo ja odtworzyc przy undo/redo
                        removeFromTree((ForumTreeItem) viewNode.getChildren().get(i)); // usuwamy wezel modelu z widoku
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
