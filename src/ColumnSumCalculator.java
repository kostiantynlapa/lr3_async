import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ColumnSumCalculator {
    public static void main(String[] args) {
        // Введення даних користувача
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.println("Введіть кількість рядків матриці:");
        int rows = scanner.nextInt();
        System.out.println("Введіть кількість стовпців матриці:");
        int columns = scanner.nextInt();
        System.out.println("Введіть мінімальне значення елементів:");
        int min = scanner.nextInt();
        System.out.println("Введіть максимальне значення елементів:");
        int max = scanner.nextInt();

        // Генерація матриці
        int[][] matrix = generateMatrix(rows, columns, min, max);
        System.out.println("Згенерована матриця:");
        printMatrix(matrix);

        // Обчислення сум
        ForkJoinPool pool = new ForkJoinPool();
        long startTime = System.nanoTime();
        int[] columnSums = pool.invoke(new ColumnSumTask(matrix, 0, columns));
        long endTime = System.nanoTime();

        // Результати
        System.out.println("Суми елементів у стовпцях:");
        for (int i = 0; i < columnSums.length; i++) {
            System.out.println("Стовпець " + (i + 1) + ": " + columnSums[i]);
        }
        System.out.println("Час виконання: " + (endTime - startTime) / 1_000_000 + " мс");
    }

    private static int[][] generateMatrix(int rows, int columns, int min, int max) {
        Random random = new Random();
        int[][] matrix = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = random.nextInt(max - min + 1) + min;
            }
        }
        return matrix;
    }

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + "\t");
            }
            System.out.println();
        }
    }

    static class ColumnSumTask extends RecursiveTask<int[]> {
        private final int[][] matrix;
        private final int startColumn;
        private final int endColumn;

        private static final int THRESHOLD = 2;

        public ColumnSumTask(int[][] matrix, int startColumn, int endColumn) {
            this.matrix = matrix;
            this.startColumn = startColumn;
            this.endColumn = endColumn;
        }

        @Override
        protected int[] compute() {
            if (endColumn - startColumn <= THRESHOLD) {
                int[] sums = new int[endColumn - startColumn];
                for (int col = startColumn; col < endColumn; col++) {
                    int sum = 0;
                    for (int[] row : matrix) {
                        sum += row[col];
                    }
                    sums[col - startColumn] = sum;
                }
                return sums;
            } else {
                int mid = (startColumn + endColumn) / 2;
                ColumnSumTask leftTask = new ColumnSumTask(matrix, startColumn, mid);
                ColumnSumTask rightTask = new ColumnSumTask(matrix, mid, endColumn);
                invokeAll(leftTask, rightTask);
                int[] leftResult = leftTask.join();
                int[] rightResult = rightTask.join();
                int[] result = new int[leftResult.length + rightResult.length];
                System.arraycopy(leftResult, 0, result, 0, leftResult.length);
                System.arraycopy(rightResult, 0, result, leftResult.length, rightResult.length);
                return result;
            }
        }
    }
}
