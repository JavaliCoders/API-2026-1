package api.service;

import api.DAO.notificacaoDAO;
import api.model.Notificacao;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.List;
import java.util.Properties;

public class NotificacaoService {

    private static final String SMTP_HOST       = "smtp.gmail.com";
    private static final int    SMTP_PORT        = 587;
    private static final String EMAIL_REMETENTE  = "gestaocomprasnewe@gmail.com";
    private static final String SENHA_REMETENTE  = "ztqsoxyxpdwoemty";
    private static final String NOME_REMETENTE   = "Sistema de Compras";

    // ─────────────────────────────────────────────────────────
    // 🚀 NOVO PEDIDO
    // ─────────────────────────────────────────────────────────
    public static void notificarNovoPedido(int idPedido,
                                           String numPedido,
                                           double valorTotal,
                                           String nomeSolicitante,
                                           String dataHora) {
        String titulo = "📋 Novo pedido aguardando aprovação";
        String msg    = String.format("Pedido %s de %s no valor de %s cadastrado em %s.",
                numPedido, nomeSolicitante, formatarValor(valorTotal), dataHora);

        inserirNotificacaoDiretoria(idPedido, titulo, msg, "Pedido");
        enviarEmailDiretoria("Novo Pedido " + numPedido, montarEmailSimples("Novo pedido cadastrado", msg));
    }

    // ─────────────────────────────────────────────────────────
    // ✏️ PEDIDO ALTERADO
    // ─────────────────────────────────────────────────────────
    public static void notificarPedidoAlterado(int idPedido,
                                               String numPedido,
                                               double valorTotal,
                                               String nomeUsuario,
                                               String dataHora) {
        String titulo = "✏️ Pedido atualizado";
        String msg    = String.format("Pedido %s atualizado por %s em %s. Novo valor: %s.",
                numPedido, nomeUsuario, dataHora, formatarValor(valorTotal));

        inserirNotificacaoDiretoria(idPedido, titulo, msg, "Pedido");
        enviarEmailDiretoria("Pedido atualizado " + numPedido, montarEmailSimples("Pedido atualizado", msg));
    }

    // ─────────────────────────────────────────────────────────
    // ✅ PEDIDO APROVADO (total ou parcialmente)
    // ─────────────────────────────────────────────────────────
    public static void notificarPedidoAprovado(int idPedido,
                                               String numPedido,
                                               double valor,
                                               String nomeAprovador,
                                               String nomeSolicitante,
                                               int idSolicitante) {
        notificarDecisaoPedido(idPedido, numPedido, valor,
                nomeAprovador, idSolicitante, false, false);
    }

    public static void notificarPedidoAprovadoParcialmente(int idPedido,
                                                           String numPedido,
                                                           double valor,
                                                           String nomeAprovador,
                                                           String nomeSolicitante,
                                                           int idSolicitante) {
        notificarDecisaoPedido(idPedido, numPedido, valor,
                nomeAprovador, idSolicitante, false, true);
    }

    public static void notificarPedidoNegado(int idPedido,
                                             String numPedido,
                                             String nomeAprovador,
                                             int idSolicitante) {
        notificarDecisaoPedido(idPedido, numPedido, 0,
                nomeAprovador, idSolicitante, true, false);
    }

    /**
     * Método central para decisão de pedido.
     * Insere notificação no banco apenas para o SOLICITANTE.
     * Envia email separado para solicitante + diretoria.
     * Não duplica inserção no banco para diretores.
     */
    private static void notificarDecisaoPedido(int idPedido,
                                                String numPedido,
                                                double valor,
                                                String nomeAprovador,
                                                int idSolicitante,
                                                boolean negado,
                                                boolean parcial) {
        String titulo;

        if (negado) {
            titulo = "❌ Pedido negado";
        } else if (parcial) {
            titulo = "⚠️ Pedido aprovado parcialmente";
        } else {
            titulo = "✅ Pedido aprovado";
        }
        String msg    = negado
                ? String.format("Pedido %s foi negado por %s.", numPedido, nomeAprovador)
                : String.format("Pedido %s aprovado por %s no valor de %s.",
                numPedido, nomeAprovador, formatarValor(valor));

        // 🔔 Banco — solicitante
        notificacaoDAO.inserir(new Notificacao(idSolicitante, titulo, msg, "Pedido", idPedido));

        // 🔔 Banco — financeiro
        inserirNotificacaoFinanceiro(idPedido, titulo, msg, "Pedido");

        // 📧 Email em background — solicitante + diretores
        String emailSolic     = notificacaoDAO.buscarEmailUsuario(idSolicitante);
        List<String> emailsDir = notificacaoDAO.buscarEmailsDiretores();

        new Thread(() -> {
            if (emailSolic != null)
                enviarEmail(emailSolic, titulo + " — " + numPedido,
                        montarEmailSimples(titulo, msg));
            for (String email : emailsDir)
                enviarEmail(email, titulo + " — " + numPedido,
                        montarEmailSimples(titulo, msg));
        }).start();
    }

