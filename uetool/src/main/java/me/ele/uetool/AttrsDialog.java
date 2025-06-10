package me.ele.uetool;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.List;
import me.ele.uetool.attrdialog.AttrsDialogItemViewBinder;
import me.ele.uetool.attrdialog.AttrsDialogMultiTypePool;
import me.ele.uetool.base.DimenUtilKt;
import me.ele.uetool.base.Element;
import me.ele.uetool.base.IAttrs;
import me.ele.uetool.base.ItemArrayList;
import me.ele.uetool.base.item.BriefDescItem;
import me.ele.uetool.base.item.Item;
import me.ele.uetool.base.item.SwitchItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AttrsDialog extends Dialog {
    private RecyclerView vList;
    private Adapter adapter = new Adapter();
    private RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

    public AttrsDialog(Context context) {
        super(context, R.style.uet_Theme_Holo_Dialog_background_Translucent);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uet_dialog_attrs);
        vList = findViewById(R.id.list);
        vList.setAdapter(adapter);
        vList.setLayoutManager(layoutManager);
    }

    public void show(Element element) {
        show();
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        lp.x = element.rect.left;
        lp.y = element.rect.bottom;
        lp.width = DimenUtilKt.getScreenWidth() - DimenUtilKt.dip2px(30);
        lp.height = DimenUtilKt.getScreenHeight() / 2;
        dialogWindow.setAttributes(lp);
        adapter.notifyDataSetChanged(element);
        layoutManager.scrollToPosition(0);
        for (int i = 0; i < adapter.getItemCount(); i++) {
            Item item = adapter.getItem(i);
            if (item instanceof SwitchItem && ((SwitchItem) item).getType() == SwitchItem.Type.TYPE_SHOW_VALID_VIEWS) {
                ((SwitchItem) item).isChecked = true;
                if (adapter.callback != null) {
                    adapter.callback.showValidViews(i, true);
                }
                break;
            }
        }

        View myCat = findViewById(R.id.cat);
        myCat.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean isDragged = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录触摸开始时的位置
                        initialX = dialogWindow.getAttributes().x;
                        initialY = dialogWindow.getAttributes().y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragged = false;
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!isDragged) {
                            // 这里处理点击事件
                            v.performClick();
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // 计算移动的距离
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);
                        if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {  // 10像素的阈值来判断是否拖拽
                            isDragged = true;
                            // 更新对话框的位置
                            WindowManager.LayoutParams params = dialogWindow.getAttributes();
                            params.x = initialX + deltaX;
                            params.y = initialY + deltaY;
                            dialogWindow.setAttributes(params);
                        }

                        return true;
                }
                return false;
            }
        });

        View sign = findViewById(R.id.sign);
        myCat.setOnClickListener(v -> {
            if(vList.getVisibility() == View.GONE) {
                vList.setVisibility(View.VISIBLE);
                sign.setAlpha(1f);
                v.setAlpha(1f);
            } else {
                vList.setVisibility(View.GONE);
                sign.setAlpha(0.2f);
                v.setAlpha(0.2f);
            }
        });
    }

    public void notifyValidViewItemInserted(int positionStart, List<Element> validElements, Element targetElement) {
        List<Item> validItems = new ArrayList<>();
        boolean flag = false;
        for (int i = validElements.size() - 1; i >= 0; i--) {
            Element element = validElements.get(i);
            if (element.view.getClass().getName().equals("com.android.internal.policy.DecorView")) {
                flag = true;
            }
            if (flag) {
                validItems.add(new BriefDescItem(element, targetElement.equals(element)));
            }
        }
        adapter.notifyValidViewItemInserted(positionStart, validItems);
    }

    public final void notifyItemRangeRemoved(int positionStart) {
        adapter.notifyValidViewItemRemoved(positionStart);
    }

    public void setAttrDialogCallback(AttrDialogCallback callback) {
        adapter.setAttrDialogCallback(callback);
    }

    public static class Adapter extends RecyclerView.Adapter {

        private List<Item> items = new ItemArrayList<>();
        private List<Item> validItems = new ArrayList<>();
        private AttrDialogCallback callback;

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
}