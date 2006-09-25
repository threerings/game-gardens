//
// $Id$

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

    public boolean isLegalMove (ReversiObject.Piece piece)
    {
        // determine whether this piece "captures" pieces of the opposite color
        for (int ii = 0; ii < DX.length; ii++) {
            // look in this direction for captured pieces
            boolean sawOther = false, sawSelf = false;
            int x = piece.x, y = piece.y;
            for (int dd = 0; dd < _size; dd++) {
                x += DX[ii];
                y += DY[ii];

                // stop when we end up off the board
                if (x < 0 || x >= _size || y < 0 || y >= _size) {
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
                if (x < 0 || x >= _size || y < 0 || y >= _size) {
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

    protected int _size;
    protected int[] _state;

    protected static final int[] DX = { -1, 0, 1, -1, 1, -1, 0, 1 };
    protected static final int[] DY = { -1, -1, -1, 0, 0, 1, 1, 1 };
}
