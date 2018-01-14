package com.github.deusvalen.expensetracer.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.github.deusvalen.expensetracer.db.ExpenseDbHelper;

public class ExpensesProvider extends ContentProvider {
    public static final int EXPENSES = 10;
    public static final int EXPENSES_ID = 11;

    public static final int CATEGORIES = 20;
    public static final int CATEGORIES_ID = 21;

    public static final int EXPENSES_WITH_CATEGORIES = 30;
    public static final int EXPENSES_WITH_CATEGORIES_DATE = 31;
    public static final int EXPENSES_WITH_CATEGORIES_DATE_RANGE = 32;
    public static final int EXPENSES_WITH_CATEGORIES_SUM_DATE = 33;
    public static final int EXPENSES_WITH_CATEGORIES_SUM_DATE_RANGE = 34;

    private SQLiteOpenHelper mDbHelper;
    private SQLiteDatabase mDatabase;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ExpensesContract.AUTHORITY, "expenses", EXPENSES);
        sUriMatcher.addURI(ExpensesContract.AUTHORITY, "expenses/#", EXPENSES_ID);
        sUriMatcher.addURI(ExpensesContract.AUTHORITY, "categories", CATEGORIES);
        sUriMatcher.addURI(ExpensesContract.AUTHORITY, "categories/#", CATEGORIES_ID);
        sUriMatcher.addURI(ExpensesContract.AUTHORITY, "expensesWithCategories",
                EXPENSES_WITH_CATEGORIES);
        sUriMatcher.addURI(ExpensesContract.AUTHORITY, "expensesWithCategories/date",
                EXPENSES_WITH_CATEGORIES_DATE);
        sUriMatcher.addURI(ExpensesContract.AUTHORITY, "expensesWithCategories/dateRange",
                EXPENSES_WITH_CATEGORIES_DATE_RANGE);
        sUriMatcher.addURI(ExpensesContract.AUTHORITY, "expensesWithCategories/date/sum",
                EXPENSES_WITH_CATEGORIES_SUM_DATE);
        sUriMatcher.addURI(ExpensesContract.AUTHORITY, "expensesWithCategories/dateRange/sum",
                EXPENSES_WITH_CATEGORIES_SUM_DATE_RANGE);
    }

    /*
     * SELECT expenses._id, expenses.value, categories.name, expenses.date
     * FROM expenses JOIN categories
     * ON expenses.category_id = categories._id
     */
    private static final String BASE_SELECT_JOIN_EXPENSES_CATEGORIES_QUERY =
            "SELECT " + ExpenseDbHelper.EXPENSES_TABLE_NAME + "." + ExpensesContract.Expenses._ID + ", " +
                    ExpenseDbHelper.EXPENSES_TABLE_NAME + "." + ExpensesContract.Expenses.VALUE + ", " +
                    ExpenseDbHelper.CATEGORIES_TABLE_NAME + "." + ExpensesContract.Categories.NAME + ", " +
                    ExpenseDbHelper.EXPENSES_TABLE_NAME + "." + ExpensesContract.Expenses.DATE + " FROM " +
                    ExpenseDbHelper.EXPENSES_TABLE_NAME + " JOIN " + ExpenseDbHelper.CATEGORIES_TABLE_NAME + " ON " +
                    ExpenseDbHelper.EXPENSES_TABLE_NAME + "." + ExpensesContract.Expenses.CATEGORY_ID + " = " +
                    ExpenseDbHelper.CATEGORIES_TABLE_NAME + "." + ExpensesContract.Categories._ID;


    @Override
    public boolean onCreate() {
        mDbHelper = new ExpenseDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        String table;
        String rawQuery;
        mDatabase = mDbHelper.getReadableDatabase();
        switch (sUriMatcher.match(uri)) {
            // Входящий URI для всех категорий
            case CATEGORIES:
                table = ExpenseDbHelper.CATEGORIES_TABLE_NAME;
                sortOrder = (sortOrder == null || sortOrder.isEmpty())
                        ? ExpensesContract.Categories.DEFAULT_SORT_ORDER
                        : sortOrder;
                break;

            // Входящий URI для одной строки из категорий
            case CATEGORIES_ID:
                table = ExpenseDbHelper.CATEGORIES_TABLE_NAME;
                // Defines selection criteria for the row to query
                selection = ExpensesContract.Categories._ID + " = ?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                break;

            // Входящий URI для всех трат
            case EXPENSES:
                table = ExpenseDbHelper.EXPENSES_TABLE_NAME;
                sortOrder = (sortOrder == null || sortOrder.isEmpty())
                        ? ExpensesContract.Expenses.DEFAULT_SORT_ORDER
                        : sortOrder;
                break;

            // Входящий URI для одной строки из трат
            case EXPENSES_ID:
                table = ExpenseDbHelper.EXPENSES_TABLE_NAME;
                // Определяет критерии выборки для строки в запросе
                selection = ExpensesContract.Expenses._ID + " = ?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                break;

            // Входящий URI для всех трат с категориями
            case EXPENSES_WITH_CATEGORIES:
                /*
                 * SELECT expenses._id, expenses.value, categories.name, expenses.date
                 * FROM expenses JOIN categories
                 * ON expenses.category_id = categories._id
                 */
                return mDatabase.rawQuery(BASE_SELECT_JOIN_EXPENSES_CATEGORIES_QUERY, null);

            // Входящий URI для трат с категориями по определенной дате
            case EXPENSES_WITH_CATEGORIES_DATE:
                /*
                 * SELECT expenses._id, expenses.value, categories.name, expenses.date
                 * FROM expenses JOIN categories
                 * ON expenses.category_id = categories._id
                 * WHERE expense.date = ?
                 */
                rawQuery =
                        BASE_SELECT_JOIN_EXPENSES_CATEGORIES_QUERY + " WHERE " +
                        ExpenseDbHelper.EXPENSES_TABLE_NAME + "." + ExpensesContract.Expenses.DATE + " = ?";

                return mDatabase.rawQuery(rawQuery, selectionArgs);

            // Входящий URI для для суммы трат по промежутку дат
            case EXPENSES_WITH_CATEGORIES_SUM_DATE:
                /*
                 * SELECT SUM(expenses.value) as values_sum
                 * FROM expenses WHERE expenses.date = ?
                 */
                rawQuery =
                        "SELECT SUM(" + ExpenseDbHelper.EXPENSES_TABLE_NAME + "." + ExpensesContract.Expenses.VALUE + ") as " +
                        ExpensesContract.Expenses.VALUES_SUM + " FROM " + ExpenseDbHelper.EXPENSES_TABLE_NAME +
                        " WHERE " + ExpenseDbHelper.EXPENSES_TABLE_NAME + "." + ExpensesContract.Expenses.DATE + " = ?";

                return mDatabase.rawQuery(rawQuery, selectionArgs);

            // Входящий URI для трат с категориями по промежутку дат
            case EXPENSES_WITH_CATEGORIES_DATE_RANGE:
                /*
                 * SELECT expenses._id, expenses.value, categories.name, expenses.date
                 * FROM expenses JOIN categories
                 * ON expenses.category_id = categories._id
                 * WHERE expense.date BETWEEN ? AND ?
                 */
                rawQuery =
                        BASE_SELECT_JOIN_EXPENSES_CATEGORIES_QUERY + " WHERE " +
                        ExpenseDbHelper.EXPENSES_TABLE_NAME + "." + ExpensesContract.Expenses.DATE + " BETWEEN ? AND ?";

                return mDatabase.rawQuery(rawQuery, selectionArgs);

            // Входящий URI для суммы трат по промежутку дат
            case EXPENSES_WITH_CATEGORIES_SUM_DATE_RANGE:
                /*
                 * SELECT SUM(expenses.value) as values_sum
                 * FROM expenses WHERE expense.date BETWEEN ? AND ?
                 */
                rawQuery =
                        "SELECT SUM(" + ExpenseDbHelper.EXPENSES_TABLE_NAME + "." + ExpensesContract.Expenses.VALUE + ") as " +
                        ExpensesContract.Expenses.VALUES_SUM + " FROM " + ExpenseDbHelper.EXPENSES_TABLE_NAME +
                        " WHERE " + ExpenseDbHelper.EXPENSES_TABLE_NAME + "." + ExpensesContract.Expenses.DATE + " BETWEEN ? AND ?";

                return mDatabase.rawQuery(rawQuery, selectionArgs);

            default:
                throw new IllegalArgumentException("Unknown Uri provided.");
        }

            cursor = mDatabase.query(
                    table,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );

            return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String table;
        Uri contentUri;
        switch (sUriMatcher.match(uri)) {
            // Входящий URI для всех категорий
            case CATEGORIES:
                table = ExpenseDbHelper.CATEGORIES_TABLE_NAME;
                contentUri = ExpensesContract.Categories.CONTENT_URI;
                break;
            // Входящий URI для всех трат
            case EXPENSES:
                table = ExpenseDbHelper.EXPENSES_TABLE_NAME;
                contentUri = ExpensesContract.Expenses.CONTENT_URI;
                break;
            // Входящий URI для одной строки категорий
            case CATEGORIES_ID:
            // Входящий URI для одной строки трат
            case EXPENSES_ID:
                throw new UnsupportedOperationException("Inserting rows with specified IDs is forbidden.");
            case EXPENSES_WITH_CATEGORIES:
            case EXPENSES_WITH_CATEGORIES_DATE:
            case EXPENSES_WITH_CATEGORIES_DATE_RANGE:
            case EXPENSES_WITH_CATEGORIES_SUM_DATE:
            case EXPENSES_WITH_CATEGORIES_SUM_DATE_RANGE:
                throw new UnsupportedOperationException("Modifying joined results is forbidden.");
            default:
                throw new IllegalArgumentException("Unknown Uri provided.");
        }

        mDatabase = mDbHelper.getWritableDatabase();

        long newRowID = mDatabase.insert(
                table,
                null,
                values
        );

        Uri newItemUri = ContentUris.withAppendedId(contentUri, newRowID);

        return (newRowID < 1) ? null : newItemUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table;
        switch (sUriMatcher.match(uri)) {
            // Входящий URI для одной строки из категорий
            case CATEGORIES_ID:
                table = ExpenseDbHelper.CATEGORIES_TABLE_NAME;
                // Определяет критерии выборки для удаления строки
                selection = ExpensesContract.Categories._ID + " = ?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                break;
            // Входящий URI для для всех трат
            case EXPENSES:
                table = ExpenseDbHelper.EXPENSES_TABLE_NAME;
                break;
            // Входящий URI для для одиночной строки из трат
            case EXPENSES_ID:
                table = ExpenseDbHelper.EXPENSES_TABLE_NAME;
                // Определяет критерии выборки для удаления строки
                selection = ExpensesContract.Expenses._ID + " = ?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                break;
            // Входящий URI для всех категорий
            case CATEGORIES:
                throw new UnsupportedOperationException("Removing multiple rows from the table is forbidden.");
            case EXPENSES_WITH_CATEGORIES:
            case EXPENSES_WITH_CATEGORIES_DATE:
            case EXPENSES_WITH_CATEGORIES_DATE_RANGE:
            case EXPENSES_WITH_CATEGORIES_SUM_DATE:
            case EXPENSES_WITH_CATEGORIES_SUM_DATE_RANGE:
                throw new UnsupportedOperationException("Modifying joined results is forbidden.");
            default:
                throw new IllegalArgumentException("Unknown Uri provided.");
        }

        mDatabase = mDbHelper.getWritableDatabase();

        return mDatabase.delete(
                table,
                selection,
                selectionArgs
        );
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String table;
        switch (sUriMatcher.match(uri)) {

            // Входящий URI для строки из категорий
            case CATEGORIES_ID:
                table = ExpenseDbHelper.CATEGORIES_TABLE_NAME;
                // Определяет критерии выборки для удаления строки
                selection = ExpensesContract.Categories._ID + " = ?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                break;

            // Входящий URI для строки из трат
            case EXPENSES_ID:
                table = ExpenseDbHelper.EXPENSES_TABLE_NAME;
                // Определяет критерии выборки для удаления строки
                selection = ExpensesContract.Expenses._ID + " = ?";
                selectionArgs = new String[]{ uri.getLastPathSegment() };
                break;

            // Входящий URI для всех категорий
            case CATEGORIES:

            // Входящий URI для всех трат
            case EXPENSES:
                throw new UnsupportedOperationException("Updating multiple table rows is forbidden.");
            case EXPENSES_WITH_CATEGORIES:
            case EXPENSES_WITH_CATEGORIES_DATE:
            case EXPENSES_WITH_CATEGORIES_DATE_RANGE:
            case EXPENSES_WITH_CATEGORIES_SUM_DATE:
            case EXPENSES_WITH_CATEGORIES_SUM_DATE_RANGE:
                throw new UnsupportedOperationException("Modifying joined results is forbidden.");
            default:
                throw new IllegalArgumentException("Unknown Uri provided.");
        }

        mDatabase = mDbHelper.getWritableDatabase();

        return mDatabase.update(
                table,
                values,
                selection,
                selectionArgs
        );
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CATEGORIES:
                return ExpensesContract.Categories.CONTENT_TYPE;
            case CATEGORIES_ID:
                return ExpensesContract.Categories.CONTENT_ITEM_TYPE;
            case EXPENSES:
                return ExpensesContract.Expenses.CONTENT_TYPE;
            case EXPENSES_ID:
                return ExpensesContract.Expenses.CONTENT_ITEM_TYPE;
            case EXPENSES_WITH_CATEGORIES:
            case EXPENSES_WITH_CATEGORIES_DATE:
            case EXPENSES_WITH_CATEGORIES_DATE_RANGE:
            case EXPENSES_WITH_CATEGORIES_SUM_DATE:
            case EXPENSES_WITH_CATEGORIES_SUM_DATE_RANGE:
                return ExpensesContract.ExpensesWithCategories.CONTENT_TYPE;
            default:
                return null;
        }
    }
}
