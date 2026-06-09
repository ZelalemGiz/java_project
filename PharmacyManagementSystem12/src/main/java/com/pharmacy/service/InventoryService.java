package com.pharmacy.service;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.model.Medicine;
import java.util.List;

public class InventoryService {
    private MedicineDAO medicineDAO = new MedicineDAO();

    public List<Medicine> getLowStockMedicines(int threshold) {
        List<Medicine> all = medicineDAO.getAllMedicines();
        all.removeIf(m -> m.getStock() >= threshold);
        return all;
    }

    public boolean updateStock(int medicineId, int newStock) {
        return medicineDAO.updateStock(medicineId, newStock);
    }
}
