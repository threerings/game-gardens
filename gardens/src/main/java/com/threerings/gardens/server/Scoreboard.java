//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.server;

/** Provides a means to customize the scoreboard provided in the standard UI sidebar. */
public interface Scoreboard {

    /** Configures a logo to be displayed above the scoreboard.
     * @param imagePath the path (relative to your game's resource root) of the logo image.
     */
    void setLogo (String imagePath);

    /** Configures the icon for the specified player. This appears to the left of their name in the
     * scoreboard.
     * @param iconPath the path (relative to your game's resource root) of the icon image.
     */
    void setIcon (int playerIdx, String iconPath);

    /** Sets the stat value for the specified player and column to {@code data}. Stats are optional
     * tabular data displayed to the right of the player's names in the scoreboard. You can also
     * sort the scoreboard by a particular stat column.
     * @param data the data to display in the specified stat cell. This should not be more than
     * three or four characters wide or your scoreboard will probably look crappy.
     */
    void setStat (int playerIdx, int column, String data);

    /** Sets the icon to display above the specified stat column. This can provide some indication
     * to the player of the meaning of that stat column, and the {@code tooltip} (which is shown if
     * the player taps or hovers their mouse over the column) can provide further details.
     * @param iconPath the path (relative to your game's resource root) of the icon image.
     */
    void setStatHeader (int column, String iconPath, String tooltip);

    /** Causes the scoreboard to highlight the row of the specified player. This can be useful to
     * indicate that it is that player's turn, or at the end of the game to highlight that that
     * player was the winner.
     */
    void setHighlight (int playerIdx);

    /** Causes the scoreboard to sort the players based on the value of the specified stat column.
     * The order will automatically be updated when any stat data changes.
     * @param numeric if true, the data will be interpreted as numbers, if false it will be treated
     * lexically.
     * @param ascending if true the data will be sorted from lowest to highest (0 to n, or a to z),
     * if false it will be sorted from highest to lowest (n to 0, z to a).
     */
    void setSortColumn (int column, boolean numeric, boolean ascending);
}
