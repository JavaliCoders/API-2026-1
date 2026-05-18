package api.controller;

import api.DAO.CotacaoDAO;
import api.DAO.pedidoDAO;
import api.model.*;
import api.service.HistoricoService;
import api.service.NotificacaoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ResourceBundle;

public class cadastroCotacaoController implements Initializable {

    // ── Cabeçalho ─────────────────────────────────────────────
    @FXML private Label     labelTitulo;
    @FXML private Label     labelSubtitulo;
    @FXML private TextField fieldNumPedido;
    @FXML private TextField fieldSolicitante;
    @FXML private TextField fieldValorPedido;

    // ── Dados da cotação ──────────────────────────────────────
    @FXML private ComboBox<Fornecedor> fieldFornecedor;
    @FXML private TextArea             fieldObservacoes;

    // ── Tabela de itens ───────────────────────────────────────
    @FXML private TextField  searchProduto;
    @FXML private ListView<PedidoProduto> listaProdutos;
    @FXML private TextField  fieldQtd;
    @FXML private TextField  fieldValorUnit;
    @FXML private Label      labelProdutoSelecionado;
    @FXML private HBox       boxProdutoSelecionado;
    @FXML private Label      erroProduto;
    @FXML private TableView<CotacaoItem>            tabelaItens;
    @FXML private TableColumn<CotacaoItem, String>  colProduto;
    @FXML private TableColumn<CotacaoItem, String>  colUnidade;
    @FXML private TableColumn<CotacaoItem, String>  colQtd;
    @FXML private TableColumn<CotacaoItem, String>  colVlrUnit;
    @FXML private TableColumn<CotacaoItem, String>  colVlrTotal;
    @FXML private TableColumn<CotacaoItem, Void>    colRemover;
    @FXML private Label      labelTotal;

    // ── Anexo ─────────────────────────────────────────────────
    @FXML private Label labelArquivoSelecionado;
    @FXML private Label labelNomeArquivo;
    @FXML private Label labelTamanhoArquivo;
    @FXML private HBox  boxPreviewAnexo;

    // ── Erros ─────────────────────────────────────────────────
    @FXML private Label erroFornecedor;
    @FXML private Label erroAnexo;

    // ── Estado ────────────────────────────────────────────────
    private AnchorPane  areaPrincipal;
    private Pedido      pedido;
    private Cotacao     cotacaoEdicao = null; // null = novo cadastro
    private File        arquivoSelecionado;
    private PedidoProduto produtoSelecionado;

    private ObservableList<PedidoProduto> itensDoPedido = FXCollections.observableArrayList();
    private ObservableList<CotacaoItem>   itensCotacao  = FXCollections.observableArrayList();

    private static final String PASTA_ANEXOS = "anexos/cotacoes/";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColunaItens();
        tabelaItens.setItems(itensCotacao);

        fieldValorUnit.textProperty().addListener((obs, a, n) -> {
            if (!n.matches("[\\d,\\.]*")) fieldValorUnit.setText(a);
        });
        fieldQtd.textProperty().addListener((obs, a, n) -> {
            if (!n.matches("\\d*")) fieldQtd.setText(a);
        });

        listaProdutos.setOnMouseClicked(e -> {
            PedidoProduto pp = listaProdutos.getSelectionModel().getSelectedItem();
            if (pp != null) selecionarProduto(pp);
        });

