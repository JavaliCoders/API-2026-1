package api.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Cotacao {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final IntegerProperty idCotacao;
    private final ObjectProperty<Pedido> pedido;
    private final ObjectProperty<Fornecedor> fornecedor;
    private final ObjectProperty<Usuario> aprovador;
    private final ObjectProperty<Anexo> anexo;
    private final ObjectProperty<LocalDateTime> dataCriacao;
    private final ObjectProperty<LocalDateTime> dataAprovacao;

    private final StringProperty status;
    private final StringProperty parecer;
    private final DoubleProperty valorTotal;

    // 🔥 CONSTRUTOR COMPLETO
    public Cotacao(int idCotacao, Pedido pedido, Fornecedor fornecedor,
                   Usuario aprovador, Anexo anexo,
                   LocalDateTime dataCriacao, LocalDateTime dataAprovacao,
                   String status, String parecer, double valorTotal) {

        this.idCotacao      = new SimpleIntegerProperty(idCotacao);
        this.pedido         = new SimpleObjectProperty<>(pedido);
        this.fornecedor     = new SimpleObjectProperty<>(fornecedor);
        this.aprovador      = new SimpleObjectProperty<>(aprovador);
        this.anexo          = new SimpleObjectProperty<>(anexo);
        this.dataCriacao    = new SimpleObjectProperty<>(dataCriacao);
        this.dataAprovacao  = new SimpleObjectProperty<>(dataAprovacao);
        this.status         = new SimpleStringProperty(status);
        this.parecer        = new SimpleStringProperty(parecer);
        this.valorTotal     = new SimpleDoubleProperty(valorTotal);
    }

    // 🔥 CONSTRUTOR PARA NOVA COTAÇÃO
    public Cotacao(Pedido pedido, Fornecedor fornecedor,
                   LocalDateTime dataCriacao, double valorTotal, Anexo anexo) {

        this(0, pedido, fornecedor, null, anexo,
                dataCriacao, null,
                "AGUARDANDO_APROVACAO", null, valorTotal);
    }

    // Getters
    public int getIdCotacao() { return idCotacao.get(); }
    public Pedido getPedido() { return pedido.get(); }
    public Fornecedor getFornecedor() { return fornecedor.get(); }
    public Usuario getAprovador() { return aprovador.get(); }
    public Anexo getAnexo() { return anexo.get(); }

    public LocalDateTime getDataCriacao() { return dataCriacao.get(); }
    public LocalDateTime getDataAprovacao() { return dataAprovacao.get(); }

    public String getStatus() { return status.get(); }
    public String getParecer() { return parecer.get(); }
    public double getValorTotal() { return valorTotal.get(); }

    // Getters auxiliares para TableView
    public String getNumPedido() { return pedido.get() != null ? pedido.get().getNumPedido() : ""; }
    public String getNomeFornecedor() { return fornecedor.get() != null ? fornecedor.get().getNome() : ""; }
    public String getNomeAprovador() { return aprovador.get() != null ? aprovador.get().getNome() : ""; }
    public String getDataCriacaoFormatada() { return dataCriacao.get() != null ? dataCriacao.get().format(FORMATTER) : ""; }
    public String getDataAprovacaoFormatada() { return dataAprovacao.get() != null ? dataAprovacao.get().format(FORMATTER) : ""; }

    // Properties
    public IntegerProperty idCotacaoProperty() { return idCotacao; }
    public ObjectProperty<Pedido> pedidoProperty() { return pedido; }
    public ObjectProperty<Fornecedor> fornecedorProperty() { return fornecedor; }
    public ObjectProperty<Usuario> aprovadorProperty() { return aprovador; }
    public ObjectProperty<Anexo> anexoProperty() { return anexo; }

    public ObjectProperty<LocalDateTime> dataCriacaoProperty() { return dataCriacao; }
    public ObjectProperty<LocalDateTime> dataAprovacaoProperty() { return dataAprovacao; }

    public StringProperty statusProperty() { return status; }
    public StringProperty parecerProperty() { return parecer; }
    public DoubleProperty valorTotalProperty() { return valorTotal; }
}