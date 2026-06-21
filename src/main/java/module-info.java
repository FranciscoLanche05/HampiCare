module com.hampicare {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.hampicare to javafx.fxml;
    exports com.hampicare;
}