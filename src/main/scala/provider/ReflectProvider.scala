package com.limitra.sdk.core.provider

import java.lang.reflect.{Field, ParameterizedType}

import scala.reflect.ClassTag

sealed class ReflectProvider {
  /**
    * Converts a first specified type of object to the second specified type and returns the new instance as a result.
    * Example :
    * val aObj: A = new A()
    * val bObj: B = Reflect.FromTo[A, B](aObj)
    */
  def FromTo[C](obj: Any)(implicit tag: ClassTag[C]): Option[C] = {
    _history = Seq()

    val tarObj: C = _instanceOf[C](tag.runtimeClass)
    return _controller(obj, tarObj, 0)
  }

  // The assigned historical definitions are stored here based on their levels.
  private var _history: Seq[(String, Int)] = Seq()

  private def _controller[C](obj: Any, tarObj: C, level: Int): Option[C] = {
    return _null[C](obj, tarObj, (real: Any, target: C) => {
      if (_classTypeName(real).contains("Tuple")) {
        var source: C = target
        _getClass(real).getDeclaredFields.reverse.foreach(tuple => {
          tuple.setAccessible(true)
          val tupled = _null(tuple.get(real), target, (subReal: Any, subTarget: C) => {
            _controller(subReal, subTarget, level).getOrElse(target)
          })

          if (tupled.isDefined) {
            source = tupled.get
          }
        })
        source
      } else {
        if (_setIsValid(obj)) {
          _checker(obj, tarObj, level)
          tarObj
        } else {
          _reflect(obj, tarObj, level)
        }
      }
    })
  }

  private def _null[C](obj: Any, tarObj: C, call: (Any, C) => C): Option[C] = {
    if (obj.isInstanceOf[Option[_]]) {
      val optObj = obj.asInstanceOf[Option[_]]
      if (optObj.isDefined) {
        return Some(call(optObj.get, tarObj))
      } else {
        return None
      }
    } else if (obj == null) {
      return None
    } else {
      return Some(call(obj, tarObj))
    }
  }

  private def _reflect[C](obj: Any, tarObj: C, level: Int, parent: (Field, Any, Boolean) = null): C = {
    _getClass(obj).getDeclaredFields.foreach(field => {
      field.setAccessible(true)
      _checker(obj, tarObj, level, field, parent)
    })

    // The values ​​of the same type can be skipped in the next assignment thanks to the history definition records.
    _history = _history :+ (_classTypeName(obj), level)

    return tarObj
  }

  private def _checker(obj: Any, tarObj: Any, level: Int, field: Field = null, parent: (Field, Any, Boolean) = null): Unit = {
    if (field != null) {
      try {
        // #Field~Parent Check Reflect
        // Destination-One -> Checked For Nested Reflection -> Success
        val skip = _history.filter(y => y == (_classTypeName(tarObj), level)).length > 0
        if (!skip) {
          val dest1 = _getClass(tarObj).getDeclaredFields.filter(x => x.getName == field.getName
            || (if (parent != null) parent._1.getName + x.getName == field.getName else false)
            || x.getName == _classTypeName(obj) + field.getName
            || (x.getName.contains("_") && _classTypeName(obj).contains(x.getName.split('_').head) && field.getName == x.getName.split('_').last)
          ).headOption
          if (dest1.isDefined) {
            val value = field.get(obj)
            if (value.isInstanceOf[Option[_]]) {
              val optValue = value.asInstanceOf[Option[_]]
              if (optValue.isDefined) {
                _setter(dest1.get, tarObj, optValue.get, level, parent)
              }
            } else if (value != null) {
              _setter(dest1.get, tarObj, value, level, parent)
            }
          }
        }
      }
      catch {
        case ex =>
      }

      try {
        // #Field~Contains(TypeName) Object Reflect
        val dest2 = _getClass(tarObj).getDeclaredFields.filter(x => field.getName.contains(_fieldTypeName(x, tarObj))).headOption
        if (dest2.isDefined) {
          dest2.get.setAccessible(true)

          var sourceValue: Any = field.get(obj)
          if (sourceValue.isInstanceOf[Option[_]]) {
            val optValue = sourceValue.asInstanceOf[Option[_]]
            if (optValue.isDefined) {
              sourceValue = optValue.get
            }
          }

          if (sourceValue != null) {
            var destValue: Any = dest2.get.get(tarObj)
            if (destValue.isInstanceOf[Option[_]]) {
              destValue = _instanceOf(dest2.get.getGenericType.asInstanceOf[ParameterizedType].getActualTypeArguments.head.asInstanceOf[Class[_]])
              _reflect(sourceValue, destValue, level + 1, (dest2.get, tarObj, true))
            } else {
              destValue = _instanceOf(dest2.get.getGenericType.asInstanceOf[Class[_]])
              _reflect(sourceValue, destValue, level + 1, (dest2.get, tarObj, false))
            }
          }
        }
      } catch {
        case ex =>
      }

      try {
        // #Type+Field Combined Reflect
        val dest3 = _getClass(tarObj).getDeclaredFields.filter(x => _classTypeName(obj) + x.getName == field.getName).headOption
        if (dest3.isDefined) {
          val value = field.get(obj)
          if (value.isInstanceOf[Option[_]]) {
            val optValue = value.asInstanceOf[Option[_]]
            if (optValue.isDefined) {
              _setter(dest3.get, tarObj, optValue.get, level, parent)
            }
          } else if (value != null) {
            _setter(dest3.get, tarObj, value, level, parent)
          }
        }
      } catch {
        case ex =>
      }
    }

    try {
      // #Object Reflect: History definition records are checked, if the current record is found, it jumps to the next field.
      val dest4Source = _getClass(tarObj).getDeclaredFields.filter(x => _classTypeName(obj) == _fieldTypeName(x, tarObj))
      val length = dest4Source.map(x => _history.filter(y => y == (_fieldTypeName(x, tarObj), level)).length).sum / dest4Source.length
      val dest4 = dest4Source.drop(length).headOption

      if (dest4.isDefined) {
        dest4.get.setAccessible(true)

        var destValue: Any = dest4.get.get(tarObj)
        if (destValue.isInstanceOf[Option[_]]) {
          destValue = _instanceOf(dest4.get.getGenericType.asInstanceOf[ParameterizedType].getActualTypeArguments.head.asInstanceOf[Class[_]])
          _reflect(obj, destValue, level + 1, (dest4.get, tarObj, true))
        } else {
          destValue = _instanceOf(dest4.get.getGenericType.asInstanceOf[Class[_]])
          _reflect(obj, destValue, level + 1, (dest4.get, tarObj, false))
        }
      }
    } catch {
      case ex =>
    }

    try {
      // #Field Reflect: History definition records are checked, if the current record is found, it jumps to the next field.
      val dest5Source = _getClass(tarObj).getDeclaredFields.filter(x => _setIsValid(obj) && _classTypeName(obj).toLowerCase().contains(_fieldTypeName(x, tarObj).toLowerCase()))
      val length = dest5Source.map(x => _history.filter(y => y == (_fieldTypeName(x, tarObj), level)).length).sum / dest5Source.length
      val dest5 = dest5Source.drop(length).headOption
      if (dest5.isDefined) {
        val value = obj
        if (value.isInstanceOf[Option[_]]) {
          val optValue = value.asInstanceOf[Option[_]]
          if (optValue.isDefined) {
            _setter(dest5.get, tarObj, optValue.get, level, parent)
          }
        } else if (value != null) {
          _setter(dest5.get, tarObj, value, level, parent)
        }
      }
    } catch {
      case ex =>
    }
  }

