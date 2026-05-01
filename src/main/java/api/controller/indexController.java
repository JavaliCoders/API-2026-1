package api.controller;

import api.DAO.notificacaoDAO;
import api.model.SessaoUsuario;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import api.util.PermissaoUtil;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class indexController implements Initializable {

    // Sidebar
    @FXML private VBox sidebar;

    // Menus
    @FXML private HBox menuEstoque;
    @FXML private HBox menuFornecedores;
    @FXML private HBox menuPedidos;
    @FXML private HBox menuUsuarios;

    // Textos dos menus
    @FXML private Label textoEstoque;
    @FXML private Label textoFornecedores;
    @FXML private Label textoPedidos;
    @FXML private Label textoSair;
    @FXML private Label labelSistema;
    @FXML private Label textoUsuarios;

    // Ícones dos botões
    @FXML private Label iconEstoque;
    @FXML private Label iconPedidos;
    @FXML private Label iconFornecedores;
    @FXML private Label iconSair;
    @FXML private Label iconMenu;

    // Badge de notificações
    @FXML private Label badgeNotificacoes;
    @FXML private Label textoNotificacoes;

    // Área central e cabeçalho
    @FXML private AnchorPane areaPrincipal;
    @FXML private Label labelPagina;
    @FXML private Button btnAcao;

    // HBoxes especiais
    @FXML private HBox hboxTopo;
    @FXML private HBox hboxSair;

    // Controle de estado da sidebar
    private boolean sidebarExpandida = true;

    private static final double SIDEBAR_EXPANDIDA = 220;
    private static final double SIDEBAR_RECOLHIDA  = 64;

    // Estilos com padding normal (sidebar expandida)
    private static final String MENU_ATIVO =
            "-fx-background-color: #2563eb; -fx-background-radius: 8; -fx-padding: 12 16;";
    private static final String MENU_INATIVO =
            "-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 12 16;";

    // Estilos com padding reduzido (sidebar recolhida)
    private static final String MENU_ATIVO_MINI =
            "-fx-background-color: #2563eb; -fx-background-radius: 8; -fx-padding: 12 8;";
    private static final String MENU_INATIVO_MINI =
            "-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 12 8;";

    // Estilo do botão Sair
    private static final String SAIR_ESTILO_BASE =
            "-fx-border-color: #1e1e35 transparent transparent transparent; -fx-border-width: 1; -fx-cursor: hand;";

    // Polling de notificações a cada 30 segundos
    private static final int POLLING_SEGUNDOS = 30;
    private Timeline pollingTimeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        carregarTela("/view/estoque.fxml", "Controle e monitore seu inventário", "+ Novo Produto");
        configurarHover();
        if (!PermissaoUtil.temPermissao("DIRETOR")) {
            menuUsuarios.setVisible(false);
            menuUsuarios.setManaged(false);
        }

        if (!PermissaoUtil.temPermissao("FINANCEIRO")) {
            menuFornecedores.setVisible(false);
            menuFornecedores.setManaged(false);
        }

        // Atualiza o badge imediatamente ao abrir
        atualizarBadgeNotificacoes();

        // Inicia polling periódico para refletir novas notificações em tempo real
        pollingTimeline = new Timeline(
                new KeyFrame(Duration.seconds(POLLING_SEGUNDOS),
                        e -> atualizarBadgeNotificacoes())
        );
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
    }

    // ── Badge de notificações ─────────────────────────────────

    /**
     * Consulta o banco e atualiza o badge azul sobre o ícone 🔔.
     * Chamado na inicialização e pelo polling periódico.
     */
    public void atualizarBadgeNotificacoes() {
        int idUsuario = SessaoUsuario.getInstancia().getIdUsuarioLogado();
        int naoLidas  = notificacaoDAO.contarNaoLidas(idUsuario);

        if (naoLidas > 0) {
            // Exibe a contagem (máximo "99+" para não estourar o badge)
            badgeNotificacoes.setText(naoLidas > 99 ? "99+" : String.valueOf(naoLidas));
            badgeNotificacoes.setVisible(true);
            badgeNotificacoes.setManaged(true);
        } else {
            badgeNotificacoes.setVisible(false);
            badgeNotificacoes.setManaged(false);
        }
    }

    // ── Toggle da sidebar ─────────────────────────────────────

    @FXML
    private void toggleSidebar() {
        if (sidebarExpandida) {
            sidebar.setPrefWidth(SIDEBAR_RECOLHIDA);
            ocultarTextos();
        } else {
            sidebar.setPrefWidth(SIDEBAR_EXPANDIDA);
            exibirTextos();
        }
        sidebarExpandida = !sidebarExpandida;
    }

    private void ocultarTextos() {
        Label[] textos = {labelSistema, textoEstoque, textoFornecedores,
                textoPedidos, textoSair, textoUsuarios, textoNotificacoes};
        for (Label l : textos) {
            l.setVisible(false);
            l.setManaged(false);
        }

        hboxTopo.setAlignment(javafx.geometry.Pos.CENTER);

        HBox[] menus = {menuEstoque, menuFornecedores, menuPedidos, menuUsuarios};
        for (HBox m : menus) {
            m.setAlignment(javafx.geometry.Pos.CENTER);
            m.setSpacing(0);
            if (m.getStyle().contains("#2563eb")) {
                m.setStyle(MENU_ATIVO_MINI);
            } else {
                m.setStyle(MENU_INATIVO_MINI);
            }
        }

        hboxSair.setAlignment(javafx.geometry.Pos.CENTER);
        hboxSair.setSpacing(0);
        hboxSair.setStyle("-fx-padding: 12 8; " + SAIR_ESTILO_BASE);
    }

    private void exibirTextos() {
        Label[] textos = {labelSistema, textoEstoque, textoFornecedores,
                textoPedidos, textoSair, textoUsuarios, textoNotificacoes};
        for (Label l : textos) {
            l.setVisible(true);
            l.setManaged(true);
        }

        hboxTopo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        HBox[] menus = {menuEstoque, menuFornecedores, menuPedidos, menuUsuarios};
        for (HBox m : menus) {
            m.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            m.setSpacing(12);
            if (m.getStyle().contains("#2563eb")) {
                m.setStyle(MENU_ATIVO);
            } else {
                m.setStyle(MENU_INATIVO);
            }
        }

        hboxSair.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        hboxSair.setSpacing(12);
        hboxSair.setStyle("-fx-padding: 16 28; " + SAIR_ESTILO_BASE);
    }

    // ── Navegação ─────────────────────────────────────────────

    private void carregarTela(String fxmlPath, String subtitulo, String textoBotao) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node tela = loader.load();

            Object controller = loader.getController();

            if (controller instanceof estoqueController) {
                ((estoqueController) controller).setAreaPrincipal(areaPrincipal);
            } else if (controller instanceof cadastroProdutoController) {
                ((cadastroProdutoController) controller).setAreaPrincipal(areaPrincipal);
            } else if (controller instanceof fornecedorController) {
                ((fornecedorController) controller).setAreaPrincipal(areaPrincipal);
            } else if (controller instanceof cadastroFornecedorController) {
                ((cadastroFornecedorController) controller).setAreaPrincipal(areaPrincipal);
            } else if (controller instanceof usuarioController) {
                ((usuarioController) controller).setAreaPrincipal(areaPrincipal);
            } else if (controller instanceof cadastroUsuarioController) {
                ((cadastroUsuarioController) controller).setAreaPrincipal(areaPrincipal);
            } else if (controller instanceof pedidoController) {
                ((pedidoController) controller).setAreaPrincipal(areaPrincipal);
            } else if (controller instanceof cadastroPedidoController) {
                ((cadastroPedidoController) controller).setAreaPrincipal(areaPrincipal);
            } else if (controller instanceof editarPedidoController) {
                ((editarPedidoController) controller).setAreaPrincipal(areaPrincipal);
            }

            AnchorPane.setTopAnchor(tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor(tela, 0.0);
            AnchorPane.setRightAnchor(tela, 0.0);

            areaPrincipal.getChildren().setAll(tela);
            labelPagina.setText(subtitulo);
            btnAcao.setText(textoBotao);

            if (fxmlPath.contains("estoque.fxml")) {
                if (!PermissaoUtil.temPermissao("FINANCEIRO")) {
                    btnAcao.setVisible(false);
                    btnAcao.setManaged(false);
                } else {
                    btnAcao.setVisible(true);
                    btnAcao.setManaged(true);
                }
            } else {
                btnAcao.setVisible(true);
                btnAcao.setManaged(true);
            }

        } catch (IOException e) {
            System.err.println("Erro ao carregar: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ── Handlers dos menus ────────────────────────────────────

    @FXML
    private void onEstoqueClicked() {
        ativarMenu(menuEstoque);
        carregarTela("/view/estoque.fxml", "Controle e monitore seu inventário", "+ Novo Produto");
    }

    @FXML
    private void onFornecedoresClicked() {
        ativarMenu(menuFornecedores);
        carregarTela("/view/fornecedor.fxml", "Gerencie seus fornecedores", "+ Novo Fornecedor");
    }

    @FXML
    private void onPedidosClicked() {
        ativarMenu(menuPedidos);
        carregarTela("/view/pedido.fxml", "Acompanhe seus pedidos", "+ Novo Pedido");
    }

    @FXML
    private void onUsuariosClicked() {
        ativarMenu(menuUsuarios);
        carregarTela("/view/usuario.fxml", "Usuários cadastrados", "+ Novo Usuário");
    }

    @FXML
    private void onNotificacoesClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/notificacao.fxml"));
            Node tela = loader.load();

            notificacaoController controller = loader.getController();
            controller.setAreaPrincipal(areaPrincipal);
            // Passa referência para o controller de notificações poder
            // atualizar o badge ao marcar como lidas
            controller.setIndexController(this);

            AnchorPane.setTopAnchor(tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor(tela, 0.0);
            AnchorPane.setRightAnchor(tela, 0.0);

            areaPrincipal.getChildren().setAll(tela);

            labelPagina.setText("Notificações");
            btnAcao.setVisible(false);
            btnAcao.setManaged(false);

            // Zera o badge assim que o usuário abre a tela de notificações
            atualizarBadgeNotificacoes();

        } catch (IOException e) {
            System.err.println("Erro ao abrir notificações: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onSairClicked() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja realmente sair?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Sair");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // Para o polling antes de sair
                if (pollingTimeline != null) pollingTimeline.stop();

                SessaoUsuario.getInstancia().encerrarSessao();

                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/view/login.fxml"));
                    AnchorPane root = loader.load();

                    Scene scene = new Scene(root, 900, 600);

                    try {
                        scene.getStylesheets().add(
                                getClass().getResource("/style/loginStyle.css")
                                        .toExternalForm());
                    } catch (Exception e) {
                        System.out.println("CSS não encontrado.");
                    }

                    Stage stage = (Stage) sidebar.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Sistema de Pedidos - Login");
                    stage.setResizable(false);
                    stage.show();

                } catch (IOException e) {
                    System.err.println("Erro ao voltar para login: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void onBtnAcao() {
        if (btnAcao.getText().equals("+ Novo Fornecedor")) {
            carregarTela("/view/cadastroFornecedor.fxml", "Cadastro de Fornecedor", "+ Novo Fornecedor");
        } else if (btnAcao.getText().equals("+ Novo Usuário")) {
            carregarTela("/view/cadastroUsuario.fxml", "Cadastro de Usuário", "+ Novo Usuário");
        } else if (btnAcao.getText().equals("+ Novo Pedido")) {
            carregarTela("/view/cadastroPedido.fxml", "Novo Pedido", "+ Novo Pedido");
        } else {
            carregarTela("/view/cadastroProduto.fxml", "Cadastro de Produto", "+ Novo Produto");
        }
    }

    // ── Utilitários ───────────────────────────────────────────

    private void ativarMenu(HBox menuAtivo) {
        HBox[] menus = {menuEstoque, menuFornecedores, menuPedidos, menuUsuarios};
        for (HBox m : menus) {
            m.setStyle(sidebarExpandida ? MENU_INATIVO : MENU_INATIVO_MINI);
        }
        menuAtivo.setStyle(sidebarExpandida ? MENU_ATIVO : MENU_ATIVO_MINI);
    }

    private void configurarHover() {
        HBox[] menus = {menuFornecedores, menuPedidos, menuEstoque, menuUsuarios};
        for (HBox menu : menus) {
            menu.setOnMouseEntered(e -> {
                if (!menu.getStyle().contains("#2563eb")) {
                    String base = sidebarExpandida ? MENU_INATIVO : MENU_INATIVO_MINI;
                    menu.setStyle(base + " -fx-background-color: #1e1e35; -fx-font-weight: bold;");
                }
            });
            menu.setOnMouseExited(e -> {
                if (!menu.getStyle().contains("#2563eb")) {
                    menu.setStyle(sidebarExpandida ? MENU_INATIVO : MENU_INATIVO_MINI);
                }
            });
        }
    }
}