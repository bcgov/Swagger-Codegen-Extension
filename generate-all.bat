@ECHO OFF
IF %1.==. GOTO No1
IF %2.==. GOTO No2
IF %3.==. GOTO No3

set executable=target/swagger-codegen-extension-1.0.1-jar-with-dependencies.jar;swagger-codegen-cli.jar
set JAVA_OPTS=-Dfile.encoding=UTF-8 -DapiTests=true -DmodelTests=true
set JAVA_OPTS=%JAVA_OPTS% -Xmx1024M
set args=generate -i %1 -l aspnetmvc -c %3 -o %2

java %JAVA_OPTS% -cp %executable% io.swagger.codegen.SwaggerCodegen %args% 


GOTO End1
:No1
  ECHO Please specify the location of the swagger JSON file.
GOTO USAGE:
:No2
  ECHO Please specify the output directory.
:No3
  ECHO Please specify the config location.

GOTO USAGE:
:USAGE
  
ECHO generate-api ^<Swagger JSON file^> ^<output folder^> ^<config location^> 

:End1
