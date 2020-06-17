package phonebook;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        // Load files
        File dirFile = new File("./directory.txt");
        File findFile = new File("./find.txt");

        var dirArr = new ArrayList<String[]>();
        var findArr = new ArrayList<String>();

        try (Scanner scannerDir = new Scanner(dirFile)) {
            while (scannerDir.hasNext()) {
                dirArr.add(new String[]{Integer.toString(scannerDir.nextInt()), scannerDir.nextLine().substring(1)});
                //[num, name]
            }
        } catch (FileNotFoundException e) {
            System.out.println("No file found: " + dirFile.getAbsolutePath());
        }

        try (Scanner scannerFind = new Scanner(findFile)) {
            while (scannerFind.hasNext()) {
                findArr.add(scannerFind.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("No file found: " + findFile.getAbsolutePath());
        }

        // Linear Search
        long thresholdTime = 10L * (linearSearch(dirArr, findArr));

        // Bubble Sort and Jump Search
        boolean isBubbleSortCompleted = bubbleSort(dirArr, findArr, thresholdTime);

        // Quick Sort and Binary Search
        quickSortWrapper(dirArr, findArr, 0, dirArr.size()-1);

        // Hash Table
        hashTableSearch(dirArr, findArr);
    }

    public static long linearSearch(ArrayList<String[]> dirArr, ArrayList<String> findArr) {
        return linearSearch(dirArr, findArr, false, 0L);
    }

    public static long linearSearch(ArrayList<String[]> dirArr, ArrayList<String> findArr, boolean isBubbleAborted, long bubbleTime) {
        int matchesCount = 0;
        //int numFindEntries = findArr.size();
        if (!isBubbleAborted) {
            System.out.println("Start searching (linear search)...");
        }
        long time0 = System.currentTimeMillis();
        for (String findElem : findArr) {
            for (String[] dirElem : dirArr) {
                if (findElem.equals(dirElem[1])) {
                    matchesCount++;
                    break;
                }
            }
        }
        long time1 = System.currentTimeMillis();
        long timeTaken = time1-time0;

        long[] timeTakenSearchBreakdown = timeBreakdown(timeTaken);
        long[] timeTakenSearchSortBreakdown = timeBreakdown(timeTaken+bubbleTime);
        long[] timeTakenSortBreakdown = timeBreakdown(bubbleTime);

        if (!isBubbleAborted) {
            System.out.printf("Found %d / %d entries. Time taken: %d min. %d sec. %d ms.%n%n", matchesCount, findArr.size(), timeTakenSearchBreakdown[0], timeTakenSearchBreakdown[1], timeTakenSearchBreakdown[2]);
        } else {
            System.out.printf("Found %d / %d entries. Time taken: %d min. %d sec. %d ms.%n", matchesCount, findArr.size(), timeTakenSearchSortBreakdown[0], timeTakenSearchSortBreakdown[1], timeTakenSearchSortBreakdown[2]);
            System.out.printf("Sorting time: %d min. %d sec. %d ms. - STOPPED, moved to linear search%n", timeTakenSortBreakdown[0], timeTakenSortBreakdown[1], timeTakenSortBreakdown[2]);
            System.out.printf("Searching time: %d min. %d sec. %d ms.%n", timeTakenSearchBreakdown[0], timeTakenSearchBreakdown[1], timeTakenSearchBreakdown[2]);
        }

        return timeTaken;
    }

    public static boolean bubbleSort(ArrayList<String[]> dirArr, ArrayList<String> findArr, long thresholdTime) {
        // issue boolean so can break out of method with return if it exceeds threshold
        long time0 = System.currentTimeMillis();
        ArrayList<String[]> origDirArr = cloneList(dirArr);

        System.out.println("Start searching (bubble sort + jump search)...");

        for (int i=0; i<dirArr.size()-1; i++) {
            for (int j=0; j<dirArr.size()-i-1; j++) {
                if (System.currentTimeMillis()-time0>thresholdTime) {
                    long time1 = System.currentTimeMillis();
                    long timeTaken = time1-time0;
                    linearSearch(origDirArr, findArr, true, timeTaken);
                    return false;
                }
                if (isFirstAlphabeticallyFirst(dirArr.get(j+1)[1],dirArr.get(j)[1])) {
                    String[] temp = dirArr.get(j);
                    dirArr.set(j, dirArr.get(j+1));
                    dirArr.set(j+1, temp);
                }
            }
        }
        long time1 = System.currentTimeMillis();
        long timeTaken = time1-time0;

        // send sorted dirArr to jumpSearch
        jumpSearch(dirArr, findArr, timeTaken);
        return true;
        //return String.format("Sorting time: %d min. %d sec. %d ms.", timeTakenMin, timeTakenSec, timeTakenMillis);
    }

    public static void jumpSearch(ArrayList<String[]> dirArr, ArrayList<String> findArr, long bubbleTime) {
        int jumpMatchCount=0;

        long time0 = System.currentTimeMillis();
        for (String target : findArr) {
            if (jumpSearchSingle(dirArr, target)) {
                jumpMatchCount++;
            }
        }

        long time1 = System.currentTimeMillis();
        long timeTaken = time1-time0;

        long[] timeTakenSearchBreakdown = timeBreakdown(timeTaken);
        long[] timeTakenSearchSortBreakdown = timeBreakdown(timeTaken+bubbleTime);
        long[] timeTakenSortBreakdown = timeBreakdown(bubbleTime);

        System.out.printf("Found %d / %d entries. Time taken: %d min. %d sec. %d ms.%n", jumpMatchCount, findArr.size(), timeTakenSearchSortBreakdown[0], timeTakenSearchSortBreakdown[1], timeTakenSearchSortBreakdown[2]);
        System.out.printf("Sorting time: %d min. %d sec. %d ms.%n", timeTakenSortBreakdown[0], timeTakenSortBreakdown[1], timeTakenSortBreakdown[2]);
        System.out.printf("Searching time: %d min. %d sec. %d ms.%n%n", timeTakenSearchBreakdown[0], timeTakenSearchBreakdown[1], timeTakenSearchBreakdown[2]);
    }

    public static boolean jumpSearchSingle(ArrayList<String[]> arr, String target) {
        // return true for match, false for none

        //arr[0] is phone number as String
        //arr[1] is name as String

        int jumpLength = (int) (Math.sqrt(arr.size()));

        int prevRight = 0;
        int currRight = 0;
        //int count=1;

        if (arr.size()<1) {
            return false;
        }

        if (arr.get(0)[1].equals(target)) {
            return true;
        }

        while (currRight<arr.size()-1) {
            currRight = Math.min(arr.size()-1, currRight+jumpLength);

            if (isFirstAlphabeticallyFirst(target,arr.get(currRight)[1])) {
                // found a block possibly containing our target
                break;
            }

            prevRight=currRight;
        }

        if ((currRight==arr.size()-1) && isFirstAlphabeticallyFirst(arr.get(currRight)[1],target)) {
            return false;
        }

        if (arr.get(currRight)[1].equals(target)) {
            return true;
        }

        return backwardSearch(arr, target, currRight, prevRight);

    }

    public static boolean backwardSearch(ArrayList<String[]> arr, String target, int currRight, int prevRight) {
        for (int i=currRight-1; i>prevRight; i--) {
            if (arr.get(i)[1].equals(target)) {
                return true;
            } else if (isFirstAlphabeticallyFirst(arr.get(i)[1], target)) {
                return false;
            }
        }

        return false;
    }

    public static void quickSortWrapper(ArrayList<String[]> dirArr, ArrayList<String> findArr, int left, int right) {
        // sorts left to right inclusive
        long time0 = System.currentTimeMillis();
        System.out.println("Start searching (quick sort + binary search)...");
        quickSort(dirArr, left, right);
        long time1 = System.currentTimeMillis();
        long timeTaken = time1-time0;

        // send sorted dirArr to binarySearch
        binarySearch(dirArr, findArr, timeTaken);
    }

    public static void quickSort(ArrayList<String[]> dirArr, int left, int right) {
        if (left < right) {
            int pivotIndex = partition(dirArr, left, right); // the pivot is already on its place
            quickSort(dirArr, left, pivotIndex - 1);  // sort the left subarray
            quickSort(dirArr, pivotIndex + 1, right); // sort the right subarray
        }
    }

    private static int partition(ArrayList<String[]> array, int left, int right) {
        String pivot = array.get(right)[1]; //array[right];  // choose the rightmost element as the pivot
        int partitionIndex = left; // the first element greater than the pivot

        /* move large values into the right side of the array */
        for (int i = left; i < right; i++) {
            if (isFirstAlphabeticallyFirst(array.get(i)[1],pivot)) {
                swap(array, i, partitionIndex);
                partitionIndex++;
            }
        }

        swap(array, partitionIndex, right); // put the pivot on a suitable position

        return partitionIndex;
    }

    private static void swap(ArrayList<String[]> array, int i, int j) {
        String[] iVal = array.get(i);
        String[] jVal = array.get(j);
        array.set(i, jVal);;
        array.set(j, iVal);;
    }

    public static void binarySearch(ArrayList<String[]> dirArr, ArrayList<String> findArr, long qsTime) {
        int binMatchCount=0;

        long time0 = System.currentTimeMillis();
        for (String target : findArr) {
            if (binarySearchSingle(dirArr, target, 0, dirArr.size()-1)) {
                binMatchCount++;
            }
        }

        long time1 = System.currentTimeMillis();
        long timeTaken = time1-time0;

        long[] timeTakenSearchBreakdown = timeBreakdown(timeTaken);
        long[] timeTakenSearchSortBreakdown = timeBreakdown(timeTaken+qsTime);
        long[] timeTakenSortBreakdown = timeBreakdown(qsTime);

        System.out.printf("Found %d / %d entries. Time taken: %d min. %d sec. %d ms.%n", binMatchCount, findArr.size(), timeTakenSearchSortBreakdown[0], timeTakenSearchSortBreakdown[1], timeTakenSearchSortBreakdown[2]);
        System.out.printf("Sorting time: %d min. %d sec. %d ms.%n", timeTakenSortBreakdown[0], timeTakenSortBreakdown[1], timeTakenSortBreakdown[2]);
        System.out.printf("Searching time: %d min. %d sec. %d ms.%n%n", timeTakenSearchBreakdown[0], timeTakenSearchBreakdown[1], timeTakenSearchBreakdown[2]);
    }

    public static boolean binarySearchSingle(ArrayList<String[]> dirArr, String elem, int left, int right) {
        while (left <= right) {
            int mid = left + (right - left) / 2; // the index of the middle element

            if (elem.equals(dirArr.get(mid)[1])) {
                //return mid; // the element is found, return its index
                return true; // the element is found, return true
            } else if (isFirstAlphabeticallyFirst(elem,dirArr.get(mid)[1])) {
                right = mid - 1; // go to the left subarray
            } else {
                left = mid + 1;  // go the the right subarray
            }
        }
        //return -1; // the element is not found
        return false; // the element is not found
    }

    public static boolean isFirstAlphabeticallyFirst(String first, String second) {
        for (int k=0; k<Math.min(first.length(),second.length()); k++) {
            if (first.toLowerCase().charAt(k)<second.toLowerCase().charAt(k)) {
                return true;
            } else if (first.toLowerCase().charAt(k)>second.toLowerCase().charAt(k)) {
                return false;
            }
        }

        return first.length() <= second.length();

    }

    public static ArrayList<String[]> cloneList(ArrayList<String[]> dirArr) {
        ArrayList<String[]> clone = new ArrayList<String[]>(dirArr.size());
        for (String[] item : dirArr) {
            clone.add(item.clone());
        }
        return clone;
    }

    public static long[] timeBreakdown(long timeTaken) {
        long timeTakenMin = Math.floorDiv(timeTaken, 60000L);
        long timeTakenNotInMin= timeTaken - timeTakenMin*60000L;
        long timeTakenSec = Math.floorDiv(timeTakenNotInMin, 1000L);
        long timeTakenMillis = (timeTaken - (timeTakenMin*60000L) - (timeTakenSec*1000L));

        return new long[] {timeTakenMin, timeTakenSec, timeTakenMillis};
    }

    public static void hashTableSearch(ArrayList<String[]> dirArr, ArrayList<String> findArr) {

        System.out.println("Start searching (hash table)...");

        long timeCreate0 = System.currentTimeMillis();


        var hashTable = new HashTable<String>(1318379);


        for (int i=0; i<dirArr.size(); i++) {
            hashTable.put(dirArr.get(i)[1],Integer.parseInt(dirArr.get(i)[0]));
        }

        long timeCreate1 = System.currentTimeMillis();
        long timeTakenCreate = timeCreate1-timeCreate0;
        long[] timeTakenCreateBreakdown = timeBreakdown(timeTakenCreate);

        long timeSearch0 = System.currentTimeMillis();
        int count =0;
        for (int j=0; j<findArr.size(); j++) {
            if (!((hashTable.get(findArr.get(j)))==-1)) {
                count++;
            }
        }

        long timeSearch1 = System.currentTimeMillis();
        long timeTakenSearch = timeSearch1-timeSearch0;
        long[] timeTakenSearchBreakdown = timeBreakdown(timeTakenSearch);

        long[] timeTakenCreateSearchBreakdown = timeBreakdown(timeTakenCreate+timeTakenSearch);

        System.out.printf("Found %d / %d entries. Time taken: %d min. %d sec. %d ms.%n", count, findArr.size(), timeTakenCreateSearchBreakdown[0], timeTakenCreateSearchBreakdown[1], timeTakenCreateSearchBreakdown[2]);
        System.out.printf("Creating time: %d min. %d sec. %d ms.%n", timeTakenCreateBreakdown[0], timeTakenCreateBreakdown[1], timeTakenCreateBreakdown[2]);
        System.out.printf("Searching time: %d min. %d sec. %d ms.%n", timeTakenSearchBreakdown[0], timeTakenSearchBreakdown[1], timeTakenSearchBreakdown[2]);
    }
}
