#
# $Id$
#
# Proguard configuration file for Atlantis. Bundles all needed code into a
# single jar file.

-injars dist/just-atlantis.jar

# We need this because we use some features of the samskivert library that
# are not included by default in the Game Gardens client.
-injars ../lib/samskivert.jar(!**/velocity/**,!**/xml/**)

-libraryjars <java.home>/lib/rt.jar
-libraryjars ../client/gg-client.jar
-libraryjars ../lib/narya-parlor.jar(**/server/**)
-libraryjars ../lib/narya-distrib.jar(**/server/**)

# No need to complicate matters.
-dontobfuscate

-outjars dist/atlantis.jar

# We require the inclusion of anything our our packages. Anything those
# classes depend on (that are not already in one of the library jars) will
# automatically be included as well.
-keep public class com.samskivert.atlanti.** {
    public protected *;
}