        listaProdutos.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(PedidoProduto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getNomeProduto() + "  [" + item.getUnidadeProduto() + "]");
            }
        });
    }

    public void setAreaPrincipal(AnchorPane ap) { this.areaPrincipal = ap; }

    // ── Chamado para NOVO cadastro ────────────────────────────
    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
        preencherCabecalho();
        carregarFornecedores();
        carregarItensDoPedido();
    }

    // ── Chamado para EDIÇÃO ────────────────────────────────────
    public void setCotacaoEdicao(Cotacao c, Pedido pedidoCompleto) {
        this.cotacaoEdicao = c;
        this.pedido        = pedidoCompleto;

        labelTitulo.setText("Editar Cotação");
        labelSubtitulo.setText("Editando cotação do pedido " + pedidoCompleto.getNumPedido());

        preencherCabecalho();
        carregarFornecedores();
        carregarItensDoPedido();

        // Pré-seleciona fornecedor
        fieldFornecedor.getItems().stream()
                .filter(f -> f.getIdFornecedor() == c.getFornecedor().getIdFornecedor())
                .findFirst().ifPresent(fieldFornecedor::setValue);

        // Carrega itens existentes
        itensCotacao.setAll(CotacaoDAO.listarItens(c.getIdCotacao()));
        atualizarTotal();

        // Mostra anexo existente se houver
        if (c.temAnexo()) {
            labelArquivoSelecionado.setText(c.getNomeAnexo());
            labelNomeArquivo.setText(c.getNomeAnexo());
            labelTamanhoArquivo.setText("arquivo existente");
            boxPreviewAnexo.setVisible(true);
            boxPreviewAnexo.setManaged(true);
        }
    }

    private void preencherCabecalho() {
        fieldNumPedido  .setText(pedido.getNumPedido());
        fieldSolicitante.setText(pedido.getNomeSolicitante());
        fieldValorPedido.setText(
                String.format("R$ %.2f", pedido.getValorTotalEstimado()).replace(".", ","));
        if (labelSubtitulo.getText().isBlank())
            labelSubtitulo.setText("Registrando cotação para o pedido " + pedido.getNumPedido());
    }

    private void carregarFornecedores() {
        ObservableList<Fornecedor> lista = api.DAO.fornecedorDAO.listarAtivos();
        fieldFornecedor.setItems(lista);
        fieldFornecedor.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Fornecedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getNome() + "  —  CNPJ: " + item.getCnpj());
            }
        });
        fieldFornecedor.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Fornecedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });
    }

    private void carregarItensDoPedido() {
        itensDoPedido.setAll(pedidoDAO.listarItens(pedido.getIdPedido()));
    }

    // ── Busca de produto ──────────────────────────────────────
    @FXML
    private void onBuscarProduto() {
        String texto = searchProduto.getText().trim().toLowerCase();
        if (texto.isBlank()) {
            listaProdutos.setVisible(false);
            listaProdutos.setManaged(false);
            return;
        }
        ObservableList<PedidoProduto> filtrados = itensDoPedido.filtered(pp ->
                pp.getNomeProduto().toLowerCase().contains(texto));
        listaProdutos.setItems(filtrados);
        listaProdutos.setVisible(!filtrados.isEmpty());
        listaProdutos.setManaged(!filtrados.isEmpty());
    }

    private void selecionarProduto(PedidoProduto pp) {
        this.produtoSelecionado = pp;
        labelProdutoSelecionado.setText(pp.getNomeProduto() + "  [" + pp.getUnidadeProduto() + "]");
        boxProdutoSelecionado.setVisible(true);
        boxProdutoSelecionado.setManaged(true);
        listaProdutos.setVisible(false);
        listaProdutos.setManaged(false);
        searchProduto.clear();
        erroProduto.setText("");
    }

    @FXML
    private void onLimparProduto() {
        produtoSelecionado = null;
        boxProdutoSelecionado.setVisible(false);
        boxProdutoSelecionado.setManaged(false);
        fieldQtd.clear();
        fieldValorUnit.clear();
    }

    @FXML
    private void onAdicionarProduto() {
        erroProduto.setText("");
        if (produtoSelecionado == null) {
            erroProduto.setText("Selecione um produto da lista."); return;
        }
        if (fieldQtd.getText().isBlank()) {
            erroProduto.setText("Informe a quantidade."); return;
        }
        if (fieldValorUnit.getText().isBlank()) {
            erroProduto.setText("Informe o valor unitário."); return;
        }

        int    qtd;
        double valorUnit;
        try {
            qtd       = Integer.parseInt(fieldQtd.getText().trim());
            valorUnit = Double.parseDouble(fieldValorUnit.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            erroProduto.setText("Quantidade ou valor inválido."); return;
        }
        if (qtd <= 0 || valorUnit <= 0) {
            erroProduto.setText("Quantidade e valor devem ser maiores que zero."); return;
        }

        // Impede duplicata do mesmo produto
        boolean jaNaLista = itensCotacao.stream()
                .anyMatch(i -> i.getIdPedidoProduto() == produtoSelecionado.getIdPedidoProduto());
        if (jaNaLista) {
            erroProduto.setText("Este produto já foi adicionado."); return;
        }

        itensCotacao.add(new CotacaoItem(
                produtoSelecionado.getIdPedidoProduto(),
                produtoSelecionado.getNomeProduto(),
                produtoSelecionado.getUnidadeProduto(),
                qtd, valorUnit
        ));
        atualizarTotal();
        onLimparProduto();
    }

    private void atualizarTotal() {
        double total = itensCotacao.stream().mapToDouble(CotacaoItem::getValorTotal).sum();
        labelTotal.setText(String.format("R$ %.2f", total).replace(".", ","));
    }

    // ── Colunas da tabela de itens ────────────────────────────
    private void configurarColunaItens() {
        colProduto .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colUnidade .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidade()));
        colQtd     .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQtdCotada())));
        colVlrUnit .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getValorUnitFormatado()));
        colVlrTotal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getValorTotalFormatado()));

        colRemover.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("🗑");
            {
                btn.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#dc2626;" +
                        "-fx-background-radius:6; -fx-border-color:transparent;" +
                        "-fx-cursor:hand; -fx-font-size:13px; -fx-padding:4 8;");
                btn.setOnAction(e -> {
                    itensCotacao.remove(getTableView().getItems().get(getIndex()));
                    atualizarTotal();
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(btn); box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });
    }

    // ── Anexo ─────────────────────────────────────────────────
    @FXML
    private void onSelecionarArquivo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Selecionar Arquivo da Cotação");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Documentos e Imagens",
                "*.pdf","*.PDF","*.png","*.PNG","*.jpg","*.JPG",
                "*.jpeg","*.JPEG","*.docx","*.xlsx","*.xls"));
        File arquivo = fc.showOpenDialog(areaPrincipal.getScene().getWindow());
        if (arquivo != null) {
            arquivoSelecionado = arquivo;
            erroAnexo.setText("");
            labelArquivoSelecionado.setText(arquivo.getName());
            labelNomeArquivo.setText(arquivo.getName());
            labelTamanhoArquivo.setText(formatarTamanho(arquivo.length()));
            boxPreviewAnexo.setVisible(true);
            boxPreviewAnexo.setManaged(true);
        }
    }

    @FXML
    private void onRemoverArquivo() {
        arquivoSelecionado = null;
        labelArquivoSelecionado.setText("Nenhum arquivo selecionado");
        boxPreviewAnexo.setVisible(false);
        boxPreviewAnexo.setManaged(false);
    }

    // ── Salvar ────────────────────────────────────────────────
    @FXML
    private void onSalvar() {
        limparErros();
        if (!validar()) return;

        double valorTotal = itensCotacao.stream()
                .mapToDouble(CotacaoItem::getValorTotal).sum();

        if (cotacaoEdicao == null) {
            // ── NOVO ──────────────────────────────────────────
            String caminhoFinal;
            try { caminhoFinal = copiarArquivo(arquivoSelecionado); }
            catch (IOException e) { erroAnexo.setText("Erro ao salvar arquivo: " + e.getMessage()); return; }

            int idCadastrador = SessaoUsuario.getInstancia().getIdUsuarioLogado();
            int idCotacao = CotacaoDAO.inserir(
                    valorTotal,
                    pedido.getIdPedido(),
                    fieldFornecedor.getValue().getIdFornecedor(),
                    idCadastrador,
                    arquivoSelecionado.getName(),
                    caminhoFinal,
                    itensCotacao
            );
            if (idCotacao == -1) { new Alert(Alert.AlertType.ERROR, "Erro ao salvar cotação.").showAndWait(); return; }

            CotacaoDAO.marcarPedidoEmCotacao(pedido.getIdPedido());

            StringBuilder descItens = new StringBuilder();
            for (CotacaoItem item : itensCotacao)
                descItens.append(item.getNomeProduto())
                        .append(" (qtd: ").append(item.getQtdCotada())
                        .append(", unit: ").append(item.getValorUnitFormatado()).append("); ");

            HistoricoService.registrar("Cotação", "Cadastro", idCotacao,
                    "Cotação para " + pedido.getNumPedido()
                            + " — Fornecedor: " + fieldFornecedor.getValue().getNome()
                            + " — Valor Total: R$ " + String.format("%.2f", valorTotal)
                            + " — Itens: " + descItens
                            + " — por " + SessaoUsuario.getInstancia().getNomeUsuarioLogado());

            NotificacaoService.notificarNovaCotacao(
                    pedido.getIdPedido(), pedido.getNumPedido(),
                    fieldFornecedor.getValue().getNome(), valorTotal,
                    SessaoUsuario.getInstancia().getNomeUsuarioLogado());

            sucesso("Cotação registrada! O pedido " + pedido.getNumPedido() + " agora está Em Cotação.");

        } else {
            // ── EDIÇÃO ────────────────────────────────────────
            String nomeArq    = arquivoSelecionado != null ? arquivoSelecionado.getName() : null;
            String caminhoArq = null;
            if (arquivoSelecionado != null) {
                try { caminhoArq = copiarArquivo(arquivoSelecionado); }
                catch (IOException e) { erroAnexo.setText("Erro ao salvar arquivo: " + e.getMessage()); return; }
            }

            boolean ok = CotacaoDAO.atualizar(
                    cotacaoEdicao.getIdCotacao(), valorTotal,
                    fieldFornecedor.getValue().getIdFornecedor(),
                    nomeArq, caminhoArq, itensCotacao);

            if (!ok) { new Alert(Alert.AlertType.ERROR, "Erro ao atualizar cotação.").showAndWait(); return; }

            HistoricoService.registrar("Cotação", "Alteração", cotacaoEdicao.getIdCotacao(),
                    "Cotação " + cotacaoEdicao.getIdCotacao() + " editada por "
                            + SessaoUsuario.getInstancia().getNomeUsuarioLogado()
                            + " — novo valor: R$ " + String.format("%.2f", valorTotal));

            sucesso("Cotação atualizada com sucesso!");
        }

        navegarParaCotacoes();
    }

    private boolean validar() {
        boolean ok = true;
        if (fieldFornecedor.getValue() == null) {
            erroFornecedor.setText("Selecione o fornecedor."); ok = false;
        }
        if (itensCotacao.isEmpty()) {
            erroProduto.setText("Adicione ao menos um produto à cotação."); ok = false;
        }
        // Arquivo obrigatório apenas no cadastro novo
        if (cotacaoEdicao == null && arquivoSelecionado == null) {
            erroAnexo.setText("Selecione o arquivo de cotação."); ok = false;
        }
        return ok;
    }

    private void limparErros() {
        erroFornecedor.setText("");
        erroProduto   .setText("");
        erroAnexo     .setText("");
    }

    // ── Navegação ─────────────────────────────────────────────
    @FXML private void onCancelar() { navegarParaCotacoes(); }

    private void navegarParaCotacoes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/cotacao.fxml"));
            Node tela = loader.load();
            CotacaoController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            if (pedido != null) ctrl.filtrarPorPedido(pedido);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Utilitários ───────────────────────────────────────────
    private String copiarArquivo(File origem) throws IOException {
        Path pasta = Paths.get(PASTA_ANEXOS);
        Files.createDirectories(pasta);
        String nomeUnico = System.currentTimeMillis() + "_" + origem.getName();
        Path destino = pasta.resolve(nomeUnico);
        Files.copy(origem.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        return destino.toString();
    }

    private void anchorar(Node tela) {
        AnchorPane.setTopAnchor   (tela, 0.0); AnchorPane.setBottomAnchor(tela, 0.0);
        AnchorPane.setLeftAnchor  (tela, 0.0); AnchorPane.setRightAnchor (tela, 0.0);
    }

    private void sucesso(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Sucesso"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private String formatarTamanho(long bytes) {
        if (bytes < 1024)        return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}