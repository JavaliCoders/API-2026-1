package api.controller;

import api.DAO.UsuarioDAO;
import api.model.Perfil;
import api.model.Usuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class usuarioController implements Initializable {

    @FXML private TextField searchCod;
    @FXML private TextField searchNome;
    @FXML private ComboBox<String> filtroStatus;

    @FXML private TableView<Usuario>            tabelaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String>  colNome;
    @FXML private TableColumn<Usuario, String>  colUsuario;
    @FXML private TableColumn<Usuario, String>  colEmail;
    @FXML private TableColumn<Usuario, String>  colPerfil;
    @FXML private TableColumn<Usuario, String>  colStatus;
    @FXML private TableColumn<Usuario, Void>    colAcoes;

    @FXML private StackPane overlayDetalhes;
    @FXML private Label     detalheNome;
    @FXML private Label     detalheCod;
    @FXML private Label     detalheUsuario;
    @FXML private Label     detalheEmail;
    @FXML private Label     detalhePerfil;
    @FXML private Label     detalheStatus;

    private AnchorPane areaPrincipal;
    private ObservableList<Usuario> todosUsuarios;
    private FilteredList<Usuario>   usuariosFiltrados;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        carregarDados();
        configurarBusca();
        configurarSelecao();
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    private void configurarFiltros() {
        filtroStatus.setItems(FXCollections.observableArrayList(
                "Todos os status", "ATIVO", "INATIVO"));
        filtroStatus.setValue("Todos os status");
    }

    private void configurarColunas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Perfil vem de um objeto aninhado, então usamos lambda
        colPerfil.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNomePerfil()));

        // Badge de Perfil
        colPerfil.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(100);
                badge.setStyle("-fx-background-color: #ede9fe; -fx-text-fill: #6d28d9; " +
                        "-fx-background-radius: 6; -fx-padding: 4 10;");
                setGraphic(badge);
                setText(null);
            }
        });

        // Badge de Status
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(70);
                badge.setStyle(item.equals("ATIVO")
                        ? "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-background-radius: 6; -fx-padding: 4 10;"
                        : "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 6; -fx-padding: 4 10;");
                setGraphic(badge);
                setText(null);
            }
        });

        // Botão de editar
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("✏");

            {
                btnEditar.setStyle(
                        "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; " +
                                "-fx-background-radius: 6; -fx-font-size: 14px; " +
                                "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;");

                btnEditar.setOnMouseEntered(e -> btnEditar.setStyle(
                        "-fx-background-color: #2563eb; -fx-text-fill: white; " +
                                "-fx-background-radius: 6; -fx-font-size: 14px; " +
                                "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;"));

                btnEditar.setOnMouseExited(e -> btnEditar.setStyle(
                        "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; " +
                                "-fx-background-radius: 6; -fx-font-size: 14px; " +
                                "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;"));

                btnEditar.setOnAction(e -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    abrirEdicao(u);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(btnEditar);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        // Zebra striping
        tabelaUsuarios.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: white;");
                } else {
                    setStyle(getIndex() % 2 == 0
                            ? "-fx-background-color: white;"
                            : "-fx-background-color: #fafafa;");
                }
            }
        });
    }

    private void configurarSelecao() {
        tabelaUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, novo) -> {
                    if (novo != null) exibirDetalhes(novo);
                });
    }

    private void exibirDetalhes(Usuario u) {
        detalheNome.setText(u.getNome());
        detalheCod.setText(String.valueOf(u.getIdUsuario()));
        detalheUsuario.setText(u.getUsuario());
        detalheEmail.setText(u.getEmail());
        detalhePerfil.setText(u.getNomePerfil());

        if (u.getStatus().equals("ATIVO")) {
            detalheStatus.setText("ATIVO");
            detalheStatus.setStyle(
                    "-fx-font-size: 12px; -fx-font-weight: bold; " +
                            "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; " +
                            "-fx-background-radius: 6; -fx-padding: 4 12;");
        } else {
            detalheStatus.setText("INATIVO");
            detalheStatus.setStyle(
                    "-fx-font-size: 12px; -fx-font-weight: bold; " +
                            "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; " +
                            "-fx-background-radius: 6; -fx-padding: 4 12;");
        }

        overlayDetalhes.setVisible(true);
        overlayDetalhes.setManaged(true);
    }

    @FXML
    private void fecharDetalhes() {
        overlayDetalhes.setVisible(false);
        overlayDetalhes.setManaged(false);
        tabelaUsuarios.getSelectionModel().clearSelection();
    }

    private void abrirEdicao(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/cadastroUsuario.fxml"));
            Node tela = loader.load();

            cadastroUsuarioController controller = loader.getController();
            controller.setUsuarioEdicao(usuario);
            controller.setAreaPrincipal(areaPrincipal);

            AnchorPane.setTopAnchor(tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor(tela, 0.0);
            AnchorPane.setRightAnchor(tela, 0.0);

            areaPrincipal.getChildren().setAll(tela);

        } catch (IOException e) {
            System.err.println("Erro ao abrir edição: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void carregarDados() {
        todosUsuarios     = UsuarioDAO.listarTodos();
        usuariosFiltrados = new FilteredList<>(todosUsuarios, u -> true);

        SortedList<Usuario> ordenados = new SortedList<>(usuariosFiltrados);
        ordenados.comparatorProperty().bind(tabelaUsuarios.comparatorProperty());

        colId.setSortType(TableColumn.SortType.ASCENDING);
        tabelaUsuarios.getSortOrder().add(colId);
        tabelaUsuarios.setItems(ordenados);
    }

    private void configurarBusca() {
        searchCod.textProperty().addListener((obs, a, n)  -> aplicarFiltros());
        searchNome.textProperty().addListener((obs, a, n) -> aplicarFiltros());
        filtroStatus.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String cod    = searchCod.getText()  == null ? "" : searchCod.getText().toLowerCase();
        String nome   = searchNome.getText() == null ? "" : searchNome.getText().toLowerCase();
        String status = filtroStatus.getValue();

        usuariosFiltrados.setPredicate(u -> {
            boolean matchCod    = cod.isEmpty()  || String.valueOf(u.getIdUsuario()).contains(cod);
            boolean matchNome   = nome.isEmpty() || u.getNome().toLowerCase().contains(nome);
            boolean matchStatus = status == null || status.equals("Todos os status")
                    || u.getStatus().equals(status);
            return matchCod && matchNome && matchStatus;
        });
    }

    @FXML private void onSearch()       { aplicarFiltros(); }
    @FXML private void onFiltroStatus() { aplicarFiltros(); }
}