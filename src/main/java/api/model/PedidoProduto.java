package api.model;

import javafx.beans.property.*;

public class PedidoProduto {

    private final IntegerProperty idPedidoProduto;
    private final IntegerProperty idPedido;
    private final ObjectProperty<Produto> produto;
    private final StringProperty nomeProduto;
    private final StringProperty unidadeProduto;
    private final IntegerProperty qtdSolicitada;
    private final IntegerProperty qtdAprovada;
    private final IntegerProperty qtdRecebida;
    private final DoubleProperty  qtdComprada  = new SimpleDoubleProperty(0);
    private final DoubleProperty  valorUnitario = new SimpleDoubleProperty(0);

    // ── Construtor completo com Produto (usado ao criar novo pedido) ──
    public PedidoProduto(int idPedidoProduto, int idPedido,
                         Produto produto, int qtdSolicitada,
                         int qtdAprovada, int qtdRecebida) {
        this.idPedidoProduto = new SimpleIntegerProperty(idPedidoProduto);
        this.idPedido        = new SimpleIntegerProperty(idPedido);
        this.produto         = new SimpleObjectProperty<>(produto);
        this.nomeProduto     = new SimpleStringProperty(
                produto != null ? produto.getProduto() : "");
        this.unidadeProduto  = new SimpleStringProperty(
                produto != null ? produto.getUnidadeMedida() : "");
        this.qtdSolicitada   = new SimpleIntegerProperty(qtdSolicitada);
        this.qtdAprovada     = new SimpleIntegerProperty(qtdAprovada);
        this.qtdRecebida     = new SimpleIntegerProperty(qtdRecebida);
        this.valorUnitario.set(produto != null ? produto.getValorEstimado() : 0.0);
    }

    // ── Construtor "plano" (usado pelo pedidoDAO.listarItens via SQL direto) ──
    public PedidoProduto(int idPedidoProduto, String nomeProduto, String unidade,
                         int qtdSolicitada, int qtdAprovada, double valorUnitario) {
        this.idPedidoProduto = new SimpleIntegerProperty(idPedidoProduto);
        this.idPedido        = new SimpleIntegerProperty(0);
        this.produto         = new SimpleObjectProperty<>(null);
        this.nomeProduto     = new SimpleStringProperty(nomeProduto);
        this.unidadeProduto  = new SimpleStringProperty(unidade);
        this.qtdSolicitada   = new SimpleIntegerProperty(qtdSolicitada);
        this.qtdAprovada     = new SimpleIntegerProperty(qtdAprovada);
        this.qtdRecebida     = new SimpleIntegerProperty(0);
        this.valorUnitario.set(valorUnitario);
    }

    // ── Construtor mínimo (novo item de pedido) ────────────────────────────────
    public PedidoProduto(Produto produto, int qtdSolicitada) {
        this(0, 0, produto, qtdSolicitada, 0, 0);
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int     getIdPedidoProduto() { return idPedidoProduto.get(); }
    public int     getIdPedido()        { return idPedido.get(); }
    public Produto getProduto()         { return produto.get(); }
    public int     getQtdSolicitada()   { return qtdSolicitada.get(); }
    public int     getQtdAprovada()     { return qtdAprovada.get(); }
    public int     getQtdRecebida()     { return qtdRecebida.get(); }
    public double  getQtdComprada()     { return qtdComprada.get(); }
    public double  getQtdPendente()     { return getQtdAprovada() - getQtdComprada(); }

    // ── Auxiliares para TableView / DAO ──────────────────────────────────────
    public String getNomeProduto() {
        String n = nomeProduto.get();
        if (n != null && !n.isBlank()) return n;
        return produto.get() != null ? produto.get().getProduto() : "";
    }

    public String getUnidadeProduto() {
        String u = unidadeProduto.get();
        if (u != null && !u.isBlank()) return u;
        return produto.get() != null ? produto.get().getUnidadeMedida() : "";
    }

    public double getValorUnitario() {
        double v = valorUnitario.get();
        if (v > 0) return v;
        return produto.get() != null ? produto.get().getValorEstimado() : 0.0;
    }

    public double getValorTotal() {
        return getValorUnitario() * qtdSolicitada.get();
    }

    public boolean podeComprar(double qtd) {
        return qtd > 0 && qtd <= getQtdPendente();
    }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setQtdSolicitada(int qtd) { qtdSolicitada.set(qtd); }
    public void setQtdAprovada(int qtd)   { qtdAprovada.set(qtd); }
    public void setQtdComprada(double qtd){ qtdComprada.set(qtd); }

    // ── Properties ───────────────────────────────────────────────────────────
    public IntegerProperty         idPedidoProdutoProperty() { return idPedidoProduto; }
    public IntegerProperty         idPedidoProperty()        { return idPedido; }
    public ObjectProperty<Produto> produtoProperty()         { return produto; }
    public IntegerProperty         qtdSolicitadaProperty()   { return qtdSolicitada; }
    public IntegerProperty         qtdAprovadaProperty()     { return qtdAprovada; }
    public IntegerProperty         qtdRecebidaProperty()     { return qtdRecebida; }
    public DoubleProperty          qtdCompradaProperty()     { return qtdComprada; }
    public DoubleProperty          valorUnitarioProperty()   { return valorUnitario; }
}