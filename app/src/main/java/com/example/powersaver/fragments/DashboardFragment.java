package com.example.powersaver.fragments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.powersaver.databinding.FragmentDashboardBinding;
public class DashboardFragment extends Fragment{
    private FragmentDashboardBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);


        // on below line creating a child fragment
        Fragment powerFragment = new PowerUsageFragment();
        Fragment billFragment = new BillFragment();

        // on below line creating a fragment transaction and initializing it.
        //FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        //transaction.replace(R.id.PowerUsageFragmentContain, powerFragment).commit();
        //transaction.replace(R.id.BillFragmentContain, billFragment).commit();

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}