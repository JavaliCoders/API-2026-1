package api.DAO;

import api.connection.ConexaoDB;
import api.model.DashboardData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class dashboardDAO {

    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(PT_BR);
    private static final NumberFormat NUMERO = NumberFormat.getIntegerInstance(PT_BR);
    private static final String COL_TIPO_MOV = "`tipo_movimenta\u00e7\u00e3o`";

    public static DashboardData carregar(String periodo) {
        try (Connection con = ConexaoDB.getConexao()) {
            if (con == null) {
                return DashboardData.exemploLovable();
            }

            DashboardData data = carregarDoBanco(con, periodo);
            if (data.getMetrics().isEmpty()) {
                return DashboardData.exemploLovable();
            }
            return data;
        } catch (Throwable e) {
            return DashboardData.exemploLovable();
        }
    }

    private static DashboardData carregarDoBanco(Connection con, String periodo) throws SQLException {
        LocalDate referencia = buscarDataReferencia(con).toLocalDate();
        PeriodWindow atual = PeriodWindow.current(periodo, referencia);
        PeriodWindow anterior = atual.previous();
        String periodoNome = atual.label();

        // ── Query 1: tb_pedido — 14 scalars → 1 query ──────────────────────────────
        double valorSolicitacoes, valorSolicitacoesAtual, valorSolicitacoesAnterior;
        int    totalSolicitacoes, totalSolicitacoesAtual, totalSolicitacoesAnterior;
        int    aguardando, aprovados, aprovadosAnterior, rejeitados, rejeitadosAnterior, finalizados;
        LocalDateTime maisAntiga;
        double horasAprovacao;

        try (PreparedStatement ps = con.prepareStatement("""
                SELECT
                  COALESCE(SUM(valor_total_estimado), 0)                                                                                  AS valor_total,
                  COALESCE(SUM(CASE WHEN data_abertura >= ? AND data_abertura < ? THEN valor_total_estimado ELSE 0 END), 0)                AS valor_atual,
                  COALESCE(SUM(CASE WHEN data_abertura >= ? AND data_abertura < ? THEN valor_total_estimado ELSE 0 END), 0)                AS valor_anterior,
                  COUNT(*)                                                                                                                  AS total,
                  SUM(CASE WHEN data_abertura >= ? AND data_abertura < ? THEN 1 ELSE 0 END)                                                AS total_atual,
                  SUM(CASE WHEN data_abertura >= ? AND data_abertura < ? THEN 1 ELSE 0 END)                                                AS total_anterior,
                  SUM(CASE WHEN status IN ('EM_APROVACAO','AGUARDANDO_APROVACAO') THEN 1 ELSE 0 END)                                        AS aguardando,
                  MIN(CASE WHEN status IN ('EM_APROVACAO','AGUARDANDO_APROVACAO') THEN data_abertura END)                                   AS mais_antiga,
                  SUM(CASE WHEN status IN ('APROVADO','APROVADO_PARCIALMENTE') AND data_aprovacao >= ? AND data_aprovacao < ? THEN 1 ELSE 0 END) AS aprovados,
                  SUM(CASE WHEN status IN ('APROVADO','APROVADO_PARCIALMENTE') AND data_aprovacao >= ? AND data_aprovacao < ? THEN 1 ELSE 0 END) AS aprovados_anterior,
                  SUM(CASE WHEN status = 'NEGADO' AND data_aprovacao >= ? AND data_aprovacao < ? THEN 1 ELSE 0 END)                         AS rejeitados,
                  SUM(CASE WHEN status = 'NEGADO' AND data_aprovacao >= ? AND data_aprovacao < ? THEN 1 ELSE 0 END)                         AS rejeitados_anterior,
                  SUM(CASE WHEN status = 'FINALIZADO' AND data_abertura >= ? AND data_abertura < ? THEN 1 ELSE 0 END)                       AS finalizados,
                  COALESCE(AVG(CASE WHEN data_aprovacao IS NOT NULL THEN TIMESTAMPDIFF(HOUR, data_abertura, data_aprovacao) END), 0)         AS horas_aprovacao
                FROM tb_pedido
                """)) {
            ps.setTimestamp( 1, atual.startTs());    ps.setTimestamp( 2, atual.endTs());
            ps.setTimestamp( 3, anterior.startTs()); ps.setTimestamp( 4, anterior.endTs());
            ps.setTimestamp( 5, atual.startTs());    ps.setTimestamp( 6, atual.endTs());
            ps.setTimestamp( 7, anterior.startTs()); ps.setTimestamp( 8, anterior.endTs());
            ps.setTimestamp( 9, atual.startTs());    ps.setTimestamp(10, atual.endTs());
            ps.setTimestamp(11, anterior.startTs()); ps.setTimestamp(12, anterior.endTs());
            ps.setTimestamp(13, atual.startTs());    ps.setTimestamp(14, atual.endTs());
            ps.setTimestamp(15, anterior.startTs()); ps.setTimestamp(16, anterior.endTs());
            ps.setTimestamp(17, atual.startTs());    ps.setTimestamp(18, atual.endTs());
            ResultSet rs = ps.executeQuery();
            rs.next();
            valorSolicitacoes         = rs.getDouble("valor_total");
            valorSolicitacoesAtual    = rs.getDouble("valor_atual");
            valorSolicitacoesAnterior = rs.getDouble("valor_anterior");
            totalSolicitacoes         = rs.getInt   ("total");
            totalSolicitacoesAtual    = rs.getInt   ("total_atual");
            totalSolicitacoesAnterior = rs.getInt   ("total_anterior");
            aguardando                = rs.getInt   ("aguardando");
            Timestamp tsMaisAntiga    = rs.getTimestamp("mais_antiga");
            maisAntiga                = tsMaisAntiga != null ? tsMaisAntiga.toLocalDateTime() : null;
            aprovados                 = rs.getInt   ("aprovados");
            aprovadosAnterior         = rs.getInt   ("aprovados_anterior");
            rejeitados                = rs.getInt   ("rejeitados");
            rejeitadosAnterior        = rs.getInt   ("rejeitados_anterior");
            finalizados               = rs.getInt   ("finalizados");
            horasAprovacao            = rs.getDouble("horas_aprovacao");
        }

        // ── Query 2: tb_compra — 2 scalars → 1 query ───────────────────────────────
        double valorComprado, valorCompradoAnterior;
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT
                  COALESCE(SUM(CASE WHEN status = 'REALIZADA' AND data >= ? AND data < ? THEN valor_total ELSE 0 END), 0) AS valor_atual,
                  COALESCE(SUM(CASE WHEN status = 'REALIZADA' AND data >= ? AND data < ? THEN valor_total ELSE 0 END), 0) AS valor_anterior
                FROM tb_compra
                """)) {
            ps.setTimestamp(1, atual.startTs());    ps.setTimestamp(2, atual.endTs());
            ps.setTimestamp(3, anterior.startTs()); ps.setTimestamp(4, anterior.endTs());
            ResultSet rs = ps.executeQuery();
            rs.next();
            valorComprado         = rs.getDouble("valor_atual");
            valorCompradoAnterior = rs.getDouble("valor_anterior");
        }

        // ── Query 3: horas aprovação→compra (JOIN necessário) ──────────────────────
        double horasAprovacaoCompra = scalarDouble(con,
                "SELECT COALESCE(AVG(TIMESTAMPDIFF(HOUR, p.data_aprovacao, c.data)), 0) FROM tb_pedido p JOIN tb_compra c ON c.id_pedido = p.id_pedido WHERE p.data_aprovacao IS NOT NULL");

        // ── Query 4: horas compra→recebimento (JOIN necessário) ────────────────────
        double horasCompraRecebimento = scalarDouble(con,
                "SELECT COALESCE(AVG(TIMESTAMPDIFF(HOUR, c.data, nf.data_registro)), 0) FROM tb_compra c JOIN tb_notasfiscal nf ON nf.id_compra = c.id_compra");

        // ── Query 5: tb_produto — 3 scalars → 1 query ──────────────────────────────
        int skus, abaixoMinimo;
        double valorEstoque;
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT
                  SUM(CASE WHEN status = 'ATIVO' THEN 1 ELSE 0 END)                                   AS skus,
                  COALESCE(SUM(CASE WHEN status = 'ATIVO' THEN saldo * valor_estimado ELSE 0 END), 0)  AS valor_estoque,
                  SUM(CASE WHEN status = 'ATIVO' AND saldo <= nivel_minimo THEN 1 ELSE 0 END)           AS abaixo_minimo
                FROM tb_produto
                """)) {
            ResultSet rs = ps.executeQuery();
            rs.next();
            skus         = rs.getInt   ("skus");
            valorEstoque = rs.getDouble("valor_estoque");
            abaixoMinimo = rs.getInt   ("abaixo_minimo");
        }

        // ── Query 6: tb_movimentacao — 2 scalars → 1 query ─────────────────────────
        int entradas, saidas;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT " +
                "COALESCE(SUM(CASE WHEN " + COL_TIPO_MOV + " LIKE 'ENTRADA%' AND data >= ? AND data < ? THEN quantidade ELSE 0 END), 0) AS entradas, " +
                "COALESCE(SUM(CASE WHEN " + COL_TIPO_MOV + " LIKE 'SA%'      AND data >= ? AND data < ? THEN quantidade ELSE 0 END), 0) AS saidas " +
                "FROM tb_movimentacao")) {
            ps.setTimestamp(1, atual.startTs()); ps.setTimestamp(2, atual.endTs());
            ps.setTimestamp(3, atual.startTs()); ps.setTimestamp(4, atual.endTs());
            ResultSet rs = ps.executeQuery();
            rs.next();
            entradas = rs.getInt("entradas");
            saidas   = rs.getInt("saidas");
        }

        DashboardData data = new DashboardData();
        data.setSourceLabel("Dados do banco - referencia: " + formatDate(referencia));
        data.addMetric("Valor total solicita\u00e7\u00f5es", MOEDA.format(valorSolicitacoes), deltaPercent(valorSolicitacoesAtual, valorSolicitacoesAnterior), "primary", "banknote", "DIRETOR", "FINANCEIRO");
        data.addMetric("Total de solicita\u00e7\u00f5es", NUMERO.format(totalSolicitacoes), deltaPercent(totalSolicitacoesAtual, totalSolicitacoesAnterior), "info", "shopping-cart", "DIRETOR", "FINANCEIRO", "ESTOQUE", "OPERACIONAL");
        data.addMetric("Aguardando aprova\u00e7\u00e3o", NUMERO.format(aguardando), pendenciaLabel(maisAntiga, referencia), "warning", "clock-4", "DIRETOR", "FINANCEIRO", "ESTOQUE", "OPERACIONAL");
        data.addMetric("Aprovados " + periodoNome, NUMERO.format(aprovados), deltaPercent(aprovados, aprovadosAnterior), "success", "circle-check", "DIRETOR", "FINANCEIRO");
        data.addMetric("Rejeitados " + periodoNome, NUMERO.format(rejeitados), deltaAbsolute(rejeitados, rejeitadosAnterior), "danger", "circle-x", "DIRETOR", "FINANCEIRO");
        data.addMetric("Tempo m\u00e9dio aprova\u00e7\u00e3o", formatHours(horasAprovacao), "Meta: at\u00e9 2 dias", "neutral", "timer", "DIRETOR", "FINANCEIRO");
        data.addMetric("Finalizados " + periodoNome, NUMERO.format(finalizados), "", "success", "package-check", "DIRETOR", "FINANCEIRO", "ESTOQUE", "OPERACIONAL");
        data.addMetric("Valor comprado " + periodoNome, MOEDA.format(valorComprado), deltaPercent(valorComprado, valorCompradoAnterior), "primary", "trending-up", "DIRETOR", "FINANCEIRO");
        data.addMetric("Aprova\u00e7\u00e3o -> Compra", formatHours(horasAprovacaoCompra), "Tempo m\u00e9dio", "info", "timer", "DIRETOR", "FINANCEIRO");
        data.addMetric("Compra -> Recebimento", formatHours(horasCompraRecebimento), "Tempo m\u00e9dio", "info", "timer", "DIRETOR", "ESTOQUE");
        data.addMetric("Total de itens (SKUs)", NUMERO.format(skus), "", "neutral", "boxes", "DIRETOR", "ESTOQUE");
        data.addMetric("Valor do estoque", MOEDA.format(valorEstoque), "", "primary", "banknote", "DIRETOR", "ESTOQUE");
        data.addMetric("Abaixo do m\u00ednimo", NUMERO.format(abaixoMinimo), "Reposi\u00e7\u00e3o necess\u00e1ria", "warning", "triangle-alert", "DIRETOR", "ESTOQUE");
        data.addMetric("Entradas " + periodoNome, NUMERO.format(entradas), "", "success", "package", "DIRETOR", "ESTOQUE");
        data.addMetric("Sa\u00eddas " + periodoNome, NUMERO.format(saidas), "", "neutral", "package-minus", "DIRETOR", "ESTOQUE", "OPERACIONAL");

        carregarComparacaoMensal(con, data, referencia);
        carregarValorComprado(con, data, referencia);
        carregarStatusEstoque(con, data);
        carregarAgrupamento(con, data.getRequestsBySector(),
                "SELECT s.setor nome, COUNT(*) total FROM tb_pedido p JOIN tb_setor s ON s.id_setor = p.id_setor WHERE p.data_abertura >= ? AND p.data_abertura < ? GROUP BY s.setor ORDER BY total DESC",
                "SELECT s.setor nome, COUNT(*) total FROM tb_pedido p JOIN tb_setor s ON s.id_setor = p.id_setor GROUP BY s.setor ORDER BY total DESC",
                atual);
        carregarAgrupamento(con, data.getRequestsByCostCenter(),
                "SELECT cc.centro_custo nome, COUNT(*) total FROM tb_pedido p JOIN tb_centrocusto cc ON cc.id_centrocusto = p.id_centrocusto WHERE p.data_abertura >= ? AND p.data_abertura < ? GROUP BY cc.centro_custo ORDER BY total DESC",
                "SELECT cc.centro_custo nome, COUNT(*) total FROM tb_pedido p JOIN tb_centrocusto cc ON cc.id_centrocusto = p.id_centrocusto GROUP BY cc.centro_custo ORDER BY total DESC",
                atual);
        carregarAgrupamento(con, data.getPurchasedBySupplier(),
                "SELECT f.nome nome, COALESCE(SUM(c.valor_total), 0) total FROM tb_compra c JOIN tb_fornecedor f ON f.id_fornecedor = c.id_fornecedor WHERE c.status = 'REALIZADA' AND c.data >= ? AND c.data < ? GROUP BY f.nome ORDER BY total DESC LIMIT 5",
                "SELECT f.nome nome, COALESCE(SUM(c.valor_total), 0) total FROM tb_compra c JOIN tb_fornecedor f ON f.id_fornecedor = c.id_fornecedor WHERE c.status = 'REALIZADA' GROUP BY f.nome ORDER BY total DESC LIMIT 5",
                atual);
        carregarMovimentacao(con, data, referencia);
        carregarTopProdutos(con, data, atual);
        carregarUsuarios(con, data, atual);

        preencherVaziosComExemplo(data);
        return data;
    }

    private static void carregarComparacaoMensal(Connection con, DashboardData data, LocalDate referencia) throws SQLException {
        LocalDate inicio = referencia.withDayOfMonth(1).minusMonths(4);
        LocalDate fim = referencia.withDayOfMonth(1).plusMonths(1);
        Map<String, DashboardData.SeriesPoint> pontos = new HashMap<>();

        String sql = """
                SELECT DATE_FORMAT(data_abertura, '%Y-%m') periodo,
                       SUM(CASE WHEN status IN ('APROVADO', 'APROVADO_PARCIALMENTE') THEN 1 ELSE 0 END) aprovados,
                       SUM(CASE WHEN status = 'NEGADO' THEN 1 ELSE 0 END) rejeitados,
                       SUM(CASE WHEN status = 'FINALIZADO' THEN 1 ELSE 0 END) finalizados
                FROM tb_pedido
                WHERE data_abertura >= ? AND data_abertura < ?
                GROUP BY DATE_FORMAT(data_abertura, '%Y-%m')
                ORDER BY periodo
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(fim.atStartOfDay()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                pontos.put(rs.getString("periodo"), new DashboardData.SeriesPoint(
                        mesLabel(rs.getString("periodo")),
                        rs.getDouble("aprovados"),
                        rs.getDouble("rejeitados"),
                        rs.getDouble("finalizados")));
            }
        }

        for (int i = 0; i < 5; i++) {
            LocalDate mes = inicio.plusMonths(i);
            String chave = String.format("%04d-%02d", mes.getYear(), mes.getMonthValue());
            data.getMonthlyComparison().add(pontos.getOrDefault(chave,
                    new DashboardData.SeriesPoint(mesLabel(chave), 0, 0, 0)));
        }
    }

    private static void carregarValorComprado(Connection con, DashboardData data, LocalDate referencia) throws SQLException {
        LocalDate inicio = referencia.withDayOfMonth(1).minusMonths(4);
        LocalDate fim = referencia.withDayOfMonth(1).plusMonths(1);
        Map<String, Double> valores = new HashMap<>();

        String sql = """
                SELECT DATE_FORMAT(data, '%Y-%m') periodo, COALESCE(SUM(valor_total), 0) total
                FROM tb_compra
                WHERE status = 'REALIZADA' AND data >= ? AND data < ?
                GROUP BY DATE_FORMAT(data, '%Y-%m')
                ORDER BY periodo
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(fim.atStartOfDay()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                valores.put(rs.getString("periodo"), rs.getDouble("total"));
            }
        }

        for (int i = 0; i < 5; i++) {
            LocalDate mes = inicio.plusMonths(i);
            String chave = String.format("%04d-%02d", mes.getYear(), mes.getMonthValue());
            data.getPurchasedValueEvolution().add(new DashboardData.NameValue(mesLabel(chave), valores.getOrDefault(chave, 0.0)));
        }
    }

    private static void carregarStatusEstoque(Connection con, DashboardData data) throws SQLException {
        int abaixo = scalarInt(con, "SELECT COUNT(*) FROM tb_produto WHERE status = 'ATIVO' AND saldo <= nivel_minimo");
        int disponivel = scalarInt(con, "SELECT COUNT(*) FROM tb_produto WHERE status = 'ATIVO' AND saldo > nivel_minimo");
        int inativo = scalarInt(con, "SELECT COUNT(*) FROM tb_produto WHERE status = 'INATIVO'");
        data.getStockStatus().add(new DashboardData.NameValue("Dispon\u00edvel", disponivel));
        data.getStockStatus().add(new DashboardData.NameValue("Abaixo do m\u00ednimo", abaixo));
        data.getStockStatus().add(new DashboardData.NameValue("Inativo", inativo));
    }

    private static void carregarAgrupamento(Connection con, java.util.List<DashboardData.NameValue> destino,
                                            String sqlPeriodo, String sqlFallback, PeriodWindow atual) throws SQLException {
        queryNameValues(con, destino, sqlPeriodo, atual.startTs(), atual.endTs());
        if (destino.isEmpty() || destino.stream().mapToDouble(DashboardData.NameValue::getValue).sum() == 0) {
            destino.clear();
            queryNameValues(con, destino, sqlFallback);
        }
    }

    private static void carregarMovimentacao(Connection con, DashboardData data, LocalDate referencia) throws SQLException {
        LocalDate inicio = referencia.withDayOfMonth(1).minusMonths(4);
        LocalDate fim = referencia.withDayOfMonth(1).plusMonths(1);
        Map<String, DashboardData.SeriesPoint> pontos = new HashMap<>();

        String sql = "SELECT DATE_FORMAT(data, '%Y-%m') periodo, " +
                "SUM(CASE WHEN " + COL_TIPO_MOV + " LIKE 'ENTRADA%' THEN quantidade ELSE 0 END) entradas, " +
                "SUM(CASE WHEN " + COL_TIPO_MOV + " LIKE 'SA%' THEN quantidade ELSE 0 END) saidas " +
                "FROM tb_movimentacao WHERE data >= ? AND data < ? " +
                "GROUP BY DATE_FORMAT(data, '%Y-%m') ORDER BY periodo";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(fim.atStartOfDay()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                pontos.put(rs.getString("periodo"), new DashboardData.SeriesPoint(
                        mesLabel(rs.getString("periodo")),
                        rs.getDouble("entradas"),
                        rs.getDouble("saidas"),
                        0));
            }
        }

        for (int i = 0; i < 5; i++) {
            LocalDate mes = inicio.plusMonths(i);
            String chave = String.format("%04d-%02d", mes.getYear(), mes.getMonthValue());
            data.getStockMovement().add(pontos.getOrDefault(chave,
                    new DashboardData.SeriesPoint(mesLabel(chave), 0, 0, 0)));
        }
    }

    private static void carregarTopProdutos(Connection con, DashboardData data, PeriodWindow atual) throws SQLException {
        String topSql = "SELECT pr.produto nome, SUM(mv.quantidade) total " +
                "FROM tb_movimentacao mv JOIN tb_produto pr ON pr.id_produto = mv.id_produto " +
                "WHERE mv." + COL_TIPO_MOV + " LIKE 'SA%' AND mv.data >= ? AND mv.data < ? " +
                "GROUP BY pr.produto ORDER BY total DESC LIMIT 5";
        queryNameValues(con, data.getTopOutgoingProducts(), topSql, atual.startTs(), atual.endTs());

        if (data.getTopOutgoingProducts().isEmpty()) {
            String fallbackTop = "SELECT pr.produto nome, SUM(mv.quantidade) total " +
                    "FROM tb_movimentacao mv JOIN tb_produto pr ON pr.id_produto = mv.id_produto " +
                    "WHERE mv." + COL_TIPO_MOV + " LIKE 'SA%' GROUP BY pr.produto ORDER BY total DESC LIMIT 5";
            queryNameValues(con, data.getTopOutgoingProducts(), fallbackTop);
        }

        String lowSql = "SELECT pr.produto nome, COALESCE(SUM(CASE WHEN mv." + COL_TIPO_MOV + " LIKE 'SA%' THEN mv.quantidade ELSE 0 END), 0) total " +
                "FROM tb_produto pr LEFT JOIN tb_movimentacao mv ON mv.id_produto = pr.id_produto " +
                "WHERE pr.status = 'ATIVO' GROUP BY pr.produto ORDER BY total ASC, pr.produto LIMIT 3";
        queryNameValues(con, data.getLowOutgoingProducts(), lowSql);
    }

    private static void carregarUsuarios(Connection con, DashboardData data, PeriodWindow atual) throws SQLException {
        String sql = """
                SELECT u.nome nome, COUNT(*) total
                FROM tb_pedido p
                JOIN tb_usuario u ON u.id_usuario = p.id_solicitante
                WHERE p.data_abertura >= ? AND p.data_abertura < ?
                GROUP BY u.nome
                ORDER BY total DESC
                LIMIT 5
                """;
        queryNameValues(con, data.getTopRequestingUsers(), sql, atual.startTs(), atual.endTs());
        if (data.getTopRequestingUsers().isEmpty()) {
            queryNameValues(con, data.getTopRequestingUsers(), """
                    SELECT u.nome nome, COUNT(*) total
                    FROM tb_pedido p
                    JOIN tb_usuario u ON u.id_usuario = p.id_solicitante
                    GROUP BY u.nome
                    ORDER BY total DESC
                    LIMIT 5
                    """);
        }
    }

    private static void preencherVaziosComExemplo(DashboardData data) {
        DashboardData exemplo = DashboardData.exemploLovable();
        if (allZero(data.getMonthlyComparison())) replace(data.getMonthlyComparison(), exemplo.getMonthlyComparison());
        if (data.getPurchasedValueEvolution().isEmpty() || allZeroNameValue(data.getPurchasedValueEvolution())) replace(data.getPurchasedValueEvolution(), exemplo.getPurchasedValueEvolution());
        if (data.getStockStatus().isEmpty() || allZeroNameValue(data.getStockStatus())) replace(data.getStockStatus(), exemplo.getStockStatus());
        if (data.getRequestsBySector().isEmpty()) replace(data.getRequestsBySector(), exemplo.getRequestsBySector());
        if (data.getRequestsByCostCenter().isEmpty()) replace(data.getRequestsByCostCenter(), exemplo.getRequestsByCostCenter());
        if (data.getPurchasedBySupplier().isEmpty()) replace(data.getPurchasedBySupplier(), exemplo.getPurchasedBySupplier());
        if (allZero(data.getStockMovement())) replace(data.getStockMovement(), exemplo.getStockMovement());
        if (data.getTopOutgoingProducts().isEmpty()) replace(data.getTopOutgoingProducts(), exemplo.getTopOutgoingProducts());
        if (data.getLowOutgoingProducts().isEmpty()) replace(data.getLowOutgoingProducts(), exemplo.getLowOutgoingProducts());
        if (data.getTopRequestingUsers().isEmpty()) replace(data.getTopRequestingUsers(), exemplo.getTopRequestingUsers());
    }

    private static <T> void replace(java.util.List<T> target, java.util.List<T> source) {
        target.clear();
        target.addAll(source);
    }

    private static boolean allZero(java.util.List<DashboardData.SeriesPoint> values) {
        return values.isEmpty() || values.stream().allMatch(p -> p.getFirst() == 0 && p.getSecond() == 0 && p.getThird() == 0);
    }

    private static boolean allZeroNameValue(java.util.List<DashboardData.NameValue> values) {
        return values.stream().allMatch(v -> v.getValue() == 0);
    }

    private static LocalDateTime buscarDataReferencia(Connection con) throws SQLException {
        LocalDateTime data = scalarDateTime(con, """
                SELECT MAX(data_ref) FROM (
                    SELECT MAX(data_abertura) data_ref FROM tb_pedido
                    UNION ALL SELECT MAX(data) data_ref FROM tb_compra
                    UNION ALL SELECT MAX(data) data_ref FROM tb_movimentacao
                ) datas
                """);
        return data != null ? data : LocalDateTime.now();
    }

    private static void queryNameValues(Connection con, java.util.List<DashboardData.NameValue> destino,
                                        String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            setParams(ps, params);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                destino.add(new DashboardData.NameValue(rs.getString("nome"), rs.getDouble("total")));
            }
        }
    }

    private static int scalarInt(Connection con, String sql, Object... params) throws SQLException {
        return (int) Math.round(scalarDouble(con, sql, params));
    }

    private static double scalarDouble(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            setParams(ps, params);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    private static LocalDateTime scalarDateTime(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            setParams(ps, params);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Timestamp ts = rs.getTimestamp(1);
            return ts == null ? null : ts.toLocalDateTime();
        }
    }

    private static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if (param instanceof Timestamp timestamp) {
                ps.setTimestamp(i + 1, timestamp);
            } else {
                ps.setObject(i + 1, param);
            }
        }
    }

    private static String deltaPercent(double atual, double anterior) {
        if (anterior <= 0) {
            return atual > 0 ? "Sem base anterior" : "";
        }
        double delta = ((atual - anterior) / anterior) * 100.0;
        return String.format(PT_BR, "%+.1f%% vs per. ant.", delta);
    }

    private static String deltaAbsolute(int atual, int anterior) {
        int delta = atual - anterior;
        if (delta == 0) return "Sem varia\u00e7\u00e3o";
        return String.format("%+d pedidos", delta);
    }

    private static String pendenciaLabel(LocalDateTime maisAntiga, LocalDate referencia) {
        if (maisAntiga == null) {
            return "Nenhum pedido pendente";
        }
        long dias = ChronoUnit.DAYS.between(maisAntiga.toLocalDate(), referencia);
        return "Mais antiga h\u00e1 " + Math.max(dias, 0) + " dias";
    }

    private static String formatHours(double totalHours) {
        long horas = Math.round(totalHours);
        long dias = horas / 24;
        long resto = horas % 24;
        if (dias <= 0) return resto + "h";
        return dias + "d " + resto + "h";
    }

    private static String formatDate(LocalDate date) {
        return String.format("%02d/%02d/%04d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    private static String mesLabel(String yearMonth) {
        int mes = Integer.parseInt(yearMonth.substring(5, 7));
        String[] labels = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        return labels[mes - 1];
    }

    private static class PeriodWindow {
        private final LocalDate start;
        private final LocalDate end;
        private final String periodo;

        private PeriodWindow(LocalDate start, LocalDate end, String periodo) {
            this.start = start;
            this.end = end;
            this.periodo = periodo;
        }

        static PeriodWindow current(String periodo, LocalDate reference) {
            String normalized = periodo == null ? "MES" : periodo;
            if ("ANO".equals(normalized)) {
                LocalDate start = LocalDate.of(reference.getYear(), 1, 1);
                return new PeriodWindow(start, start.plusYears(1), normalized);
            }
            if ("TRIMESTRE".equals(normalized)) {
                int firstMonth = ((reference.getMonthValue() - 1) / 3) * 3 + 1;
                LocalDate start = LocalDate.of(reference.getYear(), firstMonth, 1);
                return new PeriodWindow(start, start.plusMonths(3), normalized);
            }
            LocalDate start = reference.withDayOfMonth(1);
            return new PeriodWindow(start, start.plusMonths(1), "MES");
        }

        PeriodWindow previous() {
            if ("ANO".equals(periodo)) {
                return new PeriodWindow(start.minusYears(1), start, periodo);
            }
            if ("TRIMESTRE".equals(periodo)) {
                return new PeriodWindow(start.minusMonths(3), start, periodo);
            }
            return new PeriodWindow(start.minusMonths(1), start, periodo);
        }

        Timestamp startTs() {
            return Timestamp.valueOf(start.atStartOfDay());
        }

        Timestamp endTs() {
            return Timestamp.valueOf(end.atStartOfDay());
        }

        String label() {
            if ("ANO".equals(periodo)) return "no ano";
            if ("TRIMESTRE".equals(periodo)) return "no trimestre";
            return "no m\u00eas";
        }
    }
}