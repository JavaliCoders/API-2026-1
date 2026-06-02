package api.controller;

import api.DAO.dashboardDAO;
import api.model.DashboardData;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dashboardController implements Initializable {

    @FXML private TilePane kpiGrid;
    @FXML private StackPane comparacaoChartBox;
    @FXML private StackPane valorCompradoChartBox;
    @FXML private StackPane statusEstoqueChartBox;
    @FXML private StackPane setorChartBox;
    @FXML private StackPane centroCustoChartBox;
    @FXML private StackPane fornecedorChartBox;
    @FXML private StackPane movimentacaoChartBox;
    @FXML private VBox listaMaisSaem;
    @FXML private VBox listaMenosSaem;
    @FXML private VBox listaUsuarios;
    @FXML private Label lblFonte;

    @FXML private ToggleButton periodoMes;
    @FXML private ToggleButton periodoTrimestre;
    @FXML private ToggleButton periodoAno;
    @FXML private ToggleButton perfilDiretor;
    @FXML private ToggleButton perfilFinanceiro;
    @FXML private ToggleButton perfilEstoque;
    @FXML private StackPane overlayDetalhes;
    @FXML private StackPane detalheConteudo;
    @FXML private VBox detalheVariacaoCard;
    @FXML private Label detalheTitulo;
    @FXML private Label detalheSubtitulo;
    @FXML private Label detalheAtual;
    @FXML private Label detalheAnterior;
    @FXML private Label detalheVariacao;
    @FXML private Button tabComparacao;
    @FXML private Button tabAno;
    @FXML private Button tabRegistros;
    @FXML private Button btnExportar;
    @FXML private Button btnFecharDetalhes;

    private static final NumberFormat NUMERO = NumberFormat.getIntegerInstance(new Locale("pt", "BR"));
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("[-+]?\\d+(?:[,.]\\d+)?");
    private static final Duration ANIMACAO_RAPIDA = Duration.millis(140);
    private static final Duration ANIMACAO_MEDIA = Duration.millis(220);
    private static final Duration ANIMACAO_GRAFICO = Duration.millis(320);
    private static final Map<String, String[]> ICONES_LUCIDE = criarIconesLucide();
    private static final String[] CORES_GRAFICO = {
            "#2563eb", "#16a34a", "#f59e0b", "#dc2626", "#64748b", "#0ea5e9", "#14b8a6", "#8b5cf6"
    };

    private static final String TOGGLE_SELECTED =
            "-fx-background-color: white; -fx-text-fill: #2563eb; -fx-font-weight: bold; " +
                    "-fx-background-radius: 6; -fx-border-color: transparent; -fx-padding: 7 12;";
    private static final String TOGGLE_DEFAULT =
            "-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-weight: bold; " +
                    "-fx-background-radius: 6; -fx-border-color: transparent; -fx-padding: 7 12;";

    private DashboardData dashboardData;
    private DetalheDashboard detalheAberto;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarIconesFixos();
        configurarToggleGroups();
        carregarDashboard();
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
    }

    @FXML
    private void onFiltroAlterado() {
        garantirSelecao();
        carregarDashboard();
    }

    private void configurarIconesFixos() {
        if (btnExportar != null) {
            btnExportar.setGraphic(criarLucideIcone("download", "#64748b", 16));
            btnExportar.setGraphicTextGap(6);
        }
        if (btnFecharDetalhes != null) {
            btnFecharDetalhes.setText("");
            btnFecharDetalhes.setGraphic(criarLucideIcone("x", "#475569", 20));
        }
    }

    @FXML
    private void fecharDetalhes() {
        if (!overlayDetalhes.isVisible()) return;

        Node modal = modalDetalhes();
        FadeTransition fade = new FadeTransition(ANIMACAO_MEDIA, overlayDetalhes);
        fade.setFromValue(overlayDetalhes.getOpacity());
        fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(ANIMACAO_MEDIA, modal);
        scale.setFromX(modal.getScaleX());
        scale.setFromY(modal.getScaleY());
        scale.setToX(0.98);
        scale.setToY(0.98);

        ParallelTransition transition = new ParallelTransition(fade, scale);
        transition.setOnFinished(e -> {
            overlayDetalhes.setVisible(false);
            overlayDetalhes.setManaged(false);
            overlayDetalhes.setOpacity(1);
            modal.setScaleX(1);
            modal.setScaleY(1);
        });
        transition.play();
    }

    @FXML
    private void onDetalheComparacao() {
        if (detalheAberto == null) return;
        selecionarAbaDetalhe(tabComparacao);
        trocarConteudoDetalhe(criarDetalheComparacao(detalheAberto));
    }

    @FXML
    private void onDetalheAno() {
        if (detalheAberto == null) return;
        selecionarAbaDetalhe(tabAno);
        trocarConteudoDetalhe(criarDetalheAno(detalheAberto));
    }

    @FXML
    private void onDetalheRegistros() {
        if (detalheAberto == null) return;
        selecionarAbaDetalhe(tabRegistros);
        trocarConteudoDetalhe(criarDetalheRegistros(detalheAberto));
    }

    @FXML
    private void onExportarDetalhes() {
        if (detalheAberto == null) return;
        detalheSubtitulo.setText(detalheAberto.subtitulo + " - exportacao preparada.");
    }

    private void configurarToggleGroups() {
        ToggleGroup grupoPeriodo = new ToggleGroup();
        periodoMes.setToggleGroup(grupoPeriodo);
        periodoTrimestre.setToggleGroup(grupoPeriodo);
        periodoAno.setToggleGroup(grupoPeriodo);
        periodoMes.setSelected(true);

        ToggleGroup grupoPerfil = new ToggleGroup();
        perfilDiretor.setToggleGroup(grupoPerfil);
        perfilFinanceiro.setToggleGroup(grupoPerfil);
        perfilEstoque.setToggleGroup(grupoPerfil);
        perfilDiretor.setSelected(true);

        atualizarEstiloToggles();
    }

    private void garantirSelecao() {
        if (!periodoMes.isSelected() && !periodoTrimestre.isSelected() && !periodoAno.isSelected()) {
            periodoMes.setSelected(true);
        }
        if (!perfilDiretor.isSelected() && !perfilFinanceiro.isSelected() && !perfilEstoque.isSelected()) {
            perfilDiretor.setSelected(true);
        }
        atualizarEstiloToggles();
    }

    private void atualizarEstiloToggles() {
        ToggleButton[] toggles = {periodoMes, periodoTrimestre, periodoAno, perfilDiretor, perfilFinanceiro, perfilEstoque};
        for (ToggleButton toggle : toggles) {
            toggle.setStyle(toggle.isSelected() ? TOGGLE_SELECTED : TOGGLE_DEFAULT);
        }
    }

    private void carregarDashboard() {
        atualizarEstiloToggles();
        dashboardData = dashboardDAO.carregar(periodoSelecionado());
        lblFonte.setText(dashboardData.getSourceLabel());
        renderKpis();
        renderCharts();
        renderLists();
    }

    private String periodoSelecionado() {
        if (periodoAno.isSelected()) return "ANO";
        if (periodoTrimestre.isSelected()) return "TRIMESTRE";
        return "MES";
    }

    private String perfilSelecionado() {
        if (perfilFinanceiro.isSelected()) return "FINANCEIRO";
        if (perfilEstoque.isSelected()) return "ESTOQUE";
        return "DIRETOR";
    }

    private void renderKpis() {
        kpiGrid.getChildren().clear();
        for (DashboardData.Metric metric : dashboardData.getMetricsForProfile(perfilSelecionado())) {
            kpiGrid.getChildren().add(criarKpiCard(metric));
        }
    }

    private Button criarKpiCard(DashboardData.Metric metric) {
        Button card = new Button();
        card.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        card.setAlignment(Pos.TOP_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add("kpi-card");
        card.setOnAction(e -> abrirDetalhesMetrica(metric));
        animarHover(card, 1.015);

        VBox texto = new VBox(6);
        Label titulo = new Label(metric.getTitle().toUpperCase(new Locale("pt", "BR")));
        titulo.getStyleClass().add("kpi-title");
        titulo.setWrapText(true);

        Label valor = new Label(metric.getValue());
        valor.getStyleClass().add("kpi-value");
        valor.setWrapText(true);

        texto.getChildren().addAll(titulo, valor);
        if (!metric.getDetail().isBlank()) {
            Label detalhe = new Label(metric.getDetail());
            detalhe.getStyleClass().add("kpi-detail");
            detalhe.setStyle(detalheStyle(metric.getVariant()));
            detalhe.setWrapText(true);
            texto.getChildren().add(detalhe);
        }

        StackPane icon = new StackPane();
        icon.setMinSize(42, 42);
        icon.setPrefSize(42, 42);
        icon.setMaxSize(42, 42);
        icon.setStyle(iconStyle(metric.getVariant()));
        icon.getChildren().add(criarLucideIcone(iconKey(metric), "white", 20));

        HBox linha = new HBox(12, texto, new Region(), icon);
        HBox.setHgrow(linha.getChildren().get(1), Priority.ALWAYS);
        linha.setAlignment(Pos.TOP_LEFT);

        card.setGraphic(linha);
        return card;
    }

    private String iconStyle(String variant) {
        return switch (variant) {
            case "success" -> "-fx-background-color: #16a34a; -fx-text-fill: white; -fx-background-radius: 8;";
            case "warning" -> "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 8;";
            case "danger" -> "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-background-radius: 8;";
            case "info" -> "-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-background-radius: 8;";
            case "primary" -> "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 8;";
            default -> "-fx-background-color: #475569; -fx-text-fill: white; -fx-background-radius: 8;";
        };
    }

    private String detalheStyle(String variant) {
        return switch (variant) {
            case "success" -> "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-background-radius: 999; -fx-padding: 3 8;";
            case "warning" -> "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-background-radius: 999; -fx-padding: 3 8;";
            case "danger" -> "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 999; -fx-padding: 3 8;";
            default -> "-fx-text-fill: #64748b;";
        };
    }

    private void renderCharts() {
        comparacaoChartBox.getChildren().setAll(criarComparacaoMensal());
        valorCompradoChartBox.getChildren().setAll(criarEvolucaoValor());
        statusEstoqueChartBox.getChildren().setAll(criarPieChart(dashboardData.getStockStatus()));
        setorChartBox.getChildren().setAll(criarBarChartSimples("Setor", "Solicita\u00e7\u00f5es", dashboardData.getRequestsBySector()));
        centroCustoChartBox.getChildren().setAll(criarPieChart(dashboardData.getRequestsByCostCenter()));
        fornecedorChartBox.getChildren().setAll(criarHorizontalBarChart(dashboardData.getPurchasedBySupplier()));
        movimentacaoChartBox.getChildren().setAll(criarMovimentacaoChart());
    }

    private Node criarComparacaoMensal() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.getStyleClass().add("dashboard-chart");
        chart.setAnimated(true);
        chart.setLegendVisible(true);
        chart.setVerticalGridLinesVisible(false);
        chart.setCategoryGap(18);
        chart.setBarGap(4);
        chart.setMinHeight(260);

        XYChart.Series<String, Number> aprovados = new XYChart.Series<>();
        aprovados.setName("Aprovados");
        XYChart.Series<String, Number> rejeitados = new XYChart.Series<>();
        rejeitados.setName("Rejeitados");
        XYChart.Series<String, Number> finalizados = new XYChart.Series<>();
        finalizados.setName("Finalizados");

        for (DashboardData.SeriesPoint point : dashboardData.getMonthlyComparison()) {
            aprovados.getData().add(new XYChart.Data<>(point.getLabel(), point.getFirst()));
            rejeitados.getData().add(new XYChart.Data<>(point.getLabel(), point.getSecond()));
            finalizados.getData().add(new XYChart.Data<>(point.getLabel(), point.getThird()));
        }

        chart.getData().add(aprovados);
        chart.getData().add(rejeitados);
        chart.getData().add(finalizados);
        return chart;
    }

    private Node criarEvolucaoValor() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.getStyleClass().add("dashboard-chart");
        chart.setAnimated(true);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setVerticalGridLinesVisible(false);
        chart.setMinHeight(260);

        XYChart.Series<String, Number> valores = new XYChart.Series<>();
        valores.setName("Valor comprado");
        for (DashboardData.NameValue value : dashboardData.getPurchasedValueEvolution()) {
            valores.getData().add(new XYChart.Data<>(value.getName(), value.getValue()));
        }
        chart.getData().add(valores);
        return chart;
    }

    private Node criarPieChart(List<DashboardData.NameValue> values) {
        PieChart chart = new PieChart();
        chart.getStyleClass().add("dashboard-chart");
        chart.setAnimated(true);
        chart.setLegendVisible(false);
        chart.setLabelsVisible(false);
        chart.setMinHeight(220);
        chart.setPrefHeight(230);
        for (DashboardData.NameValue value : values) {
            chart.getData().add(new PieChart.Data(value.getName(), value.getValue()));
        }
        return criarGraficoComLegenda(chart, criarLegendaValores(values));
    }

    private Node criarBarChartSimples(String eixoX, String eixoY, List<DashboardData.NameValue> values) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(eixoX);
        yAxis.setLabel(eixoY);
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.getStyleClass().add("dashboard-chart");
        chart.setAnimated(true);
        chart.setLegendVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setMinHeight(220);
        chart.setPrefHeight(230);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < values.size(); i++) {
            DashboardData.NameValue value = values.get(i);
            XYChart.Data<String, Number> data = new XYChart.Data<>(value.getName(), value.getValue());
            aplicarCorDado(data, corGrafico(i));
            series.getData().add(data);
        }
        chart.getData().add(series);
        return criarGraficoComLegenda(chart, criarLegendaValores(values));
    }

    private Node criarHorizontalBarChart(List<DashboardData.NameValue> values) {
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();
        BarChart<Number, String> chart = new BarChart<>(xAxis, yAxis);
        chart.getStyleClass().add("dashboard-chart");
        chart.setAnimated(true);
        chart.setLegendVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setMinHeight(220);
        chart.setPrefHeight(230);

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        for (int i = 0; i < values.size(); i++) {
            DashboardData.NameValue value = values.get(i);
            XYChart.Data<Number, String> data = new XYChart.Data<>(value.getValue(), value.getName());
            aplicarCorDado(data, corGrafico(i));
            series.getData().add(data);
        }
        chart.getData().add(series);
        return criarGraficoComLegenda(chart, criarLegendaValores(values));
    }

    private Node criarMovimentacaoChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.getStyleClass().add("dashboard-chart");
        chart.setAnimated(true);
        chart.setLegendVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setCategoryGap(20);
        chart.setBarGap(5);
        chart.setMinHeight(220);
        chart.setPrefHeight(230);

        XYChart.Series<String, Number> entradas = new XYChart.Series<>();
        entradas.setName("Entradas");
        XYChart.Series<String, Number> saidas = new XYChart.Series<>();
        saidas.setName("Sa\u00eddas");

        for (DashboardData.SeriesPoint point : dashboardData.getStockMovement()) {
            entradas.getData().add(new XYChart.Data<>(point.getLabel(), point.getFirst()));
            saidas.getData().add(new XYChart.Data<>(point.getLabel(), point.getSecond()));
        }

        chart.getData().add(entradas);
        chart.getData().add(saidas);
        return criarGraficoComLegenda(chart, List.of(
                new LegendaItem("Entradas", corGrafico(0)),
                new LegendaItem("Sa\u00eddas", corGrafico(1))
        ));
    }

    private Node criarGraficoComLegenda(Node chart, List<LegendaItem> itens) {
        VBox container = new VBox(6);
        container.setAlignment(Pos.CENTER);
        VBox.setVgrow(chart, Priority.ALWAYS);
        container.getChildren().addAll(chart, criarLegenda(itens));
        return container;
    }

    private TilePane criarLegenda(List<LegendaItem> itens) {
        TilePane legenda = new TilePane();
        legenda.getStyleClass().add("dashboard-custom-legend");
        legenda.setAlignment(Pos.CENTER);
        legenda.setTileAlignment(Pos.CENTER_LEFT);
        legenda.setHgap(12);
        legenda.setVgap(6);
        legenda.setPrefColumns(Math.min(3, Math.max(1, itens.size())));

        for (LegendaItem item : itens) {
            Region marcador = new Region();
            marcador.getStyleClass().add("legend-marker");
            marcador.setStyle("-fx-background-color: " + item.cor + ";");

            Label texto = new Label(item.rotulo);
            texto.getStyleClass().add("legend-label");
            texto.setWrapText(true);
            texto.setMaxWidth(120);

            HBox linha = new HBox(6, marcador, texto);
            linha.getStyleClass().add("legend-item");
            linha.setAlignment(Pos.CENTER_LEFT);
            legenda.getChildren().add(linha);
        }

        return legenda;
    }

    private List<LegendaItem> criarLegendaValores(List<DashboardData.NameValue> values) {
        List<LegendaItem> itens = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            itens.add(new LegendaItem(values.get(i).getName(), corGrafico(i)));
        }
        return itens;
    }

    private String corGrafico(int index) {
        return CORES_GRAFICO[index % CORES_GRAFICO.length];
    }

    private <X, Y> void aplicarCorDado(XYChart.Data<X, Y> data, String cor) {
        data.nodeProperty().addListener((obs, oldNode, node) -> {
            if (node != null) {
                node.setStyle("-fx-bar-fill: " + cor + ";");
            }
        });
    }

    private void renderLists() {
        preencherListaRanking(listaMaisSaem, dashboardData.getTopOutgoingProducts(), false,
                "Produtos que mais saem", "Top 5 do periodo selecionado");
        preencherListaRanking(listaMenosSaem, dashboardData.getLowOutgoingProducts(), true,
                "Produtos que menos saem", "Itens com baixo giro");
        preencherListaUsuarios();
    }

    private void preencherListaRanking(VBox destino, List<DashboardData.NameValue> values, boolean muted,
                                       String titulo, String subtitulo) {
        destino.getChildren().clear();
        for (int i = 0; i < values.size(); i++) {
            DashboardData.NameValue value = values.get(i);
            HBox row = criarLinhaRanking(i + 1, value.getName(), NUMERO.format(value.getValue()), muted);
            row.setOnMouseClicked(e -> abrirDetalhesRanking(titulo, subtitulo, value, values));
            animarHover(row, 1.01);
            destino.getChildren().add(row);
        }
    }

    private HBox criarLinhaRanking(int posicao, String nome, String valor, boolean muted) {
        Label badge = new Label(String.valueOf(posicao));
        badge.setAlignment(Pos.CENTER);
        badge.setMinSize(26, 26);
        badge.setPrefSize(26, 26);
        badge.setStyle(muted
                ? "-fx-background-color: #64748b; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold;"
                : "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold;");

        Label nomeLabel = new Label(nome);
        nomeLabel.getStyleClass().add("list-name");
        nomeLabel.setMaxWidth(Double.MAX_VALUE);

        Label valorLabel = new Label(valor);
        valorLabel.getStyleClass().add("list-value");

        HBox row = new HBox(10, badge, nomeLabel, new Region(), valorLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("list-row");
        row.setStyle("-fx-cursor: hand;");
        HBox.setHgrow(row.getChildren().get(2), Priority.ALWAYS);
        return row;
    }

    private void preencherListaUsuarios() {
        listaUsuarios.getChildren().clear();
        for (DashboardData.NameValue value : dashboardData.getTopRequestingUsers()) {
            Label iniciais = new Label(iniciais(value.getName()));
            iniciais.setAlignment(Pos.CENTER);
            iniciais.setMinSize(30, 30);
            iniciais.setPrefSize(30, 30);
            iniciais.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 999; -fx-font-weight: bold; -fx-font-size: 11px;");

            Label nome = new Label(value.getName());
            nome.getStyleClass().add("list-name");
            Label total = new Label(NUMERO.format(value.getValue()) + " pedidos");
            total.getStyleClass().add("list-value");

            HBox row = new HBox(10, iniciais, nome, new Region(), total);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("list-row");
            row.setStyle("-fx-cursor: hand;");
            row.setOnMouseClicked(e -> abrirDetalhesRanking("Usuarios que mais solicitam",
                    "Pedidos por solicitante", value, dashboardData.getTopRequestingUsers()));
            animarHover(row, 1.01);
            HBox.setHgrow(row.getChildren().get(2), Priority.ALWAYS);
            listaUsuarios.getChildren().add(row);
        }
    }

    private void abrirDetalhesMetrica(DashboardData.Metric metric) {
        detalheAberto = criarDetalheMetrica(metric);
        preencherCabecalhoDetalhe();
        onDetalheComparacao();
        exibirOverlayDetalhes();
    }

    private void abrirDetalhesRanking(String titulo, String subtitulo, DashboardData.NameValue selecionado,
                                      List<DashboardData.NameValue> origem) {
        List<DashboardData.SeriesPoint> comparacao = criarTendenciaSintetica(selecionado.getValue());
        detalheAberto = new DetalheDashboard(
                selecionado.getName(),
                subtitulo + " - " + titulo,
                NUMERO.format(selecionado.getValue()),
                NUMERO.format(Math.max(0, Math.round(selecionado.getValue() * 0.82))),
                "+18,0%",
                comparacao,
                origem,
                origem
        );
        preencherCabecalhoDetalhe();
        onDetalheComparacao();
        exibirOverlayDetalhes();
    }

    private DetalheDashboard criarDetalheMetrica(DashboardData.Metric metric) {
        String titulo = metric.getTitle();
        String atual = normalizarValorAtual(titulo, metric.getValue());
        String variacao = normalizarVariacao(metric);
        String anterior = calcularValorAnterior(metric, atual, variacao);

        List<DashboardData.SeriesPoint> comparacao = escolherComparacao(titulo);
        List<DashboardData.NameValue> porAno = escolherSerieAnual(titulo);
        List<DashboardData.NameValue> registros = escolherRegistros(titulo);

        return new DetalheDashboard(
                titulo,
                subtituloDetalhe(titulo),
                atual,
                anterior,
                variacao,
                comparacao,
                porAno,
                registros
        );
    }

    private void preencherCabecalhoDetalhe() {
        detalheTitulo.setText(detalheAberto.titulo);
        detalheSubtitulo.setText(detalheAberto.subtitulo);
        detalheAtual.setText(detalheAberto.atual);
        detalheAnterior.setText(detalheAberto.anterior);
        detalheVariacao.setText(detalheAberto.variacao);

        String cor = detalheAberto.variacao.startsWith("-") ? "#dc2626" : "#166534";
        if (detalheAberto.titulo.toLowerCase(new Locale("pt", "BR")).contains("tempo")) {
            cor = detalheAberto.variacao.startsWith("-") ? "#166534" : "#dc2626";
        }
        detalheVariacao.setStyle("-fx-text-fill: " + cor + ";");
        detalheVariacao.setGraphic(criarLucideIcone(detalheAberto.variacao.startsWith("-")
                ? "arrow-down-right" : "arrow-up-right", cor, 22));
        detalheVariacao.setGraphicTextGap(5);
        detalheVariacao.setContentDisplay(ContentDisplay.LEFT);
    }

    private void selecionarAbaDetalhe(Button selecionada) {
        Button[] tabs = {tabComparacao, tabAno, tabRegistros};
        for (Button tab : tabs) {
            tab.getStyleClass().setAll(tab == selecionada ? "details-tab-selected" : "details-tab");
        }
    }

    private Node criarDetalheComparacao(DetalheDashboard detalhe) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.getStyleClass().add("details-chart");
        chart.setAnimated(true);
        chart.setLegendVisible(true);
        chart.setVerticalGridLinesVisible(false);
        chart.setCategoryGap(28);
        chart.setBarGap(6);

        XYChart.Series<String, Number> atual = new XYChart.Series<>();
        atual.setName("Periodo atual");
        XYChart.Series<String, Number> anterior = new XYChart.Series<>();
        anterior.setName("Periodo anterior");

        for (DashboardData.SeriesPoint point : detalhe.comparacao) {
            atual.getData().add(new XYChart.Data<>(point.getLabel(), point.getFirst()));
            anterior.getData().add(new XYChart.Data<>(point.getLabel(), point.getSecond()));
        }

        chart.getData().add(atual);
        chart.getData().add(anterior);
        return chart;
    }

    private Node criarDetalheAno(DetalheDashboard detalhe) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.getStyleClass().add("details-chart");
        chart.setAnimated(true);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setVerticalGridLinesVisible(false);

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName(detalhe.titulo);
        for (DashboardData.NameValue item : detalhe.porAno) {
            serie.getData().add(new XYChart.Data<>(item.getName(), item.getValue()));
        }
        chart.getData().add(serie);

        TilePane cards = new TilePane(8, 8);
        cards.setPrefColumns(5);
        cards.setPrefTileWidth(120);
        cards.setPrefTileHeight(68);
        cards.getStyleClass().add("details-year-grid");

        for (DashboardData.NameValue item : detalhe.porAno) {
            VBox card = criarCardAno(item);
            cards.getChildren().add(card);
        }
        VBox conteudo = new VBox(14, chart, cards);
        conteudo.setFillWidth(true);
        return conteudo;
    }

    private Node criarDetalheRegistros(DetalheDashboard detalhe) {
        VBox tabela = new VBox(0);
        tabela.getStyleClass().add("details-table");

        HBox header = criarLinhaTabela("Registro", "Categoria", "Situacao", "Valor", true);
        tabela.getChildren().add(header);

        int index = 0;
        for (DashboardData.NameValue item : detalhe.registros) {
            index++;
            HBox row = criarLinhaTabela(
                    item.getName(),
                    categoriaRegistro(detalhe.titulo, index),
                    statusRegistro(detalhe.titulo, item.getValue(), index),
                    formatRegistro(item.getValue()),
                    false
            );
            animarHover(row, 1.006);
            tabela.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(tabela);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("details-scroll");
        return scroll;
    }

    private VBox criarCardAno(DashboardData.NameValue item) {
        Label ano = new Label(item.getName());
        ano.getStyleClass().add("details-year-label");
        ano.setGraphic(criarLucideIcone("calendar", "#64748b", 13));
        ano.setGraphicTextGap(5);

        Label valor = new Label(formatRegistro(item.getValue()));
        valor.getStyleClass().add("details-year-value");
        valor.setWrapText(true);

        VBox card = new VBox(5, ano, valor);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("details-year-card");
        return card;
    }

    private HBox criarLinhaTabela(String primeira, String segunda, String terceira, String quarta, boolean header) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add(header ? "details-table-header" : "details-table-row");

        Label col1 = criarCelulaTabela(primeira, header, false);
        Label col2 = criarCelulaTabela(segunda, header, false);
        Label col3 = criarCelulaTabela(terceira, header, false);
        Label col4 = criarCelulaTabela(quarta, header, true);

        col1.setPrefWidth(300);
        col2.setPrefWidth(180);
        col3.setPrefWidth(160);
        col4.setPrefWidth(150);

        row.getChildren().addAll(col1, col2, col3, col4);
        return row;
    }

    private Label criarCelulaTabela(String texto, boolean header, boolean direita) {
        Label label = new Label(texto);
        label.setWrapText(true);
        label.setAlignment(direita ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        label.getStyleClass().add(header ? "details-table-header-cell" : "details-table-cell");
        if (direita && !header) {
            label.getStyleClass().add("numeric");
        }
        return label;
    }

    private String categoriaRegistro(String titulo, int index) {
        String lower = titulo.toLowerCase(new Locale("pt", "BR"));
        if (lower.contains("fornecedor") || lower.contains("valor")) return "Compras";
        if (lower.contains("estoque") || lower.contains("sku") || lower.contains("entrada") || lower.contains("saida") || lower.contains("sa\u00edda")) return "Estoque";
        if (lower.contains("usuario") || lower.contains("usu\u00e1rio")) return "Solicitante";
        return "Pedido " + String.format("%03d", index);
    }

    private String statusRegistro(String titulo, double valor, int index) {
        String lower = titulo.toLowerCase(new Locale("pt", "BR"));
        if (lower.contains("rejeitado")) return "Negado";
        if (lower.contains("aguardando")) return "Aguardando";
        if (lower.contains("m\u00ednimo") || lower.contains("minimo")) return valor <= 10 ? "Critico" : "Baixo";
        if (lower.contains("menos")) return "Baixo giro";
        if (lower.contains("entrada")) return "Recebido";
        if (lower.contains("saida") || lower.contains("sa\u00edda")) return "Retirado";
        return index % 3 == 0 ? "Finalizado" : "Aprovado";
    }

    private void exibirOverlayDetalhes() {
        Node modal = modalDetalhes();
        overlayDetalhes.setManaged(true);
        overlayDetalhes.setVisible(true);
        overlayDetalhes.setOpacity(0);
        modal.setScaleX(0.97);
        modal.setScaleY(0.97);

        FadeTransition fade = new FadeTransition(ANIMACAO_MEDIA, overlayDetalhes);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(ANIMACAO_MEDIA, modal);
        scale.setFromX(0.97);
        scale.setFromY(0.97);
        scale.setToX(1);
        scale.setToY(1);

        new ParallelTransition(fade, scale).play();
    }

    private Node modalDetalhes() {
        return overlayDetalhes.getChildren().isEmpty() ? overlayDetalhes : overlayDetalhes.getChildren().get(0);
    }

    private void trocarConteudoDetalhe(Node conteudo) {
        prepararAnimacaoConteudoDetalhe(conteudo);
        detalheConteudo.getChildren().setAll(conteudo);
        executarAposLayout(() -> animarConteudoDetalhe(conteudo));
    }

    private void prepararAnimacaoConteudoDetalhe(Node conteudo) {
        List<Node> charts = coletarGraficos(conteudo);
        for (Node chart : charts) {
            prepararNoEntrada(chart, 12);
        }

        for (Node header : coletarPorClasse(conteudo, "details-table-header")) {
            prepararNoEntrada(header, 6);
        }

        for (Node row : coletarPorClasse(conteudo, "details-table-row")) {
            prepararNoEntrada(row, 10);
        }

        for (Node card : coletarPorClasse(conteudo, "details-year-card")) {
            prepararNoEntrada(card, 10);
        }
    }

    private void animarConteudoDetalhe(Node conteudo) {
        List<Node> charts = coletarGraficos(conteudo);
        for (Node chart : charts) {
            animarGraficoDetalhe(chart);
        }

        animarEntradaEscalonada(coletarPorClasse(conteudo, "details-table-header"), 0);
        animarEntradaEscalonada(coletarPorClasse(conteudo, "details-table-row"), 38);
        animarEntradaEscalonada(coletarPorClasse(conteudo, "details-year-card"), 45);
    }

    private void executarAposLayout(Runnable runnable) {
        Platform.runLater(() -> Platform.runLater(runnable));
    }

    private void prepararNoEntrada(Node node, double translateY) {
        node.setOpacity(0);
        node.setTranslateY(translateY);
    }

    private List<Node> coletarGraficos(Node root) {
        List<Node> nodes = new ArrayList<>();
        coletarGraficos(root, nodes);
        return nodes;
    }

    private void coletarGraficos(Node node, List<Node> destino) {
        if (node == null) return;
        if ((node instanceof XYChart<?, ?> || node instanceof PieChart) && !destino.contains(node)) {
            destino.add(node);
        }
        for (Node child : filhosDiretos(node)) {
            coletarGraficos(child, destino);
        }
    }

    private List<Node> coletarPorClasse(Node root, String styleClass) {
        List<Node> nodes = new ArrayList<>();
        coletarPorClasse(root, styleClass, nodes);
        return nodes;
    }

    private void coletarPorClasse(Node node, String styleClass, List<Node> destino) {
        if (node == null) return;
        if (node.getStyleClass().contains(styleClass) && !destino.contains(node)) {
            destino.add(node);
        }
        for (Node child : filhosDiretos(node)) {
            coletarPorClasse(child, styleClass, destino);
        }
    }

    private List<Node> filhosDiretos(Node node) {
        List<Node> children = new ArrayList<>();
        if (node instanceof ScrollPane scroll && scroll.getContent() != null) {
            children.add(scroll.getContent());
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (!children.contains(child)) {
                    children.add(child);
                }
            }
        }
        return children;
    }

    private void animarGraficoDetalhe(Node chart) {
        chart.setOpacity(0);
        chart.setTranslateY(12);

        FadeTransition fade = new FadeTransition(ANIMACAO_MEDIA, chart);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(ANIMACAO_MEDIA, chart);
        slide.setFromY(12);
        slide.setToY(0);

        new ParallelTransition(fade, slide).play();

        Platform.runLater(() -> Platform.runLater(() -> {
            if (chart instanceof Parent parent) {
                parent.applyCss();
                parent.layout();
            }

            List<Node> barras = new ArrayList<>(chart.lookupAll(".chart-bar"));
            for (int i = 0; i < barras.size(); i++) {
                animarNoGrafico(barras.get(i), i, true);
            }

            List<Node> linhas = new ArrayList<>(chart.lookupAll(".chart-series-line"));
            for (int i = 0; i < linhas.size(); i++) {
                animarNoGrafico(linhas.get(i), i, false);
            }

            List<Node> pontos = new ArrayList<>(chart.lookupAll(".chart-line-symbol"));
            for (int i = 0; i < pontos.size(); i++) {
                animarNoGrafico(pontos.get(i), i, false);
            }

            List<Node> fatias = new ArrayList<>(chart.lookupAll(".chart-pie"));
            for (int i = 0; i < fatias.size(); i++) {
                animarNoGrafico(fatias.get(i), i, false);
            }
        }));
    }

    private void animarNoGrafico(Node node, int index, boolean barra) {
        node.setOpacity(0);
        node.setScaleX(barra ? 1 : 0.65);
        node.setScaleY(barra ? 0.08 : 0.65);
        node.setTranslateY(barra ? 18 : 6);

        PauseTransition delay = new PauseTransition(Duration.millis(index * 34L));
        delay.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(ANIMACAO_GRAFICO, node);
            fade.setFromValue(0);
            fade.setToValue(1);

            ScaleTransition scale = new ScaleTransition(ANIMACAO_GRAFICO, node);
            scale.setToX(1);
            scale.setToY(1);

            TranslateTransition slide = new TranslateTransition(ANIMACAO_GRAFICO, node);
            slide.setToY(0);

            new ParallelTransition(fade, scale, slide).play();
        });
        delay.play();
    }

    private void animarEntradaEscalonada(List<Node> nodes, long intervaloMs) {
        Platform.runLater(() -> {
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                node.setOpacity(0);
                node.setTranslateY(10);

                PauseTransition delay = new PauseTransition(Duration.millis(i * intervaloMs));
                delay.setOnFinished(e -> {
                    FadeTransition fade = new FadeTransition(ANIMACAO_GRAFICO, node);
                    fade.setFromValue(0);
                    fade.setToValue(1);

                    TranslateTransition slide = new TranslateTransition(ANIMACAO_GRAFICO, node);
                    slide.setFromY(10);
                    slide.setToY(0);

                    new ParallelTransition(fade, slide).play();
                });
                delay.play();
            }
        });
    }

    private void animarHover(Node node, double escala) {
        node.setOnMouseEntered(e -> animarEscala(node, escala));
        node.setOnMouseExited(e -> animarEscala(node, 1));
    }

    private void animarEscala(Node node, double escala) {
        ScaleTransition transition = new ScaleTransition(ANIMACAO_RAPIDA, node);
        transition.setToX(escala);
        transition.setToY(escala);
        transition.play();
    }

    private StackPane criarLucideIcone(String key, String color, double size) {
        String[] paths = ICONES_LUCIDE.getOrDefault(key, ICONES_LUCIDE.get("file-text"));
        Group group = new Group();
        for (String content : paths) {
            SVGPath path = new SVGPath();
            path.setContent(content);
            path.setFill(Color.TRANSPARENT);
            path.setStroke(Color.web(color));
            path.setStrokeWidth(2);
            path.setStrokeLineCap(StrokeLineCap.ROUND);
            path.setStrokeLineJoin(StrokeLineJoin.ROUND);
            group.getChildren().add(path);
        }

        double escala = size / 24.0;
        group.setScaleX(escala);
        group.setScaleY(escala);

        StackPane wrapper = new StackPane(group);
        wrapper.setMinSize(size, size);
        wrapper.setPrefSize(size, size);
        wrapper.setMaxSize(size, size);
        wrapper.setMouseTransparent(true);
        return wrapper;
    }

    private String iconKey(DashboardData.Metric metric) {
        if (metric.getIcon() != null && ICONES_LUCIDE.containsKey(metric.getIcon())) {
            return metric.getIcon();
        }

        String titulo = metric.getTitle().toLowerCase(new Locale("pt", "BR"));
        if (titulo.contains("valor total") || titulo.contains("valor do estoque")) return "banknote";
        if (titulo.contains("total de solicita")) return "shopping-cart";
        if (titulo.contains("aguardando")) return "clock-4";
        if (titulo.contains("aprovado")) return "circle-check";
        if (titulo.contains("rejeitado")) return "circle-x";
        if (titulo.contains("tempo") || titulo.contains("aprova\u00e7\u00e3o -> compra") || titulo.contains("recebimento")) return "timer";
        if (titulo.contains("finalizado")) return "package-check";
        if (titulo.contains("valor comprado")) return "trending-up";
        if (titulo.contains("sku") || titulo.contains("itens")) return "boxes";
        if (titulo.contains("m\u00ednimo") || titulo.contains("minimo")) return "triangle-alert";
        if (titulo.contains("entrada")) return "package";
        if (titulo.contains("sa\u00edda") || titulo.contains("saida")) return "package-minus";
        return switch (metric.getVariant()) {
            case "success" -> "circle-check";
            case "warning" -> "triangle-alert";
            case "danger" -> "circle-x";
            case "primary" -> "banknote";
            default -> "file-text";
        };
    }

    private static Map<String, String[]> criarIconesLucide() {
        Map<String, String[]> icons = new HashMap<>();
        icons.put("arrow-down-right", new String[]{
                "M7 7L17 17",
                "M17 7v10H7"
        });
        icons.put("arrow-up-right", new String[]{
                "M7 7h10v10",
                "M7 17L17 7"
        });
        icons.put("banknote", new String[]{
                "M4 6h16a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2Z",
                "M10 12a2 2 0 1 0 4 0a2 2 0 1 0-4 0",
                "M6 12h.01M18 12h.01"
        });
        icons.put("boxes", new String[]{
                "M2.97 12.92A2 2 0 0 0 2 14.63v3.24a2 2 0 0 0 .97 1.71l3 1.8a2 2 0 0 0 2.06 0L12 19v-5.5l-5-3-4.03 2.42Z",
                "M7 16.5 2.26 13.65",
                "M7 16.5l5-3",
                "M7 16.5v5.17",
                "M12 13.5V19l3.97 2.38a2 2 0 0 0 2.06 0l3-1.8a2 2 0 0 0 .97-1.71v-3.24a2 2 0 0 0-.97-1.71L17 10.5l-5 3Z",
                "M17 16.5l-5-3",
                "M17 16.5l4.74-2.85",
                "M17 16.5v5.17",
                "M7.97 4.42A2 2 0 0 0 7 6.13v4.37l5 3 5-3V6.13a2 2 0 0 0-.97-1.71l-3-1.8a2 2 0 0 0-2.06 0l-3 1.8Z",
                "M12 8 7.26 5.15",
                "M12 8l4.74-2.85",
                "M12 13.5V8"
        });
        icons.put("calendar", new String[]{
                "M8 2v4",
                "M16 2v4",
                "M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2Z",
                "M3 10h18"
        });
        icons.put("circle-check", new String[]{
                "M12 2a10 10 0 1 0 0 20a10 10 0 1 0 0 -20",
                "M9 12l2 2 4-4"
        });
        icons.put("circle-x", new String[]{
                "M12 2a10 10 0 1 0 0 20a10 10 0 1 0 0 -20",
                "M15 9l-6 6",
                "M9 9l6 6"
        });
        icons.put("clock-4", new String[]{
                "M12 2a10 10 0 1 0 0 20a10 10 0 1 0 0 -20",
                "M12 6v6l4 2"
        });
        icons.put("download", new String[]{
                "M12 15V3",
                "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4",
                "M7 10l5 5 5-5"
        });
        icons.put("file-text", new String[]{
                "M6 2h8l6 6v12a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2Z",
                "M14 2v6h6",
                "M16 13H8",
                "M16 17H8",
                "M10 9H8"
        });
        icons.put("package", new String[]{
                "M11 21.73a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73Z",
                "M12 22V12",
                "M3.29 7 12 12 20.71 7",
                "M7.5 4.27l9 5.15"
        });
        icons.put("package-check", new String[]{
                "M16 16l2 2 4-4",
                "M21 10V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l2-1.14",
                "M7.5 4.27l9 5.15",
                "M3.29 7 12 12 20.71 7",
                "M12 22V12"
        });
        icons.put("package-minus", new String[]{
                "M16 16h6",
                "M21 10V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l2-1.14",
                "M7.5 4.27l9 5.15",
                "M3.29 7 12 12 20.71 7",
                "M12 22V12"
        });
        icons.put("shopping-cart", new String[]{
                "M8 20a1 1 0 1 0 0 2a1 1 0 1 0 0-2",
                "M19 20a1 1 0 1 0 0 2a1 1 0 1 0 0-2",
                "M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12"
        });
        icons.put("timer", new String[]{
                "M10 2h4",
                "M12 14l3-3",
                "M12 6a8 8 0 1 0 0 16a8 8 0 1 0 0 -16"
        });
        icons.put("trending-up", new String[]{
                "M16 7h6v6",
                "M22 7l-8.5 8.5-5-5L2 17"
        });
        icons.put("triangle-alert", new String[]{
                "M21.73 18l-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3",
                "M12 9v4",
                "M12 17h.01"
        });
        icons.put("users", new String[]{
                "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
                "M16 3.128a4 4 0 0 1 0 7.744",
                "M22 21v-2a4 4 0 0 0-3-3.87",
                "M9 3a4 4 0 1 0 0 8a4 4 0 1 0 0 -8"
        });
        icons.put("x", new String[]{
                "M18 6 6 18",
                "M6 6l12 12"
        });
        return icons;
    }

    private String normalizarValorAtual(String titulo, String valor) {
        String lower = titulo.toLowerCase(new Locale("pt", "BR"));
        if (lower.contains("tempo") && valor.contains("d")) {
            return Math.round(extrairHoras(valor)) + "h";
        }
        return valor;
    }

    private String normalizarVariacao(DashboardData.Metric metric) {
        String detalhe = metric.getDetail();
        if (detalhe == null || detalhe.isBlank() || detalhe.startsWith("Meta:") || detalhe.startsWith("Tempo")) {
            return "0,0%";
        }
        return detalhe.replace(" vs per. ant.", "")
                .replace(" vs mês ant.", "")
                .replace(" vs mes ant.", "");
    }

    private String calcularValorAnterior(DashboardData.Metric metric, String atualTexto, String variacaoTexto) {
        String lower = metric.getTitle().toLowerCase(new Locale("pt", "BR"));
        if (lower.contains("tempo") && atualTexto.endsWith("h")) {
            double atual = extrairNumero(atualTexto);
            return NUMERO.format(Math.round(atual / 0.781)) + "h";
        }

        double atual = extrairNumero(atualTexto);
        double percentual = extrairNumero(variacaoTexto);
        if (atual == 0 || percentual == 0) {
            return atualTexto;
        }

        double anterior = atual / (1 + (percentual / 100.0));
        if (atualTexto.contains("R$")) {
            return MOEDA.format(anterior);
        }
        return NUMERO.format(Math.max(0, Math.round(anterior)));
    }

    private List<DashboardData.SeriesPoint> escolherComparacao(String titulo) {
        String lower = titulo.toLowerCase(new Locale("pt", "BR"));
        if (lower.contains("entrada") || lower.contains("saida") || lower.contains("saída")) {
            return dashboardData.getStockMovement();
        }
        if (lower.contains("valor comprado") || lower.contains("fornecedor")) {
            return seriesFromNameValues(dashboardData.getPurchasedValueEvolution());
        }
        if (lower.contains("estoque") || lower.contains("sku") || lower.contains("mínimo") || lower.contains("minimo")) {
            return seriesFromNameValues(dashboardData.getStockStatus());
        }
        return dashboardData.getMonthlyComparison();
    }

    private List<DashboardData.NameValue> escolherSerieAnual(String titulo) {
        String lower = titulo.toLowerCase(new Locale("pt", "BR"));
        if (lower.contains("estoque") || lower.contains("sku") || lower.contains("mínimo") || lower.contains("minimo")) {
            return dashboardData.getStockStatus();
        }
        if (lower.contains("fornecedor") || lower.contains("valor comprado")) {
            return dashboardData.getPurchasedValueEvolution();
        }
        if (lower.contains("entrada") || lower.contains("saida") || lower.contains("saída")) {
            List<DashboardData.NameValue> values = new ArrayList<>();
            for (DashboardData.SeriesPoint point : dashboardData.getStockMovement()) {
                values.add(new DashboardData.NameValue(point.getLabel(), point.getFirst() + point.getSecond()));
            }
            return values;
        }
        List<DashboardData.NameValue> values = new ArrayList<>();
        for (DashboardData.SeriesPoint point : dashboardData.getMonthlyComparison()) {
            values.add(new DashboardData.NameValue(point.getLabel(), point.getFirst() + point.getSecond() + point.getThird()));
        }
        return values;
    }

    private List<DashboardData.NameValue> escolherRegistros(String titulo) {
        String lower = titulo.toLowerCase(new Locale("pt", "BR"));
        if (lower.contains("usuario") || lower.contains("usuário") || lower.contains("solicita")) {
            return dashboardData.getTopRequestingUsers();
        }
        if (lower.contains("fornecedor") || lower.contains("valor comprado")) {
            return dashboardData.getPurchasedBySupplier();
        }
        if (lower.contains("estoque") || lower.contains("sku") || lower.contains("mínimo") || lower.contains("minimo")) {
            return dashboardData.getStockStatus();
        }
        if (lower.contains("entrada") || lower.contains("saida") || lower.contains("saída")) {
            return dashboardData.getTopOutgoingProducts();
        }
        return dashboardData.getRequestsBySector();
    }

    private List<DashboardData.SeriesPoint> seriesFromNameValues(List<DashboardData.NameValue> values) {
        List<DashboardData.SeriesPoint> series = new ArrayList<>();
        for (DashboardData.NameValue value : values) {
            series.add(new DashboardData.SeriesPoint(value.getName(), value.getValue(), value.getValue() * 0.82, 0));
        }
        return series;
    }

    private List<DashboardData.SeriesPoint> criarTendenciaSintetica(double atual) {
        String[] meses = {"Jan", "Fev", "Mar", "Abr", "Mai"};
        double[] fatores = {0.72, 0.88, 0.81, 0.94, 1.0};
        List<DashboardData.SeriesPoint> series = new ArrayList<>();
        for (int i = 0; i < meses.length; i++) {
            double valorAtual = atual * fatores[i];
            series.add(new DashboardData.SeriesPoint(meses[i], valorAtual, valorAtual * 0.82, 0));
        }
        return series;
    }

    private String subtituloDetalhe(String titulo) {
        String lower = titulo.toLowerCase(new Locale("pt", "BR"));
        if (lower.contains("tempo")) {
            return "Em horas - evolucao do tempo medio de resposta";
        }
        if (lower.contains("valor")) {
            return "Evolucao financeira do periodo selecionado";
        }
        if (lower.contains("estoque") || lower.contains("sku") || lower.contains("mínimo") || lower.contains("minimo")) {
            return "Situacao dos produtos e necessidade de reposicao";
        }
        return "Comparativo mensal e registros detalhados";
    }

    private String formatRegistro(double value) {
        if (value >= 1000) {
            return NUMERO.format(value);
        }
        return NUMERO.format(Math.round(value));
    }

    private double extrairHoras(String texto) {
        Matcher matcher = NUMERIC_PATTERN.matcher(texto);
        double total = 0;
        if (matcher.find()) {
            total += Double.parseDouble(matcher.group().replace(",", ".")) * 24;
        }
        if (matcher.find()) {
            total += Double.parseDouble(matcher.group().replace(",", "."));
        }
        return total;
    }

    private double extrairNumero(String texto) {
        if (texto == null) return 0;
        Matcher matcher = NUMERIC_PATTERN.matcher(texto.replace(".", ""));
        if (!matcher.find()) return 0;
        try {
            return Double.parseDouble(matcher.group().replace(",", "."));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static class DetalheDashboard {
        private final String titulo;
        private final String subtitulo;
        private final String atual;
        private final String anterior;
        private final String variacao;
        private final List<DashboardData.SeriesPoint> comparacao;
        private final List<DashboardData.NameValue> porAno;
        private final List<DashboardData.NameValue> registros;

        private DetalheDashboard(String titulo, String subtitulo, String atual, String anterior, String variacao,
                                 List<DashboardData.SeriesPoint> comparacao,
                                 List<DashboardData.NameValue> porAno,
                                 List<DashboardData.NameValue> registros) {
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.atual = atual;
            this.anterior = anterior;
            this.variacao = variacao;
            this.comparacao = comparacao;
            this.porAno = porAno;
            this.registros = registros;
        }
    }

    private static class LegendaItem {
        private final String rotulo;
        private final String cor;

        private LegendaItem(String rotulo, String cor) {
            this.rotulo = rotulo;
            this.cor = cor;
        }
    }

    private String iniciais(String nome) {
        if (nome == null || nome.isBlank()) return "--";
        String[] partes = nome.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        String segunda = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primeira + segunda).toUpperCase(new Locale("pt", "BR"));
    }
}