  private def _setter(field: Field, obj: Any, value: Any, level: Int, parent: (Field, Any, Boolean) = null): Unit = {
    field.setAccessible(true)
    try {
      if (_setIsValid(value)) {
        val destValue = field.get(obj)
        if (destValue.isInstanceOf[Option[_]]) {
          field.set(obj, Some(value))
        } else {
          field.set(obj, value)
        }

        if (parent != null) {
          parent._1.setAccessible(true)
          // Optional
          if (parent._3) {
            parent._1.set(parent._2, Some(obj))
          } else {
            parent._1.set(parent._2, obj)
          }
        }

        _history = _history :+ (_fieldTypeName(field, obj), level)
      }
    } catch {
      case ex =>
    }
  }

  private def _getClass(obj: Any): Class[_] = {
    var cls: Class[_] = null
    if (!obj.getClass.toString.contains("$")) {
      cls = obj.getClass
    } else {
      cls = obj.getClass.getAnnotatedSuperclass.getType.asInstanceOf[Class[_]]
    }
    return cls
  }

  private def _classTypeName(obj: Any): String = {
    return _typeNameSim(_getClass(obj).getSimpleName)
  }

  private def _typeNameSim(typeName: String): String = {
    return typeName.replace("ET", "").replace("RT", "").replace("DTO", "")
  }

  private def _fieldTypeName(field: Field, obj: Any): String = {
    field.setAccessible(true)

    try {
      val value = field.get(obj)
      if (value.isInstanceOf[Option[_]]) {
        return _typeNameSim(field.getGenericType.asInstanceOf[ParameterizedType].getActualTypeArguments.head.asInstanceOf[Class[_]].getSimpleName)
      } else {
        return _typeNameSim(field.getGenericType.asInstanceOf[Class[_]].getSimpleName)
      }
    } catch {
      case ex => return ""
    }
  }

  private def _setIsValid(obj: Any): Boolean = {
    val cls = _getClass(obj)
    return Seq("java.", "scala.").filter(x => cls.toString.contains(x)).length > 0
  }

  private def _instanceOf[C](cls: Class[_]): C = {
    var args: Seq[AnyRef] = Seq()
    cls.getDeclaredFields.foreach(field => {
      field.setAccessible(true)
      var arg: AnyRef = field.getType.getTypeName match {
        case "scala.Option" => None
        case "boolean" => false.asInstanceOf[AnyRef]
        case "long" => new java.lang.Long(0)
        case "int" => new java.lang.Integer(0)
        case "float" => new java.lang.Float(0)
        case "double" => new java.lang.Double(0)
        case "byte" => new java.lang.Byte(0.toByte)
        case "java.lang.String" => new java.lang.String("")
        case "scala.math.BigDecimal" => BigDecimal(0)
        case "scala.collection.Seq" => Seq()
        case "scala.collection.immutable.List" => List()
        case _ => null
      }
      args = args :+ arg
    })

    cls.getConstructors()(0).newInstance(args: _*).asInstanceOf[C]
  }
}
