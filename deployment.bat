@echo off

javac -parameters -d classes -sourcepath mg mg\itu\prom16\*.java mg\itu\prom16\annotation\*.java mg\itu\prom16\mapping\*.java -cp "C:\Program Files\Apache Software Foundation\Tomcat 10.1\lib\*"

cd classes
jar -cvf jars/Framework2362.jar mg

pause
if errorlevel 1 (
  echo Compilation failed.
    exit /b 1
)