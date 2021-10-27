#!/usr/bin/env groovy

/**
 *
 * @Title ${FILE_NAME}* @Package com.itaddr.demo.testing* @Author 马嘉祺* @Date 2021/10/19 20:43
 * @Description ${TODO}
 */

def dd = "curl -X GET \"http://192.168.1.241:30005/api/v2.0/projects/sparrow/repositories/dev-speeder-mgr/artifacts?page=1&page_size=10&with_tag=true&with_label=false&with_scan_overview=false&with_signature=false&with_immutable_status=false\" -H \"accept: application/json\" -H \"authorization: Basic ZGV2b3BzOlpGNFBtSlc0N0hzYUc2V0JCYUxQ\" -H \"X-Harbor-CSRF-Token: OEBX0uWYlLZKkdAn3WBdOLuL1j0oIacYl9HMLtieLLcmJr8DetVJ6ncW5YJkBoMmc0Nv3QyNtpioXwfhkUkMhw==\""
def command = "curl -X GET \"http://192.168.1.241:30005/api/v2.0/projects/sparrow/repositories/dev-speeder-mgr/artifacts?page=1&page_size=10&with_tag=true&with_label=false&with_scan_overview=false&with_signature=false&with_immutable_status=false\" -H \"accept: application/json\" -H \"authorization: Basic ZGV2b3BzOlpGNFBtSlc0N0hzYUc2V0JCYUxQ\" -H \"X-Harbor-CSRF-Token: OEBX0uWYlLZKkdAn3WBdOLuL1j0oIacYl9HMLtieLLcmJr8DetVJ6ncW5YJkBoMmc0Nv3QyNtpioXwfhkUkMhw==\""   // Create the String
def proc = command.execute()                 // Call *execute* on the string
proc.waitFor()                               // Wait for the command to finish

// Obtain status and output
println "return code: ${proc.exitValue()}"
println "stderr: ${proc.err.text}"
println "stdout: ${proc.in.text}" // *out* from the external program is *in* for groovy


//def command = ["/bin/bash", "-c", """ curl -X GET "http://192.168.1.241:30005/api/v2.0/projects/sparrow/repositories/dev-speeder-mgr/artifacts?page=1&page_size=10&with_tag=true&with_label=false&with_scan_overview=false&with_signature=false&with_immutable_status=false" -H "accept: application/json" -H "authorization: Basic ZGV2b3BzOlpGNFBtSlc0N0hzYUc2V0JCYUxQ" -H "X-Harbor-CSRF-Token: 3ddYpyH1o7HvSezy/BJgvCq+OEBX0uWYlLZKkdAn3WBdOLuL1j0oIacYl9HMLtieLLcmJr8DetVJ6ncW5YJkBoMmc0Nv3QyNtpioXwfhkUkMhw==" |jq .[].tags[].name|sort -nr|xargs echo |sed -e 's/ /,/g' """]
//def proc = command.execute()
//proc.waitFor()
//DOCKER_TAG="${proc.in.text}"


#!/usr/bin/env groovy
//def sout = new StringBuilder()
//def serr = new StringBuilder()
//def proc = "mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.artifactId -q -DforceStdout -f /home/jenkins/home/workspace/pipeline-test/pom.xml".execute()
//proc.consumeProcessOutput(sout, serr)
//proc.waitFor()
//println "${sout.toString()}"
//PROJECT_NAMES = sout.toString()


#!/usr/bin/env groovy
//def proc = "mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.artifactId -q -DforceStdout -f /home/jenkins/home/workspace/pipeline-test/pom.xml".execute()
//proc.waitForOrKill(30000)
//def result = proc.in.text
//PROJECT_NAMES = result
