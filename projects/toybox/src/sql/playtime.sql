/**
 * $Id: games.sql 88 2005-01-14 08:11:57Z mdb $
 *
 * Schema for the PLAYS table.
 */

drop table if exists PLAYTIME;

/**
 * Used to record how much (in minutes played) a particular game was
 * played in a particular time period.
 */
CREATE TABLE PLAYTIME
(
    /** A unique integer identifier for this game. */
    GAME_ID INTEGER NOT NULL,

    /** The start of the time period in question. */
    PERIOD DATE NOT NULL,

    /** The number of minutes this game has been played in the week
     * represented by this period. */
    PLAYTIME INTEGER NOT NULL,

    /** Define our table keys. */
    PRIMARY KEY (GAME_ID, PERIOD)
);
