//
// Sagashi - A word finding game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sagashi/LICENSE

package com.samskivert.sagashi.data;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.RandomUtil;

import com.threerings.io.Streamable;

/**
 * Contains a 2D grid of letters and routines for searching it for words.
 */
public class SagashiBoard
    implements Streamable
{
    /** Indicates the number of letters we support. Currently only the 26
     * in the English alphabet. */
    public static final int LETTERS = 26;

    public SagashiBoard ()
    {
    }

    public SagashiBoard (int size, int[] frequencies)
    {
        _size = size;
        _letters = new int[size*size];

        // populate our board with random letters
        for (int ii = 0; ii < _letters.length; ii++) {
            _letters[ii] = RandomUtil.getWeightedIndex(frequencies);
        }
    }

    /** Indicates the size of one side of the board. */
    public int getSize ()
    {
        return _size;
    }

    /** Returns the letter code at the specified index. */
    public int getLetterCode (int index)
    {
        return _letters[index];
    }

    /**
     * Searches this board for the specified word, returns true if the
     * word is contained on the board (can be formed by following a path
     * starting at a letter, moving to adjacent letters and never looping
     * back upon itself), false if not.
     */
    public boolean containsWord (String word)
    {
        // no funny business
        if (word.length() == 0) {
            return false;
        }

        // search through the board for the starting letter
        ArrayIntSet path = new ArrayIntSet();
        for (int ii = 0; ii < _letters.length; ii++) {
            if (checkNode(word, 0, ii, path)) {
                return true;
            }
        }
        return false;
    }

    /** Helper function for {@link #containsWord}. */
    protected boolean checkNode (
        String word, int widx, int bidx, ArrayIntSet path)
    {
        // if the letter doesn't match or was already used, it's no good
        if (_letters[bidx] != getCode(word.charAt(widx)) ||
            path.contains(bidx)) {
            return false;
        }

        // if we hit the end of the word, we're done
        if (widx == word.length()-1) {
            return true;
        }

        // note that we're at this board position
        path.add(bidx);

        // now look for the next letter in the word among our neighbors
        int xx = bidx % _size, yy = bidx / _size;
        if (xx > 0) {
            if (yy > 0) {
                if (checkNode(word, widx+1, bidx-_size-1, path)) {
                    return true;
                }
            }
            if (yy < _size-1) {
                if (checkNode(word, widx+1, bidx+_size-1, path)) {
                    return true;
                }
            }
            if (checkNode(word, widx+1, bidx-1, path)) {
                return true;
            }
        }
        if (xx < _size-1) {
            if (yy > 0) {
                if (checkNode(word, widx+1, bidx-_size+1, path)) {
                    return true;
                }
            }
            if (yy < _size-1) {
                if (checkNode(word, widx+1, bidx+_size+1, path)) {
                    return true;
                }
            }
            if (checkNode(word, widx+1, bidx+1, path)) {
                return true;
            }
        }
        if (yy > 0) {
            if (checkNode(word, widx+1, bidx-_size, path)) {
                return true;
            }
        }
        if (yy < _size-1) {
            if (checkNode(word, widx+1, bidx+_size, path)) {
                return true;
            }
        }

        // we didn't find the word here, so remove this node from the path
        // and begone
        path.remove(bidx);

        return false;
    }

    /** Returns a string representation of the board. */
    @Override
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        for (int ii = 0; ii < _letters.length; ii++) {
            if (ii > 0 && ii % _size == 0) {
                buf.append("\n");
            }
            buf.append((char)('a' + _letters[ii])).append(" ");
        }
        return buf.toString();
    }

    /**
     * Returns a numeric code for this letter (0 for a and 25 for z) or -1
     * if the letter is not one of the 26 letters in the English alphabet.
     */
    public static int getCode (Character letter)
    {
        int code = Character.getNumericValue(letter);
        return (code >= 10 && code < 36) ? (code-10) : -1;
    }

    protected int _size;
    protected int[] _letters;
}
