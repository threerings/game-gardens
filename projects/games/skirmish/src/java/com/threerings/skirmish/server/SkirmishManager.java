//
// $Id: SkirmishManager.java,v 1.21 2004/08/27 18:51:26 mdb Exp $

package com.threerings.skirmish.server;

import com.samskivert.util.IntervalManager;
import com.samskivert.util.StringUtil;
import com.threerings.util.MessageBundle;
import com.threerings.util.RandomUtil;

import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.server.util.SafeInterval;

import com.threerings.crowd.chat.server.SpeakProvider;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.PlaceManager;

import com.threerings.parlor.game.GameManager;

import com.threerings.skirmish.Log;
import com.threerings.skirmish.data.SkirmishAction;
import com.threerings.skirmish.data.SkirmishBoard;
import com.threerings.skirmish.data.SkirmishCodes;
import com.threerings.skirmish.data.SkirmishConfig;
import com.threerings.skirmish.data.SkirmishHand;
import com.threerings.skirmish.data.SkirmishObject;
import com.threerings.skirmish.data.SkirmishVessel;

/**
 * Handles the server side of a skirmish game.
 */
public class SkirmishManager extends GameManager
    implements SkirmishCodes
{
    // documentation inherited
    protected Class getPlaceObjectClass ()
    {
        return SkirmishObject.class;
    }

    // documentation inherited
    public void didInit ()
    {
        super.didInit();

        // register our message handlers
        registerMessageHandler(
            SET_HAND_REQUEST, new SetHandHandler());
    }

    // documentation inherited
    public void didStartup ()
    {
        super.didStartup();

        // grab our own casted game object reference
        _skobj = (SkirmishObject)_gameobj;
    }

    // documentation inherited
    protected void gameWillStart ()
    {
        super.gameWillStart();

        // get a casted reference to our game configuration
        _skonfig = (SkirmishConfig)_config;
        Log.info("Starting game " + _skonfig + ".");

        // generate the game board
        _skobj.setBoard(SkirmishBoard.generateBoard(
                            _skonfig.boardWidth, _skonfig.boardHeight,
                            _skonfig.featureDensity));

//         // pick a random 5x5 area in which to start the vessels
//         int dx = RandomUtil.getInt(_skonfig.boardWidth-5),
//             dy = RandomUtil.getInt(_skonfig.boardHeight-5);

        // start the vessels in the center of the "board"
        int dx = _skonfig.boardWidth/2-3,
            dy = _skonfig.boardHeight/2-3;

        // generate randomly positioned vessels and blank hands
        int pcount = getPlayerCount();
        SkirmishHand[] hands = new SkirmishHand[pcount];
        SkirmishVessel[] vessels = new SkirmishVessel[pcount];
        for (int ii = 0; ii < pcount; ii++) {
            // create a blank hand
            hands[ii] = new SkirmishHand(_skonfig.handSize);

            // and a randomly positioned vessel
            int col = dx + RandomUtil.getInt(6);
            int row = dy + RandomUtil.getInt(6);
            int orient = RandomUtil.getInt(4);
            vessels[ii] = new SkirmishVessel(col, row, orient);
        }
        _skobj.setHands(hands);
        _skobj.setVessels(vessels);

        // clear out the action cache
        _skobj.setActionCache(new int[4*pcount]);

        // reset the escape counter
        _skobj.setEscapeCounter(0);

        // reset the turn counter
        _turnCounter = 0;

        // choose a random attacker (this may not be a good way to pick
        // small random numbers, but we don't care for now)
        int attidx = RandomUtil.getInt(1000) % getPlayerCount();
        _skobj.setAttackerIndex(attidx);

        // set up the initial damage inidcators
        int[] damage = new int[pcount];
        if (_skonfig.handicap < 0) {
            damage[attidx] = -_skonfig.handicap;
        } else if (_skonfig.handicap > 0) {
            for (int ii = 0; ii < getPlayerCount(); ii++) {
                if (ii == attidx) {
                    continue;
                }
                damage[ii] = _skonfig.handicap;
            }
        }
        // clear out the damage indicators
        _skobj.setDamage(damage);

        // start up our turn execution timer
        SafeInterval eeint = new SafeInterval(CrowdServer.omgr) {
            public void run () {
                executeTurn();
            }
        };
        _eeid = IntervalManager.register(
            eeint, _skonfig.turnInterval * 1000L, null, true);

        // update the next turn timer
        _skobj.setNextTurn(System.currentTimeMillis() +
                           _skonfig.turnInterval * 1000L);
    }

    // documentation inherited
    protected void gameDidEnd ()
    {
        super.gameDidEnd();

        // shut down our hand execution timer
        if (_eeid != -1) {
            IntervalManager.remove(_eeid);
            _eeid = -1;
        }
    }

    /**
     * Called after the turn time has elapsed; executes a single turn.
     */
    protected void executeTurn ()
    {
        int hcount = _skobj.hands.length;

        // increment the escape counter
        _skobj.setEscapeCounter(_skobj.escapeCounter+1);

        // increment the turn counter
        _turnCounter++;

        // iterate through the hands held by each player, executing the
        // action in the appropriate slot and then effect any actions
        // caused by the vessels' board position
        for (int ii = 0; ii < _skonfig.handSize; ii++) {
            // we clone the vessels array so that we can safely update it
            // and broadcast the updates to the users (without overwriting
            // things next time through the loop)
            SkirmishVessel[] vessels = _skobj.cloneVessels();

            // let them know which hand position is being executed
            _skobj.setHandPos(ii);

            // first affect any movements due to the actions in the
            // vessels' hands
            for (int vv = 0; vv < hcount; vv++) {
                SkirmishAction action = _skobj.hands[vv].actions[ii];

                // clear out old fire data
                vessels[vv].shotOrient = -1;

                // if this is a NOOP or a FIRE, we can bail now; there's
                // no movement to be done
                if (action.code == SkirmishAction.NOOP_ACTION ||
                    action.code == SkirmishAction.FIRE_ACTION) {
                    continue;
                }

                // ensure that they actually have the necessary tokens to
                // apply this action
                int tidx = vv*4 + SkirmishAction.toIndex(action.code);
                if (_skobj.actionCache[tidx] == 0) {
                    Log.warning("Refusing to take action for which user " +
                                "has none [pidx=" + vv + ", hidx=" + ii +
                                ", atype=" + action.code +
                                ", hand=" + _skobj.hands[vv] + "].");
                    continue;
                }

                // update the vessel and decrement their action cache
                action.apply(vessels[vv]);
                _skobj.actionCache[tidx]--;

                // prevent them from going out of bounds
                keepInBounds(vessels[vv]);
            }
            _skobj.setVessels(vessels);

            // check to see if there are any vessel interceptions
            if (checkIntercept(vessels)) {
                return;
            }

            // apply any board effects (doing so in a separate update)
            vessels = _skobj.cloneVessels();
            boolean modified = false;
            for (int vv = 0; vv < hcount; vv++) {
                // update the vessel accordingly
                modified = (_skobj.board.apply(vessels[vv]) || modified);
                // prevent them from going out of bounds
                keepInBounds(vessels[vv]);
            }
            if (modified) {
                _skobj.setVessels(vessels);
            }

            // check to see if there are any vessel interceptions
            if (checkIntercept(vessels)) {
                return;
            }

            // finally fire the cannons
            vessels = _skobj.cloneVessels();
            boolean fired = false;
            for (int vv = 0; vv < hcount; vv++) {
                SkirmishAction action = _skobj.hands[vv].firings[ii];
                // if they're not shooting, skip it
                if (action.code != SkirmishAction.FIRE_ACTION) {
                    continue;
                }
                // ensure that they have the necessary tokens
                int tidx = vv*4 + SkirmishAction.toIndex(action.code);
                if (_skobj.actionCache[tidx] == 0) {
                    Log.warning("Refusing to fire for which user has no " +
                                "fire tokens [pidx=" + vv + ", hidx=" + ii +
                                ", hand=" + _skobj.hands[vv] + "].");
                    continue;
                }
                // fire the cannon and decrement their action cache
                fired = true;
                handleFire(vv, vessels);
                _skobj.actionCache[tidx]--;
            }
            if (fired) {
                _skobj.setVessels(vessels);
            }
        }

        // note that we're done executing hands
        _skobj.setHandPos(-1);

        // update the action cache levels
        _skobj.setActionCache(_skobj.actionCache);

        // clear out all of the hands and broadcast an update
        for (int vv = 0; vv < hcount; vv++) {
            _skobj.hands[vv].reset(_turnCounter);
        }
        _skobj.setHands(_skobj.hands);

        // if we've exceeded the requisite number of escape turns, end the
        // game and report that the target has escaped
        if (_skobj.escapeCounter >= _skonfig.escapeDuration) {
            String msg = MessageBundle.tcompose(
                "m.escaped", String.valueOf(_skonfig.escapeDuration));
            SpeakProvider.sendInfo(_skobj, SKIRMISH_MESSAGE_BUNDLE, msg);
            endGame();

        } else {
            // update the next turn timer
            _skobj.setNextTurn(System.currentTimeMillis() +
                               _skonfig.turnInterval * 1000L);
        }
    }

    /**
     * Checks to see if the supplied vessel has escaped the bounds of the
     * game board. If it has, it is moved back into the bounds of the
     * board.
     */
    protected void keepInBounds (SkirmishVessel vessel)
    {
        if (vessel.column < 0) {
            vessel.column = 0;
        }
        if (vessel.row < 0) {
            vessel.row = 0;
        }
        if (vessel.column >= _skonfig.boardWidth) {
            vessel.column = (byte)(_skonfig.boardWidth-1);
        }
        if (vessel.row >= _skonfig.boardHeight) {
            vessel.row = (byte)(_skonfig.boardHeight-1);
        }
    }

    /**
     * Determine whether or not any vessels occupy the same slots.
     */
    protected boolean checkIntercept (SkirmishVessel[] vessels)
    {
        // since there are so few vessels, we'll use the proverbial brute
        // force technique
        int vcount = vessels.length;
        for (int v1 = 0; v1 < vcount; v1++) {
            SkirmishVessel ves1 = vessels[v1];
            for (int v2 = v1+1; v2 < vcount; v2++) {
                SkirmishVessel ves2 = vessels[v2];
                // if they don't overlap, they're safe
                if (ves1.column != ves2.column || ves1.row != ves2.row) {
                    continue;
                }
                // otherwise we have an interception
                SpeakProvider.sendInfo(
                    _skobj, SKIRMISH_MESSAGE_BUNDLE, "m.intercepted");
                endGame();
                return true;
            }
        }
        return false;
    }

    /**
     * Called when a vessel fires its cannon.
     */
    protected void handleFire (int playerIdx, SkirmishVessel[] vessels)
    {
        // check to see if another vessel is within three squares of
        // either side of the firing vessel
        SkirmishVessel firer = vessels[playerIdx];
        int lvx = firer.column, rvx = firer.column,
            lvy = firer.row, rvy = firer.row;
        int lorient = (firer.orient + 3) % 4, rorient = (firer.orient + 1) % 4;
        int vcount = vessels.length;
        int target = -1;

      OUTER:
        for (int ii = 0; ii < CANNON_FIRE_DISTANCE; ii++) {
            lvx += DX[lorient]; lvy += DY[lorient];
            rvx += DX[rorient]; rvy += DY[rorient];
            for (int vv = 0; vv < vcount; vv++) {
                SkirmishVessel ves = vessels[vv];
                if (lvx == ves.column && lvy == ves.row) {
                    target = vv;
                    break OUTER;

                } else if (rvx == ves.column && rvy == ves.row) {
                    target = vv;
                    break OUTER;
                }
            }
        }

        // determine the orientation and distance of the shot
        int orient, dist;

        if (target != -1) {
            SkirmishVessel tvessel = vessels[target];
            orient = SkirmishVessel.cannonOrient(firer, tvessel);
            if (orient == NORTH || orient == SOUTH) {
                dist = Math.abs(tvessel.row - firer.row);
            } else {
                dist = Math.abs(tvessel.column - firer.column);
            }

            // increment this vessels damage
            _skobj.setDamageAt(_skobj.damage[target] + 1, target);

            // if the firer is the attacker, this resets the escape
            // counter
            if (playerIdx == _skobj.attackerIndex) {
                _skobj.setEscapeCounter(0);
            }

        } else {
            // pick someone to shoot at
            SkirmishVessel tvessel = null;
            for (int vv = 0; vv < vcount; vv++) {
                if (vv == playerIdx) {
                    continue;
                }
                tvessel = vessels[vv];
            }
            if (tvessel == null) {
                Log.warning("Couldn't find anyone to shoot at? " +
                            "[firer=" + playerIdx +
                            ", vessels=" + StringUtil.toString(vessels) + "].");
                return;
            }

            orient = SkirmishVessel.cannonOrient(firer, tvessel);
            dist = -1;
        }

        // configure the vessel's shot data
        firer.shotOrient = (byte)orient;
        firer.shotDist = (byte)dist;
    }

    /**
     * Returns the index of the player with the specified user object id
     * or -1 if no player exists with that user oid.
     */
    protected int getPlayerIndex (int playerOid)
    {
        for (int ii = 0; ii < _playerOids.length; ii++) {
            if (_playerOids[ii] == playerOid) {
                return ii;
            }
        }
        return -1;
    }

    /**
     * Called when a user wishes to update their current hand.
     */
    protected void handleSetHandRequest (MessageEvent event)
    {
        int pidx = getPlayerIndex(event.getSourceOid());
        if (pidx == -1) {
            Log.warning("Received set hand request from non-player? " +
                        "[event=" + event +
                        ", poids=" + StringUtil.toString(_playerOids) + "].");
            return;
        }

        SkirmishHand hand = (SkirmishHand)event.getArgs()[0];
        if (hand.turnCounter != _turnCounter) {
            Log.info("Ignoring belated hand submission [event=" + event + "].");
            return;
        } else {
            _skobj.setHandsAt(hand, pidx);
        }
    }

    /** Handles set hand requests. */
    protected class SetHandHandler implements MessageHandler
    {
        public void handleEvent (MessageEvent event, PlaceManager pmgr)
        {
            handleSetHandRequest(event);
        }
    }

    /** A casted reference to our game object. */
    protected SkirmishObject _skobj;

    /** Our game configuration object. */
    protected SkirmishConfig _skonfig;

    /** The interval id of our turn execution timer. */
    protected int _eeid = -1;

    /** A monotonically increasing turn counter. */
    protected int _turnCounter;
}
