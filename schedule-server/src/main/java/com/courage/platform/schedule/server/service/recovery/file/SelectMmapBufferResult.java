package com.courage.platform.schedule.server.service.recovery.file;

import java.nio.ByteBuffer;

public class SelectMmapBufferResult {

    private final long startOffset;

    private final ByteBuffer byteBuffer;
    protected MmapFile mappedFile;
    private int size;

    public SelectMmapBufferResult(long startOffset, ByteBuffer byteBuffer, int size, MmapFile mappedFile) {
        this.startOffset = startOffset;
        this.byteBuffer = byteBuffer;
        this.size = size;
        this.mappedFile = mappedFile;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int s) {
        this.size = s;
        this.byteBuffer.limit(this.size);
    }

    public MmapFile getMappedFile() {
        return mappedFile;
    }

    public synchronized void release() {
        if (this.mappedFile != null) {
            this.mappedFile.release();
            this.mappedFile = null;
        }
    }

    public long getStartOffset() {
        return startOffset;
    }

    public static void release(SelectMmapBufferResult sbr) {
        if (sbr != null) {
            sbr.release();
        }
    }
}
