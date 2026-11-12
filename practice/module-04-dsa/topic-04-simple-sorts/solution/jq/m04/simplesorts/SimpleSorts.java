package jq.m04.simplesorts;

public class SimpleSorts {

    public static void bubble(int[] a) {
        bubblePasses(a);
    }

    public static int bubblePasses(int[] a) {
        int passes = 0;
        for (int pass = 0; pass < a.length - 1; pass++) {
            passes++;
            boolean swapped = false;
            for (int j = 0; j < a.length - 1 - pass; j++) {
                if (a[j] > a[j + 1]) {
                    swap(a, j, j + 1);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
        return passes;
    }

    public static void selection(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < a.length; j++) {
                if (a[j] < a[min]) {
                    min = j;
                }
            }
            swap(a, i, min);
        }
    }

    public static void insertion(int[] a) {
        for (int i = 1; i < a.length; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= 0 && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    private static void swap(int[] a, int i, int j) {
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }
}
