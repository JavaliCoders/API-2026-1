package api.controller;

import api.DAO.compraDAO;
import api.DAO.fornecedorDAO;
import api.DAO.pedidoDAO;
import api.model.*;
import api.service.HistoricoService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.converter.DoubleStringConverter;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;

public class cadastroCompraController implements Initializable {

    // ── Cabeçalho ─────────────────────────────────────────────────────────────
    @FXML private Label labelNumPedido;
    @FXML private Label labelSolicitante;
    @FXML private Label labelSetor;
    @FXML private Label labelStatusPedido;

    // ── Formulário ────────────────────────────────────────────────────────────
    @FXML private ComboBox<Fornecedor> comboFornecedor;
    @FXML private DatePicker           dataEntregaPrevista;

    // ── Tabela de novos itens a comprar ───────────────────────────────────────
    @FXML private TableView<CompraItem>            tabelaItens;
    @FXML private TableColumn<CompraItem, String>  colProduto;
    @FXML private TableColumn<CompraItem, String>  colUnidade;
    @FXML private TableColumn<CompraItem, String>  colPendente;
    @FXML private TableColumn<CompraItem, Double>  colQtdCompra;
    @FXML private TableColumn<CompraItem, Double>  colValorUnit;
    @FXML private TableColumn<CompraItem, String>  colTotal;
    @FXML private TableColumn<CompraItem, Void>    colRemover;

    // ── Resumo / total ────────────────────────────────────────────────────────
    @FXML private Label labelTotalNova;

    // ── Tabela de compras já registradas ──────────────────────────────────────
    @FXML private TableView<Compra>            tabelaComprasExistentes;
    @FXML private TableColumn<Compra, String>  colCompraNum;
    @FXML private TableColumn<Compra, String>  colCompraData;
    @FXML private TableColumn<Compra, String>  colCompraFornecedor;
    @FXML private TableColumn<Compra, String>  colCompraValor;
    @FXML private TableColumn<Compra, String>  colCompraStatus;
    @FXML private TableColumn<Compra, Void>    colCompraCancelar;

    // ── Info sobre itens pendentes ─────────────────────────────────────────────
    @FXML private Label labelAviso;

