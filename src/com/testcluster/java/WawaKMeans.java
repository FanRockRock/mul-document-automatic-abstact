package com.testcluster.java;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.vector.java.TermVector;

public class WawaKMeans {
	// / <summary>
	// / 数据的数量
	// / </summary>
	private int _coordCount;
	// / <summary>
	// / 原始数据
	// / </summary>
	private double[][] _coordinates;
	// / <summary>
	// / 聚类的数量
	// / </summary>
	private  int _k;
	// / <summary>
	// / 聚类
	// / </summary>
	private WawaCluster[] _clusters;
	
	WawaCluster[] getClusters() {
		return _clusters;
	}

	// / <summary>
	// / 定义一个变量用于记录和跟踪每个资料点属于哪个群聚类
	// / _clusterAssignments[j]=i;// 表示第 j 个资料点对象属于第 i 个群聚类
	// / </summary>
	final int[] _clusterAssignments;
	// / <summary>
	// / 定义一个变量用于记录和跟踪每个资料点离聚类最近
	// / </summary>
	private final int[] _nearestCluster;
	// / <summary>
	// / 定义一个变量，来表示资料点到中心点的距离,
	// / 其中—_distanceCache[i][j]表示第i个资料点到第j个群聚对象中心点的距离；
	// / </summary>
	private final double[][] _distanceCache;
	
	//private static final Random _rnd = new Random(0);
	
	
	public WawaKMeans(double[][] data, int K) {
		_coordinates = data;
		_coordCount = data.length;

		_k = K;

		_clusterAssignments = new int[_coordCount];
		_nearestCluster = new int[_coordCount];
		_distanceCache = new double[_coordCount][data.length];
		InitRandom1();
	}

	public WawaKMeans(double[][] data,Canopy canopy) {
		_coordinates = data;
		_coordCount = data.length;

		_clusterAssignments = new int[_coordCount];
		_nearestCluster = new int[_coordCount];
		_distanceCache = new double[_coordCount][data.length];
		InitRandom2(canopy);
	}

	// / <summary>
	// / 随机初始化k个聚类，知道k的情况下
	// / </summary>
	private void InitRandom1() {
		int shuzu[] = new int[_k];	
		int chazhi=_coordCount/_k;
		int data=0;
		for(int i=0;i<_k;i++){
			shuzu[i]=data;
			data+=chazhi;
		}
		System.out.println("生成的随机数如下:");
		for(int i=0;i<_k;i++)
			System.out.print(shuzu[i]+" ");
		_clusters = new WawaCluster[_k];
		for (int i = 0; i < _k; i++) {
			int temp =shuzu[i];
			_clusterAssignments[temp] = i; // 记录第temp个资料属于第i个聚类
			_clusters[i] = new WawaCluster(temp, _coordinates[temp]);
		}
	}

	// / <summary>
	// / 不知道k的情况下，canopy算法得到k
	// / </summary>
	private void InitRandom2(Canopy canopy) {
		_k=canopy.getClusterNumber();
		_clusters = new WawaCluster[_k];

		List<Point> centerPoints = canopy.getClusterCenterPoints();
        int shuzu[]=canopy.getClustersRepresent();
//        for(int i=0;i<shuzu.length;i++)
//        	System.out.println(shuzu[i]);
		for (int i = 0; i < _k; i++) {
			int temp =shuzu[i];
			_clusterAssignments[temp] = i; // 记录第temp个资料属于第i个聚类
			_clusters[i] = new WawaCluster(temp, _coordinates[temp]);
			//_clusters[i] = new WawaCluster(0, centerPoints.get(i).data);
		}
	}

	public void Start() {
		int iter = 0;
		while (true) {
			System.out.println("迭代 " + (iter++) + "...");
			// 1、重新计算每个聚类的均值
			for (int i = 0; i < _k; i++) {
				_clusters[i].UpdateMean(_coordinates);
			}
			// 2、计算每个数据和每个聚类中心的距离
			for (int i = 0; i < _coordCount; i++) {
				for (int j = 0; j < _k; j++) {
					double dist = getDistance(_coordinates[i],
							_clusters[j].Mean);
					_distanceCache[i][j] = dist;
				}
			}

			// 3、计算每个数据离哪个聚类最近
			for (int i = 0; i < _coordCount; i++) {
				_nearestCluster[i] = nearestCluster(i);
			}

			// 4、比较每个数据最近的聚类是否就是它所属的聚类
			// 如果全相等表示所有的点已经是最佳距离了，直接返回；
			int k = 0;
			for (int i = 0; i < _coordCount; i++) {
				if (_nearestCluster[i] == _clusterAssignments[i])
					k++;

			}
			if (k == _coordCount)
				break;

			// 5、否则需要重新调整资料点和群聚类的关系，调整完毕后再重新开始循环；
			// 需要修改每个聚类的成员和表示某个数据属于哪个聚类的变量
			for (int j = 0; j < _k; j++) {
				_clusters[j].CurrentMembership.clear();
			}
			for (int i = 0; i < _coordCount; i++) {
				_clusters[_nearestCluster[i]].CurrentMembership.add(i);
				_clusterAssignments[i] = _nearestCluster[i];
			}
		}
	}
	// / <summary>
	// / 计算某个数据离哪个聚类最近
	// / </summary>
	// / <param name="ndx"></param>
	// / <returns></returns>
	int nearestCluster(int ndx) {
		int nearest = -1;
		double min = Double.MAX_VALUE;
		for (int c = 0; c < _k; c++) {
			double d = _distanceCache[ndx][c];
			if (d < min) {
				min = d;
				nearest = c;
			}
		}
		if (nearest == -1) {
			return 0;
		}
		return nearest;
	}
	// / <summary>
	// / 计算某数据离某聚类中心的距离
	// / </summary>
	// / <param name="coord"></param>
	// / <param name="center"></param>
	// / <returns></returns>
	static double getDistance(double[] coord, double[] center) {
		//也可以用余弦夹角来计算某数据离某聚类中心的距离
		return 1 - TermVector.ComputeCosineSimilarity(coord, center);
	}
}
