/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.anisjr.currency.test.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;
import java.util.Map;
import java.util.Set;

import android.test.AndroidTestCase;
import com.example.anisjr.currency.data.CurrencyContract.CurrencyEntry;
import com.example.anisjr.currency.data.CurrencyDbHelper;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(CurrencyDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new CurrencyDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {


        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        CurrencyDbHelper dbHelper = new CurrencyDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = createCurrencyValues();

        long currencyRowId;
        currencyRowId = db.insert(CurrencyEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(currencyRowId != -1);
        Log.d(LOG_TAG, "New row id: " + currencyRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.



        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                CurrencyEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, testValues);

        dbHelper.close();
        }


    static ContentValues createCurrencyValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(CurrencyEntry.COLUMN_DATETEXT,  "2015-03-05");
        testValues.put(CurrencyEntry.COLUMN_FROM_Currency, "EUR");
        testValues.put(CurrencyEntry.COLUMN_TO_Currency,"TND");
        testValues.put(CurrencyEntry.COLUMN_RATE,2.2222);

        return testValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}