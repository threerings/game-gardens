//
// Reversi - A reversi implementation for the Game Gardens platform
// http://github.com/threerings/game-gardens/blob/master/projects/games/sreversi/LICENSE

package com.samskivert.reversi

import java.lang.reflect.{Method, Modifier}

import com.threerings.presents.dobj.DObject
import com.threerings.presents.dobj.Accessor
  
/**
 * Customizes a DObject for use with Scala, by overriding {@link DObject#getAccessors} to return
 * accessors that operate using Scala's getter/setter methods as appropriate.
 */
trait ScalaDObject { this :DObject =>
  override protected def createAccessors :Array[Accessor] = {
    val acc = collection.mutable.ArrayBuffer[Accessor]()
    val clazz = getClass
    // we may have inherited some public fields from java
    acc ++= clazz.getFields.filter(f => !Modifier.isStatic(f.getModifiers)).map(
      f => new Accessor.ByField(f))
    // now look for Scala setters and extrapolate our getters from there
    acc ++= getClass.getMethods.filter(m => m.getName.endsWith("_$eq")).map(
      m => new ScalaAccessor(clazz.getMethod(m.getName.dropRight(4)), m))
    println("Accessors! " + acc.map(_.name))
    acc.toArray
  }
}

class ScalaAccessor (getter :Method, setter :Method) extends Accessor(getter.getName) {
  def get (obj :DObject) = getter.invoke(obj)
  def set (obj :DObject, value :Object) = setter.invoke(obj, value)
}
