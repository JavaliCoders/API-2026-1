package api.model;

import javafx.beans.property.*;

public class Setor {

    private final IntegerProperty idSetor;
    private final StringProperty  setor;

    public Setor(int idSetor, String setor) {
        this.idSetor = new SimpleIntegerProperty(idSetor);
        this.setor   = new SimpleStringProperty(setor);
    }

    public Setor() {
        this(0, "");
    }

    public int    getIdSetor() { return idSetor.get(); }
    public String getSetor()   { return setor.get(); }

    public void setIdSetor(int id)     { idSetor.set(id); }
    public void setSetor(String setor) { this.setor.set(setor); }

    public IntegerProperty idSetorProperty() { return idSetor; }
    public StringProperty  setorProperty()   { return setor; }

    // Usado pelo ComboBox para exibir o nome do setor
    @Override
    public String toString() { return setor.get(); }
}