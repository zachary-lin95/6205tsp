
import java.util.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class GeneticAlgorithm {
    private int size;//the size of population
    private int cityNum; // the length of chromosome
    private int MAX_GEN; // the maximum number of reproduction
    private int[][] distance; // the distance matrix
    private int bestT;// generation number which contains the first best length
    private int bestLength; // best length
    private int[] bestTour; // best path


    private int[][] oldPopulation;//parent generation
    //the number of rows represents the size of the population, one row represents an individual,ie, the chromosome
    //the column represents the chromosomal gene fragment
    private int[][] newPopulation;//offspring generation
    private int[] fitness;// population fitness for every individual

    private float[] Pi;// individual's selected probability
    private float Pc;// crossover probability
    private float Pm;// mutated probability
    private int t;// current generation number

    private Random random;



    public GeneticAlgorithm(int s, int n, int g, float c, float m) {
        size = s;
        cityNum = n;
        MAX_GEN = g;
        Pc = c;
        Pm = m;
    }

    public void init(String filename) throws IOException {
        // read data
        int[] x;
        int[] y;
        String strbuff;
        BufferedReader data = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename)));
        distance = new int[cityNum][cityNum];
        x = new int[cityNum];
        y = new int[cityNum];
        for (int i = 0; i < cityNum; i++) {
            // read data
            strbuff = data.readLine();
            String[] strcol = strbuff.split(" ");
            x[i] = Integer.valueOf(strcol[1]);// x coordinate
            y[i] = Integer.valueOf(strcol[2]);// y coordinate
        }
        // calculate the distance matrix
        for (int i = 0; i < cityNum - 1; i++) {
            distance[i][i] = 0;
            for (int j = i + 1; j < cityNum; j++) {
                double rij = Math
                        .sqrt(((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j])
                                * (y[i] - y[j])) / 10.0);
                int tij = (int) Math.round(rij);
                if (tij < rij) {
                    distance[i][j] = tij + 1;
                    distance[j][i] = distance[i][j];
                } else {
                    distance[i][j] = tij;
                    distance[j][i] = distance[i][j];
                }
            }
        }
        distance[cityNum - 1][cityNum - 1] = 0;

        bestLength = Integer.MAX_VALUE;
        bestTour = new int[cityNum + 1];
        bestT = 0;
        t = 0;

        newPopulation = new int[size][cityNum];
        oldPopulation = new int[size][cityNum];
        fitness = new int[size];
        Pi = new float[size];

        random = new Random(System.currentTimeMillis());
    }

    // Initialize population
    void initGroup() {
        int i, j, k;
        // Random random = new Random(System.currentTimeMillis());
        for (k = 0; k < size; k++)// population size
        {
            oldPopulation[k][0] = random.nextInt(65535) % cityNum;
            for (i = 1; i < cityNum;)// chromosome length
            {
                oldPopulation[k][i] = random.nextInt(65535) % cityNum;
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

    }

    // length for every individual chromosome which used to calculate the fitness
    public int evaluate(int[] chromosome) {
        // 0123
        int len = 0;
        for (int i = 1; i < cityNum; i++) {
            len += distance[chromosome[i - 1]][chromosome[i]];
        }
        // the distance from the final city to the first city
        len += distance[chromosome[cityNum - 1]][chromosome[0]];
        return len;
    }

    // calculate the individual's selected possibility 计算种群中各个个体的累积概率，前提是已经计算出各个个体的适应度fitness[max]，作为赌轮选择策略一部分，Pi[max]
    void countRate() {
        int k;
        double sumFitness = 0;// sumfitness

        double[] tempf = new double[size];

        for (k = 0; k < size; k++) {
            tempf[k] = 10.0 / fitness[k];
            sumFitness += tempf[k];
        }

        Pi[0] = (float) (tempf[0] / sumFitness);
        for (k = 1; k < size; k++) {
            Pi[k] = (float) (tempf[k] / sumFitness + Pi[k - 1]);
        }

    }

    // elitist selection
    public void selectBestGh() {
        int k, i, maxid;
        int maxevaluation;

        maxid = 0;
        maxevaluation = fitness[0];
        for (k = 1; k < size; k++) {
            if (maxevaluation > fitness[k]) {
                maxevaluation = fitness[k];
                maxid = k;
            }
        }

        if (bestLength > maxevaluation) {
            bestLength = maxevaluation;
            bestT = t;// 最好的染色体出现的代数;
            for (i = 0; i < cityNum; i++) {
                bestTour[i] = oldPopulation[maxid][i];
            }
        }

        copyGh(0, maxid);// copy the best organism from the current generation to the next
    }

    // Copy the chromosome, k represents the position of the new chromosome in the population
    // kk represents the position of the old chromosome in the population
    public void copyGh(int k, int kk) {
        int i;
        for (i = 0; i < cityNum; i++) {
            newPopulation[k][i] = oldPopulation[kk][i];
        }
    }

    // roulette wheel selection
    public void select() {
        int k, i, selectId;
        float ran1;
        for (k = 1; k < size; k++) {
            ran1 = (float) (random.nextInt(65535) % 1000 / 1000.0);
            // System.out.println("possibility"+ran1);
            for (i = 0; i < size; i++) {
                if (ran1 <= Pi[i]) {
                    break;
                }
            }
            selectId = i;
            // System.out.println("selected" + selectId);
            copyGh(k, selectId);
        }
    }

    //evolution function
    public void evolution() {
        int k;
        // elitist selection
        selectBestGh();

        // use roulette wheel selection selects the rest
        select();

        float r;

        // 交叉方法
        for (k = 0; k < size; k = k + 2) {
            r = random.nextFloat();// /产生概率
            // System.out.println("交叉率..." + r);
            if (r < Pc) {
                // System.out.println(k + "与" + k + 1 + "进行交叉...");
                //OXCross(k, k + 1);// 进行交叉
                OXCross1(k, k + 1);
            } else {
                r = random.nextFloat();// /产生概率
                // System.out.println("变异率1..." + r);
                // 变异
                if (r < Pm) {
                    // System.out.println(k + "变异...");
                    OnCVariation(k);
                }
                r = random.nextFloat();// /产生概率
                // System.out.println("变异率2..." + r);
                // 变异
                if (r < Pm) {
                    // System.out.println(k + 1 + "变异...");
                    OnCVariation(k + 1);
                }
            }

        }
    }

    //进化函数，保留最好染色体不进行交叉变异
    public void evolution1() {
        int k;
        // 挑选某代种群中适应度最高的个体
        selectBestGh();

        // 赌轮选择策略挑选scale-1个下一代个体
        select();

        // Random random = new Random(System.currentTimeMillis());
        float r;

        for (k = 1; k + 1 < size / 2; k = k + 2) {
            r = random.nextFloat();// /产生概率
            if (r < Pc) {
               // OXCross1(k, k + 1);// 进行交叉
                OXCross(k,k+1);//进行交叉
            } else {
                r = random.nextFloat();// /产生概率
                // 变异
                if (r < Pm) {
                    OnCVariation(k);
                }
                r = random.nextFloat();// /产生概率
                // 变异
                if (r < Pm) {
                    OnCVariation(k + 1);
                }
            }
        }
        if (k == size / 2 - 1)// 剩最后一个染色体没有交叉L-1
        {
            r = random.nextFloat();// /产生概率
            if (r < Pm) {
                OnCVariation(k);
            }
        }

    }

    // 类OX交叉算子
    void OXCross(int k1, int k2) {
        int i, j, k, flag;
        int ran1, ran2, temp;
        int[] Gh1 = new int[cityNum];
        int[] Gh2 = new int[cityNum];
        // Random random = new Random(System.currentTimeMillis());

        ran1 = random.nextInt(65535) % cityNum;
        ran2 = random.nextInt(65535) % cityNum;
        // System.out.println();
        // System.out.println("-----------------------");
        // System.out.println("----"+ran1+"----"+ran2);

        while (ran1 == ran2) {
            ran2 = random.nextInt(65535) % cityNum;
        }

        if (ran1 > ran2)// 确保ran1<ran2
        {
            temp = ran1;
            ran1 = ran2;
            ran2 = temp;
        }
        // System.out.println();
        // System.out.println("-----------------------");
        // System.out.println("----"+ran1+"----"+ran2);
        // System.out.println("-----------------------");
        // System.out.println();
        flag = ran2 - ran1 + 1;
        for (i = 0, j = ran1; i < flag; i++, j++) {
            Gh1[i] = newPopulation[k2][j];
            Gh2[i] = newPopulation[k1][j];
        }

        for (k = 0, j = flag; j < cityNum;)// 染色体长度
        {
            Gh1[j] = newPopulation[k1][k++];
            for (i = 0; i < flag; i++) {
                if (Gh1[i] == Gh1[j]) {
                    break;
                }
            }
            if (i == flag) {
                j++;
            }
        }

        for (k = 0, j = flag; j < cityNum;)// 染色体长度
        {
            Gh2[j] = newPopulation[k2][k++];
            for (i = 0; i < flag; i++) {
                if (Gh2[i] == Gh2[j]) {
                    break;
                }
            }
            if (i == flag) {
                j++;
            }
        }

        for (i = 0; i < cityNum; i++) {
            newPopulation[k1][i] = Gh1[i];// crossover finished
            newPopulation[k2][i] = Gh2[i];
        }

        // System.out.println("crossover--------------------------");
        // System.out.println(k1+"after crossover...");
        // for (i = 0; i < cityNum; i++) {
        // System.out.print(newPopulation[k1][i] + "-");
        // }
        // System.out.println();
        // System.out.println(k2+"after crossover...");
        // for (i = 0; i < cityNum; i++) {
        // System.out.print(newPopulation[k2][i] + "-");
        // }
        // System.out.println();
        // System.out.println("crossover finished--------------------------");
    }

    // crossover operators,same chromosomes crossover and create different chromosomes
    public void OXCross1(int k1, int k2) {
        int i, j, k, flag;
        int ran1, ran2, temp;
        int[] Gh1 = new int[cityNum];
        int[] Gh2 = new int[cityNum];
        // Random random = new Random(System.currentTimeMillis());

        ran1 = random.nextInt(65535) % cityNum;
        ran2 = random.nextInt(65535) % cityNum;
        while (ran1 == ran2) {
            ran2 = random.nextInt(65535) % cityNum;
        }

        if (ran1 > ran2)// to make sure ran1 <ran2
        {
            temp = ran1;
            ran1 = ran2;
            ran2 = temp;
        }

        // move the third part of chromosome 1 to the head of chromosome 2
        for (i = 0, j = ran2; j < cityNum; i++, j++) {
            Gh2[i] = newPopulation[k1][j];
        }

        flag = i;// chromosome 2 original gene's starting position

        for (k = 0, j = flag; j < cityNum;)// the length of chromosome
        {
            Gh2[j] = newPopulation[k2][k++];
            for (i = 0; i < flag; i++) {
                if (Gh2[i] == Gh2[j]) {
                    break;
                }
            }
            if (i == flag) {
                j++;
            }
        }

        flag = ran1;
        for (k = 0, j = 0; k < cityNum;)// the length of chromosome
        {
            Gh1[j] = newPopulation[k1][k++];
            for (i = 0; i < flag; i++) {
                if (newPopulation[k2][i] == Gh1[j]) {
                    break;
                }
            }
            if (i == flag) {
                j++;
            }
        }

        flag = cityNum - ran1;

        for (i = 0, j = flag; j < cityNum; j++, i++) {
            Gh1[j] = newPopulation[k2][i];
        }

        for (i = 0; i < cityNum; i++) {
            newPopulation[k1][i] = Gh1[i];// Put the new one back to the population
            newPopulation[k2][i] = Gh2[i];// Put the new one back to the population
        }
    }

    // mutation operator
    public void OnCVariation(int k) {
        int ran1, ran2, temp;
        int count;// exchange times

        // Random random = new Random(System.currentTimeMillis());
        count = random.nextInt(65535) % cityNum;

        for (int i = 0; i < count; i++) {

            ran1 = random.nextInt(65535) % cityNum;
            ran2 = random.nextInt(65535) % cityNum;
            while (ran1 == ran2) {
                ran2 = random.nextInt(65535) % cityNum;
            }
            temp = newPopulation[k][ran1];
            newPopulation[k][ran1] = newPopulation[k][ran2];
            newPopulation[k][ran2] = temp;
        }

        /*
         * for(i=0;i<L;i++) { printf("%d ",newGroup[k][i]); } printf("\n");
         */
    }

    public void solve() {
        int i;
        int k;

        // initial the population
        initGroup();
        // calculate initial population's fitness
        for (k = 0; k < size; k++) {
            fitness[k] = evaluate(oldPopulation[k]);
            // System.out.println(fitness[k]);
        }

        countRate();
//        System.out.println("initial population...");
//        for (k = 0; k < size; k++) {
//            for (i = 0; i < cityNum; i++) {
//                System.out.print(oldPopulation[k][i] + ",");
//            }
//            System.out.println();
//            System.out.println("----" + fitness[k] + " " + Pi[k]);
//        }

        for (t = 0; t < MAX_GEN; t++) {
            evolution1();
            //evolution();
            // copy the new population to the old population
            for (k = 0; k < size; k++) {
                for (i = 0; i < cityNum; i++) {
                    oldPopulation[k][i] = newPopulation[k][i];
                }
            }
            // calculate the old population's fitness
            for (k = 0; k < size; k++) {
                fitness[k] = evaluate(oldPopulation[k]);
            }
            // calculate the possibility
            countRate();
        }

        System.out.println("final population...");
        for (k = 0; k < size; k++) {
            for (i = 0; i < cityNum; i++) {
                System.out.print(oldPopulation[k][i] + ",");
            }
            System.out.println();
            System.out.println("---" + fitness[k] + " " + Pi[k]);
        }

        System.out.println("generation number which contains the first best length：");
        System.out.println(bestT);
        System.out.println("the shortest length");
        System.out.println(bestLength);

        System.out.println("the best path：");

        for (i = 0; i < cityNum; i++) {
            System.out.print(bestTour[i] + ",");
        }
        System.out.println("");



    }

    public  void getexpected(){
        int i;
        int k;

        // initial the population
        initGroup();
        // calculate initial population's fitness
        for (k = 0; k < size; k++) {
            fitness[k] = evaluate(oldPopulation[k]);
            // System.out.println(fitness[k]);
        }

        countRate();
        for (t = 0; t < MAX_GEN; t++) {
            evolution1();
            //evolution();
            // copy the new population to the old population
            for (k = 0; k < size; k++) {
                for (i = 0; i < cityNum; i++) {
                    oldPopulation[k][i] = newPopulation[k][i];
                }
            }
            // calculate the old population's fitness
            for (k = 0; k < size; k++) {
                fitness[k] = evaluate(oldPopulation[k]);
            }
            // calculate the possibility
            countRate();
        }

        System.out.println("the expected shortest length:");
        System.out.println(bestLength);

    }

    public int getBestLength() {
        return this.bestLength;
    }

    public int[] getBestTour() {
        return this.bestTour;
    }

    public static void main(String[] args) throws IOException {
        GeneticAlgorithm ga = new GeneticAlgorithm(30, 20, 100, 0.8f, 0.001f);
        GeneticAlgorithm expectedga = new GeneticAlgorithm(30,20,10000,0.8f,0.05f);
        expectedga.init("citys data.txt");
        ga.init("citys data.txt");
        ga.solve();
        expectedga.getexpected();
    }

}
