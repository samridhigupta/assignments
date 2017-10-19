import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by samridhi on 10/09/15.
 */
public class Binning {
    public static void main (String args[]){
        String studentIdString = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40";
        String physicsMarksString ="65,78,37,75,55,54,55,63,95,87,46,59,67,91,58,57,58,74,75,74,66,47,67,79,64,70,67,56,62,52,68,48,98,51,13,74,63,52,99,24";
        String mathsMarksString ="82,48,45,87,15,60,19,32,65,48,14,43,61,100,30,45,19,45,37,10,26,2,14,65,43,23,49,21,54,62,69,34,91,14,4,8,68,43,88,0";
        String cPlusPlusString ="A,B,C,A,C,B,C,C,A,B,D,C,B,A,C,C,C,B,C,C,C,D,C,B,B,C,B,C,B,B,B,C,A,C,D,C,B,C,A,D";
        String[] studentID = studentIdString.split(",");
        
        double[] physicsMarks = getDoubleArray(physicsMarksString.split(","));
        double[] mathsMarks = getDoubleArray(mathsMarksString.split(","));
        String[] cPlusPlus = cPlusPlusString.split(",");

        double[] totalMarks= sum(physicsMarks, mathsMarks);

        int numberOfBins = 5;

        System.out.println("Answer 1: ");
        System.out.println("In Equal Width binning, number of students in each grade are: ");
        double[] gradesAccordingToEqualWidthBinning = calculateEqualWidthBinning(totalMarks, numberOfBins);

        System.out.println("Id      Maths       Physics       Total          Grade(Equal Width binning)");
        for (int i = 0; i < studentID.length; i++) {
            System.out.println(studentID[i] + "         " + mathsMarks[i] + "       " + physicsMarks[i] + "            "
                    + totalMarks[i] + "         "+ Character.toString((char) (70 - gradesAccordingToEqualWidthBinning[i] )));
        }

        System.out.println("Answer 2 a: ");
        System.out.println("In Equal Frequency binning, number of students in each grade are: ");
        double[] gradesAcordingToEqualFreequencyBinning = calculateGradesWithEqualFrequencyBinning(totalMarks, numberOfBins);

        System.out.println("Id      Maths       Physics       Total          Grade(Width binning) Grade(frequency binning)");
        for (int i = 0; i < studentID.length; i++) {
            System.out.println(studentID[i] + "         " + mathsMarks[i] + "       " + physicsMarks[i] + "            "
                    + totalMarks[i] + "             "+ Character.toString((char) (70 - gradesAccordingToEqualWidthBinning[i]))
                    + "                  "+Character.toString((char) (70 - gradesAcordingToEqualFreequencyBinning[i] )));
        }
        List<String> studentsHappyWithEqualWidthBinning = new ArrayList<>();
        List<String> studentsHappyWithEqualFrequencyBinning = new ArrayList<>();
        List<String> studentsWithChangedGrades = new ArrayList<>();
        for (int i = 0; i < studentID.length; i++) {
            if(gradesAccordingToEqualWidthBinning[i]>gradesAcordingToEqualFreequencyBinning[i]){
                studentsHappyWithEqualWidthBinning.add(studentID[i]);
            }else if(gradesAcordingToEqualFreequencyBinning[i]>gradesAccordingToEqualWidthBinning[i]){
                studentsHappyWithEqualFrequencyBinning.add(studentID[i]);
            }
            if(gradesAccordingToEqualWidthBinning[i]!=gradesAcordingToEqualFreequencyBinning[i]){
                studentsWithChangedGrades.add(studentID[i]);
            }
        }
        System.out.println("Answer 2 b: ");
        System.out.println("Students happy with equal width partition");
        for (String id : studentsHappyWithEqualWidthBinning){
            System.out.println(id);
        }
        System.out.println("Students happy with equal frequency partition");
        for (String id : studentsHappyWithEqualFrequencyBinning){
            System.out.println(id);
        }

        System.out.println("Answer 3: ");
        System.out.println("Students with changed grades because of change in binning method");
        for (String id : studentsWithChangedGrades){
            System.out.println(id);
        }
//Steps For question 4:
        double physicsMarksTotal= sum(physicsMarks);

        double mathsMarksTotal= sum(mathsMarks);

        double meanPhysicsMarks = physicsMarksTotal/physicsMarks.length;
        double meanMathsMarks = mathsMarksTotal/mathsMarks.length;

        double standardDeviationPhysicsMarks = computeStandardDeviation(physicsMarks,meanPhysicsMarks);
        double standardDeviationMathsMarks = computeStandardDeviation(mathsMarks,meanMathsMarks);

        double[] zScorePhysics = computeZScore(physicsMarks, meanPhysicsMarks, standardDeviationPhysicsMarks);
        double[] zScoreMaths = computeZScore(mathsMarks, meanMathsMarks, standardDeviationMathsMarks);

        double[] totalZScore = sum(zScoreMaths,zScorePhysics);
        System.out.println("Answer 4: ");
        System.out.println("In Equal Frequency binning method over ZScore, number of students in each grade are: ");
        double[] gradesAcordingToEqualFreequencyBinningOnZScore = calculateGradesWithEqualFrequencyBinning(totalZScore, numberOfBins);

        System.out.println("Id     Maths         Physics     Total            ZSore Total              Grade(frequency binning On ZScore)");
        for (int i = 0; i < studentID.length; i++) {
            System.out.println(studentID[i] + "      " + mathsMarks[i] + "          " + physicsMarks[i] + "         "
                    + totalMarks[i] + "          "+ totalZScore[i]
                    + "           "+Character.toString((char) (70 - gradesAcordingToEqualFreequencyBinningOnZScore[i] )));
        }

        List<String> studentsHappyWithZScoreBinning = new ArrayList<>();
        List<String> studentsHappyWithFrequencyBinningWithoutZScore = new ArrayList<>();
        List<String> studentsWithChangedGradesAfterZScore = new ArrayList<>();
        for (int i = 0; i < studentID.length; i++) {
            if(gradesAcordingToEqualFreequencyBinningOnZScore[i]>gradesAcordingToEqualFreequencyBinning[i]){
                studentsHappyWithZScoreBinning.add(studentID[i]);
            }else if(gradesAcordingToEqualFreequencyBinning[i]>gradesAcordingToEqualFreequencyBinningOnZScore[i]){
                studentsHappyWithFrequencyBinningWithoutZScore.add(studentID[i]);
            }
            if(gradesAcordingToEqualFreequencyBinningOnZScore[i]!=gradesAcordingToEqualFreequencyBinning[i]){
                studentsWithChangedGradesAfterZScore.add(studentID[i]);
            }
        }

        System.out.println("Answer 5: ");
        System.out.println("Students happy with equal frequency binning on ZScore");
        for (String id : studentsHappyWithZScoreBinning){
            System.out.println(id);
        }
        System.out.println("Students happy with equal frequency partition without Z Score");
        for (String id : studentsHappyWithFrequencyBinningWithoutZScore){
            System.out.println(id);
        }

        System.out.println("Students with changed grades because of binning based on ZScore");
        for (String id : studentsWithChangedGradesAfterZScore){
            System.out.println(id);
        }

        System.out.println("Answer 6: ");
        /*
        These Students are happy with frequency binning on the summation of ZScore because using binning on ZScore method
        we normalize the scores trying to give each attribute equal weight. This way we keep in mind performance in both
        the test individually. Since ZScore normalizes data on mean, if there were difference in the grading
        technique for maths and physics this method will try to mitigate that difference.*/
        System.out.println("These Students are happy with frequency binning on the summation of ZScore " +
                "because using binning on ZScore method we normalize the scores trying to give each attribute equal weight. " +
                "This way we keep in mind performance in both the test individually. Since ZScore normalizes data on mean " +
                "if there were difference in the grading technique for maths and physics this method will try to mitigate that difference.");


        //Steps for Question 7
        String[] levelScoreForMaths = computeLevelFor(zScoreMaths);
        String[] levelScoreForPhysics = computeLevelFor(zScorePhysics);
//        System.out.println("Level score for Maths");
//        for (int i = 0; i < zScoreMaths.length; i++) {
//            System.out.println(studentID[i]+"   "+zScoreMaths[i]+"  "+levelScoreForMaths[i]);
//        }
//        System.out.println("Level score for Physics");
//        for (int i = 0; i < zScorePhysics.length; i++) {
//            System.out.println(studentID[i]+"   "+zScorePhysics[i]+"  "+levelScoreForPhysics[i]);
//        }
        System.out.println("Answer 7: ");

        System.out.println("Level score for Maths And Physics");
        System.out.println("Id    Level Score(Maths)      Level Score(Physics)");
        for (int i = 0; i < zScoreMaths.length; i++) {
            System.out.println(studentID[i]+"           "+levelScoreForMaths[i]+"                 "+levelScoreForPhysics[i]);
        }

//        System.out.println("Level score for Maths, Physics and C++");
//        for (int i = 0; i < zScoreMaths.length; i++) {
//            System.out.println(studentID[i]+"   "+levelScoreForMaths[i]+"  "+levelScoreForPhysics[i]+"  "+cPlusPlus[i]);
//        }

        System.out.println("Answer 8: ");
        double entropyPhysics = computeEntropy(levelScoreForPhysics);
        double entropyMaths = computeEntropy(levelScoreForMaths);

        String betterPredictor = "Maths";
        if(entropyPhysics>entropyMaths)
            betterPredictor.replaceAll("Maths","Physics");
        System.out.println("Out of Physics or Maths, "+betterPredictor+" is the better predictor of C++ grades");
    }


