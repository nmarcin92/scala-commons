package com.avsystem.commons
package rpc.akka

import com.avsystem.commons.rpc.RPCFramework
import monix.reactive.Observable

trait MonixRPCFramework extends RPCFramework {
  override type RawRPC <: MonixRawRPC

  trait MonixRawRPC { this: RawRPC =>
    def observe(rpcName: String, args: BMap[String, RawValue]): Observable[RawValue]
  }

  implicit def ObservableRealHandler[A: Writer]: RealInvocationHandler[Observable[A], Observable[RawValue]] =
    RealInvocationHandler[Observable[A], Observable[RawValue]](_.map(write[A] _))

  implicit def ObservableRawHandler[A: Reader]: RawInvocationHandler[Observable[A]] =
    RawInvocationHandler[Observable[A]]((rawRpc, rpcName, args) => rawRpc.observe(rpcName, args).map(read[A] _))
}
