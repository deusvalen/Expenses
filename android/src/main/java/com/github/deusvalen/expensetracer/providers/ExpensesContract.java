package com.github.deusvalen.expensetracer.providers;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ExpensesContract {
    /**
     * Authority для expenses provider
     */
    public static final String AUTHORITY = "com.github.deusvalen.expensetracer.provider";
    /**
     * content:// стиль URI для expenses provider
     */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * Contract класс не может быть инстанцирован
     */
    private ExpensesContract(){}

    public static class Categories implements BaseColumns, CategoriesColumns {
        /**
         * Этот utility класс не может быть инстанцирован
         */
        private Categories() {}

        /**
         * content:// стиль URI для этой таблицы
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "categories");

        /**
         * MIME тип {@link #CONTENT_URI} для доступа к папке с категориями.
         */
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.deusvalen.expensetracer.provider.expense_category";

        /**
         * MIME тип {@link #CONTENT_URI} поддиректория одной категории.
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.deusvalen.expensetracer.provider.expense_category";

        /**
         * Сортировка по возрастанию _id в столбце (порядок по которому было добавление)
         */
        public static final String DEFAULT_SORT_ORDER = _ID + " ASC";
    }

    public static class Expenses implements BaseColumns, ExpensesColumns {
        /**
         * utility класс не может быть инстанцирован
         */
        private Expenses() {}

        /**
         * content:// стиль URI для этой таблицы
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "expenses");

        /**
         * MIME тип {@link #CONTENT_URI} предоставляет доступ к папке с тратами.
        */
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.deusvalen.expensetracer.provider.expense";

        /**
         * MIME тип {@link #CONTENT_URI} поддериктории одной траты.
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.deusvalen.expensetracer.provider.expense";

        /**
         * Сортировка по убыванию даты (самые свежие в конце)
         */
        public static final String DEFAULT_SORT_ORDER = DATE + " ASC";

        /**
         * Значение имени столбца суммы трат для объединения таблиц
         */
        public static final String VALUES_SUM = "values_sum";
    }

    public static class ExpensesWithCategories implements BaseColumns {
        /**
         * Utility класс не может быть инстанцирован.
         */
        private ExpensesWithCategories() {}

        /**
         * content:// стиль URI для этой таблицы.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "expensesWithCategories");

        /**
         * MIME тип {@link #CONTENT_URI} доступ к папке трат с категориями.
         */
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.deusvalen.expensetracer.provider.expense_with_category";

        /**
         * content:// стиль URI для соединенной таблицы для фильтрации по дате.
         */
        public static final Uri DATE_CONTENT_URI = Uri.withAppendedPath(CONTENT_URI, "date");

        /**
         * content:// стиль URI для соединенной таблицы для фильтрации по промежутку дат.
         */
        public static final Uri DATE_RANGE_CONTENT_URI = Uri.withAppendedPath(CONTENT_URI, "dateRange");

        /**
         * content:// стиль URI для получения суммы трат
         * для этого объединяем таблицу по фильтру "дата".
         */
        public static final Uri SUM_DATE_CONTENT_URI = Uri.withAppendedPath(DATE_CONTENT_URI, "sum");

        /**
         * content:// стиль URI для получения суммы трат
         * для этого объединям таблицу по фильтру "Промежуток дат".
         */
        public static final Uri SUM_DATE_RANGE_CONTENT_URI =
                Uri.withAppendedPath(DATE_RANGE_CONTENT_URI, "sum");
    }

    protected interface CategoriesColumns {
        String NAME = "name";
    }

    protected interface ExpensesColumns {
        String VALUE = "value";
        String DATE = "date";
        String CATEGORY_ID = "category_id";
    }
}
