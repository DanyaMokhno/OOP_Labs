/**
 * Лабораторная работа №1
 *
 * @author Даниил Мохно 3312
 * @version 1.0
 */
public class Main {
    /**
     * Точка входа в программу. Выполняет сортировку заранее заданного массива.
     *
     * @param args аргументы командной строки (в данном случае не используются)
     */
    public static void main(String[] args) {
        // целочисленный массив
        int[] arr = {
                4, 3, 5, 909, 404, 33, 84, 2, 99
        };

        System.out.println("Массив до сортировки:");

        // Вывод исходного массива
        for (int k : arr) System.out.print(k + " ");

        // Сортировка массива
        sort(arr, 0, arr.length - 1);

        System.out.println("\nМассив после сортировки по возрастанию:");

        // Вывод отсортированного массива
        for (int j : arr) System.out.print(j + " ");
    }

    /**
     * Функция быстрой сортировки
     *
     * @param arr массив чисел
     * @param b   правая граница массива
     * @param e   левая граница массива
     */
    public static void sort(int[] arr, int b, int e) {
        int left = b, right = e, middle;
        // Опорный элемент — значение в середине массива
        middle = arr[(left + right) / 2];

        // Основной цикл сортировки
        while (left <= right) {
            while (arr[left] < middle) left++;
            while (arr[right] > middle) right--;
            if (left <= right) swap(arr, left++, right--);
        }

        // Рекурсивная сортировка подмассивов
        if (b < right) sort(arr, b, right);
        if (e > left) sort(arr, left, e);
    }

    /**
     * Функция смены местами двух элементов массива
     *
     * @param arr массив, в котором происходит замена
     * @param i   индекс первого элемента
     * @param j   индекс второго элемента
     */
    static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}