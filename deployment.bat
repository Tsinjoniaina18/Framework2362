@echo off

javac -d classes -sourcepath mg mg\itu\prom16\*.java mg\itu\prom16\annotation\*.java -cp C:\Users\Tsinjoniaina\Downloads\ITU\S4\Programmation\WebDynamique\Sprint\Framework2362\classes\jars\*

cd classes
jar -cvf jars/Framework2362.jar mg

pause
if errorlevel 1 (
  echo Compilation failed.
    exit /b 1
)