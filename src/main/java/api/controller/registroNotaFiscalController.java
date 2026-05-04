package api.controller;

import api.DAO.compraDAO;
import api.DAO.notaFiscalDAO;
import api.model.*;
import api.service.HistoricoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class registroNotaFiscalController implements Initializable {

    @FXML private TextField   fieldNumeroNota;
    @FXML private DatePicker  fieldDataEmissao;
    @FXML private TextField   fieldValorTotal;
    @FXML private TextField   fieldTotalItens;
    @FXML private ComboBox<String> comboCompra;
    @FXML private Label       labelAnexo;
    @FXML private Button      btnAnexar;
    @FXML private Button      btnSalvar;
    @FXML private Button      btnCancelar;
    @FXML private Label       labelErro;

    private AnchorPane areaPrincipal;
    private File       arquivoAnexo;

    // Cache de compras disponíveis (REALIZADA sem NF ainda — ou com NF para referência)
    private javafx.collections.ObservableList<Compra> comprasDisponiveis;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        carregarCompras();
        labelErro.setVisible(false);
        labelErro.setManaged(false);

        // Máscara simples: aceita apenas números com vírgula no valor
        fieldValorTotal.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("[0-9]*[,.]?[0-9]*")) fieldValorTotal.setText(o);
        });
        fieldTotalItens.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("[0-9]*")) fieldTotalItens.setText(o);
        });
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    // ── Carregar compras ──────────────────────────────────────
    private void carregarCompras() {
        comprasDisponiveis = compraDAO.listarRealizadasSemNota();
        javafx.collections.ObservableList<String> labels = FXCollections.observableArrayList();
        for (Compra c : comprasDisponiveis) {
            labels.add(c.getNumPedido() + " — " + c.getNomeFornecedor()
                    + " — R$ " + String.format("%.2f", c.getValorTotal()).replace(".", ","));
        }
        comboCompra.setItems(labels);
    }

    // ── Anexar arquivo ────────────────────────────────────────
    @FXML private void onAnexar() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecionar Nota Fiscal");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF / Imagens", "*.pdf", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Todos os arquivos", "*.*")
        );
        File file = chooser.showOpenDialog(btnAnexar.getScene().getWindow());
        if (file != null) {
            arquivoAnexo = file;
            labelAnexo.setText(file.getName());
            labelAnexo.setStyle("-fx-text-fill: #166534;");
        }
    }

    // ── Salvar ────────────────────────────────────────────────
    @FXML private void onSalvar() {
        if (!validar()) return;

        int idxCompra = comboCompra.getSelectionModel().getSelectedIndex();
        Compra compraSelecionada = comprasDisponiveis.get(idxCompra);

        double valor = Double.parseDouble(
                fieldValorTotal.getText().replace(",", "."));
        int totalItens = Integer.parseInt(fieldTotalItens.getText().trim());
        LocalDateTime dataEmissao = fieldDataEmissao.getValue().atStartOfDay();

        String caminhoAnexo = arquivoAnexo != null ? arquivoAnexo.getAbsolutePath() : "";
        String nomeAnexo    = arquivoAnexo != null ? arquivoAnexo.getName() : "";

        Usuario usuarioLogado = new Usuario(
                SessaoUsuario.getInstancia().getIdUsuarioLogado(),
                SessaoUsuario.getInstancia().getNomeUsuarioLogado(),
                "", "", "", "ATIVO", new Perfil());

        NotaFiscal nf = new NotaFiscal(
                0, fieldNumeroNota.getText().trim(), dataEmissao,
                LocalDateTime.now(), usuarioLogado, compraSelecionada,
                valor, "REGISTRADA", totalItens,
                caminhoAnexo, nomeAnexo, null, null);

        int idNota = notaFiscalDAO.inserir(nf);
        if (idNota == -1) {
            mostrarErro("Erro ao registrar a nota fiscal. Verifique os dados.");
            return;
        }

        // Histórico
        HistoricoService.registrar("Nota fiscal", "Cadastro", idNota,
                "Nota " + nf.getNumeroNota() + " registrada por "
                        + SessaoUsuario.getInstancia().getNomeUsuarioLogado());

        new Alert(Alert.AlertType.INFORMATION,
                "Nota fiscal registrada com sucesso!").showAndWait();
        voltarParaNotas();
    }

    @FXML private void onCancelar() {
        voltarParaNotas();
    }

    // ── Validações ────────────────────────────────────────────
    private boolean validar() {
        if (fieldNumeroNota.getText().isBlank()) {
            mostrarErro("Informe o número da nota fiscal."); return false;
        }
        if (fieldDataEmissao.getValue() == null) {
            mostrarErro("Informe a data de emissão."); return false;
        }
        if (fieldDataEmissao.getValue().isAfter(LocalDate.now())) {
            mostrarErro("A data de emissão não pode ser futura."); return false;
        }
        if (fieldValorTotal.getText().isBlank()) {
            mostrarErro("Informe o valor total da nota."); return false;
        }
        try { Double.parseDouble(fieldValorTotal.getText().replace(",", ".")); }
        catch (NumberFormatException e) { mostrarErro("Valor inválido."); return false; }

        if (fieldTotalItens.getText().isBlank()) {
            mostrarErro("Informe o total de itens."); return false;
        }
        if (comboCompra.getSelectionModel().getSelectedIndex() < 0) {
            mostrarErro("Selecione a compra relacionada."); return false;
        }
        labelErro.setVisible(false);
        labelErro.setManaged(false);
        return true;
    }

    private void mostrarErro(String msg) {
        labelErro.setText("⚠  " + msg);
        labelErro.setVisible(true);
        labelErro.setManaged(true);
    }

    // ── Navegação ─────────────────────────────────────────────
    private void voltarParaNotas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/notaFiscal.fxml"));
            Node tela = loader.load();
            notaFiscalController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void anchorar(Node tela) {
        AnchorPane.setTopAnchor   (tela, 0.0); AnchorPane.setBottomAnchor(tela, 0.0);
        AnchorPane.setLeftAnchor  (tela, 0.0); AnchorPane.setRightAnchor (tela, 0.0);
    }
}