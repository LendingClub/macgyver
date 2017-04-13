

println "services.groovy execution"
foo="bar"
services {

    foo {
        xyz {
            password="eyJrIjoibWFjMCIsImQiOiIrdTlEQVlPZVlld3Z4YXZ4VVdtU256OFM4UWxIUWFIOUVNc1REY0xxejM4PSJ9"
            password="eyJrIjoibWFjMCIsImQiOiIxdVREM1M2L3hmME1lY1IvVlFxaE83NmFMZjFCR1ZvZ2pHaHZWUGxiMVBzPSJ9"
        }
    }
   

}

myTestBean.foo="bar"


unittest.testBeanName.serviceType="testService"
unittest.testBeanName.foo="bar"

a.b.c.url="http://whatever"


proxy.test0.host="foo"
proxy.test0.port=8000
proxy.test0.username="scott"
proxy.test0.password="tiger"


proxy.test1.host="foo"
//proxy.test1.port="a"
//proxy.test1.username="scott"
//proxy.test0.password="tiger"