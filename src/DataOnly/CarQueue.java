package DataOnly;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import DataObjects.DataCar;

public class CarQueue implements Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Overriding clone() method of Object class
	public CarQueue clone() throws CloneNotSupportedException {
		return (CarQueue) super.clone();
	}

	public Queue<DataCar> Cars = new ArrayDeque<>();

	public Integer Size = 5;

	public CarQueue() {}

	public boolean AddCar(DataCar car) {
		if (Cars.size() < Size) {
			Cars.add(car);
			return true;
		}
		return false;
	}

	public boolean CanAddCar() {
		return Cars.size() < Size;
	}

	public DataCar PopCar() {
		System.out.println("Popping car: " + Cars.peek());
		return Cars.poll();
	}

	public String toString() {
		ArrayList<String> temp1 = new ArrayList<String>();
		for (DataCar car : Cars) {
			if (car == null)
				temp1.add("NULL");
			else
				temp1.add(car.toString());
		}

		return "(" + String.join(",", temp1) + ")";
	}

	public boolean FirstCarGoesTo(String placeName) {
		try {
			return Cars.peek().Value.Target.equals(placeName);
		} catch(Exception e) {
			return false;
		}
	}
};
