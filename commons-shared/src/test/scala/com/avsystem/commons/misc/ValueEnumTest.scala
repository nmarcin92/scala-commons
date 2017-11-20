package com.avsystem.commons
package misc

import org.scalatest.FunSuite

class ValueEnumTest extends FunSuite {
  class SomeValueEnum(implicit enumCtx: EnumCtx) extends AbstractValueEnum
  object SomeValueEnum extends AbstractValueEnumCompanion[SomeValueEnum] {
    final val One, Two, Three: Value = new SomeValueEnum
    final val Four: Value = new SomeValueEnum {}
  }

  test("value enum test") {
    import SomeValueEnum._
    assert(values == List(One, Two, Three, Four))
    assert(values.map(_.ordinal) == List.range(0, 4))
    assert(values.map(_.name) == List("One", "Two", "Three", "Four"))
  }

  test("enum constant member validation") {
    assertCompiles(
      """
        |final class Enumz(implicit enumCtx: EnumCtx) extends AbstractValueEnum
        |object Enumz extends AbstractValueEnumCompanion[Enumz] {
        |  final val Constant: Value = new Enumz
        |}
      """.stripMargin
    )
    assertDoesNotCompile(
      """
        |final class Enumz(implicit enumCtx: EnumCtx) extends AbstractValueEnum
        |object Enumz extends AbstractValueEnumCompanion[Enumz] {
        |  private final val Constant: Value = new Enumz
        |}
      """.stripMargin
    )
    assertDoesNotCompile(
      """
        |final class Enumz(implicit enumCtx: EnumCtx) extends AbstractValueEnum
        |object Enumz extends AbstractValueEnumCompanion[Enumz] {
        |  final def Constant: Value = new Enumz
        |}
      """.stripMargin
    )
    assertDoesNotCompile(
      """
        |final class Enumz(implicit enumCtx: EnumCtx) extends AbstractValueEnum
        |object Enumz extends AbstractValueEnumCompanion[Enumz] {
        |  val Constant: Value = new Enumz
        |}
      """.stripMargin
    )
    assertDoesNotCompile(
      """
        |final class Enumz(implicit enumCtx: EnumCtx) extends AbstractValueEnum
        |object Enumz extends AbstractValueEnumCompanion[Enumz] {
        |  final lazy val Constant: Value = new Enumz
        |}
      """.stripMargin
    )
    assertDoesNotCompile(
      """
        |final class Enumz(implicit enumCtx: EnumCtx) extends AbstractValueEnum
        |object Enumz extends AbstractValueEnumCompanion[Enumz] {
        |  final val Constant = new Enumz
        |}
      """.stripMargin
    )
    assertDoesNotCompile(
      """
        |final class Enumz(implicit enumCtx: EnumCtx) extends AbstractValueEnum
        |object Enumz extends AbstractValueEnumCompanion[Enumz] {
        |  object Inner {
        |    final val Constant: Value = new Enumz
        |  }
        |}
      """.stripMargin
    )
  }
}
