package com.binal.chatapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.CRC32;

/**
 * Database handler class that contains APIs that can be used to handle necessary functions
 * Created by binal on 19/7/16.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String logH = "DBHelper";

    // constants that are used in db calls
    private static final int DB_V = 1;
    private static final String DB_N = "chatapp";

    private static final String tbu = "users";
    private static final String tbm = "ToM";

    private static final String idA = "id_A";
    private static final String userA = MainActivity.user_id;
    private static int userAI;
    private static final String idB = "id_B";
    private static final String name = "name";
    private static final String rflag = "rflag";

    private static final String mid = "msg_id";
    private static final String msg = "message";
    private static final String Tc  = "created_time";

    private static final String rid = "rid";

    private SQLiteDatabase db;
    private Context context;

    public DBHelper(Context context){
        super(context, DB_N, null, DB_V);
        this.context = context;
        if(userA!=null)
           userAI = Integer.parseInt(userA);
        Log.d(logH, "Constructor Called " + userA + " : " + userAI);
    }

    // create the three tables that are required by the app
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + tbu + "( " + idB + " CHAR(10), " + name + " CHAR(64), PRIMARY KEY (" + idB + "));");
        db.execSQL("CREATE TABLE " + tbm + "( " + mid + " CHAR(20), " + rid + " CHAR(20), " + idA + " CHAR(10), " + idB + " CHAR(10), " + name + " CHAR(64), " + msg + " BLOB, " + Tc + " CHAR(20), " + rflag + " CHAR(1), PRIMARY KEY (" + mid + "));");
        Log.d(logH, "Tables Created");

    }

    // on updation, drop the tables and recreate them
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldv, int newv){
        db.execSQL("DROP TABLE IF EXISTS " + tbu );
        db.execSQL("DROP TABLE IF EXISTS " + tbm );
        onCreate(db);
        Log.d(logH, "Tables Updated");

    }

    // to add a contact to the table users
    // TODO Check SQLInjection
    public void addContact(Contact contact){
        db = this.getWritableDatabase();
        db.execSQL("INSERT INTO " + tbu + " (" + idB + ", " + name + ") VALUES ( '" + contact.getId() + "', '" + contact.getName() + "');");
        db.close();
        Log.d(logH, "New Contact Inserted : " + contact.getId() + " - " + contact.getName());

    }

    // to get a single contact using user_id
    public Contact getContact(String id){
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tbu + " WHERE " + idB + " = ?", new String[]{id});
        cursor.moveToFirst();
        Contact contact = new Contact();
        contact.setId(cursor.getString(0));
        contact.setName(cursor.getString(1));
        //contact.setLast_seen(cursor.get);
        //Log.d("getDB", "" + contact.getId()+ " " + contact.getName() + " " + cursor.getBlob(2));
        cursor.close();
        db.close();
        return contact;
    }

    // to get all the contacts in the database
    public ArrayList<Contact> getAllContacts(){
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tbu, null);
        ArrayList<Contact> contacts = new ArrayList<>();
        for(int i = 0; i<cursor.getCount(); i++){
            cursor.moveToNext();
            Contact contact = new Contact();
            contact.setId(cursor.getString(0));
            contact.setName(cursor.getString(1));
            contacts.add(contact);
        }
        cursor.close();
        db.close();

        Log.d(logH, "All Contacts Fetched : count = " + contacts.size());

        return contacts;
    }

    // to get all user Ids (probably useless soon)
    // this function is currently used to populate the ChatNow and ShowFriends Activities
    // but since only name are to be used, this will become obsolete
    public String[] getAllUserIDs(){
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tbu, null);

        if(cursor.getCount() == 0) return null;

        String[] id_array = new String[cursor.getCount()];
        for(int i = 0; i< cursor.getCount(); i++){
            cursor.moveToNext();
            id_array[i] = cursor.getString(0);
        }

        cursor.close();
        db.close();
        return id_array;
    }

    // to update contact
    // this is probably never called as there is only one field for a contact that can be edited.
    // and that may be unnecessary, or dangerous
    public void updateContact(Contact contact){
        db = this.getWritableDatabase();
        db.execSQL("UPDATE " + tbu + " SET " + name + " = " + contact.getName() + " WHERE " + idB + " = " + contact.getId());
        db.close();
    }

    // to remove contact from Database
    public void deleteContact(String id){
        db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + tbu + " WHERE " + idB + " = " + id);
        db.close();
    }

    public String getContactName(String id){
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT (" + name + ") FROM users WHERE " + idB + " = ?", new String[]{id});
        cursor.moveToFirst();
        String Name = cursor.getString(cursor.getColumnIndex(name));
        db.close();
        cursor.close();

        Log.d(logH, "Contact Name Obtained : " + id + " = " + Name);
        return Name;
    }

    // to insert a message to database
/*    public void addMsg(Message mesg, boolean read){
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(mid, mesg.getMid());
        contentValues.put(idA, mesg.getIda());
        contentValues.put(idB, mesg.getIdb());
        contentValues.put(name, mesg.getName());
        contentValues.put(msg, mesg.getMsg());
        contentValues.put(Tc,  mesg.getCreated());
        contentValues.put(rflag, mesg.getRflag());

        db.insert(tbm, null, contentValues);
        db.close();
    }
*/
    public void addMsg(String msid, String Aid, String Bid, String Name, String message, String Ct, boolean read){
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        int a = Integer.parseInt(Aid);
        int b = Integer.parseInt(Bid);
        String Rid;
        if(a>b)
            Rid = "" + b + a;
        else
            Rid = "" + a + b;

        contentValues.put(mid, msid);
        contentValues.put(rid,Rid);
        contentValues.put(idA, Aid);
        contentValues.put(idB, Bid);
        contentValues.put(name, Name);
        contentValues.put(msg, message);
        contentValues.put(Tc, Ct);

        if(read)
            contentValues.put(rflag, "y");
        else
            contentValues.put(rflag, "n");
        db.insert(tbm, null, contentValues);
        db.close();
        Log.d(logH, "Message Added " + Rid + " : " + msid + " : " + Aid + " : " + Bid + " : " + Name + " : " + message + " : " + Ct + " : " + rflag);

    }

    // to delete a message from the database
    public void deleteMsg(String meid){
        db = this.getWritableDatabase();
        db.delete(tbm, mid, new String[]{meid});
        db.close();
    }

    // to get a particular message from database
    // probably is useless
    public Message getMsg(String meid){
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tbm + " WHERE " + mid + " = ?", new String[]{meid});
        cursor.moveToFirst();
        Message msg = new Message();
        msg.setMid(cursor.getString(0));
        msg.setIda(cursor.getString(2));
        msg.setIdb(cursor.getString(3));
        msg.setName(cursor.getString(4));
        msg.setMsg(cursor.getString(5));
        msg.setCreated(cursor.getString(6));
        msg.setRflag(cursor.getString(7));
        cursor.close();
        db.close();

        return msg;
    }

    // to get all the messages from the database
    // also probably useless
    public ArrayList<Message> getAllMsgs(){
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tbm, null);
        ArrayList<Message> msgList = new ArrayList<>();
        for(int i = 0; i<cursor.getCount(); i++){
            cursor.moveToNext();
            Message msg = new Message();
            msg.setMid(cursor.getString(0));
            msg.setIda(cursor.getString(2));
            msg.setIdb(cursor.getString(3));
            msg.setName(cursor.getString(4));
            msg.setMsg(cursor.getString(5));
            msg.setCreated(cursor.getString(6));
            msg.setRflag(cursor.getString(7));
            msgList.add(msg);
        }

        cursor.close();
        db.close();
        return msgList;
    }

    // to get all messages related to a particular user_id from database
    // TODO limit the number of messages retreived -- DONE NOT TESTED
    public ArrayList<Message> getMessagesFromUserId(String id){
        db = this.getReadableDatabase();
        int b = Integer.parseInt(id);
        String Rid = userAI>b? "" + b + userA : "" + userA + b;
        Cursor cursor = db.rawQuery("SELECT * FROM " + tbm + " WHERE " + rid + " = ? ORDER BY " + Tc + " DESC LIMIT 30;", new String[]{Rid});
        ArrayList<Message> list = new ArrayList<>();
        for(int i = 0; i<cursor.getCount(); i++){
            cursor.moveToNext();
            Message message = new Message();
            message.setMid(cursor.getString(0));
            message.setIda(cursor.getString(2));
            message.setIdb(cursor.getString(3));
            message.setName(cursor.getString(4));
            message.setMsg(cursor.getString(5));
            message.setCreated(cursor.getString(6));
            message.setRflag(cursor.getString(7));
            list.add(0, message);
            Log.d(logH, "listing : " + message.getMid() + "   " + message.getIda() + "   " + message.getIdb() + "   " + message.getMsg() + "   " + message.getName() + "   " + message.getCreated() + "    " + message.getRflag());
        }
        cursor.close();
        db.close();

        Log.d(logH, "All messages for " + Rid + " Obtained : count = " + list.size());

        return list;
    }

    // to get older messages older than a particular message
    // this will probably be used instead of getMessagesFromUserId()
    // TODO this is not correct, fix it!
    public ArrayList<Message> getMessagesPastThis(String userId, String messageId){
        db = this.getReadableDatabase();
        int b = Integer.parseInt(userId);
        String Rid;
        if(userAI>b)
            Rid = "" + b + userA;
        else
            Rid ="" + userA + b;
        Cursor cursor = db.rawQuery("SELECT * FROM " + tbm + " WHERE " + rid + " = ? ORDER BY " + Tc + " DESC LIMIT 30;", new String[]{Rid});
        ArrayList<Message> list = new ArrayList<>();
        for(int i = 0; i<cursor.getCount(); i++){
            cursor.moveToNext();
            Message message = new Message();
            message.setMid(cursor.getString(0));
            message.setIda(cursor.getString(2));
            message.setIdb(cursor.getString(3));
            message.setName(cursor.getString(4));
            message.setMsg(cursor.getString(5));
            message.setCreated(cursor.getString(6));
            message.setRflag(cursor.getString(7));
            list.add(message);
        }
        cursor.close();
        db.close();
        return list;
    }

    // to retrieve chat history
    public ArrayList<ChatHistoryItem> getChatHistory(){
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tbm + " GROUP BY (" + rid + ") ORDER BY " + Tc + " DESC;", null);
        ArrayList<ChatHistoryItem> history = new ArrayList<>();
        for(int i = 0; i<cursor.getCount(); i++){
            cursor.moveToNext();
            ChatHistoryItem item = new ChatHistoryItem();
            item.setMid(cursor.getString(0));
            if(cursor.getString(cursor.getColumnIndex(idB)).equals(userA))
                item.setIdb(cursor.getString(cursor.getColumnIndex(idA)));
            else
                item.setIdb(cursor.getString(cursor.getColumnIndex(idB)));
            item.setName(cursor.getString(4));
            item.setMsg(cursor.getString(5));
            item.setCreated(cursor.getString(6));
            Cursor counter = db.rawQuery("SELECT COUNT(*) FROM " + tbm + " WHERE " + rflag + " = 'n' AND " + rid + " = ? ;", new String[]{cursor.getString(1)});
            counter.moveToFirst();
            item.setUnreads(counter.getInt(0));
            Log.d(logH, "History Item :: " + cursor.getString(3) + " : " + cursor.getString(4) + " : " + cursor.getString(5) + " count = " + counter.getInt(0));

            history.add(item);
        }
        cursor.close();
        db.close();
        Log.d(logH, "ChatHistory Fetched : count = " + history.size());

        return history;
    }

    public void markAsRead(String id){
        db = this.getWritableDatabase();
        int b = Integer.parseInt(id);
        String Rid = userAI>b? "" + b + userA : "" + userA + b;
        //ContentValues val = new ContentValues();
        //val.put(rflag, "y");
        //db.update(tbm,val,rid + " = ? ", new String[]{Rid});
        db.execSQL("UPDATE " + tbm + " SET " + rflag + " = 'y' WHERE " + rid + " = ? " + " AND " + rflag + " = 'n'", new String[]{Rid});
        db.close();
    }

    // to format timestamp data received from server to string
    public static String formatDateTime(Context context, String timeToFormat) {

        String finalDateTime = "";

        SimpleDateFormat iso8601Format = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");

        Date date = null;
        if (timeToFormat != null) {
            try {
                date = iso8601Format.parse(timeToFormat);
            } catch (ParseException e) {
                date = null;
            }

            if (date != null) {
                long when = date.getTime();
                int flags = 0;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_TIME;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_DATE;
                flags |= android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_YEAR;

                finalDateTime = android.text.format.DateUtils.formatDateTime(context,
                        when + TimeZone.getDefault().getOffset(when), flags);
            }
        }
        return finalDateTime;
    }

}
