package com.github.deusvalen.expensetracer.fragments;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.AppCompatSpinner;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.github.deusvalen.expensetracer.providers.ExpensesContract;
import com.github.deusvalen.expensetracer.utils.Utils;

import java.util.ArrayList;
import java.util.Date;

public class ExpenseEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String EXTRA_EDIT_EXPENSE = "com.github.deusvalen.expensetracer.edit_expense";

    private static final int EXPENSE_LOADER_ID = 1;
    private static final int CATEGORIES_LOADER_ID = 0;

    private EditText mExpValueEditText;
    private AppCompatSpinner mCategorySpinner;
    private SimpleCursorAdapter mAdapter;
    private View mCatProgressBar;
    private long mExtraValue;
    private long mExpenseCategoryId = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Вставка layout для этого фрагмента
        View rootView = inflater.inflate(com.github.deusvalen.expensetracer.R.layout.fragment_expense_edit, container, false);

        mExpValueEditText = (EditText) rootView.findViewById(com.github.deusvalen.expensetracer.R.id.expense_value_edit_text);
        mCatProgressBar = rootView.findViewById(com.github.deusvalen.expensetracer.R.id.cat_select_progress_bar);
        mCategorySpinner = (AppCompatSpinner) rootView.findViewById(com.github.deusvalen.expensetracer.R.id.category_choose_spinner);

        setEditTextDefaultValue();

        // Нажатие кнопки Done на клавиатуре
        mExpValueEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    checkValueFieldForIncorrectInput();
                    return true;
                }
                return false;
            }
        });

        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mExpenseCategoryId = id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                null,
                new String[] { ExpensesContract.Categories.NAME },
                new int[] { android.R.id.text1 },
                0);
        // Уточнение layout'a для использования в списке с выбором
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применение адаптера к spinner
        mCategorySpinner.setAdapter(mAdapter);

        mExtraValue = getActivity().getIntent().getLongExtra(EXTRA_EDIT_EXPENSE, -1);
        // Создать новую затрату
        if (mExtraValue < 1) {
            getActivity().setTitle(com.github.deusvalen.expensetracer.R.string.add_expense);
            loadCategories();

            // Изменить существующую затрату
        } else {
            getActivity().setTitle(com.github.deusvalen.expensetracer.R.string.edit_expense);
            loadExpenseData();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(com.github.deusvalen.expensetracer.R.menu.fragment_expense_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.github.deusvalen.expensetracer.R.id.done_expense_edit_menu_item:
                if (checkForIncorrectInput()) {
                    // Создать новую затрату
                    if (mExtraValue < 1) {
                        insertNewExpense();

                    // Изменить существующую затрату
                    } else {
                        updateExpense(mExtraValue);
                    }
                    getActivity().finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkForIncorrectInput() {
        if (!checkValueFieldForIncorrectInput()) {
            mExpValueEditText.selectAll();
            return false;
        }
        // Будущая проверка других полей

        return true;
    }

    private boolean checkValueFieldForIncorrectInput() {
        String etValue = mExpValueEditText.getText().toString();
        try {
            if (etValue.length() == 0) {
                mExpValueEditText.setError(getResources().getString(com.github.deusvalen.expensetracer.R.string.error_empty_field));
                return false;
            } else if (Float.parseFloat(etValue) == 0.00f) {
                mExpValueEditText.setError(getResources().getString(com.github.deusvalen.expensetracer.R.string.error_zero_value));
                return false;
            }
        } catch (Exception e) {
            mExpValueEditText.setError(getResources().getString(com.github.deusvalen.expensetracer.R.string.error_incorrect_input));
            return false;
        }
        return true;
    }

    private void loadCategories() {
        // Показать progress bar следующим за spinner из категории
        mCatProgressBar.setVisibility(View.VISIBLE);

        getLoaderManager().initLoader(CATEGORIES_LOADER_ID, null, this);
    }

    private void loadExpenseData() {
        getLoaderManager().initLoader(EXPENSE_LOADER_ID, null, this);
        loadCategories();
    }

    private void setEditTextDefaultValue() {
        mExpValueEditText.setText(String.valueOf(0));
        mExpValueEditText.selectAll();
    }

    @Override
    public CursorLoader onCreateLoader(int id, Bundle args) {
        String[] projectionFields = null;
        Uri uri = null;
        switch (id) {
            case EXPENSE_LOADER_ID:
                projectionFields = new String[] {
                        ExpensesContract.Expenses._ID,
                        ExpensesContract.Expenses.VALUE,
                        ExpensesContract.Expenses.CATEGORY_ID
                };

                uri = ContentUris.withAppendedId(ExpensesContract.Expenses.CONTENT_URI, mExtraValue);
                break;
            case CATEGORIES_LOADER_ID:
                projectionFields = new String[] {
                        ExpensesContract.Categories._ID,
                        ExpensesContract.Categories.NAME
                };

                uri = ExpensesContract.Categories.CONTENT_URI;
                break;
        }

        return new CursorLoader(getActivity(),
                uri,
                projectionFields,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case EXPENSE_LOADER_ID:
                int expenseValueIndex = data.getColumnIndex(ExpensesContract.Expenses.VALUE);
                int expenseCategoryIdIndex = data.getColumnIndex(ExpensesContract.Expenses.CATEGORY_ID);

                data.moveToFirst();
                mExpenseCategoryId = data.getLong(expenseCategoryIdIndex);
                updateSpinnerSelection();

                mExpValueEditText.setText(String.valueOf(data.getFloat(expenseValueIndex)));
                mExpValueEditText.selectAll();
                break;
            case CATEGORIES_LOADER_ID:
                // Спрятать Progress bar следующий за spinner'ом категории
                mCatProgressBar.setVisibility(View.GONE);

                if (null == data || data.getCount() < 1) {
                    mExpenseCategoryId = -1;
                    // Заполнение spinner'a значениями по-умолчанию
                    ArrayList<String> defaultItems = new ArrayList<>();
                    defaultItems.add(getResources().getString(com.github.deusvalen.expensetracer.R.string.no_categories_string));

                    ArrayAdapter<String> tempAdapter = new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_spinner_item,
                            defaultItems);
                    mCategorySpinner.setAdapter(tempAdapter);
                    // Отключение spinner'a
                    mCategorySpinner.setEnabled(false);
                } else {
                    // Установка адаптера
                    mCategorySpinner.setAdapter(mAdapter);
                    // Обновление данных spinner'a
                    mAdapter.swapCursor(data);
                    // Включение spinner'a
                    mCategorySpinner.setEnabled(true);
                    updateSpinnerSelection();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case EXPENSE_LOADER_ID:
                mExpenseCategoryId = -1;
                setEditTextDefaultValue();
                break;
            case CATEGORIES_LOADER_ID:
                mAdapter.swapCursor(null);
                break;
        }
    }

    private void updateSpinnerSelection() {
        mCategorySpinner.setSelection(0);
        for (int pos = 0; pos < mAdapter.getCount(); ++pos) {
            if (mAdapter.getItemId(pos) == mExpenseCategoryId) {
                // Установка выбранного значения spinner'a в соответствии
                // со значениями из БД
                mCategorySpinner.setSelection(pos);
                break;
            }
        }
    }

    private void insertNewExpense() {
        ContentValues insertValues = new ContentValues();
        insertValues.put(ExpensesContract.Expenses.VALUE, Float.parseFloat(mExpValueEditText.getText().toString()));
        insertValues.put(ExpensesContract.Expenses.DATE, Utils.getDateString(new Date())); // Put current date (today)
        insertValues.put(ExpensesContract.Expenses.CATEGORY_ID, mExpenseCategoryId);

        getActivity().getContentResolver().insert(
                ExpensesContract.Expenses.CONTENT_URI,
                insertValues
        );

        Toast.makeText(getActivity(),
                getResources().getString(com.github.deusvalen.expensetracer.R.string.expense_added),
                Toast.LENGTH_SHORT).show();
    }

    private void updateExpense(long id) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(ExpensesContract.Expenses.VALUE, Float.parseFloat(mExpValueEditText.getText().toString()));
        updateValues.put(ExpensesContract.Expenses.CATEGORY_ID, mExpenseCategoryId);

        Uri expenseUri = ContentUris.withAppendedId(ExpensesContract.Expenses.CONTENT_URI, id);

        getActivity().getContentResolver().update(
                expenseUri,
                updateValues,
                null,
                null
        );

        Toast.makeText(getActivity(),
                getResources().getString(com.github.deusvalen.expensetracer.R.string.expense_updated),
                Toast.LENGTH_SHORT).show();
    }
}
