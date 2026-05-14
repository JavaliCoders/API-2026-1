package api.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotaFiscal {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final IntegerProperty idNota;
    private final StringProperty  numeroNota;
    private final ObjectProperty<LocalDateTime> dataEmissao;
    private final ObjectProperty<LocalDateTime> dataRegistro;
    private final ObjectProperty<Usuario>       usuarioRegistro;
    private final ObjectProperty<Compra>        compra;
    private final DoubleProperty  valorNf;
    private final StringProperty  status;
    private final IntegerProperty totalItens;
    // anexo simplificado: só o caminho
    private final StringProperty  caminhoAnexo;
    private final StringProperty  nomeAnexo;

    // Aprovação/conferência
    private final ObjectProperty<Usuario>       usuarioConferencia;
    private final ObjectProperty<LocalDateTime> dataConferencia;

    public NotaFiscal(int idNota, String numeroNota, LocalDateTime dataEmissao,
                      LocalDateTime dataRegistro, Usuario usuarioRegistro,
                      Compra compra, double valorNf, String status, int totalItens,
                      String caminhoAnexo, String nomeAnexo,
                      Usuario usuarioConferencia, LocalDateTime dataConferencia) {
        this.idNota             = new SimpleIntegerProperty(idNota);
        this.numeroNota         = new SimpleStringProperty(numeroNota);
        this.dataEmissao        = new SimpleObjectProperty<>(dataEmissao);
        this.dataRegistro       = new SimpleObjectProperty<>(dataRegistro);
        this.usuarioRegistro    = new SimpleObjectProperty<>(usuarioRegistro);
        this.compra             = new SimpleObjectProperty<>(compra);
        this.valorNf            = new SimpleDoubleProperty(valorNf);
        this.status             = new SimpleStringProperty(status);
        this.totalItens         = new SimpleIntegerProperty(totalItens);
        this.caminhoAnexo       = new SimpleStringProperty(caminhoAnexo);
        this.nomeAnexo          = new SimpleStringProperty(nomeAnexo);
        this.usuarioConferencia = new SimpleObjectProperty<>(usuarioConferencia);
        this.dataConferencia    = new SimpleObjectProperty<>(dataConferencia);
    }

    // ── Getters ───────────────────────────────────────────────
    public int           getIdNota()              { return idNota.get(); }
    public String        getNumeroNota()          { return numeroNota.get(); }
    public LocalDateTime getDataEmissao()         { return dataEmissao.get(); }
    public LocalDateTime getDataRegistro()        { return dataRegistro.get(); }
    public Usuario       getUsuarioRegistro()     { return usuarioRegistro.get(); }
    public Compra        getCompra()              { return compra.get(); }
    public double        getValorNf()             { return valorNf.get(); }
    public String        getStatus()              { return status.get(); }
    public int           getTotalItens()          { return totalItens.get(); }
    public String        getCaminhoAnexo()        { return caminhoAnexo.get(); }
    public String        getNomeAnexo()           { return nomeAnexo.get(); }
    public Usuario       getUsuarioConferencia()  { return usuarioConferencia.get(); }
    public LocalDateTime getDataConferencia()     { return dataConferencia.get(); }

    // Formatados para exibição
    public String getDataEmissaoFormatada()  { return dataEmissao.get()  != null ? dataEmissao.get().format(FMT_DATA) : "—"; }
    public String getDataRegistroFormatada() { return dataRegistro.get() != null ? dataRegistro.get().format(FMT) : "—"; }
    public String getNumPedido()             { return compra.get() != null ? compra.get().getNumPedido() : "—"; }
    public String getNomeFornecedor()        { return compra.get() != null ? compra.get().getNomeFornecedor() : "—"; }

    // ── Setters ───────────────────────────────────────────────
    public void setStatus(String s)                          { status.set(s); }
    public void setUsuarioConferencia(Usuario u)             { usuarioConferencia.set(u); }
    public void setDataConferencia(LocalDateTime dt)         { dataConferencia.set(dt); }

    // ── Properties ───────────────────────────────────────────
    public IntegerProperty                   idNotaProperty()              { return idNota; }
    public StringProperty                    numeroNotaProperty()          { return numeroNota; }
    public ObjectProperty<LocalDateTime>     dataEmissaoProperty()         { return dataEmissao; }
    public ObjectProperty<LocalDateTime>     dataRegistroProperty()        { return dataRegistro; }
    public ObjectProperty<Usuario>           usuarioRegistroProperty()     { return usuarioRegistro; }
    public ObjectProperty<Compra>            compraProperty()              { return compra; }
    public DoubleProperty                    valorNfProperty()             { return valorNf; }
    public StringProperty                    statusProperty()              { return status; }
    public IntegerProperty                   totalItensProperty()          { return totalItens; }
    public StringProperty                    caminhoAnexoProperty()        { return caminhoAnexo; }
    public StringProperty                    nomeAnexoProperty()           { return nomeAnexo; }
    public ObjectProperty<Usuario>           usuarioConferenciaProperty()  { return usuarioConferencia; }
    public ObjectProperty<LocalDateTime>     dataConferenciaProperty()     { return dataConferencia; }
}