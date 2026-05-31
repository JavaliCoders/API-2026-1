package api.controller;

import api.DAO.notificacaoDAO;
import api.model.Notificacao;
import api.model.SessaoUsuario;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class notificacaoController implements Initializable {

    @FXML private VBox       listaNotificacoes;
    @FXML private ScrollPane scrollNotificacoes;
    @FXML private Label      labelTotalNaoLidas;
    @FXML private Button     btnMarcarTodas;
    @FXML private Button     btnLimparLidas;
    @FXML private VBox       labelVazia;

    private AnchorPane      areaPrincipal;
    private indexController indexCtrl;
    private int             idUsuarioLogado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        idUsuarioLogado = SessaoUsuario.getInstancia().getIdUsuarioLogado();
        carregarNotificacoes();
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    public void setIndexController(indexController indexCtrl) {
        this.indexCtrl = indexCtrl;
    }

    // ── Carrega e renderiza as notificações ───────────────────
    private void carregarNotificacoes() {
        listaNotificacoes.getChildren().clear();
        ObservableList<Notificacao> lista = notificacaoDAO.listarPorUsuario(idUsuarioLogado);

        int  naoLidas = (int) lista.stream().filter(n -> !n.isLida()).count();
        long qtdLidas =       lista.stream().filter(Notificacao::isLida).count();

        labelTotalNaoLidas.setText(naoLidas > 0
                ? naoLidas + " não lida" + (naoLidas > 1 ? "s" : "")
                : "Todas lidas");

        // "Limpar lidas" só aparece quando há notificações lidas
        btnLimparLidas.setVisible(qtdLidas > 0);
        btnLimparLidas.setManaged(qtdLidas > 0);

        // "Marcar todas como lidas" só aparece quando há não lidas
        btnMarcarTodas.setVisible(naoLidas > 0);
        btnMarcarTodas.setManaged(naoLidas > 0);

        if (lista.isEmpty()) {
            labelVazia.setVisible(true);
            labelVazia.setManaged(true);
        } else {
            labelVazia.setVisible(false);
            labelVazia.setManaged(false);
            for (Notificacao n : lista) {
                listaNotificacoes.getChildren().add(criarCardNotificacao(n));
            }
        }

        atualizarBadgeSidebar();
    }

    // ── Cria card visual de uma notificação ───────────────────
    private HBox criarCardNotificacao(Notificacao n) {

        // Barra lateral colorida
        Region barraLateral = new Region();
        barraLateral.setPrefWidth(4);
        barraLateral.setMinWidth(4);
        barraLateral.setMaxWidth(4);
        barraLateral.setStyle(n.isLida()
                ? "-fx-background-color: #e5e7eb; -fx-background-radius: 4 0 0 4;"
                : "-fx-background-color: #2563eb; -fx-background-radius: 4 0 0 4;");

        // Ícone
        Label icone = new Label("📋");
        icone.setFont(Font.font(22));
        icone.setPadding(new Insets(0, 4, 0, 0));

        // Textos
        Label lblTitulo = new Label(n.getTitulo());
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblTitulo.setStyle(n.isLida() ? "-fx-text-fill: #6b7280;" : "-fx-text-fill: #111827;");
        lblTitulo.setWrapText(true);

        Label lblMensagem = new Label(n.getMensagem());
        lblMensagem.setFont(Font.font("Segoe UI", 12));
        lblMensagem.setStyle("-fx-text-fill: #6b7280;");
        lblMensagem.setWrapText(true);

        Label lblData = new Label(n.getDataFormatada());
        lblData.setFont(Font.font("Segoe UI", 11));
        lblData.setStyle("-fx-text-fill: #9ca3af;");

        Label badgeLida = new Label(n.isLida() ? "" : "● Nova");
        badgeLida.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        badgeLida.setStyle("-fx-text-fill: #2563eb;");

        HBox rodape = new HBox(8, lblData, badgeLida);
        rodape.setAlignment(Pos.CENTER_LEFT);

        VBox textos = new VBox(4, lblTitulo, lblMensagem, rodape);
        textos.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textos, Priority.ALWAYS);

        // Botão "Ver pedido"
        Button btnVer = new Button("Ver pedido");
        btnVer.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        btnVer.setStyle(
                "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; " +
                        "-fx-background-radius: 6; -fx-border-color: transparent; " +
                        "-fx-padding: 6 14; -fx-cursor: hand;");
        btnVer.setOnMouseEntered(e -> btnVer.setStyle(
                "-fx-background-color: #2563eb; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-border-color: transparent; " +
                        "-fx-padding: 6 14; -fx-cursor: hand;"));
        btnVer.setOnMouseExited(e -> btnVer.setStyle(
                "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; " +
                        "-fx-background-radius: 6; -fx-border-color: transparent; " +
                        "-fx-padding: 6 14; -fx-cursor: hand;"));
        btnVer.setOnAction(e -> abrirPedidos(n));

        // Botão excluir individual (🗑) — discreto, fica mais visível no hover
        Button btnExcluir = new Button("🗑");
        btnExcluir.setFont(Font.font(13));
        btnExcluir.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #9ca3af; " +
                        "-fx-border-color: transparent; -fx-padding: 4 8; -fx-cursor: hand;");
        btnExcluir.setOnMouseEntered(e -> btnExcluir.setStyle(
                "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; " +
                        "-fx-background-radius: 6; -fx-border-color: transparent; " +
                        "-fx-padding: 4 8; -fx-cursor: hand;"));
        btnExcluir.setOnMouseExited(e -> btnExcluir.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #9ca3af; " +
                        "-fx-border-color: transparent; -fx-padding: 4 8; -fx-cursor: hand;"));
        btnExcluir.setOnAction(e -> excluirNotificacao(n));

        // Agrupa os dois botões
        HBox botoes = new HBox(6, btnVer, btnExcluir);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        // Container central
        HBox centro = new HBox(12, icone, textos, botoes);
        centro.setAlignment(Pos.CENTER_LEFT);
        centro.setPadding(new Insets(14, 16, 14, 12));
        HBox.setHgrow(centro, Priority.ALWAYS);

        // Card completo
        HBox card = new HBox(barraLateral, centro);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(n.isLida()
                ? "-fx-background-color: #fafafa; -fx-background-radius: 10; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 10; -fx-border-width: 1;"
                : "-fx-background-color: #eff6ff; -fx-background-radius: 10; " +
                "-fx-border-color: #bfdbfe; -fx-border-radius: 10; -fx-border-width: 1;");
        VBox.setMargin(card, new Insets(0, 0, 8, 0));

        // Hover no card
        card.setOnMouseEntered(e -> {
            if (!n.isLida()) card.setStyle(
                    "-fx-background-color: #dbeafe; -fx-background-radius: 10; " +
                            "-fx-border-color: #93c5fd; -fx-border-radius: 10; -fx-border-width: 1;");
        });
        card.setOnMouseExited(e -> card.setStyle(n.isLida()
                ? "-fx-background-color: #fafafa; -fx-background-radius: 10; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 10; -fx-border-width: 1;"
                : "-fx-background-color: #eff6ff; -fx-background-radius: 10; " +
                "-fx-border-color: #bfdbfe; -fx-border-radius: 10; -fx-border-width: 1;"));

        return card;
    }

    // ── Excluir notificação individual (sem confirmação) ──────
    private void excluirNotificacao(Notificacao n) {
        notificacaoDAO.excluir(n.getIdNotificacao());
        carregarNotificacoes();
    }

    // ── Abre a tela de pedidos e marca como lida ──────────────
    private void abrirPedidos(Notificacao n) {
        if (!n.isLida()) {
            notificacaoDAO.marcarComoLida(n.getIdNotificacao());
            atualizarBadgeSidebar();
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/pedido.fxml"));
            Node tela = loader.load();

            pedidoController controller = loader.getController();
            controller.setAreaPrincipal(areaPrincipal);

            AnchorPane.setTopAnchor(tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor(tela, 0.0);
            AnchorPane.setRightAnchor(tela, 0.0);

            areaPrincipal.getChildren().setAll(tela);

        } catch (IOException e) {
            System.err.println("Erro ao abrir pedidos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Marcar todas como lidas ───────────────────────────────
    @FXML
    private void onMarcarTodas() {
        notificacaoDAO.marcarTodasComoLidas(idUsuarioLogado);
        carregarNotificacoes();
    }

    // ── Limpar todas as notificações lidas (com confirmação) ──
    @FXML
    private void onLimparLidas() {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Limpar notificações");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText(
                "Deseja excluir todas as notificações já lidas? Esta ação não pode ser desfeita.");
        confirmacao.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                notificacaoDAO.excluirTodasLidas(idUsuarioLogado);
                carregarNotificacoes();
            }
        });
    }

    // ── Recarregar ────────────────────────────────────────────
    @FXML
    private void onRecarregar() {
        carregarNotificacoes();
    }

    // ── Atualiza badge na sidebar ─────────────────────────────
    private void atualizarBadgeSidebar() {
        if (indexCtrl != null) {
            indexCtrl.atualizarBadgeNotificacoes();
        }
    }
}