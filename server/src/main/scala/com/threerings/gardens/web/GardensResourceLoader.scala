//
// Game Gardens - a platform for hosting simple multiplayer Java games
// Copyright (c) 2005-2013, Three Rings Design, Inc. - All rights reserved.
// https://github.com/threerings/game-gardens/blob/master/LICENSE

package com.threerings.gardens.web

import com.samskivert.velocity.ClasspathResourceLoader

class GardensResourceLoader extends ClasspathResourceLoader {

  // we need to eliminate the leading slash and prefix everything with web; easy!
  override protected def getResourceStream (loader :ClassLoader, name :String) =
    super.getResourceStream(loader, "web" + name)
}
