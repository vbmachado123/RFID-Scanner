package adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rfidscanner.R;

import java.util.ArrayList;
import java.util.List;

import model.Equipamento;
import model.Leitura;

public class EquipamentoAdapter extends BaseAdapter {

    private List<Equipamento> equipamentos;
    private Activity activity;

    public EquipamentoAdapter(Activity activity, ArrayList<Equipamento> equipamentos) {
        this.activity = activity;
        this.equipamentos = equipamentos;
    }

    @Override
    public int getCount() {
        return equipamentos.size();
    }

    @Override
    public Object getItem(int i) {
        return equipamentos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return equipamentos.get(i).getId();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View v = activity.getLayoutInflater().inflate(R.layout.lista_equipamento, parent, false);
        TextView tag = v.findViewById(R.id.txtTag);
        TextView descricao = v.findViewById(R.id.txtDescricao);

        Equipamento e = equipamentos.get(i);

        tag.setText(e.getNumeroTag());
        descricao.setText(e.getDescricao());

        return v;
    }
}
