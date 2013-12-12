PA-Stats-Server
===============

PA Stats Backend

To run your own server you need a postgres database. PA Stats uses a few postgres extensions and is only tested on postgres.
The PA Stats Server itself is a packaged as war and run inside a jetty webserver.

Configuration steps:

PA Stats was build to use a database called rbztest for testing and rbz for production use. These names can be configured, but for simplicity this 
info will use them. Search the project for those names if you want to change them.

0.)
Clone the repository using your favorite tools. I use the scala eclipse from typesafe with some git-plugin. To create eclipse project configuration
you can run "eclipse" in sbt. Read about sbt a few steps below.

1.) Setup database rbztest, run the tablesv2.sql you can find under db_definition to create all necessary tables.
1.1) For production use create additionally a database called rbz the same way.

2.) Open /PA-Stats-Server/src/main/webapp/WEB-INF/jetty-env.xml It is filled with the config I currently use, excluding login information.
Configure it for your database, that means at least change the urls if necessary.

3.) run sbt.bat or ./sbt, execute "~; container:start; container:reload /" (one command, ommit quotes) to start the server in a way to make it automatically reload all changes you do to the code/resources. Go to http://127.0.0.1:8080/ to see the server is running.

4.) For production use I run "package" in sbt. This will create a war you can deploy in a jetty application server. To use the production (rbz) database and to tell liftweb to generally be more efficient run the container with -Drun.mode=production Note that there are different configurations under /PA-Stats-Server/src/main/resources/props/ for the run-modes.

5.) To run the mod in testmode put this var in your common.js in PA:
var statsDevelopment = true;
When you run in production mode you probably need to configure a few URLs in these places:
uimod/scenes/global.js
uimod/pa_stats_loader.js
/PA-Stats-Server/src/main/resources/props/production.default.props

6.) If you change the database schema read the readme under jooq_code_generation. It tells you how to create new java classes (found under src/main/java/info.nanodesu.genereated) that are used to build sql statements in the project.

A little overview about the packages in src/main/scala/info/nanodesu/:

General information about liftweb: http://cookbook.liftweb.net/

snippet
=======
Here are so called "snippets" that are used in templates (found under src/main/webapp/, which is the folder that contains all web-resources for the deployment). Snippets are responsible too process the html are called from to create the webpage. Generally to design a new part of the webpage you can first just design the html directly and later write a snippet to fill it with data.

rest
====
Here you will find the class that creates the webservices that are used to communicate with the mods or some js parts of the page

pages
=====
manage the different pages with their basic parameters, so if you want to link another part of the page you can find a method to generate the link here.

model
=====
Dataprocessing things. under db you will find "collectors" and "updaters". A collector is meant to fetch all data necessary i.e. for a snippet and package it in an easy to use format for the snippet. Thus database-access and dataprocessing is not part of the snippets.
An updater is meant to update things in the database, currently this means you will find the logic to process reports from PA here. Note that this is a 2 step process: The ui-mod first sends a big datapackage and gets back an id that is used to send more data. That way the server does not need to process redundant information multiple times.

lib
===
helper classes

comet
=====
similar to the snippets these classes are meant to modify html templates. Difference is they insert comet javascript into the pages, enabling the server to push live updates. This is currently used for the game info, it is planned to be used for all live updates (especially the chart-updates) at some point.

Tests
=====
There are tests under src/test/scala
You can run them as junit tests or from within sbt with "test"

Generally the master ui-mod and the master server should work together.

Feel free to ask me questions about this. If somebody actually reads until this point ;)