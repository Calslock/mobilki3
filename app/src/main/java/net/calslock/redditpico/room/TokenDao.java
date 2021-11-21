package net.calslock.redditpico.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TokenDao {
    //Insert token with ID to DB
    @Insert
    void insert(TokenEntity tokenEntity);

    //Clear DB
    @Query("DELETE from tokens")
    void delete();

    //Get token from DB given ID
    @Query("SELECT * FROM tokens WHERE id=(:id)")
    TokenEntity getToken(int id);

    @Query("SELECT * FROM tokens")
    List<TokenEntity> getAllTokens(int id);
}
