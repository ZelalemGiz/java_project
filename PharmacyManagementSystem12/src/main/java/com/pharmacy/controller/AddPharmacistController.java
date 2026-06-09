package com.pharmacy.controller;

import com.pharmacy.dao.PharmacistDAO;
import com.pharmacy.model.Pharmacist;
import com.pharmacy.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;

public class AddPharmacistController {

    @FXML private ImageView photo;
    @FXML private TextField txtIdNumber;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtMiddleName;
    @FXML private TextField txtLastName;
    @FXML private ComboBox<String> cbGender;
    @FXML private DatePicker dpDob;
    @FXML private ComboBox<String> cbNationality;
    @FXML private ComboBox<String> cbEducation;
    @FXML private TextField txtSalary;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtAddress;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private ComboBox<String> cbSecurityQuestion;
    @FXML private TextField txtSecurityAnswer;
    @FXML private ComboBox<String> cbBranch;

    private final PharmacistDAO pharmacistDAO = new PharmacistDAO();
    private PharmacistController parentController;
    private byte[] photoBytes;
    private Pharmacist editingPharmacist;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        cbGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        cbGender.getSelectionModel().selectFirst();
        
        cbRole.setItems(FXCollections.observableArrayList("PHARMACIST", "ADMIN"));
        cbRole.getSelectionModel().selectFirst();
        
        if (cbNationality != null) {
            cbNationality.setItems(FXCollections.observableArrayList(
                "American", "British", "Canadian", "Chinese", "Ethiopian", 
                "French", "German", "Indian", "Japanese", "Kenyan", 
                "Nigerian", "South African", "Tanzanian", "Ugandan", "Other"
            ));
            cbNationality.setPromptText("Select Nationality");
        }
        
        if (cbEducation != null) {
            cbEducation.setItems(FXCollections.observableArrayList(
                "High School", "Diploma", "Associate Degree",
                "Bachelor of Pharmacy (B.Pharm)", 
                "Master of Pharmacy (M.Pharm)",
                "Doctor of Pharmacy (Pharm.D)", 
                "PhD in Pharmacy"
            ));
            cbEducation.setPromptText("Select Education Level");
        }
        
        if (cbSecurityQuestion != null) {
            cbSecurityQuestion.setItems(FXCollections.observableArrayList(
                "What is your mother's maiden name?",
                "What was the name of your first pet?",
                "What city were you born in?",
                "What is your favorite color?",
                "What was your childhood nickname?",
                "What is the name of your favorite teacher?",
                "What was the make of your first car?"
            ));
            cbSecurityQuestion.setPromptText("Select Security Question");
        }
        
