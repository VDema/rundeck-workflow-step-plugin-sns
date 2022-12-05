package com.plugin.sns;

import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.dtolabs.rundeck.plugins.util.PropertyBuilder;

import java.util.Map;

import static com.dtolabs.rundeck.core.Constants.INFO_LEVEL;

@Plugin(service = ServiceNameConstants.WorkflowStep, name = "sns")
@PluginDescription(title = "sns", description = "Send message in AWS SNS")
public class Sns implements StepPlugin, Describable {

    private static final String SERVICE_PROVIDER_NAME = "sns";

    /**
     * Overriding this method gives the plugin a chance to take part in building the {@link
     * com.dtolabs.rundeck.core.plugins.configuration.Description} presented by this plugin.  This subclass can use the
     * {@link DescriptionBuilder} to modify all aspects of the description, add or remove properties, etc.
     */
    @Override
    public Description getDescription() {
        return DescriptionBuilder.builder()
                .name(SERVICE_PROVIDER_NAME)
                .title("sns")
                .description("SNS step")
                .property(PropertyBuilder.builder()
                        .string("messageBody")
                        .title("Json message")
                        .description("This message will be sent once it's triggered")
                        .required(true)
                        .build()
                )
                .property(PropertyBuilder.builder()
                        .string("topic")
                        .title("Sns topic ARN")
                        .description("Sqn topic ARN where the message will be sent")
                        .required(true)
                        .build()
                )
                .build();
    }

    /**
     * Here is the meat of the plugin implementation, which should perform the appropriate logic for your plugin.
     * <p/>
     * The {@link PluginStepContext} provides access to the appropriate Nodes, the configuration of the plugin, and
     * details about the step number and context.
     */
    @Override
    public void executeStep(
            final PluginStepContext context,
            final Map<String, Object> configuration) {
        final PluginLogger logger = context.getLogger();
        logger.log(INFO_LEVEL, "Step configuration: " + configuration);
        logger.log(INFO_LEVEL, "Step num: " + context.getStepNumber());
        logger.log(INFO_LEVEL, "Step context: " + context.getStepContext());
        logger.log(INFO_LEVEL, "EC2ContainerCredentialsProviderWrapper");
        final String json = (String) configuration.get("messageBody");
        final String topicArn = (String) configuration.get("topic");
        sendToSns(topicArn, json);
    }

    public boolean sendToSns(final String topicArn, final String json) {
        final AmazonSNS snsClient = buildAmazonSNS(System.getenv("AWS_REGION"));
        final PublishRequest publishRequest = new PublishRequest(topicArn, generateMessage(json));
        final PublishResult publishResult = snsClient.publish(publishRequest);
        return true;
    }

    protected AmazonSNS buildAmazonSNS(final String region) {
        return AmazonSNSClientBuilder.standard()
                .withCredentials(new EC2ContainerCredentialsProviderWrapper())
                .withRegion(Regions.fromName(region))
                .build();
    }

    private String generateMessage(String json) {
        return json;
    }

}
