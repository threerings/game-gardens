//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/samskivert/game-gardens
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.reversi;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Performs some analysis of Reversi board state to determine move legality and
 * piece flipping.
 */
public class ReversiLogic
{
    public ReversiLogic (int size)
    {
        _size = size;
        _state = new int[size*size];
    }

    public void setState (Iterable<ReversiObject.Piece> pieces)
    {
        Arrays.fill(_state, -1);
        for (ReversiObject.Piece piece : pieces) {
            _state[piece.y * _size + piece.x] = piece.owner;
        }
    }

    /**
     * Returns the index into the {@link ReversiObject#players} array of the
     * player to whom control should transition.
     */
    public int getNextTurnHolderIndex (int curTurnIdx)
    {
        // if the next player can move, they're up
        if (hasLegalMoves(1-curTurnIdx)) {
            return 1-curTurnIdx;
        }

        // otherwise see if the current player can still move
        if (hasLegalMoves(curTurnIdx)) {
            return curTurnIdx;
        }

        // otherwise the game is over
        return -1;
    }

    /**
     * Returns true if the supplied piece represents a legal move for the
     * owning player.
     */
    public boolean isLegalMove (ReversiObject.Piece piece)
    {
        // disallow moves on out of bounds and already occupied spots
        if (!inBounds(piece.x, piece.y) || getColor(piece.x, piece.y) != -1) {
            return false;
        }

        // determine whether this piece "captures" pieces of the opposite color
        for (int ii = 0; ii < DX.length; ii++) {
            // look in this direction for captured pieces
            boolean sawOther = false, sawSelf = false;
            int x = piece.x, y = piece.y;
            for (int dd = 0; dd < _size; dd++) {
                x += DX[ii];
                y += DY[ii];

                // stop when we end up off the board
                if (!inBounds(x, y)) {
                    break;
                }

                int color = getColor(x, y);
                if (color == -1) {
                    break;
                } else if (color == 1-piece.owner) {
                    sawOther = true;
                } else if (color == piece.owner) {
                    sawSelf = true;
                    break;
                }
            }

            // if we saw at least one other piece and one of our own, we have a
            // legal move
            if (sawOther && sawSelf) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the player with the specified color has legal moves.
     */
    public boolean hasLegalMoves (int color)
    {
        // search every board position for a legal move
        ReversiObject.Piece piece = new ReversiObject.Piece();
        piece.owner = color;
        for (int yy = 0; yy < _size; yy++) {
            for (int xx = 0; xx < _size; xx++) {
                if (getColor(xx, yy) != -1) {
                    continue;
                }
                piece.x = xx;
                piece.y = yy;
                if (isLegalMove(piece)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines which pieces should be flipped based on the placement of the
     * specified piece onto the board. The pieces in question are changed to
     * the appropriate color and updated in the game object.
     */
    public void flipPieces (ReversiObject.Piece placed, ReversiObject gameobj)
    {
        ArrayList<ReversiObject.Piece> toflip =
            new ArrayList<ReversiObject.Piece>();

        // determine where this piece "captures" pieces of the opposite color
        for (int ii = 0; ii < DX.length; ii++) {
            // look in this direction for captured pieces
            int x = placed.x, y = placed.y;
            for (int dd = 0; dd < _size; dd++) {
                x += DX[ii];
                y += DY[ii];

                // stop when we end up off the board
                if (!inBounds(x, y)) {
                    break;
                }

                int color = getColor(x, y);
                if (color == -1) {
                    break;

                } else if (color == 1-placed.owner) {
                    // add the piece at this coordinates to our to flip list
                    for (ReversiObject.Piece piece : gameobj.pieces) {
                        if (piece.x == x && piece.y == y) {
                            toflip.add(piece);
                            break;
                        }
                    }

                } else if (color == placed.owner) {
                    // flip all the toflip pieces because we found our pair
                    for (ReversiObject.Piece piece : toflip) {
                        piece.owner = 1-piece.owner;
                        gameobj.updatePieces(piece);
                    }
                    break;
                }
            }
            toflip.clear();
        }
    }

    protected final int getColor (int x, int y)
    {
        return _state[y * _size + x];
    }

    protected final boolean inBounds (int x, int y)
    {
        return x >= 0 && y >= 0 && x < _size && y < _size;
    }

    protected int _size;
    protected int[] _state;

    protected static final int[] DX = { -1, 0, 1, -1, 1, -1, 0, 1 };
    protected static final int[] DY = { -1, -1, -1, 0, 0, 1, 1, 1 };
}
