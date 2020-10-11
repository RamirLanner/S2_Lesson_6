package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import java.util.Scanner;

public class Client {
    private static final Scanner scanner = new Scanner(System.in);
    private static volatile boolean serverOff;

    public static void main(String[] args) {
        System.out.println("Client command list:\n 1) /end-> program close.");
        try {
            Socket clientSocket = new Socket("localhost", 8189);
            System.out.println("The connection is established.");
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            out.writeUTF("Hello server! I am Client!");

            Thread inStream = new Thread(() -> {
                while (!serverOff) {
                    try {
                        String msg = in.readUTF();
                        System.out.println("Server broadcast message: " + msg);
                        if (msg.equals("/end")) {
                            serverOff = true;
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            });

            inStream.setDaemon(true);
            inStream.start();

            while (!serverOff) {
                String terminateMsg = scanner.nextLine();
                if (serverOff) {
                    disconnectedServer(clientSocket);
                    //scanner.close();
                    break;
                }
                if (terminateMsg.equals("/end")) {
                    out.writeUTF("/end");
                    clientSocket.close();
                    break;
                }
                out.writeUTF(terminateMsg);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void disconnectedServer(Socket socket) throws IOException {
        socket.close();
        System.out.println("Server was been disabled. Input something in the console and the program will close.");
    }

}
