package agentbased;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Agentbased_lib {
	private double mu[][], popularity[]; 
	private int time;
	Random rnd = new Random();
	private int K; //ノード数
	private int N; //系内の客数
	private int C; //クラス数
	ArrayList<Integer> customer[];//ノードで並んでいる客のクラス(並んでいる順番に)
	ArrayList<Integer> timequeue[]; //時系列の各ノードでの客数
	
	public Agentbased_lib(double[][] mu, double[] popularity, int time, int n, int k, int c) {
		this.mu = mu;
		this.popularity = popularity;
		this.time = time;
		K = k;
		N = n;
		C = c;
		this.customer = new ArrayList[K];
		this.timequeue = new ArrayList[K];
		for(int i = 0; i < customer.length; i++) customer[i] = new ArrayList<Integer>();
		for(int i = 0; i < timequeue.length; i++) timequeue[i] = new ArrayList<Integer>();
	}

	public double[] getSimulation() {
		double service[] = new double[K];
		double result[] = new double[K];
		int queue[] = new int[K]; //各ノードのサービス中を含むキューの長さ
		double elapse = 0;
		
		//スタート時は全てノード0、クラスは均等に割り当てる
		for(int i = 0; i < this.N; i++) {
			customer[0].add(i%C);
			queue[0]++;
		}
		
		service[0] = this.getExponential(mu[0][0]); //先頭客のサービス時間設定(最初はクラス0の客)
		double total_queue[] = new double[K]; //各ノードの延べ系内人数
		double total_queuelength[] = new double[K]; //待ち人数
		
		while(elapse < time) {
			double mini_service = 100000; //最小のサービス時間
			int mini_index = -1; //最小のサービス時間をもつノード
			int mini_class = -1; //サービス対象となる客のクラス
			for(int i = 0; i < K; i++) { //待ち人数がいる中で最小のサービス時間を持つノードを算出
				if( queue[i] > 0) {
					if( mini_service > service[i]) {
						mini_service = service[i];
						mini_index = i;
					}
				}
			}
			//mini_indexの時のクラス
			mini_class = customer[mini_index].get(0);
			customer[mini_index].remove(0);//先頭を削除
			
			for(int i = 0; i < K; i++) { //ノードiから退去
				total_queue[i] += queue[i] * mini_service;
				if( queue[i] > 0) service[i] -= mini_service;
				if( queue[i] > 0 ) total_queuelength[i] += ( queue[i] - 1 ) * mini_service;
				else if ( queue[i] == 0 ) total_queuelength[i] += queue[i] * mini_service;
			}
			
			queue[mini_index] --;
			elapse += mini_service;
			if( queue[mini_index] > 0) //退去後まだ待ち人数がある場合、サービス時間設定
				service[mini_index] = this.getExponential(mu[mini_class][mini_index]);
			
			//退去客の行き先決定
			//ここから実装する
			//(1)人気のある拠点にいきたい
			//(2)待ち人数が多いところには行きたくない
			/*
			 人気度(割合) + 現在の空き人数(Nで割る)をウエイトとする
			 空き人数はN/Kをキャパとする
			 */
			int sum_popularity = 0;
			for(int i = 0; i < popularity.length;i++) {
				sum_popularity += popularity[i];
			}
			double[] weight = new double[K];
			for(int i = 0; i < weight.length; i++) {
				weight[i] = popularity[i]/sum_popularity + (N/K - queue[i])/N;
			}
			//System.out.println("Weight = "+Arrays.toString(weight));
			double sum_weight = 0;
			for(int i = 0; i < weight.length; i++) {
				sum_weight += weight[i];
			}
			for(int i = 0; i < weight.length; i++) {
				weight[i] = weight[i] / sum_weight;
			}
			//行き先決定
			//クラスは確率p_classで変化(p_class = 0.9程度)
			double rand = rnd.nextDouble();
			double sum_rand = 0;
			int destination_index = -1;
			//行き先
			for(int i = 0; i < weight.length; i++) {
				sum_rand += weight[i];
				if( rand < sum_rand) {
					destination_index = i; //Kのどこか
					break;
				}
			}
			//クラス
			int destination_class = -1;
			double p_class = 0.9; //同じクラスへの滞在率
			double rand_class = rnd.nextDouble();
			if(rand_class <= p_class) {
				destination_class = mini_class;
			}else { //ひとまずクラスが2の場合
				destination_class = (mini_class +1) % 2;
			}
			
			//行き先が決まらない場合
			if( destination_index == -1) {
				destination_index = K-1;//クラスの最後のノードにしておく
			}
			//推移先で待っている客がいなければサービス時間設定(即時サービス)
			if(queue[destination_index] == 0) {
				service[destination_index] = this.getExponential(mu[destination_class][destination_index]);
			}
			queue[destination_index]++;
			customer[destination_index].add(destination_class);
			
			//時系列での各ノードにおける客数(クラス別ではない)
			for(int i = 0; i < timequeue.length; i++)
				timequeue[i].add(customer[i].size());
		}
		for(int i = 0; i < K; i++) {
			result[i] = total_queue[i] / time; //平均系内人数
		}
		return result;
	}
	
	//指数乱数発生
	public double getExponential(double param) {
		return - Math.log(1 - rnd.nextDouble()) / param;
	}

	public ArrayList<Integer>[] getTimequeue() {
		return timequeue;
	}
	
}
