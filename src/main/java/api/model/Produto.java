package api.model;

import javafx.beans.property.*;

public class Produto {

    private final IntegerProperty idProduto;
    private final StringProperty  produto;
    private final StringProperty  descricao;
    private final StringProperty  unidadeMedida;
    private final IntegerProperty nivelMinimo;
    private final DoubleProperty  valorEstimado;
    private final StringProperty  status;
    private final IntegerProperty saldo;

    // Construtor para busca do banco (com ID)
    public Produto(int idProduto, String produto, String descricao,
                   String unidadeMedida, int nivelMinimo, double valorEstimado,
                   String status, int saldo) {
        this.idProduto     = new SimpleIntegerProperty(idProduto);
        this.produto       = new SimpleStringProperty(produto);
        this.descricao     = new SimpleStringProperty(descricao);
        this.unidadeMedida = new SimpleStringProperty(unidadeMedida);
        this.nivelMinimo   = new SimpleIntegerProperty(nivelMinimo);
        this.valorEstimado = new SimpleDoubleProperty(valorEstimado);
        this.status        = new SimpleStringProperty(status);
        this.saldo         = new SimpleIntegerProperty(saldo);
    }

    // Construtor para inserção (sem ID, banco gera automaticamente)
    public Produto(String produto, String descricao, String unidadeMedida,
                   int nivelMinimo, double valorEstimado, String status, int saldo) {
        this(0, produto, descricao, unidadeMedida, nivelMinimo, valorEstimado, status, saldo);
    }

    // Getters
    public int    getIdProduto()     { return idProduto.get(); }
    public String getProduto()       { return produto.get(); }
    public String getDescricao()     { return descricao.get(); }
    public String getUnidadeMedida() { return unidadeMedida.get(); }
    public int    getNivelMinimo()   { return nivelMinimo.get(); }
    public double getValorEstimado() { return valorEstimado.get(); }
    public String getStatus()        { return status.get(); }
    public int    getSaldo()         { return saldo.get(); }

    // Properties
    public IntegerProperty idProdutoProperty()     { return idProduto; }
    public StringProperty  produtoProperty()       { return produto; }
    public StringProperty  descricaoProperty()     { return descricao; }
    public StringProperty  unidadeMedidaProperty() { return unidadeMedida; }
    public IntegerProperty nivelMinimoProperty()   { return nivelMinimo; }
    public DoubleProperty  valorEstimadoProperty() { return valorEstimado; }
    public StringProperty  statusProperty()        { return status; }
    public IntegerProperty saldoProperty()         { return saldo; }
}