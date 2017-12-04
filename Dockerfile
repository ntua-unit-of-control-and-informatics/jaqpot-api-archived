FROM jboss/wildfly

USER root

EXPOSE 8080
EXPOSE 9990

ADD ./target/jaqpot-api-4.0.3.war /opt/jboss/wildfly/standalone/deployments/
ADD ./standalone/standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml


CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
