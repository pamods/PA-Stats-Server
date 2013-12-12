java -Xmx512M -Xss2M -XX:MaxPermSize=256m -XX:+CMSClassUnloadingEnabled -jar `dirname $0`/sbt-launch-0.12.1.jar "$@"
