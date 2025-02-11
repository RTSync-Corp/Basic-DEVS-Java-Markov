package com.ms4systems.devs.markov;

import com.ms4systems.devs.analytics.*;
//import markovmodel.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
//import com.ms4systems.devs.markovmodel.*;


// import org.json.simple.JSONArray;
// import org.json.simple.JSONObject;
// import org.json.simple.parser.JSONParser;
// import org.json.simple.parser.ParseException;

//import com.ms4systems.devs.analytics.SampleFromExponential;

import com.ms4systems.devs.analytics.*;

public class ContinuousTimeMarkov {
	public static long Seed = 2349991;
	public static Random Rand = new Random(Seed);
    protected double AccLifeTime = 0;
    protected ArrayList<TimeInState> TimeInStateList;
    protected ArrayList<TransitionInfo> TransitionInfoList;
    protected double AvgLifeTime = 0;
    
  //  protected MarkovMat2 mm;
    protected String nextState = "";
    protected double timeToNextEvent;
    
    protected boolean isOutput;
    
    protected HashSet<String> endStateSet;
    
    protected String initialState ="";
    public ContinuousTimeMarkov() {
    	nextState = "";
    	AccLifeTime = 0;
    	AvgLifeTime = 0;
//    	Seed = 2349991;
//    	Rand = new Random(Seed);
    	TimeInStateList=new ArrayList<TimeInState>();
    	TransitionInfoList =new ArrayList<TransitionInfo>();
    	endStateSet = new HashSet<String>();
    	isOutput = false;
    }
    //BPZ
    public double getHoldTime(String phase,double mean) {
    	TransitionInfo ti = new TransitionInfo();
    	ti.sfd = new SampleFromExponential(1);
    	return mean*ti.sfd.getSample();
    }
//    public void internalTransitionFor(String state) {
//        ArrayList<String> successors = getSuccs(state);
//        if (timeToNextEvent == 0) {
//            timeToNextEvent = Double.POSITIVE_INFINITY;
//            int i = 0;
//            for (String succ : successors) {
//                TransitionInfo ti = getTransitionInfoFor(state, succ);
//                double prob = ti.getProbValue();
//                double time = timeToNextEvent(prob);
//                if (time < timeToNextEvent) {
//                    timeToNextEvent = time;
//                    nextState = succ;
//                }
//                i++;
//            }
//        }
//        if (previousPhase != null && previousPhase.equals(state)) {
//            timeToNextEvent = 0.;
//            holdIn(nextState, 0.);
//        } else {
//            TimeInState tm = getTimeInState(state);
//            if (tm == null) {
//                tm = new TimeInState(); //state,0,0.)
//                tm.setStateName(state);
//                tm.setCountInState(0);
//                tm.setElapsedTime(0.);
//                TimeInStateList.add(tm);
//            }
//            holdIn(state, timeToNextEvent);
//            previousPhase = state;
//            incCount(tm);
//            updateElapsedTime(tm, timeToNextEvent);
//            AccLifeTime += timeToNextEvent;
//            printTimeInState();
//        }
//    }
//
//    public void internalTransitionFor(String state, String[] successors,
//        double[] probabilities) {
//        if (timeToNextEvent == 0) {
//            timeToNextEvent = Double.POSITIVE_INFINITY;
//            int i = 0;
//            for (String succ : successors) {
//                double time = timeToNextEvent(probabilities[i]);
//                if (time < timeToNextEvent) {
//                    timeToNextEvent = time;
//                    nextState = succ;
//                }
//                i++;
//            }
//        }
//        if (previousPhase != null && previousPhase.equals(state)) {
//            timeToNextEvent = 0.;
//            holdIn(nextState, 0.);
//        } else {
//            TimeInState tm = getTimeInState(state);
//            if (tm == null) {
//                tm = new TimeInState(); //state,0,0.)
//                tm.setStateName(state);
//                tm.setCountInState(0);
//                tm.setElapsedTime(0.);
//                TimeInStateList.add(tm);
//            }
//            holdIn(state, timeToNextEvent);
//            previousPhase = state;
//            incCount(tm);
//            updateElapsedTime(tm, timeToNextEvent);
//            AccLifeTime += timeToNextEvent;
//            printTimeInState();
//        }
//    }

