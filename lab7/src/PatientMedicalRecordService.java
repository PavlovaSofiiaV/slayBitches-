import java.util.Objects;

// 1. DTO / Доменна модель
class MedicalRecord {
    private String recordId;
    private String patientId;
    private String diagnosis;

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
}

// 2. Абстракції (Інтерфейси) — Точки інверсії залежностей
interface FileStorage {
    void saveEncryptedRecord(String recordId, String data);
}

interface PatientRepository {
    void insertRecord(MedicalRecord record);
}

interface EventPublisher {
    void publishMedicalEvent(String eventType, String recordId);
}

// 3. Низькорівневі Production-реалізації
class EncryptedLocalFileStorage implements FileStorage {
    @Override
    public void saveEncryptedRecord(String recordId, String data) {
        System.out.println("[Encrypted File Storage] Картку " + recordId + " зашифровано AES-256 та збережено у локальне сховище.");
    }
}

class OracleHealthDatabase implements PatientRepository {
    @Override
    public void insertRecord(MedicalRecord record) {
        System.out.println("[Oracle Health DB] Запис " + record.getRecordId() + " для пацієнта " + record.getPatientId() + " успішно збережено в таблицю 'PATIENT_RECORDS'.");
    }
}

class HospitalEventBusPublisher implements EventPublisher {
    @Override
    public void publishMedicalEvent(String eventType, String recordId) {
        System.out.println("[Hospital EventBus] Опубліковано подію '" + eventType + "' для медичного запису " + recordId + ".");
    }
}

// 4. Test / Mock Реалізації (Демонстрація легкої підміни)
class MockPatientRepository implements PatientRepository {
    @Override
    public void insertRecord(MedicalRecord record) {
        System.out.println("[MOCK DB] Тестовий запис у оперативну пам'ять ID: " + record.getRecordId());
    }
}

// 5. Високорівневий сервіс бізнес-логіки (Дотримується DIP)
public class PatientMedicalRecordService {
    // Залежність виключно від АБСТРАКЦІЙ!
    private final FileStorage fileStorage;
    private final PatientRepository repository;
    private final EventPublisher eventPublisher;

    // Впровадження залежностей через конструктор (Constructor Injection)
    public PatientMedicalRecordService(FileStorage fileStorage, PatientRepository repository, EventPublisher eventPublisher) {
        this.fileStorage = Objects.requireNonNull(fileStorage, "FileStorage не може бути null");
        this.repository = Objects.requireNonNull(repository, "PatientRepository не може бути null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "EventPublisher не може бути null");
    }

    public void savePatientRecord(MedicalRecord record) {
        if (record == null || record.getPatientId() == null || record.getPatientId().isEmpty()) {
            throw new IllegalArgumentException("Некоректний медичний запис пацієнта.");
        }

        this.repository.insertRecord(record);
        this.fileStorage.saveEncryptedRecord(record.getRecordId(), record.getDiagnosis());
        this.eventPublisher.publishMedicalEvent("PATIENT_DIAGNOSIS_UPDATED", record.getRecordId());
    }
}

// 6. Точка Збірки (Composition Root)
class CompositionRoot {
    public static void main(String[] args) {
        MedicalRecord record = new MedicalRecord();
        record.setRecordId("REC-9981");
        record.setPatientId("PAT-4402");
        record.setDiagnosis("ГРВІ");

        System.out.println("=== Production Режим ===");
        // Створюємо та передаємо реальні інфраструктурні об'єкти
        PatientMedicalRecordService prodService = new PatientMedicalRecordService(
                new EncryptedLocalFileStorage(),
                new OracleHealthDatabase(),
                new HospitalEventBusPublisher()
        );
        prodService.savePatientRecord(record);

        System.out.println("\n=== Test / Mock Режим ===");
        // Легко підміняємо OracleHealthDatabase на MockPatientRepository БЕЗ зміни коду сервісу!
        PatientMedicalRecordService testService = new PatientMedicalRecordService(
                new EncryptedLocalFileStorage(),
                new MockPatientRepository(),
                new HospitalEventBusPublisher()
        );
        testService.savePatientRecord(record);
    }
}
