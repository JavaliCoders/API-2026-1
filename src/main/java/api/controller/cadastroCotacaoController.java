package api.controller;

import api.DAO.cotacaoDAO;
import api.DAO.fornecedorDAO;
import api.model.*;
import api.service.HistoricoService;
import api.service.NotificacaoService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

public class cadastroCotacaoController implements Initializable {

    @FXML private Label     labelSubtitulo;
    @FXML private TextField fieldNumPedido;
    @FXML private TextField fieldSolicitante;
    @FXML private TextField fieldValorPedido;

    @FXML private ComboBox<Fornecedor> fieldFornecedor;
    @FXML private TextField            fieldValorTotal;
    @FXML private TextArea             fieldObservacoes;

    @FXML private Label labelArquivoSelecionado;
    @FXML private Label labelNomeArquivo;
    @FXML private Label labelTamanhoArquivo;
    @FXML private HBox  boxPreviewAnexo;

    @FXML private Label erroFornecedor;
    @FXML private Label erroValor;
    @FXML private Label erroAnexo;

    private AnchorPane areaPrincipal;
    private Pedido     pedido;
    private File       arquivoSelecionado;

    /** Pasta onde os anexos serão copiados. Ajuste conforme seu servidor. */
    private static final String PASTA_ANEXOS = "anexos/cotacoes/";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        carregarFornecedores();
        fieldValorTotal.textProperty().addListener((obs, a, n) -> {
            if (!n.matches("[\\d,]*")) fieldValorTotal.setText(a);
        });
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
        fieldNumPedido  .setText(pedido.getNumPedido());
        fieldSolicitante.setText(pedido.getNomeSolicitante());
        fieldValorPedido.setText(
                String.format("R$ %.2f", pedido.getValorTotalEstimado()).replace(".", ","));
        labelSubtitulo.setText("Registrando cotação para o pedido " + pedido.getNumPedido());
    }

    private void carregarFornecedores() {
        ObservableList<Fornecedor> lista = fornecedorDAO.listarAtivos();
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

    // ── Arquivo ───────────────────────────────────────────────

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

        String caminhoFinal;
        try {
            caminhoFinal = copiarArquivo(arquivoSelecionado);
        } catch (IOException e) {
            erroAnexo.setText("Erro ao salvar arquivo: " + e.getMessage());
            return;
        }

        // ✅ Converte valor UMA vez só
        double valor = Double.parseDouble(fieldValorTotal.getText().replace(",", "."));

        int idCotacao = cotacaoDAO.inserir(
                valor,
                pedido.getIdPedido(),
                fieldFornecedor.getValue().getIdFornecedor(),
                arquivoSelecionado.getName(),
                caminhoFinal
        );

        // SE DER ERRO → NÃO NOTIFICA
        if (idCotacao == -1) {
            new Alert(Alert.AlertType.ERROR, "Erro ao salvar cotação.").showAndWait();
            return;
        }

        // ATUALIZA STATUS
        cotacaoDAO.marcarPedidoEmCotacao(pedido.getIdPedido());

        // HISTÓRICO
        HistoricoService.registrar("Cotação", "Cadastro", idCotacao,
                "Cotação para " + pedido.getNumPedido()
                        + " — Fornecedor: " + fieldFornecedor.getValue().getNome()
                        + " — Valor: R$ " + fieldValorTotal.getText()
                        + " — por " + SessaoUsuario.getInstancia().getNomeUsuarioLogado());


        NotificacaoService.notificarNovaCotacao(
                pedido.getIdPedido(),
                pedido.getNumPedido(),
                fieldFornecedor.getValue().getNome(),
                valor,
                SessaoUsuario.getInstancia().getNomeUsuarioLogado()
        );

        // ALERTA
        Alert ok = new Alert(Alert.AlertType.INFORMATION);
        ok.setTitle("Sucesso");
        ok.setHeaderText(null);
        ok.setContentText("Cotação registrada! O pedido " + pedido.getNumPedido()
                + " agora está Em Cotação.");
        ok.showAndWait();

        // NAVEGAÇÃO
        navegarParaCotacoes(pedido);
    }

    private String copiarArquivo(File origem) throws IOException {
        Path pasta = Paths.get(PASTA_ANEXOS);
        Files.createDirectories(pasta);
        String nomeUnico = System.currentTimeMillis() + "_" + origem.getName();
        Path destino = pasta.resolve(nomeUnico);
        Files.copy(origem.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        return destino.toString();
    }

    private boolean validar() {
        boolean ok = true;
        if (fieldFornecedor.getValue() == null) {
            erroFornecedor.setText("Selecione o fornecedor.");
            ok = false;
        }
        String vt = fieldValorTotal.getText().trim();
        if (vt.isBlank()) {
            erroValor.setText("Informe o valor da cotação.");
            ok = false;
        } else {
            try {
                double v = Double.parseDouble(vt.replace(",", "."));
                if (v <= 0) { erroValor.setText("O valor deve ser maior que zero."); ok = false; }
            } catch (NumberFormatException e) {
                erroValor.setText("Valor inválido. Use vírgula (ex: 1500,00).");
                ok = false;
            }
        }
        if (arquivoSelecionado == null) {
            erroAnexo.setText("Selecione o arquivo de cotação.");
            ok = false;
        }
        return ok;
    }

    private void limparErros() {
        erroFornecedor.setText("");
        erroValor.setText("");
        erroAnexo.setText("");
    }

    // ── Navegação ─────────────────────────────────────────────

    @FXML private void onCancelar() { voltarParaPedidos(); }

    private void voltarParaPedidos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/pedido.fxml"));
            Node tela = loader.load();
            pedidoController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navegarParaCotacoes(Pedido pedidoOrigem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/cotacao.fxml"));
            Node tela = loader.load();
            cotacaoController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            ctrl.filtrarPorPedido(pedidoOrigem);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void anchorar(Node tela) {
        AnchorPane.setTopAnchor   (tela, 0.0);
        AnchorPane.setBottomAnchor(tela, 0.0);
        AnchorPane.setLeftAnchor  (tela, 0.0);
        AnchorPane.setRightAnchor (tela, 0.0);
    }

    private String formatarTamanho(long bytes) {
        if (bytes < 1024)        return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}