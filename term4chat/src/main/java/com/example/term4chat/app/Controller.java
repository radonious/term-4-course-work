package com.example.term4chat.app;

import com.example.term4chat.net.Client;
import com.example.term4chat.net.DtoObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;

/* Класс управления приложением */
public class Controller {
    boolean isConnected; /* Есть ли подключение к серверу */
    Client client; /* Экземпляр серверного клиента для работы по сети */

    /* FXML поля */
    @FXML
    private ListView<String> chatsListView;

    @FXML
    private ListView<String> membersListView;

    @FXML
    private ListView<String> usersListView;

    @FXML
    private Pane chatsPane;

    @FXML
    private VBox chatsVBox;

    @FXML
    private TextArea historyTextArea;

    @FXML
    private TextField messageTextField;

    @FXML
    private Pane rootPane;

    @FXML
    private Button sendBtn;

    @FXML
    private Pane usersPane;

    @FXML
    private VBox usersVBox;


    @FXML /* Ожидание нажатия ENTER для эмуляции нажатия кнопки Send */
    void rootPaneKeyPressed(KeyEvent event) {
        KeyCode pressedKey = event.getCode();
        if (pressedKey == KeyCode.ENTER) {
            System.out.println("Pressed Enter: Message sending...");
            sendBtnClick();
        }
    }

    @FXML /* Обработка нажатия кнопки Send */
    void sendBtnClick() {
        if (!messageTextField.getText().equals("")) {
            if (isConnected) {
                /* Отправка DTO о новом сообщении на сервер при наличии подключения */
                DtoObject DTO = DtoObject.builder()
                        .dtoType(DtoObject.dtoType.CLIENT_REQUEST)
                        .dtoObject(DtoObject.dtoObject.MESSAGE)
                        .fromName(client.getUserName())
                        .chat_message("[" + client.getUserName() + "] - " + messageTextField.getText() + "\n")
                        .chat_name(chatsListView.getSelectionModel().getSelectedItem())
                        .build();
                messageTextField.clear();
                client.sendDTO(DTO);
            } else {
                /* Дополнения текущей истории чата при отчутствии подклюения */
                if (historyTextArea.getText(0, 1).equals("\n"))
                    historyTextArea.deleteText(0, 1);
                historyTextArea.appendText(messageTextField.getText() + "\n");
                messageTextField.clear();
            }
        }
    }

    /* Инициализация первичных данных при входе */
    public void initialize() {
        historyTextArea.setText("\n");
        chatsListView.getItems().add("Group chat");
        chatsListView.getSelectionModel().select(0);
        usersListView.getItems().add("Disconnected");
        usersListView.getSelectionModel().select(0);
        membersListView.getItems().add("Disconnected");
        membersListView.getSelectionModel().select(0);

        isConnected = false;
    }

