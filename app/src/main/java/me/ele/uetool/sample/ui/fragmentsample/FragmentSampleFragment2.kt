package me.ele.uetool.sample.ui.fragmentsample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.ele.uetool.sample.R

class FragmentSampleFragment2 : Fragment() {

    companion object {
        fun newInstance() = FragmentSampleFragment2()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sample_fragment, container, false)
    }
}