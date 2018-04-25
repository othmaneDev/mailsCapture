How to use the application : 

First of all, you need to configure the application so that it can connect to the server with the right credentials and the desired protocol.
This information must be filled in the "src / main / java / resources / application.properties" file.

Once the application is configured, we need to launch this mavn command :

mvn clean package -> A jar that can be executed will be placed on the target directory of the project

Finally, to test the application, use the following command :

java -jar mails-capture-1.0-SNAPSHOT.jar