    /* Отображение подключенных пользователей */
    public void setConnections(ArrayList<String> clients, String you) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (usersListView.getItems() != null)
                    usersListView.getItems().clear();
                for (String client : clients) {
                    if (client.equals(you)) usersListView.getItems().add(client + " - You");
                    else usersListView.getItems().add(client);
                }
            }
        });
    }

    @FXML /* Установка соединения с сервером */
    void connectToServer() {
        if (isConnected) return; /* Отмена, если подключение уже существует */

        /* Инициализация сцены */
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        StackPane pane = new StackPane();

        TextField area_ip = new TextField();
        area_ip.setText("127.0.0.1");
        Label label_ip = new Label();
        label_ip.setText("          IP:");
        HBox box_ip = new HBox();
        box_ip.getChildren().addAll(label_ip, area_ip);
        box_ip.setSpacing(20);

        TextField area_port = new TextField();
        area_port.setText("8888");
        Label label_port = new Label();
        label_port.setText("  PORT:");
        HBox box_port = new HBox();
        box_port.getChildren().addAll(label_port, area_port);
        box_port.setSpacing(20);

        /* Обработка нажатия кнопок Connect и Close */
        Button btnConnect = new Button("Connect");
        btnConnect.setDefaultButton(true);
        btnConnect.setOnAction(actionEvent -> {
            client = new Client(this);
            isConnected = client.connect(area_ip.getText(), Integer.parseInt(area_port.getText()));
            if (isConnected) {
                client.start();
                /* Отправка сообщения о новом подключении */
                DtoObject DTO = DtoObject.builder()
                        .dtoType(DtoObject.dtoType.CLIENT_REQUEST)
                        .dtoObject(DtoObject.dtoObject.MESSAGE)
                        .fromName(client.getUserName())
                        .chat_message("New user joined to server.\n")
                        .chat_name("Group chat")
                        .build();
                client.sendDTO(DTO);
            } else {
                /* Сообщение об ошибке */
                popAlert("Can not connect");
            }
            stage.close();
        });
        Button btnClose = new Button("Close");
        btnClose.setOnAction(actionEvent -> {
            /* Закрытие приложения */
            stage.close();
        });
        HBox box_btn = new HBox();
        box_btn.getChildren().addAll(btnConnect, btnClose);
        box_btn.setAlignment(Pos.CENTER);
        box_btn.setSpacing(10);

        final VBox vbox = new VBox();
        vbox.getChildren().addAll(box_ip, box_port, box_btn);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(20);
        pane.getChildren().add(vbox);
        Scene scene = new Scene(pane, 250, 150);

        stage.setScene(scene);
        stage.setTitle("Connection");
        stage.showAndWait();
    }

    @FXML /* Отключение от сервера */
    void disconnectFromServer() {
        if (!isConnected) return;
        DtoObject DTO = DtoObject.builder()
                .dtoType(DtoObject.dtoType.CLIENT_REQUEST)
                .dtoObject(DtoObject.dtoObject.MESSAGE)
                .fromName(client.getUserName())
                .chat_message("[" + client.getUserName() + "] disconnected from server.\n")
                .chat_name("Group chat")
                .build();
        client.sendDTO(DTO);
        isConnected = client.disconnect();
        usersListView.getItems().clear();
        usersListView.getItems().add("Disconnected");
    }

    @FXML /* Отображение окна об ошибке */
    private void popAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning Dialog");
        alert.setHeaderText(msg);
        alert.showAndWait();
    }

    /* Обновление окна истории чата, если он открыт и пришло сообщение */
    public void updateChatIfCurrent(String chat_name) {
        if (chatsListView.getSelectionModel().getSelectedItem().equals(chat_name)) {
            Platform.runLater(() -> {
                historyTextArea.setText(client.getChatHistory(chat_name));
                historyTextArea.setScrollTop(Double.MAX_VALUE);
                membersListView.getItems().clear();
                membersListView.getItems().addAll(client.getChatUsers(chat_name));
            });
        }
    }

    @FXML /* Создение приватного чата */
    void createPrivateChat() {
        String companion = usersListView.getSelectionModel().getSelectedItem();
        if (companion.equals("Disconnected")) {
            popAlert("You are disconnected");
        } else if (companion.contains("You")) {
            popAlert("Can't create private chat with yourself");
        } else if (chatsListView.getItems().contains(client.getUserName() + " / " + companion + " chat") ||
                chatsListView.getItems().contains(companion + " / " + client.getUserName() + " chat")) {
            popAlert("Chat already exists");
        } else {
            chatsListView.getItems().add(client.getUserName() + " / " + companion + " chat");
            client.createChat(usersListView.getSelectionModel().getSelectedItem());
        }
    }

    @FXML /* Удаление приватного чата */
    void deletePrivateChat() {
        if (!chatsListView.getSelectionModel().getSelectedItem().equals("Group chat")) {
            client.deleteChat(chatsListView.getSelectionModel().getSelectedItem());
        } else {
            popAlert("Can't delete main chat");
        }
    }

    @FXML /* Отображение вабранного чата в окне истории чата */
    void loadSelectedChat() {
        if (isConnected) {
            historyTextArea.setText(client.getChatHistory(chatsListView.getSelectionModel().getSelectedItem()));
            historyTextArea.setScrollTop(Double.MAX_VALUE);
            membersListView.getItems().clear();
            membersListView.getItems().addAll(client.getChatUsers(chatsListView.getSelectionModel().getSelectedItem()));
        }
    }

    /* Добавление чата если его не существует (usage: если с пользователем создали приватный чат) */
    public void addChatIfNotExist(String name) {
        Platform.runLater(() -> {
            if (!chatsListView.getItems().contains(name))
                chatsListView.getItems().add(name);
        });
    }
    /* Удаление чата из списка чатов */
    public void removeChatFromList(String chat_name) {
        Platform.runLater(() -> {
            chatsListView.getItems().remove(chat_name);
            chatsListView.getSelectionModel().select(0);
            loadSelectedChat();
        });
    }

    @FXML /* Добавление нового клиента к чату где его нет */
    public void addToSelectedChat() {
        String chat_name = chatsListView.getSelectionModel().getSelectedItem();
        String user_name = usersListView.getSelectionModel().getSelectedItem();

        if (!isConnected) {
            popAlert("You are disconnected");
        } else if (client.getChatUsers(chat_name).contains(user_name) || user_name.contains("You")) {
            popAlert("User already joined");
        }  else {
            client.addUserToChat(user_name, chat_name);
        }
    }
}