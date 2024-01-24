package com.desabisc.examplea;

public class ReadInventoryThread extends Thread {
    @Override
    public void run() { // overrides method in Thread
        System.out.println("Printing zoo inventory");
    }

    public static void main(String[] args) {
        (new ReadInventoryThread()).start();
    }
}
