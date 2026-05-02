package api.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Anexo {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IntegerProperty idAnexo;
    private final StringProperty tipo;
    private final StringProperty nomeArquivo;
    private final StringProperty caminhoArquivo;
    private final ObjectProperty<LocalDateTime> dataUpload;


    public Anexo(int idAnexo, String tipo, String nomeArquivo,
                 String caminhoArquivo, LocalDateTime dataUpload) {
        this.idAnexo        = new SimpleIntegerProperty(idAnexo);
        this.tipo           = new SimpleStringProperty(tipo);
        this.nomeArquivo    = new SimpleStringProperty(nomeArquivo);
        this.caminhoArquivo = new SimpleStringProperty(caminhoArquivo);
        this.dataUpload     = new SimpleObjectProperty<>(dataUpload);
    }

    // Constructor para novo anexo
    public Anexo(String tipo, String nomeArquivo, String caminhoArquivo) {
        this(0, tipo, nomeArquivo, caminhoArquivo, LocalDateTime.now());
    }

    // Getter
    public int getIdAnexo() { return idAnexo.get(); }
    public String getTipo() { return tipo.get(); }
    public String getNomeArquivo() { return nomeArquivo.get(); }
    public String getCaminhoArquivo() { return caminhoArquivo.get(); }
    public LocalDateTime getDataUpload() { return dataUpload.get(); }

    // Getters auxiliares para TableView
    public String getDataUploadFormatada() { return dataUpload.get() != null ? dataUpload.get().format(FORMATTER) : ""; }

    // Properties
    public IntegerProperty idAnexoProperty() { return idAnexo; }
    public StringProperty tipoProperty() { return tipo; }
    public StringProperty nomeArquivoProperty() { return nomeArquivo; }
    public StringProperty caminhoArquivoProperty() { return caminhoArquivo; }
    public ObjectProperty<LocalDateTime> dataUploadProperty() { return dataUpload; }
}