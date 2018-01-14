package com.github.deusvalen.expensetracer.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import com.github.deusvalen.expensetracer.fragments.ExpenseEditFragment;

public class ExpenseEditActivity extends BaseFragmentActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        insertFragment(new ExpenseEditFragment());
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
