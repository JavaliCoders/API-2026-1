package api.util;

import api.model.SessaoUsuario;

public class PermissaoUtil {

    public static boolean temPermissao(String... perfisPermitidos) {
        if (SessaoUsuario.getInstancia().getUsuarioLogado() == null) {
            return false;
        }

        String perfil = SessaoUsuario.getInstancia()
                .getUsuarioLogado()
                .getPerfil()
                .getPerfil();

        for (String p : perfisPermitidos) {
            if (p.equalsIgnoreCase(perfil)) {
                return true;
            }
        }
        return false;
    }
}
