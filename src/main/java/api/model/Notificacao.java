package api.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notificacao {

    private int           idNotificacao;
    private int           idUsuario;
    private String        titulo;
    private String        mensagem;
    private String        entidadeTipo;
    private Integer       entidadeId;
    private boolean       lida;
    private LocalDateTime data;

    // ── Construtor completo (banco) ───────────────────────────
    public Notificacao(int idNotificacao, int idUsuario, String titulo,
                       String mensagem, String entidadeTipo,
                       Integer entidadeId, boolean lida, LocalDateTime data) {
        this.idNotificacao = idNotificacao;
        this.idUsuario     = idUsuario;
        this.titulo        = titulo;
        this.mensagem      = mensagem;
        this.entidadeTipo  = entidadeTipo;
        this.entidadeId    = entidadeId;
        this.lida          = lida;
        this.data          = data;
    }

    // ── Construtor para inserção (sem ID e data — banco gera) ─
    public Notificacao(int idUsuario, String titulo, String mensagem,
                       String entidadeTipo, Integer entidadeId) {
        this(0, idUsuario, titulo, mensagem, entidadeTipo, entidadeId, false, LocalDateTime.now());
    }

    // ── Getters ───────────────────────────────────────────────
    public int           getIdNotificacao() { return idNotificacao; }
    public int           getIdUsuario()     { return idUsuario; }
    public String        getTitulo()        { return titulo; }
    public String        getMensagem()      { return mensagem; }
    public String        getEntidadeTipo()  { return entidadeTipo; }
    public Integer       getEntidadeId()    { return entidadeId; }
    public boolean       isLida()           { return lida; }
    public LocalDateTime getData()          { return data; }

    // ── Setter ────────────────────────────────────────────────
    public void setLida(boolean lida) { this.lida = lida; }

    // ── Utilitário de exibição ────────────────────────────────
    public String getDataFormatada() {
        if (data == null) return "";
        return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}