    public double timeToNextEvent(double lambda) {
        double sample = Rand.nextDouble();
        return -(1 / lambda) * Math.log(sample);
    }
    
    // Get a sample time from time information in TransitionInfo (9/17/2017 cseo)
    public double timeToNextEvent(String state, String succ, double norm) {
    	if(state.equals(succ)) return Double.MAX_VALUE;
    	TransitionInfo ti = getTransitionInfoFor(state, succ);
    	if(ti.getTInfo().getType().equals("None")){
    		return ti.sfd.getSample(norm);
    	}
    	return ti.sfd.getSample();
    }
    
    public double getMeanValue(String state, String succ){
    	if(state.equals(succ)) return -1.0;
    	TransitionInfo ti = getTransitionInfoFor(state, succ);
    	if(ti.getTInfo().getType().equals("None")){
//    		double lambda = normalFactor(state);
//    		if(lambda != 0.0) return 1/lambda;
    		return -1.0;
    	}
    	return ti.sfd.getMean();
    }
    
    public void incCount(TimeInState tm) {
        int ct = tm.getCountInState() + 1;
        tm.setCountInState(ct);
    }

    public void updateElapsedTime(TimeInState tm, double e) {
        double ct = tm.getElapsedTime() + e;
        tm.setElapsedTime(ct);
    }

    public TimeInState getTimeInState(String state) {
        for (TimeInState tm : TimeInStateList) {
            if (tm.getStateName().equals(state)) {
                return tm;
            }
        }
        return null;
    }

    public void printTimeInState() {
    	HashSet hs = getStates();
        for (TimeInState tm : TimeInStateList) {
        	hs.remove(tm.getStateName());
            System.out.println(tm.getStateName() + " " + tm.getElapsedTime() +
                " " + tm.getCountInState());
        }
        System.out.println("Unreached states "+hs);
    }

//    public String TimeInStateString() {
//        String s = "" + getName() + " ";
//        for (TimeInState tm : TimeInStateList) {
//            s += (tm.getStateName() + " " + tm.getElapsedTime() + " " +
//            tm.getCountInState());
//        }
//        return s;
//    }
    public String TimeInStateString(String state) {
        String s = "" + state + " ";
        for (TimeInState tm : TimeInStateList) {
            s += (tm.getStateName() + " " + tm.getElapsedTime() + " " +
            tm.getCountInState());
        }
        return s;
    }
    ///BPZ
    public void addTransitionInfoForExponential(String state, String[] successors,
        double[] probabilities,double[] mean) {
        int i = 0;
        for (String succ : successors) {
            TransitionInfo ti = new TransitionInfo();
            ti.setStartState(state);
            ti.setEndState(succ);
            ti.setProbValue(probabilities[i]);
            TransitionInfoList.add(ti);
            ///BPZ
            TimeInfo tf = new TimeInfo();
            tf.setType("Exponential");
            tf.setMean(mean[i]);
            ti.setTInfo(tf);
            i++;
        }
    }
    ///BPZ
    public void addTransitionInfoForExponential(String state, String[] successors,
            double[] probabilities) {
    	double[] means = new double[probabilities.length];
    	for (int i=0;i<means.length;i++) means[i] = 1.;
    	addTransitionInfoForExponential(state,successors,probabilities,means);
    }
    ///BPZ
    public void addTimeInfo(String state, String successor,String type,double mean){{
            TransitionInfo ti = this.getTransitionInfoFor(state, successor);
            TimeInfo tf = new TimeInfo();
            tf.setType(type);
            tf.setMean(mean);
            ti.setTInfo(tf);
        }
    }
    public void addTransitionInfo(String state, String[] successors,
            double[] probabilities) {
            int i = 0;
            for (String succ : successors) {
                TransitionInfo ti = new TransitionInfo();
                ti.setStartState(state);
                ti.setEndState(succ);
                ti.setProbValue(probabilities[i]);
                TransitionInfoList.add(ti);
                i++;
            }
        }
    public void replaceTransitions(ArrayList<TransitionInfo> tis) {
        for (TransitionInfo ti : tis) {
            replaceTransition(ti);
        }
    }

