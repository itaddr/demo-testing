package com.lbole.demo.testing

import java.nio.charset.StandardCharsets

import org.junit.Test

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
 *
 * @Author 马嘉祺
 * @Date 2020/7/1 0001 21 16
 * @Description <p></p>
 *
 */
class BaseTest01 {
    
    @Test
    @throws(classOf[Exception]) def helloWorld(): Unit = {
        println("hello world")
        println("------------------")
        println(java.util.Arrays.toString(System.getProperty("line.separator").getBytes(StandardCharsets.US_ASCII)))
        println("------------------")
    }
    
    @Test
    @throws(classOf[Exception]) def test01(): Unit = {
        val languageToCount = Map("Scala" -> 10, "Java" -> 20, "Ruby" -> 5)
        println(languageToCount.map { case (_, count) => count + 1 }.toSeq)
    }
    
    @Test
    @throws(classOf[Exception]) def test02(): Unit = {
        val var1 = Seq(1, 2, 3, 4, 5)
        val var2 = ArrayBuffer(1, 2, 3, 4, 5) += 6
        val var3 = ListBuffer(1, 2, 3, 4, 5) += 7
        println(var1.getClass)
        println(var1.head)
        println(var2)
        println(var3)
    }
    
    @Test
    @throws(classOf[Exception]) def test03(): Unit = {
        val caseBean = CaseBean01(1, "CaseBean01")
        val baseBean = new BaseBean01(2, "BaseBean02", 18)
        
        /*baseBean match {
            case BaseBean01(id, name, age) => println(name)
            case CaseBean01(id, name) => println(name)
        }
        
        caseBean match {
            case BaseBean01(id, name, age) => println(name)
            case CaseBean01(id, name) => println(name)
        }*/
        
    }
    
}

object BaseTest01 {
}
