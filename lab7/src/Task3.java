import java.math.BigDecimal;
import java.util.Objects;

public class Task3 {
    // Незмінний об'єкт-значення для артикулу товару (SKU)
    static final class SKU {
        private final String value;

        public SKU(String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("SKU не може бути порожнім.");
            }
            if (!value.matches("^[A-Z0-9]{3,10}$")) {
                throw new IllegalArgumentException("Некоректний формат SKU: " + value);
            }
            this.value = value;
        }

        public String getValue() { return value; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SKU)) return false;
            SKU sku = (SKU) o;
            return Objects.equals(value, sku.value);
        }

        @Override
        public int hashCode() { return Objects.hash(value); }

        @Override
        public String toString() { return value; }
    }

    // Незмінний об'єкт-значення для ціни товару (Money)
    static final class Money {
        private final BigDecimal amount;

        public Money(BigDecimal amount) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Ціна не може бути від'ємною або null.");
            }
            this.amount = amount;
        }

        public BigDecimal getAmount() { return amount; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Money)) return false;
            Money money = (Money) o;
            return Objects.equals(amount, money.amount);
        }

        @Override
        public int hashCode() { return Objects.hash(amount); }

        @Override
        public String toString() { return amount.toString() + " грн"; }
    }

    // Базовий інтерфейс для будь-якого товару в магазині
    interface Item {
        SKU getSku();
        String getTitle();
        Money getUnitPrice();
    }

    // Інтерфейс тільки для товарів, які підтримують фізичне повернення
    interface PhysicalReturnable {
        void returnToSupplier(int amount);
    }

    // Інтерфейс тільки для товарів, які підтримують поповнення залишків
    interface PhysicalRestockable {
        void restock(int amount);
    }

    // Фізичний складський товар (реалізує всі складські операції)
    static class PhysicalInventoryItem implements Item, PhysicalRestockable, PhysicalReturnable {
        private final SKU sku;
        private final String title;
        private final Money unitPrice;
        private int stockQuantity;

        public PhysicalInventoryItem(SKU sku, String title, int stockQuantity, Money unitPrice) {
            this.sku = Objects.requireNonNull(sku, "SKU не може бути null");
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Назва товару не може бути порожньою.");
            }
            if (stockQuantity < 0) {
                throw new IllegalArgumentException("Кількість товару не може бути від'ємною.");
            }
            this.title = title;
            this.stockQuantity = stockQuantity;
            this.unitPrice = Objects.requireNonNull(unitPrice, "Ціна не може бути null");
        }

        @Override public SKU getSku() { return sku; }
        @Override public String getTitle() { return title; }
        @Override public Money getUnitPrice() { return unitPrice; }
        public int getStockQuantity() { return stockQuantity; }

        @Override
        public void restock(int amount) {
            if (amount <= 0) {
                throw new IllegalArgumentException("Кількість для поповнення має бути більшою за 0.");
            }
            this.stockQuantity += amount;
            System.out.println("Поповнено на " + amount + " шт. Товар: " + title + ". Залишок: " + stockQuantity);
        }

        @Override
        public void returnToSupplier(int amount) {
            if (amount <= 0) {
                throw new IllegalArgumentException("Кількість для повернення має бути більшою за 0.");
            }
            if (amount > stockQuantity) {
                throw new IllegalArgumentException("Неможливо повернути більше товарів, ніж є на складі.");
            }
            this.stockQuantity -= amount;
            System.out.println("Повернуто постачальнику " + amount + " шт. Товар: " + title + ". Залишок: " + stockQuantity);
        }
    }

    // Цифровий товар (ліцензія) — реалізує лише Item (без UnsupportedOperationException!)
    static class DigitalLicenseItem implements Item {
        private final SKU sku;
        private final String title;
        private final Money unitPrice;
        private final String licenseKeyPattern;

        public DigitalLicenseItem(SKU sku, String title, Money unitPrice, String licenseKeyPattern) {
            this.sku = Objects.requireNonNull(sku, "SKU не може бути null");
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Назва не може бути порожньою.");
            }
            if (licenseKeyPattern == null || licenseKeyPattern.trim().isEmpty()) {
                throw new IllegalArgumentException("Шаблон ключа не може бути порожнім.");
            }
            this.title = title;
            this.unitPrice = Objects.requireNonNull(unitPrice, "Ціна не може бути null");
            this.licenseKeyPattern = licenseKeyPattern;
        }

        @Override public SKU getSku() { return sku; }
        @Override public String getTitle() { return title; }
        @Override public Money getUnitPrice() { return unitPrice; }
        public String getLicenseKeyPattern() { return licenseKeyPattern; }
    }

    static class WarehouseManager {
        // Метод приймає тільки PhysicalReturnable, захищаючи від помилок на етапі компіляції
        public void processDefectiveReturn(PhysicalReturnable item, int quantity) {
            item.returnToSupplier(quantity);
        }
    }

    public static void main(String[] args) {
        WarehouseManager manager = new WarehouseManager();
        PhysicalInventoryItem mouse = new PhysicalInventoryItem(
                new SKU("LOGI123"),
                "Миша Logitech G Pro",
                15,
                new Money(new BigDecimal("1499.00"))
        );

        mouse.restock(5);
        manager.processDefectiveReturn(mouse, 3);

        DigitalLicenseItem winLicense = new DigitalLicenseItem(
                new SKU("WIN11PRO"),
                "Windows 11 Pro Key",
                new Money(new BigDecimal("4500.00")),
                "XXXXX-XXXXX-XXXXX-XXXXX"
        );

        System.out.println("Створено цифровий товар: " + winLicense.getTitle() + " (Ціна: " + winLicense.getUnitPrice() + ")");

    }
}