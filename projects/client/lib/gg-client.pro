-injars ../../lib/commons-io.jar
-injars ../../lib/getdown.jar(!**/tools/**)
-injars ../../lib/narya-base.jar(!**/tools/**,!**/server/**)
-injars ../../lib/narya-distrib.jar(!**/tools/**,!**/server/**)
-injars ../../lib/narya-media.jar(!**/tools/**,!**/server/**)
-injars ../../lib/narya-parlor.jar(!**/tools/**,!**/server/**)
-injars ../../lib/samskivert.jar(!**/velocity/**,!**/xml/**)
-injars ../../lib/toybox.jar(!**/tools/**,!**/server/**,!**/xml/**)

-libraryjars <java.home>/lib/rt.jar

-dontobfuscate

-outjars gg-client.jar

-keep public class com.threerings.** {
    public protected *;
}
