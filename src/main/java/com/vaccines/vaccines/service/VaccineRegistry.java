package com.vaccines.vaccines.service;
import java.util.*;
import java.util.stream.Collectors;


/**
 * VaccineRegistry – zentrale Datenbasis aller empfohlenen Impfungen.
 *
 * Quellen:
 *  - STIKO-Impfkalender 2026 (RKI / Paul-Ehrlich-Institut)
 *  - StIKo Vet – Leitlinie zur Impfung von Kleintieren (5. Aufl., März 2023)
 *
 * Struktur jedes Eintrags:
 *  Impfung für → Handelsnamen → Schutzzeitraum → Auffrischung nach (Monate)
 *
 * WICHTIG: Die Handelsnamen-Liste erhebt keinen Anspruch auf Vollständigkeit.
 * Neue und ausgelaufene Präparate sind möglich. Der Fallback im UI
 * (manuelle Krankheitsauswahl) bleibt daher unverzichtbar.
 */
public class VaccineRegistry {

    // =========================================================================
    // Datenmodell
    // =========================================================================

    public enum ProfileType {
        ERWACHSENER,
        KIND,
        HUND,
        KATZE
    }

    public static class VaccineEntry {
        private final String diseaseName;
        private final ProfileType profile;
        private final List<String> tradeNames;
        private final String protectionDuration;
        private final int boosterAfterMonths;    // 0 = einmalig / situationsabhaengig
        private final String category;
        private final String notes;

        public VaccineEntry(String diseaseName, ProfileType profile,
                            List<String> tradeNames, String protectionDuration,
                            int boosterAfterMonths, String category, String notes) {
            this.diseaseName        = diseaseName;
            this.profile            = profile;
            this.tradeNames         = Collections.unmodifiableList(tradeNames);
            this.protectionDuration = protectionDuration;
            this.boosterAfterMonths = boosterAfterMonths;
            this.category           = category;
            this.notes              = notes;
        }

        public String getDiseaseName()        { return diseaseName; }
        public ProfileType getProfile()       { return profile; }
        public List<String> getTradeNames()   { return tradeNames; }
        public String getProtectionDuration() { return protectionDuration; }
        public int getBoosterAfterMonths()    { return boosterAfterMonths; }
        public String getCategory()           { return category; }
        public String getNotes()              { return notes; }

        @Override
        public String toString() {
            return String.format("[%s] %s | %s | Auffrischung: %s",
                    profile, diseaseName, category,
                    boosterAfterMonths == 0 ? "einmalig/individuell"
                                           : boosterAfterMonths + " Monate");
        }
    }

    // =========================================================================
    // Interne Indizes
    // =========================================================================

    private static final Map<String, VaccineEntry> BY_TRADE_NAME  = new HashMap<>();
    private static final Map<ProfileType, List<VaccineEntry>> BY_PROFILE = new EnumMap<>(ProfileType.class);
    public  static final List<VaccineEntry> ALL_VACCINES;

    // =========================================================================
    // Datenbasis
    // =========================================================================