    private static double computeEntropy(String[] levelScore) {
        int[] level = getCountForEachLevel(levelScore);
        double pLow = level[0]/levelScore.length;
        double pMid= level[1]/levelScore.length;
        double pHigh = level[2]/levelScore.length;
        double logLow = Math.log(pLow);
        double logMid = Math.log(pMid);
        double logHigh = Math.log(pHigh);
        return -1*((pLow*logLow)+(pMid*logMid)+(pHigh*logHigh));
    }

    private static int[] getCountForEachLevel(String[] levelScore) {
        int[] level = new int[3];
        for (int i = 0; i < levelScore.length; i++) {
            if(levelScore[i].equals("low"))
                level[0]++;
            else if(levelScore[i].equals("mid"))
                level[1]++;
            else
                level[2]++;
        }
        return level;
    }

    private static String[] computeLevelFor(double[] zScore) {
        HashMap<String,Double> levelMap = new HashMap<>();
        levelMap.put("low", -0.3);
        levelMap.put("mid", 0.3);
        String[] level = new String[zScore.length];
        for (int i = 0; i < zScore.length; i++) {
            if(zScore[i]<levelMap.get("low")){
                level[i]="low";
            }else if(zScore[i]<=levelMap.get("mid")){
                level[i]="mid";
            }else {
                level[i]="high";
            }
        }
        return level;
    }

