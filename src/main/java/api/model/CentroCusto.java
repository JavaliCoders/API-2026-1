package api.model;

import javafx.beans.property.*;

public class CentroCusto {

    private final IntegerProperty idCentroCusto;
    private final StringProperty  centroCusto;

    public CentroCusto(int idCentroCusto, String centroCusto) {
        this.idCentroCusto = new SimpleIntegerProperty(idCentroCusto);
        this.centroCusto   = new SimpleStringProperty(centroCusto);
    }

    public int    getIdCentroCusto() { return idCentroCusto.get(); }
    public String getCentroCusto()   { return centroCusto.get(); }

    public IntegerProperty idCentroCustoProperty() { return idCentroCusto; }
    public StringProperty  centroCustoProperty()   { return centroCusto; }

    @Override
    public String toString() { return centroCusto.get(); }
}