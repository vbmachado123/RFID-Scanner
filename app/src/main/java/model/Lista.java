package model;

import java.io.Serializable;
import java.util.ArrayList;

public class Lista implements Serializable {

    private ArrayList<Leitura> leituras;

    public ArrayList<Leitura> getLeituras() {
        return leituras;
    }

    public void setLeituras(ArrayList<Leitura> leituras) {
        this.leituras = leituras;
    }
}