    static {
        List<VaccineEntry> v = new ArrayList<>();

        // =====================================================================
        // ERWACHSENE  (STIKO 2026)
        // =====================================================================

        v.add(new VaccineEntry(
            "Tetanus + Diphtherie (Td)",
            ProfileType.ERWACHSENER,
            Arrays.asList("Td-pur", "Revaxis", "diTeBooster", "Ditanrix"),
            "ca. 10 Jahre", 120, "Standard",
            "Reine Td-Auffrischung nach einmaliger Tdap-Gabe. Alle 10 Jahre."
        ));

        v.add(new VaccineEntry(
            "Tetanus + Diphtherie + Pertussis (Tdap)",
            ProfileType.ERWACHSENER,
            Arrays.asList("Boostrix", "Covaxis"),
            "ca. 10 Jahre", 120, "Standard",
            "Einmalige Pertussis-Auffrischung bei naechstfaelliger Td-Impfung. " +
            "Danach nur noch Td alle 10 Jahre."
        ));

        v.add(new VaccineEntry(
            "Tetanus + Diphtherie + Pertussis + Polio (Tdap-IPV)",
            ProfileType.ERWACHSENER,
            Arrays.asList("Boostrix Polio", "Repevax"),
            "ca. 10 Jahre", 120, "Standard",
            "Kombiniert Tdap mit Polio-Komponente. Sinnvoll wenn Polio-Auffrischung indiziert."
        ));

        v.add(new VaccineEntry(
            "FSME (Fruehsommer-Meningoenzephalitis)",
            ProfileType.ERWACHSENER,
            Arrays.asList(
                "Encepur Erwachsene",    // Bavarian Nordic, ab 12 J.
                "FSME-IMMUN Erwachsene", // Pfizer/Valneva, ab 16 J.
                "FSME-IMMUN 0,5 ml"      // alternative Schreibweise im Impfpass
            ),
            "3-5 Jahre nach 1. Auffrischung", 36, "Indikation",
            "Empfohlen in FSME-Risikogebieten (Schwarzwald, Bayern, BaWue). " +
            "Grundimmunisierung: 3 Dosen. Erste Auffrischung nach 3 Jahren. " +
            "Weitere: unter 60 J. alle 5-10 J., ab 60 J. alle 3 J."
        ));

        v.add(new VaccineEntry(
            "Influenza (Grippe)",
            ProfileType.ERWACHSENER,
            Arrays.asList(
                "Influvac Tetra",
                "Fluarix Tetra",
                "Vaxigrip Tetra",
                "Efluelda",               // Hochdosis fuer >= 60 J. (Sanofi)
                "Fluzone High-Dose Tetra" // aelterer Name Hochdosis
            ),
            "ca. 1 Jahr (saisonal)", 12, "Standard (ab 60 J.) / Indikation",
            "Jaehrliche Impfung im Herbst. Efluelda fuer Personen >= 60 J. empfohlen."
        ));

        v.add(new VaccineEntry(
            "COVID-19",
            ProfileType.ERWACHSENER,
            Arrays.asList(
                "Comirnaty",                    // BioNTech/Pfizer - mRNA
                "Spikevax",                     // Moderna - mRNA
                "Vaxzevria",                    // AstraZeneca - Vektor
                "COVID-19 Vaccine AstraZeneca", // aelterer AZ-Name
                "Nuvaxovid",                    // Novavax - Protein
                "VidPrevtyn Beta",              // Sanofi/GSK - Protein
                "COVID-19 Vaccine Janssen"      // Janssen - Vektor (einmalig)
            ),
            "Basisimmunitaet dauerhaft; saisonale Auffrischung fuer Risikogruppen",
            12, "Standard",
            "Basisimmunitaet: mind. 3 Antigenkontakte, davon mind. 1 Impfung. " +
            "Jaehrliche Herbst-Auffrischung fuer Risikogruppen und >= 60 J."
        ));

        v.add(new VaccineEntry(
            "Herpes Zoster (Guertelrose)",
            ProfileType.ERWACHSENER,
            Arrays.asList("Shingrix"),
            "Langfristig (>10 Jahre)", 0, "Standard (ab 60 J.) / Indikation (ab 18 J.)",
            "2 Dosen im Abstand von 2-6 Monaten. Kein weiterer Booster. " +
            "Ab 18 J. bei Immundefizienz oder schwerer Grunderkrankung."
        ));

        v.add(new VaccineEntry(
            "Pneumokokken",
            ProfileType.ERWACHSENER,
            Arrays.asList(
                "Prevenar 20",   // Pfizer - PCV20, empfohlen ab 60 J.
                "Apexxnar",      // Pfizer - PCV20, Alternativname
                "Pneumovax 23"   // MSD - PPSV23, eingeschraenkt empfohlen
            ),
            "Langfristig", 0, "Standard (ab 60 J.) / Indikation",
            "Einmalige Impfung fuer alle ab 60 J. Prevenar 20 bevorzugt (STIKO 2026)."
        ));

        v.add(new VaccineEntry(
            "Masern/Mumps/Roeteln (MMR) - Nachholimpfung Erwachsene",
            ProfileType.ERWACHSENER,
            Arrays.asList("Priorix", "M-M-RVaxPro"),
            "Lebenslang (nach 2 Impfungen)", 0, "Nachholimpfung",
            "Nach 1970 geborene Erwachsene >= 18 J.: einmalige Impfung wenn Impfstatus unklar " +
            "oder nur 1 Impfung in Kindheit. Masernschutzgesetz gilt fuer bestimmte Berufe."
        ));

        v.add(new VaccineEntry(
            "Hepatitis B",
            ProfileType.ERWACHSENER,
            Arrays.asList(
                "Engerix-B",
                "Engerix-B Erwachsene",
                "HBVAXPRO 10",    // Standarddosis
                "HBVAXPRO 40",    // fuer Dialysepatienten
                "Fendrix",        // adjuvantiert fuer Immunsupprimierte
                "Twinrix",        // Hep A+B Kombination
                "Twinrix Erwachsene"
            ),
            "10+ Jahre", 0, "Indikation / Nachholimpfung",
            "3 Dosen (0-1-6 Monate). Auffrischung nur bei Titercheck indiziert. " +
            "Twinrix kombiniert Hep A und B."
        ));

        v.add(new VaccineEntry(
            "Hepatitis A",
            ProfileType.ERWACHSENER,
            Arrays.asList(
                "Havrix 1440",   // GSK Erwachsenendosis
                "Vaqta 50",      // MSD Erwachsenendosis
                "Twinrix",
                "Twinrix Erwachsene"
            ),
            "20+ Jahre (nach 2 Dosen)", 0, "Indikation / Reise",
            "2 Dosen (0 und 6-12 Monate). Fuer Reisende in Endemiegebiete, Risikogruppen."
        ));

        v.add(new VaccineEntry(
            "Meningokokken ACWY",
            ProfileType.ERWACHSENER,
            Arrays.asList("Menveo", "Nimenrix", "MenQuadfi"),
            "ca. 5 Jahre", 0, "Indikation",
            "Fuer Risikogruppen, Reisende, Erstimmatrikulierte."
        ));

        v.add(new VaccineEntry(
            "Meningokokken B",
            ProfileType.ERWACHSENER,
            Arrays.asList("Bexsero", "Trumenba"),
            "Grundimmunisierung", 0, "Indikation",
            "Fuer Risikogruppen (Asplenie, Komplementdefekte). Nicht allgemein empfohlen."
        ));

        // =====================================================================
        // MENSCH - KINDER  (STIKO 2026)
        // =====================================================================

        v.add(new VaccineEntry(
            "Sechsfachimpfung: Tetanus, Diphtherie, Pertussis, Polio, Hib, Hepatitis B",
            ProfileType.KIND,
            Arrays.asList(
                "Hexyon",        // Sanofi
                "Infanrix hexa", // GSK
                "Vaxelis"        // MSD/Sanofi
            ),
            "Grundimmunisierung", 0, "Standard",
            "2+1-Schema: 2, 4, 11 Monate. Auffrischung: 5-6 J. und 9-16 J."
        ));

        v.add(new VaccineEntry(
            "Fuenffachimpfung: Tetanus, Diphtherie, Pertussis, Polio, Hib",
            ProfileType.KIND,
            Arrays.asList(
                "Infanrix-IPV+Hib", // GSK
                "Pentavac",         // Sanofi
                "Pentaxim"          // Sanofi
            ),
            "Grundimmunisierung", 0, "Standard (Alternativschema)",
            "Wenn Hep-B separat gegeben wird oder 3+1-Schema erforderlich."
        ));

        v.add(new VaccineEntry(
            "Tetanus + Diphtherie + Pertussis Auffrischung (Kind/Jugend)",
            ProfileType.KIND,
            Arrays.asList(
                "Boostrix",      // Tdap (GSK)
                "Infanrix",      // DTaP (juengere Kinder, GSK)
                "Repevax",       // Tdap+IPV (Sanofi)
                "Boostrix Polio",// Tdap+IPV (GSK)
                "Infanrix-IPV"   // DTaP+IPV (GSK)
            ),
            "ca. 10 Jahre", 120, "Standard",
            "1. Auffrischung: 5-6 Jahre. 2. Auffrischung: 9-16 Jahre."
        ));

        v.add(new VaccineEntry(
            "Masern, Mumps, Roeteln (MMR)",
            ProfileType.KIND,
            Arrays.asList("Priorix", "M-M-RVaxPro"),
            "Lebenslang (nach 2 Dosen)", 0, "Standard",
            "1. Dosis: 11-14 Monate. 2. Dosis: 15-23 Monate."
        ));

        v.add(new VaccineEntry(
            "Masern, Mumps, Roeteln, Varizellen (MMRV)",
            ProfileType.KIND,
            Arrays.asList("Priorix-Tetra", "ProQuad"),
            "Lebenslang (nach 2 Dosen)", 0, "Standard",
            "Kombiniert MMR und Windpocken. Nur als 2. MMR-Dosis ab 15 Monaten empfohlen."
        ));

        v.add(new VaccineEntry(
            "Varizellen (Windpocken)",
            ProfileType.KIND,
            Arrays.asList("Varilrix", "Varivax"),
            "Langfristig (nach 2 Dosen)", 0, "Standard",
            "2 Dosen: 11-14 und 15-23 Monate. Alternativ als MMRV-Kombination."
        ));

        v.add(new VaccineEntry(
            "Pneumokokken (Kinder)",
            ProfileType.KIND,
            Arrays.asList(
                "Prevenar 13",  // aeltere Formulierung, noch in Impfpaessen
                "Prevenar 20",  // aktuell empfohlen
                "Synflorix"     // GSK PCV10
            ),
            "Grundimmunisierung", 0, "Standard",
            "2+1-Schema: 2, 4, 11 Monate."
        ));

        v.add(new VaccineEntry(
            "Meningokokken B (Kinder)",
            ProfileType.KIND,
            Arrays.asList("Bexsero", "Trumenba"),
            "Grundimmunisierung", 0, "Standard",
            "3-Dosen-Schema: 2, 4, 12 Monate. Bexsero bevorzugt."
        ));

        v.add(new VaccineEntry(
            "Meningokokken ACWY (Kinder/Jugendliche)",
            ProfileType.KIND,
            Arrays.asList("Menveo", "Nimenrix", "MenQuadfi"),
            "ca. 5 Jahre", 0, "Standard (neu ab STIKO 2026)",
            "Standardimpfung fuer alle Kinder 12-14 Jahre. Neu in STIKO 2026."
        ));

        v.add(new VaccineEntry(
            "HPV (Humane Papillomviren)",
            ProfileType.KIND,
            Arrays.asList(
                "Gardasil 9",  // MSD - 9-valent (empfohlen)
                "Cervarix"     // GSK - bivalent (kaum noch verwendet)
            ),
            "Langfristig (10+ Jahre)", 0, "Standard",
            "9-14 Jahre: 2 Dosen (0 und 6 Monate). Ab 15 J.: 3 Dosen (0, 2, 6 Monate). " +
            "Gilt fuer Maedchen und Jungen."
        ));

        v.add(new VaccineEntry(
            "Rotaviren",
            ProfileType.KIND,
            Arrays.asList(
                "Rotarix", // GSK - 2 Dosen Schluckimpfung
                "RotaTeq"  // MSD - 3 Dosen Schluckimpfung
            ),
            "Grundimmunisierung", 0, "Standard",
            "Schluckimpfung ab 6. Lebenswoche. Rotarix: 2 Dosen. RotaTeq: 3 Dosen. " +
            "Muss fruehzeitig abgeschlossen sein (Altersgrenze beachten)."
        ));

        v.add(new VaccineEntry(
            "FSME (Kinder)",
            ProfileType.KIND,
            Arrays.asList(
                "FSME-IMMUN Junior",  // Pfizer/Valneva - ab 1 Jahr
                "Encepur Kinder"      // Bavarian Nordic - ab 1 Jahr
            ),
            "3 Jahre nach 1. Auffrischung", 36, "Indikation",
            "In FSME-Risikogebieten. Grundimmunisierung: 3 Dosen. Erste Auffrischung nach 3 J."
        ));

        v.add(new VaccineEntry(
            "Influenza (Kinder)",
            ProfileType.KIND,
            Arrays.asList(
                "Fluenz Tetra",   // AstraZeneca - Nasenspray Lebendimpfstoff (2-17 J.)
                "Influvac Tetra",
                "Fluarix Tetra",
                "Vaxigrip Tetra"
            ),
            "1 Jahr (saisonal)", 12, "Indikation (Grunderkrankung)",
            "Jaehrlich im Herbst. Fluenz Tetra (Nasenspray) bevorzugt fuer Kinder 2-17 J. " +
            "Erstimpfung < 9 J.: 2 Dosen im Abstand von 4 Wochen."
        ));

        v.add(new VaccineEntry(
            "Hepatitis A (Kinder)",
            ProfileType.KIND,
            Arrays.asList("Havrix 720", "Vaqta 25", "Twinrix Kinder"),
            "20+ Jahre (nach 2 Dosen)", 0, "Indikation / Reise",
            "Ab 1 Jahr. 2 Dosen (0 und 6-12 Monate)."
        ));

        v.add(new VaccineEntry(
            "Hepatitis B (Kinder, separat)",
            ProfileType.KIND,
            Arrays.asList("Engerix-B Kinder", "HBVAXPRO 5"),
            "Langfristig", 0, "Standard (meist Teil 6-fach)",
            "Nur wenn nicht ueber Sechsfachimpfung abgedeckt. Selten als Einzelimpfstoff."
        ));

        // =====================================================================
        // HUND  (StIKo Vet 2023)
        // =====================================================================

        v.add(new VaccineEntry(
            "Staupe",
            ProfileType.HUND,
            Arrays.asList(
                "Nobivac SHPPi",          // MSD - 4-fach (Staupe+HCC+Parvo+Pi)
                "Nobivac DHP",            // MSD - 3-fach (ohne Pi)
                "Nobivac DHPPi",          // MSD - 4-fach
                "Versican Plus DHPPi",    // Zoetis - 4-fach
                "Versican Plus DHP",      // Zoetis - 3-fach
                "Virbagen canis SHAPPi",  // Virbac - 5-fach
                "Virbagen canis SHAP",    // Virbac - 4-fach
                "Eurican SHPPi2",         // Boehringer Ingelheim - 4-fach
                "Eurican SHP"             // Boehringer Ingelheim - 3-fach
            ),
            "3 Jahre", 36, "Core",
            "Grundimmunisierung: 8., 12., 16. Woche + 15. Monat. Dann alle 3 Jahre. " +
            "Immer als Kombiimpfstoff."
        ));

        v.add(new VaccineEntry(
            "Parvovirose",
            ProfileType.HUND,
            Arrays.asList(
                "Nobivac Parvo",          // MSD - Monoimpfstoff
                "Canigen P",              // Zoetis - Monoimpfstoff
                "Versican Plus P",        // Zoetis - Monoimpfstoff
                "Nobivac SHPPi",          // Kombis mit Parvo-Anteil
                "Nobivac DHP",
                "Versican Plus DHPPi",
                "Eurican SHPPi2",
                "Virbagen canis SHAPPi"
            ),
            "3 Jahre", 36, "Core",
            "Monoimpfstoffe fuer Spezialfaelle (Fruehimmunisierung). " +
            "Grundimmunisierung wie Staupe."
        ));

        v.add(new VaccineEntry(
            "Leptospirose",
            ProfileType.HUND,
            Arrays.asList(
                "Nobivac L4",             // MSD - 4 Serovare (empfohlen)
                "Nobivac Lepto",          // MSD - 2 Serovare (aeltere Version, noch in Paessen)
                "Versican Plus L4",       // Zoetis - 4 Serovare
                "Versican L3",            // Zoetis - 3 Serovare (aeltere Variante)
                "Virbagen Lepto",         // Virbac
                "Eurican L",              // Boehringer Ingelheim
                "Versican Plus DHPPiL4R"  // Kombi inkl. Lepto und Tollwut
            ),
            "1 Jahr", 12, "Core",
            "Jaehrliche Auffrischung zwingend. L4-Impfstoffe decken 4 Serovare ab. " +
            "Grundimmunisierung: 8. und 12. Woche."
        ));

        v.add(new VaccineEntry(
            "Hepatitis contagiosa canis (HCC / Adenovirus)",
            ProfileType.HUND,
            Arrays.asList(
                "Nobivac SHPPi",       // immer Teil des Kombi
                "Nobivac DHP",
                "Versican Plus DHPPi",
                "Eurican SHPPi2",
                "Virbagen canis SHAPPi"
            ),
            "3 Jahre", 36, "Core (immer in Kombi)",
            "Kein Monoimpfstoff verfuegbar. Immer Teil des SHP-Kombiimpfstoffs."
        ));

        v.add(new VaccineEntry(
            "Parainfluenza / Zwingerhusten (CPiV)",
            ProfileType.HUND,
            Arrays.asList(
                "Nobivac Pi",             // MSD - Monoimpfstoff
                "Nobivac SHPPi",          // Kombi
                "Nobivac BbPi",           // MSD - intranasal Bb+Pi
                "Versican Plus DHPPi",
                "Versican Plus BbPi IN",  // Zoetis - intranasal
                "Eurican SHPPi2"
            ),
            "1 Jahr", 12, "Non-Core (Risikoimpfung)",
            "Jaehrliche Auffrischung. Empfohlen bei Kontakt zu anderen Hunden."
        ));

        v.add(new VaccineEntry(
            "Tollwut - Hund",
            ProfileType.HUND,
            Arrays.asList(
                "Nobivac T",              // MSD - Monoimpfstoff (haeufig im Impfpass)
                "Nobivac Rabies",         // MSD - Monoimpfstoff
                "Versiguard Rabies",      // Zoetis - Mono
                "Rabisin",                // Boehringer Ingelheim - Mono (auch fuer Katzen)
                "Virbagen Tollwutimpfstoff", // Virbac
                "Nobivac LT",             // MSD - Lepto + Tollwut Kombi
                "Nobivac DHPPiLT",        // MSD - 6-fach inkl. Tollwut
                "Versican Plus L4R",      // Zoetis - Lepto + Tollwut
                "Versican Plus DHPPiL4R", // Zoetis - 6-fach
                "Virbagen canis LT",      // Virbac - Lepto + Tollwut
                "Virbagen canis SHAP/LT"  // Virbac - 5-fach + Tollwut
            ),
            "3 Jahre (je nach Hersteller 1-3 Jahre)", 36, "Non-Core (Pflicht Ausland)",
            "Deutschland seit 2008 tollwutfrei - keine Core-Impfung mehr. " +
            "Pflicht fuer EU-Reisen und Drittlaender. Erstimpfung ab 12 Wochen."
        ));

        v.add(new VaccineEntry(
            "Lyme-Borreliose - Hund",
            ProfileType.HUND,
            Arrays.asList(
                "Nobivac Lyme",      // MSD
                "Merilym 3",         // Boehringer Ingelheim
                "Recombitek Lyme"    // Boehringer Ingelheim (aeltere Bezeichnung)
            ),
            "1 Jahr", 12, "Non-Core (Risikoimpfung)",
            "In Zecken-Risikogebieten. Jaehrliche Auffrischung. " +
            "Schuetzt nicht gegen andere Zeckenkrankheiten."
        ));

        v.add(new VaccineEntry(
            "Zwingerhusten (Bordetella bronchiseptica)",
            ProfileType.HUND,
            Arrays.asList(
                "Nobivac KC",              // MSD - intranasal
                "Nobivac BbPi",            // MSD - intranasal Bb+Pi
                "Nobivac Respira Bb",      // MSD - subkutan inaktiviert
                "Versican Plus Bb IN",     // Zoetis - intranasal
                "Versican Plus Bb Oral",   // Zoetis - oral
                "Versican Plus BbPi IN",   // Zoetis - intranasal Bb+Pi
                "Bronchicine"              // Boehringer Ingelheim - subkutan
            ),
            "1 Jahr", 12, "Non-Core (Risikoimpfung)",
            "Vor Pension, Hundeschule, Ausstellungen. Intranasal mind. 1 Woche, " +
            "subkutan mind. 2-4 Wochen vor Exposition."
        ));

        // =====================================================================
        // KATZE  (StIKo Vet 2023)
        // =====================================================================

        v.add(new VaccineEntry(
            "Katzenseuche (Feline Panleukopenie / FPV)",
            ProfileType.KATZE,
            Arrays.asList(
                "Purevax RCP",          // Boehringer - Katze 3-fach adjuvansfrei
                "Purevax RCPCh",        // Boehringer - + Chlamydien
                "Purevax RCP+FeLV",     // Boehringer - + Leukamie
                "Purevax RCPCh FeLV",   // Boehringer - 5-fach
                "Nobivac Tricat Trio",  // MSD - 3-fach
                "Nobivac Forcat",       // MSD - 4-fach + Chlamydien
                "Feligen CRP",          // Zoetis - 3-fach
                "Feligen CRP + Ch",     // Zoetis - + Chlamydien
                "Felocell CVR",         // Zoetis - aeltere Bezeichnung
                "Anicat"                // andere Marke
            ),
            "3 Jahre", 36, "Core",
            "Grundimmunisierung: 8., 12., 16. Woche + 15. Monat. Dann alle 3 Jahre. " +
            "Auch fuer Wohnungskatzen empfohlen (Erreger einschleppbar)."
        ));

        v.add(new VaccineEntry(
            "Katzenschnupfen (FHV + FCV / Herpes- und Calicivirus)",
            ProfileType.KATZE,
            Arrays.asList(
                "Purevax RCP",
                "Purevax RCPCh",
                "Purevax RCP+FeLV",
                "Purevax RCPCh FeLV",
                "Nobivac Tricat Trio",
                "Nobivac Forcat",
                "Feligen CRP",
                "Feligen CRP + Ch",
                "Felocell CVR",
                "Anicat"
            ),
            "1-3 Jahre (herstellerabhaengig)", 12, "Core",
            "Immer als Kombiimpfstoff mit Katzenseuche. Jaehrliche Auffrischung empfohlen."
        ));

        v.add(new VaccineEntry(
            "Tollwut - Katze",
            ProfileType.KATZE,
            Arrays.asList(
                "Purevax Rabies",            // Boehringer - adjuvansfrei (bevorzugt)
                "Rabisin",                   // Boehringer - auch fuer Katzen
                "Nobivac T",                 // MSD - auch fuer Katzen zugelassen
                "Nobivac Rabies",            // MSD
                "Virbagen Tollwutimpfstoff"  // Virbac
            ),
            "2-3 Jahre (je nach Impfstoff)", 36, "Core Freigeher / Non-Core Wohnungskatze",
            "Purevax Rabies bevorzugt (adjuvansfrei, geringeres Sarkomrisiko). " +
            "Pflicht fuer Auslandsreisen und Pensionen."
        ));

        v.add(new VaccineEntry(
            "Katzenleukamie (FeLV)",
            ProfileType.KATZE,
            Arrays.asList(
                "Purevax FeLV",        // Boehringer - Mono adjuvansfrei
                "Purevax RCP+FeLV",    // Boehringer - Kombi
                "Purevax RCPCh FeLV",  // Boehringer - 5-fach
                "Nobivac Felv",        // MSD
                "Versifel CVR+FeLV",   // Zoetis
                "Felocel 4"            // Zoetis - Kombi inkl. FeLV
            ),
            "1-2 Jahre", 12, "Non-Core (Freigeher / Mehrkatzenhaushalte)",
            "Fuer Freigeher und Katzen mit Kontakt zu Artgenossen. " +
            "Vor Erstimpfung FeLV-Test empfohlen."
        ));

        v.add(new VaccineEntry(
            "Chlamydiose - Katze",
            ProfileType.KATZE,
            Arrays.asList(
                "Purevax RCPCh",      // Boehringer
                "Purevax RCPCh FeLV", // Boehringer
                "Nobivac Forcat",     // MSD
                "Feligen CRP + Ch"    // Zoetis
            ),
            "1 Jahr", 12, "Non-Core (Risikoimpfung)",
            "Empfohlen in Mehrkatzenhaushalten, Zucht, Tierheim, Pension."
        ));

        v.add(new VaccineEntry(
            "Zwingerhusten - Katze (Bordetella bronchiseptica)",
            ProfileType.KATZE,
            Arrays.asList("Nobivac Bb"),  // MSD - intranasal, auch fuer Katzen
            "1 Jahr", 12, "Non-Core (Risikoimpfung)",
            "Nur fuer Katzen mit engem Hundekontakt oder in Risikohaltungen."
        ));

        v.add(new VaccineEntry(
            "FIP (Feline Infektioese Peritonitis)",
            ProfileType.KATZE,
            Arrays.asList("Primucell FIP"),  // Zoetis - intranasal
            "Nicht ausreichend belegt", 0, "Non-Core (nicht empfohlen)",
            "Von StIKo Vet nicht empfohlen. Wirksamkeit unzureichend belegt. " +
            "Nur in Einzelfaellen nach individueller Beratung."
        ));

        // =====================================================================
        // Indizes aufbauen
        // =====================================================================

        ALL_VACCINES = Collections.unmodifiableList(v);

        for (VaccineEntry entry : ALL_VACCINES) {
            for (String name : entry.getTradeNames()) {
                BY_TRADE_NAME.put(name.trim().toLowerCase(), entry);
            }
        }

        for (ProfileType type : ProfileType.values()) {
            BY_PROFILE.put(type, ALL_VACCINES.stream()
                    .filter(e -> e.getProfile() == type)
                    .collect(Collectors.toList()));
        }
    }

