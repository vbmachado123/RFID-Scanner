package adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rfidscanner.R;

import java.util.ArrayList;
import java.util.List;

import model.Local;
import model.SubLocal;

public class SubLocalAdapter extends BaseAdapter {

    private List<SubLocal> sublocal;
    private Activity activity;

    public SubLocalAdapter(Activity activity, ArrayList<SubLocal> local) {
        this.activity = activity;
        this.sublocal = local;
    }

    @Override
    public int getCount() {
        return sublocal.size();
    }

    @Override
    public Object getItem(int i) {
        return sublocal.get(i);
    }

    @Override
    public long getItemId(int i) {
        return sublocal.get(i).getId();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View v = activity.getLayoutInflater().inflate(R.layout.row_sublocal, parent, false);
        TextView sublocal = v.findViewById(R.id.tvSubLocal);

        SubLocal l = this.sublocal.get(i);

        sublocal.setText(l.getDescricao());
        return v;
    }
}
