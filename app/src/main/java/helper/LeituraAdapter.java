package helper;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rfidscanner.R;

import java.util.ArrayList;
import java.util.List;

import model.Leitura;

public class LeituraAdapter extends BaseAdapter {
    private List<Leitura> leitura;
    private Activity activity;

    public LeituraAdapter(Activity activity, ArrayList<Leitura> leitura) {
        this.activity = activity;
        this.leitura = leitura;
    }

    @Override
    public int getCount() {
        return leitura.size();
    }

    @Override
    public Object getItem(int i) {
        return leitura.get(i);
    }

    @Override
    public long getItemId(int i) {
        return leitura.get(i).getId();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View v = activity.getLayoutInflater().inflate(R.layout.lista_leituras, parent, false);
        TextView tag = v.findViewById(R.id.txtTag);
        TextView data = v.findViewById(R.id.txtData);

        Leitura l = leitura.get(i);

        tag.setText(l.getNumeroTag());
        data.setText(l.getDataHora());

        return v;
    }
}
