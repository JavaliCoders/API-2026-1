package api.model;

import javafx.beans.property.*;

public class PedidoProduto {

    private final IntegerProperty idPedidoProduto;
    private final IntegerProperty idPedido;
    private final ObjectProperty<Produto> produto;
    private final IntegerProperty qtdSolicitada;
    private final IntegerProperty qtdAprovada;
    private final IntegerProperty qtdRecebida;

    public PedidoProduto(int idPedidoProduto, int idPedido,
                         Produto produto, int qtdSolicitada,
                         int qtdAprovada, int qtdRecebida) {
        this.idPedidoProduto = new SimpleIntegerProperty(idPedidoProduto);
        this.idPedido        = new SimpleIntegerProperty(idPedido);
        this.produto         = new SimpleObjectProperty<>(produto);
        this.qtdSolicitada   = new SimpleIntegerProperty(qtdSolicitada);
        this.qtdAprovada     = new SimpleIntegerProperty(qtdAprovada);
        this.qtdRecebida     = new SimpleIntegerProperty(qtdRecebida);
    }

    // Construtor para novo item
    public PedidoProduto(Produto produto, int qtdSolicitada) {
        this(0, 0, produto, qtdSolicitada, 0, 0);
    }

    // Getters
    public int     getIdPedidoProduto() { return idPedidoProduto.get(); }
    public int     getIdPedido()        { return idPedido.get(); }
    public Produto getProduto()         { return produto.get(); }
    public int     getQtdSolicitada()   { return qtdSolicitada.get(); }
    public int     getQtdAprovada()     { return qtdAprovada.get(); }
    public int     getQtdRecebida()     { return qtdRecebida.get(); }

    // Getters auxiliares para TableView
    public String getNomeProduto() {
        return produto.get() != null ? produto.get().getProduto() : "";
    }
    public String getUnidadeProduto() {
        return produto.get() != null ? produto.get().getUnidadeMedida() : "";
    }
    public double getValorUnitario() {
        return produto.get() != null ? produto.get().getValorEstimado() : 0.0;
    }
    public double getValorTotal() {
        return getValorUnitario() * qtdSolicitada.get();
    }

    // Setters
    public void setQtdSolicitada(int qtd) { qtdSolicitada.set(qtd); }

    // Properties
    public IntegerProperty             idPedidoProdutoProperty() { return idPedidoProduto; }
    public IntegerProperty             idPedidoProperty()        { return idPedido; }
    public ObjectProperty<Produto>     produtoProperty()         { return produto; }
    public IntegerProperty             qtdSolicitadaProperty()   { return qtdSolicitada; }
    public IntegerProperty             qtdAprovadaProperty()     { return qtdAprovada; }
    public IntegerProperty             qtdRecebidaProperty()     { return qtdRecebida; }
}