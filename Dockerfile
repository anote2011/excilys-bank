FROM tomcat:7
ADD excilys-bank-web/target/excilys-bank-web-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/excilys-bank.war
