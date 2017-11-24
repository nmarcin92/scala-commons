package com.avsystem.commons
package rpc

import com.avsystem.commons.concurrent.{HasExecutionContext, RunNowEC}
import com.avsystem.commons.rpc.DummyRPC._
import com.github.ghik.silencer.silent
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.collection.mutable.ArrayBuffer

class RPCTest extends WordSpec with Matchers with BeforeAndAfterAll {

  trait RunNowFutureCallbacks extends HasExecutionContext {
    protected implicit final def executionContext: ExecutionContext = RunNowEC
  }

  def get[T](f: Future[T]) =
    f.value.get.get

  "rpc caller" should {
    "should properly deserialize RPC calls" in {
      val invocations = new ArrayBuffer[(String, BMap[String, Any])]
      val rawRpc = AsRawRPC[TestRPC].asRaw(TestRPC.rpcImpl((name, args, _) => {
        invocations += ((name, args))
        name
      }))

      rawRpc.fire("handleMore", Map.empty)
      rawRpc.fire("doStuff", Map("lol" -> 42, "fuu" -> "omgsrsly", "cos" -> Some(true)))
      assert("doStuffResult" === get(rawRpc.call("doStuffBoolean", Map("yes" -> true))))
      rawRpc.fire("doStuffInt", Map("num" -> 5))
      rawRpc.fire("handleMore", Map.empty)
      rawRpc.fire("handle", Map.empty)
      rawRpc.fire("srslyDude", Map.empty)
      rawRpc.get("innerRpc", Map("name" -> "innerName")).fire("proc", Map.empty)
      assert("innerRpc.funcResult" === get(rawRpc.get("innerRpc", Map("name" -> "innerName")).call("func", Map("arg" -> 42))))

      assert(invocations.toList === List(
        ("handleMore", Map.empty),
        ("doStuff", Map("lol" -> 42, "fuu" -> "omgsrsly", "cos" -> Some(true))),
        ("doStuffBoolean", Map("yes" -> true)),
        ("doStuffInt", Map("num" -> 5)),
        ("handleMore", Map.empty),
        ("handle", Map.empty),
        ("srslyDude", Map.empty),
        ("innerRpc", Map("name" -> "innerName")),
        ("innerRpc.proc", Map.empty),
        ("innerRpc", Map("name" -> "innerName")),
        ("innerRpc.func", Map("arg" -> 42))
      ))
    }

    "fail on bad input" in {
      val rawRpc = AsRawRPC[TestRPC].asRaw(TestRPC.rpcImpl((_, _, _) => ()))
      intercept[Exception](rawRpc.fire("whatever", Map.empty))
    }

    "real rpc should properly serialize calls to raw rpc" in {
      val invocations = new ArrayBuffer[(String, BMap[String, Any])]

      object rawRpc extends RawRPC with RunNowFutureCallbacks {
        def fire(rpcName: String, args: BMap[String, Any]): Unit =
          invocations += ((rpcName, args))

        def call(rpcName: String, args: BMap[String, Any]): Future[Any] = {
          invocations += ((rpcName, args))
          Future.successful(rpcName + "Result")
        }

        def get(rpcName: String, args: BMap[String, Any]): RawRPC = {
          invocations += ((rpcName, args))
          this
        }
      }

      @silent
      val realRpc = AsRealRPC[TestRPC].asReal(rawRpc)

      realRpc.handleMore()
      realRpc.doStuff(42, "omgsrsly")(Some(true))
      assert("doStuffBooleanResult" === get(realRpc.doStuff(true)))
      realRpc.doStuff(5)
      realRpc.handleMore()
      realRpc.handle
      realRpc.innerRpc("innerName").proc()
      realRpc.innerRpc("innerName").moreInner("moreInner").moreInner("evenMoreInner").func(42)

      assert(invocations.toList === List(
        ("handleMore", Map.empty),
        ("doStuff", Map("lol" -> 42, "fuu" -> "omgsrsly", "cos" -> Some(true))),
        ("doStuffBoolean", Map("yes" -> true)),
        ("doStuffInt", Map("num" -> 5)),
        ("handleMore", Map.empty),
        ("handle", Map.empty),

        ("innerRpc", Map("name" -> "innerName")),
        ("proc", Map.empty),

        ("innerRpc", Map("name" -> "innerName")),
        ("moreInner", Map("name" -> "moreInner")),
        ("moreInner", Map("name" -> "evenMoreInner")),
        ("func", Map("arg" -> 42))
      ))
    }

    @RPC trait BaseRPC[T] {
      def accept(t: T): Unit
    }

    trait ConcreteRPC extends BaseRPC[String]

    "rpc should work with parameterized interface types" in {
      materializeFullInfo[ConcreteRPC]
    }

    @RPC trait EmptyRPC

    "rpc should work with empty interface types" in {
      materializeFullInfo[EmptyRPC]: @silent
    }
  }
}
