call mvn package
del tfrs\*.* /s /q
cd ..\tfrs\tfrs\ApiSpec\
call update.bat
cd  ..\..\..\Swagger-Codegen-Extension

rem goto end1:
call generate-all-django.bat ../tfrs/tfrs/ApiSpec/TFRSswagger.yaml tfrs ../tfrs/tfrs/ApiSpec/swagger-codegen-config.json


rem copy over files.

xcopy /Y tfrs\models ..\tfrs\server\models
rem xcopy /Y tfrs\views ..\tfrs\server\views
copy tfrs\admin.py ..\tfrs\server
copy tfrs\serializers.py ..\tfrs\server
copy tfrs\urls.py ..\tfrs\server
copy tfrs\test_api.py ..\tfrs\server
copy tfrs\views.py ..\tfrs\server
copy tfrs\fakedata.py ..\tfrs\server

rem goto end1:


:end1