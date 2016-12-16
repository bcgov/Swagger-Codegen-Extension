set executable=target/aspnetCore-swagger-codegen-1.0.0.jar;swagger-codegen-cli.jar
set JAVA_OPTS=-Dfile.encoding=UTF-8 -Dmodels
set JAVA_OPTS=%JAVA_OPTS% -Xmx1024M
set args=generate -i swagger.json -l aspnetmvc -o output-models -c config.json 

java %JAVA_OPTS% -cp %executable% io.swagger.codegen.SwaggerCodegen %args% 