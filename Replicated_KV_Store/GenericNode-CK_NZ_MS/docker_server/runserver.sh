#!/bin/bash
# Dummy jar file
#java -jar GenericNode.jar

centralizedTCPMembershipServer=localhost
centralizedTCPMembershipPort=1234

java -jar GenericNode.jar ts 1235 tcp $centralizedTCPMembershipServer $centralizedTCPMembershipPort

#java -jar GenericNode.jar ts 1236 tcp $centralizedTCPMembershipServer $centralizedTCPMembershipPort

#java -jar GenericNode.jar ts 1237 tcp $centralizedTCPMembershipServer $centralizedTCPMembershipPort

#java -jar GenericNode.jar ts 1238 tcp $centralizedTCPMembershipServer $centralizedTCPMembershipPort

#java -jar GenericNode.jar ts 1239 tcp $centralizedTCPMembershipServer $centralizedTCPMembershipPort