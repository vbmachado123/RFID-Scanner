package helper;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rfidscanner.R;

import java.util.List;

import model.Leitura;

public class LeituraAdapter extends BaseAdapter {

    private List<Leitura> leitura;
    private Activity activity;
    private String numTag, data;

    public LeituraAdapter(Activity activity, List<Leitura> leitura, String numTag, String data) {

        this.activity = activity;
        this.leitura = leitura;
        this.numTag = numTag;
        this.data = data;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = activity.getLayoutInflater().inflate(R.layout.lista_leituras, parent, false);
        TextView tag = v.findViewById(R.id.txtTag);
        TextView data = v.findViewById(R.id.txtData);

        //Formulario f = formulario.get(i);

        tag.setText(numTag);
        data.setText(this.data);

        return v;
    }
}
