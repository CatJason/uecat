package me.ele.uetool.sample.ui.fragmentsample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import me.ele.uetool.sample.R;

public class FragmentSampleFragment extends Fragment {

    public static FragmentSampleFragment newInstance() {
        return new FragmentSampleFragment();
    }

    private FragmentSampleViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sample_fragment, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(FragmentSampleViewModel.class);
        // TODO: Use the ViewModel

        getChildFragmentManager().beginTransaction()
                .replace(R.id.container, FragmentSampleFragment2.newInstance())
                .commitNow();
    }
}
