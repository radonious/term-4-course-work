package com.example.term4chat.net;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@Builder
@ToString
@AllArgsConstructor
/* DTO класс для осуществления связи между клинтом и сервером */
public class DtoObject implements Serializable {
    /* Типизация */
    private dtoType dtoType; /* Тип сообщения */
    private dtoObject dtoObject; /* Объект сообщения */
    /* Адресация */
    private String fromName; /* Имя отправителя */
    private String toName; /* Имя получателя */
    /* Чат */
    private String chat_name; /* Имя чата */
    private String chat_history; /* История чата */
    private ArrayList<String> chat_clients; /* Пользователи чата */
    private String chat_message; /* Сообщения */
    /* Сервер */
    private ArrayList<String> connected_clients; /* Список подключенных клиентов */

    public enum dtoType {
        CLIENT_REQUEST,
        CLIENT_REPLY,
        SERVER_REQUEST,
        SERVER_REPLY,
        SERVER_ERROR;
    }

    public enum dtoObject {
        CLIENT_LIST,
        CLIENT_REMOVE,
        MESSAGE,
        MESSAGE_REMOVE,
        CHAT,
        CHAT_REMOVE;
    }
}