    // ── State ─────────────────────────────────────────────────────────────────
    private AnchorPane areaPrincipal;
    private Pedido pedido;
    private final ObservableList<CompraItem> itensNovos     = FXCollections.observableArrayList();
    private final ObservableList<Compra>     comprasExist   = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabelaNovaCompra();
        configurarTabelaComprasExistentes();
        carregarFornecedores();
    }

    public void setAreaPrincipal(AnchorPane area) { this.areaPrincipal = area; }

    public void setPedidoSelecionado(Pedido pedido) {
        this.pedido = pedido;
        preencherCabecalho();
        carregarItensDisponiveis();
        carregarComprasExistentes();
    }

    // ── Cabeçalho ─────────────────────────────────────────────────────────────

    private void preencherCabecalho() {
        labelNumPedido   .setText(pedido.getNumPedido());
        labelSolicitante .setText(pedido.getNomeSolicitante());
        labelSetor       .setText(pedido.getNomeSetor());
        labelStatusPedido.setText(formatarStatus(pedido.getStatus()));
        labelStatusPedido.setStyle(estiloBadge(pedido.getStatus()));
    }

    // ── Itens disponíveis para nova compra ────────────────────────────────────

    private void carregarItensDisponiveis() {
        itensNovos.clear();

        for (PedidoProduto pp : pedidoDAO.listarItens(pedido.getIdPedido())) {
            double pendente = pp.getQtdPendente();
            if (pendente <= 0) continue;

            CompraItem item = new CompraItem(pp, pp.getValorUnitario(), 0);
            itensNovos.add(item);
        }

        tabelaItens.setItems(itensNovos);
        atualizarAviso();
        calcularTotal();
    }

    // ── Compras já registradas ────────────────────────────────────────────────

    private void carregarComprasExistentes() {
        comprasExist.setAll(compraDAO.listarPorPedido(pedido.getIdPedido()));
        tabelaComprasExistentes.setItems(comprasExist);
    }

    // ── Configuração da tabela de nova compra ─────────────────────────────────

    private void configurarTabelaNovaCompra() {
        tabelaItens.setEditable(true);

        colProduto.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNomeProduto()));

        colUnidade.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getUnidade()));

        colPendente.setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.format("%.0f", d.getValue().getPedidoProduto().getQtdPendente())));

        // Qtd a comprar — editável
        colQtdCompra.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getQtdComprada()));
        colQtdCompra.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colQtdCompra.setEditable(true);
        colQtdCompra.setOnEditCommit(e -> {
            CompraItem item = e.getRowValue();
            double novaQtd = e.getNewValue() == null ? 0 : e.getNewValue();
            double pendente = item.getPedidoProduto().getQtdPendente();

            if (novaQtd < 0) novaQtd = 0;
            if (novaQtd > pendente) {
                mostrarAviso("Quantidade excede o pendente (" + (int) pendente + "). Ajustado automaticamente.");
                novaQtd = pendente;
            }

            item.setQtdComprada(novaQtd);
            tabelaItens.refresh();
            calcularTotal();
        });

        // Valor unitário — editável
        colValorUnit.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getValorUni()));
        colValorUnit.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colValorUnit.setEditable(true);
        colValorUnit.setOnEditCommit(e -> {
            CompraItem item = e.getRowValue();
            double novoValor = e.getNewValue() == null ? 0 : e.getNewValue();
            if (novoValor < 0) novoValor = 0;
            item.setValorUni(novoValor);
            tabelaItens.refresh();
            calcularTotal();
        });

        // Total por linha
        colTotal.setCellValueFactory(d ->
                new SimpleStringProperty(formatarMoeda(d.getValue().getValorTotal())));

        // Botão remover item
        colRemover.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("🗑");
            {
                btn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; " +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;");
                btn.setFont(Font.font("Segoe UI", 12));
                btn.setOnMouseEntered(e -> btn.setStyle(
                        "-fx-background-color: #fca5a5; -fx-text-fill: #991b1b; " +
                                "-fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;"));
                btn.setOnMouseExited(e -> btn.setStyle(
                        "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; " +
                                "-fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;"));
                btn.setOnAction(e -> {
                    CompraItem item = getTableView().getItems().get(getIndex());
                    itensNovos.remove(item);
                    calcularTotal();
                    atualizarAviso();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(btn); box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        // Zebra
        tabelaItens.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(CompraItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color: white;");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color: white;"
                        : "-fx-background-color: #fafafa;");
            }
        });

        // Colunas de texto com estilo consistente
        for (TableColumn<CompraItem, String> col : new TableColumn[]{
                colProduto, colUnidade, colPendente, colTotal}) {
            col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String val, boolean empty) {
                    super.updateItem(val, empty);
                    if (empty || val == null) { setText(null); return; }
                    setText(val);
                    setFont(Font.font("Segoe UI", 13));
                    setStyle("-fx-text-fill: #0f172a;");
                }
            });
        }
        // Re-bind após setCellFactory
        colProduto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colUnidade.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidade()));
        colPendente.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%.0f", d.getValue().getPedidoProduto().getQtdPendente())));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(
                formatarMoeda(d.getValue().getValorTotal())));
    }

    // ── Configuração da tabela de compras existentes ──────────────────────────

    private void configurarTabelaComprasExistentes() {

        colCompraNum.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getIdCompra())));
        colCompraData.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDataFormatada()));
        colCompraFornecedor.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNomeFornecedor()));
        colCompraValor.setCellValueFactory(d ->
                new SimpleStringProperty(formatarMoeda(d.getValue().getValorTotal())));

        // Badge de status
        colCompraStatus.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatus()));
        colCompraStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(status);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(90);
                badge.setStyle(status.equals("REALIZADA")
                        ? "-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-background-radius:6; -fx-padding:4 8;"
                        : "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b; -fx-background-radius:6; -fx-padding:4 8;");
                HBox box = new HBox(badge); box.setAlignment(Pos.CENTER);
                setGraphic(box); setText(null);
            }
        });

        // Botão cancelar compra existente
        colCompraCancelar.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Cancelar");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                btn.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; " +
                        "-fx-background-radius:6; -fx-border-color:transparent; -fx-padding:5 12; -fx-cursor:hand;");
                btn.setOnMouseEntered(e -> btn.setStyle(
                        "-fx-background-color:#fca5a5; -fx-text-fill:#991b1b; " +
                                "-fx-background-radius:6; -fx-border-color:transparent; -fx-padding:5 12; -fx-cursor:hand;"));
                btn.setOnMouseExited(e -> btn.setStyle(
                        "-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; " +
                                "-fx-background-radius:6; -fx-border-color:transparent; -fx-padding:5 12; -fx-cursor:hand;"));
                btn.setOnAction(e -> {
                    Compra compra = getTableView().getItems().get(getIndex());
                    onCancelarCompra(compra);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                Compra c = (Compra) getTableRow().getItem();
                if ("REALIZADA".equals(c.getStatus())) {
                    HBox box = new HBox(btn); box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }
        });

        // Zebra
        tabelaComprasExistentes.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Compra item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color: white;");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color: white;"
                        : "-fx-background-color: #fafafa;");
            }
        });
    }

    // ── Ações ─────────────────────────────────────────────────────────────────

    @FXML
    private void onSalvar() {
        // Validações
        if (comboFornecedor.getValue() == null) {
            mostrarErro("Selecione um fornecedor.");
            return;
        }

        long itensComQtd = itensNovos.stream().filter(i -> i.getQtdComprada() > 0).count();
        if (itensComQtd == 0) {
            mostrarErro("Informe a quantidade de pelo menos um item.");
            return;
        }

        // Monta objeto Compra
        Compra compra = new Compra();
        compra.setPedido(pedido);
        compra.setFornecedor(comboFornecedor.getValue());
        compra.setComprador(SessaoUsuario.getInstancia().getUsuarioLogado());
        compra.setData(LocalDateTime.now());
        compra.setValorTotal(calcularTotal());

        if (dataEntregaPrevista.getValue() != null)
            compra.setDataPrevista(dataEntregaPrevista.getValue().atStartOfDay());

        int id = compraDAO.registrar(compra, itensNovos);

        if (id > 0) {
            HistoricoService.registrar("Compra", "Cadastro", id,
                    "Compra registrada para pedido " + pedido.getNumPedido()
                            + " por " + SessaoUsuario.getInstancia().getNomeUsuarioLogado());

            new Alert(Alert.AlertType.INFORMATION, "Compra registrada com sucesso!").showAndWait();

            // Recarrega dados — pode haver mais itens pendentes
            carregarItensDisponiveis();
            carregarComprasExistentes();
            comboFornecedor.setValue(null);
            dataEntregaPrevista.setValue(null);

        } else if (id == -2) {
            mostrarErro("Informe a quantidade de ao menos um item.");
        } else if (id == -3) {
            mostrarErro("A quantidade informada excede o total pendente de algum item.");
        } else {
            mostrarErro("Erro ao registrar a compra. Verifique os dados.");
        }
    }

    private void onCancelarCompra(Compra compra) {
        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
        dlg.setTitle("Cancelar Compra");
        dlg.setHeaderText(null);
        dlg.setContentText("Deseja cancelar esta compra?\n" +
                "As quantidades compradas serão revertidas e os itens voltarão como pendentes.");

        Optional<ButtonType> resultado = dlg.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean ok = compraDAO.cancelar(compra.getIdCompra());

            if (ok) {
                HistoricoService.registrar("Compra", "Cancelamento", compra.getIdCompra(),
                        "Compra cancelada — pedido " + pedido.getNumPedido()
                                + " por " + SessaoUsuario.getInstancia().getNomeUsuarioLogado());
                new Alert(Alert.AlertType.INFORMATION, "Compra cancelada com sucesso.").showAndWait();
                carregarItensDisponiveis();
                carregarComprasExistentes();
            } else {
                mostrarErro("Erro ao cancelar a compra.");
            }
        }
    }

    @FXML
    private void onVoltar() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/view/pedido.fxml"));
            javafx.scene.Node tela = loader.load();
            pedidoController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Auxiliares ────────────────────────────────────────────────────────────

    private void carregarFornecedores() {
        comboFornecedor.setItems(fornecedorDAO.listarAtivos());
        comboFornecedor.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Fornecedor f) { return f != null ? f.getNome() : ""; }
            @Override public Fornecedor fromString(String s) { return null; }
        });
    }

    private double calcularTotal() {
        double total = itensNovos.stream()
                .mapToDouble(i -> i.getQtdComprada() * i.getValorUni())
                .sum();
        if (labelTotalNova != null)
            labelTotalNova.setText(formatarMoeda(total));
        return total;
    }

    private void atualizarAviso() {
        if (labelAviso == null) return;
        if (itensNovos.isEmpty()) {
            labelAviso.setText("✅  Todos os itens do pedido já possuem compra cadastrada.");
            labelAviso.setStyle("-fx-text-fill: #166534; -fx-font-size: 13px; -fx-font-weight: bold;");
        } else {
            labelAviso.setText("⚠️  " + itensNovos.size() + " item(ns) ainda pendente(s) de compra.");
            labelAviso.setStyle("-fx-text-fill: #854d0e; -fx-font-size: 13px; -fx-font-weight: bold;");
        }
    }

    private void mostrarAviso(String msg) {
        if (labelAviso != null) {
            labelAviso.setText("⚠️  " + msg);
            labelAviso.setStyle("-fx-text-fill: #854d0e; -fx-font-size: 13px;");
        }
    }

    private void mostrarErro(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private void anchorar(javafx.scene.Node tela) {
        AnchorPane.setTopAnchor(tela, 0.0); AnchorPane.setBottomAnchor(tela, 0.0);
        AnchorPane.setLeftAnchor(tela, 0.0); AnchorPane.setRightAnchor(tela, 0.0);
    }

    private String formatarMoeda(double valor) {
        return String.format("R$ %.2f", valor).replace(".", ",");
    }

    private String formatarStatus(String s) {
        return switch (s) {
            case "EM_APROVACAO"          -> "Em Aprovação";
            case "APROVADO"              -> "Aprovado";
            case "APROVADO_PARCIALMENTE" -> "Aprov. Parcial";
            case "NEGADO"                -> "Negado";
            case "EM_COTACAO"            -> "Em Cotação";
            case "EM_COMPRA"             -> "Em Compra";
            case "FINALIZADO"            -> "Finalizado";
            default                      -> s;
        };
    }

    private String estiloBadge(String s) {
        String base = "-fx-background-radius:6; -fx-padding:4 10; -fx-font-size:11px; -fx-font-weight:bold;";
        return base + switch (s) {
            case "EM_APROVACAO"          -> "-fx-background-color:#fef9c3; -fx-text-fill:#854d0e;";
            case "APROVADO"              -> "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
            case "APROVADO_PARCIALMENTE" -> "-fx-background-color:#d1fae5; -fx-text-fill:#065f46;";
            case "NEGADO"                -> "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
            case "EM_COTACAO"            -> "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af;";
            case "EM_COMPRA"             -> "-fx-background-color:#ede9fe; -fx-text-fill:#5b21b6;";
            case "FINALIZADO"            -> "-fx-background-color:#d1fae5; -fx-text-fill:#065f46;";
            default                      -> "-fx-background-color:#f3f4f6; -fx-text-fill:#6b7280;";
        };
    }
}