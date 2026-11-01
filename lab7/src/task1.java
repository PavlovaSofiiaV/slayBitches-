import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class task1 {
    public static void main(String[] args) {
        // Створення тестової заявки на бронювання
        BookingRequest request = new BookingRequest();
        request.setGuestName("Руслана");
        request.setGuestPhone("+380991234567");
        request.setRoomType("Deluxe");
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(4));
        request.setLoyaltyMember(true);
        request.setExtraServices(List.of("Breakfast", "Spa"));

        // Запуск оркестратора
        HotelBookingService bookingService = new HotelBookingService();
        bookingService.createBooking(request);
    }
}

// 1. DTO / Доменні сутності (без public, бо живуть у Task1.java)
class BookingRequest {
    private String guestName;
    private String guestPhone;
    private String roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private boolean isLoyaltyMember;
    private List<String> extraServices;

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public boolean isLoyaltyMember() { return isLoyaltyMember; }
    public void setLoyaltyMember(boolean loyaltyMember) { isLoyaltyMember = loyaltyMember; }

    public List<String> getExtraServices() { return extraServices; }
    public void setExtraServices(List<String> extraServices) { this.extraServices = extraServices; }

    public long getNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
}

// 2. Валідація вхідних даних (Відповідальність: Перевірка правил)
class BookingValidator {
    public void validate(BookingRequest request) {
        if (request.getGuestName() == null || request.getGuestName().isBlank() ||
                request.getGuestPhone() == null || request.getGuestPhone().isBlank()) {
            throw new IllegalArgumentException("Вкажіть ім'я гостя та контактний номер телефону.");
        }

        if (request.getCheckInDate() == null || request.getCheckOutDate() == null ||
                request.getCheckInDate().isBefore(LocalDate.now()) ||
                !request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalStateException("Некоректний період проживання.");
        }
    }
}

// 3. Розрахунок вартості (Відповідальність: Бізнес-математика)
class BookingPriceCalculator {
    public BigDecimal calculateTotal(BookingRequest request) {
        BigDecimal basePricePerNight = getBasePrice(request.getRoomType());

        // Сезонний коефіцієнт (літній період)
        int checkInMonth = request.getCheckInDate().getMonthValue();
        if (checkInMonth >= 6 && checkInMonth <= 8) {
            basePricePerNight = basePricePerNight.multiply(new BigDecimal("1.3"));
        }

        long nights = request.getNights();
        BigDecimal roomTotal = basePricePerNight.multiply(BigDecimal.valueOf(nights));

        // Розрахунок додаткових послуг
        BigDecimal extrasTotal = BigDecimal.ZERO;
        if (request.getExtraServices() != null) {
            for (String service : request.getExtraServices()) {
                if ("Breakfast".equalsIgnoreCase(service)) {
                    extrasTotal = extrasTotal.add(new BigDecimal("300").multiply(BigDecimal.valueOf(nights)));
                }
                if ("Spa".equalsIgnoreCase(service)) {
                    extrasTotal = extrasTotal.add(new BigDecimal("800"));
                }
                if ("Transfer".equalsIgnoreCase(service)) {
                    extrasTotal = extrasTotal.add(new BigDecimal("500"));
                }
            }
        }

        BigDecimal grandTotal = roomTotal.add(extrasTotal);

        // Знижка за програмою лояльності
        if (request.isLoyaltyMember()) {
            grandTotal = grandTotal.multiply(new BigDecimal("0.90"));
        }

        return grandTotal;
    }

    private BigDecimal getBasePrice(String roomType) {
        if ("Standard".equalsIgnoreCase(roomType)) return new BigDecimal("1200");
        if ("Deluxe".equalsIgnoreCase(roomType)) return new BigDecimal("2500");
        if ("Suite".equalsIgnoreCase(roomType)) return new BigDecimal("4500");
        throw new IllegalArgumentException("Невідома категорія номера.");
    }
}

// 4. Генерація ваучера (Відповідальність: Форматування/UI)
class VoucherGenerator {
    public String generateHtmlVoucher(BookingRequest request, BigDecimal totalAmount) {
        String bookingNumber = UUID.randomUUID().toString().substring(0, 8);
        String extrasText = (request.getExtraServices() != null && !request.getExtraServices().isEmpty())
                ? String.join(", ", request.getExtraServices())
                : "Немає";

        return String.format("<html><body>" +
                        "<h2>Ваучер Бронювання №%s</h2>" +
                        "<p><b>Гість:</b> %s</p>" +
                        "<p><b>Тип номера:</b> %s</p>" +
                        "<p><b>Заїзд:</b> %s | <b>Виїзд:</b> %s (%d ночей)</p>" +
                        "<p><b>Додаткові послуги:</b> %s</p>" +
                        "<h3>Загальна вартість: %.2f грн</h3>" +
                        "</body></html>",
                bookingNumber,
                request.getGuestName(),
                request.getRoomType(),
                request.getCheckInDate().toString(),
                request.getCheckOutDate().toString(),
                request.getNights(),
                extrasText,
                totalAmount
        );
    }
}

// 5. Логування / Збереження (Відповідальність: Data Access)
class BookingRepository {
    public void save(BookingRequest request, BigDecimal totalAmount) {
        String nowStr = LocalDate.now().toString();
        String dbEntry = String.format("%s|%s|%s|%dNights|%.2f%n",
                nowStr, request.getGuestName(), request.getRoomType(), request.getNights(), totalAmount);

        try (FileWriter writer = new FileWriter("hotel_bookings.log", true)) {
            writer.write(dbEntry);
        } catch (IOException e) {
            System.err.println("Помилка логування бронювання: " + e.getMessage());
        }
    }
}

// 6. Відправка повідомлень (Відповідальність: Зовнішні API)
class NotificationService {
    public void sendBookingConfirmation(BookingRequest request, BigDecimal totalAmount) {
        try {
            String message = String.format("Шановний(а) %s, ваше бронювання %s на %d ночей зафіксовано. Сума: %.2f грн.",
                    request.getGuestName(), request.getRoomType(), request.getNights(), totalAmount);
            System.out.println("Надсилання меседжу на номер " + request.getGuestPhone() + " через http://viber-gateway.local/send");
            System.out.println("Текст: " + message);
        } catch (Exception ex) {
            System.out.println("Помилка відправки меседжу гостю: " + ex.getMessage());
        }
    }
}

// 7. Головний Оркестратор бізнес-процесу
class HotelBookingService {
    private final BookingValidator validator = new BookingValidator();
    private final BookingPriceCalculator calculator = new BookingPriceCalculator();
    private final VoucherGenerator voucherGenerator = new VoucherGenerator();
    private final BookingRepository repository = new BookingRepository();
    private final NotificationService notificationService = new NotificationService();

    public void createBooking(BookingRequest request) {
        validator.validate(request);
        BigDecimal totalAmount = calculator.calculateTotal(request);
        String voucherHtml = voucherGenerator.generateHtmlVoucher(request, totalAmount);
        repository.save(request, totalAmount);
        notificationService.sendBookingConfirmation(request, totalAmount);
    }
}