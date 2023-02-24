package edu.edi.arep.server;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;

public class HttpServer {

    private static HttpServer _instance = new HttpServer();
    public HttpServer(){}

    public static HttpServer getInstance() {
        return _instance;
    }

    public static void run(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String inputLine, outputLine = null;

            boolean status = true;
            String request = "";
            while ((inputLine = in.readLine()) != null) {
                if (status) {
                    request = inputLine.split(" ")[1];
                    status = false;
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }
            System.out.println(request);

            if (request.startsWith("/consulta")) {
                String temp = request.replace("%20", "");
                System.out.println(temp);
                String command = temp.split("\\(")[0].split("=")[1];
                System.out.println(command);
                String className = temp.split("\\(")[1].replace(")", "");
                if (command.equals("Class")) {
                    System.out.println(className);
                    //System.out.println(classMethods(className));
                    outputLine = classMethods(className);
                } else if (command.equals("invoke")) {
                    String method = temp.split("\\(")[1].split(",")[1].replace(")", "");
                    System.out.println(method);
                    outputLine = invokeMethod(className, method);

                }
            } else {
                outputLine = htmlGetForm();
            }

            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    public static String classMethods(String className) throws ClassNotFoundException {
        Class<?> c = Class.forName(className);
        Field[] classFields = c.getFields();
        Method[] classMethods = c.getMethods();
        String fields = "";
        String methods = "";
        for (Method m : classMethods) {
            //System.out.println(m);
            methods += m + " ";
        }
        for (Field f : classFields) {
            //System.out.println(m);
            fields += f + " ";
        }
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n" + "Fields: \n\n" + fields + "Methods: \n\n" + methods;
    }

    public static String invokeMethod(String className, String method) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> c = Class.forName(className.split(",")[0]);
        Method classMethod = c.getMethod(method);
        String content = "" + classMethod.invoke(null);

        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + content;
    }

    public static String htmlClass(String content) {
        return  "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + content;
    }

    public static String htmlGetForm() {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-type: text/html\r\n" +
                "\r\n" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <title>Reflective ChatGPT</title>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1>Reflective ChatGPT</h1>\n" +
                "        <form action=\"/consulta\">\n" +
                "            <label for=\"command\">Command:</label><br>\n" +
                "            <input type=\"text\" id=\"command\" command=\"command\" value=\"John\"><br><br>\n" +
                "            <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n" +
                "        </form> \n" +
                "        <div id=\"getrespmsg\"></div>\n" +
                "\n" +
                "        <script>\n" +
                "            function loadGetMsg() {\n" +
                "                let nameVar = document.getElementById(\"command\").value;\n" +
                "                const xhttp = new XMLHttpRequest();\n" +
                "                xhttp.onload = function() {\n" +
                "                    document.getElementById(\"getrespmsg\").innerHTML =\n" +
                "                    this.responseText;\n" +
                "                }\n" +
                "                xhttp.open(\"GET\", \"/consulta?comando=\"+nameVar);\n" +
                "                xhttp.send();\n" +
                "            }\n" +
                "        </script>\n" +
                "\n" +
                "    </body>\n" +
                "</html>";
    }

}