    public void replaceTransition(TransitionInfo ti) {
        if (ti.getStartState() != null && ti.getEndState() != null) {
            replaceTransition(ti.getStartState(), ti.getEndState(),
                ti.getProbValue());
        }
    }

    public void replaceTransition(String state, String succ, double prob) {
        TransitionInfo ti = this.getTransitionInfoFor(state, succ);
        if (ti != null) {
            double p = ti.getProbValue();
            ti.setProbValue(prob);
            return;
        }
        ti = new TransitionInfo();
        ti.setStartState(state);
        ti.setEndState(succ);
        ti.setProbValue(prob);
        TransitionInfoList.add(ti);
    }

    public ArrayList<TransitionInfo> lumpTransitionInfoList(String state,
        String endState, TransitionInfo[] tis, double[] occurProbs) {
        TransitionInfo tbase = getTransitionInfoFor(state, endState);
        if (tbase == null) {
            return TransitionInfoList;
        }
        double pbase = tbase.getProbValue();
        double sumOfOccur = 0;
        double sumOfWeightedTransProb = 0;
        int i = 0;
        for (TransitionInfo ti : tis) {
            double pv = ti.getProbValue();
            double pt = occurProbs[i];
            sumOfWeightedTransProb += pt * pv;
            sumOfOccur += pt;
            i++;
        }
        double pbleft = 1 - sumOfOccur;
        sumOfWeightedTransProb += pbleft * pbase;
        ArrayList<TransitionInfo> newtl = new ArrayList<TransitionInfo>();
        TransitionInfo tn = new TransitionInfo();
        tn.setStartState(state);
        tn.setEndState(endState);
        tn.setProbValue(sumOfWeightedTransProb);
        for (TransitionInfo ti : TransitionInfoList) {
            if (!ti.getStartState().equals(state)) {
                newtl.add(ti);
            } else {
                newtl.add(tn);
            }
        }
        return newtl;
    }

    public ArrayList<TransitionInfo> getTransitionInfoFor(String state) {
        ArrayList<TransitionInfo> sublist = new ArrayList<TransitionInfo>();
        ArrayList<TransitionInfo> l = getTransitionInfoList();
        for (TransitionInfo ti : l) {
            if (ti.getStartState().equals(state)) {
                sublist.add(ti);
            }
        }
        return sublist;
    }

    public TransitionInfo getTransitionInfoFor(String state, String succ) {
        ArrayList<TransitionInfo> l = getTransitionInfoFor(state);
        for (TransitionInfo ti : l) {
            if (ti.getEndState().equals(succ)) {
                return ti;
            }
        }
        return null;
    }
    public double[] probabilitiesfor(String state){
    	double probs[] = new double[getStates().size()];
        for (int i = 0; i < probs.length; i++) {
            probs[i] = 0;
        }
        for (int i = 0; i < TransitionInfoList.size(); i++) {
            TransitionInfo ti = TransitionInfoList.get(i);
            if (ti.getStartState().equals(state)) {
                int index = getIndex(ti.getEndState());
                probs[index] = ti.getProbValue();
            }
        }
        double sum = 0;
        for (int i = 0; i < probs.length; i++) {
            sum += probs[i];
        }
        if (sum <1) probs[getIndex(state)] = 1 - sum;
    	
    	return probs;
    }
    public double[] TransitionMeansfor(String state){
    	double means[]= new double[getStates().size()];
        for (int i = 0; i < means.length; i++) {
        	means[i] = 0;
        }
        for (int i = 0; i < TransitionInfoList.size(); i++) {
            TransitionInfo ti = TransitionInfoList.get(i);
            if (ti.getStartState().equals(state)) {
            	if(ti.getTInfo().getType().equals("none")){
            		int index = getIndex(ti.getEndState());
                    means[index] = 1/normalFactor(state);
            	}else{
            		int index = getIndex(ti.getEndState());
                    means[index] = ti.sfd.getMean();
            	}                
            }
        }            	
    	return means;
    }
    public double normalFactor(String state) {
        double sum = 0;
        for (String succ : getSuccs(state)) {
            if (succ.equals(state)) {
                continue;
            }
            TransitionInfo ti = getTransitionInfoFor(state, succ);
            sum += ti.getProbValue();
        }
        return sum;
    }
    
