package api.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Pedido {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IntegerProperty               idPedido;
    private final StringProperty                numPedido;
    private final ObjectProperty<LocalDateTime> dataAbertura;
    private final StringProperty                status;
    private final DoubleProperty                valorTotalEstimado;
    private final ObjectProperty<Usuario>       solicitante;
    private final ObjectProperty<CentroCusto>   centroCusto;
    private final ObjectProperty<Setor>         setor;

    // Campos de aprovação
    private Usuario       aprovador;
    private LocalDateTime dataAprovacao;
    private String        parecer;

    // ── Construtor completo ───────────────────────────────────────────────────
    public Pedido(int idPedido, String numPedido, LocalDateTime dataAbertura,
                  String status, double valorTotalEstimado,
                  Usuario solicitante, CentroCusto centroCusto, Setor setor) {
        this.idPedido           = new SimpleIntegerProperty(idPedido);
        this.numPedido          = new SimpleStringProperty(numPedido);
        this.dataAbertura       = new SimpleObjectProperty<>(dataAbertura);
        this.status             = new SimpleStringProperty(status);
        this.valorTotalEstimado = new SimpleDoubleProperty(valorTotalEstimado);
        this.solicitante        = new SimpleObjectProperty<>(solicitante);
        this.centroCusto        = new SimpleObjectProperty<>(centroCusto);
        this.setor              = new SimpleObjectProperty<>(setor);
    }

    // ── Construtor para novo pedido ───────────────────────────────────────────
    public Pedido(String numPedido, LocalDateTime dataAbertura,
                  double valorTotalEstimado,
                  Usuario solicitante, CentroCusto centroCusto, Setor setor) {
        this(0, numPedido, dataAbertura, "EM_APROVACAO",
                valorTotalEstimado, solicitante, centroCusto, setor);
    }

    // ── Construtor sem args — usado no compraDAO para montar objetos parciais ─
    public Pedido() {
        this(0, "", null, "", 0, null, null, null);
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public int           getIdPedido()          { return idPedido.get(); }
    public String        getNumPedido()          { return numPedido.get(); }
    public LocalDateTime getDataAbertura()       { return dataAbertura.get(); }
    public String        getStatus()             { return status.get(); }
    public double        getValorTotalEstimado() { return valorTotalEstimado.get(); }
    public Usuario       getSolicitante()        { return solicitante.get(); }
    public CentroCusto   getCentroCusto()        { return centroCusto.get(); }
    public Setor         getSetor()              { return setor.get(); }

    // ── Getters/Setters de aprovação ──────────────────────────────────────────
    public Usuario        getAprovador()      { return aprovador; }
    public void           setAprovador(Usuario aprovador) { this.aprovador = aprovador; }

    public LocalDateTime  getDataAprovacao()  { return dataAprovacao; }
    public void           setDataAprovacao(LocalDateTime dataAprovacao) { this.dataAprovacao = dataAprovacao; }

    public String         getParecer()        { return parecer; }
    public void           setParecer(String parecer) { this.parecer = parecer; }

    // ── Setters simples via Property — usados no compraDAO.listarTodas/listarPorPedido ──
    public void setNumPedidoSimples(String num) { this.numPedido.set(num); }
    public void setIdPedidoSimples(int id)      { this.idPedido.set(id); }
    public void setStatus(String status)        { this.status.set(status); }

    // ── Getters auxiliares para TableView ─────────────────────────────────────
    public String getDataAberturaFormatada() {
        return dataAbertura.get() != null ? dataAbertura.get().format(FORMATTER) : "";
    }
    public String getNomeSolicitante() {
        return solicitante.get() != null ? solicitante.get().getNome() : "";
    }
    public String getNomeCentroCusto() {
        return centroCusto.get() != null ? centroCusto.get().getCentroCusto() : "";
    }
    public String getNomeSetor() {
        return setor.get() != null ? setor.get().getSetor() : "";
    }

    // ── Properties ───────────────────────────────────────────────────────────
    public IntegerProperty               idPedidoProperty()           { return idPedido; }
    public StringProperty                numPedidoProperty()          { return numPedido; }
    public ObjectProperty<LocalDateTime> dataAberturaProperty()       { return dataAbertura; }
    public StringProperty                statusProperty()             { return status; }
    public DoubleProperty                valorTotalEstimadoProperty() { return valorTotalEstimado; }
    public ObjectProperty<Usuario>       solicitanteProperty()        { return solicitante; }
    public ObjectProperty<CentroCusto>   centroCustoProperty()        { return centroCusto; }
    public ObjectProperty<Setor>         setorProperty()              { return setor; }
}