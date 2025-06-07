package me.ele.uetool;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import me.ele.uetool.base.DimenUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.view.Gravity.BOTTOM;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static me.ele.uetool.TransparentActivity.Type.*;
import static me.ele.uetool.base.DimenUtil.px2dip;

import androidx.annotation.IntDef;
import androidx.appcompat.app.AppCompatActivity;

public class TransparentActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "extra_type";

    private ViewGroup vContainer;
    private int type;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            finish();
            return;
        }
        Util.setStatusBarColor(getWindow(), Color.TRANSPARENT);
        Util.enableFullscreen(getWindow());
        setContentView(R.layout.uet_activity_transparent);

        vContainer = findViewById(R.id.container);

        final BoardTextView board = new BoardTextView(this);
        board.setOnClickListener(v -> {
            UETool.INSTANCE.getTargetActivity().finish();
            finish();
        });
        board.setGravity(Gravity.CENTER);

        // 初始触摸位置
        final float[] initialTouch = new float[2];

        board.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 记录触摸位置
                    initialTouch[0] = event.getRawX();
                    initialTouch[1] = event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    // 计算偏移量
                    float dx = event.getRawX() - initialTouch[0];
                    float dy = event.getRawY() - initialTouch[1];

                    // 设置新的位置
                    v.setX(v.getX() + dx);
                    v.setY(v.getY() + dy);

                    // 更新触摸位置
                    initialTouch[0] = event.getRawX();
                    initialTouch[1] = event.getRawY();
                    break;

                case MotionEvent.ACTION_UP:
                    // 可以在这里处理点击事件或者其他事情
                    break;
            }
            return true;
        });


        type = getIntent().getIntExtra(EXTRA_TYPE, TYPE_UNKNOWN);

        switch (type) {
            case TYPE_EDIT_ATTR:
                EditAttrLayout editAttrLayout = new EditAttrLayout(this);
                editAttrLayout.setOnDragListener(board::updateInfo);
                vContainer.addView(editAttrLayout);
                break;
            case TYPE_RELATIVE_POSITION:
                vContainer.addView(new RelativePositionLayout(this));
                break;
            case TYPE_SHOW_GRIDDING:
                vContainer.addView(new GriddingLayout(this));
                board.updateInfo("LINE_INTERVAL: " + px2dip(GriddingLayout.Companion.getLINE_INTERVAL(), true));
                break;
            default:
                Toast.makeText(this, getString(R.string.uet_coming_soon), Toast.LENGTH_SHORT).show();
                finish();
                break;
        }

        int width = 100;
        int height = 100;
        int marginInDp = 20;

        // 将宽度和高度转换为像素
        Resources resources = getResources();
        float scale = resources.getDisplayMetrics().density;
        int widthInPx = (int) (width * scale + 0.5f);
        int heightInPx = (int) (height * scale + 0.5f);
        int marginInPx = (int) (marginInDp * scale + 0.5f);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(widthInPx, heightInPx);
        params.gravity = BOTTOM;
        params.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);
        vContainer.addView(board, params);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UETool.INSTANCE.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    public void dismissAttrsDialog() {
        for (int i = 0; i < vContainer.getChildCount(); i++) {
            View child = vContainer.getChildAt(i);
            if (child instanceof EditAttrLayout) {
                ((EditAttrLayout) child).dismissAttrsDialog();
            }
        }
    }

    @IntDef({
            TYPE_UNKNOWN,
            TYPE_EDIT_ATTR,
            TYPE_SHOW_GRIDDING,
            TYPE_RELATIVE_POSITION,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int TYPE_UNKNOWN = -1;
        int TYPE_EDIT_ATTR = 1;
        int TYPE_SHOW_GRIDDING = 2;
        int TYPE_RELATIVE_POSITION = 3;
    }
}
