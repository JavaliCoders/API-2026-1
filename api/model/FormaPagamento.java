package api.model;

public class FormaPagamento {
    private int    idFormaPagamento;
    private String forma;

    public FormaPagamento() {}
    public FormaPagamento(int id, String forma) {
        this.idFormaPagamento = id;
        this.forma = forma;
    }

    public int    getIdFormaPagamento() { return idFormaPagamento; }
    public String getForma()            { return forma; }

    @Override public String toString() { return forma; }
}