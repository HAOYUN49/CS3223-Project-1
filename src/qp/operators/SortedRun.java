package qp.operators;

import qp.utils.Batch;
import qp.utils.Tuple;

import java.io.*;
import java.util.ArrayList;

class SortedRun {
    static int fn = 0;
    String filename;
    int batchsize;
    boolean eos;
    ObjectInputStream in;
    ObjectOutputStream out;

    public SortedRun(int batchsize) {
        fn++;
        filename = "SortedRun-" + fn;
        this.batchsize = batchsize;
        try {
            out = new ObjectOutputStream(new FileOutputStream((filename)));
        } catch (IOException e) {
            System.err.println("SortedRun: Error writing " + filename);
        }
    }

    public SortedRun(ArrayList<Tuple> tuples, int batchsize) {
        fn++;
        filename = "SortedRun-" + fn;
        this.batchsize = batchsize;
        try {
            out = new ObjectOutputStream(new FileOutputStream((filename)));
        } catch (IOException e) {
            System.err.println("SortedRun: Error writing " + filename);
        }
        Batch batch = new Batch(batchsize);
        for (Tuple tuple : tuples) {
            batch.add(tuple);
            if (batch.isFull()) {
                try {
                    out.writeObject(batch);
                } catch (IOException e) {
                    System.err.println("SortedRun: cannot write object");
                }
                batch = new Batch(batchsize);
            }
        }
        if (batch.size() > 0) {
            try {
                out.writeObject(batch);
            } catch (IOException e) {
                System.err.println("SortedRun: cannot write object");
            }
        }
    }

    public boolean add(Batch batch) {
        try {
            out.writeObject(batch);
        } catch (IOException e) {
            System.err.println("SortedRun: cannot write object");
            return false;
        }
        return true;
    }

    public Batch next() {
        /** The file reached its end and no more to read **/
        if (eos) {
            return null;
        }
        try {
            in = new ObjectInputStream(new FileInputStream(filename));
        } catch (IOException e) {
            System.err.println("SortedRun: Error reading " + filename);
        }
        Batch tuples = new Batch(batchsize);
        try {
            Batch data = (Batch) in.readObject();
        } catch (ClassNotFoundException cnf) {
            System.err.println("SortedRun:Class not found for reading file  " + filename);
            System.exit(1);
        } catch (EOFException EOF) {
            /** At this point incomplete page is sent and at next call it considered
             ** as end of file
             **/
            eos = true;
            return tuples;
        } catch (IOException e) {
            System.err.println("SortedRun:Error reading " + filename);
            System.exit(1);
        }
        return tuples;
    }

    public boolean close() {
        try {
            in.close();
            out.close();
        } catch (IOException io) {
            System.err.println("SortedRun: Error closing file");
            return false;
        }
        File file = new File(filename);
        return true;
    }
}
