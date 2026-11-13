@echo off
chcp 65001 >nul
set "JAVA_HOME=D:\jdk-21"
set "MAVEN_HOME=D:\apache-maven-3.9.9"
set "PATH=D:\jdk-21\bin;D:\apache-maven-3.9.9\bin;%PATH%"
cd /d D:\DSH\camp-rent\camp-back
echo Starting Spring Boot backend...
mvn spring-boot:run > D:\DSH\camp-rent\back.log 2>&1
echo.
echo ===== backend exited, code=%errorlevel% =====
pause
