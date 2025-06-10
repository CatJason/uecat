package me.ele.uetool;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import me.ele.uetool.attrdialog.AttrsDialogItemViewBinder;
import me.ele.uetool.attrdialog.AttrsDialogMultiTypePool;
import me.ele.uetool.base.DimenUtilKt;
import me.ele.uetool.base.Element;
import me.ele.uetool.base.IAttrs;
import me.ele.uetool.base.ItemArrayList;
import me.ele.uetool.base.item.AddMinusEditItem;
import me.ele.uetool.base.item.BriefDescItem;
import me.ele.uetool.base.item.EditTextItem;
import me.ele.uetool.base.item.Item;
import me.ele.uetool.base.item.SwitchItem;
import me.ele.uetool.base.item.TextItem;
import static me.ele.uetool.UtilsKt.clipText;
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

        public static abstract class BaseViewHolder<T extends Item> extends RecyclerView.ViewHolder {

            protected T item;

            public BaseViewHolder(View itemView) {
                super(itemView);
            }

            public void bindView(T t) {
                item = t;
            }
        }

        public static class TextViewHolder extends BaseViewHolder<TextItem> {

            private TextView vName;
            private TextView vDetail;

            public TextViewHolder(View itemView) {
                super(itemView);
                vName = itemView.findViewById(R.id.name);
                vDetail = itemView.findViewById(R.id.detail);
            }

            public static TextViewHolder newInstance(ViewGroup parent) {
                return new TextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.uet_cell_text, parent, false));
            }

            @Override
            public void bindView(final TextItem textItem) {
                super.bindView(textItem);
                vName.setText(textItem.name);
                final String detail = textItem.detail;
                if (textItem.onClickListener != null) {
                    vDetail.setText(Html.fromHtml("<u>" + detail + "</u>"));
                    vDetail.setOnClickListener(textItem.onClickListener);
                } else {
                    vDetail.setText(detail);
                    if (textItem.isEnableCopy()) {
                        vDetail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                clipText(detail);
                            }
                        });
                    }
                }

            }
        }

        public static class EditTextViewHolder<T extends EditTextItem>
                extends BaseViewHolder<T> {

            protected TextView vName;
            protected EditText vDetail;
            @Nullable
            private View vColor;

            protected TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        if (item.type == EditTextItem.Type.TYPE_TEXT) {
                            TextView textView = ((TextView) (item.element.view));
                            if (!TextUtils.equals(textView.getText().toString(), s.toString())) {
                                textView.setText(s.toString());
                            }
                        } else if (item.type == EditTextItem.Type.TYPE_TEXT_SIZE) {
                            TextView textView = ((TextView) (item.element.view));
                            float textSize = Float.valueOf(s.toString());
                            if (textView.getTextSize() != textSize) {
                                textView.setTextSize(textSize);
                            }
                        } else if (item.type == EditTextItem.Type.TYPE_TEXT_COLOR) {
                            TextView textView = ((TextView) (item.element.view));
                            int color = Color.parseColor(vDetail.getText().toString());
                            if (color != textView.getCurrentTextColor()) {
                                vColor.setBackgroundColor(color);
                                textView.setTextColor(color);
                            }
                        } else if (item.type == EditTextItem.Type.TYPE_WIDTH) {
                            View view = item.element.view;
                            int width = DimenUtilKt.dip2px(Integer.valueOf(s.toString()));
                            if (Math.abs(width - view.getWidth()) >= DimenUtilKt.dip2px(1)) {
                                view.getLayoutParams().width = width;
                                view.requestLayout();
                            }
                        } else if (item.type == EditTextItem.Type.TYPE_HEIGHT) {
                            View view = item.element.view;
                            int height = DimenUtilKt.dip2px(Integer.valueOf(s.toString()));
                            if (Math.abs(height - view.getHeight()) >= DimenUtilKt.dip2px(1)) {
                                view.getLayoutParams().height = height;
                                view.requestLayout();
                            }
                        } else if (item.type == EditTextItem.Type.TYPE_PADDING_LEFT) {
                            View view = item.element.view;
                            int paddingLeft = DimenUtilKt.dip2px(Integer.valueOf(s.toString()));
                            if (Math.abs(paddingLeft - view.getPaddingLeft()) >= DimenUtilKt.dip2px(1)) {
                                view.setPadding(paddingLeft, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                            }
                        } else if (item.type == EditTextItem.Type.TYPE_PADDING_RIGHT) {
                            View view = item.element.view;
                            int paddingRight = DimenUtilKt.dip2px(Integer.valueOf(s.toString()));
                            if (Math.abs(paddingRight - view.getPaddingRight()) >= DimenUtilKt.dip2px(1)) {
                                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), paddingRight, view.getPaddingBottom());
                            }
                        } else if (item.type == EditTextItem.Type.TYPE_PADDING_TOP) {
                            View view = item.element.view;
                            int paddingTop = DimenUtilKt.dip2px(Integer.valueOf(s.toString()));
                            if (Math.abs(paddingTop - view.getPaddingTop()) >= DimenUtilKt.dip2px(1)) {
                                view.setPadding(view.getPaddingLeft(), paddingTop, view.getPaddingRight(), view.getPaddingBottom());
                            }
                        } else if (item.type == EditTextItem.Type.TYPE_PADDING_BOTTOM) {
                            View view = item.element.view;
                            int paddingBottom = DimenUtilKt.dip2px(Integer.valueOf(s.toString()));
                            if (Math.abs(paddingBottom - view.getPaddingBottom()) >= DimenUtilKt.dip2px(1)) {
                                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), paddingBottom);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            };

            public EditTextViewHolder(View itemView) {
                super(itemView);
                vName = itemView.findViewById(R.id.name);
                vDetail = itemView.findViewById(R.id.detail);
                vColor = itemView.findViewById(R.id.color);
                vDetail.addTextChangedListener(textWatcher);
            }

            public static EditTextViewHolder newInstance(ViewGroup parent) {
                return new EditTextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.uet_cell_edit_text, parent, false));
            }

            @Override
            public void bindView(final T editTextItem) {
                super.bindView(editTextItem);
                vName.setText(editTextItem.name);
                vDetail.setText(editTextItem.detail);
                if (vColor != null) {
                    try {
                        vColor.setBackgroundColor(Color.parseColor(editTextItem.detail));
                        vColor.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        vColor.setVisibility(View.GONE);
                    }
                }
            }
        }

        public static class AddMinusEditViewHolder extends EditTextViewHolder<AddMinusEditItem> {

            private View vAdd;
            private View vMinus;

            public AddMinusEditViewHolder(View itemView) {
                super(itemView);
                vAdd = itemView.findViewById(R.id.add);
                vMinus = itemView.findViewById(R.id.minus);
                vAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            int textSize = Integer.valueOf(vDetail.getText().toString());
                            vDetail.setText(String.valueOf(++textSize));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                vMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            int textSize = Integer.valueOf(vDetail.getText().toString());
                            if (textSize > 0) {
                                vDetail.setText(String.valueOf(--textSize));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            public static AddMinusEditViewHolder newInstance(ViewGroup parent) {
                return new AddMinusEditViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.uet_cell_add_minus_edit, parent, false));
            }

            @Override
            public void bindView(AddMinusEditItem editTextItem) {
                super.bindView(editTextItem);
            }
        }

    }
}