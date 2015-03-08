System-wide configuration:

Modify the file standalone-full.xml
which is typically found at 
{WILDFLY_BASE_PATH}/standalone/configuration/standalone-full.xml

add the following inside the tag <jms-destinations>:

<jms-topic name="training">
    <entry name="jms/topic/training"/>
    <entry name="java:jboss/exported/jms/topic/training"/>
</jms-topic>
<jms-topic name="prediction">
    <entry name="jms/topic/prediction"/>
    <entry name="java:jboss/exported/jms/topic/prediction"/>
</jms-topic>

and example standalone-full.xml file is found in this directory.