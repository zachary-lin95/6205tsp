

import javax.swing.*;

import java.awt.*;
import java.io.*;

@SuppressWarnings("serial")
public class MyPanel extends JPanel {
	int cityNum = 20;

	public MyPanel() {
	}

	public void paintComponent(Graphics g ) {
		super.paintComponent(g);

		try {
			GeneticAlgorithm ga = new GeneticAlgorithm(50, 20, 1000, 0.8F, 0.9F);
			GeneticAlgorithm expectedga = new GeneticAlgorithm(30,20,10000,0.8f,0.05f);
			expectedga.init("citys data.txt");
			ga.init("citys data.txt");
			ga.solve();
			expectedga.getexpected();
			int[] bestTour = ga.getBestTour();
			int bestLength = ga.getBestLength();
			int[] expectedbestTour = expectedga.getBestTour();
			int expectedbestLength = expectedga.getBestLength();
			g.drawString("Best Length:" + bestLength, 30, 340);
			StringBuilder sb = new StringBuilder();


			for(int i = 0; i < this.cityNum; ++i) {
				sb.append(bestTour[i] + "-");

			}

			sb.delete(sb.length() - 1, sb.length());
			g.drawString("Best Path:" + sb.toString(), 30, 370);
			g.setColor(Color.black);
			BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream("citys data.txt")));
			int[] x = new int[this.cityNum];
			int[] y = new int[this.cityNum];

			int j;
			for(j = 0; j < this.cityNum; ++j) {
				String strbuff = data.readLine();
				String[] strcol = strbuff.split(" ");
				x[j] = Integer.valueOf(strcol[1]);
				y[j] = Integer.valueOf(strcol[2]);
				g.fillOval(x[j]/3  , y[j]/3 , 5, 5);
				g.drawString(String.valueOf(j), x[j]/3 , y[j]/3 );
				//eg.fillOval(x[j]/3 +500  , y[j]/3+500 , 5, 5);
				//eg.drawString(String.valueOf(j), x[j]/3+500, y[j]/3+500);
			}

			data.close();
			g.setColor(Color.BLACK);
			//eg.setColor(Color.BLACK);
			for(j = 0; j < this.cityNum - 1; ++j) {
				g.drawLine(x[bestTour[j]]/3, y[bestTour[j]]/3 , x[bestTour[j + 1]]/3, y[bestTour[j + 1]]/3 );
				//eg.drawLine(x[bestTour[j]]/3+500, y[bestTour[j]]/3+500 , x[bestTour[j + 1]]/3+500, y[bestTour[j + 1]]/3+500 );
			}

			g.setColor(Color.yellow);
			g.fillOval(x[bestTour[0]] , y[bestTour[0]], 6, 6);
			g.fillOval(x[bestTour[this.cityNum - 1]]/3 , y[bestTour[this.cityNum - 1]]/3, 6, 6);
		} catch (Exception var12) {
			var12.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {
		JFrame f = new JFrame();
		f.setTitle("GA-tsp");
		f.getContentPane().add(new MyPanel());
		f.setSize(500, 500);
		f.setDefaultCloseOperation(3);
		f.setVisible(true);
	}
}
