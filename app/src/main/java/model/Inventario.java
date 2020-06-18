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
    private int idSubLocal; /* Opcional neste primeiro momento */
    private String dataHora;
    private String latitude, longitude;

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

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
