setlocal
set PATH=C:\Program Files\Java\jdk7\bin;%PATH%
set SCRIPT_DIR=%~dp0
java -Drun.mode=development -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=1024m -Xmx2000M -Xss2M -jar "%SCRIPT_DIR%\sbt-launch-0.12.1.jar" %*
