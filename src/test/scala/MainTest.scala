import Fibo.fibonacci
import org.scalatest.funsuite.{AnyFunSuite, AsyncFunSuite}

class MainTest extends AnyFunSuite {
  test("Fibonacci of 10 is 55") {
    assert(fibonacci(10) == 5)
  }
}
