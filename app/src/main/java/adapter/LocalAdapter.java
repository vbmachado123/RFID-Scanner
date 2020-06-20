package adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rfidscanner.R;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import model.Leitura;
import model.Local;
import model.SubLocal;

public class LocalAdapter extends BaseAdapter {

    private List<Local> local;
    private Activity activity;

    public LocalAdapter(Activity activity, ArrayList<Local> local) {
        this.activity = activity;
        this.local = local;
    }

    @Override
    public int getCount() {
        return local.size();
    }

    @Override
    public Object getItem(int i) {
        return local.get(i);
    }

    @Override
    public long getItemId(int i) {
        return local.get(i).getId();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View v = activity.getLayoutInflater().inflate(R.layout.row_local, parent, false);
        TextView local = v.findViewById(R.id.tvLocal);

        Local l = this.local.get(i);

        local.setText(l.getDescricao());
        return v;
    }
}