    public HashSet<String> getStates() {
        HashSet<String> str = new HashSet<String>();
        for (int i = 0; i < TransitionInfoList.size(); i++) {
            TransitionInfo ti = TransitionInfoList.get(i);
            str.add(ti.getStartState());
        }
        return str;
    }

    public ArrayList<String> indexStates() {
        HashSet<String> h = getStates();
        ArrayList<String> al = new ArrayList<String>();
        for (String str : h) {
            al.add(str);
        }
        Collections.sort(al);
        return al;
    }
    public ArrayList<String> indexStatesNoSorting() {
        //HashSet<String> h = getStates();
        ArrayList<String> al = new ArrayList<String>();
        for (int i = 0; i < TransitionInfoList.size(); i++) {
            TransitionInfo ti = TransitionInfoList.get(i);
            al.add(ti.getStartState());
        }
       
        //Collections.sort(al);
        return al;
    }
    public int getIndex(String state) {
        ArrayList<String> al = indexStates();
        for (int i = 0; i < al.size(); i++) {
            if (al.get(i).equals(state)) {
                return i;
            }
        }
        return -1;
    }

    public double[] getProbs(String state) {
        double[] probs = new double[getStates().size()];
        for (int i = 0; i < probs.length; i++) {
            probs[i] = 0;
        }
        for (int i = 0; i < TransitionInfoList.size(); i++) {
            TransitionInfo ti = TransitionInfoList.get(i);
            if (ti.getStartState().equals(state)) {
                int index = getIndex(ti.getEndState());
                probs[index] = ti.getProbValue();
            }
        }
        double sum = 0;
        for (int i = 0; i < probs.length; i++) {
            sum += probs[i];
        }
        if (sum <1)
        probs[getIndex(state)] = 1 - sum;
        return probs;
    }
    public ArrayList<Double> getProbsArray(String state) {
        ArrayList<Double> ar = new ArrayList<Double>();
        ArrayList<TransitionInfo> l = getTransitionInfoFor(state);
        for (int i = 0; i < l.size(); i++) {
            TransitionInfo ti = l.get(i);
            ar.add(ti.getProbValue());
        }
        return ar;
    }

    public ArrayList<String> getSuccs(String state) {
        ArrayList<String> ar = new ArrayList<String>();
        ArrayList<TransitionInfo> l = getTransitionInfoFor(state);
        for (int i = 0; i < l.size(); i++) {
            TransitionInfo ti = l.get(i);
            ar.add(ti.getEndState());
        }
        return ar;
    }

