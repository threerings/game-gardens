/**
 * $Id: genres.sql,v 1.1 2004/01/20 14:44:40 mdb Exp $
 *
 * Schema for the GENRE_MAP table.
 */

drop table if exists GENRE_MAP;

/**
 * Associates games with genres.
 */
CREATE TABLE GENRE_MAP
(
    /**
     * The unique identifier of the game.
     */
    GAME_ID INTEGER UNSIGNED NOT NULL,

    /**
     * The string identifier of the genre.
     */
    GENRE VARCHAR(32) NOT NULL,

    /**
     * Define our table keys.
     */
    PRIMARY KEY (GAME_ID, GENRE)
);
