package api.model;

public record UsuarioAutenticado(
        String nome,
        String usuario,
        String email,
        String perfil
) {
}