    public String nextPhase(String phase) {
	return ProbabilityChoice.selectPhase(Rand, getProbsArray(phase), getSuccs(phase) );
    }
	
//    public void setInitialStateAndTransitionMatrix(MarkovMat devs) {
//        devs.stateVector = new double[getStates().size()];
//        devs.stateVector[0] = 1;
//        devs.lastStateVector = devs.stateVector;
//        devs.TransitionMatrix = new Matrix(getStates().size());
//        ArrayList<String> states = indexStates();
//        for (int i = 0; i < states.size(); i++) {
//            devs.TransitionMatrix.setColumn(i, getProbs(states.get(i)));
//        }
//        devs.TransitionMatrix.print();
//        SimulationOptionsImpl options =
//            new SimulationOptionsImpl(new String[] {  }, true);
//
//        // Uncomment the following line to disable SimViewer for this model
//        options.setDisableViewer(true);
//
//        // Uncomment the following line to disable plotting for this model
//        options.setDisablePlotting(true);
//
//        devs.options = options;
//
//        if (options.isDisableViewer()) { // Command line output only
//            Simulation sim =
//                new com.ms4systems.devs.core.simulation.impl.SimulationImpl(" Simulation",
//                    devs, options);
//            sim.startSimulation(0);
//            sim.simulateIterations(Long.MAX_VALUE);
//        } else { // Use SimViewer
//            SimViewer viewer = new SimViewer();
//            viewer.open(devs, options);
//        }
//    }
//
//    public void setInitialStateAndTransitionList(ContinuousTimeMarkov devs) {
//        devs.fillTransitionInfoList(devs.getName() + ".xml");
//        devs.TimeInStateList = new ArrayList<TimeInState>();
//
//        SimulationOptionsImpl options =
//            new SimulationOptionsImpl(new String[] {  }, true);
//
//        // Uncomment the following line to disable SimViewer for this model
//        options.setDisableViewer(true);
//
//        // Uncomment the following line to disable plotting for this model
//        options.setDisablePlotting(true);
// 
//        devs.options = options;
//
//        if (options.isDisableViewer()) { // Command line output only
//            Simulation sim =
//                new com.ms4systems.devs.core.simulation.impl.SimulationImpl(" Simulation",
//                    devs, options);
//            sim.startSimulation(0);
//            sim.simulateIterations(Long.MAX_VALUE);
//        } else { // Use SimViewer
//            SimViewer viewer = new SimViewer();
//            viewer.open(devs, options);
//        }
//    }
    
//    public ContinuousTimeMarkov setInitialStateAndTransitionList(int size,
//            double prob) {
//            ContinuousTimeMarkov ctm = new ContinuousTimeMarkov();
//            MarkovMat mm = new MarkovMat();
//            mm.setRand(new Random(234567));
//            mm.setInitialStateAndTransitionMatrix(size, prob);
//            ctm.TransitionInfoList = new ArrayList<TransitionInfo>();
//            String[] states = new String[size];
//            for (int i = 0; i < size; i++) {
//                states[i] = "State" + i;
//            }
//            markov.Matrix mat = mm.getTransitionMatrix();
//            for (int i = 0; i < size; i++) {
//                double[] probs = new double[size];
//                String state = "State" + i;
//                String[] successors = new String[size];
//                for (int j = 0; j < size; j++) {
//                    double p = (mat.m)[j][i];
//                        probs[j] = p;
//                    }
//                ctm.addTransitionInfo(state, states, probs);
//            }
//            ctm.setTimeInStateList(new ArrayList<TimeInState>());
//            return ctm;
//        }

//    //////////////////////////////// xml
//    public Document parseXmlFile(String fname) {
//
//        //get the factory
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",null);
//        Document dom = null;
//        try {
//
//            //Using factory get an instance of document builder
//            DocumentBuilder db = dbf.newDocumentBuilder();
//
//            //parse using builder to get DOM representation of the XML file
//            File f = new File(fname);
//            if(f.exists()){
//            	dom = db.parse(f);
//            }else{
//            	MessageBox info_writeFile = new MessageBox(PlatformUI.getWorkbench()
//        				.getDisplay().getActiveShell(), SWT.ICON_ERROR);
//                info_writeFile.setText("File Not Existing Error");
//                info_writeFile.setMessage("Selected XML file does not exist.");
//                info_writeFile.open();
//            }
//            
//
//        } catch (ParserConfigurationException pce) {
//            pce.printStackTrace();
//        } catch (SAXException se) {
//            se.printStackTrace();
//        } catch (IOException ioe) {        	
//            ioe.printStackTrace();
//        }
//        return dom;
//    }
//    public Document parseXmlFile(String fname, String path) {
//
//        //get the factory
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        Document dom = null;
//        try {
//
//            //Using factory get an instance of document builder
//            DocumentBuilder db = dbf.newDocumentBuilder();
//
//            //parse using builder to get DOM representation of the XML file
//            File f = new File(path+fname);
//            if(f.exists()){
//            	dom = db.parse(f);
//            }else{
//            	MessageBox info_writeFile = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_ERROR);
//                info_writeFile.setText("File Not Existing Error");
//                info_writeFile.setMessage("Selected XML file does not exist.");
//                info_writeFile.open();
//            }
//
//        } catch (ParserConfigurationException pce) {
//            pce.printStackTrace();
//        } catch (SAXException se) {
//            se.printStackTrace();
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//        return dom;
//    }
//    // Parse a xml content (1/18/2018 cseo)
//    public Document parseXmlContent(String xmlContent) {
//
//        //get the factory
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",null);
//        Document dom = null;
//        
//        try {
//
//            //Using factory get an instance of document builder
//            DocumentBuilder db = dbf.newDocumentBuilder();
//            
//            //parse using builder to get DOM representation of the XML Content
//            StringReader reader = new StringReader(xmlContent);
//            InputSource in = new InputSource(reader);
//            dom = db.parse(in);
//                        
//        } catch (ParserConfigurationException pce) {
//            pce.printStackTrace();
//        } catch (SAXException se) {
//            se.printStackTrace();
//        } catch (IOException ioe) {        	
//            ioe.printStackTrace();
//        }
//        return dom;
//    }
//    public void fillTransitionInfoList(String fname) {
//        if (TransitionInfoList == null) {
//            TransitionInfoList = new ArrayList<TransitionInfo>();
//        }
//        Document dom = parseXmlFile(fname);
//        if(dom==null)return;
//        //get the root element
//        Element docEle = dom.getDocumentElement();
//
//        //get a nodelist of elements
//        NodeList nl = docEle.getElementsByTagName("TransitionInfo");
//        if (nl != null && nl.getLength() > 0) {
//            for (int i = 0; i < nl.getLength(); i++) {
//
//                //get the TransitionInfo element
//                Element el = (Element) nl.item(i);
//
//                //get the Employee object
//                TransitionInfo ti = getTransitionInfo(el);
//                
//                if(i==0) initialState = ti.getStartState();
//                //add it to list
//                TransitionInfoList.add(ti);
//            }
//        }
//        
//        // add missing TransitionInfo
//        ArrayList<TransitionInfo> tempList = new ArrayList<TransitionInfo>();
//        Iterator<String> it = endStateSet.iterator();
//        while(it.hasNext()){
//        	String state = it.next();
//        	boolean isStart = false;
//        	for(TransitionInfo tranInfo : TransitionInfoList){
//            	String start = tranInfo.getStartState();
//            	if(state.equals(start)){
//            		isStart = true;
//            		break;
//            	}
//            }
//        	if(!isStart) tempList.add(getTransitionInfo(state));
//        }
//        
//        
//        if(tempList.size()>0){
//        	TransitionInfoList.addAll(tempList);
//        }
//    }
//    public void fillTransitionInfoListFromJSON(String fname) {
//        if (TransitionInfoList == null) {
//            TransitionInfoList = new ArrayList<TransitionInfo>();
//        }
//        endStateSet = new HashSet<String>();
//        
//        JSONParser parser = new JSONParser();
//
//        try {
//        	
//            Object obj = parser.parse(new FileReader(fname));
//
//            JSONObject jsonObject = (JSONObject) obj;
//            //System.out.println(jsonObject);
//
//            String title = (String) jsonObject.get("title");
//
//            // loop array
//            JSONArray states = (JSONArray) jsonObject.get("states");
//            Iterator<String> iterator = states.iterator();
//            List<String> stateList=new ArrayList<String>();
//            boolean initial = true;
//            while (iterator.hasNext()) {
//            	String state = iterator.next();
//            	stateList.add(state);
//            	// The first state is an initial state
//            	if(initial) {
//            		initialState = state;
//            		initial = false;
//            	}                              
//            }
//            
//            //System.out.println(stateList);
//            JSONArray timedata = (JSONArray) jsonObject.get("timedata");
//            
//            TimeArray[][] timeInfo = new TimeArray[stateList.size()][stateList.size()];
//            Iterator<JSONArray> timeIt = timedata.iterator();
//            int i = 0;
//            while(timeIt.hasNext()){
//            	JSONArray timeinfo = timeIt.next();            	
//            	Iterator<List> tList = timeinfo.iterator();
//            	int k=0;
//            	while(tList.hasNext()){
//	            	List<Double> row = tList.next();
//	            	int j=0;
//	            	double [] data = new double[6];
//	            	for(double d : row){
//	            		data[j]=d;
//	            		//System.out.println(" "+d);
//	            		j++;
//	            	}
//	            	TimeArray ta = new TimeArray();
//	            	ta.setData(data);
//	            	timeInfo[i][k]=ta;
//	            	k++;
//            	}
//            	i++;
//            }  
//            
//            JSONArray data = (JSONArray) jsonObject.get("probabilitydata");
//            Iterator<List> iterator1 = data.iterator();
//            int rowIndex = 0;
//            while (iterator1.hasNext()) {
//            	List<Double> row = iterator1.next(); 
//                //System.out.println(row);
//                String[] typeArray = { "Uniform","Normal","Exponential","LogNormal","Pareto"};
//                int colIndex = 0;
//                for(double d : row){
//                	//System.out.println(d);
//                	if(rowIndex != colIndex && d != 0.0){
//	                	
//	                	TimeArray dArray = timeInfo[rowIndex][colIndex];
//	                	double[] tInfo = dArray.getData();
//	                	int index = (int)tInfo[0];
////	                	
//	                	//Create a TransitionInfo instance
//	                	TransitionInfo ti = new TransitionInfo();
//	                	String start = stateList.get(rowIndex);
//	                	String end = stateList.get(colIndex);
//	                    ti.StartState = start;
//	                    ti.EndState = end;
//	                    endStateSet.add(end);
//	                    ti.ProbValue = d;
//	                    String type = typeArray[index-1];
//	                    double lower=tInfo[1];
//	                    double upper=tInfo[2];
//	                    double mean=tInfo[3];
//	                    double sigma=tInfo[4];
//	                    double alpha=tInfo[5];
//	                    TimeInfo t = new TimeInfo(type,lower,upper,mean,sigma,alpha);
//	                    ti.setTInfo(t);
//	                    
//	                    TransitionInfoList.add(ti);
//                	}
//                	colIndex++;
//                }
//                rowIndex++;
//            }
//           
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        
//        // add missing TransitionInfo
//        ArrayList<TransitionInfo> tempList = new ArrayList<TransitionInfo>();
//        Iterator<String> it = endStateSet.iterator();
//        while(it.hasNext()){
//        	String state = it.next();
//        	boolean isStart = false;
//        	for(TransitionInfo tranInfo : TransitionInfoList){
//            	String start = tranInfo.getStartState();
//            	if(state.equals(start)){
//            		isStart = true;
//            		break;
//            	}
//            }
//        	if(!isStart) tempList.add(getTransitionInfo(state));
//        }
//                
//        if(tempList.size()>0){
//        	TransitionInfoList.addAll(tempList);
//        }
//    }
//    // Fill TransitionInfo List from xml content (1/18/2018 cseo)
//    public void fillTransitionInfoListFromXmlContent(String xmlContent) {
//        if (TransitionInfoList == null) {
//            TransitionInfoList = new ArrayList<TransitionInfo>();
//        }
//        Document dom = parseXmlContent(xmlContent);
//        if(dom==null)return;
//        //get the root element
//        Element docEle = dom.getDocumentElement();
//
//        //get a nodelist of elements
//        NodeList nl = docEle.getElementsByTagName("TransitionInfo");
//        if (nl != null && nl.getLength() > 0) {
//            for (int i = 0; i < nl.getLength(); i++) {
//
//                //get the TransitionInfo element
//                Element el = (Element) nl.item(i);
//
//                //get the Employee object
//                TransitionInfo ti = getTransitionInfo(el);
//                
//                if(i==0) initialState = ti.getStartState();
//                //add it to list
//                TransitionInfoList.add(ti);
//            }
//        }
//        
//        // add missing TransitionInfo
//        ArrayList<TransitionInfo> tempList = new ArrayList<TransitionInfo>();
//        Iterator<String> it = endStateSet.iterator();
//        while(it.hasNext()){
//        	String state = it.next();
//        	boolean isStart = false;
//        	for(TransitionInfo tranInfo : TransitionInfoList){
//            	String start = tranInfo.getStartState();
//            	if(state.equals(start)){
//            		isStart = true;
//            		break;
//            	}
//            }
//        	if(!isStart) tempList.add(getTransitionInfo(state));
//        }
//        
//        
//        if(tempList.size()>0){
//        	TransitionInfoList.addAll(tempList);
//        }
//    }
//    public TransitionInfo getTransitionInfo(String state) {
//       
//        double p = 1.0;
//
//        TransitionInfo ti = new TransitionInfo();
//        ti.StartState = state;
//        ti.EndState = state;
//        ti.ProbValue = p;
//        return ti;
//    }
//    public TransitionInfo getTransitionInfo(Element TransEl) {
//        String start = getTextValue(TransEl, "StartState");
//        String end = getTextValue(TransEl, "EndState");
//        double p = getDoubleValue(TransEl, "ProbValue");
//        NodeList tInfoList = TransEl.getElementsByTagName("TimeInfo");
//        TimeInfo tInfo = new TimeInfo();
//        if(tInfoList!=null && tInfoList.getLength()>0){
//        	tInfo = getTimeInfo(TransEl);
//        }
//        
//        TransitionInfo ti = new TransitionInfo();
//        ti.StartState = start;
//        ti.EndState = end;
//        endStateSet.add(end);
//        ti.ProbValue = p;
//        ti.setTInfo(tInfo);
//        return ti;
//    }
//    
//    public TimeInfo getTimeInfo(Element TransEl){
//    	String type = getTextValue(TransEl, "Type");
//    	double lower = getDoubleValue(TransEl, "Lower");
//    	double upper = getDoubleValue(TransEl, "Upper");
//    	double mean = getDoubleValue(TransEl, "Mean");
//    	double sigma = getDoubleValue(TransEl, "Sigma");
//    	double alpha = getDoubleValue(TransEl, "Alpha");
//    	
//    	TimeInfo tInfo = new TimeInfo(type,lower,upper,mean,sigma,alpha);
//    	return tInfo;
//    }
//    public String getTextValue(Element ele, String tagName) {
//        String textVal = null;
//        NodeList nl = ele.getElementsByTagName(tagName);
//        if (nl != null && nl.getLength() > 0) {
//            Element el = (Element) nl.item(0);
//            textVal = el.getFirstChild().getNodeValue();
//        }
//
//        return textVal;
//    }
//
//    public Double getDoubleValue(Element ele, String tagName) {
//        return Double.parseDouble(getTextValue(ele, tagName));
//    }

