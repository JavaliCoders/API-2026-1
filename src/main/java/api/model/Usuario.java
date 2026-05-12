package api.model;

import javafx.beans.property.*;

public class Usuario {

    private final IntegerProperty idUsuario;
    private final StringProperty  nome;
    private final StringProperty  usuario;
    private final StringProperty  senha;
    private final StringProperty  email;
    private final StringProperty  status;
    private final ObjectProperty<Perfil> perfil;

    // Construtor com ID (busca do banco)
    public Usuario(int idUsuario, String nome, String usuario, String senha,
                   String email, String status, Perfil perfil) {
        this.idUsuario = new SimpleIntegerProperty(idUsuario);
        this.nome      = new SimpleStringProperty(nome);
        this.usuario   = new SimpleStringProperty(usuario);
        this.senha     = new SimpleStringProperty(senha);
        this.email     = new SimpleStringProperty(email);
        this.status    = new SimpleStringProperty(status);
        this.perfil    = new SimpleObjectProperty<>(perfil);
    }

    // Construtor sem ID (novo cadastro)
    public Usuario(String nome, String usuario, String senha,
                   String email, String status, Perfil perfil) {
        this(0, nome, usuario, senha, email, status, perfil);
    }

    // Getters
    public int    getIdUsuario() { return idUsuario.get(); }
    public String getNome()      { return nome.get(); }
    public String getUsuario()   { return usuario.get(); }
    public String getSenha()     { return senha.get(); }
    public String getEmail()     { return email.get(); }
    public String getStatus()    { return status.get(); }
    public Perfil getPerfil()    { return perfil.get(); }

    // Getter auxiliar para exibir o nome do perfil na tabela
    public String getNomePerfil() {
        return perfil.get() != null ? perfil.get().getPerfil() : "";
    }

    // Properties
    public IntegerProperty             idUsuarioProperty() { return idUsuario; }
    public StringProperty              nomeProperty()      { return nome; }
    public StringProperty              usuarioProperty()   { return usuario; }
    public StringProperty              senhaProperty()     { return senha; }
    public StringProperty              emailProperty()     { return email; }
    public StringProperty              statusProperty()    { return status; }
    public ObjectProperty<Perfil>      perfilProperty()    { return perfil; }
}