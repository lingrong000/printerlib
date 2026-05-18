package com.cumtenn.printerlib;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cumtenn.printerlib.databinding.ItemPrinterBinding;

import java.util.List;

public class PrinterAdapter extends RecyclerView.Adapter<PrinterAdapter.PrinterViewHolder> {

    private List<Printer> printerList;
    private OnPrinterClickListener onPrinterClickListener;

    public PrinterAdapter(List<Printer> printerList, OnPrinterClickListener onPrinterClickListener) {
        this.printerList = printerList;
        this.onPrinterClickListener = onPrinterClickListener;
    }

    @NonNull
    @Override
    public PrinterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPrinterBinding binding = ItemPrinterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PrinterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PrinterViewHolder holder, int position) {
        Printer printer = printerList.get(position);
        holder.binding.tvPrinterName.setText(printer.getName());
        holder.binding.tvPrinterInfo.setText(printer.getIp());
        
        holder.itemView.setOnClickListener(v -> {
            if (onPrinterClickListener != null) {
                onPrinterClickListener.onPrinterClick(printer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return printerList.size();
    }

    public void setPrinterList(List<Printer> printerList) {
        this.printerList = printerList;
        notifyDataSetChanged();
    }

    public static class PrinterViewHolder extends RecyclerView.ViewHolder {
        ItemPrinterBinding binding;

        public PrinterViewHolder(ItemPrinterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnPrinterClickListener {
        void onPrinterClick(Printer printer);
    }
}