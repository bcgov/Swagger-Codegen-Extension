@ECHO OFF
IF %1.==. GOTO No1
IF %2.==. GOTO No2

set executable=target/aspnetCore-swagger-codegen-1.0.0.jar;swagger-codegen-cli.jar
set JAVA_OPTS=-Dfile.encoding=UTF-8 -DapiTests -DmodelTests
set JAVA_OPTS=%JAVA_OPTS% -Xmx1024M
set args=generate -i %1 -l aspnetmvc -c config.json -o %2

java %JAVA_OPTS% -cp %executable% io.swagger.codegen.SwaggerCodegen %args% 


GOTO End1
:No1
  ECHO Please specify the location of the swagger JSON file.
GOTO USAGE:
:No2
  ECHO Please specify the output directory.
GOTO USAGE:
:USAGE
  
ECHO generate-api <Swagger JSON file> <output folder>

:End1
