import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ShopCatalog {
    private String productName;
    private String unitOfMeasure;
    private int quantity;
    private double unitPrice;
    private String arrivalDate;
    private List<String> description;

    public ShopCatalog(String productName, String unitOfMeasure, int quantity, double unitPrice, String arrivalDate, List<String> description) {
        this.productName = productName;
        this.unitOfMeasure = unitOfMeasure;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.arrivalDate = arrivalDate;
        this.description = description;
    }

    public String getProductName() {
        return productName;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public String getArrivalDate() {
        return arrivalDate;
    }

    public List<String> getDescription() {
        return description;
    }

    // Головний метод для паралельної обробки каталогу
    public static List<ShopCatalog> processCatalogInParallel(List<ShopCatalog> catalog) {
        int processors = Runtime.getRuntime().availableProcessors();
        ForkJoinPool forkJoinPool = new ForkJoinPool(processors);
        return forkJoinPool.invoke(new CatalogProcessor(catalog, 0, catalog.size()));
    }

    private static class CatalogProcessor extends RecursiveTask<List<ShopCatalog>> {
        private static final int THRESHOLD = 5;
        private List<ShopCatalog> catalog;
        private int start;
        private int end;

        public CatalogProcessor(List<ShopCatalog> catalog, int start, int end) {
            this.catalog = catalog;
            this.start = start;
            this.end = end;
        }

        @Override
        protected List<ShopCatalog> compute() {
            if (end - start <= THRESHOLD) {
                return processSequentially();
            } else {
                int middle = (start + end) / 2;
                CatalogProcessor leftTask = new CatalogProcessor(catalog, start, middle);
                CatalogProcessor rightTask = new CatalogProcessor(catalog, middle, end);
                invokeAll(leftTask, rightTask);

                List<ShopCatalog> leftResult = leftTask.join();
                List<ShopCatalog> rightResult = rightTask.join();

                return merge(leftResult, rightResult);
            }
        }

        private List<ShopCatalog> processSequentially() {
            List<ShopCatalog> result = new ArrayList<>();
            for (int i = start; i < end; i++) {
                result.add(catalog.get(i));
            }
            return result;
        }

        private List<ShopCatalog> merge(List<ShopCatalog> left, List<ShopCatalog> right) {
            List<ShopCatalog> result = new ArrayList<>();
            result.addAll(left);
            result.addAll(right);
            return result;
        }
    }

    public static void main(String[] args) {
        List<ShopCatalog> catalog = new ArrayList<>();
        catalog.add(new ShopCatalog("ItemA", "kg", 10, 2.5, "2024-01-01", Collections.singletonList("FeatureA: ValueA")));
        catalog.add(new ShopCatalog("ItemB", "unit", 20, 1.75, "2024-01-02", Collections.singletonList("FeatureB: ValueB")));
        catalog.add(new ShopCatalog("ItemC", "liter", 5, 4.0, "2024-01-03", Collections.singletonList("FeatureC: ValueC")));

        System.out.println("Before sorting:");
        printCatalog(catalog);

        // Сортування каталогу за найменуванням товару
        Collections.sort(catalog, (item1, item2) -> item1.getProductName().compareTo(item2.getProductName()));
        System.out.println("\nAfter sorting by product name:");
        printCatalog(catalog);

        // Сортування каталогу за ціною одиниці
        Collections.sort(catalog, (item1, item2) -> Double.compare(item1.getUnitPrice(), item2.getUnitPrice()));
        System.out.println("\nAfter sorting by unit price:");
        printCatalog(catalog);

        // Сортування каталогу за датою надходження
        Collections.sort(catalog, (item1, item2) -> item1.getArrivalDate().compareTo(item2.getArrivalDate()));
        System.out.println("\nAfter sorting by arrival date:");
        printCatalog(catalog);

        // Паралельна обробка каталогу
        long parallelStartTime = System.currentTimeMillis();
        List<ShopCatalog> parallelResult = ShopCatalog.processCatalogInParallel(catalog);
        long parallelEndTime = System.currentTimeMillis();
        System.out.println("\nParallel processing time: " + (parallelEndTime - parallelStartTime) + " ms");

        // Послідовна обробка каталогу
        long sequentialStartTime = System.currentTimeMillis();
        List<ShopCatalog> sequentialResult = processSequentially(catalog);
        long sequentialEndTime = System.currentTimeMillis();
        System.out.println("Sequential processing time: " + (sequentialEndTime - sequentialStartTime) + " ms");
    }

    private static List<ShopCatalog> processSequentially(List<ShopCatalog> catalog) {
        List<ShopCatalog> result = new ArrayList<>();

        for (ShopCatalog item : catalog) {
            // Логіка обробки послідовно
            int descriptionSize = item.getDescription().size();

            result.add(new ShopCatalog(
                    item.getProductName(),
                    item.getUnitOfMeasure(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getArrivalDate(),
                    new ArrayList<>(item.getDescription())
            ));

            // Вивід розміру опису для кожного товару
            System.out.println("Item " + item.getProductName() + " - Description Size: " + descriptionSize);
        }

        return result;
    }

    private static void printCatalog(List<ShopCatalog> catalog) {
        for (ShopCatalog item : catalog) {
            System.out.println("Item " + item.getProductName() + " - Unit Price: " +
                    item.getUnitPrice() + ", Arrival Date: " + item.getArrivalDate());
        }
    }
}
