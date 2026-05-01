package api.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Movimentacao {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IntegerProperty               idMovimentacao;
    private final ObjectProperty<Produto>       produto;
    private final StringProperty                tipo;
    private final IntegerProperty               quantidade;
    private final ObjectProperty<Usuario>       usuario;
    private final ObjectProperty<LocalDateTime> data;
    private final StringProperty                observacao;

    public Movimentacao(int idMovimentacao, Produto produto, String tipo,
                        int quantidade, Usuario usuario,
                        LocalDateTime data, String observacao) {
        this.idMovimentacao = new SimpleIntegerProperty(idMovimentacao);
        this.produto        = new SimpleObjectProperty<>(produto);
        this.tipo           = new SimpleStringProperty(tipo);
        this.quantidade     = new SimpleIntegerProperty(quantidade);
        this.usuario        = new SimpleObjectProperty<>(usuario);
        this.data           = new SimpleObjectProperty<>(data);
        this.observacao     = new SimpleStringProperty(observacao);
    }

    // Construtor para nova movimentação
    public Movimentacao(Produto produto, String tipo, int quantidade,
                        Usuario usuario, String observacao) {
        this(0, produto, tipo, quantidade, usuario, LocalDateTime.now(), observacao);
    }

    // Getters
    public int           getIdMovimentacao() { return idMovimentacao.get(); }
    public Produto       getProduto()        { return produto.get(); }
    public String        getTipo()           { return tipo.get(); }
    public int           getQuantidade()     { return quantidade.get(); }
    public Usuario       getUsuario()        { return usuario.get(); }
    public LocalDateTime getData()           { return data.get(); }
    public String        getObservacao()     { return observacao.get(); }

    // Getters auxiliares para TableView
    public String getNomeProduto()  { return produto.get()  != null ? produto.get().getProduto()   : ""; }
    public String getNomeUsuario()  { return usuario.get()  != null ? usuario.get().getNome()       : ""; }
    public String getDataFormatada(){ return data.get()     != null ? data.get().format(FORMATTER)  : ""; }

    // Properties
    public IntegerProperty               idMovimentacaoProperty() { return idMovimentacao; }
    public ObjectProperty<Produto>       produtoProperty()        { return produto; }
    public StringProperty                tipoProperty()           { return tipo; }
    public IntegerProperty               quantidadeProperty()     { return quantidade; }
    public ObjectProperty<Usuario>       usuarioProperty()        { return usuario; }
    public ObjectProperty<LocalDateTime> dataProperty()           { return data; }
    public StringProperty                observacaoProperty()     { return observacao; }
}
