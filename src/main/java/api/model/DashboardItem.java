package api.model;

public class DashboardItem {

    private String status;
    private int quantidade;

    public DashboardItem(String status, int quantidade) {
        this.status = status;
        this.quantidade = quantidade;
    }

    public String getStatus() {
        return status;
    }

    public int getQuantidade() {
        return quantidade;
    }
}