    // =========================================================================
    // Oeffentliche Such-Methoden
    // =========================================================================

    /**
     * Sucht Eintrag anhand des Handelsnamens (wie im Impfpass).
     * Case-insensitiv. Gibt null zurueck wenn nicht gefunden -> Fallback nutzen.
     */
    public static VaccineEntry findByTradeName(String tradeName) {
        if (tradeName == null || tradeName.isBlank()) return null;
        return BY_TRADE_NAME.get(tradeName.trim().toLowerCase());
    }

    /**
     * Alle Impfungen fuer ein Profil.
     */
    public static List<VaccineEntry> getVaccinesForProfile(ProfileType profile) {
        return BY_PROFILE.getOrDefault(profile, Collections.emptyList());
    }

    /**
     * Alle bekannten Handelsnamen sortiert.
     * Fuer den Haupt-Dropdown im UI (alle Profile gemischt).
     */
    public static List<String> getAllTradeNames() {
        return ALL_VACCINES.stream()
                .flatMap(e -> e.getTradeNames().stream())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    /**
     * Handelsnamen gefiltert nach Profil.
     * Fuer gefilterten Dropdown wenn User erst das Profil (Hund/Katze/Mensch) gewaehlt hat.
     */
    public static List<String> getTradeNamesForProfile(ProfileType profile) {
        return getVaccinesForProfile(profile).stream()
                .flatMap(e -> e.getTradeNames().stream())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Demo / Testausgabe
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== Lookup via Handelsname ===");
        String[] tests = {
            "Nobivac T", "Boostrix", "Comirnaty", "Encepur Erwachsene",
            "Purevax RCP", "Versican Plus L4", "Nobivac L4", "Nobivac Lepto",
            "Repevax", "Priorix", "Shingrix", "Gardasil 9", "Fluenz Tetra",
            "FSME-IMMUN Junior", "Rabisin", "UnbekannterImpfstoff"
        };
        for (String name : tests) {
            VaccineEntry e = findByTradeName(name);
            System.out.printf("%-35s -> %s%n", name,
                    e != null ? e.getDiseaseName() + " [" + e.getProfile() + "]"
                              : "NICHT GEFUNDEN (Fallback)");
        }

        System.out.println("\n=== Hund: alle Impfungen ===");
        for (VaccineEntry e : getVaccinesForProfile(ProfileType.HUND)) {
            System.out.printf("  %-45s [%s]%n", e.getDiseaseName(), e.getCategory());
        }

        System.out.printf("%nEintraege gesamt: %d%n", ALL_VACCINES.size());
        System.out.printf("Handelsnamen gesamt: %d%n", getAllTradeNames().size());
    }
}
