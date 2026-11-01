import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

// 1. Доменна модель товару
class ImportGoods {
    private final String goodsCode;
    private final String name;
    private final BigDecimal declaredValue;
    private final BigDecimal weightKg;

    public ImportGoods(String goodsCode, String name, BigDecimal declaredValue, BigDecimal weightKg) {
        this.goodsCode = goodsCode;
        this.name = name;
        this.declaredValue = declaredValue;
        this.weightKg = weightKg;
    }

    public String getGoodsCode() { return goodsCode; }
    public String getName() { return name; }
    public BigDecimal getDeclaredValue() { return declaredValue; }
    public BigDecimal getWeightKg() { return weightKg; }
}

// 2. Результат розрахунку (Value Object)
record TaxCalculationResult(
        BigDecimal importDuty,
        BigDecimal exciseTax,
        BigDecimal vatAmount,
        BigDecimal totalTax
) {}

// 3. Інтерфейс Стратегії
interface TaxStrategy {
    String getCategoryName();
    TaxCalculationResult calculateTax(ImportGoods goods);
}

// 4. Конкретні стратегії для кожної категорії товарів
class ElectronicsTaxStrategy implements TaxStrategy {
    @Override
    public String getCategoryName() { return "ELECTRONICS"; }

    @Override
    public TaxCalculationResult calculateTax(ImportGoods goods) {
        BigDecimal declaredValue = goods.getDeclaredValue();
        BigDecimal importDuty = declaredValue.multiply(new BigDecimal("0.05"));
        BigDecimal exciseTax = BigDecimal.ZERO;
        BigDecimal vatRate = new BigDecimal("0.20");

        BigDecimal taxableAmount = declaredValue.add(importDuty).add(exciseTax);
        BigDecimal vatAmount = taxableAmount.multiply(vatRate);
        BigDecimal totalTax = importDuty.add(exciseTax).add(vatAmount).setScale(2, RoundingMode.HALF_UP);

        return new TaxCalculationResult(importDuty, exciseTax, vatAmount, totalTax);
    }
}

class FoodTaxStrategy implements TaxStrategy {
    @Override
    public String getCategoryName() { return "FOOD"; }

    @Override
    public TaxCalculationResult calculateTax(ImportGoods goods) {
        BigDecimal declaredValue = goods.getDeclaredValue();
        BigDecimal importDuty = declaredValue.multiply(new BigDecimal("0.10"))
                .add(goods.getWeightKg().multiply(new BigDecimal("0.50")));
        BigDecimal exciseTax = BigDecimal.ZERO;
        BigDecimal vatRate = new BigDecimal("0.07");

        BigDecimal taxableAmount = declaredValue.add(importDuty).add(exciseTax);
        BigDecimal vatAmount = taxableAmount.multiply(vatRate);
        BigDecimal totalTax = importDuty.add(exciseTax).add(vatAmount).setScale(2, RoundingMode.HALF_UP);

        return new TaxCalculationResult(importDuty, exciseTax, vatAmount, totalTax);
    }
}

class LuxuryGoodsTaxStrategy implements TaxStrategy {
    @Override
    public String getCategoryName() { return "LUXURY_GOODS"; }

    @Override
    public TaxCalculationResult calculateTax(ImportGoods goods) {
        BigDecimal declaredValue = goods.getDeclaredValue();
        BigDecimal importDuty = declaredValue.multiply(new BigDecimal("0.25"));
        BigDecimal exciseTax = declaredValue.multiply(new BigDecimal("0.15"));
        BigDecimal vatRate = new BigDecimal("0.20");

        BigDecimal taxableAmount = declaredValue.add(importDuty).add(exciseTax);
        BigDecimal vatAmount = taxableAmount.multiply(vatRate);
        BigDecimal totalTax = importDuty.add(exciseTax).add(vatAmount).setScale(2, RoundingMode.HALF_UP);

        return new TaxCalculationResult(importDuty, exciseTax, vatAmount, totalTax);
    }
}

class PharmaceuticalsTaxStrategy implements TaxStrategy {
    @Override
    public String getCategoryName() { return "PHARMACEUTICALS"; }

    @Override
    public TaxCalculationResult calculateTax(ImportGoods goods) {
        BigDecimal declaredValue = goods.getDeclaredValue();
        BigDecimal importDuty = BigDecimal.ZERO;
        BigDecimal exciseTax = BigDecimal.ZERO;
        BigDecimal vatRate = new BigDecimal("0.07");

        BigDecimal taxableAmount = declaredValue.add(importDuty).add(exciseTax);
        BigDecimal vatAmount = taxableAmount.multiply(vatRate);
        BigDecimal totalTax = importDuty.add(exciseTax).add(vatAmount).setScale(2, RoundingMode.HALF_UP);

        return new TaxCalculationResult(importDuty, exciseTax, vatAmount, totalTax);
    }
}

class AutomobileTaxStrategy implements TaxStrategy {
    @Override
    public String getCategoryName() { return "AUTOMOBILES"; }

    @Override
    public TaxCalculationResult calculateTax(ImportGoods goods) {
        BigDecimal declaredValue = goods.getDeclaredValue();
        BigDecimal importDuty = declaredValue.multiply(new BigDecimal("0.10"));
        BigDecimal weightInTons = goods.getWeightKg().divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
        BigDecimal exciseTax = weightInTons.multiply(new BigDecimal("100"));
        BigDecimal vatRate = new BigDecimal("0.20");

        BigDecimal taxableAmount = declaredValue.add(importDuty).add(exciseTax);
        BigDecimal vatAmount = taxableAmount.multiply(vatRate);
        BigDecimal totalTax = importDuty.add(exciseTax).add(vatAmount).setScale(2, RoundingMode.HALF_UP);

        return new TaxCalculationResult(importDuty, exciseTax, vatAmount, totalTax);
    }
}

// 5. Головний клас із точкою входу main
public class task2 {
    public BigDecimal calculateTotalTax(ImportGoods goods, TaxStrategy strategy) {
        Objects.requireNonNull(goods, "Товар не може бути null");
        Objects.requireNonNull(strategy, "Стратегія розрахунку податку не може бути null");

        TaxCalculationResult result = strategy.calculateTax(goods);

        System.out.println("[TAX] Товар '" + goods.getName() + "' (" + strategy.getCategoryName() +
                "): Мито=$" + result.importDuty() +
                ", Акциз=$" + result.exciseTax() +
                ", ПДВ=$" + result.vatAmount() +
                " | Всього до сплати: $" + result.totalTax());

        return result.totalTax();
    }

    // ТОЧКА ВХОДУ ДЛЯ ЗАПУСКУ ПРОГРАМИ
    public static void main(String[] args) {
        task2 calculator = new task2();

        // Тест 1: Електроніка
        ImportGoods laptop = new ImportGoods("G-001", "Ноутбук", new BigDecimal("1000"), new BigDecimal("2.5"));
        calculator.calculateTotalTax(laptop, new ElectronicsTaxStrategy());

        // Тест 2: Продукти
        ImportGoods apples = new ImportGoods("G-002", "Яблука", new BigDecimal("500"), new BigDecimal("100"));
        calculator.calculateTotalTax(apples, new FoodTaxStrategy());
    }
}