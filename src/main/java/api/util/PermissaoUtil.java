package api.util;

import api.model.SessaoUsuario;
import java.util.List;
import java.util.Map;

public class PermissaoUtil {

    // FINANCEIRO herda ESTOQUE — mas ESTOQUE não herda FINANCEIRO
    // ADMIN herda tudo
    // DIRETOR herda FINANCEIRO e ESTOQUE
    private static final Map<String, List<String>> HERANCA = Map.of(
            "ADMIN",       List.of("ADMIN", "DIRETOR", "FINANCEIRO", "ESTOQUE", "SOLICITANTE"),
            "DIRETOR",     List.of("DIRETOR", "FINANCEIRO", "ESTOQUE", "SOLICITANTE"),
            "FINANCEIRO",  List.of("FINANCEIRO", "ESTOQUE"),
            "ESTOQUE",     List.of("ESTOQUE"),
            "SOLICITANTE", List.of("SOLICITANTE")
    );

    /**
     * Verifica se o usuário logado tem permissão para pelo menos um dos perfis informados,
     * levando em conta a hierarquia (FINANCEIRO inclui ESTOQUE, etc).
     */
    public static boolean temPermissao(String... perfisPermitidos) {
        if (SessaoUsuario.getInstancia().getUsuarioLogado() == null) return false;

        String perfilAtual = SessaoUsuario.getInstancia()
                .getUsuarioLogado().getPerfil().getPerfil()
                .toUpperCase();

        List<String> perfisEfetivos = HERANCA.getOrDefault(perfilAtual, List.of(perfilAtual));

        for (String permitido : perfisPermitidos) {
            if (perfisEfetivos.stream()
                    .anyMatch(p -> p.equalsIgnoreCase(permitido))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica permissão EXATA — sem herança.
     * Use quando quiser que ESTOQUE veja algo mas FINANCEIRO não.
     * Exemplo: botão que só faz sentido para quem opera fisicamente o estoque.
     */
    public static boolean temPermissaoExata(String... perfisPermitidos) {
        if (SessaoUsuario.getInstancia().getUsuarioLogado() == null) return false;

        String perfilAtual = SessaoUsuario.getInstancia()
                .getUsuarioLogado().getPerfil().getPerfil();

        for (String permitido : perfisPermitidos) {
            if (permitido.equalsIgnoreCase(perfilAtual)) return true;
        }
        return false;
    }
}