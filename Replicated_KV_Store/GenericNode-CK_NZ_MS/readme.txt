CODES TO IDENTIFY MEMBERSHIP TRACKING METHODS

F Static file membership tracking – file is not reread
FD Static file membership tracking DYNAMIC - file is read every time getNodes() is called by nodes in a way it is dynamic
T TCP membership tracking – servers are configured to refer to central membership server

------------------------------------------------------------------------------------------------------

Programming Language: JAVA 11
IDE: IntelliJ
Build: mvn verify

------------------------------------------------------------------------------------------------------


# Starting of TCP based centralized node registry membership server
# java -jar GenericNode.jar ts_cs <portNumber>

java -jar GenericNode.jar ts_cs 1234



------------------------------------------------------------------------------------------------------

# Starting of TCP Serer Node that can register/deregister dynamically to tcp based centralized node registery membership server
# java -jar GenericNode.jar ts <portNumberOf_TCP_SERVER> tcp <hostNameOf_centralized_node_registry_membership_server> <portNumber_centralized_node_registry_membership_server>

java -jar GenericNode.jar ts 1235 tcp localhost 1234


------------------------------------------------------------------------------------------------------

# client commands the applies to all servers including centralized membership server and tcp server nodes. They are similar to assignment one. Centralized membership server wont broadcast messages for put and del. Only TCP Server broadcasts the messages

java -jar GenericNode.jar tc localhost 1235 put a 25

java -jar GenericNode.jar tc localhost 1235 get a

java -jar GenericNode.jar tc localhost 1235 del a

java -jar GenericNode.jar tc localhost 1235 store

java -jar GenericNode.jar tc localhost 1235 exit


------------------------------------------------------------------------------------------------------------------------------
# Please dont try below commands, these are broadcast commands sent by each TCP Server node when they get put or del commands
# These commands are described for reference of what kind of distributed commands are published by TCP Server


java -jar GenericNode.jar tc localhost 1235 dput1 a 25

java -jar GenericNode.jar tc localhost 1235 dput2 a 25

java -jar GenericNode.jar tc localhost 1235 dputabort a 25




java -jar GenericNode.jar tc localhost 1235 ddel1 a

java -jar GenericNode.jar tc localhost 1235 ddel2 a

java -jar GenericNode.jar tc localhost 1235 ddelabort a

------------------------------------------------------------------------------------------------------------------------------






