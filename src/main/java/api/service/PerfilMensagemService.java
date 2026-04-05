package api.service;

import java.util.Locale;

public final class PerfilMensagemService {

    private PerfilMensagemService() {
    }

    public static String mensagemPara(String perfil) {
        return switch (perfil.toUpperCase(Locale.ROOT)) {
            case "DIRETOR" -> "Acesso liberado ao painel da diretoria.\n\n"
                    + "- Aprovar ou negar pedidos\n"
                    + "- Consultar indicadores gerais\n"
                    + "- Acompanhar movimentacoes e historico";
            case "FINANCEIRO" -> "Acesso liberado ao painel financeiro.\n\n"
                    + "- Consultar cotacoes e compras\n"
                    + "- Acompanhar pagamentos e notas fiscais\n"
                    + "- Validar custos e centros de custo";
            case "ESTOQUE" -> "Acesso liberado ao painel de estoque.\n\n"
                    + "- Registrar entradas e saidas\n"
                    + "- Consultar saldo de produtos\n"
                    + "- Acompanhar recebimento de itens";
            case "OPERACIONAL" -> "Acesso liberado ao painel operacional.\n\n"
                    + "- Abrir pedidos\n"
                    + "- Consultar andamento das solicitacoes\n"
                    + "- Ver notificacoes do processo";
            default -> "Perfil reconhecido, mas sem painel configurado ainda.";
        };
    }
}
