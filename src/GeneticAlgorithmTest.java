import org.junit.Test;
import java.io.IOException;
import static java.lang.Math.random;
import java.util.Random;
import static org.junit.Assert.*;

public class GeneticAlgorithmTest {


    @Test
    public void evaluateTest() throws IOException {
        GeneticAlgorithm ga = new GeneticAlgorithm(30, 20, 100, 0.99f, 0.01f);
        ga.init("citys data.txt");
        int i, j, k;
        // Random random = new Random(System.currentTimeMillis());
        int[][] oldPopulation = new int[30][20];
        for (k = 0; k < 30; k++)// population size
        {

            oldPopulation[k][0] = 20 % 20;
            for (i = 1; i < 20; )// chromosome length
            {
                oldPopulation[k][i] = i % 20;
                for (j = 0; j < i; j++) {
                    if (oldPopulation[k][i] == oldPopulation[k][j]) {
                        break;
                    }
                }
                if (j == i) {
                    i++;
                }
            }
        }
        assertEquals(2414, ga.evaluate(oldPopulation[0]));

    }

    @Test
    public void countRateTest() throws IOException {
        GeneticAlgorithm ga = new GeneticAlgorithm(30, 20, 100, 0.99f, 0.01f);
        ga.init("citys data.txt");
        ga.setFitness();
        ga.countRate();
        float[] pi = ga.getPi();
        assertEquals(0.033814207, pi[0], 0.0001);

    }

    @Test
    public void selectBestGhTest() throws IOException {
        GeneticAlgorithm ga = new GeneticAlgorithm(30, 20, 100, 0.99f, 0.01f);
        ga.init("citys data.txt");
        ga.setOldPopulation();
        ga.setFitness();
        ga.selectBestGh();
        int[] bt = ga.getBestTour();
        assertEquals(0, bt[0]);
        assertEquals(1, bt[1]);
        assertEquals(2, bt[2]);
    }

    @Test
    public void copyGhTest() throws IOException {
        GeneticAlgorithm ga = new GeneticAlgorithm(30, 20, 100, 0.99f, 0.01f);
        ga.init("citys data.txt");
        //ga.solve();
        ga.copyGh(0, 1);
        int[][] newPopulation = ga.getNewPopulation();
        int[][] oldPopulation = ga.getOldPopulation();
        for (int i = 0; i < 20; i++) {
            assertTrue(newPopulation[0][i] == oldPopulation[1][i]);
        }
    }

    @Test
    public static void main(String Args[]) throws IOException {
        GeneticAlgorithmTest gt = new GeneticAlgorithmTest();
        gt.evaluateTest();
        gt.countRateTest();
        gt.selectBestGhTest();
        gt.copyGhTest();
    }
}