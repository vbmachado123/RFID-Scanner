package model;

import java.io.Serializable;

public class Leitura implements Serializable {

    private int id;

    private String numeroTag;

    private String dataHora;

    private int vezesLida;

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

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }

    public int getVezesLida() {
        return vezesLida;
    }

    public void setVezesLida(int vezesLida) {
        this.vezesLida = vezesLida;
    }

    public String toString(){
        return getNumeroTag();
    }
}
