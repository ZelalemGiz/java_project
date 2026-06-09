package com.pharmacy.controller;

import com.pharmacy.dao.DashboardDAO;
import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.dao.PharmacistDAO;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Pharmacist;
import com.pharmacy.util.AlertUtil;
import com.pharmacy.util.UserSession;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML
    private BorderPane mainPane;
    @FXML
    private ImageView userImage;
    @FXML
    private Circle imageBorder;
    @FXML
    private Label welcomeLabel, roleLabel;
    @FXML
    private ScrollPane pane;
    @FXML
    private Button btnMedicine, btnPOS, btnInventory, btnPharmacists, btnReports;

    @FXML
    private Label medicinesCountLabel, totalMedicinePriceLabel, salesLabel, lowStockLabel, expiryMedicineLabel;
    @FXML
    private VBox medicineAmount, MedicinePriceLabel, lowStock;
    @FXML
    private LineChart<String, Number> revenueChart;
    @FXML
    private BarChart<String, Number> medicinesChart;
    @FXML
    private HBox chartsRow;

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final PharmacistDAO pharmacistDAO = new PharmacistDAO();
    //private final SalesDAO salesDAO = new SalesDAO();

    private Timeline refreshTimer;
    private boolean isPharmacist = false;
    private String pharmacistBranch = null;

    @FXML
    public void initialize() {
        if (UserSession.getCurrentUser() != null) {
            isPharmacist = "PHARMACIST".equalsIgnoreCase(UserSession.getCurrentUser().getRole());
            if (isPharmacist) {
                Pharmacist ph = pharmacistDAO.getPharmacistById(UserSession.getCurrentUser().getId());
                pharmacistBranch = ph != null ? ph.getBranchName() : null;
            }
        }

        setupUserProfile();
        setupRoleBasedAccess();
        setupDashboardVisibility();
        refreshDashboard();
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        refreshTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> refreshDashboard()));
        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();
    }

    @FXML
    public void handleBack(ActionEvent event) {
        mainPane.setCenter(pane);
        refreshDashboard();
    }

    private void setupUserProfile() {
        if (userImage != null && imageBorder != null) {
            double radius = Math.min(userImage.getFitWidth(), userImage.getFitHeight()) / 2.0;
            imageBorder.setRadius(radius);
            imageBorder.setCenterX(radius);
            imageBorder.setCenterY(radius);
            Circle clipCircle = new Circle(radius, radius, radius);
            userImage.setClip(clipCircle);
            loadUserPhoto();
        }

        if (welcomeLabel != null && UserSession.getCurrentUser() != null) {
            welcomeLabel.setText("Welcome, " + UserSession.getCurrentUser().getUsername());
        }
        if (roleLabel != null && UserSession.getCurrentUser() != null) {
            roleLabel.setText(UserSession.getCurrentUser().getRole());
        }
    }

    private void loadUserPhoto() {
        if (userImage == null)
            return;
        try {
            if (UserSession.getCurrentUser() != null && UserSession.getCurrentUser().getPhoto() != null) {
                byte[] bytes = UserSession.getCurrentUser().getPhoto();
                if (bytes.length > 0) {
                    userImage.setImage(new Image(new ByteArrayInputStream(bytes)));
                    return;
                }
            }
            Pharmacist ph = pharmacistDAO.getPharmacistById(UserSession.getCurrentUser().getId());
            if (ph != null && ph.getPhoto() != null && ph.getPhoto().length > 0) {
                userImage.setImage(new Image(new ByteArrayInputStream(ph.getPhoto())));
                UserSession.getCurrentUser().setPhoto(ph.getPhoto());
                return;
            }
            loadDefaultImage();
        } catch (Exception e) {
            loadDefaultImage();
        }
    }

    private void loadDefaultImage() {
        try {
            Image img = new Image(getClass().getResourceAsStream("/com/pharmacy/images/user.png"));
            if (img != null)
                userImage.setImage(img);
        } catch (Exception e) {
            userImage.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 45px;");
        }
    }

    private void setupDashboardVisibility() {
        if (isPharmacist) {
            setNodeVisible(medicineAmount, false);
            setNodeVisible(MedicinePriceLabel, false);
            setNodeVisible(lowStock, false);
            setNodeVisible(chartsRow, false);
            // ✅ Keep expiryMedicine visible for pharmacists!
        }
    }

    private void setupRoleBasedAccess() {
        if (UserSession.getCurrentUser() != null) {
            String role = UserSession.getCurrentUser().getRole();
            if ("PHARMACIST".equalsIgnoreCase(role)) {
                setButtonVisible(btnMedicine, false);
                setButtonVisible(btnInventory, false);
                setButtonVisible(btnPharmacists, false);
                setButtonVisible(btnReports, false);
            }
            if ("ADMIN".equalsIgnoreCase(role)) {
                setButtonVisible(btnPOS, false);
            }
        }
    }

    private void setButtonVisible(Button btn, boolean v) {
        if (btn != null) {
            btn.setVisible(v);
            btn.setManaged(v);
        }
    }

    private void setNodeVisible(javafx.scene.Node n, boolean v) {
        if (n != null) {
            n.setVisible(v);
            n.setManaged(v);
        }
    }

    private void loadDashboardStats() {
        // ✅ Get medicines based on role
        List<Medicine> medicines;
        if (isPharmacist && pharmacistBranch != null) {
            medicines = medicineDAO.getMedicinesByBranch(pharmacistBranch);
        } else {
            medicines = medicineDAO.getAllMedicines();
        }

        if (medicines == null)
            medicines = new ArrayList<>();

        // Total medicines count
        if (medicinesCountLabel != null) {
            medicinesCountLabel.setText(String.valueOf(medicines.size()));
        }

        // Total medicine price
        if (totalMedicinePriceLabel != null) {
            double totalPrice = medicines.stream().mapToDouble(m -> m.getSellingPrice() * m.getStock()).sum();
            totalMedicinePriceLabel.setText(String.format("$%.2f", totalPrice));
        }

        // Low stock alerts
        if (lowStockLabel != null) {
            long low = medicines.stream().filter(m -> m.getStock() < 10 && m.getStock() > 0).count();
            lowStockLabel.setText(String.valueOf(low));
        }

        // ✅ Expiry alerts - works for both roles
        if (expiryMedicineLabel != null) {
            LocalDate today = LocalDate.now();
            LocalDate thirtyDaysLater = today.plusDays(30);
            long expired = medicines.stream()
                    .filter(m -> m.getExpiryDate() != null && m.getExpiryDate().isBefore(thirtyDaysLater))
                    .count();
            expiryMedicineLabel.setText(String.valueOf(expired));
        }
    }

    private void updateAnalytics() {
        // ✅ Total Sales - pharmacist sees own sales total
        double sales;
        if (isPharmacist && UserSession.getCurrentUser() != null) {
            sales = dashboardDAO.getDailySalesBySeller(UserSession.getCurrentUser().getId());
        } else {
            sales = dashboardDAO.getDailySales();
        }

        if (salesLabel != null) {
            salesLabel.setText(String.format("$%.2f", sales));
        }

        if (!isPharmacist) {
            loadRevenueChart();
            loadTopMedicinesChart();
        }
    }

    private void loadRevenueChart() {
        if (revenueChart == null)
            return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        LocalDate today = LocalDate.now();
        Map<LocalDate, Double> data = dashboardDAO.getRevenueLast7Days();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            series.getData().add(new XYChart.Data<>(d.format(fmt), data.getOrDefault(d, 0.0)));
        }
        revenueChart.getData().clear();
        revenueChart.getData().add(series);
    }

    private void loadTopMedicinesChart() {
        if (medicinesChart == null)
            return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Units Sold");
        Map<String, Integer> data = dashboardDAO.getTopSellingMedicines();
        if (data == null || data.isEmpty()) {
            series.getData().add(new XYChart.Data<>("No Data", 0));
        } else {
            data.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
        }
        medicinesChart.getData().clear();
        medicinesChart.getData().add(series);
    }

    private void loadView(String fxml) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/pharmacy/view/" + fxml));
            mainPane.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert("Error", "Could not load view: " + fxml, Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleOpenMedicines(ActionEvent e) {
        loadView("medicine.fxml");
    }

    @FXML
    public void handleOpenPOS(ActionEvent e) {
        loadView("pos.fxml");
    }

    @FXML
    public void handleOpenSales(ActionEvent e) {
        loadView("sales.fxml");
    }

    @FXML
    public void handleOpenInventory(ActionEvent e) {
        loadView("inventory.fxml");
    }

    @FXML
    public void handleOpenPharmacists(ActionEvent e) {
        loadView("pharmacist.fxml");
    }

    @FXML
    public void handleOpenReports(ActionEvent e) {
        loadView("reports.fxml");
    }

    @FXML
    public void handleOpenProfile(ActionEvent e) {
        loadView("profile.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        if (refreshTimer != null)
            refreshTimer.stop();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to logout?", ButtonType.OK,
                ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                UserSession.cleanUserSession();
                try {
                    Parent view = FXMLLoader.load(getClass().getResource("/com/pharmacy/view/login.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(view));
                    stage.setTitle("Pharmacy Management - Login");
                    stage.centerOnScreen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void refreshDashboard() {
        loadDashboardStats();
        updateAnalytics();
    }
}