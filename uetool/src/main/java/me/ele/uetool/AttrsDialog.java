package me.ele.uetool;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StrikethroughSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.style.ForegroundColorSpan;
import java.util.ArrayList;
import java.util.List;

import me.ele.uetool.attrdialog.AttrsDialogItemViewBinder;
import me.ele.uetool.attrdialog.AttrsDialogMultiTypePool;
import me.ele.uetool.base.Element;
import me.ele.uetool.base.IAttrs;
import me.ele.uetool.base.ItemArrayList;
import me.ele.uetool.base.item.AddMinusEditItem;
import me.ele.uetool.base.item.BitmapItem;
import me.ele.uetool.base.item.BriefDescItem;
import me.ele.uetool.base.item.EditTextItem;
import me.ele.uetool.base.item.Item;
import me.ele.uetool.base.item.SwitchItem;
import me.ele.uetool.base.item.TextItem;
import me.ele.uetool.base.item.TitleItem;
import me.ele.uetool.cat.StringUtilKt;
import me.ele.uetool.cat.ViewXRayKt;
import static me.ele.uetool.base.DimenUtil.dip2px;
import static me.ele.uetool.base.DimenUtil.getScreenHeight;
import static me.ele.uetool.base.DimenUtil.getScreenWidth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
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
        lp.width = getScreenWidth() - dip2px(30);
        lp.height = getScreenHeight() / 2;
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

    public interface AttrDialogCallback {
        void enableMove();

        void showValidViews(int position, boolean isChecked);

        void selectView(Element element);
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

        public static class TitleViewHolder extends BaseViewHolder<TitleItem> {

            private TextView vTitle;

            public TitleViewHolder(View itemView) {
                super(itemView);
                vTitle = itemView.findViewById(R.id.title);
            }

            public static TitleViewHolder newInstance(ViewGroup parent) {
                return new TitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.uet_cell_title, parent, false));
            }

            @Override
            public void bindView(TitleItem titleItem) {
                super.bindView(titleItem);
                vTitle.setText(titleItem.name);
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
                                Util.clipText(detail);
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
                            int width = dip2px(Integer.valueOf(s.toString()));
                            if (Math.abs(width - view.getWidth()) >= dip2px(1)) {
                                view.getLayoutParams().width = width;
                                view.requestLayout();
                            }
                        } else if (item.type == EditTextItem.Type.TYPE_HEIGHT) {
                            View view = item.element.view;
                            int height = dip2px(Integer.valueOf(s.toString()));
                            if (Math.abs(height - view.getHeight()) >= dip2px(1)) {
                                view.getLayoutParams().height = height;
                                view.requestLayout();
                            }
                        } else if (item.type == EditTextItem.Type.TYPE_PADDING_LEFT) {
                            View view = item.element.view;
                            int paddingLeft = dip2px(Integer.valueOf(s.toString()));
                            if (Math.abs(paddingLeft - view.getPaddingLeft()) >= dip2px(1)) {
                                view.setPadding(paddingLeft, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                            }
                        } else if (item.type == EditTextItem.Type.TYPE_PADDING_RIGHT) {
                            View view = item.element.view;
                            int paddingRight = dip2px(Integer.valueOf(s.toString()));
                            if (Math.abs(paddingRight - view.getPaddingRight()) >= dip2px(1)) {
                                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), paddingRight, view.getPaddingBottom());
                            }
                        } else if (item.type == EditTextItem.Type.TYPE_PADDING_TOP) {
                            View view = item.element.view;
                            int paddingTop = dip2px(Integer.valueOf(s.toString()));
                            if (Math.abs(paddingTop - view.getPaddingTop()) >= dip2px(1)) {
                                view.setPadding(view.getPaddingLeft(), paddingTop, view.getPaddingRight(), view.getPaddingBottom());
                            }
                        } else if (item.type == EditTextItem.Type.TYPE_PADDING_BOTTOM) {
                            View view = item.element.view;
                            int paddingBottom = dip2px(Integer.valueOf(s.toString()));
                            if (Math.abs(paddingBottom - view.getPaddingBottom()) >= dip2px(1)) {
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

        public static class SwitchViewHolder extends BaseViewHolder<SwitchItem> {

            private TextView vName;
            private SwitchCompat vSwitch;

            public SwitchViewHolder(View itemView, final AttrDialogCallback callback) {
                super(itemView);

                vName = itemView.findViewById(R.id.name);
                vSwitch = itemView.findViewById(R.id.switch_view);
                vSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        try {
                            if (item.getType() == SwitchItem.Type.TYPE_MOVE) {
                                if (callback != null && isChecked) {
                                    callback.enableMove();
                                }
                                return;
                            } else if (item.getType() == SwitchItem.Type.TYPE_SHOW_VALID_VIEWS) {
                                if(item.isChecked != isChecked) {
                                    item.isChecked = isChecked;
                                    if (callback != null) {
                                        callback.showValidViews(getAdapterPosition(), isChecked);
                                    }
                                }
                                return;
                            }
                            if (item.element.view instanceof TextView) {
                                TextView textView = ((TextView) (item.element.view));
                                if (item.getType() == SwitchItem.Type.TYPE_IS_BOLD) {
                                    Typeface tf = Typeface.create(textView.getTypeface(), isChecked ? Typeface.BOLD : Typeface.NORMAL);
                                    textView.setTypeface(tf);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            public static SwitchViewHolder newInstance(ViewGroup parent, AttrDialogCallback callback) {
                return new SwitchViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.uet_cell_switch, parent, false), callback);
            }

            @Override
            public void bindView(SwitchItem switchItem) {
                super.bindView(switchItem);
                vName.setText(switchItem.name);
                vSwitch.setChecked(switchItem.isChecked);
            }
        }

        public static class BitmapInfoViewHolder extends BaseViewHolder<BitmapItem> {

            private final int imageHeight = dip2px(58);

            private TextView vName;
            private ImageView vImage;
            private TextView vInfo;

            public BitmapInfoViewHolder(View itemView) {
                super(itemView);

                vName = itemView.findViewById(R.id.name);
                vImage = itemView.findViewById(R.id.image);
                vInfo = itemView.findViewById(R.id.info);
            }

            public static BitmapInfoViewHolder newInstance(ViewGroup parent) {
                return new BitmapInfoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.uet_cell_bitmap_info, parent, false));
            }

            @Override
            public void bindView(BitmapItem bitmapItem) {
                super.bindView(bitmapItem);

                vName.setText(bitmapItem.name);
                Bitmap bitmap = bitmapItem.bitmap;

                int height = Math.min(bitmap.getHeight(), imageHeight);
                int width = (int) ((float) height / bitmap.getHeight() * bitmap.getWidth());

                ViewGroup.LayoutParams layoutParams = vImage.getLayoutParams();
                layoutParams.width = width;
                layoutParams.height = height;
                vImage.setImageBitmap(bitmap);
                vInfo.setText(bitmap.getWidth() + "px*" + bitmap.getHeight() + "px");
            }
        }

        public static class BriefDescViewHolder extends BaseViewHolder<BriefDescItem> {

            private TextView vDesc;

            public BriefDescViewHolder(View itemView, final AttrDialogCallback callback) {
                super(itemView);
                vDesc = (TextView) itemView;
                vDesc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callback != null) {
                            callback.selectView(item.element);
                        }
                    }
                });
            }

            public static BriefDescViewHolder newInstance(ViewGroup parent, AttrDialogCallback callback) {
                return new BriefDescViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(
                                        R.layout.uet_cell_brief_view_desc,
                                        parent,
                                        false
                                ),
                        callback
                );
            }

            private static boolean isSystemClass(View view) {
                if (view == null) {
                    return false;
                }

                String className = view.getClass().getName();
                // 检查类名是否以 "android." 开头
                return className.startsWith("android.") || className.startsWith("com.android");
            }

            @Override
            public void bindView(BriefDescItem briefDescItem) {
                super.bindView(briefDescItem);
                View view = briefDescItem.element.view;
                String resName = Util.getResourceName(view.getId());

                // 创建带样式的文本
                SpannableString spannableText = buildDisplayTextWithStyles(view, resName);

                // 设置带样式的文本到 TextView
                vDesc.setText(spannableText);
                vDesc.setSelected(briefDescItem.isSelected);
                vDesc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
            }

            private SpannableString buildDisplayTextWithStyles(View view, String resName) {
                StringBuilder sb = new StringBuilder();

                // 添加空格缩进
                int numberOfSpaces = ViewXRayKt.getViewLayer(view) * 2;
                sb.append("  ".repeat(Math.max(0, numberOfSpaces)));

                // 添加类别前缀和类名
                sb.append(numberOfSpaces > 0 ? "└ " : "")
                        .append(isSystemClass(view) ? "[系统] " : "[自定义] ")
                        .append(StringUtilKt.extractAfterLastDot(view.getClass().getName()));

                // 添加资源名称（如果存在）
                if (!TextUtils.isEmpty(resName)) {
                    sb.append(" (@+id/").append(resName).append(")");
                } else {
                    sb.append(" (未设置 id)");
                }

                return applySpannableStyles(sb.toString(), resName);
            }

            private SpannableString applySpannableStyles(String text, String resName) {
                SpannableString spannableString = new SpannableString(text);

                // 如果资源名称存在，则应用绿色前景色
                if (!TextUtils.isEmpty(resName)) {
                    int start = text.indexOf("(@+id/");
                    int end = start + resName.length() + 6; // 加上 "(@+id/" 和 ")" 的长度
                    spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    // 否则，对 "(未设置 id)" 应用黄色删除线
                    int start = text.indexOf("(未设置 id)");
                    int end = start + "(未设置 id)".length();
                    spannableString.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new ForegroundColorSpan(Color.YELLOW), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                return spannableString;
            }
        }
    }
}