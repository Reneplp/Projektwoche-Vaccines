package com.vaccines.vaccines.model;
import java.time.LocalDate;

public class Vaccination {

    private String tradeName;
    private String diseaseName;
    private String protectionDuration;
    private int boosterAfterMonths;
    private LocalDate lastVaccination;
    private LocalDate nextDue;

    public Vaccination(String tradeName, String diseaseName, String protectionDuration, int boosterAfterMonths, LocalDate lastVaccination) {
        this.tradeName = tradeName;
        this.diseaseName = diseaseName;
        this.protectionDuration = protectionDuration;
        this.boosterAfterMonths = boosterAfterMonths;
        this.lastVaccination = lastVaccination;
        nextDue = lastVaccination.plusMonths(boosterAfterMonths);
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public String getProtectionDuration() {
        return protectionDuration;
    }

    public void setProtectionDuration(String protectionDuration) {
        this.protectionDuration = protectionDuration;
    }

    public int getBoosterAfterMonths() {
        return boosterAfterMonths;
    }

    public void setBoosterAfterMonths(int boosterAfterMonths) {
        this.boosterAfterMonths = boosterAfterMonths;
    }

    public LocalDate getLastVaccination() {
        return lastVaccination;
    }

    public void setLastVaccination(LocalDate lastVaccination) {
        this.lastVaccination = lastVaccination;
        this.nextDue = lastVaccination.plusMonths(boosterAfterMonths);
    }

    public LocalDate getNextDue() {
        return nextDue;
    }

}
