package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    private static final int SERVER_PORT = 8189;//49155
    private static volatile boolean serverOff;
    private static List<Socket> clientsArr = new ArrayList<>();
    static volatile Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Server command list:\n 1) /off -> serverSocket.close()");
        System.out.println("---***---");
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            Thread listenerThread = new Thread(() -> {
                try {
                    while (!serverOff){
                        Socket clientSocket = serverSocket.accept();
                        clientsArr.add(clientSocket);
                        listenerConnection(clientSocket);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Не получилось подключить клиента к серверу");
                }
            }, "listenerConnection");


            Thread commandLineThread = new Thread(() -> {
                while (!serverOff) {
                    //Scanner scanner = new Scanner(System.in);
                    String com = scanner.nextLine();
                    if (com.equals("/off")) {
                        serverOff = true;
                        sendMsgAllClient("/end");
                        //closeClientsSockets();
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                        sendMsgAllClient(com);
                }
            }, "commandLineThread");

            listenerThread.setDaemon(true);
            commandLineThread.start();
            listenerThread.start();

            listenerThread.join();
            commandLineThread.join();

        } catch (IOException | InterruptedException e) {/**/
            e.printStackTrace();
        }

    }

    private static void listenerConnection(Socket clientSocket) /*throws IOException*/ {
            new Thread(() -> {
                System.out.println("Client has been connected.");
                try {
                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                    while (true) {
                        String msg = in.readUTF();
                        System.out.println("Receive msg: " + msg);
                        if (msg.equals("/end")) {
                            //close client connection
                            clientSocket.close();// нужно ли закрывть??(программа может работать и без этой строчки)
                            clientsArr.remove(clientSocket);
                            System.out.println("Client disconnected.");
                            break;
                        }
                        if(msg.equals("/off")){
                            serverOff =true;
                            System.out.println("Server was closed.");
                        }
                        //out.writeUTF("Echo: " + msg);
                        //out.writeUTF("Echo: " + scanner.nextLine());
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
    }

//    private static void closeClientsSockets() throws IOException {
//        for (Socket socket : clientsArr) {
//            clientsArr.remove(socket);
//            socket.close();
//        }
//    }

    private static void sendMsgAllClient(String msg){
        try {
            for (Socket socket : clientsArr) {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(msg);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
