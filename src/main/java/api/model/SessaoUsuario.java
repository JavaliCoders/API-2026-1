package api.model;

/**
 * Classe Singleton que guarda o usuário logado durante a sessão.
 * Pode ser acessada de qualquer controller com SessaoUsuario.getInstancia()
 */
public class SessaoUsuario {

    // Instância única — só existe uma durante toda a execução
    private static SessaoUsuario instancia;

    // Usuário atualmente logado
    private Usuario usuarioLogado;

    // Construtor privado — ninguém cria instância diretamente
    private SessaoUsuario() {}

    /**
     * Retorna a instância única da sessão.
     * Cria uma nova se ainda não existir.
     */
    public static SessaoUsuario getInstancia() {
        if (instancia == null) {
            instancia = new SessaoUsuario();
        }
        return instancia;
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public void setUsuarioLogado(Usuario usuario) {
        this.usuarioLogado = usuario;
    }

    // Verifica se há alguém logado
    public boolean isLogado() {
        return usuarioLogado != null;
    }

    // Limpa a sessão ao fazer logout
    public void encerrarSessao() {
        usuarioLogado = null;
    }

    // Retorna o nome do usuário logado
    public String getNomeUsuarioLogado() {
        return usuarioLogado != null ? usuarioLogado.getNome() : "";
    }

    // Retorna o perfil do usuário logado
    public String getPerfilUsuarioLogado() {
        return usuarioLogado != null ? usuarioLogado.getNomePerfil() : "";
    }
}