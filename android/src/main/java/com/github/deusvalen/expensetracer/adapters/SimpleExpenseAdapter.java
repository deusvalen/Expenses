package com.github.deusvalen.expensetracer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.deusvalen.expensetracer.providers.ExpensesContract;
import com.github.deusvalen.expensetracer.utils.Utils;


public class SimpleExpenseAdapter extends CursorAdapter {
    private String mCurrency;

    public SimpleExpenseAdapter(Context context) {
        super(context, null, 0);
    }

    public void setCurrency(String currency) {
        mCurrency = currency;
        notifyDataSetChanged();
    }

    // Метод используется для вставки нового view и возращает его
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(com.github.deusvalen.expensetracer.R.layout.expense_list_item, parent, false);
    }

    // Метод используется для биндинга всех данных принимаемого view
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Поиск полей для заполнения их по шаблону
        TextView tvExpenseValue = (TextView) view.findViewById(com.github.deusvalen.expensetracer.R.id.expense_value_text_view);
        TextView tvExpenseCurrency = (TextView) view.findViewById(com.github.deusvalen.expensetracer.R.id.expense_currency_text_view);
        TextView tvExpenseCatName = (TextView) view.findViewById(com.github.deusvalen.expensetracer.R.id.expense_category_name_text_view);

        // Вывод значений из курсора
        float expValue = cursor.getFloat(cursor.getColumnIndexOrThrow(ExpensesContract.Expenses.VALUE));
        String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Categories.NAME));

        // Заполнение views полученными значениями
        tvExpenseValue.setText(Utils.formatToCurrency(expValue));
        tvExpenseCatName.setText(categoryName);
        tvExpenseCurrency.setText(mCurrency);
    }
}
