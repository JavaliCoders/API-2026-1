package api.controller;

import api.DAO.notificacaoDAO;
import api.model.SessaoUsuario;
import api.util.LucideIconFactory;
import api.util.PermissaoUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class indexController implements Initializable {

    @FXML private VBox sidebar;

    @FXML private HBox menuDashboard;
    @FXML private HBox menuEstoque;
    @FXML private HBox menuFornecedores;
    @FXML private HBox menuPedidos;
    @FXML private HBox menuCotacoes;
    @FXML private HBox menuUsuarios;
    @FXML private HBox menuCompras;
    @FXML private HBox menuNotificacoes;
    @FXML private HBox menuCentroCusto;
    @FXML private HBox menuNotaFiscal;
    @FXML private HBox menuMovimentacao;
    @FXML private HBox menuSaida;
    @FXML private HBox menuHistorico;

    @FXML private Label textoDashboard;
    @FXML private Label textoEstoque;
    @FXML private Label textoFornecedores;
    @FXML private Label textoPedidos;
    @FXML private Label textoCotacoes;
    @FXML private Label textoUsuarios;
    @FXML private Label textoCompras;
    @FXML private Label textoNotificacoes;
    @FXML private Label textoSair;
    @FXML private Label labelSistema;
    @FXML private Label textoCentro;
    @FXML private Label textoNotaFiscal;
    @FXML private Label textoMovimentacao;
    @FXML private Label textoSaida;
    @FXML private Label textoHistorico;

    @FXML private Label iconDashboard;
    @FXML private Label iconEstoque;
    @FXML private Label iconPedidos;
    @FXML private Label iconCotacoes;
    @FXML private Label iconFornecedores;
    @FXML private Label iconCompras;
    @FXML private Label iconSair;
    @FXML private Label iconMenu;
    @FXML private Label iconNotificacoes;
    @FXML private Label badgeNotificacoes;
    @FXML private Label iconUsuarios;
    @FXML private Label iconCentro;
    @FXML private Label iconNotaFiscal;
    @FXML private Label iconMovimentacao;
    @FXML private Label iconSaida;
    @FXML private Label iconHistorico;

    @FXML private AnchorPane areaPrincipal;
    @FXML private Label      labelPagina;
    @FXML private Button     btnAcao;
    @FXML private HBox       hboxTopo;
    @FXML private HBox       hboxSair;

    private boolean sidebarExpandida = true;

    private static final double SIDEBAR_EXPANDIDA = 220;
    private static final double SIDEBAR_RECOLHIDA = 64;

    private static final String MENU_ATIVO        = "-fx-background-color: #2563eb; -fx-background-radius: 8; -fx-padding: 12 16;";
    private static final String MENU_INATIVO      = "-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 12 16;";
    private static final String MENU_ATIVO_MINI   = "-fx-background-color: #2563eb; -fx-background-radius: 8; -fx-padding: 12 8;";
    private static final String MENU_INATIVO_MINI = "-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 12 8;";
    private static final String SAIR_ESTILO_BASE  = "-fx-border-color: #1e1e35 transparent transparent transparent; -fx-border-width: 1; -fx-cursor: hand;";

    private static final int POLLING_SEGUNDOS = 30;
    private Timeline pollingTimeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        aplicarPermissoesMenu();
        carregarTelaInicial();
        configurarHover();
        atualizarBadgeNotificacoes();

        pollingTimeline = new Timeline(
                new KeyFrame(Duration.seconds(POLLING_SEGUNDOS), e -> atualizarBadgeNotificacoes()));
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
    }

    // ── Permissões ────────────────────────────────────────────────────────────

    private void aplicarPermissoesMenu() {
        boolean isDiretor     = PermissaoUtil.temPermissaoExata("DIRETOR");
        boolean isFinanceiro  = PermissaoUtil.temPermissaoExata("FINANCEIRO");
        boolean isEstoque     = PermissaoUtil.temPermissaoExata("ESTOQUE");
        boolean isSolicitante = PermissaoUtil.temPermissaoExata("SOLICITANTE");

        // Usuários — só DIRETOR
        if (!isDiretor) esconder(menuUsuarios);

        // Fornecedores — DIRETOR e FINANCEIRO
        if (!isDiretor && !isFinanceiro) esconder(menuFornecedores);

        // Cotações — DIRETOR e FINANCEIRO
        if (!isDiretor && !isFinanceiro) esconder(menuCotacoes);

        // Compras — DIRETOR e FINANCEIRO
        if (!isDiretor && !isFinanceiro) esconder(menuCompras);

        // Centro de Custo — DIRETOR e FINANCEIRO
        if (!isDiretor && !isFinanceiro) esconder(menuCentroCusto);

        // Nota Fiscal — só FINANCEIRO e ESTOQUE
        if (!isFinanceiro && !isEstoque) esconder(menuNotaFiscal);

        // Saída de Estoque — só FINANCEIRO e ESTOQUE
        if (!isFinanceiro && !isEstoque) esconder(menuSaida);

        // Dashboard — todos têm acesso (ninguém esconde)
        // Estoque — todos têm acesso (botão controlado no mostraBotao)
        // Pedidos — todos têm acesso
        // Movimentação — todos têm acesso (botão controlado no mostraBotao)
        // Histórico — todos têm acesso
        // Notificações — todos têm acesso
    }

    private void esconder(HBox menu) {
        if (menu != null) {
            menu.setVisible(false);
            menu.setManaged(false);
        }
    }

    private void carregarTelaInicial() {
        carregarTela("/view/dashboard.fxml", "Dashboard de indicadores", "");
        ativarMenu(menuDashboard);
    }

    // ── Badge ─────────────────────────────────────────────────────────────────

    public void atualizarBadgeNotificacoes() {
        int idUsuario = SessaoUsuario.getInstancia().getIdUsuarioLogado();
        int naoLidas  = notificacaoDAO.contarNaoLidas(idUsuario);
        if (naoLidas > 0) {
            badgeNotificacoes.setText(naoLidas > 99 ? "99+" : String.valueOf(naoLidas));
            badgeNotificacoes.setVisible(true);
            badgeNotificacoes.setManaged(true);
        } else {
            badgeNotificacoes.setVisible(false);
            badgeNotificacoes.setManaged(false);
        }
    }

    // ── Toggle sidebar ────────────────────────────────────────────────────────

    @FXML private void toggleSidebar() {
        if (sidebarExpandida) {
            sidebar.setPrefWidth(SIDEBAR_RECOLHIDA);
            ocultarTextos();
        } else {
            sidebar.setPrefWidth(SIDEBAR_EXPANDIDA);
            exibirTextos();
        }
        sidebarExpandida = !sidebarExpandida;
    }

    private HBox[] todosMenus() {
        return new HBox[]{menuDashboard, menuEstoque, menuFornecedores, menuPedidos,
                menuCotacoes, menuCompras, menuUsuarios, menuNotificacoes,
                menuNotaFiscal, menuMovimentacao, menuSaida, menuCentroCusto, menuHistorico};
    }

    private Label[] todosTextos() {
        return new Label[]{labelSistema, textoDashboard, textoEstoque, textoFornecedores,
                textoPedidos, textoCotacoes, textoCompras, textoSair,
                textoUsuarios, textoNotificacoes,
                textoNotaFiscal, textoMovimentacao, textoSaida, textoCentro, textoHistorico};
    }

    private void ocultarTextos() {
        for (Label l : todosTextos()) {
            if (l != null) { l.setVisible(false); l.setManaged(false); }
        }
        hboxTopo.setAlignment(javafx.geometry.Pos.CENTER);
        for (HBox m : todosMenus()) {
            if (m != null && m.isManaged()) {
                m.setAlignment(javafx.geometry.Pos.CENTER);
                m.setSpacing(0);
                m.setStyle(m.getStyle().contains("#2563eb") ? MENU_ATIVO_MINI : MENU_INATIVO_MINI);
            }
        }
        hboxSair.setAlignment(javafx.geometry.Pos.CENTER);
        hboxSair.setSpacing(0);
        hboxSair.setStyle("-fx-padding: 12 8; " + SAIR_ESTILO_BASE);
    }

    private void exibirTextos() {
        for (Label l : todosTextos()) {
            if (l != null) { l.setVisible(true); l.setManaged(true); }
        }
        hboxTopo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        for (HBox m : todosMenus()) {
            if (m != null && m.isManaged()) {
                m.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                m.setSpacing(12);
                m.setStyle(m.getStyle().contains("#2563eb") ? MENU_ATIVO : MENU_INATIVO);
            }
        }
        hboxSair.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        hboxSair.setSpacing(12);
        hboxSair.setStyle("-fx-padding: 16 28; " + SAIR_ESTILO_BASE);
    }

    // ── Navegação ─────────────────────────────────────────────────────────────

    private void carregarTela(String fxmlPath, String subtitulo, String textoBotao) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node tela = loader.load();
            Object controller = loader.getController();

            if      (controller instanceof estoqueController c)                    c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof cadastroProdutoController c)            c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof fornecedorController c)                 c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof cadastroFornecedorController c)         c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof usuarioController c)                    c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof cadastroUsuarioController c)            c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof pedidoController c)                     c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof cadastroPedidoController c)             c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof editarPedidoController c)               c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof compraController c)                     c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof cadastroCompraController c)             c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof CotacaoController c)                    c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof notaFiscalController c)                 c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof movimentacaoController c)               c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof saidaEstoqueController c)               c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof centroCustoController c)                c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof cadastroCentroCustoController c)        c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof historicoSistemaController c)           c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof dashboardController c)                  c.setAreaPrincipal(areaPrincipal);
            else if (controller instanceof cadastroMovimentacaoManualController c) c.setAreaPrincipal(areaPrincipal);

            AnchorPane.setTopAnchor   (tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor  (tela, 0.0);
            AnchorPane.setRightAnchor (tela, 0.0);

            areaPrincipal.getChildren().setAll(tela);

            javafx.application.Platform.runLater(() -> {
                javafx.stage.Stage stage = (javafx.stage.Stage) areaPrincipal.getScene().getWindow();
                if (stage != null && !stage.isMaximized()) stage.setMaximized(true);
            });

            labelPagina.setText(subtitulo);
            btnAcao.setText(textoBotao);

            boolean isDiretor    = PermissaoUtil.temPermissaoExata("DIRETOR");
            boolean isFinanceiro = PermissaoUtil.temPermissaoExata("FINANCEIRO");
            boolean isEstoque    = PermissaoUtil.temPermissaoExata("ESTOQUE");

            boolean mostraBotao = switch (fxmlPath) {
                // Estoque: só FINANCEIRO cadastra produto
                case "/view/estoque.fxml"        -> isFinanceiro;
                // Fornecedor: só FINANCEIRO cadastra
                case "/view/fornecedor.fxml"     -> isFinanceiro;
                // Pedidos: todos cadastram
                case "/view/pedido.fxml"         -> true;
                // Cotações: só FINANCEIRO cadastra, DIRETOR só visualiza
                case "/view/cotacao.fxml"        -> isFinanceiro;
                // Compras: só FINANCEIRO cadastra, DIRETOR só visualiza
                case "/view/compra.fxml"         -> isFinanceiro;
                // Nota Fiscal: FINANCEIRO e ESTOQUE cadastram
                case "/view/notaFiscal.fxml"     -> isFinanceiro || isEstoque;
                // Movimentação: FINANCEIRO e ESTOQUE cadastram (botão abre cadastro manual)
                case "/view/movimentacao.fxml"   -> isFinanceiro || isEstoque;
                // Centro de Custo: DIRETOR e FINANCEIRO cadastram
                case "/view/centroCusto.fxml"    -> isDiretor || isFinanceiro;
                // Usuários: só DIRETOR cadastra
                case "/view/usuario.fxml"        -> isDiretor;
                // Sem botão
                case "/view/saidaEstoque.fxml",
                     "/view/historicoSistema.fxml",
                     "/view/dashboard.fxml"      -> false;
                default                          -> false;
            };

            btnAcao.setVisible(mostraBotao);
            btnAcao.setManaged(mostraBotao);

        } catch (IOException e) {
            System.err.println("Erro ao carregar: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    @FXML private void onDashboardClicked() {
        ativarMenu(menuDashboard);
        carregarTela("/view/dashboard.fxml", "Dashboard de indicadores", "");
    }

    @FXML private void onEstoqueClicked() {
        ativarMenu(menuEstoque);
        carregarTela("/view/estoque.fxml", "Controle e monitore seu inventário", "+ Novo Produto");
    }

    @FXML private void onFornecedoresClicked() {
        ativarMenu(menuFornecedores);
        carregarTela("/view/fornecedor.fxml", "Gerencie seus fornecedores", "+ Novo Fornecedor");
    }

    @FXML private void onPedidosClicked() {
        ativarMenu(menuPedidos);
        carregarTela("/view/pedido.fxml", "Acompanhe seus pedidos", "+ Novo Pedido");
    }

    @FXML private void onCotacoesClicked() {
        ativarMenu(menuCotacoes);
        carregarTela("/view/cotacao.fxml", "Cotações de pedidos", "");
    }

    @FXML private void onUsuariosClicked() {
        ativarMenu(menuUsuarios);
        carregarTela("/view/usuario.fxml", "Usuários cadastrados", "+ Novo Usuário");
    }

    @FXML private void onComprasClicked() {
        ativarMenu(menuCompras);
        carregarTela("/view/compra.fxml", "Histórico de compras realizadas", "");
    }

    @FXML private void onNotaFiscalClicked() {
        ativarMenu(menuNotaFiscal);
        carregarTela("/view/notaFiscal.fxml", "Notas Fiscais", "+ Nova Nota Fiscal");
    }

    @FXML private void onMovimentacaoClicked() {
        ativarMenu(menuMovimentacao);
        carregarTela("/view/movimentacao.fxml", "Movimentações de Estoque", "");
    }

    @FXML private void onSaidaClicked() {
        ativarMenu(menuSaida);
        carregarTela("/view/saidaEstoque.fxml", "Saída de Estoque — Atendimento", "");
    }

    @FXML private void onCentroCustoClicked() {
        ativarMenu(menuCentroCusto);
        carregarTela("/view/centroCusto.fxml", "Gerencie os centros de custo", "+ Novo Centro de Custo");
    }

    @FXML private void onHistoricoClicked() {
        ativarMenu(menuHistorico);
        carregarTela("/view/historicoSistema.fxml", "Histórico de ações do sistema", "");
    }

    @FXML private void onNotificacoesClicked() {
        ativarMenu(menuNotificacoes);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/notificacao.fxml"));
            Node tela = loader.load();
            notificacaoController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            ctrl.setIndexController(this);

            AnchorPane.setTopAnchor   (tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor  (tela, 0.0);
            AnchorPane.setRightAnchor (tela, 0.0);

            areaPrincipal.getChildren().setAll(tela);
            labelPagina.setText("Notificações");
            btnAcao.setVisible(false);
            btnAcao.setManaged(false);
            atualizarBadgeNotificacoes();

        } catch (IOException e) {
            System.err.println("Erro ao abrir notificações: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void onSairClicked() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja realmente sair?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Sair");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                if (pollingTimeline != null) pollingTimeline.stop();
                SessaoUsuario.getInstancia().encerrarSessao();
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
                    Parent root = loader.load();
                    Scene scene = new Scene(root);
                    try {
                        scene.getStylesheets().add(
                                getClass().getResource("/style/loginStyle.css").toExternalForm());
                    } catch (Exception e) {
                        System.out.println("CSS não encontrado.");
                    }
                    Stage stage = (Stage) sidebar.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Sistema de Pedidos - Login");
                    stage.setResizable(true);
                    stage.setMaximized(true);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML private void onBtnAcao() {
        String texto = btnAcao.getText();
        switch (texto) {
            case "+ Novo Fornecedor"       -> carregarTela("/view/cadastroFornecedor.fxml",  "Cadastro de Fornecedor",       "+ Novo Fornecedor");
            case "+ Novo Usuário"          -> carregarTela("/view/cadastroUsuario.fxml",     "Cadastro de Usuário",          "+ Novo Usuário");
            case "+ Novo Pedido"           -> carregarTela("/view/cadastroPedido.fxml",      "Novo Pedido",                  "+ Novo Pedido");
            case "+ Nova Nota Fiscal"      -> carregarTela("/view/registroNotaFiscal.fxml",  "Registrar Nota Fiscal",        "+ Nova Nota Fiscal");
            case "+ Novo Centro de Custo"  -> carregarTela("/view/cadastroCentroCusto.fxml", "Cadastro de Centro de Custo",  "+ Novo Centro de Custo");
            default                        -> carregarTela("/view/cadastroProduto.fxml",     "Cadastro de Produto",          "+ Novo Produto");
        }
    }

    // ── Utilitários ───────────────────────────────────────────────────────────

    private void ativarMenu(HBox menuAtivo) {
        for (HBox m : todosMenus()) {
            if (m != null && m.isManaged())
                m.setStyle(sidebarExpandida ? MENU_INATIVO : MENU_INATIVO_MINI);
        }
        if (menuAtivo != null)
            menuAtivo.setStyle(sidebarExpandida ? MENU_ATIVO : MENU_ATIVO_MINI);
    }

    private void configurarHover() {
        for (HBox menu : todosMenus()) {
            if (menu == null) continue;
            menu.setOnMouseEntered(e -> {
                if (!menu.getStyle().contains("#2563eb"))
                    menu.setStyle((sidebarExpandida ? MENU_INATIVO : MENU_INATIVO_MINI)
                            + " -fx-background-color: #1e1e35;");
            });
            menu.setOnMouseExited(e -> {
                if (!menu.getStyle().contains("#2563eb"))
                    menu.setStyle(sidebarExpandida ? MENU_INATIVO : MENU_INATIVO_MINI);
            });
        }
    }
}