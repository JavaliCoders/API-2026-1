package api.service;

import api.DAO.notificacaoDAO;
import api.model.Notificacao;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.List;
import java.util.Properties;

/**
 * Serviço central de notificações (sistema + email).
 *
 * ═══════════════════════════════════════════════════════════════════════
 *  CONFIGURAÇÃO DE EMAIL — PASSO A PASSO (Gmail)
 * ═══════════════════════════════════════════════════════════════════════
 *
 *  1. Acesse sua conta Google em https://myaccount.google.com
 *  2. Vá em "Segurança" → ative a "Verificação em duas etapas" (obrigatório)
 *  3. Ainda em "Segurança", busque "Senhas de app" (App Passwords)
 *  4. Selecione "Outro (nome personalizado)" → dê o nome "SistemaCompras"
 *  5. O Google gera uma senha de 16 caracteres (ex.: abcd efgh ijkl mnop)
 *  6. Cole essa senha (SEM espaços) em SENHA_REMETENTE abaixo
 *  7. Em EMAIL_REMETENTE coloque o endereço Gmail completo
 *
 *  Para outros provedores (Outlook, Yahoo etc.) ajuste SMTP_HOST e SMTP_PORT.
 *  Outlook: smtp-mail.outlook.com  porta 587
 *  Yahoo  : smtp.mail.yahoo.com    porta 587
 *
 *  RECOMENDAÇÃO: Mova as credenciais para um arquivo .env ou .properties
 *  e nunca as comite no controle de versão.
 * ═══════════════════════════════════════════════════════════════════════
 */
public class NotificacaoService {

    // ── Configurações de SMTP ─────────────────────────────────
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int    SMTP_PORT = 587;

    /** E-mail da conta que enviará as notificações (seu Gmail). */
    private static final String EMAIL_REMETENTE = "gestaocomprasnewe@gmail.com";

    /**
     * Senha de App gerada no Google (16 caracteres, sem espaços).
     * NÃO use sua senha normal do Gmail aqui.
     */
    private static final String SENHA_REMETENTE = "ztqs oxyx pdwo emty";

    /** Nome exibido na caixa de entrada do destinatário. */
    private static final String NOME_REMETENTE  = "Sistema de Gestão de Compras Newe";

    // ── Notificar DIRETOREs sobre novo pedido ─────────────────
    /**
     * Chame este método logo após persistir um novo pedido no banco.
     *
     * @param idPedido        ID gerado pelo banco
     * @param numPedido       Número formatado (ex.: "PED-0012")
     * @param valorTotal      Valor total estimado do pedido
     * @param nomeSolicitante Nome do usuário que cadastrou
     * @param dataHora        Data/hora de abertura formatada (dd/MM/yyyy HH:mm)
     */
    public static void notificarNovoPedido(int idPedido,
                                           String numPedido,
                                           double valorTotal,
                                           String nomeSolicitante,
                                           String dataHora) {

        String titulo   = "📋 Novo pedido aguardando aprovação";
        String valorFmt = String.format("R$ %.2f", valorTotal).replace(".", ",");
        String mensagem = String.format(
                "Novo pedido %s no valor de %s, cadastrado por %s em %s, aguardando aprovação.",
                numPedido, valorFmt, nomeSolicitante, dataHora
        );

        // 1. Notificações no sistema (banco de dados)
        List<int[]> diretores = notificacaoDAO.buscarUsuariosPorPerfil("DIRETOR");
        for (int[] dir : diretores) {
            Notificacao n = new Notificacao(dir[0], titulo, mensagem, "Pedido", idPedido);
            notificacaoDAO.inserir(n);
        }

        // 2. Emails (em thread separada para não travar a UI)
        List<String> emails = notificacaoDAO.buscarEmailsDiretores();
        if (!emails.isEmpty()) {
            Thread emailThread = new Thread(() -> {
                for (String email : emails) {
                    enviarEmail(
                            email,
                            "Novo Pedido " + numPedido + " — Aguardando Aprovação",
                            montarCorpoEmailNovoPedido(numPedido, valorFmt, nomeSolicitante, dataHora)
                    );
                }
            });
            emailThread.setDaemon(true);
            emailThread.start();
        }
    }

    // ── Notificar DIRETOREs sobre pedido alterado ─────────────
    public static void notificarPedidoAlterado(int idPedido,
                                               String numPedido,
                                               double valorTotal,
                                               String nomeUsuario,
                                               String dataHora) {

        String titulo   = "✏️ Pedido atualizado";
        String valorFmt = String.format("R$ %.2f", valorTotal).replace(".", ",");
        String mensagem = String.format(
                "O pedido %s foi atualizado por %s em %s. Novo valor: %s. Aguardando aprovação!",
                numPedido, nomeUsuario, dataHora, valorFmt
        );

        // 1. Notificações no sistema
        List<int[]> diretores = notificacaoDAO.buscarUsuariosPorPerfil("DIRETOR");
        for (int[] dir : diretores) {
            Notificacao n = new Notificacao(dir[0], titulo, mensagem, "Pedido", idPedido);
            notificacaoDAO.inserir(n);
        }

        // 2. Emails (em thread separada)
        List<String> emails = notificacaoDAO.buscarEmailsDiretores();
        if (!emails.isEmpty()) {
            Thread emailThread = new Thread(() -> {
                for (String email : emails) {
                    enviarEmail(
                            email,
                            "Pedido " + numPedido + " foi Atualizado",
                            montarCorpoEmailPedidoAlterado(numPedido, valorFmt, nomeUsuario, dataHora)
                    );
                }
            });
            emailThread.setDaemon(true);
            emailThread.start();
        }
    }

