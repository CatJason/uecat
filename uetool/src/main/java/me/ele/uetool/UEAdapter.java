package me.ele.uetool;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.ele.uetool.attrdialog.AttrsDialogItemViewBinder;
import me.ele.uetool.attrdialog.AttrsDialogMultiTypePool;
import me.ele.uetool.base.Element;
import me.ele.uetool.base.IAttrs;
import me.ele.uetool.base.ItemArrayList;
import me.ele.uetool.base.item.Item;

public class UEAdapter extends RecyclerView.Adapter {

    private List<Item> items = new ItemArrayList<>();
    private List<Item> validItems = new ArrayList<>();
    public AttrDialogCallback callback;

    public void setAttrDialogCallback(AttrDialogCallback callback) {
        this.callback = callback;
    }

    public AttrDialogCallback getAttrDialogCallback() {
        return this.callback;
    }

    public void notifyDataSetChanged(Element element) {
        items.clear();
        for (String attrsProvider : UETool.INSTANCE.getAttrsProvider()) {
            try {
                IAttrs attrs = (IAttrs) Class.forName(attrsProvider).newInstance();
                items.addAll(attrs.getAttrs(element));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        notifyDataSetChanged();
    }

    public void notifyValidViewItemInserted(int positionStart, List<Item> validItems) {
        this.validItems.addAll(validItems);
        items.addAll(positionStart, validItems);
        notifyItemRangeInserted(positionStart, validItems.size());
    }

    public void notifyValidViewItemRemoved(int positionStart) {
        items.removeAll(validItems);
        notifyItemRangeRemoved(positionStart, validItems.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AttrsDialogMultiTypePool pool = UETool.INSTANCE.getAttrsDialogMultiTypePool();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return pool.getItemViewBinder(viewType).onCreateViewHolder(inflater, parent, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AttrsDialogMultiTypePool pool = UETool.INSTANCE.getAttrsDialogMultiTypePool();
        ((AttrsDialogItemViewBinder) pool.getItemViewBinder(holder.getItemViewType())).onBindViewHolder(holder, getItem(position));
    }

    @Override
    public int getItemViewType(int position) {
        Item item = getItem(position);
        AttrsDialogMultiTypePool pool = UETool.INSTANCE.getAttrsDialogMultiTypePool();
        return pool.getItemType(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected <T extends Item> T getItem(int adapterPosition) {
        if (adapterPosition < 0 || adapterPosition >= items.size()) {
            return null;
        }
        return (T) items.get(adapterPosition);
    }
}
