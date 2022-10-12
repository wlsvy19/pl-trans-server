package com.eBrother.trans.image.model;

import com.eBrother.trans.image.util.JnaMemory;

public class CacheJnaItem {

    public String getTransYmd() {
        return transYmd;
    }

    public void setTransYmd(String transYmd) {
        this.transYmd = transYmd;
    }

    String transYmd;
    String dataFile;

    JnaMemory bufPoint;

    int sizeCol;

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    long lastModified;

    public int getSizeCol() {
        return sizeCol;
    }

    public void setSizeCol(int sizeCol) {
        this.sizeCol = sizeCol;
    }

    public int getNumRow() {
        return numRow;
    }

    public void setNumRow(int numRow) {
        this.numRow = numRow;
    }

    int numRow;

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    public JnaMemory getBufPoint() {
        return bufPoint;
    }

    public void setBufPoint(JnaMemory bufPoint) {
        this.bufPoint = bufPoint;
    }

    public TransType getTransMainType () {
        return mainType;
    }

    public void setTransMainType(TransType type) {
        this.mainType = type;
    }

    TransType mainType;

    public TransType getTransSubType () {
        return transType;
    }
    public void setTransSubType(TransType type) {
        this.transType = type;
    }
    TransType transType;

}
