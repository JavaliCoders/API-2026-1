package api.controller;

import api.DAO.movimentacaoDAO;
import api.DAO.produtoDAO;
import api.model.Produto;
import api.model.SessaoUsuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class cadastroMovimentacaoManualController implements Initializable {

    // ── Tipo de operação ──────────────────────────────────────
    @FXML private ComboBox<String> comboTipo;

    // ── Busca e tabela de produtos ────────────────────────────
    @FXML private TextField fieldBuscaProduto;
    @FXML private TableView<Produto>           tabelaProdutos;
    @FXML private TableColumn<Produto, String> colCod;
    @FXML private TableColumn<Produto, String> colProduto;
    @FXML private TableColumn<Produto, String> colUnidade;
    @FXML private TableColumn<Produto, String> colSaldo;
    @FXML private TableColumn<Produto, String> colNivelMin;
    @FXML private TableColumn<Produto, String> colStatus;

    // ── Formulário ────────────────────────────────────────────
    @FXML private Label     labelProdutoSelecionado;
    @FXML private Label     labelSaldoAtual;
    @FXML private Label     labelBadgeTipo;
    @FXML private TextField fieldQuantidade;
    @FXML private TextArea  fieldObservacao;
    @FXML private Button    btnConfirmar;
    @FXML private Label     labelAviso;

    // ── Dados ─────────────────────────────────────────────────
    private AnchorPane areaPrincipal;
    private ObservableList<Produto> todosProdutos;
    private FilteredList<Produto>   produtosFiltrados;
    private Produto produtoSelecionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarComboTipo();
        configurarTabela();
        carregarProdutos();
        labelAviso.setVisible(false);
        labelAviso.setManaged(false);
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    // ── Combo tipo ────────────────────────────────────────────

    private void configurarComboTipo() {
        comboTipo.setItems(FXCollections.observableArrayList("Entrada Manual", "Saída Manual"));
        comboTipo.setValue("Entrada Manual");
        atualizarEstiloTipo("Entrada Manual");

        comboTipo.valueProperty().addListener((obs, antigo, novo) -> {
            atualizarEstiloTipo(novo);
            if (produtoSelecionado != null) atualizarPainel();
        });

        comboTipo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.equals("Saída Manual")
                        ? "-fx-text-fill:#dc2626; -fx-font-weight:bold;"
                        : "-fx-text-fill:#16a34a; -fx-font-weight:bold;");
            }
        });
        comboTipo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.equals("Saída Manual")
                        ? "-fx-text-fill:#dc2626; -fx-font-weight:bold;"
                        : "-fx-text-fill:#16a34a; -fx-font-weight:bold;");
            }
        });
    }

    private void atualizarEstiloTipo(String tipo) {
        if (tipo == null) return;
        boolean isSaida = tipo.equals("Saída Manual");

        btnConfirmar.setText(isSaida ? "Confirmar Saída" : "Confirmar Entrada");
        btnConfirmar.setStyle(
                (isSaida
                        ? "-fx-background-color:#dc2626;"
                        : "-fx-background-color:#16a34a;") +
                        " -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:8;" +
                        " -fx-padding:10 24; -fx-cursor:hand; -fx-border-color:transparent;");

        labelBadgeTipo.setText(isSaida ? "SAÍDA MANUAL" : "ENTRADA MANUAL");
        labelBadgeTipo.setStyle(
                "-fx-font-size:11px; -fx-font-weight:bold; -fx-background-radius:6; -fx-padding:4 10;" +
                        (isSaida
                                ? "-fx-background-color:#fef3c7; -fx-text-fill:#92400e;"
                                : "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af;"));
    }

    // ── Tabela de produtos ────────────────────────────────────

    private void configurarTabela() {
        colCod     .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getIdProduto())));
        colProduto .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getProduto()));
        colUnidade .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getUnidadeMedida()));
        colSaldo   .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getSaldo())));
        colNivelMin.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getNivelMinimo())));
        colStatus  .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()));

        // Badge saldo
        colSaldo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Produto p = getTableView().getItems().get(getIndex());
                boolean baixo = p.getSaldo() < p.getNivelMinimo();
                Label badge = new Label(item);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(60);
                badge.setStyle(baixo
                        ? "-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; -fx-background-radius:6; -fx-padding:4 8;"
                        : "-fx-background-color:#f3f4f6; -fx-text-fill:#374151; -fx-background-radius:6; -fx-padding:4 8;");
                setGraphic(badge); setText(null);
            }
        });

        // Badge status
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(70);
                badge.setStyle(item.equals("ATIVO")
                        ? "-fx-background-color:#dcfce7; -fx-text-fill:#16a34a; -fx-background-radius:6; -fx-padding:4 10;"
                        : "-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; -fx-background-radius:6; -fx-padding:4 10;");
                setGraphic(badge); setText(null);
            }
        });

        // Zebra
        tabelaProdutos.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Produto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color:white;");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color:white;" : "-fx-background-color:#fafafa;");
            }
        });

        // Seleção
        tabelaProdutos.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> {
                    produtoSelecionado = novo;
                    if (novo != null) atualizarPainel();
                });
    }

    private void carregarProdutos() {
        todosProdutos     = produtoDAO.listarTodos();
        produtosFiltrados = new FilteredList<>(todosProdutos, p -> true);
        tabelaProdutos.setItems(produtosFiltrados);

        fieldBuscaProduto.textProperty().addListener((obs, a, n) -> {
            String t = n == null ? "" : n.toLowerCase();
            produtosFiltrados.setPredicate(p ->
                    t.isEmpty()
                            || p.getProduto().toLowerCase().contains(t)
                            || String.valueOf(p.getIdProduto()).contains(t));
        });
    }

    private void atualizarPainel() {
        if (produtoSelecionado == null) return;
        labelProdutoSelecionado.setText(
                produtoSelecionado.getProduto() + "  (COD: " + produtoSelecionado.getIdProduto() + ")");
        labelSaldoAtual.setText(
                "Saldo atual: " + produtoSelecionado.getSaldo()
                        + " " + produtoSelecionado.getUnidadeMedida());
        boolean baixo = produtoSelecionado.getSaldo() < produtoSelecionado.getNivelMinimo();
        labelSaldoAtual.setStyle(baixo
                ? "-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#dc2626;"
                : "-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#16a34a;");
        atualizarEstiloTipo(comboTipo.getValue());
    }

    // ── Confirmar ─────────────────────────────────────────────

    @FXML private void onConfirmar() {
        labelAviso.setVisible(false);
        labelAviso.setManaged(false);

        if (produtoSelecionado == null) {
            mostrarAviso("Selecione um produto na tabela.");
            return;
        }

        String qtdTexto = fieldQuantidade.getText();
        if (qtdTexto == null || qtdTexto.isBlank()) {
            mostrarAviso("Informe a quantidade.");
            return;
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(qtdTexto.trim());
            if (quantidade <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarAviso("Informe um número inteiro positivo.");
            return;
        }

        String obs = fieldObservacao.getText() == null ? "" : fieldObservacao.getText().trim();
        if (obs.isBlank()) {
            mostrarAviso("O motivo da movimentação é obrigatório.");
            return;
        }

        boolean isSaida = comboTipo.getValue().equals("Saída Manual");

        if (isSaida && quantidade > produtoSelecionado.getSaldo()) {
            mostrarAviso("Saldo insuficiente. Disponível: "
                    + produtoSelecionado.getSaldo()
                    + " " + produtoSelecionado.getUnidadeMedida() + ".");
            return;
        }

        String acao = isSaida ? "saída" : "entrada";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmar " + acao + " de " + quantidade + " "
                        + produtoSelecionado.getUnidadeMedida()
                        + " de \"" + produtoSelecionado.getProduto() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar movimentação");
        confirm.setHeaderText(null);

        Optional<ButtonType> resp = confirm.showAndWait();
        if (resp.isEmpty() || resp.get() != ButtonType.YES) return;

        int idUsuario = SessaoUsuario.getInstancia().getIdUsuarioLogado();
        int idProduto = produtoSelecionado.getIdProduto();

        boolean ok = isSaida
                ? movimentacaoDAO.inserirSaidaManual  (idProduto, quantidade, idUsuario, obs)
                : movimentacaoDAO.inserirEntradaManual(idProduto, quantidade, idUsuario, obs);

        if (ok) {
            Alert sucesso = new Alert(Alert.AlertType.INFORMATION,
                    "Movimentação registrada com sucesso!", ButtonType.OK);
            sucesso.setTitle("Sucesso");
            sucesso.setHeaderText(null);
            sucesso.showAndWait();
            voltarParaMovimentacoes();
        } else {
            mostrarAviso("Erro ao registrar. Tente novamente.");
        }
    }

    @FXML private void onCancelar() {
        voltarParaMovimentacoes();
    }

    private void voltarParaMovimentacoes() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/movimentacao.fxml"));
            Node tela = loader.load();

            movimentacaoController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);

            AnchorPane.setTopAnchor   (tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor  (tela, 0.0);
            AnchorPane.setRightAnchor (tela, 0.0);

            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) {
            System.err.println("Erro ao voltar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarAviso(String msg) {
        labelAviso.setText("⚠  " + msg);
        labelAviso.setVisible(true);
        labelAviso.setManaged(true);
    }
}