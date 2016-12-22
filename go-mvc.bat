del mvc\*.* /s /q
call generate-all.bat http://editor-tran-schoolbus-dev.pathfinder.gov.bc.ca/export/swagger.yaml mvc
rem goto end1:

cd mvc\src\SchoolBusAPI
rem dotnet restore
rem dotnet ef migrations add initial
cd ..\..\..

:end1