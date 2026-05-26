package api.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Historico {

    private int           idHistorico;
    private String        entidadeTipo;
    private String        acao;
    private Integer       entidadeId;
    private String        descricao;
    private int           idUsuario;
    private String        nomeUsuario;   // ← novo campo (JOIN com tb_usuario)
    private LocalDateTime data;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Getters / Setters ─────────────────────────────────────

    public int           getIdHistorico()  { return idHistorico; }
    public void          setIdHistorico(int idHistorico) { this.idHistorico = idHistorico; }

    public String        getEntidadeTipo() { return entidadeTipo; }
    public void          setEntidadeTipo(String entidadeTipo) { this.entidadeTipo = entidadeTipo; }

    public String        getAcao()         { return acao; }
    public void          setAcao(String acao) { this.acao = acao; }

    public Integer       getEntidadeId()   { return entidadeId; }
    public void          setEntidadeId(Integer entidadeId) { this.entidadeId = entidadeId; }

    public String        getDescricao()    { return descricao; }
    public void          setDescricao(String descricao) { this.descricao = descricao; }

    public int           getIdUsuario()    { return idUsuario; }
    public void          setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String        getNomeUsuario()  { return nomeUsuario != null ? nomeUsuario : "—"; }
    public void          setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public LocalDateTime getData()         { return data; }
    public void          setData(LocalDateTime data) { this.data = data; }

    public String        getDataFormatada() {
        return data != null ? data.format(FMT) : "—";
    }

    public String        getEntidadeIdStr() {
        return entidadeId != null ? String.valueOf(entidadeId) : "—";
    }
}