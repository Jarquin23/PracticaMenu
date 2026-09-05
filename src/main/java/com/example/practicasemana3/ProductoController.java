package com.example.practicasemana3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductoController implements Initializable {

    @FXML private TextField txtCode;
    @FXML private TextField txtName;
    @FXML private TextField txtCategory;
    @FXML private TextField txtPrice;
    @FXML private TextField txtStock;
    @FXML private TextField txtPreparation;

    @FXML private TableView<Producto> tvProducts;
    @FXML private TableColumn<Producto, String> colCode;
    @FXML private TableColumn<Producto, String> colName;
    @FXML private TableColumn<Producto, String> colCategory;
    @FXML private TableColumn<Producto, Double> colPrice;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, String> colPreparation;

    @FXML private Label lblStatus;

    @FXML private MenuItem newMenu;
    @FXML private MenuItem cleanMenu;
    @FXML private MenuItem menuSalir;
    @FXML private MenuItem menuGuardar;
    @FXML private MenuItem menuEditar;
    @FXML private MenuItem menuEliminar;
    @FXML private MenuItem menuAbout;

    @FXML private Button btnNew;
    @FXML private Button btnSave;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private ObservableList<Producto> productosList;
    private Producto productoSeleccionado;
    private boolean editando = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productosList = FXCollections.observableArrayList();

        colCode.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colPreparation.setCellValueFactory(new PropertyValueFactory<>("preparacion"));

        tvProducts.setItems(productosList);

        configurarAtajosTeclado();

        configurarContextMenu();
        tvProducts.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        productoSeleccionado = newSelection;
                        cargarProductoEnCampos(newSelection);
                        actualizarEstadoBotones(false);
                    } else {
                        productoSeleccionado = null;
                        limpiarCampos();
                        actualizarEstadoBotones(false);
                    }
                }
        );
        actualizarEstadoBotones(false);
        lblStatus.setText("Sistema listo. Agregue un nuevo producto o seleccione uno existente.");
        agregarProductosEjemplo();
    }

    @FXML
    private void nuevoProducto() {
        limpiarCampos();
        editando = false;
        productoSeleccionado = null;
        tvProducts.getSelectionModel().clearSelection();
        actualizarEstadoBotones(false);
        lblStatus.setText("Nuevo producto. Complete los campos y presione Guardar.");
        txtCode.requestFocus();
    }

    @FXML
    private void guardarProducto() {
        if (!validarCampos()) {
            return;
        }
        try {
            String codigo = txtCode.getText();
            String nombre = txtName.getText();
            String categoria = txtCategory.getText();
            double precio = Double.parseDouble(txtPrice.getText());
            int existencia = Integer.parseInt(txtStock.getText());
            String preparacion = txtPreparation.getText();
            if (editando && productoSeleccionado != null) {
                productoSeleccionado.setCodigo(codigo);
                productoSeleccionado.setNombre(nombre);
                productoSeleccionado.setCategoria(categoria);
                productoSeleccionado.setPrecio(precio);
                productoSeleccionado.setExistencia(existencia);
                productoSeleccionado.setPreparacion(preparacion);
                tvProducts.refresh();
                lblStatus.setText("Producto editado exitosamente: " + nombre);
                editando = false;
                productoSeleccionado = null;
                actualizarEstadoBotones(false);
            } else {
                boolean existe = productosList.stream().anyMatch(p -> p.getCodigo().equals(codigo));
                if (existe) {
                    mostrarAlerta("Código duplicado",
                            "Ya existe un producto con el código " + codigo + ". Use un código diferente.",
                            Alert.AlertType.WARNING);
                    return;
                }
                Producto nuevoProducto = new Producto(codigo, nombre, categoria, precio, existencia, preparacion);
                productosList.add(nuevoProducto);
                lblStatus.setText("Producto guardado exitosamente: " + nombre);
            }
            limpiarCampos();
            actualizarEstadoBotones(false);
            tvProducts.getSelectionModel().clearSelection();
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de formato",
                    "Por favor, verifique que Precio sea un número decimal y Existencia sea un número entero.",
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void editarProducto() {
        if (productoSeleccionado == null) {
            mostrarAlerta("Sin selección",
                    "Debe seleccionar un producto en la tabla para editarlo.",
                    Alert.AlertType.WARNING);
            return;
        }
        editando = true;
        cargarProductoEnCampos(productoSeleccionado);
        actualizarEstadoBotones(true);
        lblStatus.setText("Editando producto: " + productoSeleccionado.getNombre() +
                " (modifique los campos y presione Guardar)");
        txtCode.requestFocus();
    }

    @FXML
    private void eliminarProducto() {
        if (productoSeleccionado == null) {
            mostrarAlerta("Sin selección",
                    "Debe seleccionar un producto en la tabla para eliminarlo.",
                    Alert.AlertType.WARNING);
            return;
        }
        if (!productosList.contains(productoSeleccionado)) {
            mostrarAlerta("Error",
                    "El producto ya fue eliminado.",
                    Alert.AlertType.ERROR);
            productoSeleccionado = null;
            tvProducts.getSelectionModel().clearSelection();
            actualizarEstadoBotones(false);
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar el producto?");
        confirmacion.setContentText("Producto: " + productoSeleccionado.getNombre() +
                "\nCódigo: " + productoSeleccionado.getCodigo());
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            String nombreProducto = productoSeleccionado.getNombre();
            // Eliminar
            productosList.remove(productoSeleccionado);
            // Limpiar
            productoSeleccionado = null;
            tvProducts.getSelectionModel().clearSelection();
            limpiarCampos();
            actualizarEstadoBotones(false);

            lblStatus.setText("Producto eliminado: " + nombreProducto);
        }
    }

    @FXML
    private void limpiarProductos() {
        if (productosList.isEmpty()) {
            lblStatus.setText("No hay productos para limpiar.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar limpieza");
        confirmacion.setHeaderText("¿Eliminar todos los productos?");
        confirmacion.setContentText("Esta acción eliminará todos los productos del inventario.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            productosList.clear();
            limpiarCampos();
            productoSeleccionado = null;
            tvProducts.getSelectionModel().clearSelection();
            actualizarEstadoBotones(false);
            lblStatus.setText("Inventario limpiado. Todos los productos eliminados.");
        }
    }

    @FXML
    private void verDetalleProducto() {
        if (productoSeleccionado == null) {
            mostrarAlerta("Sin selección",
                    "Debe seleccionar un producto en la tabla para ver su detalle.",
                    Alert.AlertType.WARNING);
            return;
        }
        String detalle = "DETALLE DEL PRODUCTO\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "Código: " + productoSeleccionado.getCodigo() + "\n" +
                "Nombre: " + productoSeleccionado.getNombre() + "\n" +
                "Categoría: " + productoSeleccionado.getCategoria() + "\n" +
                "Precio: C$" + String.format("%.2f", productoSeleccionado.getPrecio()) + "\n" +
                "Existencia: " + productoSeleccionado.getExistencia() + " unidades\n" +
                "Preparación: " + productoSeleccionado.getPreparacion() + "\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━";
        Alert detalleAlert = new Alert(Alert.AlertType.INFORMATION);
        detalleAlert.setTitle("Detalle del Producto");
        detalleAlert.setHeaderText("Información completa del producto");
        detalleAlert.setContentText(detalle);
        detalleAlert.showAndWait();
    }

    @FXML
    private void acercaDe() {
        Alert acercaDe = new Alert(Alert.AlertType.INFORMATION);
        acercaDe.setTitle("Acerca de");
        acercaDe.setHeaderText("Distribuidora El Güegüense - Sistema de Inventario");
        acercaDe.setContentText("Versión 1.0\n" +
                "Desarrollado por: Alfredo Jarquín\n" +
                "Curso: Programación de Aplicaciones de escritorio\n" +
                "Fecha: 2026\n\n" +
                "Sistema de gestión de inventario.");
        acercaDe.showAndWait();
    }

    @FXML
    private void salirAplicacion() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar salida");
        confirmacion.setHeaderText("¿Desea salir de la aplicación?");
        confirmacion.setContentText("Todos los datos no guardados se perderán.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    private void limpiarCampos() {
        txtCode.clear();
        txtName.clear();
        txtCategory.clear();
        txtPrice.clear();
        txtStock.clear();
        txtPreparation.clear();
    }

    private void cargarProductoEnCampos(Producto producto) {
        txtCode.setText(producto.getCodigo());
        txtName.setText(producto.getNombre());
        txtCategory.setText(producto.getCategoria());
        txtPrice.setText(String.valueOf(producto.getPrecio()));
        txtStock.setText(String.valueOf(producto.getExistencia()));
        txtPreparation.setText(producto.getPreparacion());
    }

    private boolean validarCampos() {
        if (txtCode.getText().isEmpty() || txtName.getText().isEmpty() ||
                txtCategory.getText().isEmpty() || txtPrice.getText().isEmpty() ||
                txtStock.getText().isEmpty() || txtPreparation.getText().isEmpty()) {
            mostrarAlerta("Campos incompletos",
                    "Todos los campos son obligatorios. Por favor, complete la información.",
                    Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void actualizarEstadoBotones(boolean editando) {
        boolean haySeleccion = productoSeleccionado != null;
        // Menús
        if (menuEditar != null) menuEditar.setDisable(!haySeleccion);
        if (menuEliminar != null) menuEliminar.setDisable(!haySeleccion);
        // Botones
        if (btnEdit != null) btnEdit.setDisable(!haySeleccion);
        if (btnDelete != null) btnDelete.setDisable(!haySeleccion);
        // Siempre habilitados
        if (btnNew != null) btnNew.setDisable(false);
        if (btnSave != null) btnSave.setDisable(false);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void configurarContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem itemEditar = new MenuItem("Editar producto");
        itemEditar.setOnAction(e -> editarProducto());
        MenuItem itemEliminar = new MenuItem("Eliminar producto");
        itemEliminar.setOnAction(e -> eliminarProducto());
        MenuItem itemDetalle = new MenuItem("Ver detalle");
        itemDetalle.setOnAction(e -> verDetalleProducto());
        contextMenu.getItems().addAll(itemEditar, itemEliminar, new SeparatorMenuItem(), itemDetalle);
        tvProducts.setContextMenu(contextMenu);
    }

    private void configurarAtajosTeclado() {
        newMenu.setAccelerator(new javafx.scene.input.KeyCodeCombination(KeyCode.N,
                javafx.scene.input.KeyCombination.CONTROL_DOWN));
        menuGuardar.setAccelerator(new javafx.scene.input.KeyCodeCombination(KeyCode.G,
                javafx.scene.input.KeyCombination.CONTROL_DOWN));
        menuSalir.setAccelerator(new javafx.scene.input.KeyCodeCombination(KeyCode.Q,
                javafx.scene.input.KeyCombination.CONTROL_DOWN));
    }

    private void agregarProductosEjemplo() {
        productosList.add(new Producto("P001", "Café Don José", "Bebidas", 150.00, 50, "Café tostado natural"));
        productosList.add(new Producto("P002", "Queso Palacio", "Lácteos", 85.50, 30, "Queso fresco artesanal"));
        productosList.add(new Producto("P003", "Gallo Pinto", "Alimentos", 45.00, 100, "Mezcla de arroz y frijoles"));
        productosList.add(new Producto("P004", "Salsa Rosquilla", "Salsas", 30.00, 75, "Salsa tradicional nica"));
        productosList.add(new Producto("P005", "Maduro Horneado", "Alimentos", 60.00, 40, "Plátano maduro horneado con queso"));
        lblStatus.setText("Productos de ejemplo cargados. ¡Listo para trabajar!");
    }
}