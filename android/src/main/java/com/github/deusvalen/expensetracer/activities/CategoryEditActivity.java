package com.github.deusvalen.expensetracer.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import com.github.deusvalen.expensetracer.fragments.CategoryEditFragment;

public class CategoryEditActivity extends BaseFragmentActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        insertFragment(new CategoryEditFragment());
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Показать кнопку в ActionBar
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