	public Random getRand() {
		return Rand;
	}

	public void setRand(Random rand) {
		Rand = rand;
	}

	public double getAccLifeTime() {
		return AccLifeTime;
	}

	public void setAccLifeTime(double accLifeTime) {
		AccLifeTime = accLifeTime;
	}

	public ArrayList<TimeInState> getTimeInStateList() {
		return TimeInStateList;
	}

	public void setTimeInStateList(ArrayList<TimeInState> timeInStateList) {
		TimeInStateList = timeInStateList;
	}

	public ArrayList<TransitionInfo> getTransitionInfoList() {
		return TransitionInfoList;
	}

	public void setTransitionInfoList(ArrayList<TransitionInfo> transitionInfoList) {
		TransitionInfoList = transitionInfoList;
	}

	public double getAvgLifeTime() {
		return AvgLifeTime;
	}

	public void setAvgLifeTime(double avgLifeTime) {
		AvgLifeTime = avgLifeTime;
	}

	public long getSeed() {
		return Seed;
	}

	public void setSeed(long seed) {
		Seed = seed;
	}

//	public MarkovMat getMm() {
//		return mm;
//	}
//
//	public void setMm(MarkovMat mm) {
//		this.mm = mm;
//	}

	public String getNextState() {
		return nextState;
	}

	public void setNextState(String nextState) {
		this.nextState = nextState;
	}

	public double getTimeToNextEvent() {
		return timeToNextEvent;
	}

	public void setTimeToNextEvent(double timeToNextEvent) {
		this.timeToNextEvent = timeToNextEvent;
	}

	public boolean isOutput() {
		return isOutput;
	}

	public void setOutput(boolean isOutput) {
		this.isOutput = isOutput;
	}

	public String getInitialState() {
		return initialState;
	}

	public void setInitialState(String initialState) {
		this.initialState = initialState;
	}    
    
}