    // ─────────────────────────────────────────────────────────
    // 💰 NOVA COTAÇÃO
    // ─────────────────────────────────────────────────────────
    public static void notificarNovaCotacao(int idPedido,
                                            String numPedido,
                                            String fornecedor,
                                            double valorCotacao,
                                            String usuario) {
        String titulo = "💰 Nova cotação cadastrada";
        String msg    = String.format(
                "Cotação do fornecedor %s no valor de %s para o pedido %s cadastrada por %s.",
                fornecedor, formatarValor(valorCotacao), numPedido, usuario);

        inserirNotificacaoDiretoria(idPedido, titulo, msg, "Cotacao");
        enviarEmailDiretoria("Nova cotação — Pedido " + numPedido,
                montarEmailSimples("Nova cotação cadastrada", msg));
    }

    // ─────────────────────────────────────────────────────────
    // 🔔 AUX: inserir notificação no banco para todos os diretores
    // ─────────────────────────────────────────────────────────
    private static void inserirNotificacaoDiretoria(int idRef,
                                                    String titulo,
                                                    String msg,
                                                    String tipo) {
        List<int[]> diretores = notificacaoDAO.buscarUsuariosPorPerfil("DIRETOR");
        for (int[] d : diretores)
            notificacaoDAO.inserir(new Notificacao(d[0], titulo, msg, tipo, idRef));
    }

    // ─────────────────────────────────────────────────────────
    // 📧 AUX: enviar email para todos os diretores (em background)
    // ─────────────────────────────────────────────────────────
    private static void enviarEmailDiretoria(String assunto, String html) {
        List<String> emails = notificacaoDAO.buscarEmailsDiretores();
        new Thread(() -> {
            for (String email : emails)
                enviarEmail(email, assunto, html);
        }).start();
    }

    // ─────────────────────────────────────────────────────────
// 🔔 AUX: inserir notificação no banco para o financeiro
// ─────────────────────────────────────────────────────────
    private static void inserirNotificacaoFinanceiro(int idRef,
                                                     String titulo,
                                                     String msg,
                                                     String tipo) {
        List<int[]> financeiros = notificacaoDAO.buscarUsuariosPorPerfil("FINANCEIRO");

        for (int[] f : financeiros) {
            notificacaoDAO.inserir(new Notificacao(f[0], titulo, msg, tipo, idRef));
        }
    }

    // ─────────────────────────────────────────────────────────
    // 📧 ENVIO EMAIL
    // ─────────────────────────────────────────────────────────
    private static void enviarEmail(String destino, String assunto, String html) {
        try {
            Session session = Session.getInstance(configSMTP(), new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_REMETENTE, SENHA_REMETENTE);
                }
            });
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(EMAIL_REMETENTE, NOME_REMETENTE));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(destino));
            msg.setSubject(assunto, "UTF-8");
            msg.setContent(html, "text/html; charset=UTF-8");
            Transport.send(msg);
        } catch (Exception e) {
            System.err.println("Erro email: " + e.getMessage());
        }
    }

    private static Properties configSMTP() {
        Properties p = new Properties();
        p.put("mail.smtp.auth",            "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.host",            SMTP_HOST);
        p.put("mail.smtp.port",            String.valueOf(SMTP_PORT));
        return p;
    }

    private static String montarEmailSimples(String titulo, String msg) {
        return "<h2>" + titulo + "</h2><p>" + msg + "</p>";
    }

    private static String formatarValor(double v) {
        return String.format("R$ %.2f", v).replace(".", ",");
    }
}