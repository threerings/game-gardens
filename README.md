# Game Gardens

Game Gardens is a platform for hosting simple multiplayer games written in
Java. It is built atop the [Narya](http://github.com/threerings/narya/),
[Nenya](http://github.com/threerings/nenya/), and
[Vilya](http://github.com/threerings/vilya/) multiplayer game libraries.

This project contains the under-the-hood moving parts that make up the Game
Gardens hosting platform. If you are looking to develop a game that runs on
Game Gardens, you need only use the [Game Gardens
SDK](http://wiki.gamegardens.com/), and you need not build the SDK
from this code.

This project contains two major components:

## ToyBox

The ToyBox library coordinates things like pre-game configuration and
aggregates various services useful for making games. See the docs for
[ToyBoxContext](http://threerings.github.com/game-gardens/projects/toybox/docs/api/com/threerings/toybox/util/ToyBoxContext.html)
for a summary of the services.

## Gardens Webapp

This web application handles the uploading and management of games, as well as
browsing and launching said games (via Java Web Start).
