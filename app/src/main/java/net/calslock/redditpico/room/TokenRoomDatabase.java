package net.calslock.redditpico.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {TokenEntity.class}, version = 1, exportSchema = false)
public abstract class TokenRoomDatabase extends RoomDatabase {
    private static volatile TokenRoomDatabase INSTANCE;
    static public TokenRoomDatabase getDatabase(final Context context){
        if (INSTANCE == null){
            synchronized (TokenRoomDatabase.class){
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TokenRoomDatabase.class, "tokenDB").build();
                }
            }
        }
        return INSTANCE;
    }
    public abstract TokenDao tokenDao();
}
