package net.adoptium.marketplace.client;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.nio.file.Files;

public class TestServer implements BeforeAllCallback, AfterAllCallback {

    public static Server server;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {

        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8090);
        server.addConnector(connector);

        ResourceHandler resource_handler = new ResourceHandler();

        String repo = searchForRepo("exampleRepositories");
        resource_handler.setBaseResourceAsString(new File(repo).getAbsolutePath());

        // Using ContextHandlerCollection instead of HandlerList
        ContextHandlerCollection handlers = new ContextHandlerCollection();
        handlers.setHandlers(new Handler[]{resource_handler});

        // Set DefaultHandler directly on the server
        server.setDefaultHandler(new DefaultHandler());
        
        server.setHandler(handlers);
        server.start();
    }

    private String searchForRepo(String exampleRepositories) {
        for (int i = 0; i < 10; i++) {
            if (Files.exists(new File(exampleRepositories).toPath())) {
                return exampleRepositories;
            }
            exampleRepositories = "../" + exampleRepositories;
        }
        return null;
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        server.stop();
    }

    public static void main(String[] args) throws Exception {
        new TestServer().beforeAll(null);
        Thread.sleep(Long.MAX_VALUE);
    }
}
