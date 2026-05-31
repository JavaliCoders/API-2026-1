package api.model;

import javafx.beans.property.*;

public class NfItem {

    private final IntegerProperty idNfItem;
    private final IntegerProperty idPedidoProduto;
    private final StringProperty  nomeProduto;
    private final StringProperty  unidade;
    private final IntegerProperty qtdAprovada;
    private final IntegerProperty qtdRecebida;
    private final IntegerProperty qtdRejeitada;
    private final StringProperty  motivoDivergencia;

    // qtd já comprada (para referência)
    private final IntegerProperty qtdComprada;

    public NfItem(int idNfItem, int idPedidoProduto, String nomeProduto, String unidade,
                  int qtdAprovada, int qtdRecebida, int qtdRejeitada,
                  String motivoDivergencia, int qtdComprada) {
        this.idNfItem          = new SimpleIntegerProperty(idNfItem);
        this.idPedidoProduto   = new SimpleIntegerProperty(idPedidoProduto);
        this.nomeProduto       = new SimpleStringProperty(nomeProduto);
        this.unidade           = new SimpleStringProperty(unidade);
        this.qtdAprovada       = new SimpleIntegerProperty(qtdAprovada);
        this.qtdRecebida       = new SimpleIntegerProperty(qtdRecebida);
        this.qtdRejeitada      = new SimpleIntegerProperty(qtdRejeitada);
        this.motivoDivergencia = new SimpleStringProperty(motivoDivergencia != null ? motivoDivergencia : "");
        this.qtdComprada       = new SimpleIntegerProperty(qtdComprada);
    }

    // Construtor para preenchimento na tela de conferência
    public NfItem(int idPedidoProduto, String nomeProduto, String unidade,
                  int qtdAprovada, int qtdComprada) {
        this(0, idPedidoProduto, nomeProduto, unidade, qtdAprovada, 0, 0, "", qtdComprada);
    }

    // ── Getters ───────────────────────────────────────────────
    public int    getIdNfItem()          { return idNfItem.get(); }
    public int    getIdPedidoProduto()   { return idPedidoProduto.get(); }
    public String getNomeProduto()       { return nomeProduto.get(); }
    public String getUnidade()           { return unidade.get(); }
    public int    getQtdAprovada()       { return qtdAprovada.get(); }
    public int    getQtdRecebida()       { return qtdRecebida.get(); }
    public int    getQtdRejeitada()      { return qtdRejeitada.get(); }
    public String getMotivoDivergencia() { return motivoDivergencia.get(); }
    public int    getQtdComprada()       { return qtdComprada.get(); }
    public boolean temDivergencia()      { return qtdRejeitada.get() > 0; }

    // ── Setters ───────────────────────────────────────────────
    public void setQtdRecebida(int v)       { qtdRecebida.set(v); }
    public void setQtdRejeitada(int v)      { qtdRejeitada.set(v); }
    public void setMotivoDivergencia(String v) { motivoDivergencia.set(v); }

    // ── Properties ───────────────────────────────────────────
    public IntegerProperty idNfItemProperty()          { return idNfItem; }
    public IntegerProperty idPedidoProdutoProperty()   { return idPedidoProduto; }
    public StringProperty  nomeProdutoProperty()       { return nomeProduto; }
    public StringProperty  unidadeProperty()           { return unidade; }
    public IntegerProperty qtdAprovadaProperty()       { return qtdAprovada; }
    public IntegerProperty qtdRecebidaProperty()       { return qtdRecebida; }
    public IntegerProperty qtdRejeitadaProperty()      { return qtdRejeitada; }
    public StringProperty  motivoDivergenciaProperty() { return motivoDivergencia; }
    public IntegerProperty qtdCompradaProperty()       { return qtdComprada; }
}