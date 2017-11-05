package com.spit.team_25.cswallet.database;

import android.provider.BaseColumns;

public final class MeassgeReaderContract {

    private MeassgeReaderContract(){}

    public class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "Message";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_FROM = "fromWhom";
    }

    static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + MessageEntry.TABLE_NAME + " ( " + MessageEntry._ID +
            " INTEGER PRIMARY KEY, " + MessageEntry.COLUMN_NAME_DATE + " VARCHAR(30), " +
            MessageEntry.COLUMN_NAME_MESSAGE + " LONGTEXT, " +
            MessageEntry.COLUMN_NAME_FROM + " VARCHAR(5));";

    static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME;
}
