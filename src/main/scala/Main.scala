import Fibo.fibonacci

object Fibo {
  def fibonacci(n: Int): Int =
    if(n == 0 || n == 1)
      n
    else
      fibonacci(n - 1) + fibonacci(n - 2)  }
}

object Main {
  def main(args: Array[String]): Unit = {
    println(fibonacci(10))
  }
}
