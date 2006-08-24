//
// $Id: AtlantiManager.java,v 1.28 2004/12/15 00:13:25 mdb Exp $

package com.samskivert.atlanti.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.RandomUtil;
import com.samskivert.util.StringUtil;

import com.threerings.util.MessageBundle;

import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.DynamicListener;
import com.threerings.presents.dobj.MessageEvent;

import com.threerings.crowd.chat.server.SpeakProvider;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.game.data.GameAI;
import com.threerings.parlor.game.server.GameManager;
import com.threerings.parlor.turn.server.TurnGameManager;
import com.threerings.parlor.turn.server.TurnGameManagerDelegate;

import com.samskivert.atlanti.Log;
import com.samskivert.atlanti.data.AtlantiCodes;
import com.samskivert.atlanti.data.AtlantiObject;
import com.samskivert.atlanti.data.AtlantiTile;
import com.samskivert.atlanti.data.Feature;
import com.samskivert.atlanti.data.Piecen;
import com.samskivert.atlanti.data.TileCodes;
import com.samskivert.atlanti.util.FeatureUtil;
import com.samskivert.atlanti.util.TileUtil;

/**
 * The main coordinator of the Atlantissonne game on the server side.
 */
public class AtlantiManager extends GameManager
    implements TurnGameManager, AtlantiCodes
{
    public AtlantiManager ()
    {
        // we're a turn based game, so we use a turn game manager delegate
        addDelegate(_turndel = new TurnGameManagerDelegate(this) {
            protected void setNextTurnHolder () {
                // if we have tiles left, we move to the next player as normal
                if (_tilesInBox.size() > 0) {
                    super.setNextTurnHolder();
                } else {
                    // if we don't, we ensure that a new turn isn't started by
                    // setting _turnIdx to -1
                    _turnIdx = -1;
                }
            }
        });
    }

    /**
     * Called when the user requests to place the specified tile on the board.
     * This method is dynamically invoked when a {@link MessageEvent} is
     * generated by the client.
     */
    public void placeTile (BodyObject player, AtlantiTile tile)
    {
        Log.info("Got place tile request [who=" + player.who() +
            ", tile=" + tile + "].");

        int pidx = _turndel.getTurnHolderIndex();

        // make sure it's this player's turn
        if (_playerOids[pidx] != player.getOid()) {
            Log.warning("Requested to place tile by non-turn holder " +
                "[who=" + player.who() +
                ", turnHolder=" + _atlobj.turnHolder + "].");

        // make sure this is a valid placement
        } else if (TileUtil.isValidPlacement(_tiles, tile)) {
            placeTile(pidx, tile);

        } else {
            Log.warning("Received invalid placement " +
                "[who=" + player.who() + ", tile=" + tile + ".");
        }
    }

    /**
     * Called when the user requests to forgo their piecen placement for this
     * turn. This method is dynamically invoked when a {@link MessageEvent} is
     * generated by the client.
     */
    public void placeNothing (BodyObject player)
    {
        int pidx = _turndel.getTurnHolderIndex();
        if (_playerOids[pidx] != player.getOid()) {
            Log.warning("Requested to place nothing by non-turn holder " +
                "[who=" + player.who() +
                ", turnHolder=" + _atlobj.turnHolder + "].");

        } else {
            // player doesn't want to place anything, so we just end
            // the turn
            _turndel.endTurn();
        }
    }

    /**
     * Called when the user requests to place a piecen. This method is
     * dynamically invoked when a {@link MessageEvent} is generated by the
     * client.
     */
    public void placePiecen (BodyObject player, Piecen piecen)
    {
        AtlantiTile tile = (AtlantiTile)_atlobj.tiles.get(piecen.getKey());
        int pidx = _turndel.getTurnHolderIndex();
        int pcount = TileUtil.countPiecens(_atlobj.piecens, pidx);

        // make sure it's this player's turn
        if (_playerOids[pidx] != player.getOid()) {
            Log.warning("Requested to place piecen by non-turn holder " +
                "[who=" + player.who() +
                ", turnHolder=" + _atlobj.turnHolder + "].");

        // do some checking before we place the piecen
        } else if (pcount >= PIECENS_PER_PLAYER) {
            Log.warning("Requested to place piecen for player that " +
                "has all of their piecens in play " +
                "[who=" + player.who() + "].");

        } else if (tile == null) {
            Log.warning("Can't find tile for requested piecen " +
                "placement " + piecen + ".");

        } else if (tile.claims[piecen.featureIndex] != 0) {
            Log.warning("Requested to place piecen on claimed feature " +
                "[tile=" + tile + ", piecen=" + piecen + "].");

        } else {
            placePiecen(tile, piecen);
        }
    }

    /**
     * Called when an entry is added to {@link AtlantiObject#PIECENS}.
     */
    public void piecensAdded (Piecen piecen)
    {
        // we react to piecen additions by potentially scoring the placed
        // piecen. we allow the piecen to be added to the piecens set before
        // scoring so that the players can see the piecen pop up on their
        // screen and then disappear with a scoring notice rather than never
        // show up at all; plus it simplifies our code

        // make sure this is a valid placement
        AtlantiTile tile = _atlobj.tiles.get(piecen.getKey());
        if (tile == null) {
            Log.warning("Can't find tile for piecen scoring " +
                piecen + ".");

        } else {
            // check to see if we added the piecen to a completed
            // feature, in which case we score and remove it
            scoreFeatures(tile, getPiecens(), false);
        }

        // now that we've scored the piecen, we can end the turn
        _turndel.endTurn();
    }

    // from interface TurnGameManager
    public void turnWillStart ()
    {
        AtlantiTile ntile = null;
        for (int ii = 0; ii < _tilesInBox.size(); ii++) {
            AtlantiTile tile = _tilesInBox.get(ii);
            if (enumerateLegalMoves(tile).size() > 0) {
                ntile = tile;
                _tilesInBox.remove(ii);
                break;
            }
        }

        if (ntile == null) {
            SpeakProvider.sendInfo(
                _atlobj, ATLANTI_MESSAGE_BUNDLE, "m.no_legal_moves");
            endGame();
        } else {
            ntile.x = 0;
            ntile.y = 0;
            _atlobj.setCurrentTile(ntile);
        }
    }

    // from interface TurnGameManager
    public void turnDidStart ()
    {
        // if there's no AI in this slot, there's nothing to do here
        int pidx = _turndel.getTurnHolderIndex();
        GameAI ai = (_AIs == null) ? null : _AIs[pidx];
        if (ai == null) {
            return;
        }

        // enumerate the legal moves
        AtlantiTile tile = _atlobj.currentTile.clone();
        ArrayList<AtlantiTile> moves = enumerateLegalMoves(tile);
        if (moves.size() == 0) {
            Log.warning("Ack! No legal moves!");
            _turndel.endTurn();
            return;
        }

        // select a random position for our tile and place it
        tile = (AtlantiTile)RandomUtil.pickRandom(moves);
        if (placeTile(pidx, tile)) {
            return;
        }

        // if placeTile() did not return true, we have piecens remaining and
        // the tile can be placed upon; however we randomly choose not to place
        // a piecen 45% of the time
        if (RandomUtil.getInt(100) > 54) {
            // just end our turn
            _turndel.endTurn();
            return;
        }

        // place a piecen on the piece we just placed
        int skip = RandomUtil.getInt(tile.getUnclaimedCount());
        for (int ii = 0; ii < tile.claims.length; ii++) {
            if (tile.claims[ii] == 0) {
                if (skip == 0) {
                    Piecen p = new Piecen();
                    p.owner = pidx;
                    p.x = tile.x;
                    p.y = tile.y;
                    p.featureIndex = ii;
                    placePiecen(tile, p);
                    break;
                } else {
                    skip--;
                }
            }
        }
    }

    // from interface TurnGameManager
    public void turnDidEnd ()
    {
        // if there are no tiles left, we end the game
        if (_tilesInBox.size() == 0) {
            endGame();
        }
    }

    @Override // from PlaceManager
    protected PlaceObject createPlaceObject ()
    {
        return new AtlantiObject();
    }

    @Override // from GameManager
    protected void didStartup ()
    {
        super.didStartup();

        // grab our own casted game object reference
        _atlobj = (AtlantiObject)_gameobj;

        // dynamically dispatch events to methods
        _atlobj.addListener(new DynamicListener(this));
    }

    @Override // from GameManager
    protected void gameWillStart ()
    {
        super.gameWillStart();

        // generate a shuffled tile list
        _tilesInBox = TileUtil.getStandardTileSet();
        Collections.shuffle(_tilesInBox);

        // clear out our board tiles
        _tiles.clear();

        // create a claim group vector
        _claimGroupVector = new int[getPlayerCount()];

        // clear out the scores
        _atlobj.setScores(new int[getPlayerCount()]);

        // clear out the tile and piecen set and add the starting tile; we
        // can't do this in a separate setTiles() then addToTiles() call
        // because that the tile will be added immediately to the DSet
        // before the setTiles() event is processed, thus the added tile
        // will already be in the set when the time comes to process the
        // addToTiles() event
        AtlantiTile start = TileUtil.getStartingTile();
        _atlobj.setTiles(new DSet<AtlantiTile>(new AtlantiTile[] { start }));
        _atlobj.setPiecens(new DSet<Piecen>());
        _tiles.add(start);
    }

    @Override // from GameManager
    protected void gameDidEnd ()
    {
        super.gameDidEnd();
        
        // compute the final scores by iterating over each tile and
        // scoring its features
        Piecen[] piecens = getPiecens();
        Iterator iter = _atlobj.tiles.iterator();
        while (iter.hasNext()) {
            AtlantiTile tile = (AtlantiTile)iter.next();
            scoreFeatures(tile, piecens, true);
        }

        // lastly, we have to score the farms (cue the ominous drums)...
        scoreFarms();

        // update the final scores
        _atlobj.setScores(_atlobj.scores);
    }

    /** Enumerates the legal moves for the specified piece. */
    protected ArrayList<AtlantiTile> enumerateLegalMoves (AtlantiTile tile)
    {
        // determine the extent of the board
        int minx = 0, miny = 0, maxx = 0, maxy = 0;
        for (Iterator iter = _atlobj.tiles.iterator(); iter.hasNext(); ) {
            AtlantiTile ptile = (AtlantiTile)iter.next();
            if (ptile.x < minx) {
                minx = ptile.x;
            } else if (ptile.x > maxx) {
                maxx = ptile.x;
            }
            if (ptile.y < miny) {
                miny = ptile.y;
            } else if (ptile.y > maxy) {
                maxy = ptile.y;
            }
        }

        // enumerate all legal moves
        ArrayList<AtlantiTile> moves = new ArrayList<AtlantiTile>();
        for (tile.y = miny - 1; tile.y <= maxy+1; tile.y++) {
            for (tile.x = minx - 1; tile.x <= maxx+1; tile.x++) {
                // we can't place on top of existing tiles
                if (_atlobj.tiles.containsKey(tile.getKey())) {
                    continue;
                }

                // check to see whether any orientation of this piece at
                // these coordinates is a legal move
                boolean[] orients = TileUtil.computeValidOrients(_tiles, tile);
                for (int oo = 0; oo < orients.length; oo++) {
                    if (orients[oo]) {
                        tile.orientation = oo;
                        moves.add(tile.clone());
                    }
                }
            }
        }

        return moves;
    }

    /**
     * Creates an array of piecens based on the contents of the piecens
     * set in the game object, suitable for passing to {@link
     * #scoreFeatures}.
     */
    protected Piecen[] getPiecens ()
    {
        // create a piecen array that we can manipulate while scoring
        Piecen[] piecens = new Piecen[_atlobj.piecens.size()];
        Iterator iter = _atlobj.piecens.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            piecens[i] = (Piecen)iter.next();
        }
        return piecens;
    }

    /**
     * Scores the features on this tile.
     *
     * @param tile the tile whose features should be scored.
     * @param piecens an array of the pieces on the board which we can
     * manipulate directly without having to wait for entry removed
     * events to be dispatched.
     * @param finalTally during the final tally, we score differently and
     * we don't remove piecens from the board as we score them.
     */
    protected void scoreFeatures (
        AtlantiTile tile, Piecen[] piecens, boolean finalTally)
    {
        // potentially score all features on the tile
        for (int i = 0; i < tile.features.length; i++) {
            // we only need to worry about ROAD and CITY features because
            // those are the only features on this tile that we might have
            // completed
            Feature f = tile.features[i];
            if (f.type != TileCodes.CITY && f.type != TileCodes.ROAD) {
                continue;
            }

            // see if any piecens are even on a feature in this group
            int cgroup = tile.claims[i];
            int[] cgv = getClaimGroupVector(cgroup, piecens);
            if (cgv == null) {
                // if not, we don't have anything to score
                Log.debug("Not scoring unclaimed feature " +
                          "[ttype=" + tile.type + ", feat=" + f +
                          ", cgroup=" + cgroup + "].");
                continue;
            }

            // we do have something to score, so we compute the score for
            // this feature
            int score = TileUtil.computeFeatureScore(_tiles, tile, i);

            // if the score is positive, it's a completed feature and we
            // score it regardless, we score incomplete features only
            // during the final tally
            if (score > 0 || finalTally) {
                String qual = (score > 0) ? "m.completed" : "m.incomplete";

                // convert the score into a positive value
                score = Math.abs(score);

                // adjust and report the scores
                StringBuffer names = new StringBuffer();
                for (int p = 0; p < cgv.length; p++) {
                    // adjust the score
                    _atlobj.scores[p] += (score * cgv[p]);

                    // append the scorers name to the list
                    if (cgv[p] > 0) {
                        if (names.length() > 0) {
                            names.append(", ");
                        }
                        names.append(getPlayerName(p));
                    }
                }

                String msg = MessageBundle.compose(
                    qual, "m." + TileCodes.FEATURE_NAMES[f.type]);
                msg = MessageBundle.compose(
                    "m.scored", msg, MessageBundle.taint(String.valueOf(score)),
                    MessageBundle.taint(names));
                SpeakProvider.sendInfo(
                    _atlobj, ATLANTI_MESSAGE_BUNDLE, msg);

                Log.debug("New scores: " + StringUtil.toString(_atlobj.scores));

                // broadcast the new scores if this isn't the final tally
                if (!finalTally) {
                    _atlobj.setScores(_atlobj.scores);
                }

                // and free up the scored piecens
                removePiecens(cgroup, piecens, finalTally);

            } else {
                Log.debug("Not scoring incomplete feature " +
                          "[ttype=" + tile.type + ", feat=" + f +
                          ", score=" + score + "].");
            }
        }

        // we also may have completed a cloister, so we check that as well
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                // find our neighbor and make sure they exist
                AtlantiTile neighbor =
                    TileUtil.findTile(_tiles, tile.x + dx, tile.y + dy);
                if (neighbor == null) {
                    continue;
                }

                // scan their features arrays for claimed cloisters
                for (int i = 0; i < neighbor.features.length; i++) {
                    Feature f = neighbor.features[i];
                    Piecen p = neighbor.piecen;

                    // is a cloister
                    if (f.type != TileCodes.CLOISTER) {
                        continue;
                    }

                    // tile has a piecen
                    if (p == null) {
                        Log.debug("Skipping non-piecen having " +
                                  "cloister tile [tile=" + neighbor +
                                  ", feat=" + f + "].");
                        continue;
                    }

                    // piecen is on cloister feature
                    if (neighbor.claims[i] != p.claimGroup) {
                        Log.debug("Skipping cloister tile with piecen on " +
                                  "non-cloister [tile=" + neighbor +
                                  ", feat=" + f + "].");
                        continue;
                    }

                    // score the cloister
                    int score = TileUtil.computeFeatureScore(
                        _tiles, neighbor, i);

                    // if it's completed or if we're doing the final
                    // tally, we score it
                    if (score > 0 || finalTally) {
                        String qual = (score > 0) ?
                            "m.completed" : "m.incomplete";

                        // coerce the score into positive land
                        score = Math.abs(score);

                        // deliver a chat notification to tell the
                        // players about the score
                        String msg = MessageBundle.compose(qual, "m.temple");
                        msg = MessageBundle.compose(
                            "m.scored", msg,
                            MessageBundle.taint(String.valueOf(score)),
                            MessageBundle.taint(getPlayerName(p.owner)));
                        SpeakProvider.sendInfo(
                            _atlobj, ATLANTI_MESSAGE_BUNDLE, msg);

                        // add the score to the owning player
                        _atlobj.scores[p.owner] += score;

                        // only broadcast the updated scores if this isn't
                        // the final tally
                        if (!finalTally) {
                            _atlobj.setScores(_atlobj.scores);
                        }

                        // and clear out the piecen (only removing it from
                        // the piecen set if we're not in the final tally)
                        removePiecen(p, !finalTally);
                    }
                }
            }
        }
    }

    /**
     * Scores the farms, which is the final act of scoring.
     */
    protected void scoreFarms ()
    {
        HashIntMap<int[]> cities = new HashIntMap<int[]>();
        int[] cityScores = new int[getPlayerCount()];

        // clear out the claims for incompleted cities and claim unclaimed
        // completed cities
        TileUtil.prepCitiesForScoring(_tiles);

        // do the big process-ola
        int tsize = _tiles.size();
        for (int i = 0; i < tsize; i++) {
            AtlantiTile tile = _tiles.get(i);

            // iterate over all of the city features in this tile
            for (int f = 0; f < tile.features.length; f++) {
                // get the claim group for this feature
                int cityClaim = tile.claims[f];

                // skip unclaimed and non-city features
                if (tile.features[f].type != TileCodes.CITY ||
                    cityClaim == 0) {
                    continue;
                }

                // get the list associated with this claim group
                int[] claims = cities.get(cityClaim);
                if (claims == null) {
                    // create a claim vector if we've not got one.  if a
                    // city had 35 separately claimed farms around it, all
                    // the piecens in the game would be in play and the
                    // city would not have been claimed which would be an
                    // extremely pathological case, but we love pathology
                    // (especially when we don't have a resizable int list
                    // class handy)
                    claims = new int[35];
                    cities.put(cityClaim, claims);
                }

                // iterate over all of the grass features that are
                // connected to city features on this tile and add their
                // claim groups the list for this city feature
                int[] grasses = FeatureUtil.CITY_GRASS_MAP[tile.type-1];
                for (int g = 0; g < grasses.length; g++) {
                    int farmClaim = tile.claims[grasses[g]];

                    // only worry about claimed grass regions
                    if (farmClaim == 0) {
                        Log.debug("Ignoring unclaimed farm group " +
                                  "[tile=" + tile +
                                  ", fidx=" + grasses[g] + "].");
                        continue;
                    }

                    // and the farm claim group to the list
                    for (int c = 0; c < claims.length; c++) {
                        // don't add the farm claim twice
                        if (claims[c] == farmClaim) {
                            break;
                        } else if (claims[c] == 0) {
                            claims[c] = farmClaim;
                            Log.debug("Noting city/farm abuttal " +
                                      "[tile=" + tile +
                                      ", cityClaim=" + cityClaim +
                                      ", farmClaim=" + farmClaim + "].");
                            break;
                        }
                    }
                }
            }
        }

        // now for each city, we look to see who has the most piecens that
        // are connected to the city by farms
        Iterator iter = cities.keys();
        while (iter.hasNext()) {
            int cityClaim = ((Integer)iter.next()).intValue();
            int[] farmClaims = (int[])cities.get(cityClaim);
            int[] pcount = new int[getPlayerCount()];
            int max = 0;

            Iterator piter = _atlobj.piecens.iterator();
            while (piter.hasNext()) {
                Piecen p = (Piecen)piter.next();
                // see if the piecen is on any of the farms
                for (int c = 0; c < farmClaims.length; c++) {
                    if (p.claimGroup == farmClaims[c]) {
                        Log.debug("Counting piecen [cityClaim=" + cityClaim +
                                  ", farmClaim=" + farmClaims[c] +
                                  ", piecen=" + p + "].");
                        // increment their count and track the max
                        if (max < ++pcount[p.owner]) {
                            max = pcount[p.owner];
                        }
                    }
                }
            }

            Log.debug("Counted city [cityClaim=" + cityClaim +
                      ", counts=" + StringUtil.toString(pcount) + "].");

            // ignore this city if no one has any farmers nearby
            if (max == 0) {
                continue;
            }

            // now score four points for every player that has the max
            for (int i = 0; i < pcount.length; i++) {
                if (pcount[i] == max) {
                    Log.debug("Scoring city for player [cgroup=" + cityClaim +
                              ", player=" + getPlayerName(i) +
                              ", pcount=" + pcount[i] + "].");
                    cityScores[i] += 4;
                }
            }
        }

        // now report the scoring and transfer the counts to the score
        // array
        for (int i = 0; i < getPlayerCount(); i++) {
            if (cityScores[i] > 0) {
                _atlobj.scores[i] += cityScores[i];
                String msg = MessageBundle.tcompose(
                    "m.scored_fisheries", getPlayerName(i),
                    String.valueOf(cityScores[i]));
                SpeakProvider.sendInfo(
                    _atlobj, ATLANTI_MESSAGE_BUNDLE, msg);
            }
        }
    }

    /**
     * Returns an int array with zeros and ones in the appropriate places
     * so that a score can be multiplied by a player's position in the
     * vector to determine whether or not they receive any points for the
     * scoring of a particular claim group.
     *
     * <p> Note that this function returns a static (to this instance)
     * vector, so it cannot be called again without overwriting values
     * returned previously.
     *
     * @param claimGroup the claim group that we're scoring.
     * @param piecens an array to use when looking for matching piecens.
     *
     * @return an array for the specified claim group or null if no
     * players have a piecen claiming the specified claim group.
     */
    protected int[] getClaimGroupVector (int claimGroup, Piecen[] piecens)
    {
        // clear out the vector
        Arrays.fill(_claimGroupVector, 0);

        // iterate over the piecens
        int max = 0;
        for (int i = 0; i < piecens.length; i++) {
            Piecen piecen = piecens[i];
            if (piecen == null) {
                continue;
            } else if (piecen.claimGroup == claimGroup) {
                // color == player index... somewhat sketchy
                if (++_claimGroupVector[piecen.owner] > max) {
                    // keep track of the highest scorer
                    max = _claimGroupVector[piecen.owner];
                }
            }
        }

        // now cut out everyone with scores less than the highest score
        for (int i = 0; i < _claimGroupVector.length; i++) {
            _claimGroupVector[i] = (_claimGroupVector[i] < max) ? 0 : 1;
        }

        return (max == 0) ? null : _claimGroupVector;
    }

    /**
     * Removes piecens either from the supplied array or from the game
     * object piecen set if no array is supplied.
     */
    protected void removePiecens (int claimGroup, Piecen[] piecens,
                                  boolean finalTally)
    {
        // we always clear the piecens from the array
        for (int i = 0; i < piecens.length; i++) {
            if (piecens[i] == null) {
                continue;
            } else if (piecens[i].claimGroup == claimGroup) {
                piecens[i] = null;
            }
        }

        // if this isn't the final tally, we also clear 'em from the board
        if (!finalTally) {
            Object[] pvec = _atlobj.piecens.toArray(null);
            for (int ii = 0; ii < pvec.length; ii++) {
                Piecen p = (Piecen)pvec[ii];
                if (p.claimGroup == claimGroup) {
                    removePiecen(p, true);
                }
            }
        }
    }

    /**
     * Removes the piecen from the board and optionally removes it from
     * the piecen set.
     *
     * @param piecen the piecen to be removed.
     * @param removeFromPiecens if true, the piecen will also be removed
     * from the piecens set in the game object.
     */
    protected void removePiecen (Piecen piecen, boolean removeFromPiecens)
    {
        // locate the tile that contains this piecen
        int tidx = _tiles.indexOf(new AtlantiTile(piecen.x, piecen.y));
        if (tidx == -1) {
            Log.warning("Requested to remove piecen that is not " +
                        "associated with any tile [piecen=" + piecen + "].");
        } else {
            AtlantiTile tile = _tiles.get(tidx);
            // and clear the piecen
            tile.clearPiecen();
        }

        // also remove from the piecens dset if requested
        if (removeFromPiecens) {
            _atlobj.removeFromPiecens(piecen.getKey());
        }
    }

    /**
     * Effects a tile placement. The caller should ensure that it is a valid
     * placement before calling this method.
     *
     * @return true if we automatically ended the players turn due to a lack of
     * piecen placement opportunities, false if not.
     */
    protected boolean placeTile (int pidx, AtlantiTile tile)
    {
        // count their available piecens before placing this one because
        // returned piecens are not usable on this turn
        int pcount = TileUtil.countPiecens(_atlobj.piecens, pidx);

        // add the tile to the list and resort it
        _tiles.add(tile);
        Collections.sort(_tiles);

        // inherit its claim groups
        TileUtil.inheritClaims(_tiles, tile);

        // add the tile to the tiles set
        _atlobj.addToTiles(tile);

        // placing a piece may have completed road or city features; if it
        // did, we score them now
        scoreFeatures(tile, getPiecens(), false);

        Log.debug("Placed tile " + tile + ".");

        // if the player has no free piecens or if there are no unclaimed
        // features on this tile, we end their turn straight away
        if (pcount >= PIECENS_PER_PLAYER || !tile.hasUnclaimedFeature()) {
            _turndel.endTurn();
            return true;
        }

        return false;
    }        

    /**
     * Effects a piecen placement. The caller should ensure that it is a
     * valid placement before calling this method.
     */
    protected void placePiecen (AtlantiTile tile, Piecen piecen)
    {
        // stick the piece in the tile to update the claim groups
        tile.setPiecen(piecen, _tiles);

        // and add the piecen to the game object. when we receive
        // the piecen added event, we'll score it and then end the
        // turn
        _atlobj.addToPiecens(piecen);
    }

    /** Our turn game delegate. */
    protected TurnGameManagerDelegate _turndel;

    /** A casted reference to our Atlanti game object. */
    protected AtlantiObject _atlobj;

    /** The (shuffled) list of tiles remaining to be played in this
     * game. */
    protected List<AtlantiTile> _tilesInBox;

    /** A sorted list of the tiles that have been placed on the board. */
    protected List<AtlantiTile> _tiles = new ArrayList<AtlantiTile>();

    /** Used to score features groups. */
    protected int[] _claimGroupVector;
}