    // ── Envio de email via SMTP ───────────────────────────────
    private static void enviarEmail(String destinatario,
                                    String assunto,
                                    String corpoHtml) {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            SMTP_HOST);
        props.put("mail.smtp.port",            String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.trust",       SMTP_HOST);
        // Timeout de 10s para não travar a thread indefinidamente
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout",           "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_REMETENTE, SENHA_REMETENTE);
            }
        });

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(EMAIL_REMETENTE, NOME_REMETENTE, "UTF-8"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
            msg.setSubject(assunto, "UTF-8");
            msg.setContent(corpoHtml, "text/html; charset=utf-8");
            Transport.send(msg);
            System.out.println("✅ Email enviado para: " + destinatario);

        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar email para " + destinatario + ": " + e.getMessage());
        }
    }

    // ── Template: novo pedido ─────────────────────────────────
    private static String montarCorpoEmailNovoPedido(String numPedido,
                                                     String valorFmt,
                                                     String nomeSolicitante,
                                                     String dataHora) {
        return baseHtml(
                "Novo pedido cadastrado",
                "⏳ AGUARDANDO APROVAÇÃO",
                "#fef9c3", "#854d0e",
                "Um novo pedido foi registrado no sistema e requer sua aprovação.",
                numPedido, nomeSolicitante, dataHora, valorFmt,
                "Acesse o sistema para analisar e aprovar ou negar este pedido."
        );
    }

    // ── Template: pedido alterado ─────────────────────────────
    private static String montarCorpoEmailPedidoAlterado(String numPedido,
                                                         String valorFmt,
                                                         String nomeUsuario,
                                                         String dataHora) {
        return baseHtml(
                "Pedido atualizado",
                "✏️ PEDIDO ALTERADO",
                "#dbeafe", "#1e40af",
                "Um pedido existente foi alterado e pode precisar de nova avaliação.",
                numPedido, nomeUsuario, dataHora, valorFmt,
                "Acesse o sistema para verificar as alterações realizadas."
        );
    }

    // ── Template HTML base ────────────────────────────────────
    private static String baseHtml(String titulo,
                                   String badgeTexto,
                                   String badgeBg,
                                   String badgeCor,
                                   String subtitulo,
                                   String numPedido,
                                   String nomeUsuario,
                                   String dataHora,
                                   String valorFmt,
                                   String rodapeTexto) {
        return "<!DOCTYPE html>" +
                "<html lang='pt-BR'><head><meta charset='UTF-8'/><style>" +
                "body{font-family:'Segoe UI',Arial,sans-serif;background:#f0f2f5;margin:0;padding:0;}" +
                ".container{max-width:560px;margin:32px auto;background:#fff;border-radius:12px;" +
                "overflow:hidden;box-shadow:0 4px 16px rgba(0,0,0,.08);}" +
                ".header{background:#0d0d1a;padding:28px 32px;text-align:center;}" +
                ".header h1{color:#3b82f6;font-size:20px;margin:0;}" +
                ".header p{color:#9999bb;font-size:13px;margin:6px 0 0;}" +
                ".body{padding:32px;}" +
                ".badge{display:inline-block;background:" + badgeBg + ";color:" + badgeCor + ";" +
                "font-size:12px;font-weight:bold;padding:4px 12px;border-radius:6px;margin-bottom:20px;}" +
                ".info-box{background:#f8fafc;border-left:4px solid #2563eb;" +
                "border-radius:8px;padding:16px 20px;margin:16px 0;}" +
                ".info-row{display:flex;justify-content:space-between;" +
                "padding:6px 0;border-bottom:1px solid #e5e7eb;}" +
                ".info-row:last-child{border-bottom:none;}" +
                ".label{color:#6b7280;font-size:13px;}" +
                ".value{color:#111827;font-size:13px;font-weight:600;}" +
                ".valor-destaque{color:#16a34a;font-size:16px;font-weight:bold;}" +
                ".footer{background:#f8fafc;padding:16px 32px;text-align:center;" +
                "color:#9ca3af;font-size:11px;border-top:1px solid #e5e7eb;}" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>🛒 Sistema de Gestão de Compras</h1>" +
                "<p>Notificação automática — não responda este email</p>" +
                "</div>" +
                "<div class='body'>" +
                "<div class='badge'>" + badgeTexto + "</div>" +
                "<h2 style='color:#111827;margin:0 0 8px;'>" + titulo + "</h2>" +
                "<p style='color:#6b7280;margin:0 0 20px;font-size:14px;'>" + subtitulo + "</p>" +
                "<div class='info-box'>" +
                "<div class='info-row'><span class='label'>Nº do Pedido</span>" +
                "<span class='value'>" + numPedido + "</span></div>" +
                "<div class='info-row'><span class='label'>Usuário</span>" +
                "<span class='value'>" + nomeUsuario + "</span></div>" +
                "<div class='info-row'><span class='label'>Data/Hora</span>" +
                "<span class='value'>" + dataHora + "</span></div>" +
                "<div class='info-row'><span class='label'>Valor Total Estimado</span>" +
                "<span class='valor-destaque'>" + valorFmt + "</span></div>" +
                "</div>" +
                "<p style='color:#6b7280;font-size:13px;margin-top:16px;'>" + rodapeTexto + "</p>" +
                "</div>" +
                "<div class='footer'>Este é um email automático gerado pelo Sistema de Gestão de Compras.<br/>Não responda esta mensagem.</div>" +
                "</div></body></html>";
    }
}