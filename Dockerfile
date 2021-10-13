FROM jboss/wildfly

USER root

EXPOSE 8080
EXPOSE 9990

#COPY certs/login.jaqpot.org.cer $JAVA_HOME/jre/lib/security
#COPY certs/api.jaqpot.org.cer $JAVA_HOME/jre/lib/security
#COPY certs/*.prod.openrisknet.org.cer $JAVA_HOME/jre/lib/security

#RUN \
#    cd $JAVA_HOME/jre/lib/security \
#    && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias apijaqpot -file api.jaqpot.org.cer

#RUN \
#    cd $JAVA_HOME/jre/lib/security \
#    && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias jaqpotsso -file login.jaqpot.org.cer

#RUN \
#    cd $JAVA_HOME/jre/lib/security \
#    && keytool -keystore cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias openrisk -file *.prod.openrisknet.org.cer



ADD ./target/jaqpot-api-5.0.36.war /opt/jboss/wildfly/standalone/deployments/
ADD ./standalone/standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml


CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
