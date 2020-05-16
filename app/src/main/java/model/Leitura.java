package model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "leitura")
public class Leitura implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    private String numeroTag = "";
    private String dataLeitura = "";
    private int vezesLida = 0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroTag() {
        return numeroTag;
    }

    public void setNumeroTag(String numeroTag) {
        this.numeroTag = numeroTag;
    }

    public String getDataLeitura() {
        return dataLeitura;
    }

    public void setDataLeitura(String dataLeitura) {
        this.dataLeitura = dataLeitura;
    }

    public int getVezesLida() {
        return vezesLida;
    }

    public void setVezesLida(int vezesLida) {
        this.vezesLida = vezesLida;
    }
}
