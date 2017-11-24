package com.avsystem.commons
package jetty.rpc

import com.avsystem.commons.serialization.GenCodec
import upickle.Js

object JsonJettyRPCFramework extends JettyRPCFramework {
  type RawValue = Js.Value
  type Reader[T] = GenCodec[T]
  type Writer[T] = GenCodec[T]
  type ParamTypeMetadata[T] = ClassTag[T]
  type ResultTypeMetadata[T] = DummyImplicit

  def valueToJson(value: RawValue) = upickle.json.write(value)
  def jsonToValue(json: String) = upickle.json.read(json)
  def argsToJson(args: BMap[String, RawValue]) = upickle.json.write(argsToJsObj(args))
  def jsonToArgs(json: String) = jsObjToArgs(upickle.json.read(json))

  def read[T: Reader](raw: RawValue): T = GenCodec.read[T](new JsValueInput(raw))
  def write[T: Writer](value: T): RawValue = JsValueOutput.write[T](value)

  def argsToJsObj(args: BMap[String, Js.Value]): Js.Value =
    Js.Obj(args.toList: _*)

  def jsObjToArgs(value: Js.Value): BMap[String, Js.Value] = {
    value match {
      case obj: Js.Obj => obj.value.toMap
      case _ => Map.empty
    }
  }
}
