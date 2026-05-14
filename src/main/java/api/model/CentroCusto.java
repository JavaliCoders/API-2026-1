package api.model;

import javafx.beans.property.*;

public class CentroCusto {

    private final IntegerProperty idCentroCusto;
    private final StringProperty  centroCusto;
    private final StringProperty  status;

    public CentroCusto(int idCentroCusto, String centroCusto, String status) {
        this.idCentroCusto = new SimpleIntegerProperty(idCentroCusto);
        this.centroCusto   = new SimpleStringProperty(centroCusto);
        this.status = new SimpleStringProperty(status);
    }

    public int    getIdCentroCusto() { return idCentroCusto.get(); }
    public String getCentroCusto()   { return centroCusto.get(); }
    public String getStatus()   { return status.get(); }

    public IntegerProperty idCentroCustoProperty() { return idCentroCusto; }
    public StringProperty  centroCustoProperty()   { return centroCusto; }
    public StringProperty  statusProperty()   { return status; }

    @Override
    public String toString() { return centroCusto.get(); }
}