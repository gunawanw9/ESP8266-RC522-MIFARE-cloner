package edu.labemp.inaki.rfidcloner.Model;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.labemp.inaki.rfidcloner.Controller.ESP8266Connector;
import edu.labemp.inaki.rfidcloner.Model.CardsContract.*;

/**
 * Created by inaki on 5/17/17.
 */

public class CardsDataSource {

    private CardsDbHelper mDbHelper;

    public CardsDataSource(Context context) {
        mDbHelper = new CardsDbHelper(context);
    }

    /*public long addCard(Card card) {
        return addCard(card.toJSON());
    }*/

    public long addCard(JSONObject cardJSON) {
        return addCard(cardJSON.toString());
    }

    public long addCard(String cardJSONSring) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(CardEntry.COLUMN_NAME_JSONCARD, cardJSONSring);

        // Insert the new row, returning the primary key value of the new row
        return db.insert(CardEntry.TABLE_NAME, null, values);
    }

    public int updateCard(Card card) {
        int cardId = card.getId();
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(CardEntry.COLUMN_NAME_JSONCARD, card.toJSONString());

        // Which row to update, based on the title
        String selection = CardEntry._ID+ " LIKE ?";
        String[] selectionArgs = { Integer.toString(cardId) };

        // Insert the new row, returning the primary key value of the new row
        return db.update(CardEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    public List<Card> getCards() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                CardEntry._ID,
                CardEntry.COLUMN_NAME_JSONCARD
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                CardEntry._ID + " DESC";

        Cursor cursor = db.query(
                CardEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<Card> cardsList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                cardsList.add(new Card(cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_NAME_JSONCARD)),
                        cursor.getInt(cursor.getColumnIndex(CardEntry._ID))));
            } while (cursor.moveToNext());
        }

        return cardsList;
    }

    public Card getCard(int id) {
        return new Card(getCardJSON(id), id);
    }

    public String getCardJSON(int id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                CardEntry._ID,
                CardEntry.COLUMN_NAME_JSONCARD
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = CardEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(id)};


        Cursor cursor = db.query(
                CardEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                     // The columns for the WHERE clause
                selectionArgs,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        String cardJSONString = "";

        if (cursor.moveToFirst()) {
            do {
                cardJSONString = cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_NAME_JSONCARD));

            } while (cursor.moveToNext());
        }

        return cardJSONString;
    }

    public int delCard(int id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Define 'where' part of query.
        String selection = CardEntry._ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { Integer.toString(id) };
        // Issue SQL statement.
        return db.delete(CardEntry.TABLE_NAME, selection, selectionArgs);
    }

    public void createEmptyCard(Context context) {
        addCard(Card.getEmptyCardJson());
        Toast.makeText(context, "New card added", Toast.LENGTH_SHORT).show();
        notifyCardsList(context, ESP8266Connector.UPDATE_ACTION);
    }

    public void notifyCardsList(Context context, String action) {
        Intent intent = new Intent();
        Log.d("ESP8266Connector", "Sending: " + action);
        intent.setAction(action);
        context.sendBroadcast(intent);
    }
}
