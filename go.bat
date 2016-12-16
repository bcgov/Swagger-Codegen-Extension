set executable=target/aspnetCore-swagger-codegen-1.0.0.jar;swagger-codegen-cli.jar
set JAVA_OPTS=-Dfile.encoding=UTF-8  
REM -DapiTests=true -DmodelTests=true
set JAVA_OPTS=%JAVA_OPTS% -Xmx1024M
set ags=generate -i swagger.json -l aspnetmvc -o output -c config.json

java %JAVA_OPTS% -cp %executable% io.swagger.codegen.SwaggerCodegen %ags%