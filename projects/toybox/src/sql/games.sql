/**
 * $Id$
 *
 * Schema for the GAMES table.
 */

drop table if exists GAMES;

/**
 * Every game published by the system 
 */
CREATE TABLE GAMES
(
    /** A unique integer identifier for this game. */
    GAME_ID INTEGER NOT NULL AUTO_INCREMENT,

    /** The category of this game (ie. strategy, word, classic, etc.). */
    CATEGORY VARCHAR(255) NOT NULL,

    /** The human readable name of this game. */
    NAME VARCHAR(255) NOT NULL,

    /** The user id of the maintainer. */
    MAINTAINER_ID INTEGER NOT NULL,

    /** The status of this game (whether or not it should be published, if
     * it is flagged for review, etc.). */
    STATUS VARCHAR(255) NOT NULL,

    /** The server on which this game is hosted. */
    HOST VARCHAR(255) NOT NULL,

    /** The current version's XML definition. */
    DEFINITION TEXT NOT NULL,

    /** The MD5 digest of the game jar file. */
    DIGEST VARCHAR(255) NOT NULL,

    /** A short description of the game. */
    DESCRIPTION TEXT NOT NULL,

    /** Brief instructions on how to play the game. */
    INSTRUCTIONS TEXT NOT NULL,

    /** The last update time of the game's jar file. */
    LAST_UPDATED DATETIME NOT NULL,

    /**
     * Define our table keys.
     */
    KEY (MAINTAINER_ID),
    PRIMARY KEY (GAME_ID)
);
