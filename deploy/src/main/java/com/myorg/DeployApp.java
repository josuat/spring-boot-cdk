package com.myorg;

import software.amazon.awscdk.core.App;

public class DeployApp {
    public static void main(final String[] args) {
        App app = new App();

        new DeployStack(app, "user-api-stack");

        app.synth();
    }
}
