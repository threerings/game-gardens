//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2012 Three Rings Design, Inc., All Rights Reserved
// http://github.com/threerings/game-gardens
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

package com.threerings.toybox.data {

import com.threerings.util.ClassUtil;
import com.threerings.util.StreamableHashMap;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.crowd.client.PlaceController;

import com.threerings.parlor.game.client.GameConfigurator;
import com.threerings.parlor.game.data.GameConfig;

/**
 * Provides configuration to ToyBox games.
 */
public class ToyBoxGameConfig extends GameConfig
{
    /** Our configuration parameters. These will be seeded with the defaults from the game
     * definition and then configured by the player in the lobby. */
    public var params :StreamableHashMap = new StreamableHashMap(String);

    /** A zero argument constructor used when unserializing. */
    public function ToyBoxGameConfig ()
    {
    }

    /** Returns the game definition associated with this config instance. */
    public function getGameDefinition () :GameDefinition
    {
        return _gameDef;
    }

    // from GameConfig
    override public function getGameId () :int
    {
        return _gameId;
    }

    // from GameConfig
    override public function getGameIdent () :String
    {
        return _gameDef.ident;
    }

    // from GameConfig
    override public function getMatchType () :int
    {
        return _gameDef.match.getMatchType();
    }

    // from GameConfig
    override public function createConfigurator () :GameConfigurator
    {
        return null;
    }

    // from abstract PlaceConfig
    override public function getManagerClassName () :String
    {
        throw new Error("Not implemented.");
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        params = StreamableHashMap(ins.readObject());
        _gameId = ins.readInt();
        _gameDef = GameDefinition(ins.readObject());
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(params);
        out.writeInt(_gameId);
        out.writeObject(_gameDef);
    }

    // from PlaceConfig
    override public function createController () :PlaceController
    {
        // TODO: for this to really work with server side code, the choice of controller
        // will need to refer to the type of client (flash or thane) that we are running on
        var controller :String = getGameDefinition().controller;
        if (controller == null) {
            return createDefaultController();
        }
        var c :Class = ClassUtil.getClassByName(controller);
        return (new c() as PlaceController);
    }

    /**
     * Creates the controller to be used if the game definition does not specify a custom
     * controller. This is abstract since the controller must be specifically a thane
     * or flash controller.
     */
    protected /*abstract*/ function createDefaultController () :PlaceController
    {
        throw new Error("abstract");
    }

    /** Our game's unique id. */
    protected var _gameId :int;

    /** Our game definition. */
    protected var _gameDef :GameDefinition;
}
}
