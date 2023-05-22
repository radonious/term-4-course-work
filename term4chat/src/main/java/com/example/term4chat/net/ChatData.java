package com.example.term4chat.net;

import lombok.AllArgsConstructor;

import java.util.ArrayList;

@AllArgsConstructor
/* Класс внутренней информации чата */
public class ChatData {
    public String history; /* История чата */
    public ArrayList<String> users; /* Текущие пользователи чата */
}