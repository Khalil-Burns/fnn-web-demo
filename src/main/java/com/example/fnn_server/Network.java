package com.example.fnn_server;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Network {
    String shades = "$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`\'.";
    String data = "Epoch Training Testing\n";
    int[] layerSizes;
    int size;
	Matrix input;
    Matrix[] allInputs;
    Matrix[] allTests;
    double[] allLabels;
	Matrix[] hidIn;
    Matrix[] hidOut;
	Matrix outIn;
    Matrix outOut;
    Matrix target;

    Matrix[] biases = new Matrix[2];
    Matrix[] adjustB; //adjustments that need to be subtracted from the biases. This will be calculated after the feed forward
    Matrix[] deltaB;
    Matrix[] weights = new Matrix[2];
    Matrix[] adjustW;
    Matrix[] deltaW;
    double[] correct = new double[1];

    double learnRate = 0;
    int epochs, miniBatchSize;
    String print = "";

	public Network(int[] sizes, double learningRate, int epoch, int miniBatch) {

        this.size = sizes.length;
        this.layerSizes = sizes;
		this.input = new Matrix(sizes[0], 1, false);
        this.hidIn = new Matrix[this.size - 2]; //size - (input and output layers)
        this.hidOut = new Matrix[this.size - 2];
        for (int i = 0; i < this.hidIn.length; i++) {
            this.hidIn[i] = new Matrix(sizes[i + 1], 1, false); //i + 1 because layer 0 is the input
            this.hidOut[i] = new Matrix(sizes[i + 1], 1, false);
        }
        this.outIn = new Matrix(sizes[this.size - 1], 1, false);
        this.outOut = new Matrix(sizes[this.size - 1], 1, false);
        this.target = new Matrix(sizes[this.size - 1], 1, false);

        //***IF TRAINING:***
        this.learnRate = learningRate;
        this.epochs = epoch;
        this.miniBatchSize = miniBatch;

        this.weights = new Matrix[this.size - 1]; //all but the input layer
        this.biases = new Matrix[this.size - 1];

        this.adjustW = new Matrix[this.size - 1];
        this.adjustB = new Matrix[this.size - 1];

        this.deltaW = new Matrix[this.size - 1];
        this.deltaB = new Matrix[this.size - 1];
        for (int i = 0; i < this.size - 1; i++) {
            this.weights[i] = new Matrix(sizes[i + 1], sizes[i], true); //width = number of nodes on previous layers, height = number of nodes on current layer (remember that I have switched width/x and height/y values)
            this.biases[i] = new Matrix(sizes[i + 1], 1, true);

            this.adjustW[i] = new Matrix(sizes[i + 1], sizes[i], true);
            this.adjustB[i] = new Matrix(sizes[i + 1], 1, true);

            this.deltaW[i] = new Matrix(sizes[i + 1], sizes[i], true);
            this.deltaB[i] = new Matrix(sizes[i + 1], 1, true);
        }

        //***IF ONLY TESTING:***
        this.loadWeights();
        this.loadBiases();
	}
    public void testWithPrintError() {
        //readImages(fileImg);
        //correct = readLabels(fileCor);
        //correct = this.allLabels;

        int numCorrect = 0, numCorrectFirst = 0;
            for (int loop = 0; loop < this.allTests.length; loop++) {
                this.input.values = this.allTests[loop].values;
                for (int hidden = 0; hidden < this.hidIn.length; hidden++) {
                    if (hidden == 0) {
                        this.hidIn[0] = this.weights[0].multiply(this.input);
                    }
                    else {
                        this.hidIn[hidden] = this.weights[hidden].multiply(this.hidOut[hidden - 1]);
                    }
                    this.hidIn[hidden]  = this.hidIn[hidden].add(this.biases[hidden]);
                    this.hidOut[hidden]  = this.hidIn[hidden].sigmoid();
                }

                this.outIn = this.weights[this.hidIn.length].multiply(this.hidOut[this.hidIn.length - 1]);
                this.outIn  = this.outIn.add(this.biases[this.hidIn.length]);
                this.outOut  = this.outIn.sigmoid();

                int maxIdx = 0;
                double maxN = 0, cost = 0, actual = 0;;
                for (int i = 0; i < 10; i++) {
                    if (i == this.allLabels[loop]) {
                        actual = 1.0;
                    }
                    else {
                        actual = 0;
                    }
                    this.target.values[i][0] = actual;
                    cost += square(this.outOut.values[i][0] - actual);
                    if (this.outOut.values[i][0] > maxN) {
                        maxN = this.outOut.values[i][0];
                        maxIdx = i;
                    }
                    double z = this.outIn.values[i][0];
                    //System.out.print(i + ": "+ (Math.round(this.outOut.values[i][0] * 10.0) / 10.0) + " ");
                }
                if (maxIdx == (int)this.allLabels[loop]) {
                    numCorrect++;
                }
                else {
                    print += (Math.round(this.outOut.values[0][0] * 10.0) / 10.0) + " " + (Math.round(this.outOut.values[1][0] * 10.0) / 10.0) + " " + (Math.round(this.outOut.values[2][0] * 10.0) / 10.0) + " " + (Math.round(this.outOut.values[3][0] * 10.0) / 10.0) + " " + (Math.round(this.outOut.values[4][0] * 10.0) / 10.0) + " " + (Math.round(this.outOut.values[5][0] * 10.0) / 10.0) + " " + (Math.round(this.outOut.values[6][0] * 10.0) / 10.0) + " " + (Math.round(this.outOut.values[7][0] * 10.0) / 10.0) + " " + (Math.round(this.outOut.values[8][0] * 10.0) / 10.0) + " " + (Math.round(this.outOut.values[9][0] * 10.0) / 10.0) + "\n";
                    print += "Guessed: " + maxIdx + ", Correct: " + (int)this.allLabels[loop] + "\n";

                    for (int j = 0; j < 28; j++) {
                        for (int k = 0; k < 28; k++) {
                            double x;
                            x = (1.0 - (this.allTests[loop].values[j * 28 + k][0]));
                            print += "" + this.shades.charAt((int)(x * (this.shades.length() - 1))) + "" + this.shades.charAt((int)(x * (this.shades.length() - 1)));
                        }
                        print += "\n";
                    }
                    print += "\n \n \n \n \n";
                }
                //System.out.println(maxIdx + " " + correct[loop] + ",   Correct: " + numCorrect + "/" + loop + " = " + (numCorrect / (double)loop) + ",   epoch: " + epoch);
            }
            System.out.println("" + (numCorrect / (double)(this.allTests.length / 100.0)));

            try {
                File f = new File("errors.txt");
                PrintWriter printer = new PrintWriter(f);

                printer.print(this.print);

                printer.close();
            }
            catch (IOException e) {}
        //System.out.println(maxIdx);
    }
    public void test() {
        int numCorrect = 0, numCorrectFirst = 0;
            for (int loop = 0; loop < this.allTests.length; loop++) {
                this.input.values = this.allTests[loop].values;
                for (int hidden = 0; hidden < this.hidIn.length; hidden++) {
                    if (hidden == 0) {
                        this.hidIn[0] = this.weights[0].multiply(this.input);
                    }
                    else {
                        this.hidIn[hidden] = this.weights[hidden].multiply(this.hidOut[hidden - 1]);
                    }
                    this.hidIn[hidden]  = this.hidIn[hidden].add(this.biases[hidden]);
                    this.hidOut[hidden]  = this.hidIn[hidden].sigmoid();
                }

                this.outIn = this.weights[this.hidIn.length].multiply(this.hidOut[this.hidIn.length - 1]);
                this.outIn  = this.outIn.add(this.biases[this.hidIn.length]);
                this.outOut  = this.outIn.sigmoid();

                int maxIdx = 0;
                double maxN = 0, cost = 0, actual = 0;;
                for (int i = 0; i < 10; i++) {
                    if (i == this.allLabels[loop]) {
                        actual = 1.0;
                    }
                    else {
                        actual = 0;
                    }
                    this.target.values[i][0] = actual;
                    cost += square(this.outOut.values[i][0] - actual);
                    if (this.outOut.values[i][0] > maxN) {
                        maxN = this.outOut.values[i][0];
                        maxIdx = i;
                    }
                    double z = this.outIn.values[i][0];
                    //System.out.print(i + ": "+ (Math.round(this.outOut.values[i][0] * 10.0) / 10.0) + " ");
                }
                if (maxIdx == (int)this.allLabels[loop]) {
                    numCorrect++;
                }
                //System.out.println(maxIdx + " " + correct[loop] + ",   Correct: " + numCorrect + "/" + loop + " = " + (numCorrect / (double)loop) + ",   epoch: " + epoch);
            }
            System.out.println("" + (numCorrect / (double)(this.allTests.length / 100.0)));
            data += "" + (numCorrect / (double)(this.allTests.length / 100.0)) + "\n";
        //System.out.println(maxIdx);
    }

    public void train(String fileImg, String fileCor) {
        this.allInputs = readImages(fileImg);
        correct = readLabels(fileCor);
        for (int epoch = 0; epoch < this.epochs; epoch++) {
            int numCorrect = 0;
            double totalCost = 0;
            shuffleAllInputs();
            for (int loop = 0; loop < (this.allInputs.length); loop += this.miniBatchSize) {
                double cost = 0;
                Matrix error = new Matrix(this.layerSizes[this.size - 1], 1, false);
                for (int curBatch = 0; curBatch < this.miniBatchSize; curBatch++) {
                    this.input.values = this.allInputs[loop + curBatch].values;
                    for (int hidden = 0; hidden < this.hidIn.length; hidden++) {
                        if (hidden == 0) {
                            this.hidIn[hidden] = this.weights[hidden].multiply(this.input);
                        }
                        else {
                            this.hidIn[hidden] = this.weights[hidden].multiply(this.hidOut[hidden - 1]);
                        }
                        this.hidIn[hidden]  = this.hidIn[hidden].add(this.biases[hidden]);
                        this.hidOut[hidden]  = this.hidIn[hidden].sigmoid();
                    }
                    //System.out.println(this.hidIn[0].values[0][0] + " " + this.hidIn[0].values[1][0]);
                    //System.out.println(this.hidOut[0].values[0][0] + " " + this.hidOut[0].values[1][0]);
                    this.outIn = this.weights[this.hidIn.length].multiply(this.hidOut[this.hidIn.length - 1]);
                    this.outIn  = this.outIn.add(this.biases[this.hidIn.length]);
                    this.outOut  = this.outIn.sigmoid();
                    //System.out.println(this.outOut.values[0][0] + " " + this.outOut.values[1][0]);

                    int maxIdx = 0;
                    double maxN = 0, actual = 0;
                    cost = 0.0;
                    //this.target.values[0][0] = 0.01;
                    //this.target.values[1][0] = 0.99;
                    for (int i = 0; i < this.outOut.values.length; i++) {
                        if (curBatch == 0) {
                            error.values[i][0] = 0;
                        }
                        if (i == correct[loop + curBatch]) {
                            actual = 1.0;
                        }
                        else {
                            actual = 0;
                        }
                        this.target.values[i][0] = actual;
                        cost += square(this.outOut.values[i][0] - actual);
                        if (this.outOut.values[i][0] > maxN) {
                            maxN = this.outOut.values[i][0];
                            maxIdx = i;
                        }

                        error.values[i][0] = this.outOut.values[i][0] - this.target.values[i][0];
                    }
                    totalCost += cost;
                    backProp(this.size - 1, error);
                    if (curBatch == 0) {
                        for (int i = 0; i < this.size - 1; i++) {
                            this.adjustW[i] = new Matrix(this.deltaW[i]);
                            this.adjustB[i] = new Matrix(this.deltaB[i]);
                        }
                    }
                    else {
                        for (int i = 0; i < this.size - 1; i++) {
                            this.adjustW[i] = this.adjustW[i].add(this.deltaW[i]);
                            this.adjustB[i] = this.adjustB[i].add(this.deltaB[i]);
                        }
                    }
                    if (maxIdx == (int)correct[loop + curBatch]) {
                        numCorrect++;
                    }
                }
                
                for (int i = 0; i < this.size - 1; i++) {
                    this.weights[i] = this.weights[i].subWithLRate(this.adjustW[i], this);
                    this.biases[i] = this.biases[i].subWithLRate(this.adjustB[i], this);
                }
            }
            totalCost /= this.allInputs.length;
            System.out.print("Epoch: " + epoch + ", Training: " + (numCorrect / (double)(this.allInputs.length / 100.0)) + ", Testing: ");
            data += "" + epoch + " " + (numCorrect / (double)(this.allInputs.length / 100.0)) + " ";
            test();
            // writeWeights(Math.min(epoch + 1, 3), (numCorrect / (double)this.allInputs.length));
            // writeBiases(Math.min(epoch + 1, 3), (numCorrect/ (double)this.allInputs.length));
        }
    }

    public void backProp(int layer, Matrix error) {
        if (layer <= 0) { //layer 0 is input layer
            return;
        }
        if (layer == this.size - 1) {
            for (int i = 0; i < biases[layer - 1].values.length; i++) {
                this.deltaB[layer - 1].values[i][0] = error.values[i][0] * dSigmoid(this.outIn.values[i][0])/* * this.learnRate*/;
                for (int j = 0; j < this.weights[layer - 1].values[i].length; j++) {
                    this.deltaW[layer - 1].values[i][j] = this.deltaB[layer - 1].values[i][0] * this.hidOut[layer - 2].values[j][0];
                }
            }
        }
        else {
            for (int i = 0; i < this.biases[layer - 1].values.length; i++) {
                double sum = 0;
                for (int j = 0; j < this.deltaB[layer].values.length; j++) {
                    sum += this.deltaB[layer].values[j][0] * this.weights[layer].values[j][i];
                }
                this.deltaB[layer - 1].values[i][0] = sum * dSigmoid(hidIn[layer - 1].values[i][0]);
                for (int j = 0; j < this.weights[layer - 1].values[i].length; j++) {
                    if (layer == 1) {
                        this.deltaW[layer - 1].values[i][j] = this.deltaB[layer - 1].values[i][0] * this.input.values[j][0];
                    }
                    else {
                        this.deltaW[layer - 1].values[i][j] = this.deltaB[layer - 1].values[i][0] * this.hidOut[layer - 2].values[j][0];
                    }
                }
            }
        }
        backProp(layer - 1, error);
    }

    public static double randn() {
        Random random = new Random();
        return(random.nextGaussian() / 28.0);
    }
    public static double sigmoid(double x) {
        return(1.0 / (1.0 + Math.exp(-x)));
    }
    public static double dSigmoid(double x) {
        double d = sigmoid(x);
        return(d * (1.0 - d));
    }
    public static double relu(double x) {
        return(Math.max(0, x));
    }
    public static double percep(double x) {
        return(x<0? 0:1);
    }
    public static double dRelu(double x) {
        return(x<0? 0:1);
    }
    public static double square(double x) {
        return(x * x);
    }
    public double applyLRate(double x) {
        return(x * this.learnRate);
    }
    public void shuffleAllInputs() {
        int index;
        Random rnd = new Random();
        for (int i = this.allInputs.length - 1; i > 0; i--) {
            index = rnd.nextInt(i + 1);
            if (index != i) {
                Matrix temp;
                temp = this.allInputs[index];
                this.allInputs[index] = this.allInputs[i];
                this.allInputs[i] = temp;
                double temp2;
                temp2 = this.correct[index];
                this.correct[index] = this.correct[i];
                this.correct[i] = temp2;
            }
        }
    }
    public Matrix[] readImages(String file) {
        Matrix[] data;
        try{
            FileInputStream in = new FileInputStream(file);

            int magicNumber = (in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read();
            int numOfImages = (in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read();
            int rows = (in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read();
            int cols = (in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read();
            data = new Matrix[numOfImages];
            byte[] vals = new byte[rows * cols * numOfImages];
            in.read(vals);
            for (int i = 0; i < data.length; i++) {
                data[i] = new Matrix(rows * cols, 1, true);
                for (int j = 0; j < rows; j++) {
                    for (int k = 0; k < cols; k++) {
                        data[i].values[j * cols + k][0] = ((int)(vals[(i * 784) + (j * cols + k)] & 0xFF) / 255.0);
                    }
                }
            }
            in.close();
            return(data);
        }
        catch (Exception e) {System.out.println(e); return(null);}
    }
    public double[] readLabels(String file) {
        try{
            double[] labels;
            FileInputStream in = new FileInputStream(file);
            int magicNumber = (in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read();
            int numOfLabels = (in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read();
            labels = new double[numOfLabels];
            for (int i = 0; i < numOfLabels; i++) {
                double value = in.read();
                labels[i] = value;
            }
            in.close();
            return(labels);
        }
        catch (Exception e) {
            System.out.println(e);
            return(null);
        }
    }
    public String num(double[][] newInput) {
        this.input.values = newInput;
        for (int hidden = 0; hidden < this.hidIn.length; hidden++) {
            if (hidden == 0) {
                this.hidIn[0] = this.weights[0].multiply(this.input);
            }
            else {
                this.hidIn[hidden] = this.weights[hidden].multiply(this.hidOut[hidden - 1]);
            }
            this.hidIn[hidden]  = this.hidIn[hidden].add(this.biases[hidden]);
            this.hidOut[hidden]  = this.hidIn[hidden].sigmoid();
        }

        this.outIn = this.weights[this.hidIn.length].multiply(this.hidOut[this.hidIn.length - 1]);
        this.outIn  = this.outIn.add(this.biases[this.hidIn.length]);
        this.outOut  = this.outIn.sigmoid();

        int maxIdx = 0;
        double maxN = 0;
        String output = "{";
        for (int i = 0; i < 10; i++) {
            output += "'" + i + "': '" + this.outOut.values[i][0] + "', ";
            if (this.outOut.values[i][0] > maxN) {
                maxN = this.outOut.values[i][0];
                maxIdx = i;
            }
        }
        output += "'MAX': '" + maxIdx + "'}";
        // System.out.println(output);
        // System.out.println(maxIdx);
        return(output);
    }
    public void readImg(String file) {
        BufferedImage img;
        try {
            img = ImageIO.read(new File("sample6.png"));
            double avgC = 0;
            Color c;
            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    c = new Color(img.getRGB(j, i));
                    avgC = c.getRed() + c.getGreen() + c.getBlue();
                    avgC /= 3.0;
                    avgC /= 255.0;
                    this.input.values[i * img.getWidth() + j][0] = avgC;
                }
            }
        }
        catch(IOException e) {}
    }
    public void writeWeights(int epoch, double accuracy) {
        try {
            File weights = new File(("weights" + epoch + ".txt"));
            PrintWriter out = new PrintWriter(weights);
            out.println(accuracy + "");
            for (int i = 0; i < this.weights.length; i++) {
                out.println(this.weights[i].toString());
            }
            out.close();
        }
        catch (IOException e){}
    }
    public void writeBiases(int epoch, double accuracy) {
        try {
            File biases = new File(("biases" + epoch + ".txt"));
            PrintWriter out = new PrintWriter(biases);
            out.println(accuracy + "");
            for (int i = 0; i < this.biases.length; i++) {
                out.println(this.biases[i].toString());
            }
            out.close();
        }
        catch (IOException e){}
    }

    public void train(String fileImg, double correct) {
        readImg(fileImg);
        int numCorrect = 0, numCorrectFirst = 0;
        for (int loop2 = 0; loop2 < 100; loop2++) {
            for (int hidden = 0; hidden < this.hidIn.length; hidden++) {
                if (hidden == 0) {
                    this.hidIn[0] = this.weights[0].multiply(this.input);
                }
                else {
                    this.hidIn[hidden] = this.weights[hidden].multiply(this.hidOut[hidden - 1]);
                }
                this.hidIn[hidden]  = this.hidIn[hidden].add(this.biases[hidden]);
                this.hidOut[hidden]  = this.hidIn[hidden].sigmoid();
            }
            this.outIn = this.weights[this.hidIn.length].multiply(this.hidOut[this.hidIn.length - 1]);
            this.outIn  = this.outIn.add(this.biases[this.hidIn.length]);
            this.outOut  = this.outIn.sigmoid();

            int maxIdx = 0;
            double maxN = 0, cost = 0, actual = 0;;
            for (int i = 0; i < 10; i++) {
                if (i == correct) {
                    actual = 1.0;
                }
                else {
                    actual = 0;
                }
                this.target.values[i][0] = actual;
                cost += square(this.outOut.values[i][0] - actual);
                if (this.outOut.values[i][0] > maxN) {
                    maxN = this.outOut.values[i][0];
                    maxIdx = i;
                }
                System.out.print(i + ": "+ (Math.round(this.outOut.values[i][0] * 1000.0) / 1000.0) + " ");
            }
            System.out.println("  Guessed: " + maxIdx + ", correct: "+ correct); 
            System.out.print((Math.round(cost * 100000.0) / 100000.0) + "   ");
            backProp(this.size - 1, this.outOut.subtract(this.target));
            for (int i = 0; i < this.size - 1; i++) {
                this.weights[i] = this.weights[i].subtract(this.adjustW[i]);
                this.biases[i] = this.biases[i].subtract(this.adjustB[i]);
            }
        }
    }

    public void loadWeights() {
        try {
            this.weights[0] = new Matrix(readDoubleArrayFromFile("src/main/java/com/example/fnn_server/savedWeights0.txt"));
            this.weights[1] = new Matrix(readDoubleArrayFromFile("src/main/java/com/example/fnn_server/savedWeights1.txt"));
        }
        catch (Exception e) {
            System.out.println("Error Loading Weights");
            e.printStackTrace();
        }
    }
    public void loadBiases() {
        try {
            this.biases[0] = new Matrix(readDoubleArrayFromFile("src/main/java/com/example/fnn_server/savedBiases0.txt"));
            this.biases[1] = new Matrix(readDoubleArrayFromFile("src/main/java/com/example/fnn_server/savedBiases1.txt"));
        }
        catch (Exception e) {
            System.out.println("Error Loading Biases");
            e.printStackTrace();
        }
    }

    public static double[][] readDoubleArrayFromFile(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        br.close();
        String fileContent = sb.toString().trim();

        // Remove outer curly braces
        fileContent = fileContent.substring(1, fileContent.length() - 1);

        // Split by "},{" to get each row
        String[] rows = fileContent.split("\\},\\{");

        ArrayList<double[]> arrayList = new ArrayList<>();

        for (String row : rows) {
            // Remove any remaining curly braces and split by comma to get each element
            row = row.replaceAll("\\{|\\}", "").trim();
            String[] elements = row.split(",");

            double[] doubleRow = new double[elements.length];
            for (int i = 0; i < elements.length; i++) {
                doubleRow[i] = Double.parseDouble(elements[i].trim());
            }
            arrayList.add(doubleRow);
        }

        double[][] resultArray = new double[arrayList.size()][];
        for (int i = 0; i < arrayList.size(); i++) {
            resultArray[i] = arrayList.get(i);
        }

        return resultArray;
    }
    public void setInputFromFile() {
        try {
            // Step 1: Read file content
            String content = new String(Files.readAllBytes(Paths.get("NeuralNetwork/testImageData.txt")));

            // Step 2: Remove brackets and split by comma
            content = content.replace("[", "").replace("]", "").trim();
            String[] strValues = content.split(",");

            // Convert string values to doubles
            List<Double> doubleList = new ArrayList<>();
            for (int i = 0; i < this.input.values.length; i++) {
                this.input.values[i][0] = Double.parseDouble(strValues[i].trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}