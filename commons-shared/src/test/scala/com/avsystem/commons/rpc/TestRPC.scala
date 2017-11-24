package com.avsystem.commons
package rpc

import com.github.ghik.silencer.silent


case class Record(i: Int, fuu: String)

@RPC trait InnerRPC {
  def proc(): Unit

  def func(arg: Int): Future[String]

  def moreInner(name: String): InnerRPC

  def indirectRecursion(): TestRPC
}
object InnerRPC extends DummyRPC.RPCCompanion[InnerRPC]

@RPC trait TestRPC {
  @silent
  def handle: Unit

  def handleMore(): Unit

  def doStuff(lol: Int, fuu: String)(implicit cos: Option[Boolean]): Unit

  @RPCName("doStuffBoolean")
  def doStuff(yes: Boolean): Future[String]

  @RPCName("doStuffInt")
  def doStuff(num: Int): Unit

  def takeCC(r: Record): Unit

  def srslyDude(): Unit

  def innerRpc(name: String): InnerRPC
}

@silent
object TestRPC extends DummyRPC.RPCCompanion[TestRPC] {
  def rpcImpl(onInvocation: (String, BMap[String, Any], Option[Any]) => Any) = new TestRPC { outer =>
    private def onProcedure(methodName: String, args: BMap[String, Any]): Unit =
      onInvocation(methodName, args, None)

    private def onCall[T](methodName: String, args: BMap[String, Any], result: T): Future[T] = {
      onInvocation(methodName, args, Some(result))
      Future.successful(result)
    }

    private def onGet[T](methodName: String, args: BMap[String, Any], result: T): T = {
      onInvocation(methodName, args, None)
      result
    }

    def handleMore(): Unit =
      onProcedure("handleMore", Map.empty)

    def doStuff(lol: Int, fuu: String)(implicit cos: Option[Boolean]): Unit =
      onProcedure("doStuff", Map("lol" -> lol, "fuu" -> fuu, "cos" -> cos))

    def doStuff(yes: Boolean): Future[String] =
      onCall("doStuffBoolean", Map("yes" -> yes), "doStuffResult")

    def doStuff(num: Int): Unit =
      onProcedure("doStuffInt", Map("num" -> num))

    def handle: Unit =
      onProcedure("handle", Map.empty)

    def takeCC(r: Record): Unit =
      onProcedure("recordCC", Map("r" -> r))

    def srslyDude(): Unit =
      onProcedure("srslyDude", Map.empty)

    def innerRpc(name: String): InnerRPC = {
      onInvocation("innerRpc", Map("name" -> name), None)
      new InnerRPC {
        def func(arg: Int): Future[String] =
          onCall("innerRpc.func", Map("arg" -> arg), "innerRpc.funcResult")

        def proc(): Unit =
          onProcedure("innerRpc.proc", Map.empty)

        def moreInner(name: String) =
          this

        def indirectRecursion() =
          outer
      }
    }
  }
}
