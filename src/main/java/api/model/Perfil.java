package api.model;

public class Perfil {
    private int id;
    private String nome;

    public Perfil(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() {
    	return id; 
    	}
    
    public String getNome() {
        return nome;
    	}
    
    @Override
    public String toString() {
        return nome;
    }
}