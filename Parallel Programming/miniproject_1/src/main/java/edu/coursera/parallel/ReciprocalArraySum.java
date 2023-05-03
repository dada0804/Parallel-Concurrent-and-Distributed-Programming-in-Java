package edu.coursera.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;


/**
 * Class wrapping methods for implementing reciprocal array sum in parallel.
 */
public final class ReciprocalArraySum {

    /**
     * Default constructor.
     */
    private ReciprocalArraySum() {
    }

    /**
     * Sequentially compute the sum of the reciprocal values for a given array.
     *
     * @param input Input array
     * @return The sum of the reciprocals of the array input
     */
    protected static double seqArraySum(final double[] input) {
        double sum = 0;

        // Compute sum of reciprocals of array elements
        for (int i = 0; i < input.length; i++) {
            sum += 1 / input[i];
        }

        return sum;
    }

    /**
     * Computes the size of each chunk, given the number of chunks to create
     * across a given number of elements.
     *
     * @param nChunks The number of chunks to create
     * @param nElements The number of elements to chunk across
     * @return The default chunk size
     */
    private static int getChunkSize(final int nChunks, final int nElements) {
        // Integer ceil
        return (nElements + nChunks - 1) / nChunks;
    }

    /**
     * Computes the inclusive element index that the provided chunk starts at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the start of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The inclusive index that this chunk starts at in the set of
     *         nElements
     */
    private static int getChunkStartInclusive(final int chunk,
            final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

    /**
     * Computes the exclusive element index that the provided chunk ends at,
     * given there are a certain number of chunks.
     *
     * @param chunk The chunk to compute the end of
     * @param nChunks The number of chunks created
     * @param nElements The number of elements to chunk across
     * @return The exclusive end index for this chunk
     */
    private static int getChunkEndExclusive(final int chunk, final int nChunks,
            final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }

    /**
     * This class stub can be filled in to implement the body of each task
     * created to perform reciprocal array sum in parallel.
     */
    private static class ReciprocalArraySumTask extends RecursiveAction {
        /**
         * Starting index for traversal done by this task.
         */
        private final int startIndexInclusive;
        /**
         * Ending index for traversal done by this task.
         */
        private final int endIndexExclusive;
        /**
         * Input array to reciprocal sum.
         */
        private final double[] input;
        /**
         * Intermediate value produced by this task.
         */
        private double value;

        private int numTasks;

        /**
         * Constructor.
         * @param setStartIndexInclusive Set the starting index to begin
         *        parallel traversal at.
         * @param setEndIndexExclusive Set ending index for parallel traversal.
         * @param setInput Input values
         */
        ReciprocalArraySumTask(final int setStartIndexInclusive,
                final int setEndIndexExclusive, final double[] setInput, int numTasks) {
            this.startIndexInclusive = setStartIndexInclusive;
            this.endIndexExclusive = setEndIndexExclusive;
            this.input = setInput;
            this.numTasks = numTasks;
        }

        /**
         * Getter for the value produced by this task.
         * @return Value produced by this task
         */
        public double getValue() {
            return value;
        }

        @Override
        protected void compute() {
            if(numTasks == 0){
                for (int i = startIndexInclusive; i < endIndexExclusive; i++) {
                    value += 1 / input[i];
                }
            } else {
                List<ReciprocalArraySumTask> tasks = new ArrayList<>();
                for (int i = 0; i < numTasks; i++){
                    int startIndex = getChunkStartInclusive(i,numTasks,input.length);
                    int endIndex = getChunkEndExclusive(i,numTasks, input.length);
                    ReciprocalArraySumTask subTask = new ReciprocalArraySumTask(startIndex,endIndex,input,0);
                    tasks.add(subTask);
                }

                for (ReciprocalArraySumTask task : tasks){
                    task.fork();
                }

                for (ReciprocalArraySumTask task: tasks){
                    task.join();
                    value += task.value;
                }

//                ReciprocalArraySumTask  left = new ReciprocalArraySumTask(startIndexInclusive,(startIndexInclusive+endIndexExclusive)/2, input,numTasks);
//                ReciprocalArraySumTask right = new ReciprocalArraySumTask((startIndexInclusive+endIndexExclusive)/2, endIndexExclusive,input,numTasks);
//                left.fork();
//                right.compute();
//                left.join();
//                value = left.value + right.value;
            }

        }
    }



    protected static double parArraySum(final double[] input) {
        assert input.length % 2 == 0; // The assert statement takes a boolean expression as its argument and if the expression evaluates to false, an AssertionError is thrown.
//        double sum = 0;
//        ForkJoinPool pool = new ForkJoinPool(2);
//        ReciprocalArraySumTask inputArray = new ReciprocalArraySumTask(0,input.length, input, 2);
//        pool.invoke(inputArray);
//        sum = inputArray.getValue();
//        return sum;
        return parManyTaskArraySum(input, 2);
    }

    /**
     * TODO: Extend the work you did to implement parArraySum to use a set
     * number of tasks to compute the reciprocal array sum. You may find the
     * above utilities getChunkStartInclusive and getChunkEndExclusive helpful
     * in computing the range of element indices that belong to each chunk.
     *
     * @param input Input array
     * @param numTasks The number of tasks to create
     * @return The sum of the reciprocals of the array input
     */
    protected static double parManyTaskArraySum(final double[] input,
            final int numTasks) {
        double sum = 0;

        // Compute sum of reciprocals of array elements
        ForkJoinPool pool = new ForkJoinPool(numTasks);
        ReciprocalArraySumTask inputArray = new ReciprocalArraySumTask(0,input.length, input, numTasks);
        pool.invoke(inputArray);
        sum = inputArray.getValue();
        return sum;
    }
}
