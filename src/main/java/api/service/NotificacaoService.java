package api.service;

import api.DAO.notificacaoDAO;
import api.model.Notificacao;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.List;
import java.util.Properties;

public class NotificacaoService {

    // ── CONFIG SMTP ───────────────────────────────────────────
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;

    private static final String EMAIL_REMETENTE = "gestaocomprasnewe@gmail.com";
    private static final String SENHA_REMETENTE = "ztqsoxyxpdwoemty"; // sem espaços
    private static final String NOME_REMETENTE  = "Sistema de Compras";

    // ─────────────────────────────────────────────────────────
    // 🚀 NOVO PEDIDO
    // ─────────────────────────────────────────────────────────
    public static void notificarNovoPedido(int idPedido,
                                           String numPedido,
                                           double valorTotal,
                                           String nomeSolicitante,
                                           String dataHora) {

        String valorFmt = formatarValor(valorTotal);

        String titulo = "📋 Novo pedido aguardando aprovação";
        String msg = String.format(
                "Pedido %s de %s no valor de %s cadastrado em %s.",
                numPedido, nomeSolicitante, valorFmt, dataHora
        );

        notificarDiretoria(idPedido, titulo, msg, "Pedido");

        enviarEmailDiretoria(
                "Novo Pedido " + numPedido,
                montarEmailSimples("Novo pedido cadastrado", msg)
        );
    }

    // ─────────────────────────────────────────────────────────
    // ✏️ PEDIDO ALTERADO
    // ─────────────────────────────────────────────────────────
    public static void notificarPedidoAlterado(int idPedido,
                                               String numPedido,
                                               double valorTotal,
                                               String nomeUsuario,
                                               String dataHora) {

        String valorFmt = formatarValor(valorTotal);

        String titulo = "✏️ Pedido atualizado";
        String msg = String.format(
                "Pedido %s atualizado por %s em %s. Novo valor: %s.",
                numPedido, nomeUsuario, dataHora, valorFmt
        );

        notificarDiretoria(idPedido, titulo, msg, "Pedido");

        enviarEmailDiretoria(
                "Pedido atualizado " + numPedido,
                montarEmailSimples("Pedido atualizado", msg)
        );
    }

    // ─────────────────────────────────────────────────────────
    // ✅ PEDIDO APROVADO
    // ─────────────────────────────────────────────────────────
    public static void notificarPedidoAprovado(int idPedido,
                                               String numPedido,
                                               double valor,
                                               String nomeAprovador,
                                               String nomeSolicitante,
                                               int idSolicitante) {

        String valorFmt = formatarValor(valor);

        String titulo = "✅ Pedido aprovado";
        String msg = String.format(
                "Pedido %s aprovado por %s no valor de %s.",
                numPedido, nomeAprovador, valorFmt
        );

        // 🔔 solicitante
        notificacaoDAO.inserir(
                new Notificacao(idSolicitante, titulo, msg, "Pedido", idPedido)
        );

        // 🔔 diretoria
        notificarDiretoria(idPedido, titulo, msg, "Pedido");

        // 📧 email solicitante
        String emailSolic = notificacaoDAO.buscarEmailUsuario(idSolicitante);

        // 📧 email diretoria
        List<String> emailsDir = notificacaoDAO.buscarEmailsDiretores();

        new Thread(() -> {
            if (emailSolic != null) {
                enviarEmail(
                        emailSolic,
                        "Pedido aprovado " + numPedido,
                        montarEmailSimples("Seu pedido foi aprovado", msg)
                );
            }

            for (String email : emailsDir) {
                enviarEmail(
                        email,
                        "Pedido aprovado " + numPedido,
                        montarEmailSimples("Pedido aprovado", msg)
                );
            }
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

        String msg = String.format(
                "Cotação do fornecedor %s no valor de %s para o pedido %s cadastrada por %s.",
                fornecedor, valorCotacao, numPedido, usuario
        );

        notificarDiretoria(idPedido, titulo, msg, "Cotacao");

        enviarEmailDiretoria(
                "Nova cotação - Pedido " + numPedido,
                montarEmailSimples("Nova cotação cadastrada", msg)
        );
    }

    // ─────────────────────────────────────────────────────────
    // 🔔 AUX: NOTIFICAR DIRETORIA (BANCO)
    // ─────────────────────────────────────────────────────────
    private static void notificarDiretoria(int idRef,
                                           String titulo,
                                           String msg,
                                           String tipo) {

        List<int[]> diretores = notificacaoDAO.buscarUsuariosPorPerfil("DIRETOR");

        for (int[] d : diretores) {
            notificacaoDAO.inserir(
                    new Notificacao(d[0], titulo, msg, tipo, idRef)
            );
        }
    }

    // ─────────────────────────────────────────────────────────
    // 📧 AUX: EMAIL DIRETORIA
    // ─────────────────────────────────────────────────────────
    private static void enviarEmailDiretoria(String assunto, String html) {
        List<String> emails = notificacaoDAO.buscarEmailsDiretores();

        new Thread(() -> {
            for (String email : emails) {
                enviarEmail(email, assunto, html);
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────
    // 📧 ENVIO EMAIL
    // ─────────────────────────────────────────────────────────
    private static void enviarEmail(String destino,
                                    String assunto,
                                    String html) {

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

    // ─────────────────────────────────────────────────────────
    // ⚙ CONFIG SMTP
    // ─────────────────────────────────────────────────────────
    private static Properties configSMTP() {
        Properties p = new Properties();
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.host", SMTP_HOST);
        p.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        return p;
    }

    // ─────────────────────────────────────────────────────────
    // 🎨 EMAIL SIMPLES
    // ─────────────────────────────────────────────────────────
    private static String montarEmailSimples(String titulo, String msg) {
        return "<h2>" + titulo + "</h2><p>" + msg + "</p>";
    }

    // ─────────────────────────────────────────────────────────
    // 💰 FORMATAR VALOR
    // ─────────────────────────────────────────────────────────
    private static String formatarValor(double v) {
        return String.format("R$ %.2f", v).replace(".", ",");
    }
}