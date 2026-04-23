package com.vaccines.vaccines.service;

import com.vaccines.vaccines.service.VaccineRegistry.ProfileType;
import com.vaccines.vaccines.service.VaccineRegistry.VaccineEntry;
import java.util.List;

public class VaccineService {

    public List<String> getTradeNamesForProfile(ProfileType profile) {
        return VaccineRegistry.getTradeNamesForProfile(profile);
    }

    public VaccineEntry findByTradeName(String tradeName) {
        return VaccineRegistry.findByTradeName(tradeName);
    }

    public List<VaccineEntry> getVaccinesForProfile(ProfileType profile) {
        return VaccineRegistry.getVaccinesForProfile(profile);
    }
}