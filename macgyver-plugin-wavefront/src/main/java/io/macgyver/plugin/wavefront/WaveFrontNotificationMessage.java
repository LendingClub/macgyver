package io.macgyver.plugin.wavefront;

import io.macgyver.core.event.MacGyverMessage;

public class WaveFrontNotificationMessage extends MacGyverMessage {

	public static class DeploymentNotificationMessage extends WaveFrontNotificationMessage {

	}

	public static class IncidentNotificationMessage extends WaveFrontNotificationMessage {

	}

}
