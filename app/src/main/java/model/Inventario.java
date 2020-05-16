package model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "inventario")
public class Inventario implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    private String nomeLocal;
    private String nomeEquipamentos;
    private String numeroTag;
    private String dataEHoraLeitura;
    private String numLeituraRealizada;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomeLocal() {
        return nomeLocal;
    }

    public void setNomeLocal(String nomeLocal) {
        this.nomeLocal = nomeLocal;
    }

    public String getNomeEquipamentos() {
        return nomeEquipamentos;
    }

    public void setNomeEquipamentos(String nomeEquipamentos) {
        this.nomeEquipamentos = nomeEquipamentos;
    }

    public String getNumeroTag() {
        return numeroTag;
    }

    public void setNumeroTag(String numeroTag) {
        this.numeroTag = numeroTag;
    }

    public String getDataEHoraLeitura() {
        return dataEHoraLeitura;
    }

    public void setDataEHoraLeitura(String dataEHoraLeitura) {
        this.dataEHoraLeitura = dataEHoraLeitura;
    }

    public String getNumLeituraRealizada() {
        return numLeituraRealizada;
    }

    public void setNumLeituraRealizada(String numLeituraRealizada) {
        this.numLeituraRealizada = numLeituraRealizada;
    }
}
