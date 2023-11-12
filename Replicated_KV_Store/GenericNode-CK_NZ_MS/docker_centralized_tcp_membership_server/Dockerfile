FROM ubuntu
RUN apt-get update
RUN apt-get install -y default-jre
RUN apt-get install -y net-tools
COPY GenericNode.jar /
COPY run_tcp_membership_server.sh /
ENTRYPOINT ["/run_tcp_membership_server.sh"]

