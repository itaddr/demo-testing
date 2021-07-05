package com.itaddr.demo.testing

/**
 *
 * @Author 马嘉祺
 * @Date 2020/7/3 0003 08 42
 * @Description <p></p>
 *
 */
case class BaseBean01(id: Int, name: String, age: Int)

case object BaseBean01 {
    def apply(id: Int, name: String, age: Int): BaseBean01 = new BaseBean01(id, name, age)
    
    def unapply(arg: BaseBean01): Option[(Int, String, Int)] = Some(arg.id, arg.name, arg.age)
}
