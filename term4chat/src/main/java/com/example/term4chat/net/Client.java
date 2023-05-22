package com.example.term4chat.net;

import com.example.term4chat.app.Controller;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/* Класс клиентской сетевой части */
public class Client extends Thread {
    Controller clientController; /* Экземпляр контроллеря */
    int port; /* Порт подключения */
    String ip; /* IP адрес подлючения */
    Socket socket; /* Сокет связи с серверным потоком */
    String user_name; /* Имя текущего клиента на сервере */
    ArrayList<String> clients; /* Список текущих клиентов */
    HashMap<String, ChatData> chats; /* Список текущих чатов */
    ObjectInputStream in_obj; /* Поток получения объектов */
    ObjectOutputStream out_obj; /* Поток отправки объектов */

    /* Конструктор */
    public Client(Controller ctrl) {
        clientController = ctrl;
    }

    /* Геттеры */
    public String getUserName() {
        return user_name;
    }

    public ArrayList<String> getChatUsers(String chat_name) {
        return chats.get(chat_name).users;
    }

    public String getChatHistory(String chat_name) {
        return chats.get(chat_name).history;
    }

    /* Метод запуска потока */
    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                DtoObject clientDtoObject = (DtoObject) in_obj.readObject();
                handleDTO(clientDtoObject);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("\nClient stop listening: " + e);
                disconnect();
            }
        }
    }

    /* Создание соединения с сервером */
    public boolean connect(String ip, int port) {
        boolean result = false;
        this.ip = ip;
        this.port = port;
        System.out.println("Connecting to " + ip + ":" + port);
        try {
            socket = new Socket(ip, port);
            out_obj = new ObjectOutputStream(socket.getOutputStream());
            in_obj = new ObjectInputStream(socket.getInputStream());
            chats = new HashMap<>();
            System.out.println("Socket created. Connected to " + ip + ":" + port);
            result = true;
        } catch (IOException e) {
            System.out.println("Cannot connect: " + e);
        }
        return result;
    }

    /* Отключение от сервера */
    public boolean disconnect() {
        boolean result = true;
        System.out.println("Disconnecting...");
        try {
            in_obj.close();
            out_obj.close();
            socket.close();
            System.out.println("Socket " + socket.getRemoteSocketAddress() + " closed. Disconnected");
            result = false;
        } catch (IOException e) {
            System.out.println("Cannot disconnect: " + e);
        }
        return result;
    }

    /* Отправка DTO на сервер */
    public void sendDTO(DtoObject clientDtoObject) {
        System.out.println("Trying to send DTO...");
        try {
            out_obj.writeObject(clientDtoObject);
            out_obj.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("DTO was sent. Success");
    }

    /* Обработка полученных запросов */
    public void handleDTO(DtoObject DTO) throws FileNotFoundException {
        System.out.println("\nGot a new DTO. Handling...");
        System.out.println(DTO);
        if (DTO.getDtoType().equals(DtoObject.dtoType.SERVER_REPLY) &&
                DTO.getDtoObject().equals(DtoObject.dtoObject.CLIENT_LIST)) {
            user_name = DTO.getToName();
            clients = DTO.getConnected_clients();
            clientController.setConnections(DTO.getConnected_clients(), DTO.getToName());
        } else if (DTO.getDtoType().equals(DtoObject.dtoType.SERVER_REPLY) &&
                DTO.getDtoObject().equals(DtoObject.dtoObject.CHAT)) {
            clientController.addChatIfNotExist(DTO.getChat_name());
            chats.put(DTO.getChat_name(), new ChatData(DTO.getChat_history(), DTO.getChat_clients()));
            clientController.updateChatIfCurrent(DTO.getChat_name());
        } else if (DTO.getDtoType().equals(DtoObject.dtoType.SERVER_REPLY) &&
                DTO.getDtoObject().equals(DtoObject.dtoObject.CHAT_REMOVE)) {
            clientController.removeChatFromList(DTO.getChat_name());
            chats.remove(DTO.getChat_name());
        }
        System.out.println("DTO handled successfully");
    }

    /* Отправка запроса на создание чата */
    public void createChat(String companion) {
        DtoObject DTO = DtoObject.builder()
                .dtoType(DtoObject.dtoType.CLIENT_REQUEST)
                .dtoObject(DtoObject.dtoObject.CHAT)
                .fromName(user_name)
                .toName(companion)
                .build();
        sendDTO(DTO);
    }

    /* Отправка запроса на удадление чата */
    public void deleteChat(String chat_name) {
        DtoObject DTO = DtoObject.builder()
                .dtoType(DtoObject.dtoType.CLIENT_REQUEST)
                .dtoObject(DtoObject.dtoObject.CHAT_REMOVE)
                .chat_name(chat_name)
                .fromName(user_name)
                .build();
        sendDTO(DTO);
    }

    public void addUserToChat(String user_name, String chat_name) {
        DtoObject DTO = DtoObject.builder()
                .dtoType(DtoObject.dtoType.CLIENT_REQUEST)
                .dtoObject(DtoObject.dtoObject.CLIENT_LIST)
                .chat_name(chat_name)
                .toName(user_name)
                .fromName(this.user_name)
                .build();
        sendDTO(DTO);
    }
}