package com.example.mobile;

import java.io.Serializable;

public class FasilitasOperation implements Serializable {
    public static final int OPERATION_ADD = 1;
    public static final int OPERATION_UPDATE = 2;
    public static final int OPERATION_DELETE = 3;

    private int operationType;
    private FasilitasHunianItem fasilitas;
    private FasilitasHunianItem oldFasilitas; // untuk update

    public FasilitasOperation(int operationType, FasilitasHunianItem fasilitas) {
        this.operationType = operationType;
        this.fasilitas = fasilitas;
    }

    public FasilitasOperation(int operationType, FasilitasHunianItem fasilitas, FasilitasHunianItem oldFasilitas) {
        this.operationType = operationType;
        this.fasilitas = fasilitas;
        this.oldFasilitas = oldFasilitas;
    }

    // Getters and Setters
    public int getOperationType() { return operationType; }
    public FasilitasHunianItem getFasilitas() { return fasilitas; }
    public FasilitasHunianItem getOldFasilitas() { return oldFasilitas; }

    public void setOperationType(int operationType) { this.operationType = operationType; }
    public void setFasilitas(FasilitasHunianItem fasilitas) { this.fasilitas = fasilitas; }
    public void setOldFasilitas(FasilitasHunianItem oldFasilitas) { this.oldFasilitas = oldFasilitas; }
}