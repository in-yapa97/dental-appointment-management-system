package com.dental.management.config;

import com.dental.management.entity.Dentist;
import com.dental.management.entity.Treatment;
import com.dental.management.repository.DentistRepository;
import com.dental.management.repository.TreatmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Automatically seeds standard master clinical treatments and initial dentists
 * upon application startup if tables are empty.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final TreatmentRepository treatmentRepository;
    private final DentistRepository dentistRepository;

    public DataInitializer(TreatmentRepository treatmentRepository, DentistRepository dentistRepository) {
        this.treatmentRepository = treatmentRepository;
        this.dentistRepository = dentistRepository;
    }

    @Override
    public void run(String... args) {
        seedTreatments();
        seedDentists();
    }

    private void seedTreatments() {
        if (treatmentRepository.count() == 0) {
            logger.info("Treatments table is empty. Seeding master clinical procedures catalog...");

            List<Treatment> initialTreatments = List.of(
                    new Treatment(
                            "TRT-101",
                            "Comprehensive Dental Exam & Cleaning",
                            "Routine bi-annual dental examination, ultrasonic plaque scaling, and enamel polishing.",
                            new BigDecimal("80.00")
                    ),
                    new Treatment(
                            "TRT-102",
                            "Composite Dental Filling",
                            "Tooth-colored composite resin restoration for dental caries and cavities.",
                            new BigDecimal("120.00")
                    ),
                    new Treatment(
                            "TRT-103",
                            "Simple Tooth Extraction",
                            "Surgical removal of damaged, decayed, or non-restorable tooth under local anesthesia.",
                            new BigDecimal("150.00")
                    ),
                    new Treatment(
                            "TRT-104",
                            "Root Canal Therapy (Endodontics)",
                            "Complete endodontic pulp extirpation, root canal cleaning, and gutta-percha obturation.",
                            new BigDecimal("450.00")
                    ),
                    new Treatment(
                            "TRT-105",
                            "Porcelain Ceramic Crown",
                            "Full-coverage custom porcelain ceramic dental crown restoration.",
                            new BigDecimal("600.00")
                    ),
                    new Treatment(
                            "TRT-106",
                            "Professional Teeth Whitening",
                            "In-office high-intensity laser teeth whitening and cosmetic shade brightening.",
                            new BigDecimal("250.00")
                    ),
                    new Treatment(
                            "TRT-107",
                            "Periodontal Deep Scaling",
                            "Subgingival scaling and root planing for gum disease and periodontal pocket reduction.",
                            new BigDecimal("200.00")
                    ),
                    new Treatment(
                            "TRT-108",
                            "Orthodontic Consultation & Braces",
                            "Comprehensive clinical orthodontic evaluation, cephalometric analysis, and alignment plan.",
                            new BigDecimal("100.00")
                    )
            );

            treatmentRepository.saveAll(initialTreatments);
            logger.info("Successfully seeded {} standard clinical treatments.", initialTreatments.size());
        }
    }

    private void seedDentists() {
        if (dentistRepository.count() == 0) {
            logger.info("Dentists table is empty. Seeding default practitioner catalog...");

            List<Dentist> initialDentists = List.of(
                    new Dentist(
                            "DEN-101",
                            "Dr. Marcus Vance",
                            "Orthodontics",
                            "+1-555-0101",
                            "marcus.vance@dentalcare.com"
                    ),
                    new Dentist(
                            "DEN-102",
                            "Dr. Sarah Connor",
                            "Oral Surgery",
                            "+1-555-0202",
                            "sarah.connor@dentalcare.com"
                    ),
                    new Dentist(
                            "DEN-103",
                            "Dr. Emily Thorne",
                            "General Dentistry",
                            "+1-555-0303",
                            "emily.thorne@dentalcare.com"
                    ),
                    new Dentist(
                            "DEN-104",
                            "Dr. Robert Chen",
                            "Endodontics",
                            "+1-555-0404",
                            "robert.chen@dentalcare.com"
                    )
            );

            dentistRepository.saveAll(initialDentists);
            logger.info("Successfully seeded {} default practitioners.", initialDentists.size());
        }
    }
}