    private static double sum(double[] data) {
        double sum = 0;
        for(int i = 0; i<data.length; i++){
            sum += data[i];
        }
        return sum;
    }

    private static double[] sum(double[] dataOne, double[] dataTwo) {
        double[] sum = new double[dataOne.length];
        for(int i = 0; i<dataOne.length; i++){
            sum[i] = dataOne[i]+dataTwo[i];
        }
        return sum;
    }

    private static double[] getDoubleArray(String[] data) {
        double[] array = new double[data.length]; 
        for (int i = 0; i < data.length; i++) {
            array[i]= Double.parseDouble(data[i]);
        }
        return array;
    }

    private static double[] computeZScore(double[] data, double mean, double standardDeviation) {
        double[] zScore = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            zScore[i] = ((data[i] - mean)/standardDeviation);
        }
        return zScore;
    }

    private static double computeStandardDeviation(double[] data, double mean) {
        double sum=0;
        for (int i = 0; i < data.length; i++) {
            sum += Math.pow((data[i] - mean), 2);
        }
        return Math.sqrt((sum / data.length));
    }

    private static double[] calculateGradesWithEqualFrequencyBinning(double[] data, int numberOfBins) {
        double[] sortedData = sort(Arrays.copyOf(data, data.length));
//      List<Double> sortedData = sort(Arrays.copyOf(data, data.length));
        int frequency = sortedData.length /numberOfBins;
        if(data.length%numberOfBins!= 0){
            frequency++;
        }
        double[] gradeLimit = new double[numberOfBins];
        gradeLimit[numberOfBins-1]= sortedData[sortedData.length - 1] +1;
        for (int i = 1; i < numberOfBins; i++) {
            gradeLimit[i-1]= (sortedData[((i) * frequency) - 1] + sortedData[((i) * frequency)])/2;
        }
        return getGrades(data, gradeLimit, frequency);
    }

    private static double[] getGrades(double[] totalMarks, double[] gradeLimit, int frequency) {
        double[] grades = new double[totalMarks.length];
        int[] numberOfStudentsInGrade = new int[gradeLimit.length];
        for(int i = 0; i<totalMarks.length; i++){
            for (int j = 0; j < gradeLimit.length; j++) {
                if(totalMarks[i]<=gradeLimit[j] && numberOfStudentsInGrade[j]<frequency){
                    grades[i]=j+1;
                    numberOfStudentsInGrade[j]++;
                    break;
                }
            }
        }
        int k =0;
        for (int i=numberOfStudentsInGrade.length; i>0; i--) {
            System.out.println(Character.toString((char) (65 + k++)) + " : " + numberOfStudentsInGrade[i - 1]);
        }
        return grades;
    }

    private static double[] getGrades(double[] totalMarks, double[] gradeLimit) {
        return getGrades(totalMarks,gradeLimit,totalMarks.length);
    }
    public static double[] sort(double[] input){
        double temp;
        for (int i = 1; i < input.length; i++) {
            for(int j = i ; j > 0 ; j--){
                if(input[j] < input[j-1]){
                    temp = input[j];
                    input[j] = input[j-1];
                    input[j-1] = temp;
                }
            }
        }
        return input;
    }

    private static double[] calculateEqualWidthBinning(double[] totalMarks, int numberOfBins) {
        double maxMarks = findMax(totalMarks);
        double minMarks = findMin(totalMarks,maxMarks);
        double binLimit = (double)(maxMarks-minMarks)/numberOfBins;
        double bins[] = new double[numberOfBins];
        for (int i = 0; i < numberOfBins; i++) {
            bins[i]= (double)minMarks+binLimit*(i+1);
        }
        return getGrades(totalMarks,bins);
    }

    private static double findMax(double[] marks) {
        double max = 0;
        for(int i=0;i<marks.length;i++) {
            if(max<marks[i])
                max = marks[i];
        }
        return max;
    }

    private static double findMin(double[] marks, double maxMarks) {
        double min = maxMarks;
        for(int i=0;i<marks.length;i++) {
            if(min>marks[i])
                min = marks[i];
        }
        return min;
    }
}
