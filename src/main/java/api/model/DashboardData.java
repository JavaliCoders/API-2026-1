package api.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardData {

    private final List<Metric> metrics = new ArrayList<>();
    private final List<SeriesPoint> monthlyComparison = new ArrayList<>();
    private final List<NameValue> purchasedValueEvolution = new ArrayList<>();
    private final List<NameValue> stockStatus = new ArrayList<>();
    private final List<NameValue> requestsBySector = new ArrayList<>();
    private final List<NameValue> requestsByCostCenter = new ArrayList<>();
    private final List<NameValue> purchasedBySupplier = new ArrayList<>();
    private final List<SeriesPoint> stockMovement = new ArrayList<>();
    private final List<NameValue> topOutgoingProducts = new ArrayList<>();
    private final List<NameValue> lowOutgoingProducts = new ArrayList<>();
    private final List<NameValue> topRequestingUsers = new ArrayList<>();

    private String sourceLabel = "Dados de exemplo";

    public List<Metric> getMetrics() {
        return metrics;
    }

    public List<Metric> getMetricsForProfile(String profile) {
        if ("DIRETOR".equals(profile)) {
            return metrics;
        }

        List<Metric> filtered = new ArrayList<>();
        for (Metric metric : metrics) {
            if (metric.isVisibleFor(profile)) {
                filtered.add(metric);
            }
        }
        return filtered.isEmpty() ? metrics : filtered;
    }

    public List<SeriesPoint> getMonthlyComparison() {
        return monthlyComparison;
    }

    public List<NameValue> getPurchasedValueEvolution() {
        return purchasedValueEvolution;
    }

    public List<NameValue> getStockStatus() {
        return stockStatus;
    }

    public List<NameValue> getRequestsBySector() {
        return requestsBySector;
    }

    public List<NameValue> getRequestsByCostCenter() {
        return requestsByCostCenter;
    }

    public List<NameValue> getPurchasedBySupplier() {
        return purchasedBySupplier;
    }

    public List<SeriesPoint> getStockMovement() {
        return stockMovement;
    }

    public List<NameValue> getTopOutgoingProducts() {
        return topOutgoingProducts;
    }

    public List<NameValue> getLowOutgoingProducts() {
        return lowOutgoingProducts;
    }

    public List<NameValue> getTopRequestingUsers() {
        return topRequestingUsers;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public void setSourceLabel(String sourceLabel) {
        this.sourceLabel = sourceLabel;
    }

    public void addMetric(String title, String value, String detail, String variant, String icon, String... profiles) {
        metrics.add(new Metric(title, value, detail, variant, icon, profiles));
    }

    public static DashboardData exemploLovable() {
        DashboardData data = new DashboardData();
        data.setSourceLabel("Dados de exemplo do painel Lovable");

        data.addMetric("Valor total solicita\u00e7\u00f5es", "R$ 284.350,00", "+12,4% vs m\u00eas ant.", "primary", "banknote", "DIRETOR", "FINANCEIRO");
        data.addMetric("Total de solicita\u00e7\u00f5es", "187", "+8,1%", "info", "shopping-cart", "DIRETOR", "FINANCEIRO", "ESTOQUE", "OPERACIONAL");
        data.addMetric("Aguardando aprova\u00e7\u00e3o", "14", "Mais antiga h\u00e1 3 dias", "warning", "clock-4", "DIRETOR", "FINANCEIRO", "ESTOQUE", "OPERACIONAL");
        data.addMetric("Aprovados no m\u00eas", "54", "+14,9%", "success", "circle-check", "DIRETOR", "FINANCEIRO");
        data.addMetric("Rejeitados no m\u00eas", "8", "+2 pedidos", "danger", "circle-x", "DIRETOR", "FINANCEIRO");
        data.addMetric("Tempo m\u00e9dio aprova\u00e7\u00e3o", "1d 8h", "Meta: at\u00e9 2 dias", "neutral", "timer", "DIRETOR", "FINANCEIRO");
        data.addMetric("Finalizados no m\u00eas", "49", "", "success", "package-check", "DIRETOR", "FINANCEIRO", "ESTOQUE", "OPERACIONAL");
        data.addMetric("Valor comprado no m\u00eas", "R$ 73.500,00", "+19,8%", "primary", "trending-up", "DIRETOR", "FINANCEIRO");
        data.addMetric("Aprova\u00e7\u00e3o -> Compra", "2d 4h", "Tempo m\u00e9dio", "info", "timer", "DIRETOR", "FINANCEIRO");
        data.addMetric("Compra -> Recebimento", "5d 11h", "Tempo m\u00e9dio", "info", "timer", "DIRETOR", "ESTOQUE");
        data.addMetric("Total de itens (SKUs)", "197", "", "neutral", "boxes", "DIRETOR", "ESTOQUE");
        data.addMetric("Valor do estoque", "R$ 412.900,00", "", "primary", "banknote", "DIRETOR", "ESTOQUE");
        data.addMetric("Abaixo do m\u00ednimo", "17", "Reposi\u00e7\u00e3o necess\u00e1ria", "warning", "triangle-alert", "DIRETOR", "ESTOQUE");
        data.addMetric("Entradas no m\u00eas", "1.284", "", "success", "package", "DIRETOR", "ESTOQUE");
        data.addMetric("Sa\u00eddas no m\u00eas", "1.106", "", "neutral", "package-minus", "DIRETOR", "ESTOQUE", "OPERACIONAL");

        data.monthlyComparison.add(new SeriesPoint("Jan", 38, 5, 34));
        data.monthlyComparison.add(new SeriesPoint("Fev", 42, 7, 36));
        data.monthlyComparison.add(new SeriesPoint("Mar", 47, 6, 41));
        data.monthlyComparison.add(new SeriesPoint("Abr", 51, 8, 45));
        data.monthlyComparison.add(new SeriesPoint("Mai", 54, 8, 49));

        data.purchasedValueEvolution.add(new NameValue("Jan", 48200));
        data.purchasedValueEvolution.add(new NameValue("Fev", 54100));
        data.purchasedValueEvolution.add(new NameValue("Mar", 61250));
        data.purchasedValueEvolution.add(new NameValue("Abr", 68400));
        data.purchasedValueEvolution.add(new NameValue("Mai", 73500));

        data.stockStatus.add(new NameValue("Dispon\u00edvel", 146));
        data.stockStatus.add(new NameValue("Abaixo do m\u00ednimo", 17));
        data.stockStatus.add(new NameValue("Inativo", 34));

        data.requestsBySector.add(new NameValue("Financeiro", 42));
        data.requestsBySector.add(new NameValue("Compras", 64));
        data.requestsBySector.add(new NameValue("Operacional", 81));

        data.requestsByCostCenter.add(new NameValue("Administrativo", 32));
        data.requestsByCostCenter.add(new NameValue("Manuten\u00e7\u00e3o", 24));
        data.requestsByCostCenter.add(new NameValue("Inform\u00e1tica", 19));
        data.requestsByCostCenter.add(new NameValue("Limpeza", 14));
        data.requestsByCostCenter.add(new NameValue("Infraestrutura", 11));

        data.purchasedBySupplier.add(new NameValue("Papelaria Central", 24500));
        data.purchasedBySupplier.add(new NameValue("TechSuprimentos", 18900));
        data.purchasedBySupplier.add(new NameValue("CleanMax", 14100));
        data.purchasedBySupplier.add(new NameValue("AquaPura", 8700));
        data.purchasedBySupplier.add(new NameValue("Infotech Redes", 7300));

        data.stockMovement.add(new SeriesPoint("Jan", 980, 820, 0));
        data.stockMovement.add(new SeriesPoint("Fev", 1120, 910, 0));
        data.stockMovement.add(new SeriesPoint("Mar", 1045, 980, 0));
        data.stockMovement.add(new SeriesPoint("Abr", 1190, 1035, 0));
        data.stockMovement.add(new SeriesPoint("Mai", 1284, 1106, 0));

        data.topOutgoingProducts.add(new NameValue("Papel A4 75g", 480));
        data.topOutgoingProducts.add(new NameValue("Caneta esferogr\u00e1fica azul", 312));
        data.topOutgoingProducts.add(new NameValue("Toner HP 26A", 96));
        data.topOutgoingProducts.add(new NameValue("Caf\u00e9 em p\u00f3 500g", 88));
        data.topOutgoingProducts.add(new NameValue("Cabo HDMI 2m", 64));

        data.lowOutgoingProducts.add(new NameValue("Bobina t\u00e9rmica 80mm", 3));
        data.lowOutgoingProducts.add(new NameValue("Pilha 9V", 4));
        data.lowOutgoingProducts.add(new NameValue("R\u00e9gua met\u00e1lica 30cm", 5));

        data.topRequestingUsers.add(new NameValue("Daniel Souza", 24));
        data.topRequestingUsers.add(new NameValue("Carlos Eduardo", 19));
        data.topRequestingUsers.add(new NameValue("Mariana Lima", 14));
        data.topRequestingUsers.add(new NameValue("Patr\u00edcia Alves", 11));

        return data;
    }

    public static class Metric {
        private final String title;
        private final String value;
        private final String detail;
        private final String variant;
        private final String icon;
        private final List<String> profiles;

        public Metric(String title, String value, String detail, String variant, String icon, String... profiles) {
            this.title = title;
            this.value = value;
            this.detail = detail == null ? "" : detail;
            this.variant = variant;
            this.icon = icon;
            this.profiles = Arrays.asList(profiles);
        }

        public String getTitle() {
            return title;
        }

        public String getValue() {
            return value;
        }

        public String getDetail() {
            return detail;
        }

        public String getVariant() {
            return variant;
        }

        public String getIcon() {
            return icon;
        }

        public boolean isVisibleFor(String profile) {
            return profiles.isEmpty() || profiles.contains(profile);
        }
    }

    public static class SeriesPoint {
        private final String label;
        private final double first;
        private final double second;
        private final double third;

        public SeriesPoint(String label, double first, double second, double third) {
            this.label = label;
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public String getLabel() {
            return label;
        }

        public double getFirst() {
            return first;
        }

        public double getSecond() {
            return second;
        }

        public double getThird() {
            return third;
        }
    }

    public static class NameValue {
        private final String name;
        private final double value;

        public NameValue(String name, double value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public double getValue() {
            return value;
        }
    }
}
