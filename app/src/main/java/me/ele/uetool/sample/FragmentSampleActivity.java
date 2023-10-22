package me.ele.uetool.sample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import me.ele.uetool.sample.ui.fragmentsample.FragmentSampleFragment;

public class FragmentSampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sample_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, FragmentSampleFragment.newInstance())
                    .commitNow();
        }
    }
}
