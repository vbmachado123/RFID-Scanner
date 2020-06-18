package model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Status  implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    private String status;

    /* POSSIVEIS STATUS
    * 1 - Encontrada
    * 2 - Nao_Atribuida
    * 3 - Nao_Encontrada
    * 4 - Negada_Inventario */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
