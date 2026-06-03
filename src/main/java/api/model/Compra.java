package api.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Compra {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IntegerProperty               idCompra;
    private final ObjectProperty<Pedido>        pedido;
    private final ObjectProperty<Fornecedor>    fornecedor;
    private final ObjectProperty<LocalDateTime> data;
    private final ObjectProperty<Usuario>       comprador;
    private final DoubleProperty                valorTotal;
    private final ObjectProperty<LocalDateTime> dataPrevista;
    private final StringProperty                status;

    // ── Construtor completo (usado ao carregar do BD) ─────────────────────────
    public Compra(int idCompra, Pedido pedido, Fornecedor fornecedor,
                  LocalDateTime data, Usuario comprador, double valorTotal,
                  LocalDateTime dataPrevista, String status) { // ← String status adicionado
        this.idCompra     = new SimpleIntegerProperty(idCompra);
        this.pedido       = new SimpleObjectProperty<>(pedido);
        this.fornecedor   = new SimpleObjectProperty<>(fornecedor);
        this.data         = new SimpleObjectProperty<>(data);
        this.comprador    = new SimpleObjectProperty<>(comprador);
        this.valorTotal   = new SimpleDoubleProperty(valorTotal);
        this.dataPrevista = new SimpleObjectProperty<>(dataPrevista);
        this.status       = new SimpleStringProperty(status); // ← agora recebe String corretamente
    }

    // ── Construtor para nova compra (status padrão REALIZADA) ─────────────────
    public Compra(Pedido pedido, Fornecedor fornecedor, LocalDateTime data,
                  Usuario comprador, double valorTotal, LocalDateTime dataPrevista) {
        this(0, pedido, fornecedor, data, comprador, valorTotal, dataPrevista, "REALIZADA");
    }

    // ── Construtor sem args ───────────────────────────────────────────────────
    public Compra() {
        this(0, null, null, null, null, 0, null, "REALIZADA");
    }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setPedido(Pedido p)              { pedido.set(p); }
    public void setFornecedor(Fornecedor f)      { fornecedor.set(f); }
    public void setData(LocalDateTime d)         { data.set(d); }
    public void setComprador(Usuario u)          { comprador.set(u); }
    public void setValorTotal(double v)          { valorTotal.set(v); }
    public void setDataPrevista(LocalDateTime d) { dataPrevista.set(d); }
    public void setStatus(String s)              { status.set(s); }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int           getIdCompra()     { return idCompra.get(); }
    public Pedido        getPedido()       { return pedido.get(); }
    public Fornecedor    getFornecedor()   { return fornecedor.get(); }
    public LocalDateTime getData()         { return data.get(); }
    public Usuario       getComprador()    { return comprador.get(); }
    public double        getValorTotal()   { return valorTotal.get(); }
    public LocalDateTime getDataPrevista() { return dataPrevista.get(); }
    public String        getStatus()       { return status.get(); }

    // ── Auxiliares para TableView ─────────────────────────────────────────────
    public String getNumPedido() {
        return pedido.get() != null ? pedido.get().getNumPedido() : "";
    }
    public String getNomeFornecedor() {
        return fornecedor.get() != null ? fornecedor.get().getNome() : "";
    }
    public String getNomeComprador() {
        return comprador.get() != null ? comprador.get().getNome() : "";
    }
    public String getDataFormatada() {
        return data.get() != null ? data.get().format(FORMATTER) : "";
    }
    public String getDataPrevistaFormatada() {
        return dataPrevista.get() != null ? dataPrevista.get().format(FORMATTER) : "—";
    }

    // ── Properties ───────────────────────────────────────────────────────────
    public IntegerProperty               idCompraProperty()     { return idCompra; }
    public ObjectProperty<Pedido>        pedidoProperty()       { return pedido; }
    public ObjectProperty<Fornecedor>    fornecedorProperty()   { return fornecedor; }
    public ObjectProperty<LocalDateTime> dataProperty()         { return data; }
    public ObjectProperty<Usuario>       compradorProperty()    { return comprador; }
    public DoubleProperty                valorTotalProperty()   { return valorTotal; }
    public ObjectProperty<LocalDateTime> dataPrevistaProperty() { return dataPrevista; }
    public StringProperty                statusProperty()       { return status; }
}