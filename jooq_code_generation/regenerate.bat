rmdir /s /q ..\src\main\java\info
java -cp jooq-3.2.0.jar;jooq-meta-3.2.0.jar;jooq-codegen-3.2.0.jar;postgresql-9.2-1003.jdbc4.jar; org.jooq.util.GenerationTool /conf.xml