package com.avsystem.commons
package rpc

case class RPCException(msg: String = null, cause: Throwable = null) extends Exception(msg, cause)
