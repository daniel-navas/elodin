package com.github.caeus.plutus

import com.github.caeus.plutus.json.JSON.{JSNumber, JSObject, JSText}
import com.github.caeus.plutus.json.{JSON, JsonPacker}
import zio.test.Assertion._
import zio.test._

object JsonPackerSuites extends DefaultRunnableSpec {

  override def spec = {
    suite("JsonPackerSuites")(
      test("JsonPacker") {
        val packer = new JsonPacker
        val value  = packer.run("""
	 	          |{
	           	|	"name": "Alejandro",
	           	|	"age": 30
           		|}
              |""".stripMargin)
        value.left.foreach { ex =>
          println(ex.getMessage)
        }
        assert(value)(
          isRight(
            isSubtype[JSObject](
              hasField[JSObject, Option[JSON]](
                "name",
                _.value.get("name"),
                isSome(
                  isSubtype[JSText](hasField("content", _.value, equalTo("Alejandro")))
                )
              ) && hasField(
                "age",
                _.value.get("age"),
                isSome(
                  isSubtype[JSNumber](
                    hasField("content", _.value, equalTo(BigDecimal(30)))
                  )
                )
              )
            )
          )
        )
      }
    ) @@ TestAspect.ignore
  }
}
