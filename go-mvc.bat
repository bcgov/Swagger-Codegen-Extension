del mvc\*.* /s /q
call generate-all.bat swagger.json mvc
rem goto end1:

cd mvc\src\SchoolBusAPI
dotnet restore
dotnet ef migrations add initial
cd ..\..\..

:end1