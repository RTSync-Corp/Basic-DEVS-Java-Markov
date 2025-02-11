package com.ms4systems.devs.analytics;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Random;

// import org.eclipse.swt.SWT;
// import org.eclipse.swt.layout.FillLayout;
// import org.eclipse.swt.widgets.Display;
// import org.eclipse.swt.widgets.Shell;
// import org.eclipse.ui.PlatformUI;
// import org.jfree.experimental.chart.swt.ChartComposite;
// import org.jfree.ui.RefineryUtilities;

// import com.ms4systems.devs.chart.LineChartGenerator;
// import com.ms4systems.devs.chart.XYPair;

public class PMF {// probability mass function: elements are real values
	protected Hashtable<Double, Double> h;
	// elements = h.keySet. freqs = h.values()
	protected String name;

	public PMF() {
		this(true);
	}

	public PMF(boolean b) {
		this("PMF", b);
	}

	public PMF(String nameOfPDF, boolean b) {
		this.name = nameOfPDF;
		h = new Hashtable<Double, Double>();
	}

	public PMF(Hashtable<Double, Double> hs) {
		h = hs;
		// size = hs.size();
	}

	public Hashtable<Double, Double> getHashtable() {
		return h;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEmpty() {
		return h.isEmpty();
	}

	public int size() {
		return h.size();
	}

	public double get(Double element) {
		if (h.get(element) == null) {
			return Double.NaN;
		} else {
			return h.get(element);
		}
	}

	public synchronized void put(Double element, double p) {
		if (p > 0) {
			h.put(element, p);
		} else {
			h.put(element, 0.);
		}
	}

	public synchronized void putSample(double element) {
		int size = h.size();
		h.put(element, 1.);
		if (h.size() > size)
			return;
		else {
			Double newVal = element;
			while (h.size() == size) {
				newVal += .000000001;

				h.put(newVal, 1.);
			}
		}
	}

	public HashSet<Double> keySet() {
		return new HashSet<Double>(h.keySet());
	}

	public HashSet<Double> valueSet() {
		return new HashSet<Double>(h.values());
	}

	public String toString() {
		TreeSet<Double> ts = new TreeSet<Double>();
		Set<Double> keys = h.keySet();
		for (Double k : keys) {
			ts.add(k);
		}
		String s = "" + getTotalOfFreqs() + " " + this.getMean();
		// for (Double d:ts){
		// s += " "+d+" "+h.get(d)+",";
		// }
		return s;
	}

	public void print() {
		TreeSet<Double> ts = new TreeSet<Double>();
		Set<Double> keys = h.keySet();
		for (Double k : keys) {
			ts.add(k);
		}
		for (Double d : ts) {
			System.out.println(d + " " + h.get(d));
		}
	}

	public void printKeys() {
		TreeSet<Double> ts = new TreeSet<Double>();
		Set<Double> keys = h.keySet();
		for (Double k : keys) {
			ts.add(k);
		}
		for (Double d : ts) {
			System.out.println(d);
		}
	}

	public static PMF makePMF(double[] elements,
			double[] probabilities) {
		PMF pmf = new PMF(true);
		for (int i = 0; i < elements.length; i++) {
			pmf.put(elements[i], probabilities[i]);
		}
		return pmf;
	}

	public static PMF makePMF(String nameOfPDF,
			double[] elements, double[] probabilities) {
		PMF pmf = new PMF(
				nameOfPDF, true);
		for (int i = 0; i < elements.length; i++) {
			pmf.put(elements[i], probabilities[i]);
		}
		return pmf;
	}

	public double vectorProduct(PMF other) {
		double total = 0;
		HashSet elements = new HashSet(this.keySet());
		HashSet others = other.keySet();
		elements.retainAll(others);
		for (Object element : elements) {
			double p = (double) this.get((Double) element)
					* (double) other.get((Double) element);
			total += p;
		}
		return total;
	}

	public double bernouliCombination() {
		double prod = 1;
		for (Object key : keySet()) {
			Double p = (Double) h.get(key);
			prod *= (1 - p);
		}
		return 1 - prod;
	}

	public double getTotalOfFreqs() {
		double total = 0;
		for (Double k : keySet()) {
			total += h.get(k);
		}
		return total;
	}

	public double getFreqWeightedSum() {
		double sum = 0;
		for (Double k : keySet()) {
			sum += k * h.get(k);
		}
		return sum;
	}

	public void normalize() {
		double total = getTotalOfFreqs();
		if (total <= 0)
			return;
		for (Double k : keySet()) {
			h.put(k, h.get(k) / total);
		}
	}

	public double getMean() {
		double total = getTotalOfFreqs();
		double sum = getFreqWeightedSum();
		return sum / total;
	}

	public double getStd() {
		double mean = getMean();
		double sum = 0;
		for (Double k : keySet()) {
			double delta = k - mean;
			sum += h.get(k) * delta * delta;
		}
		return Math.sqrt(sum / getTotalOfFreqs());
	}

	public double getHighestNonZero() {
		TreeSet ts = new TreeSet(keySet());
		Iterator it = ts.descendingIterator();
		while (it.hasNext()) {
			double d = ((Double) it.next()).doubleValue();
			double dd = get(d);
			if (dd == 0)
				continue;
			return d;
		}
		return 0;
	}

	public double getLowestNonZero() {
		TreeSet ts = new TreeSet(keySet());
		for (Object o : ts) {
			double d = get((Double) o);
			if (d == 0)
				continue;
			return (Double) o;
		}
		return 0;
	}

	public static PMF computeNormal(double mean,
			double sigma, double deltx) {
		PMF pmf = new PMF();
		double sump = 0;
		double sumplnp = 0;
		for (double x = -3 * sigma; x <= 3 * sigma; x = x + deltx) {
			double sigrtpi = sigma * Math.sqrt(2 * Math.PI);
			double exphalf = Math.pow(Math.E, -0.5 * (x / sigma) * (x / sigma));
			double p = deltx * exphalf / sigrtpi;
			pmf.put(x + mean, p);
		}
		return pmf;
	}

	public PMF getSamples(Random rand, int samples) {
		PMF dest = new PMF();

		for (int i = 0; i < samples; i++) {
			double x = 0;
			double choice = rand.nextDouble();
			double sum = 0;
			for (Double k : this.keySet()) {
				sum += (Double) this.get(k);
				if (choice < sum) {
					x = k;
					break;
				}
			}
			dest.putSample(x);
		}
		return dest;
	}
//	protected Hashtable<Double, Double> h;
//	// elements = h.keySet. freqs = h.values()
//	protected String name;
//
//	public PMF() {
//		this(true);
//	}
//
//	public PMF(boolean b) {
//		this("PMF", b);
//	}
//
//	public PMF(String nameOfPDF, boolean b) {
//		this.name = nameOfPDF;
//		h = new Hashtable<Double, Double>();
//	}
//
//	public PMF(Hashtable<Double, Double> hs) {
//		h = hs;
//		// size = hs.size();
//	}
//
//	public Hashtable<Double, Double> getHashtable() {
//		return h;
//	}
//
//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
//
//	public boolean isEmpty() {
//		return h.isEmpty();
//	}
//
//	public int size() {
//		return h.size();
//	}
//
//	public double get(Double element) {
//		if (h.get(element) == null) {
//			return Double.NaN;
//		} else {
//			return h.get(element);
//		}
//	}
//
//	public synchronized void put(Double element, double p) {
//		if (p > 0) {
//			h.put(element, p);
//		} else {
//			h.put(element, 0.);
//		}
//	}
//
//	public HashSet<Double> keySet() {
//		return new HashSet<Double>(h.keySet());
//	}
//
//	public HashSet<Double> valueSet() {
//		return new HashSet<Double>(h.values());
//	}
//
//	public String toString() {
//		TreeSet<Double> ts = new TreeSet<Double>();
//		Set<Double> keys = h.keySet();
//		for (Double k : keys) {
//			ts.add(k);
//		}
//		String s = "" + getTotalOfFreqs() + " " + this.getMean();
//		// for (Double d:ts){
//		// s += " "+d+" "+h.get(d)+",";
//		// }
//		return s;
//	}
//
//	public void print() {
//		TreeSet<Double> ts = new TreeSet<Double>();
//		Set<Double> keys = h.keySet();
//		for (Double k : keys) {
//			ts.add(k);
//		}
//		for (Double d : ts) {
//			System.out.println(d + " " + h.get(d));
//		}
//	}
//
//	// public synchronized boolean equals(Object o) {
//	// if (o == this)
//	// return true;
//	// Class cl = getClass();
//	// if (!cl.isInstance (o))return false;
//	// Relation t = (Relation) o;
//	// if (t.size() != size())
//	// return false;
//	//
//	// Set keyset = keySet();
//	// Set tset = t.keySet();
//	// if (!keyset.equals(tset))return false;
//	//
//	// Iterator it = keyset.iterator();
//	// while (it.hasNext()) {
//	// Object key = it.next();
//	// Set valueset = getSet(key);
//	// Set tvalueset = t.getSet(key);
//	// if (!valueset.equals(tvalueset))
//	// return false;
//	// }
//	// return true;
//	// }
//
//	public static PMF makePMF(double[] elements, double[] probabilities) {
//		PMF pmf = new PMF(true);
//		for (int i = 0; i < elements.length; i++) {
//			pmf.put(elements[i], probabilities[i]);
//		}
//		return pmf;
//	}
//
//	public static PMF makePMF(String nameOfPDF, double[] elements,
//			double[] probabilities) {
//		PMF pmf = new PMF(nameOfPDF, true);
//		for (int i = 0; i < elements.length; i++) {
//			pmf.put(elements[i], probabilities[i]);
//		}
//		return pmf;
//	}
//
//	public double vectorProduct(PMF other) {
//		double total = 0;
//		HashSet elements = new HashSet(this.keySet());
//		HashSet others = other.keySet();
//		elements.retainAll(others);
//		for (Object element : elements) {
//			double p = (double) this.get((Double) element)
//					* (double) other.get((Double) element);
//			total += p;
//		}
//		return total;
//	}
//
//	public double bernouliCombination() {
//		double prod = 1;
//		for (Object key : keySet()) {
//			Double p = (Double) h.get(key);
//			prod *= (1 - p);
//		}
//		return 1 - prod;
//	}
//
//	public double getTotalOfFreqs() {
//		double total = 0;
//		for (Double k : keySet()) {
//			total += h.get(k);
//		}
//		return total;
//	}
//
//	public double getFreqWeightedSum() {
//		double sum = 0;
//		for (Double k : keySet()) {
//			sum += k * h.get(k);
//		}
//		return sum;
//	}
//
//	public void normalize() {
//		double total = getTotalOfFreqs();
//		if (total <= 0)
//			return;
//		for (Double k : keySet()) {
//			h.put(k, h.get(k) / total);
//		}
//	}
//
//	public double getMean() {
//		double total = getTotalOfFreqs();
//		double sum = getFreqWeightedSum();
//		return sum / total;
//	}
//
//	public double getStd() {
//		double mean = getMean();
//		double sum = 0;
//		for (Double k : keySet()) {
//			double delta = k - mean;
//			sum += h.get(k) * delta * delta;
//		}
//		return Math.sqrt(sum / getTotalOfFreqs());
//	}
//
//	public double getHighestNonZero() {
//		TreeSet ts = new TreeSet(keySet());
//		Iterator it = ts.descendingIterator();
//		while (it.hasNext()) {
//			double d = ((Double) it.next()).doubleValue();
//			double dd = get(d);
//			if (dd == 0)
//				continue;
//			return d;
//		}
//		return 0;
//	}
//
//	public double getLowestNonZero() {
//		TreeSet ts = new TreeSet(keySet());
//		for (Object o : ts) {
//			double d = get((Double) o);
//			;
//			if (d == 0)
//				continue;
//			return (Double) o;
//		}
//		return 0;
//	}
//
//	public static PMF computeNormal(double mean, double sigma, double deltx) {
//		PMF pmf = new PMF();
//		double sump = 0;
//		double sumplnp = 0;
//		for (double x = -3 * sigma; x <= 3 * sigma; x = x + deltx) {
//			double sigrtpi = sigma * Math.sqrt(2 * Math.PI);
//			double exphalf = Math.pow(Math.E, -0.5 * (x / sigma) * (x / sigma));
//			double p = deltx * exphalf / sigrtpi;
//			pmf.put(x + mean, p);
//		}
//		return pmf;
//	}
//
//	public PMF getSamples(Random rand, int samples) {
//		PMF dest = new PMF();
//		for (int i = 0; i < samples; i++) {
//			double x = ProbabilityChoice.makeSelectionFromPMF(rand, this);
//			dest.put(x, 1.);
//		}
//		return dest;
//	}
// 	public void drawLineChart(){
// 		Hashtable<String,List<XYPair>> lineData = new Hashtable<String,List<XYPair>>();
// 		Hashtable<Double,Double> pmfHash = getHashtable();
// 		List<XYPair> pairList = new ArrayList<XYPair>();
// 		Enumeration<Double> en = pmfHash.keys();
// 		while(en.hasMoreElements()){
// 			Double key = en.nextElement();
// 			Double value = pmfHash.get(key);
// 			pairList.add(new XYPair(key,value));
// 		}
		
// 		lineData.put(name, pairList);
		
// 		final LineChartGenerator demo = new LineChartGenerator("Line Chart "+this.name,lineData,"Distribution","number","prob");
// 		final String name = this.name;
// 		Display.getDefault().asyncExec(new Runnable() {
//             @Override
//             public void run() {
//             	Display display = Display.getCurrent();
//                 Shell shell = new Shell(display);
//                 shell.setSize(500, 300);
//                 shell.setLayout(new FillLayout());
//                 shell.setText("Line Chart of "+name);
//                 ChartComposite frame = new ChartComposite(shell, SWT.NONE, demo.getJFreeChart(), true);
//                 frame.setDisplayToolTips(true);
//                 frame.setHorizontalAxisTrace(false);
//                 frame.setVerticalAxisTrace(false);
//                 shell.open();
//                 while (!shell.isDisposed()) {
//                     if (!display.readAndDispatch())
//                         display.sleep();
//                 }
//             }
//         });
		
// 		//		demo.pack();
// //		RefineryUtilities.centerFrameOnScreen(demo);
// //		demo.setVisible(true);
// 	}
// 	public void drawLineChart(String title){
// 		Hashtable<String,List<XYPair>> lineData = new Hashtable<String,List<XYPair>>();
// 		Hashtable<Double,Double> pmfHash = getHashtable();
// 		List<XYPair> pairList = new ArrayList<XYPair>();
// 		Enumeration<Double> en = pmfHash.keys();
// 		while(en.hasMoreElements()){
// 			Double key = en.nextElement();
// 			Double value = pmfHash.get(key);
// 			pairList.add(new XYPair(key,value));
// 		}
		
// 		lineData.put(name, pairList);
		
// 		final LineChartGenerator demo = new LineChartGenerator("Line Chart "+title,lineData,"Distribution","number","prob");
// 		final String chartTitle = title;
// 		//final JFreeChart chart = createChart(createDataset());
// 		Display.getDefault().asyncExec(new Runnable() {
//             @Override
//             public void run() {
//             	Display display = Display.getCurrent();
//                 Shell shell = new Shell(display);
//                 shell.setSize(500, 300);
//                 shell.setLayout(new FillLayout());
//                 shell.setText("Line Chart of "+chartTitle);
//                 ChartComposite frame = new ChartComposite(shell, SWT.NONE, demo.getJFreeChart(), true);
//                 frame.setDisplayToolTips(true);
//                 frame.setHorizontalAxisTrace(false);
//                 frame.setVerticalAxisTrace(false);
//                 shell.open();
//                 while (!shell.isDisposed()) {
//                     if (!display.readAndDispatch())
//                         display.sleep();
//                 }
//             }
//         });
		
// //		demo.pack();
// //		RefineryUtilities.centerFrameOnScreen(demo);
// //		demo.setVisible(true);
// 	}
// 	public void drawLineChart(String title, List<PMF> pmfList){
// 		Hashtable<String,List<XYPair>> lineData = new Hashtable<String,List<XYPair>>();
// 		Hashtable<Double,Double> pmfHash = getHashtable();
// 		List<XYPair> pairList = new ArrayList<XYPair>();
// 		Enumeration<Double> en = pmfHash.keys();
// 		while(en.hasMoreElements()){
// 			Double key = en.nextElement();
// 			Double value = pmfHash.get(key);
// 			pairList.add(new XYPair(key,value));
// 		}
		
// 		lineData.put(name, pairList);
// 		for(PMF pmf : pmfList){
// 			pmfHash = pmf.getHashtable();
// 			pairList = new ArrayList<XYPair>();
// 			en = pmfHash.keys();
// 			while(en.hasMoreElements()){
// 				Double key = en.nextElement();
// 				Double value = pmfHash.get(key);
// 				pairList.add(new XYPair(key,value));
// 			}
// 			lineData.put(pmf.getName(), pairList);
// 		}
// 		final LineChartGenerator demo = new LineChartGenerator("Line Chart "+title,lineData,"Distribution","number","prob");
// 		final String chartTitle = title;
// 		//final JFreeChart chart = createChart(createDataset());
// 		Display.getDefault().asyncExec(new Runnable() {
//             @Override
//             public void run() {
//             	Display display = Display.getCurrent();
//                 Shell shell = new Shell(display);
//                 shell.setSize(500, 300);
//                 shell.setLayout(new FillLayout());
//                 shell.setText("Line Chart of "+chartTitle);
//                 ChartComposite frame = new ChartComposite(shell, SWT.NONE, demo.getJFreeChart(), true);
//                 frame.setDisplayToolTips(true);
//                 frame.setHorizontalAxisTrace(false);
//                 frame.setVerticalAxisTrace(false);
//                 shell.open();
//                 while (!shell.isDisposed()) {
//                     if (!display.readAndDispatch())
//                         display.sleep();
//                 }
//             }
//         });
		
// //		demo.pack();
// //		RefineryUtilities.centerFrameOnScreen(demo);
// //		demo.setVisible(true);
// 	}
// 	public static void drawLineCharts(String title, List<PMF> pmfList){
// 		Hashtable<String,List<XYPair>> lineData = new Hashtable<String,List<XYPair>>();
// 		Hashtable<Double,Double> pmfHash = null;
// 		List<XYPair> pairList = new ArrayList<XYPair>();
// 		Enumeration<Double> en = null;
// //		while(en.hasMoreElements()){
// //			Double key = en.nextElement();
// //			Double value = pmfHash.get(key);
// //			pairList.add(new XYPair(key,value));
// //		}
// //		
// //		lineData.put(name, pairList);
// 		for(PMF pmf : pmfList){
// 			pmfHash = pmf.getHashtable();
// 			pairList = new ArrayList<XYPair>();
// 			en = pmfHash.keys();
// 			while(en.hasMoreElements()){
// 				Double key = en.nextElement();
// 				Double value = pmfHash.get(key);
// 				pairList.add(new XYPair(key,value));
// 			}
// 			lineData.put(pmf.getName(), pairList);
// 		}
		
// 		final LineChartGenerator demo = new LineChartGenerator("Line Chart "+title,lineData,"Distribution","number","prob");
// 		final String chartTitle = title;
// 		//final JFreeChart chart = createChart(createDataset());
// 		Display.getDefault().asyncExec(new Runnable() {
//             @Override
//             public void run() {
//             	Display display = Display.getCurrent();
//                 Shell shell = new Shell(display);
//                 shell.setSize(500, 300);
//                 shell.setLayout(new FillLayout());
//                 shell.setText("Line Chart of "+chartTitle);
//                 ChartComposite frame = new ChartComposite(shell, SWT.NONE, demo.getJFreeChart(), true);
//                 frame.setDisplayToolTips(true);
//                 frame.setHorizontalAxisTrace(false);
//                 frame.setVerticalAxisTrace(false);
//                 shell.open();
//                 while (!shell.isDisposed()) {
//                     if (!display.readAndDispatch())
//                         display.sleep();
//                 }
//             }
//         });
		
// //		demo.pack();
// //		RefineryUtilities.centerFrameOnScreen(demo);
// //		demo.setVisible(true);
// 	}
	public static void main(String[] args) {
		// PMF pmf = makePMF(new double[]{1,2,3} , new double[]{1,1,1} );
		PMF pmf = computeNormal(2, 10, .01);

		double x = ProbabilityChoice.makeSelectionFromPMF(
				new java.util.Random(), pmf);
		x = x;
	}

}
