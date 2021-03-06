package com.avsystem.commons
package mongo.core.ops

import com.avsystem.commons.mongo.DocKey
import org.bson.BsonValue

trait DocKeyKeyValueHandling[T] extends Any with KeyValueHandling[T] with DocKeyKeyHandling[T] {
  override protected def encode(t: T): BsonValue = docKey.codec.toBson(t)
}
