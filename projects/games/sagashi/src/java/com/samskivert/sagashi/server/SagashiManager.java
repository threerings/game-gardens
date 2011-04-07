//
// Sagashi - A word finding game for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sagashi/LICENSE

package com.samskivert.sagashi.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.Interval;
import com.threerings.util.MessageBundle;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.parlor.game.server.GameManager;

import com.threerings.toybox.data.ToyBoxGameConfig;

import com.samskivert.sagashi.client.SagashiService;
import com.samskivert.sagashi.data.SagashiBoard;
import com.samskivert.sagashi.data.SagashiCodes;
import com.samskivert.sagashi.data.SagashiObject;
import com.samskivert.sagashi.data.SagashiScore;

import static com.samskivert.sagashi.Log.log;

/**
 * Handles the server side of our sagashi game.
 */
public class SagashiManager extends GameManager
    implements SagashiCodes, SagashiProvider
{
    // from interface SagashiProvider
    public void submitWord (ClientObject caller, String word, SagashiService.ResultListener rl)
        throws InvocationException
    {
        BodyObject user = (BodyObject)caller;

        // avoid funny business
        word = word.toLowerCase();

        // make sure the game is still in play
        if (!_sagaobj.isInPlay()) {
            throw new InvocationException("m.game_already_ended");
        }

        // make sure it's long enough
        if (word.length() < _minLength) {
            throw new InvocationException(
                MessageBundle.tcompose("m.not_long_enough", "" + _minLength));
        }

        // make sure it's on the board (the client should already have
        // done this but we like to prevent cheating)
        if (!_sagaobj.board.containsWord(word)) {
            throw new InvocationException(
                MessageBundle.tcompose("m.not_on_board", word));
        }

        // make sure it's a legal word
        if (!_words.contains(word)) {
            throw new InvocationException(
                MessageBundle.tcompose("m.invalid_word", word));
        }

        // determine who has played this word already
        int bonus = 0;
        ArrayIntSet players = _plays.get(word);
        if (players == null) {
            bonus = 1;
            _plays.put(word, players = new ArrayIntSet());
        }

        if (players.contains(user.getOid())) {
            throw new InvocationException(
                MessageBundle.tcompose("m.already_played", word));
        }
        players.add(user.getOid());

        // compute and record their score
        int score = word.length() - (_minLength-1) + bonus;

        SagashiScore srec = _scores.get(user.getOid());
        if (srec == null) {
            srec = new SagashiScore(user.getOid());
            srec.score = score;
            _scores.put(srec.userOid, srec);
        } else {
            srec.score += score;
        }

        rl.requestProcessed(score);
    }

    @Override
    protected PlaceObject createPlaceObject ()
    {
        return new SagashiObject();
    }

    @Override
    protected void didInit ()
    {
        super.didInit();

        // read info from our configuration
        ToyBoxGameConfig config = (ToyBoxGameConfig)_config;
        _minLength = (Integer)config.params.get("min_length");
        _duration = (Integer)config.params.get("duration");

        _ticker = _omgr.newInterval(new Runnable() {
            public void run () {
                tick();
            }
        });
    }

    @Override
    protected void didStartup ()
    {
        super.didStartup();

        // grab and set up our game object
        _sagaobj = (SagashiObject)_gameobj;
        _sagaobj.setService(_invmgr.registerDispatcher(new SagashiDispatcher(this)));
        _sagaobj.setScores(new SagashiScore[0]);

        // load up our word list if we haven't already
        if (_words.size() == 0) {
            loadWords();
        }

        // allow 10 seconds to elapse before we start
        _nextEvent = System.currentTimeMillis() + 10000L;
        _sagaobj.setSecondsUntil(10);

        // start up our ticker
        _ticker.schedule(3000L, true);
    }

    @Override
    protected void gameWillStart ()
    {
        super.gameWillStart();

        // clear things out
        _plays.clear();
        _scores.clear();
        _sagaobj.setScores(new SagashiScore[0]);

        // create a new board
        ToyBoxGameConfig config = (ToyBoxGameConfig)_config;
        int size = (Integer)config.params.get("board_size");
        _sagaobj.setBoard(new SagashiBoard(size, _frequency));
    }

    @Override
    protected void gameDidEnd ()
    {
        super.gameDidEnd();

        // fill in the list of all played words (sorted by longest)
        String[] allWords = new String[_plays.size()];
        _plays.keySet().toArray(allWords);
        Arrays.sort(allWords, new Comparator<String>() {
            public int compare (String one, String two) {
                int dl = (two.length() - one.length());
                return (dl == 0) ? one.compareTo(two) : dl;
            }
        });
        _sagaobj.setAllWords(allWords);

        // figure out who won
        StringBuffer buf = new StringBuffer();
        int high = 0;
        for (SagashiScore score : _sagaobj.scores) {
            OccupantInfo info = _sagaobj.occupantInfo.get(score.userOid);
            if (info == null) {
                continue;
            }
            if (score.score > high) {
                buf = new StringBuffer(info.username.toString());
                high = score.score;
            } else if (score.score == high) {
                buf.append(", ").append(info.username);
            }
        }

        // and report it to the room
        String msg;
        if (buf.length() == 0) {
            msg = "m.no_winners";
        } else {
            msg = buf.indexOf(", ") != -1 ? "m.winners" : "m.winner";
            msg = MessageBundle.tcompose(msg, buf);
        }
        systemMessage(SagashiCodes.SAGASHI_MSG_BUNDLE, msg);
    }

    @Override
    protected void didShutdown ()
    {
        super.didShutdown();

        // unregister our invocation dispatcher
        _invmgr.clearDispatcher(_sagaobj.service);

        // shutdown our ticker
        _ticker.cancel();
    }

    /** Called every three seconds. */
    protected void tick ()
    {
        long now = System.currentTimeMillis();
        int secondsUntil = (int)((_nextEvent - now) / 1000L);

        // if we're not yet at the end of the line, just update the time
        if (secondsUntil > 0) {
            try {
                _sagaobj.startTransaction();
                _sagaobj.setSecondsUntil(secondsUntil);
                if (_sagaobj.isInPlay()) {
                    updateScores();
                }
            } finally {
                _sagaobj.commitTransaction();
            }
            return;
        }

        // if the game is not yet in play, we start a new game N seconds
        // after the previous one ended
        if (!_sagaobj.isInPlay()) {
            _sagaobj.setSecondsUntil(_duration);
            startGame();
            _nextEvent = now + _duration * 1000L;

        } else {
            // update the scores a final time
            updateScores();
            endGame();
            _sagaobj.setSecondsUntil(REST_TIME);
            _nextEvent = now + REST_TIME * 1000L;
        }
    }

    /**
     * Updates and broadcasts our scores array.
     */
    protected void updateScores ()
    {
        SagashiScore[] scores = new SagashiScore[_scores.size()];
        _scores.values().toArray(scores);
        Arrays.sort(scores);
        _sagaobj.setScores(scores);
    }

    /** Loads up our words list. */
    protected void loadWords ()
    {
        try {
            InputStream win = getClass().getClassLoader().getResourceAsStream(WORDS_PATH);
            BufferedReader bin = new BufferedReader(new InputStreamReader(win));
            String word;
            while ((word = bin.readLine()) != null) {
                _words.add(word);
                // update our frequency distribution
                for (int ii = 0, ll = word.length(); ii < ll; ii++) {
                    int idx = SagashiBoard.getCode(word.charAt(ii));
                    if (idx != -1) {
                        _frequency[idx]++;
                    }
                }
            }
            win.close();
        } catch (IOException ioe) {
            log.log(Level.WARNING, "Failed to read words file " +
                    "[path=" + WORDS_PATH + "].", ioe);
        }
    }

    /** Ticks this manager every three seconds. */
    protected Interval _ticker;

    /** A casted reference to our game object. */
    protected SagashiObject _sagaobj;

    /** Various configuration parameters. */
    protected int _minLength, _duration;

    /** A time stamp used to keep track of things. */
    protected long _nextEvent;

    /** Contains a record for all words played in this game. */
    protected HashMap<String, ArrayIntSet> _plays = new HashMap<String, ArrayIntSet>();

    /** Contains score records for all participating players. */
    protected HashMap<Integer, SagashiScore> _scores = new HashMap<Integer, SagashiScore>();

    /** Contains the set of all valid words. */
    protected static HashSet<String> _words = new HashSet<String>();

    /** Yay for English! */
    protected static int[] _frequency = new int[SagashiBoard.LETTERS];

    /** The path to our words file. */
    protected static final String WORDS_PATH = "rsrc/data/words.txt";

    /** The number of seconds between rounds. */
    protected static final int REST_TIME = 60;
}
