package com.example.term4chat.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/* Класс серверной сетевой части */
public class Server {
    private static final int port = 8888; /* Порт для запуска сервера сервера */
    static ArrayList<ServerThread> threads; /* Список потоков связанных с клиентами */
    static ServerSocket server_socket; /* Серверный сокет */
    static ArrayList<String> clients; /* Список имен подключенных клиентов */
    static HashMap<String, ChatData> chats; /* Коллекция чатов <Название, Данные> */

    /* Ручной запуск сервера и инициализация полей */
    public static void main(String[] args) throws Exception {
        server_socket = new ServerSocket(port);
        threads = new ArrayList<>();
        clients = new ArrayList<>();
        chats = new HashMap<>();
        chats.put("Group chat", new ChatData("", new ArrayList<>()));
        int user_id = 0;

        /* Цикл отслеживания новых подключений */
        while (!server_socket.isClosed()) {
            Socket client_socket = server_socket.accept();
            ++user_id;
            System.out.println("\nA new client #" + user_id + " is connected to " + client_socket.getLocalSocketAddress());
            ServerThread thread = new ServerThread(client_socket, user_id);
            clients.add("User #" + user_id);
            threads.add(thread);
            chats.get("Group chat").users.add("User #" + user_id);
            thread.start();
            shareClients();
            shareChat("Group chat");
        }
    }

    /* Рассылка нового списка, при обновлении списка пользователей */
    public static synchronized void shareClients() throws IOException {
        System.out.println("Sharing updated clients list..");
        ArrayList<String> names = new ArrayList<>(clients);
        for (ServerThread th : threads) {
            String name = th.user_name;
            DtoObject Dto = DtoObject.builder()
                    .dtoType(DtoObject.dtoType.SERVER_REPLY)
                    .dtoObject(DtoObject.dtoObject.CLIENT_LIST)
                    .toName(name)
                    .connected_clients(names)
                    .build();
            th.sendDtoToClient(Dto);
        }
    }

    /* Рассылка новой истории чата при получении соообщения */
    public static synchronized void shareChat(String chat_name) throws IOException {
        System.out.println("Sharing chat : " + chat_name + "...");
        for (ServerThread th : threads) {
            String name = th.user_name;
            if (chats.get(chat_name).users.contains(name)) {
                String new_chat_history = chats.get(chat_name).history;
                ArrayList<String> new_char_users = new ArrayList<>(chats.get(chat_name).users);
                DtoObject Dto = DtoObject.builder()
                        .dtoType(DtoObject.dtoType.SERVER_REPLY)
                        .dtoObject(DtoObject.dtoObject.CHAT)
                        .toName(name)
                        .chat_name(chat_name)
                        .chat_history(new_chat_history)
                        .chat_clients(new_char_users)
                        .build();
                th.sendDtoToClient(Dto);
            }
        }
    }

    /* Удаление потока при его закрытии */
    public static synchronized void removeThread(ServerThread thread) {
        threads.remove(thread);
        clients.remove(thread.user_name);
        chats.forEach((name, data) -> {
            data.users.remove(thread.user_name);
        });
    }

    /* Добавление сообщения в чат */
    public static synchronized void addMessageToChat(String chat_name, String msg) throws IOException {
        chats.get(chat_name).history += msg;
        shareChat(chat_name);
    }

    /* Удаление пользователя из чата */
    public static synchronized void removeUserFromChat(String chat_name, String user) throws IOException {
        chats.get(chat_name).users.remove(user);
        shareChat(chat_name);
    }

    /* Создание нового чата */
    public static synchronized void createNewChat(String fromName, String toName) throws IOException {
        String name = fromName + " / " + toName + " chat";
        chats.put(name, new ChatData("", new ArrayList<>()));
        chats.get(name).users.add(fromName);
        chats.get(name).users.add(toName);
        shareChat(name);
    }

    /* Удаление существующего чата */
    public static synchronized void removeChat(String chat_name) {
        for (ServerThread th : threads) {
            String name = th.user_name;
            if (chats.get(chat_name).users.contains(name)) {
                DtoObject DTO = DtoObject.builder()
                        .dtoType(DtoObject.dtoType.SERVER_REPLY)
                        .dtoObject(DtoObject.dtoObject.CHAT_REMOVE)
                        .toName(name)
                        .chat_name(chat_name)
                        .build();
                th.sendDtoToClient(DTO);
            }
        }
        chats.remove(chat_name);
    }

    public static synchronized void addClientToChat(String user_name, String chat_name) throws IOException {
        chats.get(chat_name).users.add(user_name);
        shareChat(chat_name);
    }
}