import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FileSizeFilter {

    // Головний метод
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Введення директорії
            System.out.print("Введіть шлях до директорії: ");
            String directoryPath = scanner.nextLine();
            File directory = new File(directoryPath);

            // Перевірка існування директорії
            if (!directory.exists() || !directory.isDirectory()) {
                System.err.println("Помилка: Шлях не існує або це не директорія.");
                return;
            }

            // Введення мінімального розміру файлу
            System.out.print("Введіть мінімальний розмір файлу (в байтах): ");
            long minSize = scanner.nextLong();

            // Створення ForkJoinPool і виконання задачі
            ForkJoinPool pool = new ForkJoinPool();
            FileSearchTask rootTask = new FileSearchTask(directory, minSize);

            System.out.println("Пошук файлів...");
            pool.invoke(rootTask);

            System.out.println("Пошук завершено.");
        } catch (Exception e) {
            System.err.println("Помилка виконання: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    // Клас задачі для Fork/Join
    static class FileSearchTask extends RecursiveTask<List<File>> {
        private final File directory;
        private final long minSize;

        public FileSearchTask(File directory, long minSize) {
            this.directory = directory;
            this.minSize = minSize;
        }

        @Override
        protected List<File> compute() {
            List<FileSearchTask> subTasks = new ArrayList<>();
            List<File> resultFiles = new ArrayList<>();

            // Отримання файлів у поточній директорії
            File[] files = directory.listFiles();
            if (files == null) {
                System.err.println("Помилка: директорія пуста або недоступна: " + directory.getAbsolutePath());
                return resultFiles; // Повертаємо порожній список
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    // Якщо це піддиректорія, створюємо підзадачу
                    System.out.println("Знайдено піддиректорію: " + file.getAbsolutePath());
                    FileSearchTask subTask = new FileSearchTask(file, minSize);
                    subTasks.add(subTask);
                } else if (file.isFile() && file.length() >= minSize) {
                    // Якщо це файл і він відповідає розміру
                    System.out.println("Знайдено файл: " + file.getName() + ", Розмір: " + file.length() + " байт");
                    resultFiles.add(file);
                }
            }

            // Рекурсивно виконуємо підзадачі
            if (!subTasks.isEmpty()) {
                invokeAll(subTasks);
                for (FileSearchTask task : subTasks) {
                    resultFiles.addAll(task.join()); // Додаємо результати підзадач
                }
            }

            return resultFiles;
        }
    }
}
