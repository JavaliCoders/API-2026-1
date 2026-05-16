package api.model;

import javafx.beans.property.*;

public class CompraItem {

    private final IntegerProperty               idCompraItem;
    private final IntegerProperty               idCompra;
    private final ObjectProperty<PedidoProduto> pedidoProduto;
    private final DoubleProperty                valorUni;
    private final DoubleProperty                qtdComprada;
    private final DoubleProperty                valorTotal;

    // ── Construtor completo (usado ao carregar do BD) ─────────────────────────
    public CompraItem(int idCompraItem, int idCompra, PedidoProduto pedidoProduto,
                      double valorUni, double qtdComprada, double valorTotal) {
        this.idCompraItem  = new SimpleIntegerProperty(idCompraItem);
        this.idCompra      = new SimpleIntegerProperty(idCompra);
        this.pedidoProduto = new SimpleObjectProperty<>(pedidoProduto);
        this.valorUni      = new SimpleDoubleProperty(valorUni);
        this.qtdComprada   = new SimpleDoubleProperty(qtdComprada);
        this.valorTotal    = new SimpleDoubleProperty(valorTotal);
    }

    // ── Construtor para novo item (na tela de cadastro) ───────────────────────
    public CompraItem(PedidoProduto pedidoProduto, double valorUni, double qtdComprada) {
        this(0, 0, pedidoProduto, valorUni, qtdComprada, valorUni * qtdComprada);
    }

    // ── Construtor sem args (para uso em controller antes de setar dados) ─────
    public CompraItem() {
        this(0, 0, null, 0, 0, 0);
    }

    // ── Setters com recálculo automático ─────────────────────────────────────
    public void setPedidoProduto(PedidoProduto pp) {
        this.pedidoProduto.set(pp);
    }

    public void setQtdComprada(double qtd) {
        this.qtdComprada.set(qtd);
        recalcularTotal();
    }

    public void setValorUni(double valor) {
        this.valorUni.set(valor);
        recalcularTotal();
    }

    private void recalcularTotal() {
        this.valorTotal.set(this.valorUni.get() * this.qtdComprada.get());
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int           getIdCompraItem()  { return idCompraItem.get(); }
    public int           getIdCompra()      { return idCompra.get(); }
    public PedidoProduto getPedidoProduto() { return pedidoProduto.get(); }
    public double        getValorUni()      { return valorUni.get(); }
    public double        getQtdComprada()   { return qtdComprada.get(); }
    public double        getValorTotal()    { return valorTotal.get(); }

    // ── Auxiliares para TableView ─────────────────────────────────────────────
    public String getNomeProduto() {
        return pedidoProduto.get() != null ? pedidoProduto.get().getNomeProduto() : "";
    }
    public String getUnidade() {
        return pedidoProduto.get() != null ? pedidoProduto.get().getUnidadeProduto() : "";
    }

    // ── Properties ───────────────────────────────────────────────────────────
    public IntegerProperty               idCompraItemProperty()  { return idCompraItem; }
    public IntegerProperty               idCompraProperty()      { return idCompra; }
    public ObjectProperty<PedidoProduto> pedidoProdutoProperty() { return pedidoProduto; }
    public DoubleProperty                valorUniProperty()      { return valorUni; }
    public DoubleProperty                qtdCompradaProperty()   { return qtdComprada; }
    public DoubleProperty                valorTotalProperty()    { return valorTotal; }
}