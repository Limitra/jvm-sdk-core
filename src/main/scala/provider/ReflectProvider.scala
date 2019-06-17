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
    val tarObj: C = tag.runtimeClass.getDeclaredConstructor().newInstance().asInstanceOf[C]
    return _controller(obj, tarObj)
  }

  private def _controller[C](obj: Any, tarObj: C): Option[C] = {
    return _null[C](obj, tarObj, (real: Any, target: C) => {
      if (real.isInstanceOf[Tuple2[_, _]]) {
        val tupleObj = obj.asInstanceOf[Tuple2[_, _]]
        val tuple1 = _null(tupleObj._1, target, (subReal: Any, subTarget: C) => {
          _controller(subReal, subTarget).getOrElse(target)
        })

        var source: C = target
        if (tuple1.isDefined) {
          source = tuple1.get
        }

        val tuple2 = _null(tupleObj._2, source, (subReal: Any, subTarget: C) => {
          _controller(subReal, subTarget).getOrElse(source)
        })

        if (tuple2.isDefined) {
          source = tuple2.get
        }
        source
      } else {
        _reflect(obj, tarObj, null)
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

  private def _reflect[C](obj: Any, tarObj: C, parent: (Field, Any, Boolean) = null): C = {
    _getClass(obj).getDeclaredFields.foreach(field => {
      field.setAccessible(true)
      _checker(field, obj, tarObj, parent)
    })
    return tarObj
  }

  private def _checker(field: Field, obj: Any, tarObj: Any, parent: (Field, Any, Boolean) = null): Unit = {
    try {
      val dest1 = _getClass(tarObj).getDeclaredFields.filter(x => x.getName == field.getName
        || (if (parent != null) parent._1.getName + x.getName == field.getName else false)).headOption
      if (dest1.isDefined) {
        val value = field.get(obj)
        if (value.isInstanceOf[Option[_]]) {
          val optValue = value.asInstanceOf[Option[_]]
          if (optValue.isDefined) {
            _setter(dest1.get, tarObj, optValue.get, parent)
          }
        } else if (value != null) {
          _setter(dest1.get, tarObj, value, parent)
        }
      }
    } catch {
      case ex =>
    }

    try {
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
            destValue = dest2.get.getGenericType.asInstanceOf[ParameterizedType].getActualTypeArguments.head.asInstanceOf[Class[_]].newInstance()
            _reflect(sourceValue, destValue, (dest2.get, tarObj, true))
          } else {
            destValue = dest2.get.getGenericType.asInstanceOf[Class[_]].newInstance()
            _reflect(sourceValue, destValue, (dest2.get, tarObj, false))
          }
        }
      }
    } catch {
      case ex =>
    }

    try {
      val dest3 = _getClass(tarObj).getDeclaredFields.filter(x => _classTypeName(obj) == _fieldTypeName(x, tarObj)).headOption
      if(dest3.isDefined) {
        dest3.get.setAccessible(true)
        var destValue: Any = dest3.get.get(tarObj)
        if (destValue.isInstanceOf[Option[_]]) {
          destValue = dest3.get.getGenericType.asInstanceOf[ParameterizedType].getActualTypeArguments.head.asInstanceOf[Class[_]].newInstance()
          _reflect(obj, destValue, (dest3.get, tarObj, true))
        } else {
          destValue = dest3.get.getGenericType.asInstanceOf[Class[_]].newInstance()
          _reflect(obj, destValue, (dest3.get, tarObj, false))
        }
      }
    } catch {
      case ex =>
    }

    try {
      val dest4 = _getClass(tarObj).getDeclaredFields.filter(x => _classTypeName(obj) + x.getName == field.getName).headOption
      if (dest4.isDefined) {
        val value = field.get(obj)
        if (value.isInstanceOf[Option[_]]) {
          val optValue = value.asInstanceOf[Option[_]]
          if (optValue.isDefined) {
            _setter(dest4.get, tarObj, optValue.get, parent)
          }
        } else if (value != null) {
          _setter(dest4.get, tarObj, value, parent)
        }
      }
    } catch {
      case ex =>
    }
  }

  private def _setter(field: Field, obj: Any, value: Any, parent: (Field, Any, Boolean) = null): Unit = {
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
}
