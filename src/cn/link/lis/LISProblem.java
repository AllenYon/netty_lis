package cn.link.lis;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Link
 * Date: 13-11-12
 * Time: AM10:45
 * To change this template use File | Settings | File Templates.
 */
public class LISProblem {

    public int[] randomIntArray;
    public int bestSolve;
    public int[] bestSolveSuq;


    public static int[] randomIntArray(int count_num, int max_num) {
        Random random = new Random();
        int[] r = new int[count_num];
        for (int i = 0; i < count_num; i++) {
            r[i] = random.nextInt(max_num + 1) + 1;
        }
        return r;
    }

    public static LISProblem solve(int[] randomIntArray) {
        LISProblem lp = new LISProblem();
        lp.randomIntArray = randomIntArray;
        int[] temp = new int[randomIntArray.length];
        lp.bestSolve = 1;
        for (int i = 0; i < randomIntArray.length; ++i) {
            temp[i] = 1;
            for (int j = 0; j < i; ++j) {
//                if (A[j] <= A[i] && d[j] + 1 > d[i])
                if (randomIntArray[j] < randomIntArray[i] && temp[j] + 1 > temp[i]) {
                    temp[i] = temp[j] + 1;
                }
            }
            if (temp[i] > lp.bestSolve) {
                lp.bestSolve = temp[i];
            }
        }
        int tempMax = lp.bestSolve;
        lp.bestSolveSuq = new int[lp.bestSolve];
        int j = 0;
        for (int i = temp.length - 1; i >= 0; i--) {
            if (temp[i] == tempMax) {
                lp.bestSolveSuq[j] = randomIntArray[i];
                tempMax--;
                j++;
            }
        }
        return lp;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < randomIntArray.length; i++) {
            sb.append(randomIntArray[i] + " ");
        }

        StringBuffer sb2 = new StringBuffer();
        for (int i = 0; i < bestSolveSuq.length; i++) {
            sb2.append(bestSolveSuq[i] + " ");

        }

        return "LISProblem{" +
                "randomIntArray=" + sb.toString() +
                ", bestSolve=" + bestSolve +
                ", bestSolveSuq=" + sb2.toString() +
                '}';
    }
}
