package com.plugin.sns

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import com.dtolabs.rundeck.plugins.PluginLogger
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import spock.lang.Specification

class SnsSpec extends Specification {

    private static final String JSON = "{ \"id\" : \"2\"}"
    def awsSnsClient = Mock(AmazonSNS)

    def "run OK"() {
        given:
            def sns = new Sns() {
                @Override
                protected AmazonSNS buildAmazonSNS(String region) {
                    return awsSnsClient
                }
            }
            def logger = Mock(PluginLogger)
            def context = getContext(logger)
            def configuration = [
                    messageBody: JSON,
                    topic      : "rundeckSns"
            ]

        when:
            sns.executeStep(context, configuration)

        then:
            1 * awsSnsClient.publish(new PublishRequest("rundeckSns", JSON))
    }

    def getContext(PluginLogger logger) {
        Mock(PluginStepContext) {
            getLogger() >> logger
        }
    }

}