        if (cbBranch != null) {
            cbBranch.setItems(FXCollections.observableArrayList(
                "Main Branch", "Branch 1", "Branch 2", "Branch 3", "Branch 4", "Branch 5"
            ));
            cbBranch.setPromptText("Select Branch");
        }
    }

    public void setParentController(PharmacistController parentController) {
        this.parentController = parentController;
    }
   
    public void setPharmacistForEdit(Pharmacist pharmacist) {
        this.editingPharmacist = pharmacist;
        this.isEditMode = true;
        
        txtIdNumber.setText(pharmacist.getIdNumber());
        txtIdNumber.setDisable(true);
        txtFirstName.setText(pharmacist.getFirstName());
        txtMiddleName.setText(pharmacist.getMiddleName());
        txtLastName.setText(pharmacist.getLastName());
        cbGender.setValue(pharmacist.getGender());
        
        if (pharmacist.getDateOfBirth() != null) {
            dpDob.setValue(pharmacist.getDateOfBirth());
        }
        
        if (cbNationality != null && pharmacist.getNationality() != null) {
            cbNationality.setValue(pharmacist.getNationality());
        }
        if (cbEducation != null && pharmacist.getEducationLevel() != null) {
            cbEducation.setValue(pharmacist.getEducationLevel());
        }
        
        txtSalary.setText(String.valueOf(pharmacist.getSalary()));
        txtPhone.setText(pharmacist.getPhoneNumber());
        txtEmail.setText(pharmacist.getEmail());
        txtAddress.setText(pharmacist.getAddress());
        txtPassword.setText(pharmacist.getPassword());
        txtPassword.setDisable(true);
        cbRole.setValue(pharmacist.getRole());
        
        if (cbSecurityQuestion != null && pharmacist.getSecurityQuestion() != null) {
            cbSecurityQuestion.setValue(pharmacist.getSecurityQuestion());
        }
        if (txtSecurityAnswer != null) {
            txtSecurityAnswer.setText(pharmacist.getSecurityAnswer());
        }
        if (cbBranch != null && pharmacist.getBranchName() != null) {
            cbBranch.setValue(pharmacist.getBranchName());
        }
        
        if (pharmacist.getPhoto() != null) {
            photoBytes = pharmacist.getPhoto();
            try {
                photo.setImage(new Image(new java.io.ByteArrayInputStream(photoBytes)));
            } catch (Exception e) { }
        }
    }
    
    @FXML
    public void handleBrowsePhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(txtIdNumber.getScene().getWindow());
        
        if (selectedFile != null) {
            try {
                byte[] bytes = java.nio.file.Files.readAllBytes(selectedFile.toPath());
                
                if (bytes.length > 2 * 1024 * 1024) {
                    AlertUtil.showAlert("Error", "Photo must be less than 2MB.", Alert.AlertType.ERROR);
                    return;
                }
                
                photoBytes = bytes;
                photo.setImage(new Image(new FileInputStream(selectedFile)));
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showAlert("Error", "Could not load photo.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {

        if (txtIdNumber.getText().trim().isEmpty()) {
            AlertUtil.showAlert("Validation Error", "ID Number is required.", Alert.AlertType.ERROR);
            txtIdNumber.requestFocus();
            return;
        }
        
        if (txtFirstName.getText().trim().isEmpty()) {
            AlertUtil.showAlert("Validation Error", "First Name is required.", Alert.AlertType.ERROR);
            txtFirstName.requestFocus();
            return;
        }
        
        if (txtLastName.getText().trim().isEmpty()) {
            AlertUtil.showAlert("Validation Error", "Last Name is required.", Alert.AlertType.ERROR);
            txtLastName.requestFocus();
            return;
        }

        if (!isEditMode) {
            String password = txtPassword.getText();
            if (password.isEmpty()) {
                AlertUtil.showAlert("Validation Error", "Password is required.", Alert.AlertType.ERROR);
                txtPassword.requestFocus();
                return;
            }
            if (password.length() < 4) {
                AlertUtil.showAlert("Validation Error", "Password must be at least 4 characters.", Alert.AlertType.ERROR);
                txtPassword.requestFocus();
                return;
            }
        }
        
        if (cbBranch == null || cbBranch.getValue() == null) {
            AlertUtil.showAlert("Validation Error", "Please select a branch.", Alert.AlertType.ERROR);
            if (cbBranch != null) cbBranch.requestFocus();
            return;
        }

        if (cbGender.getValue() == null) {
            AlertUtil.showAlert("Validation Error", "Please select gender.", Alert.AlertType.ERROR);
            cbGender.requestFocus();
            return;
        }

        if (dpDob.getValue() == null) {
            AlertUtil.showAlert("Validation Error", "Date of birth is required.", Alert.AlertType.ERROR);
            dpDob.requestFocus();
            return;
        }
        
        if (dpDob.getValue().isAfter(LocalDate.now().minusYears(18))) {
            AlertUtil.showAlert("Validation Error", "Pharmacist must be at least 18 years old.", Alert.AlertType.ERROR);
            dpDob.requestFocus();
            return;
        }
        
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            AlertUtil.showAlert("Validation Error", "Please enter a valid email address.", Alert.AlertType.ERROR);
            txtEmail.requestFocus();
            return;
        }
        
        
        String phone = txtPhone.getText().trim();
        if (!phone.isEmpty() && !phone.matches("[0-9+\\-() ]+")) {
            AlertUtil.showAlert("Validation Error", "Phone number contains invalid characters.", Alert.AlertType.ERROR);
            txtPhone.requestFocus();
            return;
        }

        double salary = 0;
        if (!txtSalary.getText().trim().isEmpty()) {
            try {
                salary = Double.parseDouble(txtSalary.getText().trim());
                if (salary < 0) {
                    AlertUtil.showAlert("Validation Error", "Salary cannot be negative.", Alert.AlertType.ERROR);
                    txtSalary.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                AlertUtil.showAlert("Validation Error", "Salary must be a valid number.", Alert.AlertType.ERROR);
                txtSalary.requestFocus();
                return;
            }
        }

        if (txtFirstName.getText().trim().length() > 100) {
            AlertUtil.showAlert("Validation Error", "First name is too long (max 100 characters).", Alert.AlertType.ERROR);
            return;
        }
        if (txtLastName.getText().trim().length() > 100) {
            AlertUtil.showAlert("Validation Error", "Last name is too long (max 100 characters).", Alert.AlertType.ERROR);
            return;
        }

        try {
            Pharmacist ph = isEditMode ? editingPharmacist : new Pharmacist();
            
            if (photoBytes != null) ph.setPhoto(photoBytes);
            if (!isEditMode) ph.setIdNumber(txtIdNumber.getText().trim());
            
            ph.setFirstName(txtFirstName.getText().trim());
            ph.setMiddleName(txtMiddleName.getText().trim());
            ph.setLastName(txtLastName.getText().trim());
            ph.setGender(cbGender.getValue());
            ph.setDateOfBirth(dpDob.getValue());
            
            if (cbNationality != null && cbNationality.getValue() != null) 
                ph.setNationality(cbNationality.getValue());
            if (cbEducation != null && cbEducation.getValue() != null) 
                ph.setEducationLevel(cbEducation.getValue());
            
            ph.setSalary(salary);
            ph.setPhoneNumber(txtPhone.getText().trim());
            ph.setEmail(txtEmail.getText().trim());
            ph.setAddress(txtAddress.getText().trim());
            ph.setRole(cbRole.getValue());
            ph.setBranchName(cbBranch.getValue());
            
            if (!isEditMode) ph.setPassword(txtPassword.getText());
            
            if (cbSecurityQuestion != null && cbSecurityQuestion.getValue() != null) 
                ph.setSecurityQuestion(cbSecurityQuestion.getValue());
            if (txtSecurityAnswer != null) 
                ph.setSecurityAnswer(txtSecurityAnswer.getText().trim());

            boolean success = isEditMode ? pharmacistDAO.updatePharmacist(ph) : pharmacistDAO.addPharmacist(ph);

            if (success) {
                AlertUtil.showAlert("Success", isEditMode ? "Pharmacist updated!" : "Pharmacist added!", Alert.AlertType.INFORMATION);
                if (parentController != null) parentController.loadData();
                closeWindow();
            } else {
                AlertUtil.showAlert("Error", "Failed to save.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    public void handleBack(ActionEvent event) {
        closeWindow();
    }
    
    @FXML
    public void handleClear(ActionEvent event) {
        txtFirstName.clear();
        txtMiddleName.clear();
        txtLastName.clear();
        txtSalary.clear();
        txtPhone.clear();
        txtEmail.clear();
        txtAddress.clear();
        if (!isEditMode) {
            txtIdNumber.clear();
            txtPassword.clear();
        }
        cbGender.getSelectionModel().selectFirst();
        dpDob.setValue(null);
        if (cbNationality != null) cbNationality.getSelectionModel().clearSelection();
        if (cbEducation != null) cbEducation.getSelectionModel().clearSelection();
        cbRole.getSelectionModel().selectFirst();
        if (cbSecurityQuestion != null) cbSecurityQuestion.getSelectionModel().clearSelection();
        if (txtSecurityAnswer != null) txtSecurityAnswer.clear();
        if (cbBranch != null) cbBranch.getSelectionModel().clearSelection();
    }
    
    private void closeWindow() {
        ((Stage) txtIdNumber.getScene().getWindow()).close();
    }
}