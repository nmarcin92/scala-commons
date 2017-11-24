package com.avsystem.commons
package rpc

trait MacroInstances { this: RPCFramework =>
  abstract class AbstractAsRawRPC[T] extends AsRawRPC[T] {
    override def asRaw(rpcImpl: T): RawRPC = ???
  }
}
