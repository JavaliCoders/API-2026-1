package api.model;

public class CotacaoItem {

    private int    idCotacaoItem;
    private int    idPedidoProduto;
    private String nomeProduto;
    private String unidade;
    private int    qtdCotada;
    private double valorUnitario;
    private double valorTotal;

    // Construtor para listagem (vindo do banco)
    public CotacaoItem(int idCotacaoItem, int idPedidoProduto,
                       String nomeProduto, String unidade,
                       int qtdCotada, double valorUnitario, double valorTotal) {
        this.idCotacaoItem   = idCotacaoItem;
        this.idPedidoProduto = idPedidoProduto;
        this.nomeProduto     = nomeProduto;
        this.unidade         = unidade;
        this.qtdCotada       = qtdCotada;
        this.valorUnitario   = valorUnitario;
        this.valorTotal      = valorTotal;
    }

    // Construtor para cadastro (ainda sem id)
    public CotacaoItem(int idPedidoProduto, String nomeProduto, String unidade,
                       int qtdCotada, double valorUnitario) {
        this.idPedidoProduto = idPedidoProduto;
        this.nomeProduto     = nomeProduto;
        this.unidade         = unidade;
        this.qtdCotada       = qtdCotada;
        this.valorUnitario   = valorUnitario;
        this.valorTotal      = qtdCotada * valorUnitario;
    }

    public int    getIdCotacaoItem()   { return idCotacaoItem; }
    public int    getIdPedidoProduto() { return idPedidoProduto; }
    public String getNomeProduto()     { return nomeProduto; }
    public String getUnidade()         { return unidade; }
    public int    getQtdCotada()       { return qtdCotada; }
    public double getValorUnitario()   { return valorUnitario; }
    public double getValorTotal()      { return valorTotal; }

    public void setQtdCotada(int q)       { this.qtdCotada = q; recalcular(); }
    public void setValorUnitario(double v) { this.valorUnitario = v; recalcular(); }
    private void recalcular()              { this.valorTotal = qtdCotada * valorUnitario; }

    public String getValorUnitFormatado() {
        return String.format("R$ %.2f", valorUnitario).replace(".", ",");
    }
    public String getValorTotalFormatado() {
        return String.format("R$ %.2f", valorTotal).replace(".", ",");
    }
}