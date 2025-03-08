module com.example.cardgame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.scripting;
    requires java.net.http;
    requires org.json;


    opens com.example.cardgame to javafx.fxml;
    exports com.example.cardgame;
}