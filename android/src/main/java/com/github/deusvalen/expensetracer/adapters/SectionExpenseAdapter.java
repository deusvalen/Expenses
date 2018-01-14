package com.github.deusvalen.expensetracer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.deusvalen.expensetracer.providers.ExpensesContract;
import com.github.deusvalen.expensetracer.utils.Utils;
import com.github.deusvalen.expensetracer.R;
import com.twotoasters.sectioncursoradapter.SectionCursorAdapter;

public class SectionExpenseAdapter extends SectionCursorAdapter {
    private String mCurrency;

    public SectionExpenseAdapter(Context context) {
        super(context, null, 0);
    }

    public void setCurrency(String currency) {
        mCurrency = currency;
        notifyDataSetChanged();
    }

    @Override
    protected Object getSectionFromCursor(Cursor cursor) {
        String dateStr = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Expenses.DATE));
        return Utils.getSystemFormatDateString(mContext, dateStr);
    }

    @Override
    protected View newSectionView(Context context, Object item, ViewGroup parent) {
        return getLayoutInflater().inflate(R.layout.expense_report_section_header, parent, false);
    }

    @Override
    protected void bindSectionView(View convertView, Context context, int position, Object item) {
        ((TextView) convertView).setText((String) item);
    }

    @Override
    protected View newItemView(Context context, Cursor cursor, ViewGroup parent) {
        return getLayoutInflater().inflate(R.layout.expense_list_item, parent, false);
    }

    @Override
    protected void bindItemView(View convertView, Context context, Cursor cursor) {
        // Поиск полей для загрузки в них данных по шаблону
        TextView tvExpenseValue = (TextView) convertView.findViewById(R.id.expense_value_text_view);
        TextView tvExpenseCurrency = (TextView) convertView.findViewById(R.id.expense_currency_text_view);
        TextView tvExpenseCatName = (TextView) convertView.findViewById(R.id.expense_category_name_text_view);

        // Вывод значений из курсора
        float expValue = cursor.getFloat(cursor.getColumnIndexOrThrow(ExpensesContract.Expenses.VALUE));
        String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Categories.NAME));

        // Установка в views извлеченными значениями
        tvExpenseValue.setText(Utils.formatToCurrency(expValue));
        tvExpenseCatName.setText(categoryName);
        tvExpenseCurrency.setText(mCurrency);
    }
}
