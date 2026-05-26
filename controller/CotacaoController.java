package api.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import api.model.*;
import api.DAO.CotacaoDAO;

public class CotacaoController {

    @FXML private TextField txtFornecedor;
    @FXML private TextField txtValor;
    @FXML private DatePicker dpDataCotacao;
    @FXML private TextArea txtObservacao;
    @FXML private Label lblArquivoAnexado;

    private Pedido pedidoVinculado; 
    private File arquivoArquivo;
    private final CotacaoDAO cotacaoDAO = new CotacaoDAO();

    /**
     * Este método deve ser chamado pela tela anterior.
     */
    public void setPedido(Pedido pedido) {
        this.pedidoVinculado = pedido;
    }

    @FXML
    public void initialize() {
        dpDataCotacao.setValue(LocalDate.now());
    }

    @FXML
    private void handleAnexarDocumento() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CA2 - Selecionar Documento");
        File file = fileChooser.showOpenDialog(txtFornecedor.getScene().getWindow());
        
        if (file != null) {
            this.arquivoArquivo = file;
            lblArquivoAnexado.setText(file.getName());
        }
    }

    @FXML
    private void handleRegistrarCotacao() {
        // Validação de segurança
        if (pedidoVinculado == null) {
            exibirAlerta("Erro", "Nenhum pedido vinculado a esta cotação.");
            return;
        }

        if (txtFornecedor.getText().isEmpty() || txtValor.getText().isEmpty()) {
            exibirAlerta("Validação", "Fornecedor e Valor são obrigatórios.");
            return;
        }

        try {
            // 1. Preparar dados (CA2)
            double valorTotal = Double.parseDouble(txtValor.getText().replace(",", "."));
            LocalDate dataSelecionada = dpDataCotacao.getValue() == null
                    ? LocalDate.now()
                    : dpDataCotacao.getValue();
            LocalDateTime dataCriacao = LocalDateTime.of(dataSelecionada, LocalTime.now());
            
         // Ordem: nome, cnpj, tipoPagamento, pedidoMinimo, status
         Fornecedor fornecedor = new Fornecedor(
             txtFornecedor.getText(), // Nome vindo da tela
             "00.000.000/0000-00",    // CNPJ (vazio por enquanto)
             "A DEFINIR",             // Tipo Pagamento
             0.0,                     // Pedido Mínimo
             "ATIVO"                  // Status
         );

            Anexo anexo = (arquivoArquivo != null)
                    ? new Anexo("COTACAO", arquivoArquivo.getName(), arquivoArquivo.getAbsolutePath())
                    : null;

            // 2. Criar objeto de modelo (CA5)
            Cotacao novaCotacao = new Cotacao(
                pedidoVinculado, 
                fornecedor, 
                dataCriacao, 
                valorTotal, 
                anexo
            );

            // 3. Regras de Negócio (CA3 e CA4)
            //pedidoVinculado.setStatus("EM_COTACAO");
            pedidoVinculado.statusProperty().set("EM_COTACAO");
            enviarNotificacoes(novaCotacao);

            // 4. Persistência Real via DAO
            //cotacaoDAO.salvar(novaCotacao); 

            exibirAlerta("Sucesso", "Cotação registrada e enviada para aprovação da diretoria.");
            limparCampos();

        } catch (NumberFormatException e) {
            exibirAlerta("Erro", "Formato de valor inválido.");
        } catch (Exception e) {
            exibirAlerta("Erro Crítico", "Erro ao conectar com o banco: " + e.getMessage());
        }
    }

    private void enviarNotificacoes(Cotacao cotacao) {
        // CA4 - Lógica de disparo de email/notificação interna
        System.out.println("Notificando diretor sobre a cotação do pedido: " + pedidoVinculado.getNumPedido());
    }

    private void exibirAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void limparCampos() {
        txtFornecedor.clear();
        txtValor.clear();
        txtObservacao.clear();
        lblArquivoAnexado.setText("Nenhum arquivo anexado");
        arquivoArquivo = null;
    }
}
