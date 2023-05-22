module com.example.term4chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;


//    opens com.example.term4chat to javafx.fxml;
//    exports com.example.term4chat;
    exports com.example.term4chat.net;
    opens com.example.term4chat.net to javafx.fxml;
    exports com.example.term4chat.app;
    opens com.example.term4chat.app to javafx.fxml;
}