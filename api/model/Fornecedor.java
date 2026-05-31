package api.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Fornecedor {

    private final IntegerProperty idFornecedor;
    private final StringProperty  nome;
    private final StringProperty  cnpj;
    private final DoubleProperty  pedidoMinimo;
    private final StringProperty  status;
    private ObservableList<FormaPagamento> formasPagamento =
            FXCollections.observableArrayList();

    public Fornecedor(int idFornecedor, String nome, String cnpj,
                      double pedidoMinimo, String status) {
        this.idFornecedor = new SimpleIntegerProperty(idFornecedor);
        this.nome         = new SimpleStringProperty(nome);
        this.cnpj         = new SimpleStringProperty(cnpj);
        this.pedidoMinimo = new SimpleDoubleProperty(pedidoMinimo);
        this.status       = new SimpleStringProperty(status);
    }

    public Fornecedor(String nome, String cnpj, double pedidoMinimo, String status) {
        this(0, nome, cnpj, pedidoMinimo, status);
    }

    public int    getIdFornecedor() { return idFornecedor.get(); }
    public String getNome()         { return nome.get(); }
    public String getCnpj()         { return cnpj.get(); }
    public double getPedidoMinimo() { return pedidoMinimo.get(); }
    public String getStatus()       { return status.get(); }

    public ObservableList<FormaPagamento> getFormasPagamento() { return formasPagamento; }
    public void setFormasPagamento(ObservableList<FormaPagamento> formas) {
        this.formasPagamento = formas;
    }

    // Retorna string formatada para exibir na tabela/detalhes
    public String getFormasPagamentoTexto() {
        if (formasPagamento == null || formasPagamento.isEmpty()) return "-";
        return formasPagamento.stream()
                .map(FormaPagamento::getForma)
                .reduce((a, b) -> a + ", " + b)
                .orElse("-");
    }

    public IntegerProperty idFornecedorProperty() { return idFornecedor; }
    public StringProperty  nomeProperty()         { return nome; }
    public StringProperty  cnpjProperty()         { return cnpj; }
    public DoubleProperty  pedidoMinimoProperty() { return pedidoMinimo; }
    public StringProperty  statusProperty()       { return status; }
}