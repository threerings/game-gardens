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
    /**
     * A unique integer identifier for this game.
     */
    GAME_ID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,

    /** 
     * A string used to identify this game.
     */
    IDENT VARCHAR(255) NOT NULL,

    /**
     * The user id of the maintainer.
     */
    MAINTAINER_ID INTEGER UNSIGNED NOT NULL,

    /**
     * The status of this game (whether or not it should be published, if
     * it is flagged for review, etc.).
     */
    STATUS INTEGER UNSIGNED NOT NULL,

    /** 
     * The server on which this game is hosted.
     */
    HOST VARCHAR(255) NOT NULL,

    /** 
     * The current version's XML definition.
     */
    DEFINITION TEXT NOT NULL,

    /** 
     * The current test version's XML definition.
     */
    TEST_DEFINITION TEXT NOT NULL,

    /**
     * Define our table keys.
     */
    KEY (MAINTAINER_ID),
    PRIMARY KEY (GAME_ID)
);
