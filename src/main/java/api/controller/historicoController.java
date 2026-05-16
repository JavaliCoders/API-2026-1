package api.controller;

import api.model.Historico;
import api.DAO.historicoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class historicoController implements Initializable {

    @FXML private DatePicker datePickerFiltro;
    @FXML private ComboBox<String> comboTipoAcao;
    @FXML private ListView<Historico> listViewHistorico;

    private ObservableList<Historico> dadosOriginais = FXCollections.observableArrayList();
    private javafx.scene.layout.AnchorPane areaPrincipal;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarFiltros();
        configurarAparenciaDaLista();
        carregarDadosDoBanco();
    }

    public void setAreaPrincipal(javafx.scene.layout.AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    private void configurarFiltros() {
        comboTipoAcao.setItems(FXCollections.observableArrayList(
                "Todos", 
                "Pedido", 
                "Cotacao", 
                "NotaFiscal",
                "Material"
        ));
        comboTipoAcao.getSelectionModel().selectFirst();
    }

    private void configurarAparenciaDaLista() {
        listViewHistorico.setCellFactory(param -> new ListCell<Historico>() {
            @Override
            protected void updateItem(Historico item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
                    String dataFormatada = item.getData() != null ? item.getData().format(formatter) : "Data não registrada";
                    
                    String textoNotificacao = String.format("[%s] %s - %s\nDetalhe: %s\nID do Usuário responsável: %d",
                            dataFormatada, 
                            item.getEntidadeTipo().toUpperCase(), 
                            item.getAcao().toUpperCase(),
                            item.getDescricao(), 
                            item.getIdUsuario()
                    );

                    setText(textoNotificacao);
                    
                    setStyle("-fx-border-color: #e0e0e0; " +
                             "-fx-border-width: 0 0 1 0; " + 
                             "-fx-padding: 15px 10px; " +
                             "-fx-font-size: 14px;");
                }
            }
        });
    }

    private void carregarDadosDoBanco() {
        /* Quando o back terminar apaga o mock e descomenta aqui
        try {
            dadosOriginais = historicoDAO.listarTodos(); 
            if (dadosOriginais == null) dadosOriginais = FXCollections.observableArrayList();
            listViewHistorico.setItems(dadosOriginais);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

    	// !!!!!!!!!!!!!!!!!
        // ↓TEMPORÁRIO
    	// !!!!!!!!!!!!!!!!!
        Historico h1 = new Historico();
        h1.setIdHistorico(1);
        h1.setEntidadeTipo("Pedido");
        h1.setAcao("Criar");
        h1.setDescricao("Pedido #1024 criado para compra de papel.");
        h1.setIdUsuario(5);
        h1.setData(LocalDateTime.now().minusHours(2));

        Historico h2 = new Historico();
        h2.setIdHistorico(2);
        h2.setEntidadeTipo("Material");
        h2.setAcao("Saida");
        h2.setDescricao("Liberado 50 mouses para o setor de TI.");
        h2.setIdUsuario(2);
        h2.setData(LocalDateTime.now().minusDays(1));

        dadosOriginais.addAll(h1, h2);
        listViewHistorico.setItems(dadosOriginais);
        // !!!!!!!!!!!!!!!!!
        // ↑TEMPORÁRIO
    	// !!!!!!!!!!!!!!!!!
    }

    @FXML
    void onFiltrar(ActionEvent event) {
        LocalDate dataFiltro = datePickerFiltro.getValue();
        String tipoFiltro = comboTipoAcao.getValue();

        ObservableList<Historico> dadosFiltrados = FXCollections.observableArrayList(
            dadosOriginais.stream()
                .filter(h -> {
                    boolean atendeData = true;
                    if (dataFiltro != null && h.getData() != null) {
                        atendeData = h.getData().toLocalDate().equals(dataFiltro);
                    }
                    
                    boolean atendeTipo = (tipoFiltro == null || tipoFiltro.equals("Todos")) 
                                         || (h.getEntidadeTipo() != null && h.getEntidadeTipo().equalsIgnoreCase(tipoFiltro));
                    
                    return atendeData && atendeTipo;
                })
                .collect(Collectors.toList())
        );

        listViewHistorico.setItems(dadosFiltrados);
    }

    @FXML
    void onLimparFiltros(ActionEvent event) {
        datePickerFiltro.setValue(null);
        comboTipoAcao.getSelectionModel().select("Todos");
        listViewHistorico.setItems(dadosOriginais); 
    }
}