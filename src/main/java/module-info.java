module com.example.practicasemana3 {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.practicasemana3 to javafx.fxml;
    exports com.example.practicasemana3;
}