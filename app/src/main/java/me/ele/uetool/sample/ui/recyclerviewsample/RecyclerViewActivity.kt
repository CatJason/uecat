package me.ele.uetool.sample.ui.recyclerviewsample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import me.ele.uetool.sample.R;

public class RecyclerViewActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        List<Item> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Item item;
            switch (i % 3) {
                case 0:
                    item = new Item1();
                    break;
                case 1:
                    item = new Item2();
                    break;
                default:
                    item = new Item3();
            }
            list.add(item);
        }
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SimpleAdapter(list));
    }
}

class SimpleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE1 = 1;
    private static final int TYPE2 = 2;
    private static final int TYPE3 = 3;
    private final List<Item> items;

    SimpleAdapter(List<Item> items) {
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_simple_textview, parent, false);
        switch (viewType) {
            case TYPE2:
                return new ViewHolder2(view);
            case TYPE3:
                return new ViewHolder3(view);
            default:
                return new ViewHolder1(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView view = (TextView) holder.itemView;
        view.setText(String.valueOf(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Item item = items.get(position);
        if (item instanceof Item1) {
            return TYPE1;
        } else if (item instanceof Item2) {
            return TYPE2;
        } else if (item instanceof Item3) {
            return TYPE3;
        }
        return 0;
    }
}

interface Item {}

class Item1 implements Item {}
class Item2 implements Item {}
class Item3 implements Item {}

class ViewHolder1 extends RecyclerView.ViewHolder {
    ViewHolder1(View itemView) {
        super(itemView);
    }
}

class ViewHolder2 extends RecyclerView.ViewHolder {
    ViewHolder2(View itemView) {
        super(itemView);
    }
}

class ViewHolder3 extends RecyclerView.ViewHolder {
    ViewHolder3(View itemView) {
        super(itemView);
    }
}
