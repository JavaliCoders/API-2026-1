package api.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Movimentacao {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IntegerProperty            idMovimentacao;
    private final StringProperty             nomeProduto;
    private final StringProperty             tipo;
    private final IntegerProperty            quantidade;
    private final StringProperty             nomeUsuario;
    private final StringProperty             numPedido;
    private final StringProperty             numeroNota;
    private final ObjectProperty<LocalDateTime> data;
    private final StringProperty             observacao;

    public Movimentacao(int idMovimentacao, String nomeProduto, String tipo,
                        int quantidade, String nomeUsuario, String numPedido,
                        String numeroNota, LocalDateTime data, String observacao) {
        this.idMovimentacao = new SimpleIntegerProperty(idMovimentacao);
        this.nomeProduto    = new SimpleStringProperty(nomeProduto);
        this.tipo           = new SimpleStringProperty(tipo);
        this.quantidade     = new SimpleIntegerProperty(quantidade);
        this.nomeUsuario    = new SimpleStringProperty(nomeUsuario);
        this.numPedido      = new SimpleStringProperty(numPedido != null ? numPedido : "—");
        this.numeroNota     = new SimpleStringProperty(numeroNota != null ? numeroNota : "—");
        this.data           = new SimpleObjectProperty<>(data);
        this.observacao     = new SimpleStringProperty(observacao != null ? observacao : "");
    }

    // ── Getters ───────────────────────────────────────────────
    public int           getIdMovimentacao() { return idMovimentacao.get(); }
    public String        getNomeProduto()    { return nomeProduto.get(); }
    public String        getTipo()           { return tipo.get(); }
    public int           getQuantidade()     { return quantidade.get(); }
    public String        getNomeUsuario()    { return nomeUsuario.get(); }
    public String        getNumPedido()      { return numPedido.get(); }
    public String        getNumeroNota()     { return numeroNota.get(); }
    public LocalDateTime getData()           { return data.get(); }
    public String        getObservacao()     { return observacao.get(); }
    public String        getDataFormatada()  { return data.get() != null ? data.get().format(FMT) : "—"; }

    // ── Properties ───────────────────────────────────────────
    public IntegerProperty               idMovimentacaoProperty() { return idMovimentacao; }
    public StringProperty                nomeProdutoProperty()    { return nomeProduto; }
    public StringProperty                tipoProperty()           { return tipo; }
    public IntegerProperty               quantidadeProperty()     { return quantidade; }
    public StringProperty                nomeUsuarioProperty()    { return nomeUsuario; }
    public StringProperty                numPedidoProperty()      { return numPedido; }
    public StringProperty                numeroNotaProperty()     { return numeroNota; }
    public ObjectProperty<LocalDateTime> dataProperty()           { return data; }
    public StringProperty                observacaoProperty()     { return observacao; }
}
