package api.model;

import javafx.beans.property.*;

public class CompraItem {

    private final IntegerProperty               idCompraItem;
    private final IntegerProperty               idCompra;
    private final ObjectProperty<PedidoProduto> pedidoProduto;
    private final DoubleProperty                valorUni;
    private final DoubleProperty                qtdComprada;
    private final DoubleProperty                valorTotal;

    public CompraItem(int idCompraItem, int idCompra, PedidoProduto pedidoProduto,
                      double valorUni, double qtdComprada, double valorTotal) {
        this.idCompraItem  = new SimpleIntegerProperty(idCompraItem);
        this.idCompra      = new SimpleIntegerProperty(idCompra);
        this.pedidoProduto = new SimpleObjectProperty<>(pedidoProduto);
        this.valorUni      = new SimpleDoubleProperty(valorUni);
        this.qtdComprada   = new SimpleDoubleProperty(qtdComprada);
        this.valorTotal    = new SimpleDoubleProperty(valorTotal);
    }

    // Construtor para novo item de compra
    public CompraItem(PedidoProduto pedidoProduto, double valorUni, double qtdComprada) {
        this(0, 0, pedidoProduto, valorUni, qtdComprada, valorUni * qtdComprada);
    }

    // Getters
    public int           getIdCompraItem()  { return idCompraItem.get(); }
    public int           getIdCompra()      { return idCompra.get(); }
    public PedidoProduto getPedidoProduto() { return pedidoProduto.get(); }
    public double        getValorUni()      { return valorUni.get(); }
    public double        getQtdComprada()   { return qtdComprada.get(); }
    public double        getValorTotal()    { return valorTotal.get(); }

    // Getters auxiliares para TableView
    public String getNomeProduto() {
        return pedidoProduto.get() != null ? pedidoProduto.get().getNomeProduto()    : "";
    }
    public String getUnidade() {
        return pedidoProduto.get() != null ? pedidoProduto.get().getUnidadeProduto() : "";
    }

    // Properties
    public IntegerProperty               idCompraItemProperty()  { return idCompraItem; }
    public IntegerProperty               idCompraProperty()      { return idCompra; }
    public ObjectProperty<PedidoProduto> pedidoProdutoProperty() { return pedidoProduto; }
    public DoubleProperty                valorUniProperty()      { return valorUni; }
    public DoubleProperty                qtdCompradaProperty()   { return qtdComprada; }
    public DoubleProperty                valorTotalProperty()    { return valorTotal; }
}
