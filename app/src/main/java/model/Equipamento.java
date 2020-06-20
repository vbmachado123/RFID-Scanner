package model;

import androidx.annotation.NonNull;
import java.io.Serializable;

public class Equipamento implements Serializable {

    @NonNull
    private int id;
    private String numeroTag = "";
    private String descricao = "";
    private int localId;
    private int subLocalId;

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }

    public int getSubLocalId() {
        return subLocalId;
    }

    public void setSubLocalId(int subLocalId) {
        this.subLocalId = subLocalId;
    }

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

    public int getVezesLida() {
        return vezesLida;
    }

    public void setVezesLida(int vezesLida) {
        this.vezesLida = vezesLida;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

}
