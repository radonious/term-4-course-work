package com.example.term4chat.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/* Класс серверного потока для свези с отдельным клиентом */
public class ServerThread extends Thread {

    int id; /* ID имя связанного клиента на сервере */
    String user_name; /* Имя связанного клиента */
    public Socket socket; /* Сокет связи с клиентом */
    private ObjectInputStream in_obj; /* Входной поток связи с клиентом  */
    private ObjectOutputStream out_obj; /* Выходной поток связи с клиентом */

    /* Конструктор */
    ServerThread(Socket socket, int id) throws IOException {
        this.setDaemon(true);
        this.socket = socket;
        try {
            out_obj = new ObjectOutputStream(socket.getOutputStream());
            in_obj = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.id = id;
        this.user_name = "User #" + id;
    }

    /* Запуск потока */
    public void run() {
        try {
            System.out.println("Thread started");
            while (!socket.isClosed()) {
                System.out.println("Client #" + id + " thread listening");
                DtoObject clientDTO = (DtoObject) in_obj.readObject();
                System.out.println("Received DTO from Client #" + id);
                handleDTO(clientDTO);
            }
        } catch (Exception e) {
            System.out.println("\nClient #" + id + " has been disconnected via: " + e);
        } finally {
            stopConnection();
            System.out.println("Client #" + id + " handle end. Thread closed");
        }

    }

    /* Обработка полученных запросов */
    public void handleDTO(DtoObject DTO) throws IOException {
        System.out.println("\nGot a new DTO. Handling...");
        System.out.println(DTO);
        if (DTO.getDtoType().equals(DtoObject.dtoType.CLIENT_REQUEST) && DTO.getDtoObject().equals(DtoObject.dtoObject.MESSAGE)) {
            Server.addMessageToChat(DTO.getChat_name(), DTO.getChat_message());
        } else if (DTO.getDtoType().equals(DtoObject.dtoType.CLIENT_REQUEST) && DTO.getDtoObject().equals(DtoObject.dtoObject.CHAT)) {
            Server.createNewChat(DTO.getFromName(), DTO.getToName());
        } else if (DTO.getDtoType().equals(DtoObject.dtoType.CLIENT_REQUEST) && DTO.getDtoObject().equals(DtoObject.dtoObject.CHAT_REMOVE)) {
            Server.removeChat(DTO.getChat_name());
        } else if (DTO.getDtoType().equals(DtoObject.dtoType.CLIENT_REQUEST) && DTO.getDtoObject().equals(DtoObject.dtoObject.CLIENT_LIST)) {
            Server.addClientToChat(DTO.getToName(), DTO.getChat_name());
        }
        System.out.println("DTO handled successfully");
    }

    /* Отключение от сокета и удаление потока на сервере */
    private void stopConnection() {
        try {
            Server.removeThread(this);
            Server.shareClients();
            in_obj.close();
            out_obj.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* Отправка DTO клиенту */
    public synchronized void sendDtoToClient(DtoObject clientDTO) {
        try {
            out_obj.writeObject(clientDTO);
            out_obj.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}