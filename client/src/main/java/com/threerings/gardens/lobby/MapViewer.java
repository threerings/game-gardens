//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.lobby;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.nexus.distrib.DMap;

public abstract class MapViewer<K,V> {

    public final DMap.Listener<K,V> listener = new DMap.Listener<K,V>() {
        @Override public void onPut (K key, V value) {
            Widget view = createView(key, value);
            _widgets.put(key, view);
            _target.add(view); // TODO: sorted
        }

        @Override public void onRemove (K key) {
            Widget view = _widgets.remove(key);
            if (view != null) _target.remove(view);
        }
    };

    public MapViewer (FlowPanel target) {
        _target = target;
    }

    public void connect (DMap<K,V> map) {
        map.connect(listener);
        for (Map.Entry<K,V> entry : map.entrySet()) {
            listener.onPut(entry.getKey(), entry.getValue());
        }
    }

    protected abstract Widget createView (K key, V value);

    protected FlowPanel _target;
    protected Map<K,Widget> _widgets = new HashMap<K,Widget>();
}
