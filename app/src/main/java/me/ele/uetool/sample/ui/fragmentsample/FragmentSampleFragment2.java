package me.ele.uetool.sample.ui.fragmentsample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import me.ele.uetool.sample.R;

public class FragmentSampleFragment2 extends Fragment {

    public static FragmentSampleFragment2 newInstance() {
        return new FragmentSampleFragment2();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample_fragment, container, false);
    }
}