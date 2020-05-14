package model;

import java.io.Serializable;

public class Tag implements Serializable {
    private int id;
    private String nomeTag = "";
    private String dataLeitura = "";
    private int vezesLida = 0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomeTag() {
        return nomeTag;
    }

    public void setNomeTag(String nomeTag) {
        this.nomeTag = nomeTag;
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
