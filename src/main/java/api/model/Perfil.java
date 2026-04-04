package api.model;

import javafx.beans.property.*;

public class Perfil {

    private final IntegerProperty idPerfil;
    private final StringProperty  perfil;

    public Perfil(int idPerfil, String perfil) {
        this.idPerfil = new SimpleIntegerProperty(idPerfil);
        this.perfil   = new SimpleStringProperty(perfil);
    }

    public Perfil() {
        this(0, "");
    }

    public int    getIdPerfil() { return idPerfil.get(); }
    public String getPerfil()   { return perfil.get(); }

    public void setIdPerfil(int id)      { idPerfil.set(id); }
    public void setPerfil(String perfil) { this.perfil.set(perfil); }

    public IntegerProperty idPerfilProperty() { return idPerfil; }
    public StringProperty  perfilProperty()   { return perfil; }

    // Usado pelo ComboBox para exibir o nome do perfil
    @Override
    public String toString() { return perfil.get(); }
}