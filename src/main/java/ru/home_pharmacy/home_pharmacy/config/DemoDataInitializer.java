package ru.home_pharmacy.home_pharmacy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.home_pharmacy.home_pharmacy.entity.ActiveIngredient;
import ru.home_pharmacy.home_pharmacy.entity.Disease;
import ru.home_pharmacy.home_pharmacy.entity.Medication;
import ru.home_pharmacy.home_pharmacy.entity.Pharmacy;
import ru.home_pharmacy.home_pharmacy.entity.Symptom;
import ru.home_pharmacy.home_pharmacy.repository.ActiveIngredientRepository;
import ru.home_pharmacy.home_pharmacy.repository.DiseaseRepository;
import ru.home_pharmacy.home_pharmacy.repository.MedicationRepository;
import ru.home_pharmacy.home_pharmacy.repository.PharmacyRepository;
import ru.home_pharmacy.home_pharmacy.repository.SymptomRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Заполняет базу демонстрационными данными при первом запуске (пустая БД).
 * Изображения берутся из папки uploads (путь /uploads/<имя_файла>).
 * Каждый блок создаётся только если соответствующая таблица пуста.
 */
@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final SymptomRepository symptomRepository;
    private final DiseaseRepository diseaseRepository;
    private final ActiveIngredientRepository activeIngredientRepository;
    private final MedicationRepository medicationRepository;
    private final PharmacyRepository pharmacyRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedSymptoms();
        seedDiseases();
        seedIngredients();
        seedMedications();
        seedPharmacies();
    }

    private void seedSymptoms() {
        if (symptomRepository.count() > 0) {
            return;
        }
        List<Symptom> symptoms = List.of(
                Symptom.builder().name("Кашель").build(),
                Symptom.builder().name("Температура").build(),
                Symptom.builder().name("Насморк").build(),
                Symptom.builder().name("Головная боль").build(),
                Symptom.builder().name("Боль в горле").build(),
                Symptom.builder().name("Жидкий стул").build(),
                Symptom.builder().name("Тошнота").build(),
                Symptom.builder().name("Кожный зуд").build(),
                Symptom.builder().name("Чихание").build(),
                Symptom.builder().name("Спазм").build()
        );
        symptomRepository.saveAll(symptoms);
    }

    private void seedDiseases() {
        if (diseaseRepository.count() > 0) {
            return;
        }
        Map<String, Symptom> symptoms = symptomRepository.findAll().stream()
                .collect(Collectors.toMap(Symptom::getName, Function.identity()));

        List<Disease> diseases = List.of(
                Disease.builder().name("ОРВИ")
                        .description("Острая респираторная вирусная инфекция")
                        .symptoms(Set.of(symptoms.get("Кашель"), symptoms.get("Температура"), symptoms.get("Насморк")))
                        .build(),
                Disease.builder().name("Головная боль")
                        .description("Головные боли различного происхождения")
                        .symptoms(Set.of(symptoms.get("Головная боль")))
                        .build(),
                Disease.builder().name("Боль в горле")
                        .description("Першение и боль при глотании")
                        .symptoms(Set.of(symptoms.get("Боль в горле"), symptoms.get("Температура")))
                        .build(),
                Disease.builder().name("Диарея")
                        .description("Расстройство пищеварения")
                        .symptoms(Set.of(symptoms.get("Жидкий стул"), symptoms.get("Тошнота")))
                        .build(),
                Disease.builder().name("Отравление")
                        .description("Пищевое отравление")
                        .symptoms(Set.of(symptoms.get("Тошнота"), symptoms.get("Жидкий стул")))
                        .build(),
                Disease.builder().name("Аллергия")
                        .description("Аллергические реакции")
                        .symptoms(Set.of(symptoms.get("Кожный зуд"), symptoms.get("Чихание")))
                        .build(),
                Disease.builder().name("Спазмы")
                        .description("Спастические боли")
                        .symptoms(Set.of(symptoms.get("Спазм")))
                        .build()
        );
        diseaseRepository.saveAll(diseases);
    }

    private void seedIngredients() {
        if (activeIngredientRepository.count() > 0) {
            return;
        }
        List<ActiveIngredient> ingredients = List.of(
                ActiveIngredient.builder().name("Парацетамол").description("Жаропонижающее, обезболивающее").build(),
                ActiveIngredient.builder().name("Ибупрофен").description("Нестероидное противовоспалительное").build(),
                ActiveIngredient.builder().name("Ацетилсалициловая кислота").description("Обезболивающее, жаропонижающее").build(),
                ActiveIngredient.builder().name("Метамизол натрия").description("Обезболивающее").build(),
                ActiveIngredient.builder().name("Амоксициллин").description("Антибиотик широкого спектра").build(),
                ActiveIngredient.builder().name("Лоперамид").description("Противодиарейное").build(),
                ActiveIngredient.builder().name("Активированный уголь").description("Энтеросорбент").build(),
                ActiveIngredient.builder().name("Дротаверин").description("Спазмолитик").build(),
                ActiveIngredient.builder().name("Цетиризин").description("Антигистаминное").build()
        );
        activeIngredientRepository.saveAll(ingredients);
    }

    private void seedMedications() {
        if (medicationRepository.count() > 0) {
            return;
        }
        Map<String, Disease> diseases = diseaseRepository.findAll().stream()
                .collect(Collectors.toMap(Disease::getName, Function.identity()));
        Map<String, ActiveIngredient> ingredients = activeIngredientRepository.findAll().stream()
                .collect(Collectors.toMap(ActiveIngredient::getName, Function.identity()));

        List<Medication> medications = List.of(
                med("Парацетамол 500 мг", "Жаропонижающее и обезболивающее средство",
                        "/uploads/paracetamol.jpg", "52.00",
                        Set.of("ОРВИ", "Головная боль"), Set.of("Парацетамол"), diseases, ingredients),
                med("Анальгин", "Обезболивающее средство",
                        "/uploads/analgin.jpg", "45.00",
                        Set.of("Головная боль"), Set.of("Метамизол натрия"), diseases, ingredients),
                med("Аспирин", "Обезболивающее и жаропонижающее",
                        "/uploads/aspirin.jpg", "63.00",
                        Set.of("ОРВИ", "Головная боль"), Set.of("Ацетилсалициловая кислота"), diseases, ingredients),
                med("Ибупрофен", "Противовоспалительное и обезболивающее",
                        "/uploads/ibuprofen.jpg", "95.00",
                        Set.of("Головная боль", "Боль в горле"), Set.of("Ибупрофен"), diseases, ingredients),
                med("Нурофен", "Обезболивающее и жаропонижающее",
                        "/uploads/nurofen.jpg", "128.00",
                        Set.of("ОРВИ", "Головная боль"), Set.of("Ибупрофен"), diseases, ingredients),
                med("Амоксициллин", "Антибиотик широкого спектра действия",
                        "/uploads/amocsicilin.jpg", "156.00",
                        Set.of("Боль в горле"), Set.of("Амоксициллин"), diseases, ingredients),
                med("Цетрин", "Средство от аллергии",
                        "/uploads/cetrin.jpg", "112.00",
                        Set.of("Аллергия"), Set.of("Цетиризин"), diseases, ingredients),
                med("Лоперамид", "Средство от диареи",
                        "/uploads/leoperamid.jpg", "74.00",
                        Set.of("Диарея"), Set.of("Лоперамид"), diseases, ingredients),
                med("Активированный уголь", "Энтеросорбент при отравлениях",
                        "/uploads/ugol.png", "26.00",
                        Set.of("Отравление"), Set.of("Активированный уголь"), diseases, ingredients),
                med("Но-шпа", "Спазмолитическое средство",
                        "/uploads/no-shpa.png", "142.00",
                        Set.of("Спазмы"), Set.of("Дротаверин"), diseases, ingredients)
        );
        medications.stream()
                .filter(m -> m.getName().equals("Амоксициллин"))
                .forEach(m -> m.setPrescriptionRequired(true));
        medicationRepository.saveAll(medications);
    }

    private void seedPharmacies() {
        if (pharmacyRepository.count() > 0) {
            return;
        }
        Map<String, Medication> meds = medicationRepository.findAll().stream()
                .collect(Collectors.toMap(Medication::getName, Function.identity()));
        LocalDate today = LocalDate.now();

        List<Pharmacy> batches = new ArrayList<>();
        // срок годности истекает в ближайшие дни
        addBatch(batches, meds.get("Парацетамол 500 мг"), 15, today.minusDays(20), today.plusDays(3));
        addBatch(batches, meds.get("Аспирин"), 10, today.minusDays(15), today.plusDays(5));
        // просроченные партии
        addBatch(batches, meds.get("Активированный уголь"), 8, today.minusDays(90), today.minusDays(5));
        addBatch(batches, meds.get("Анальгин"), 12, today.minusDays(60), today.minusDays(2));
        // обычные партии
        addBatch(batches, meds.get("Парацетамол 500 мг"), 25, today.minusDays(40), today.plusDays(300));
        addBatch(batches, meds.get("Ибупрофен"), 30, today.minusDays(10), today.plusDays(360));
        addBatch(batches, meds.get("Нурофен"), 20, today.minusDays(35), today.plusDays(340));
        addBatch(batches, meds.get("Амоксициллин"), 18, today.minusDays(50), today.plusDays(400));
        addBatch(batches, meds.get("Цетрин"), 22, today.minusDays(25), today.plusDays(330));
        addBatch(batches, meds.get("Лоперамид"), 16, today.minusDays(30), today.plusDays(280));
        addBatch(batches, meds.get("Активированный уголь"), 40, today.minusDays(45), today.plusDays(350));
        addBatch(batches, meds.get("Анальгин"), 28, today.minusDays(12), today.plusDays(260));
        addBatch(batches, meds.get("Аспирин"), 24, today.minusDays(55), today.plusDays(310));
        addBatch(batches, meds.get("Но-шпа"), 19, today.minusDays(20), today.plusDays(290));

        pharmacyRepository.saveAll(batches);
    }

    private Medication med(String name, String description, String image, String price,
                           Set<String> diseaseNames, Set<String> ingredientNames,
                           Map<String, Disease> diseases, Map<String, ActiveIngredient> ingredients) {
        return Medication.builder()
                .name(name)
                .description(description)
                .image(image)
                .price(new BigDecimal(price))
                .diseases(new HashSet<>(diseaseNames.stream().map(diseases::get).toList()))
                .activeIngredients(new HashSet<>(ingredientNames.stream().map(ingredients::get).toList()))
                .build();
    }

    private void addBatch(List<Pharmacy> list, Medication medication, int quantity,
                          LocalDate purchaseDate, LocalDate expirationDate) {
        list.add(Pharmacy.builder()
                .medication(medication)
                .quantity(quantity)
                .retailPrice(medication.getPrice())
                .purchaseDate(purchaseDate)
                .expirationDate(expirationDate)
                .build());
    }
}