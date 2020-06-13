package helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rfidscanner.R;

import java.util.List;

public class EquipamentoAdapter extends RecyclerView.Adapter<EquipamentoAdapter.EquipamentoHolder> {

    @NonNull
    @Override
    public EquipamentoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull EquipamentoHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class EquipamentoHolder extends RecyclerView.ViewHolder {
        private final TextView valorTag, dataLeitura;

        private EquipamentoHolder(View itemView) {
            super(itemView);
            valorTag = itemView.findViewById(R.id.txtTag);
            dataLeitura = itemView.findViewById(R.id.txtData);
        }
    }

}
