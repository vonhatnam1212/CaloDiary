package com.example.calodiary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Thêm class MealListFragment
public class MealListFragment extends Fragment {
    private static final String ARG_MEAL_TYPE = "meal_type";
    private int mealType;

    public static MealListFragment newInstance(int mealType) {
        MealListFragment fragment = new MealListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MEAL_TYPE, mealType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mealType = getArguments().getInt(ARG_MEAL_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Tạo view cho fragment
        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setContentDescription("Danh sách món ăn");
        
        // TODO: Set adapter và hiển thị danh sách món ăn
        
        return recyclerView;
    }
}
