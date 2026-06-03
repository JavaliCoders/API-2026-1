package api.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Cotacao {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int           idCotacao;
    private String        status;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAprovacao;
    private String        parecer;
    private Usuario       aprovador;
    private double        valorTotal;
    private int           idPedido;
    private String        numPedido;
    private Fornecedor    fornecedor;
    private int           idAnexo;
    private String        nomeAnexo;
    private String        caminhoAnexo;
    private int           idCadastrador;
    private String        nomeRegistradoPor; // ← NOVO

    public Cotacao(int idCotacao, String status, LocalDateTime dataCriacao,
                   LocalDateTime dataAprovacao, String parecer, Usuario aprovador,
                   double valorTotal, int idPedido, String numPedido,
                   Fornecedor fornecedor, int idAnexo,
                   String nomeAnexo, String caminhoAnexo,
                   int idCadastrador, String nomeRegistradoPor) { // ← NOVO
        this.idCotacao        = idCotacao;
        this.status           = status;
        this.dataCriacao      = dataCriacao;
        this.dataAprovacao    = dataAprovacao;
        this.parecer          = parecer;
        this.aprovador        = aprovador;
        this.valorTotal       = valorTotal;
        this.idPedido         = idPedido;
        this.numPedido        = numPedido;
        this.fornecedor       = fornecedor;
        this.idAnexo          = idAnexo;
        this.nomeAnexo        = nomeAnexo;
        this.caminhoAnexo     = caminhoAnexo;
        this.idCadastrador    = idCadastrador;
        this.nomeRegistradoPor = nomeRegistradoPor; // ← NOVO
    }

    public int           getIdCotacao()        { return idCotacao; }
    public String        getStatus()           { return status; }
    public LocalDateTime getDataCriacao()      { return dataCriacao; }
    public LocalDateTime getDataAprovacao()    { return dataAprovacao; }
    public String        getParecer()          { return parecer; }
    public Usuario       getAprovador()        { return aprovador; }
    public double        getValorTotal()       { return valorTotal; }
    public int           getIdPedido()         { return idPedido; }
    public String        getNumPedido()        { return numPedido; }
    public Fornecedor    getFornecedor()       { return fornecedor; }
    public int           getIdAnexo()          { return idAnexo; }
    public String        getNomeAnexo()        { return nomeAnexo; }
    public String        getCaminhoAnexo()     { return caminhoAnexo; }
    public int           getIdCadastrador()    { return idCadastrador; }
    public String        getNomeRegistradoPor(){ return nomeRegistradoPor != null ? nomeRegistradoPor : "—"; } // ← NOVO

    public void setStatus(String s)               { this.status = s; }
    public void setDataAprovacao(LocalDateTime d)  { this.dataAprovacao = d; }
    public void setParecer(String p)              { this.parecer = p; }
    public void setAprovador(Usuario u)           { this.aprovador = u; }

    public String  getDataCriacaoFormatada()   { return dataCriacao   != null ? dataCriacao.format(FMT)   : ""; }
    public String  getDataAprovacaoFormatada() { return dataAprovacao != null ? dataAprovacao.format(FMT) : "—"; }
    public String  getNomeFornecedor()         { return fornecedor    != null ? fornecedor.getNome()       : ""; }
    public String  getNomeAprovador()          { return aprovador     != null ? aprovador.getNome()        : "—"; }
    public String  getValorFormatado()         { return String.format("R$ %.2f", valorTotal).replace(".", ","); }
    public boolean temAnexo()                  { return caminhoAnexo != null && !caminhoAnexo.isBlank(); }

    public String getStatusFormatado() {
        return switch (status) {
            case "AGUARDANDO_APROVACAO"  -> "Aguardando";
            case "APROVADO"              -> "Aprovado";
            case "APROVADO_PARCIALMENTE" -> "Aprovado Parcial";
            case "NEGADO"                -> "Negado";
            default                      -> status;
        };
    }
}