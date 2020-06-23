package model;

import java.io.Serializable;
import java.util.ArrayList;

public class Lista implements Serializable {

    private ArrayList<Leitura> leituras;
    private ArrayList<Equipamento> equipamentos;

    public ArrayList<Leitura> getLeituras() {
        return leituras;
    }

    public void setLeituras(ArrayList<Leitura> leituras) {
        this.leituras = leituras;
    }

    public ArrayList<Equipamento> getEquipamentos() {
        return equipamentos;
    }

    public void setEquipamentos(ArrayList<Equipamento> equipamentos) {
        this.equipamentos = equipamentos;
    }
}
