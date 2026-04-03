package api.model;

import javafx.beans.property.*;

public class Fornecedor {

    private final IntegerProperty idFornecedor;
    private final StringProperty  nome;
    private final StringProperty  cnpj;
    private final StringProperty  tipoPagamento;
    private final DoubleProperty  pedidoMinimo;
    private final StringProperty  status;

    // Construtor com ID (busca do banco)
    public Fornecedor(int idFornecedor, String nome, String cnpj,
                      String tipoPagamento, double pedidoMinimo, String status) {
        this.idFornecedor  = new SimpleIntegerProperty(idFornecedor);
        this.nome          = new SimpleStringProperty(nome);
        this.cnpj          = new SimpleStringProperty(cnpj);
        this.tipoPagamento = new SimpleStringProperty(tipoPagamento);
        this.pedidoMinimo  = new SimpleDoubleProperty(pedidoMinimo);
        this.status        = new SimpleStringProperty(status);
    }

    // Construtor sem ID (novo cadastro)
    public Fornecedor(String nome, String cnpj, String tipoPagamento,
                      double pedidoMinimo, String status) {
        this(0, nome, cnpj, tipoPagamento, pedidoMinimo, status);
    }

    // Getters
    public int    getIdFornecedor()  { return idFornecedor.get(); }
    public String getNome()          { return nome.get(); }
    public String getCnpj()          { return cnpj.get(); }
    public String getTipoPagamento() { return tipoPagamento.get(); }
    public double getPedidoMinimo()  { return pedidoMinimo.get(); }
    public String getStatus()        { return status.get(); }

    // Properties
    public IntegerProperty idFornecedorProperty()  { return idFornecedor; }
    public StringProperty  nomeProperty()          { return nome; }
    public StringProperty  cnpjProperty()          { return cnpj; }
    public StringProperty  tipoPagamentoProperty() { return tipoPagamento; }
    public DoubleProperty  pedidoMinimoProperty()  { return pedidoMinimo; }
    public StringProperty  statusProperty()        { return status; }
}
