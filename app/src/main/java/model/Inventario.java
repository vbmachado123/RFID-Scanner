package model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Inventario implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    private int idLocal;
    private int idSubLocal;
    private String dataHora;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(int idLocal) {
        this.idLocal = idLocal;
    }

    public int getIdSubLocal() {
        return idSubLocal;
    }

    public void setIdSubLocal(int idSubLocal) {
        this.idSubLocal = idSubLocal;
    }

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }
}
