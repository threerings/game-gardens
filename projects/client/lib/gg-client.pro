#
# $Id$
#
# Proguard configuration file for Game Gardens client

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

# we need whatever we keep of samskivert to be around in its entirety so
# that if a game uses the same classfile, the whole thing is there
-keep public class com.samskivert.Log {
    public protected *;
}
-keep public class com.samskivert.io.** {
    public protected *;
}
-keep public class com.samskivert.net.AttachableURLFactory {
    public protected *;
}
-keep public class com.samskivert.net.PathUtil {
    public protected *;
}
-keep public class com.samskivert.servlet.user.Password {
    public protected *;
}
-keep public class com.samskivert.servlet.user.UserUtil {
    public protected *;
}
-keep public class com.samskivert.swing.** {
    public protected *;
}
-keep public class com.samskivert.text.MessageUtil {
    public protected *;
}
-keep public class com.samskivert.util.** {
    public protected *;
}

# similarly for all of the narya libraries
-keep public class com.threerings.** {
    public protected *;
}
