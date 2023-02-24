package edu.edi.arep.app;

import edu.edi.arep.server.HttpServer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        HttpServer server = HttpServer.getInstance();
        server.run(args);
    }
}