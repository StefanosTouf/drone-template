import Fibo.fibonacci

object Fibo {
  def fibonacci(n: Int): Int = n match {
    case 0 => 0
    case 1 => 1
    case x => fibonacci(x - 1) + fibonacci(x - 2)
  }
}

object Main {

  def main(args: Array[String]): Unit = {
    println(fibonacci(10))
  }
}
