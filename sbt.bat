set SCRIPT_DIR=%~dp0
java -Drun.mode=development -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=1024m -Xmx400M -Xss2M -jar "%SCRIPT_DIR%\sbt-launch-0.12.1.jar" %*
