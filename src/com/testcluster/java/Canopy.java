package com.testcluster.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Canopy算法
 * 
 */
public class Canopy {
	private List<Point> points = new ArrayList<Point>();
	private List<Point> oldPoints = new ArrayList<Point>();
	private List<List<Point>> clusters = new ArrayList<List<Point>>();
	private double T2 = -1;

	public Canopy(List<Point> points) {
		for (Point point : points)
		{
			this.points.add(point);
			this.oldPoints.add(point);
		}
	}

	public void cluster() {
		T2 = getAverageDistance(points);
		while (points.size() != 0) {
			List<Point> cluster = new ArrayList<Point>();
			Point basePoint = points.get(0);
			cluster.add(basePoint);
			points.remove(0);
			int index = 0;
			while (index < points.size()) {
				Point anotherPoint = points.get(index);
				double distance = getDistance(basePoint, anotherPoint);
				if (distance <= T2) {
					cluster.add(anotherPoint);
					points.remove(index);
				} else {
					index++;
				}
			}
			clusters.add(cluster);
		}
	}

	public int getClusterNumber() {
		return clusters.size();
	}

	public double getDistance(Point A, Point B) {
		return (WawaKMeans.getDistance(A.data, B.data));
	}

	public double getAverageDistance(List<Point> points) {
		double sum = 0;
		int pointSize = points.size();
		for (int i = 0; i < pointSize; i++) {
			for (int j = i + 1; j < pointSize; j++) {
				Point pointA = points.get(i);
				Point pointB = points.get(j);
				sum += getDistance(pointA, pointB);
			}
		}
		int distanceNumber = pointSize * (pointSize - 1) / 2;
		double T2 = sum / distanceNumber;
		return T2;
	}

	private double[] getCenterPoint(List<Point> points) {
		double[] data = new double[points.get(0).data.length];
		for (Point point : points) {
			for (int i = 0; i < point.data.length; i++) {
				data[i] += point.data[i];
			}
		}
		for (int i = 0; i < points.get(0).data.length; i++) {
			data[i] = data[i] / points.size();
		}
		return data;
	}

	public List<Point> getClusterCenterPoints() {
		List<Point> centerPoints = new ArrayList<Point>();
		for (List<Point> cluster : clusters) {
			centerPoints.add(new Point(getCenterPoint(cluster)));
		}
		return centerPoints;
	}

	public double getThreshold() {
		return T2;
	}

	/**
	 * 拿到每个聚类点所代表的下标
	 */
	public int[] getClustersRepresent() {
		int result[] = new int[clusters.size()];
		int k = 0;
		for (List<Point> list : clusters) {
			Point point = list.get(0);
			int i =oldPoints.indexOf(point);
			result[k++] = i;
		}
		return result;
	}
}
