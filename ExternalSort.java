import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.lang.Math;

public class ExternalSort
{

	// mergeSort and merge will be used to sort the first runs, when the input is first read in
	// Only used for the first runs
	private static void mergeSort(Integer a[], Integer tmpArray[], int left, int right )
	{
		if( left < right )
		{
			int center = ( left + right ) / 2;
	      	mergeSort( a, tmpArray, left, center );
	        mergeSort( a, tmpArray, center + 1, right );
	        merge( a, tmpArray, left, center + 1, right );
		}
	}
	private static void merge(Integer a[], Integer tmpArray[], int leftPos, int rightPos, int rightEnd )
	{
		int leftEnd = rightPos - 1;
		int tmpPos = leftPos;
		int numElements = rightEnd - leftPos + 1;
		while( leftPos <= leftEnd && rightPos <= rightEnd )   // for each half
			if(a[ leftPos ] <= a[ rightPos ])     //  take lesser
                 tmpArray[ tmpPos++ ] = a[ leftPos++ ];          //  element next
			else
				tmpArray[ tmpPos++ ] = a[ rightPos++ ];
		while( leftPos <= leftEnd )    // Copy rest of first half
			tmpArray[ tmpPos++ ] = a[ leftPos++ ];
		while( rightPos <= rightEnd )  // Copy rest of right half
             tmpArray[ tmpPos++ ] = a[ rightPos++ ];
		// Copy tmpArray back to original vector a
		for( int i = 0; i < numElements; i++, rightEnd-- )
             a[ rightEnd ] = tmpArray[ rightEnd ];  // rightEnd already at end
	}
	public static void mergeSort(Integer a[])
	{
		Integer tmpArray[] = new Integer[a.length];
		mergeSort(a,tmpArray,0,a.length-1);
	}
	// For first run to obtain data from sorted array, only used for the first runs
	public static String arrayData(Integer arr[], int runsize)
	{
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < runsize; i++)
		{
			sb.append(arr[i] + " ");
		}
		return sb.toString();
	}
	// The first pass will create the initial runs and sort them
	public static int firstPass(Path t1, Path t3, Path t4, int runsize)
	{
		Integer arr[] = new Integer[runsize];
		int numItems = 0; // Counter for the total size of the input data
		try
		{
			Scanner input = new Scanner(t1); // Read in data from input file, T1
			Path usePath; // path to be used between for alternating
			// scanner delimiter uses commas, spaces, and new lines depending on the way the input file is set up
			if(!input.hasNext())
			{
				throw new NoSuchElementException("There is no data in the file");
			}
			input.useDelimiter("[, \n]");
			int j = 0; // Used to index array
			int alternate = 0; // will be used to alternate between two files for writing runs
			emptyFilesForWrite(t3,t4); // Empty the files that will be written to
			while(input.hasNextInt())
			{
				numItems++;
				arr[j] = input.nextInt(); // Store data into array
				if(alternate % 2 == 0) // alternate between t3 and t4 for each run for each loop iteration. maybe fix?
					usePath = t3;
				else
					usePath = t4;
				if(j == runsize - 1) // when the array has the amount required for a run
				{
					// sort the array, then write the run to the file
					mergeSort(arr);
					writeToFile(arrayData(arr,runsize), usePath);
					alternate++;
					j = 0; // reset counter so array index can start at 0 again
					continue; // to avoid incrementing j
				}
				j++;
			}
			// Handling the extra data that didn't fit into a run
			if(numItems % runsize != 0)
			{
				Integer temp[] = new Integer[numItems % runsize]; // Create an array for the size of the leftover data
				for(int i = 0; i < numItems % runsize; i++)
				{
					// the arr array will have the leftover elements, but since it's size as greater than what is necessary
					// the arr array has null elements so it wouldn't work with the merge sort, but having the temp
					// array at the exact size for the leftover elements would have the merge sort work properly
					temp[i] = arr[i];
				}
				mergeSort(temp);
				if(alternate % 2 == 0)
					usePath = t3;
				else
					usePath = t4;
				writeToFile(arrayData(temp,numItems%runsize), usePath); // write sorted extra data to file
			}
			input.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
		return numItems;
	}
	public static boolean checkPowOf2(int num)
	{
		if(num < 1)
			return false;
		for(int i = num; i != 1; i /= 2)
			if(i % 2 != 0)
				return false;
		return true;
	}
	public static int calcPasses(int runsize, int numItems)
	{
		int calcPasses = 0; // calculating the number of passes to determine loop iterations
		if(numItems % runsize == 0) // for handling of Java integer division because of rounding down
			calcPasses = numItems / runsize;
		else
			calcPasses = (numItems/runsize) + 1;
		boolean result = checkPowOf2(calcPasses); // check if number is a power of 2, to handle rounding down when doing
		// log because of Java integer division
		calcPasses = (int) (Math.log(calcPasses) / Math.log(2) + 1e-10);
		if(!result)
			calcPasses += 1;
		return calcPasses;
	}
	public static Path processRuns(int runsize, Path read1, Path read2, Path write1, Path write2, int iteration)
	{
		int result = (int) Math.pow(2, iteration); // used to know size of runs after merges
		Path usePath = null; // path used for writing and then for storing path of sorted file
		try
		{
			// Two scanner from input from both files
			Scanner input1 = new Scanner(read1);
			Scanner input2 = new Scanner(read2);
			// Numbers read in from Scanners input1 and input2
			int num1 = 0;
			int num2 = 0;

			// scanner delimiter uses commas, spaces, and new lines depending on the way the input file is set up
			input1.useDelimiter("[, \n]");
			input2.useDelimiter("[, \n]");

			int alternate = 1; // used for alternating which output file gets written to
			emptyFilesForWrite(write1,write2); // empty the output files
			boolean minFromInput1 = false; // to determine where the minimum number in the runs comes from the first file
			// or the second file

 			while(input1.hasNextInt() && input2.hasNextInt())
			{
 				// outer loop handles each merge necessary for a pass and the inner loop handles the merge of two runs
				int readsInput1 = 0; // number of values read in from the first file
				int readsInput2 = 0; // number of values read in from the second file
				int max = 0; // the max of the two runs, used to determine which file to use for input
				// get the first number from each run
				num1 = input1.nextInt();
				num2 = input2.nextInt();
				readsInput1++;
				readsInput2++;
				alternate++;
				if(alternate % 2 == 0)
					usePath = write1;
				else
					usePath = write2;
				if(num1 < num2) // max is num2
				{
					minFromInput1 = true;
					max = num2;
				    writeToFile(String.valueOf(num1) + " ", usePath); // write num1 to the output file
				}
				else // max is num1
				{
					minFromInput1 = false;
					max = num1;
				    writeToFile(String.valueOf(num2) + " ", usePath); // write num2 to the output file
				}
				// When minFromInput1 is true, the data will be read in from the first input file, if minFromInput1 is
				// false then the data will be read from the second input file, the input file with the min that doesn't
				// change the max is the one that is read in
				while(true)
				{
					// Loop will run until a merge is complete for two runs
					// This loop processes two runs at a time
					 if(minFromInput1) // read in data from input1 scanner
					 {
						 // When there is no more data to read from input1, get the rest of the data from input2 and
						 // write it to the file
						 if(readsInput1 >= runsize * result || !input1.hasNextInt())
						 {
							 writeToFile(String.valueOf(max) + " ", usePath);
							 for(int i = readsInput2; i < runsize * result && input2.hasNextInt(); i++)
							 {
								 // get remaining data up until the size of the new run
								 // will only get data to complete a run, will not get leftover data that
								 // couldn't be merged in a run
								 int tempNum = input2.nextInt();
								 writeToFile(String.valueOf(tempNum) + " ", usePath);
							 }
							 break; // The merging of two runs is complete, now move on to different runs
						 }
						 num1 = input1.nextInt(); // get number
						 readsInput1++;
						 if(num1 > max) // Input from the first file found a greater max
						 {
							 int temp = max; // old max
							 max = num1; // new max
							 writeToFile(String.valueOf(temp) + " ", usePath); // write old max to file
							 minFromInput1 = false; // now start reading in from the input2 scanner
						 }
						 else // Numbers read in are less than the current max, so write numbers to file
						 {
							 writeToFile(String.valueOf(num1) + " ", usePath);
						 }
					 }
					 if(!minFromInput1) // read in data from input2 scanner
					 {
						 // When there is no more data to read from input2, get the rest of the data from input2 and
						 // write it to the file
						 if(readsInput2 >= runsize * result || !input2.hasNextInt())
						 {
							 writeToFile(String.valueOf(max) + " ", usePath);
							 for(int i = readsInput1; i < runsize * result && input1.hasNextInt(); i++)
							 {
								 // get remaining data up until the size of the new run
								 // will only get data to complete a run, will not get leftover data that
								 // couldn't be merged in a run
								 int tempNum = input1.nextInt();
								 writeToFile(String.valueOf(tempNum) + " ", usePath);
							 }
							 break; // The merging of two runs is complete, now move on to different runs
						 }
						 num2 = input2.nextInt(); // get number
						 readsInput2++;
						 if(num2 > max) // Input from the second file found a greater max
						 {
							 int temp = max; // old max
							 max = num2; // new max
							 writeToFile(String.valueOf(temp) + " ", usePath); // write old max to file
							 minFromInput1 = true; // now start reading in from input1 scanner
						 }
						 else // Numbers read in are less than the current max, so write numbers to file
						 {
							 writeToFile(String.valueOf(num2) + " ", usePath);
						 }

					 }
				}
			}
 			// Check which file to write to
 			// Leftover data that can't be merged in a run yet is retrieved and written to the output file
 			if(input1.hasNext() || input2.hasNext())
 			{
 				alternate++;
 	 			if(alternate % 2 == 0)
 	 				usePath = write1;
 	 			else
 	 				usePath = write2;
 			}
 			while(input1.hasNextInt())
 				writeToFile(String.valueOf(input1.nextInt()) + " ", usePath);
 			while(input2.hasNextInt())
 				writeToFile(String.valueOf(input2.nextInt()) + " ", usePath);
			input1.close();
			input2.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
		return usePath; // return the path of the completed pass
	}
	public static void emptyFilesForWrite(Path p1, Path p2)
	{
		try
		{
			// Empty the output files to ensure there is no extra data
			Files.writeString(p1, "", StandardOpenOption.TRUNCATE_EXISTING);
			Files.writeString(p2, "", StandardOpenOption.TRUNCATE_EXISTING);
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}
	public static void writeToFile(String writeData, Path write)
	{
		// general function for writing a file, based on the path passed in
		try
		{
			Files.writeString(write, writeData, StandardOpenOption.APPEND);
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}
	public static boolean isValid(String num)
	{
		boolean valid = true;

		try
		{
			Integer.parseInt(num);
		}
		catch(NumberFormatException e)
		{
			System.out.println(e + " You didn't provide valid input, so a default value for the runsize was stored\n");
			valid = false;
		}
		return valid;
	}
	// The external sort can handle any text file with integers that are seperated with commas, spaces, and newlines, but more delimeters can be
	// added to the scanner.useDelimiter, if the text file is organized in a different way.
	public static Path extSort(Path t1, int runsize)
	{
		// the paths of all of the files to be used as tapes
		Path t2Path = Paths.get("/Users/mohamadkhadra/Desktop/T2.txt");
		Path t3Path = Paths.get("/Users/mohamadkhadra/Desktop/T3.txt");
		Path t4Path = Paths.get("/Users/mohamadkhadra/Desktop/T4.txt");
		Path finalPath = null; // path to sorted file
		// check if files exist and create the files if they don't exist
		if(!Files.exists(t2Path))
		{
			try
			{
				Files.createFile(t2Path);
			} catch (IOException e)
			{
				System.out.println(e);
			}
		}
		if(!Files.exists(t3Path))
		{
			try
			{
				Files.createFile(t3Path);
			} catch (IOException e)
			{
				System.out.println(e);
			}
		}
		if(!Files.exists(t4Path))
		{
			try
			{
				Files.createFile(t4Path);
			} catch (IOException e)
			{
				System.out.println(e);
			}
		}

		int numItems = firstPass(t1,t3Path,t4Path,runsize); // do first pass and get the size of the input data
		int numPasses = calcPasses(runsize, numItems); // calculate the number of passes required for the sort

		if(numItems <= runsize) // When the data is less than the runsize return t3 since the sorted numbers will be
			// in the t3 file after the initial pass
			// since the first run goes in t3 during the first pass, since the number of items wasn't enough to be a run, it will go into t3
			return t3Path;
		for(int i = 0; i < numPasses; i++) // loop will run based on number of passes
		{
			// The files to write to will alternate between t1,t2 and t3,t4
			// The iteration of the loop will be sent to processRuns to help with knowing the size of runs
			// as the program progresses
			if(i % 2 == 0)
				finalPath = processRuns(runsize,t3Path,t4Path,t1,t2Path,i);
			else
				finalPath = processRuns(runsize,t1,t2Path,t3Path,t4Path,i);
		}

		return finalPath; // return the path obtained by calling the processRuns function

	}
	public static void main(String[] args)
	{
		Path t1PathSend = Paths.get("/Users/mohamadkhadra/Desktop/T1.txt");
		if(!Files.exists(t1PathSend))
			throw new RuntimeException("The file does not exist");
		// default value of 4 used when an argument is not supplied
		if(args.length == 0)
			System.out.println("The path to the sorted file is: " + extSort(t1PathSend, 4));
		else if(!isValid(args[0])) // Invalid input
			System.out.println("The path to the sorted file is: " + extSort(t1PathSend, 4));
		else // Valid input
		{
			Integer num = Integer.valueOf(args[0]); // get value of entered number
			if(num < 1) // Valid input, but invalid runsize
				System.out.println("The path to the sorted file is: " + extSort(t1PathSend, 4));
			else // valid input and valid runsize
				System.out.println("The path to the sorted file is: " + extSort(t1PathSend, num));
		}
	}

}
