package agentbased;

import java.util.ArrayList;
import java.util.Arrays;

public class Agentbased_main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int time = 300000;
		int N = 100, K = 12, C = 2;
		double mu[][] = {{5,5,10,5,5,5,7,5,5,10,5,10}, {5,5,10,5,5,5,7,5,5,10,5,10}};//サービス率
		double popularity[] = {5,5,10,5,5,5,5,5,5,10,5,10}; //人気度
		
		Agentbased_lib alib = new Agentbased_lib(mu, popularity, time, N, K, C);
		double[] result = alib.getSimulation();
		System.out.println("L = "+Arrays.toString(result));
		//ArrayList<Integer> timequeue[] = alib.getTimequeue();
		Graph graph = new Graph(alib);
		graph.setBounds(5,5,755,455);
		graph.setVisible(true);
	}

}
