System-wide configuration:

Modify the file standalone-full.xml
which is typically found at 
{WILDFLY_BASE_PATH}/standalone/configuration/standalone-full.xml

add the following inside the tag <jms-destinations>:

<jms-destinations>
    <jms-queue name="ExpiryQueue">
        <entry name="java:/jms/queue/ExpiryQueue"/>
    </jms-queue>
    <jms-queue name="DLQ">
        <entry name="java:/jms/queue/DLQ"/>
    </jms-queue>
    <jms-topic name="training">
        <entry name="jms/topic/training"/>
        <entry name="java:jboss/exported/jms/topic/training"/>
    </jms-topic>
    <jms-topic name="prediction">
        <entry name="jms/topic/prediction"/>
        <entry name="java:jboss/exported/jms/topic/prediction"/>
    </jms-topic>
    <jms-topic name="preparation">
       <entry name="jms/topic/preparation"/>
       <entry name="java:jboss/exported/jms/topic/preparation"/>
    </jms-topic> 
</jms-destinations>

an example standalone-full.xml file is found in this directory.

An example settings can be also found here.